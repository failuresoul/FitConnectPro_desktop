package com.gym.controllers.trainer;

import com.gym.dao. MessageDAO;
import com.gym.dao.TrainerDAO;
import com.gym.models.*;
import com.gym.services.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessagesController {

    @FXML private ListView<ClientDetails> clientListView;
    @FXML private Label chatHeaderLabel;
    @FXML private VBox messagesContainer;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private TextArea messageInputArea;
    @FXML private Button sendBtn;

    private TrainerDAO trainerDAO;
    private MessageDAO messageDAO;
    private Trainer currentTrainer;
    private ClientDetails selectedClient;

    public MessagesController() {
        trainerDAO = new TrainerDAO();
        messageDAO = new MessageDAO();
    }

    @FXML
    public void initialize() {
        System.out.println("========================================");
        System.out.println("✅ MessagesController initialized");
        System.out.println("========================================");

        currentTrainer = (Trainer) Session.getInstance().getCurrentUser();

        if (currentTrainer == null) {
            System.err.println("❌ No trainer in session!");
            return;
        }

        loadClients();
        setupEventHandlers();
    }

    private void loadClients() {
        if (currentTrainer == null) return;

        try {
            List<ClientDetails> clients = trainerDAO.getMyAssignedClients(currentTrainer.getTrainerId());
            clientListView. setItems(FXCollections.observableArrayList(clients));

            // Custom cell factory
            clientListView.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(ClientDetails item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.getMemberName());
                        setStyle("-fx-font-size: 14px; -fx-padding: 10;");
                    }
                }
            });

            System.out.println("✅ Loaded " + clients.size() + " clients for messaging");

        } catch (Exception e) {
            System.err. println("❌ Error loading clients: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        // Client selection
        clientListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedClient = newVal;
                loadConversation();
            }
        });

        // Send button
        if (sendBtn != null) {
            sendBtn.setOnAction(e -> sendMessage());
        }

        // Enter key to send
        if (messageInputArea != null) {
            messageInputArea.setOnKeyPressed(event -> {
                if (event. getCode().toString().equals("ENTER") && !event.isShiftDown()) {
                    event.consume();
                    sendMessage();
                }
            });
        }
    }

    private void loadConversation() {
        if (selectedClient == null) return;

        try {
            chatHeaderLabel.setText("Chat with " + selectedClient.getMemberName());
            messagesContainer.getChildren().clear();

            List<Message> messages = messageDAO.getConversation(
                    currentTrainer.getTrainerId(),
                    selectedClient.getMemberId()
            );

            for (Message message : messages) {
                addMessageBubble(message);
            }

            // Mark as read
            messageDAO.markAsRead(currentTrainer.getTrainerId(), selectedClient.getMemberId());

            // Scroll to bottom
            messagesScrollPane.layout();
            messagesScrollPane. setVvalue(1.0);

        } catch (Exception e) {
            System.err.println("❌ Error loading conversation:  " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addMessageBubble(Message message) {
        boolean isSentByTrainer = message.getSenderType().equals("TRAINER");

        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(10));
        bubble.setMaxWidth(400);
        bubble.setStyle(
                isSentByTrainer
                        ? "-fx-background-color:  #3498db; -fx-background-radius: 15; -fx-text-fill: white;"
                        :  "-fx-background-color: #ecf0f1; -fx-background-radius: 15;"
        );

        Label messageLabel = new Label(message.getMessageText());
        messageLabel.setWrapText(true);
        messageLabel.setStyle(
                isSentByTrainer
                        ? "-fx-text-fill: white; -fx-font-size: 14px;"
                        : "-fx-text-fill: #2c3e50; -fx-font-size: 14px;"
        );

        String timeStr = message.getSentDate() != null
                ? message.getSentDate().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
                : "";
        Label timeLabel = new Label(timeStr);
        timeLabel.setStyle(
                isSentByTrainer
                        ?  "-fx-text-fill: #ecf0f1; -fx-font-size: 11px;"
                        : "-fx-text-fill: #7f8c8d; -fx-font-size: 11px;"
        );

        bubble.getChildren().addAll(messageLabel, timeLabel);

        HBox container = new HBox();
        container.setPadding(new Insets(5, 10, 5, 10));
        container.setAlignment(isSentByTrainer ? Pos.CENTER_RIGHT : Pos. CENTER_LEFT);
        container. getChildren().add(bubble);

        messagesContainer.getChildren().add(container);
    }

    private void sendMessage() {
        if (selectedClient == null) {
            showAlert("Error", "Please select a client to message", Alert.AlertType.WARNING);
            return;
        }

        String messageText = messageInputArea.getText().trim();
        if (messageText.isEmpty()) {
            return;
        }

        try {
            Message message = new Message();
            message.setSenderId(currentTrainer.getTrainerId());
            message.setReceiverId(selectedClient.getMemberId());
            message.setSenderType("TRAINER");
            message.setMessageText(messageText);

            boolean success = messageDAO.sendMessage(message);

            if (success) {
                messageInputArea.clear();
                loadConversation(); // Reload to show new message
                System.out.println("✅ Message sent to " + selectedClient.getMemberName());
            } else {
                showAlert("Error", "Failed to send message", Alert.AlertType. ERROR);
            }

        } catch (Exception e) {
            System.err.println("❌ Error sending message: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "An error occurred:  " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}