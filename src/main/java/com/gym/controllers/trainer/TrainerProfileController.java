package com.gym.controllers.trainer;

import com.gym.dao. TrainerDAO;
import com. gym.models.Trainer;
import com.gym.services.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Arrays;
import java.util.Collections;

public class TrainerProfileController {

    // Profile Information
    @FXML private Label usernameLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField specializationsField;
    @FXML private Spinner<Integer> experienceSpinner;
    @FXML private TextArea certificationsArea;
    @FXML private Label accountStatusLabel;
    @FXML private Label hireDateLabel;
    @FXML private Label currentClientsLabel;
    @FXML private Label maxClientsLabel;
    @FXML private Button saveProfileBtn;

    // Change Password
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordBtn;

    private TrainerDAO trainerDAO;
    private Trainer currentTrainer;

    public TrainerProfileController() {
        trainerDAO = new TrainerDAO();
    }

    @FXML
    public void initialize() {
        System.out.println("========================================");
        System.out.println("✅ TrainerProfileController initialized");
        System.out.println("========================================");

        currentTrainer = (Trainer) Session.getInstance().getCurrentUser();

        if (currentTrainer == null) {
            System.err.println("❌ No trainer in session!");
            return;
        }

        setupSpinner();
        loadProfile();
        setupEventHandlers();
    }

    private void setupSpinner() {
        experienceSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0, 1)
        );
        experienceSpinner.setEditable(true);
    }

    private void loadProfile() {
        if (currentTrainer == null) return;

        try {
            // Reload fresh data from database
            Trainer trainer = trainerDAO. getTrainerById(currentTrainer.getTrainerId());

            if (trainer != null) {
                currentTrainer = trainer;

                // Display profile information
                usernameLabel.setText(trainer. getUsername());
                fullNameField.setText(trainer.getFullName());
                emailField.setText(trainer.getEmail());
                phoneField.setText(trainer.getPhone() != null ? trainer.getPhone() : "");

                // FIXED: Handle specializations properly
                if (trainer.getSpecializations() != null && !trainer.getSpecializations().isEmpty()) {
                    specializationsField. setText(String.join(", ", trainer.getSpecializations()));
                } else {
                    specializationsField.setText("");
                }

                experienceSpinner.getValueFactory().setValue(trainer.getExperienceYears());
                certificationsArea.setText(trainer. getCertifications() != null ? trainer.getCertifications() : "");

                accountStatusLabel.setText(trainer.getAccountStatus());
                accountStatusLabel.setStyle(
                        trainer.getAccountStatus().equals("ACTIVE")
                                ? "-fx-text-fill:  #27ae60; -fx-font-weight: bold;"
                                : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
                );

                // FIXED: Handle hireDate properly (could be LocalDateTime or String)
                String hireDate = "N/A";
                if (trainer.getHireDate() != null) {
                    hireDate = trainer.getHireDate().toString();
                    if (hireDate.contains("T")) {
                        hireDate = hireDate.substring(0, hireDate.indexOf("T"));
                    }
                }
                hireDateLabel. setText(hireDate);

                currentClientsLabel.setText(String.valueOf(trainer.getCurrentClients()));
                maxClientsLabel. setText(String.valueOf(trainer. getMaxClients()));

                System.out.println("✅ Profile loaded successfully");
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        if (saveProfileBtn != null) {
            saveProfileBtn.setOnAction(e -> saveProfile());
        }
        if (changePasswordBtn != null) {
            changePasswordBtn.setOnAction(e -> changePassword());
        }
    }

    private void saveProfile() {
        try {
            // Validation
            String fullName = fullNameField.getText().trim();
            String email = emailField.getText().trim();

            if (fullName.isEmpty() || email.isEmpty()) {
                showAlert("Validation Error", "Full name and email are required", Alert.AlertType. WARNING);
                return;
            }

            // Update trainer object
            currentTrainer.setFullName(fullName);
            currentTrainer.setEmail(email);
            currentTrainer.setPhone(phoneField.getText().trim());

            // FIXED: Convert comma-separated String to List<String>
            String specializationsText = specializationsField.getText().trim();
            if (specializationsText.isEmpty()) {
                currentTrainer.setSpecializations(Collections.emptyList());
            } else {
                // Split by comma and trim each element
                currentTrainer.setSpecializations(
                        Arrays.asList(specializationsText.split("\\s*,\\s*"))
                );
            }

            currentTrainer.setExperienceYears(experienceSpinner.getValue());
            currentTrainer.setCertifications(certificationsArea.getText().trim());

            // Save to database
            boolean success = trainerDAO.updateTrainerProfile(currentTrainer);

            if (success) {
                // Update session with the modified trainer object
                Session.getInstance().setCurrentUser(currentTrainer);

                showAlert("Success", "Profile updated successfully!", Alert.AlertType.INFORMATION);
                System.out. println("✅ Profile saved");
            } else {
                showAlert("Error", "Failed to update profile", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("❌ Error saving profile: " + e. getMessage());
            e.printStackTrace();
            showAlert("Error", "An error occurred: " + e. getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void changePassword() {
        try {
            String oldPassword = oldPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Validation
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showAlert("Validation Error", "All password fields are required", Alert.AlertType.WARNING);
                return;
            }

            if (newPassword.length() < 6) {
                showAlert("Validation Error", "New password must be at least 6 characters", Alert.AlertType. WARNING);
                return;
            }

            if (!newPassword. equals(confirmPassword)) {
                showAlert("Validation Error", "New passwords do not match", Alert. AlertType.WARNING);
                return;
            }

            // Change password
            boolean success = trainerDAO.changePassword(
                    currentTrainer.getTrainerId(),
                    oldPassword,
                    newPassword
            );

            if (success) {
                showAlert("Success", "Password changed successfully!", Alert.AlertType.INFORMATION);

                // Clear fields
                oldPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();

                System. out.println("✅ Password changed");
            } else {
                showAlert("Error", "Failed to change password.  Please check your old password.", Alert. AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("❌ Error changing password:  " + e.getMessage());
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