package com.gym. controllers.admin;

import com. gym.dao.ApplicationDAO;
import com.gym.dao.TrainerDAO;
import com.gym.models.Admin;
import com.gym.models. Trainer;
import com.gym. models.TrainerApplication;
import com.gym.services.Session;
import com.gym.utils. PasswordUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx. beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml. FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx. scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.security.SecureRandom;
import java. time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ApplicationsController {

    @FXML
    private TableView<TrainerApplication> applicationsTable;

    @FXML
    private TableColumn<TrainerApplication, Integer> idColumn;

    @FXML
    private TableColumn<TrainerApplication, String> nameColumn;

    @FXML
    private TableColumn<TrainerApplication, String> emailColumn;

    @FXML
    private TableColumn<TrainerApplication, Integer> experienceColumn;

    @FXML
    private TableColumn<TrainerApplication, String> specializationsColumn;

    @FXML
    private TableColumn<TrainerApplication, String> applicationDateColumn;

    @FXML
    private TableColumn<TrainerApplication, String> statusColumn;

    @FXML
    private TableColumn<TrainerApplication, Void> actionsColumn;

    @FXML
    private TextArea detailsArea;

    private ApplicationDAO applicationDAO;
    private TrainerDAO trainerDAO;
    private ObservableList<TrainerApplication> applicationsList;

    public ApplicationsController() {
        applicationDAO = new ApplicationDAO();
        trainerDAO = new TrainerDAO();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadApplications();
        setupSelectionListener();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getApplicationId()).asObject());

        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFullName()));

        emailColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail()));

        experienceColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getExperienceYears()).asObject());

        specializationsColumn.setCellValueFactory(cellData -> {
            List<String> specs = cellData.getValue().getSpecializations();
            String specsStr = (specs != null && !specs.isEmpty()) ? String.join(", ", specs) : "N/A";
            return new SimpleStringProperty(specsStr);
        });

        applicationDateColumn. setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getApplicationDate();
            String dateStr = (date != null) ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH: mm")) : "N/A";
            return new SimpleStringProperty(dateStr);
        });

        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));

        // Actions Column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View Details");
            private final Button approveButton = new Button("Approve");
            private final Button rejectButton = new Button("Reject");
            private final HBox buttons = new HBox(5, viewButton, approveButton, rejectButton);

            {
                viewButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-size: 11px;");
                approveButton. setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-size: 11px;");
                rejectButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding:  5 10; -fx-font-size: 11px;");

                viewButton.setOnAction(event -> {
                    TrainerApplication app = getTableView().getItems().get(getIndex());
                    viewApplicationDetails(app);
                });

                approveButton.setOnAction(event -> {
                    TrainerApplication app = getTableView().getItems().get(getIndex());
                    approveApplication(app);
                });

                rejectButton.setOnAction(event -> {
                    TrainerApplication app = getTableView().getItems().get(getIndex());
                    rejectApplication(app);
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

    private void loadApplications() {
        List<TrainerApplication> applications = applicationDAO.getPendingApplications();
        applicationsList = FXCollections.observableArrayList(applications);
        applicationsTable.setItems(applicationsList);

        System.out.println("✅ Loaded " + applications.size() + " pending applications");
    }

    private void setupSelectionListener() {
        applicationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayApplicationDetails(newSelection);
            }
        });
    }

    private void displayApplicationDetails(TrainerApplication app) {
        StringBuilder details = new StringBuilder();
        details.append("══════════════════════════════════════\n");
        details.append("        APPLICATION DETAILS\n");
        details.append("══════════════════════════════════════\n\n");
        details.append("Application ID: ").append(app.getApplicationId()).append("\n");
        details.append("Full Name: ").append(app.getFullName()).append("\n");
        details.append("Email: ").append(app.getEmail()).append("\n");
        details.append("Phone: ").append(app.getPhone()).append("\n");
        details.append("Age: ").append(app.getAge()).append("\n\n");
        details.append("Education: ").append(app.getEducation()).append("\n");
        details.append("Certifications: ").append(app.getCertifications()).append("\n");
        details.append("Experience: ").append(app.getExperienceYears()).append(" years\n");
        details.append("Specializations: ").append(String.join(", ", app.getSpecializations())).append("\n\n");
        details.append("Cover Letter:\n").append(app.getCoverLetter()).append("\n\n");
        details.append("Application Date: ").append(app.getApplicationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        details.append("Status: ").append(app.getStatus()).append("\n");

        detailsArea.setText(details.toString());
    }

    private void viewApplicationDetails(TrainerApplication app) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Application Details");
        alert.setHeaderText(app.getFullName() + "'s Application");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Email:"), 0, 0);
        grid.add(new Label(app.getEmail()), 1, 0);
        grid.add(new Label("Phone:"), 0, 1);
        grid.add(new Label(app.getPhone()), 1, 1);
        grid.add(new Label("Age:"), 0, 2);
        grid.add(new Label(String.valueOf(app. getAge())), 1, 2);
        grid.add(new Label("Education:"), 0, 3);
        grid.add(new Label(app.getEducation()), 1, 3);
        grid.add(new Label("Certifications:"), 0, 4);
        grid.add(new Label(app.getCertifications()), 1, 4);
        grid.add(new Label("Experience:"), 0, 5);
        grid.add(new Label(app.getExperienceYears() + " years"), 1, 5);
        grid.add(new Label("Specializations:"), 0, 6);
        grid.add(new Label(String.join(", ", app.getSpecializations())), 1, 6);

        TextArea coverLetterArea = new TextArea(app.getCoverLetter());
        coverLetterArea.setEditable(false);
        coverLetterArea.setWrapText(true);
        coverLetterArea.setPrefRowCount(5);
        grid.add(new Label("Cover Letter:"), 0, 7);
        grid.add(coverLetterArea, 1, 7);

        alert.getDialogPane().setContent(grid);
        alert.showAndWait();
    }

    private void approveApplication(TrainerApplication app) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation. setTitle("Approve Application");
        confirmation.setHeaderText("Approve " + app.getFullName() + "'s application?");
        confirmation.setContentText("This will create a trainer account and send credentials via email.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result. isPresent() && result.get() == ButtonType.OK) {

            // Create Trainer from Application
            Trainer trainer = new Trainer();
            trainer.setFullName(app.getFullName());
            trainer.setEmail(app.getEmail());
            trainer. setPhone(app.getPhone());
            trainer.setSpecializations(app.getSpecializations());
            trainer.setExperienceYears(app.getExperienceYears());
            trainer.setCertifications(app.getCertifications());
            trainer.setMaxClients(10);
            trainer.setAccountStatus("ACTIVE");
            trainer.setSalary(0.0); // Set default or ask admin

            // Generate username and password
            String username = generateUsername(app.getFullName());
            String password = generatePassword();

            trainer.setUsername(username);
            trainer.setPasswordHash(password);

            Admin admin = (Admin) Session.getInstance().getCurrentUser();
            trainer.setHiredByAdminId(admin. getAdminId());
            trainer.setHireDate(LocalDateTime.now());

            // Register trainer
            boolean trainerCreated = trainerDAO.registerTrainer(trainer);

            if (trainerCreated) {
                // Update application status
                boolean appApproved = applicationDAO.approveApplication(app.getApplicationId(), admin.getAdminId());

                if (appApproved) {
                    showCredentialsDialog(trainer, password);
                    loadApplications(); // Refresh list
                } else {
                    showAlert("Error", "Application approved but status update failed.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Error", "Failed to create trainer account.", Alert.AlertType.ERROR);
            }
        }
    }

    private void rejectApplication(TrainerApplication app) {
        Alert confirmation = new Alert(Alert.AlertType. CONFIRMATION);
        confirmation.setTitle("Reject Application");
        confirmation.setHeaderText("Reject " + app.getFullName() + "'s application?");
        confirmation.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmation. showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            Admin admin = (Admin) Session.getInstance().getCurrentUser();
            boolean success = applicationDAO.rejectApplication(app.getApplicationId(), admin.getAdminId());

            if (success) {
                showAlert("Success", "Application rejected.", Alert.AlertType.INFORMATION);
                loadApplications(); // Refresh list
            } else {
                showAlert("Error", "Failed to reject application.", Alert.AlertType. ERROR);
            }
        }
    }

    private String generateUsername(String fullName) {
        String[] parts = fullName.trim().toLowerCase().split("\\s+");
        String baseUsername = "";

        if (parts.length >= 2) {
            baseUsername = parts[0] + "." + parts[parts.length - 1];
        } else if (parts.length == 1) {
            baseUsername = parts[0];
        }

        int random = (int) (Math.random() * 100);
        return baseUsername + random;
    }

    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(10);

        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    private void showCredentialsDialog(Trainer trainer, String password) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("✅ Trainer Account Created");
        alert.setHeaderText("Application Approved!");
        alert.setContentText(
                "═══════════════════════════════\n" +
                        "TRAINER CREDENTIALS\n" +
                        "═══════════════════════════════\n\n" +
                        "Name: " + trainer.getFullName() + "\n" +
                        "Email: " + trainer. getEmail() + "\n\n" +
                        "USERNAME: " + trainer.getUsername() + "\n" +
                        "PASSWORD: " + password + "\n\n" +
                        "⚠️ Please save these credentials!\n" +
                        "Send them to the trainer via email."
        );
        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}