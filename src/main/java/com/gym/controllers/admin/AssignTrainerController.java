package com.gym.controllers.admin;

import com.gym.dao.MemberDAO;
import com.gym.dao.TrainerDAO;
import com.gym.models.Admin;
import com.gym.models.Member;
import com.gym.models.Trainer;
import com.gym.services.Session;
import javafx.fxml.FXML;
import javafx.scene. control.*;
import javafx.stage.Stage;
import javafx. util.StringConverter;

import java.time.LocalDate;
import java. util.List;

public class AssignTrainerController {

    @FXML
    private ComboBox<Member> memberComboBox;

    @FXML
    private ComboBox<Trainer> trainerComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private Button assignButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label memberInfoLabel;

    @FXML
    private Label trainerInfoLabel;

    @FXML
    private Label statusLabel;

    private MemberDAO memberDAO;
    private TrainerDAO trainerDAO;

    public AssignTrainerController() {
        memberDAO = new MemberDAO();
        trainerDAO = new TrainerDAO();
    }

    @FXML
    public void initialize() {
        System.out.println("🔄 AssignTrainerController initialized");

        setupComboBoxes();
        loadData();
        setupEventHandlers();

        // Set default start date to today
        startDatePicker.setValue(LocalDate.now());
    }

    private void setupComboBoxes() {
        // Member ComboBox converter
        memberComboBox.setConverter(new StringConverter<Member>() {
            @Override
            public String toString(Member member) {
                if (member == null) return "";
                return member.getFullName() + " (" + member.getEmail() + ")";
            }

            @Override
            public Member fromString(String string) {
                return null;
            }
        });

        // Trainer ComboBox converter
        trainerComboBox.setConverter(new StringConverter<Trainer>() {
            @Override
            public String toString(Trainer trainer) {
                if (trainer == null) return "";
                int currentClients = trainerDAO.getAssignedClientsCount(trainer.getTrainerId());
                return trainer.getFullName() + " (" + currentClients + "/" + trainer.getMaxClients() + " clients)";
            }

            @Override
            public Trainer fromString(String string) {
                return null;
            }
        });

        // Add listeners to show info when selected
        memberComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Integer assignedTrainerId = memberDAO.getAssignedTrainerId(newVal.getMemberId());
                if (assignedTrainerId != null) {
                    memberInfoLabel.setText("Currently assigned to trainer ID: " + assignedTrainerId);
                    memberInfoLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 11px;");
                } else {
                    memberInfoLabel.setText("Not currently assigned to any trainer");
                    memberInfoLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
                }
            }
        });

        trainerComboBox. valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int currentClients = trainerDAO.getAssignedClientsCount(newVal.getTrainerId());
                int maxClients = newVal.getMaxClients();

                if (currentClients >= maxClients) {
                    trainerInfoLabel.setText("Trainer at maximum capacity!");
                    trainerInfoLabel. setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
                    assignButton.setDisable(true);
                } else {
                    trainerInfoLabel.setText("Available slots: " + (maxClients - currentClients));
                    trainerInfoLabel. setStyle("-fx-text-fill: #27ae60; -fx-font-size: 11px;");
                    assignButton. setDisable(false);
                }
            }
        });
    }

    private void loadData() {
        try {
            // Load unassigned members (and those needing reassignment)
            List<Member> members = memberDAO.getUnassignedMembers();
            memberComboBox.getItems().setAll(members);

            // Load available trainers (not at max capacity)
            List<Trainer> trainers = trainerDAO.getAvailableTrainers();
            trainerComboBox.getItems().setAll(trainers);

            System.out.println("✅ Loaded " + members.size() + " members");
            System.out.println("✅ Loaded " + trainers.size() + " available trainers");

        } catch (Exception e) {
            System.err.println("❌ Error loading data:  " + e.getMessage());
            e.printStackTrace();
            showStatus("Error loading data: " + e.getMessage(), "error");
        }
    }

    private void setupEventHandlers() {
        assignButton. setOnAction(e -> handleAssign());
        cancelButton.setOnAction(e -> closeWindow());
    }

    private void handleAssign() {
        // Clear previous status
        statusLabel.setText("");

        // Validate inputs
        if (memberComboBox.getValue() == null) {
            showStatus("Please select a member!", "error");
            return;
        }

        if (trainerComboBox.getValue() == null) {
            showStatus("Please select a trainer!", "error");
            return;
        }

        if (startDatePicker.getValue() == null) {
            showStatus("Please select a start date!", "error");
            return;
        }

        Member selectedMember = memberComboBox. getValue();
        Trainer selectedTrainer = trainerComboBox. getValue();
        LocalDate startDate = startDatePicker.getValue();

        // Check if trainer is at capacity
        int currentClients = trainerDAO.getAssignedClientsCount(selectedTrainer.getTrainerId());
        if (currentClients >= selectedTrainer. getMaxClients()) {
            showStatus("Trainer is at maximum capacity!", "error");
            return;
        }

        // Get current admin
        Admin admin = (Admin) Session.getInstance().getCurrentUser();
        if (admin == null) {
            showStatus("Session expired.  Please login again.", "error");
            return;
        }

        // Assign trainer to member
        boolean success = trainerDAO.assignTrainerToMember(
                selectedTrainer. getTrainerId(),
                selectedMember.getMemberId(),
                admin.getAdminId(),
                startDate
        );

        if (success) {
            showAlert("Success",
                    "Trainer assigned successfully!\n\n" +
                            "Member: " + selectedMember.getFullName() + "\n" +
                            "Trainer: " + selectedTrainer.getFullName() + "\n" +
                            "Start Date: " + startDate,
                    Alert.AlertType.INFORMATION);

            closeWindow();
        } else {
            showStatus("Failed to assign trainer.  Please try again.", "error");
        }
    }

    private void showStatus(String message, String type) {
        statusLabel.setText(message);
        if ("error".equals(type)) {
            statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 12px;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 12px;");
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) assignButton.getScene().getWindow();
        stage.close();
    }
}