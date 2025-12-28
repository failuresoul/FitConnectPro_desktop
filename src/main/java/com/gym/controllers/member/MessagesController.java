package com.gym.controllers.member;

import com.gym.dao.MessageDAO;
import com.gym.models.Conversation;
import com.gym.models.Member;
import com.gym.models.Message;
import com.gym.services.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessagesController {

    @FXML private Button backButton;
    @FXML private Label unreadCountLabel;
    @FXML private VBox conversationsContainer;
    @FXML private HBox chatHeader;
    @FXML private Label chatUserNameLabel;
    @FXML private Label chatUserTypeLabel;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private HBox messageInputArea;
    @FXML private TextArea messageInputField;
    @FXML private Button sendButton;

    private MessageDAO messageDAO;
    private Member currentMember;
    private int currentChatUserId;
    private String currentChatUserType;
    private String currentChatUserName;
    private Timeline autoRefreshTimeline;

    @FXML
    public void initialize() {
        messageDAO = new MessageDAO();
        currentMember = (Member) Session.getInstance().getCurrentUser();

        loadConversations();
        loadUnreadCount();
        startAutoRefresh();
    }

    private void loadConversations() {
        try {
            List<Conversation> conversations = messageDAO.getConversations(currentMember.getMemberId());
            conversationsContainer.getChildren().clear();

            if (conversations.isEmpty()) {
                Label noConvLabel = new Label("No conversations yet");
                noConvLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 20;");
                conversationsContainer.getChildren().add(noConvLabel);
                return;
            }

            for (Conversation conv : conversations) {
                VBox convBox = createConversationBox(conv);
                conversationsContainer.getChildren().add(convBox);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load conversations: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private VBox createConversationBox(Conversation conv) {
        VBox box = new VBox(5);
        box.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;");
        box.setOnMouseEntered(e -> box.setStyle("-fx-background-color: #e9ecef; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;"));
        box.setOnMouseExited(e -> box.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;"));
        box.setOnMouseClicked(e -> loadMessages(conv.getUserId(), conv.getUserType(), conv.getUserName()));

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(conv.getUserName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        Label typeLabel = new Label(conv.getUserType());
        typeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-background-color: " +
                (conv.getUserType().equals("TRAINER") ? "#3498db" : "#2ecc71") +
                "; -fx-padding: 2 8; -fx-background-radius: 10;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (conv.getUnreadCount() > 0) {
            Label unreadBadge = new Label(String.valueOf(conv.getUnreadCount()));
            unreadBadge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; " +
                    "-fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 10;");
            headerBox.getChildren().addAll(nameLabel, typeLabel, spacer, unreadBadge);
        } else {
            headerBox.getChildren().addAll(nameLabel, typeLabel, spacer);
        }

        Label lastMsgLabel = new Label(conv.getLastMessage());
        lastMsgLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        lastMsgLabel.setMaxWidth(Double.MAX_VALUE);
        lastMsgLabel.setWrapText(false);

        if (conv.getLastMessage().length() > 40) {
            lastMsgLabel.setText(conv.getLastMessage().substring(0, 40) + "...");
        }

        Label timeLabel = new Label(formatTime(conv.getLastMessageTime()));
        timeLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10px;");

        box.getChildren().addAll(headerBox, lastMsgLabel, timeLabel);
        return box;
    }

    private void loadMessages(int otherUserId, String userType, String userName) {
        try {
            currentChatUserId = otherUserId;
            currentChatUserType = userType;
            currentChatUserName = userName;

            chatHeader.setVisible(true);
            chatHeader.setManaged(true);
            messageInputArea.setVisible(true);
            messageInputArea.setManaged(true);

            chatUserNameLabel.setText(userName);
            chatUserTypeLabel.setText(userType);

            List<Message> messages = messageDAO.getConversation(
                    currentMember.getMemberId(), otherUserId, userType);

            messagesContainer.getChildren().clear();

            if (messages.isEmpty()) {
                Label noMsgLabel = new Label("No messages yet. Start the conversation!");
                noMsgLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
                VBox emptyBox = new VBox(noMsgLabel);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(50));
                messagesContainer.getChildren().add(emptyBox);
            } else {
                for (Message msg : messages) {
                    VBox messageBox = createMessageBox(msg);
                    messagesContainer.getChildren().add(messageBox);

                    // Mark as read if received
                    if (msg.getReceiverId() == currentMember.getMemberId() && !msg.isRead()) {
                        messageDAO.markAsRead(msg.getMessageId());
                    }
                }

                // Scroll to bottom
                Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
            }

            // Mark conversation as read
            messageDAO.markConversationAsRead(currentMember.getMemberId(), otherUserId, userType);
            loadUnreadCount();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load messages: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private VBox createMessageBox(Message msg) {
        boolean isSent = msg.getSenderId() == currentMember.getMemberId();

        VBox box = new VBox(5);
        box.setMaxWidth(400);
        box.setStyle("-fx-background-color: " + (isSent ? "#2980b9" : "#ecf0f1") +
                "; -fx-padding: 10; -fx-background-radius: 12;");

        if (isSent) {
            box.setAlignment(Pos.TOP_RIGHT);
            VBox.setMargin(box, new Insets(5, 0, 5, 50));
        } else {
            box.setAlignment(Pos.TOP_LEFT);
            VBox.setMargin(box, new Insets(5, 50, 5, 0));
        }

        Label msgLabel = new Label(msg.getMessageText());
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-text-fill: " + (isSent ? "white" : "#2c3e50") + "; -fx-font-size: 13px;");

        Label timeLabel = new Label(msg.getSentAt().format(DateTimeFormatter.ofPattern("MMM dd, hh:mm a")));
        timeLabel.setStyle("-fx-text-fill: " + (isSent ? "#ecf0f1" : "#7f8c8d") + "; -fx-font-size: 10px;");

        box.getChildren().addAll(msgLabel, timeLabel);

        HBox container = new HBox();
        if (isSent) {
            container.setAlignment(Pos.CENTER_RIGHT);
        } else {
            container.setAlignment(Pos.CENTER_LEFT);
        }
        container.getChildren().add(box);

        VBox wrapper = new VBox(container);
        return wrapper;
    }

    @FXML
    private void handleSendMessage() {
        String messageText = messageInputField.getText().trim();

        if (messageText.isEmpty()) {
            showAlert("Warning", "Please enter a message", Alert.AlertType.WARNING);
            return;
        }

        if (currentChatUserId == 0) {
            showAlert("Error", "No conversation selected", Alert.AlertType.ERROR);
            return;
        }

        try {
            boolean success = messageDAO.sendMessage(
                    currentMember.getMemberId(),
                    currentChatUserId,
                    "MEMBER",
                    currentChatUserType,
                    messageText
            );

            if (success) {
                messageInputField.clear();
                loadMessages(currentChatUserId, currentChatUserType, currentChatUserName);
                loadConversations();
            } else {
                showAlert("Error", "Failed to send message", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to send message: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadUnreadCount() {
        try {
            int unreadCount = messageDAO.getUnreadCount(currentMember.getMemberId());
            unreadCountLabel.setText(unreadCount + " unread");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            if (currentChatUserId != 0) {
                loadMessages(currentChatUserId, currentChatUserType, currentChatUserName);
            }
            loadConversations();
            loadUnreadCount();
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private String formatTime(java.time.LocalDateTime time) {
        java.time.Duration duration = java.time.Duration.between(time, java.time.LocalDateTime.now());

        if (duration.toMinutes() < 1) {
            return "Just now";
        } else if (duration.toHours() < 1) {
            return duration.toMinutes() + "m ago";
        } else if (duration.toDays() < 1) {
            return duration.toHours() + "h ago";
        } else if (duration.toDays() < 7) {
            return duration.toDays() + "d ago";
        } else {
            return time.format(DateTimeFormatter.ofPattern("MMM dd"));
        }
    }

    @FXML
    private void handleBack() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/member_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Member Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
