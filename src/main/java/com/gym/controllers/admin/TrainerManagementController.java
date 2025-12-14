package com.gym.controllers.admin;

import com.gym.dao.TrainerDAO;
import com.gym.models.Trainer;
import javafx.beans.property.SimpleIntegerProperty;
import javafx. beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml. FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene. Scene;
import javafx.scene.control.*;
import javafx. scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrainerManagementController {

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button clearSearchButton;

    @FXML
    private Button addTrainerButton;

    @FXML
    private TableView<Trainer> trainersTable;

    @FXML
    private TableColumn<Trainer, Integer> idColumn;

    @FXML
    private TableColumn<Trainer, String> nameColumn;

    @FXML
    private TableColumn<Trainer, String> specializationsColumn;

    @FXML
    private TableColumn<Trainer, Integer> experienceColumn;

    @FXML
    private TableColumn<Trainer, String> salaryColumn;

    @FXML
    private TableColumn<Trainer, Integer> clientsColumn;

    @FXML
    private TableColumn<Trainer, String> statusColumn;

    @FXML
    private TableColumn<Trainer, Void> actionsColumn;

    private TrainerDAO trainerDAO;
    private ObservableList<Trainer> trainersList;
    private ObservableList<Trainer> filteredList;

    public TrainerManagementController() {
        trainerDAO = new TrainerDAO();
    }

    @FXML
    public void initialize() {
        System.out.println("🔄 TrainerManagementController initialize() called");
        setupTableColumns();
        loadTrainers();
        setupEventHandlers();
    }

    private void setupTableColumns() {
        System.out.println("🔄 Setting up table columns.. .");

        // ID Column
        idColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getTrainerId()).asObject());

        // Name Column
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFullName()));

        // Specializations Column
        specializationsColumn.setCellValueFactory(cellData -> {
            List<String> specs = cellData.getValue().getSpecializations();
            String specsStr = (specs != null && !specs.isEmpty()) ? String.join(", ", specs) : "N/A";
            return new SimpleStringProperty(specsStr);
        });

        // Experience Column
        experienceColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getExperienceYears()).asObject());

        // Salary Column
        salaryColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("$" + String.format("%.2f", cellData.getValue().getSalary())));

        // Clients Count Column
        clientsColumn.setCellValueFactory(cellData -> {
            int count = trainerDAO.getAssignedClientsCount(cellData.getValue().getTrainerId());
            return new SimpleIntegerProperty(count).asObject();
        });

        // Status Column
        statusColumn. setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAccountStatus()));

        // Actions Column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button viewButton = new Button("View Clients");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, editButton, viewButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-size: 11px; -fx-cursor: hand;");
                viewButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-size: 11px; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill:  white; -fx-padding: 5 10; -fx-font-size: 11px; -fx-cursor: hand;");

                editButton.setOnAction(event -> {
                    Trainer trainer = getTableView().getItems().get(getIndex());
                    handleEditTrainer(trainer);
                });

                viewButton.setOnAction(event -> {
                    Trainer trainer = getTableView().getItems().get(getIndex());
                    handleViewClients(trainer);
                });

                deleteButton.setOnAction(event -> {
                    Trainer trainer = getTableView().getItems().get(getIndex());
                    handleDeleteTrainer(trainer);
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

        System.out.println("✅ Table columns setup complete");
    }

    private void loadTrainers() {
        System.out.println("\n🔄 Loading trainers from database...");

        try {
            List<Trainer> trainers = trainerDAO.getAllTrainers();

            System.out.println("=== TRAINERS LOADED ===");
            System.out.println("Total trainers retrieved: " + trainers.size());

            if (trainers.isEmpty()) {
                System. out.println("⚠️  No trainers found in database!");
            } else {
                for (Trainer trainer : trainers) {
                    int clientCount = trainerDAO.getAssignedClientsCount(trainer.getTrainerId());
                    System.out.println("- ID: " + trainer.getTrainerId() +
                            " | Name: " + trainer.getFullName() +
                            " | Email: " + trainer.getEmail() +
                            " | Clients: " + clientCount);
                }
            }
            System.out.println("=======================\n");

            trainersList = FXCollections. observableArrayList(trainers);
            filteredList = FXCollections. observableArrayList(trainers);
            trainersTable.setItems(filteredList);

            System.out.println("✅ Trainers loaded into table:  " + filteredList.size() + " items");

        } catch (Exception e) {
            System. err.println("❌ Error loading trainers: " + e. getMessage());
            e.printStackTrace();

            // Show error alert
            showAlert("Error", "Failed to load trainers:  " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupEventHandlers() {
        searchButton.setOnAction(event -> handleSearch());
        clearSearchButton. setOnAction(event -> handleClearSearch());
        addTrainerButton.setOnAction(event -> handleAddTrainer());
    }

    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();

        if (searchText.isEmpty()) {
            filteredList. setAll(trainersList);
        } else {
            List<Trainer> filtered = trainersList.stream()
                    .filter(trainer ->
                            trainer.getFullName().toLowerCase().contains(searchText) ||
                                    trainer.getEmail().toLowerCase().contains(searchText) ||
                                    String.join(",", trainer.getSpecializations()).toLowerCase().contains(searchText))
                    .collect(Collectors.toList());

            filteredList. setAll(filtered);
        }

        System.out.println("🔍 Search results: " + filteredList.size() + " trainers found");
    }

    private void handleClearSearch() {
        searchField.clear();
        filteredList.setAll(trainersList);
        System.out.println("🔄 Search cleared");
    }

    private void handleAddTrainer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/trainer_registration.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Trainer");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Reload trainers after adding
            System.out.println("🔄 Reloading trainers after registration...");
            loadTrainers();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open trainer registration form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleEditTrainer(Trainer trainer) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Trainer");
        alert.setHeaderText("Edit Trainer:  " + trainer.getFullName());
        alert.setContentText(
                "Trainer ID: " + trainer.getTrainerId() + "\n" +
                        "Email: " + trainer.getEmail() + "\n" +
                        "Phone: " + trainer.getPhone() + "\n" +
                        "Salary: $" + trainer.getSalary() + "\n" +
                        "Status: " + trainer.getAccountStatus() + "\n\n" +
                        "Edit functionality will be implemented in future updates."
        );
        alert.showAndWait();
    }

    private void handleViewClients(Trainer trainer) {
        int clientCount = trainerDAO.getAssignedClientsCount(trainer.getTrainerId());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Assigned Clients");
        alert.setHeaderText(trainer.getFullName() + "'s Clients");
        alert.setContentText(
                "Total Assigned Clients: " + clientCount + "\n\n" +
                        "Client list view will be implemented in future updates."
        );
        alert.showAndWait();
    }

    private void handleDeleteTrainer(Trainer trainer) {
        int clientCount = trainerDAO.getAssignedClientsCount(trainer. getTrainerId());

        if (clientCount > 0) {
            showAlert("Cannot Delete",
                    "This trainer has " + clientCount + " assigned clients.\n\n" +
                            "Please reassign or remove clients before deleting this trainer.",
                    Alert.AlertType. WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation. setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Trainer: " + trainer.getFullName());
        confirmation.setContentText(
                "Are you sure you want to delete this trainer?\n\n" +
                        "This action cannot be undone."
        );

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = trainerDAO.deleteTrainer(trainer.getTrainerId());

            if (success) {
                showAlert("Success", "Trainer deleted successfully!", Alert.AlertType. INFORMATION);
                loadTrainers();
            } else {
                showAlert("Error", "Could not delete trainer.", Alert.AlertType.ERROR);
            }
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