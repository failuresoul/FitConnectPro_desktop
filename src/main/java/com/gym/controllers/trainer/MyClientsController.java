package com.gym.controllers.trainer;

import com.gym.dao.TrainerDAO;
import com.gym.models.ClientDetails;
import com.gym. models.Member;
import com.gym.models.Trainer;
import com.gym.services.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx. collections.ObservableList;
import javafx.fxml. FXML;
import javafx. scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout. HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MyClientsController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private Label totalClientsLabel;
    @FXML private TableView<ClientDetails> clientsTable;
    @FXML private TableColumn<ClientDetails, String> memberNameColumn;
    @FXML private TableColumn<ClientDetails, String> goalColumn;
    @FXML private TableColumn<ClientDetails, String> assignmentDateColumn;
    @FXML private TableColumn<ClientDetails, String> lastWorkoutColumn;
    @FXML private TableColumn<ClientDetails, String> progressColumn;
    @FXML private TableColumn<ClientDetails, Void> actionsColumn;

    // Details Panel
    @FXML private VBox detailsPanel;
    @FXML private Label detailNameLabel;
    @FXML private VBox detailsContent;
    @FXML private Label detailName;
    @FXML private Label detailEmail;
    @FXML private Label detailPhone;
    @FXML private Label detailGoal;
    @FXML private Label detailMembership;
    @FXML private Label detailStatus;
    @FXML private Button viewFullProfileBtn;
    @FXML private Button createPlanBtn;
    @FXML private Button sendMessageBtn;

    private TrainerDAO trainerDAO;
    private Trainer currentTrainer;
    private ObservableList<ClientDetails> allClients;
    private ClientDetails selectedClient;

    public MyClientsController() {
        trainerDAO = new TrainerDAO();
    }

    @FXML
    public void initialize() {
        System.out.println("MyClientsController initialized");

        currentTrainer = (Trainer) Session.getInstance().getCurrentUser();

        setupTableColumns();
        setupEventHandlers();
        loadClients();
    }

    private void setupTableColumns() {
        memberNameColumn. setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData. getValue().getMemberName()));

        goalColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getGoal()));

        assignmentDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getAssignmentDate() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getAssignmentDate().format(DateTimeFormatter. ofPattern("MMM dd, yyyy"))
                );
            }
            return new SimpleStringProperty("N/A");
        });

        lastWorkoutColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getLastWorkoutDate() != null) {
                return new SimpleStringProperty(
                        cellData. getValue().getLastWorkoutDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                );
            }
            return new SimpleStringProperty("No workouts");
        });

        progressColumn. setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("%.1f%%", cellData.getValue().getProgressPercentage())));

        // Actions Column with Buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button planBtn = new Button("Plan");
            private final Button msgBtn = new Button("Message");
            private final HBox buttons = new HBox(5, viewBtn, planBtn, msgBtn);

            {
                viewBtn. setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10;");
                planBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 10;");
                msgBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 5 10;");

                viewBtn.setOnAction(e -> {
                    ClientDetails client = getTableView().getItems().get(getIndex());
                    viewClientProfile(client);
                });

                planBtn.setOnAction(e -> {
                    ClientDetails client = getTableView().getItems().get(getIndex());
                    createWorkoutPlan(client);
                });

                msgBtn.setOnAction(e -> {
                    ClientDetails client = getTableView().getItems().get(getIndex());
                    sendMessage(client);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }

    private void setupEventHandlers() {
        if (searchButton != null) {
            searchButton.setOnAction(e -> handleSearch());
        }

        if (clearButton != null) {
            clearButton.setOnAction(e -> handleClear());
        }

        // Row selection listener
        clientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayClientDetails(newSelection);
            }
        });

        if (viewFullProfileBtn != null) {
            viewFullProfileBtn. setOnAction(e -> {
                if (selectedClient != null) {
                    viewClientProfile(selectedClient);
                }
            });
        }

        if (createPlanBtn != null) {
            createPlanBtn.setOnAction(e -> {
                if (selectedClient != null) {
                    createWorkoutPlan(selectedClient);
                }
            });
        }

        if (sendMessageBtn != null) {
            sendMessageBtn. setOnAction(e -> {
                if (selectedClient != null) {
                    sendMessage(selectedClient);
                }
            });
        }
    }

    private void loadClients() {
        if (currentTrainer == null) {
            System.err.println("No trainer in session!");
            return;
        }

        try {
            List<ClientDetails> clients = trainerDAO.getMyAssignedClients(currentTrainer.getTrainerId());
            allClients = FXCollections.observableArrayList(clients);
            clientsTable.setItems(allClients);
            totalClientsLabel.setText("Total Clients: " + clients.size());

            System.out.println("Loaded " + clients.size() + " clients");

        } catch (Exception e) {
            System.err.println("Error loading clients: " + e. getMessage());
            e.printStackTrace();
        }
    }

    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            clientsTable.setItems(allClients);
            return;
        }

        ObservableList<ClientDetails> filtered = allClients.stream()
                .filter(client ->
                        client.getMemberName().toLowerCase().contains(searchText) ||
                                client.getEmail().toLowerCase().contains(searchText) ||
                                client.getGoal().toLowerCase().contains(searchText)
                )
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        clientsTable.setItems(filtered);
        totalClientsLabel.setText("Total Clients: " + filtered.size() + " (filtered)");
    }

    private void handleClear() {
        searchField.clear();
        clientsTable.setItems(allClients);
        totalClientsLabel. setText("Total Clients: " + allClients.size());
    }

    private void displayClientDetails(ClientDetails client) {
        selectedClient = client;

        detailsContent.setVisible(true);
        detailName.setText(client. getMemberName());
        detailEmail.setText(client.getEmail());
        detailPhone.setText(client.getPhone());
        detailGoal.setText(client.getGoal());
        detailMembership.setText(client.getMembershipType());
        detailStatus.setText(client.getAccountStatus());

        System.out.println("Displaying details for: " + client.getMemberName());
    }

    private void viewClientProfile(ClientDetails client) {
        System.out.println("Viewing profile for: " + client.getMemberName());

        Member fullDetails = trainerDAO.getClientDetails(client.getMemberId());

        if (fullDetails != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Client Profile");
            alert.setHeaderText(fullDetails.getFullName());
            alert.setContentText(
                    "Email: " + fullDetails.getEmail() + "\n" +
                            "Phone: " + fullDetails.getPhone() + "\n" +
                            "Membership: " + fullDetails.getMembershipType() + "\n" +
                            "Status: " + fullDetails.getAccountStatus()
            );
            alert.showAndWait();
        }
    }

    private void createWorkoutPlan(ClientDetails client) {
        System.out. println("Creating workout plan for: " + client.getMemberName());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Create Workout Plan");
        alert.setHeaderText("Create Plan for " + client.getMemberName());
        alert.setContentText("Workout plan creation feature coming soon!");
        alert.showAndWait();
    }

    private void sendMessage(ClientDetails client) {
        System.out. println("Sending message to: " + client.getMemberName());

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Send Message");
        dialog.setHeaderText("Send message to " + client.getMemberName());
        dialog.setContentText("Message:");

        dialog.showAndWait().ifPresent(message -> {
            System.out.println("Message sent: " + message);
            Alert confirm = new Alert(Alert.AlertType.INFORMATION);
            confirm. setTitle("Success");
            confirm.setContentText("Message sent successfully!");
            confirm.showAndWait();
        });
    }
}