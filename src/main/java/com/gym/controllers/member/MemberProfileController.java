package com.gym.controllers.member;

import com.gym.dao.MemberDAO;
import com.gym.models.Member;
import com.gym.services.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class MemberProfileController {

    @FXML private Button backButton;

    // Personal Information
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField addressField;

    // Health Information
    @FXML private TextField currentWeightField;
    @FXML private TextField targetWeightField;
    @FXML private TextField heightField;
    @FXML private ComboBox<String> goalComboBox;

    // Membership Information
    @FXML private Label memberIdLabel;
    @FXML private Label joinDateLabel;
    @FXML private Label membershipTypeLabel;
    @FXML private Label statusLabel;

    private MemberDAO memberDAO;
    private Member currentMember;

    @FXML
    public void initialize() {
        memberDAO = new MemberDAO();
        currentMember = (Member) Session.getInstance().getCurrentUser();

        setupComboBoxes();
        loadProfileData();
    }

    private void setupComboBoxes() {
        genderComboBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        goalComboBox.setItems(FXCollections.observableArrayList(
            "Weight Loss", "Muscle Gain", "General Fitness", "Endurance", "Flexibility"
        ));
    }

    private void loadProfileData() {
        if (currentMember == null) return;

        try {
            // Personal Information
            fullNameField.setText(currentMember.getFullName());
            emailField.setText(currentMember.getEmail());
            phoneField.setText(currentMember.getPhone());

            if (currentMember.getDateOfBirth() != null) {
                dobPicker.setValue(currentMember.getDateOfBirth());
            }

            if (currentMember.getGender() != null && !currentMember.getGender().isEmpty()) {
                genderComboBox.setValue(currentMember.getGender());
            }

            // Health Information - Load from database
            loadHealthInfo();

            // Membership Information
            memberIdLabel.setText(String.valueOf(currentMember.getMemberId()));

            if (currentMember.getMembershipStart() != null) {
                joinDateLabel.setText(currentMember.getMembershipStart().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
            } else {
                joinDateLabel.setText("--");
            }

            membershipTypeLabel.setText(currentMember.getMembershipType() != null ? currentMember.getMembershipType() : "--");
            statusLabel.setText(currentMember.getAccountStatus() != null ? currentMember.getAccountStatus() : "ACTIVE");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load profile data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadHealthInfo() {
        try {
            // Get profile data from Member_Profiles table
            Map<String, Object> profileData = memberDAO.getMemberProfile(currentMember.getMemberId());

            if (profileData != null && !profileData.isEmpty()) {
                Object currentWeightObj = profileData.get("current_weight");
                if (currentWeightObj != null) {
                    double currentWeight = ((Number) currentWeightObj).doubleValue();
                    currentWeightField.setText(currentWeight > 0 ? String.valueOf(currentWeight) : "");
                }

                Object targetWeightObj = profileData.get("target_weight");
                if (targetWeightObj != null) {
                    double targetWeight = ((Number) targetWeightObj).doubleValue();
                    targetWeightField.setText(targetWeight > 0 ? String.valueOf(targetWeight) : "");
                }

                Object heightObj = profileData.get("height");
                if (heightObj != null) {
                    double height = ((Number) heightObj).doubleValue();
                    heightField.setText(height > 0 ? String.valueOf(height) : "");
                }

                String goal = (String) profileData.get("fitness_goal");
                if (goal != null && !goal.isEmpty()) {
                    goalComboBox.setValue(goal);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading health info: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveProfile() {
        try {
            // Validate inputs
            if (fullNameField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Full name is required", Alert.AlertType.WARNING);
                return;
            }

            if (emailField.getText().trim().isEmpty()) {
                showAlert("Validation Error", "Email is required", Alert.AlertType.WARNING);
                return;
            }

            // Update member object
            currentMember.setFullName(fullNameField.getText().trim());
            currentMember.setEmail(emailField.getText().trim());
            currentMember.setPhone(phoneField.getText().trim());
            currentMember.setDateOfBirth(dobPicker.getValue());
            currentMember.setGender(genderComboBox.getValue());

            // Update in database
            boolean success = memberDAO.updateMemberProfile(currentMember);

            // Update health information
            if (success) {
                success = updateHealthInfo();
            }

            if (success) {
                // Update session
                Session.getInstance().setCurrentUser(currentMember);
                showAlert("Success", "Profile updated successfully!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Failed to update profile", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean updateHealthInfo() {
        try {
            double currentWeight = parseDouble(currentWeightField.getText(), 0.0);
            double targetWeight = parseDouble(targetWeightField.getText(), 0.0);
            double height = parseDouble(heightField.getText(), 0.0);
            String goal = goalComboBox.getValue();

            return memberDAO.updateHealthInfo(currentMember.getMemberId(), currentWeight, targetWeight, height, goal);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleChangePassword() {
        try {
            // Create password change dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Change Password");
            dialog.setHeaderText("Enter your new password");

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            PasswordField currentPasswordField = new PasswordField();
            currentPasswordField.setPromptText("Current Password");
            PasswordField newPasswordField = new PasswordField();
            newPasswordField.setPromptText("New Password");
            PasswordField confirmPasswordField = new PasswordField();
            confirmPasswordField.setPromptText("Confirm Password");

            grid.add(new Label("Current Password:"), 0, 0);
            grid.add(currentPasswordField, 1, 0);
            grid.add(new Label("New Password:"), 0, 1);
            grid.add(newPasswordField, 1, 1);
            grid.add(new Label("Confirm Password:"), 0, 2);
            grid.add(confirmPasswordField, 1, 2);

            dialog.getDialogPane().setContent(grid);

            dialog.showAndWait().ifPresent(response -> {
                if (response == saveButtonType) {
                    String currentPwd = currentPasswordField.getText();
                    String newPwd = newPasswordField.getText();
                    String confirmPwd = confirmPasswordField.getText();

                    if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                        showAlert("Validation Error", "All fields are required", Alert.AlertType.WARNING);
                        return;
                    }

                    if (!newPwd.equals(confirmPwd)) {
                        showAlert("Validation Error", "New passwords do not match", Alert.AlertType.WARNING);
                        return;
                    }

                    if (newPwd.length() < 6) {
                        showAlert("Validation Error", "Password must be at least 6 characters", Alert.AlertType.WARNING);
                        return;
                    }

                    boolean success = memberDAO.changePassword(currentMember.getMemberId(), currentPwd, newPwd);

                    if (success) {
                        showAlert("Success", "Password changed successfully!", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Error", "Failed to change password. Current password may be incorrect.", Alert.AlertType.ERROR);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleReset() {
        loadProfileData();
    }

    @FXML
    private void handleBack() {
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

    private double parseDouble(String value, double defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
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

