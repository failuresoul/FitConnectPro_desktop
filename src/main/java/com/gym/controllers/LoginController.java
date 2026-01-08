package com.gym.controllers;

import com.gym.dao.AuthDAO;
import com.gym.models.Admin;
import com.gym.models.Member;
import com.gym.models.Trainer;
import com.gym.services.Session;
import com.gym.utils.ValidationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private RadioButton adminRadio;
    @FXML private RadioButton trainerRadio;
    @FXML private RadioButton memberRadio;
    @FXML private Button loginButton;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private ToggleGroup userTypeGroup;

    private AuthDAO authDAO;

    public LoginController() {
        authDAO = new AuthDAO();
    }

    @FXML
    public void initialize() {
        trainerRadio.setSelected(true);
        loginButton.setOnAction(event -> handleLogin(event));
        forgotPasswordLink.setOnAction(event -> handleForgotPassword());
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (!ValidationUtil.isNotEmpty(username)) {
                ValidationUtil.showAlert("Validation Error",
                        "Username cannot be empty!",
                        Alert.AlertType.ERROR);
                return;
            }

            if (!ValidationUtil.isNotEmpty(password)) {
                ValidationUtil.showAlert("Validation Error",
                        "Password cannot be empty!",
                        Alert.AlertType.ERROR);
                return;
            }

            RadioButton selectedRadio = (RadioButton) userTypeGroup.getSelectedToggle();
            String userType = selectedRadio.getText().toUpperCase();

            boolean loginSuccessful = false;
            Object loggedInUser = null;

            switch (userType) {
                case "ADMIN":
                    Admin admin = authDAO.authenticateAdmin(username, password);
                    if (admin != null) {
                        loggedInUser = admin;
                        loginSuccessful = true;
                        Session.getInstance().setCurrentUser(admin, "ADMIN");
                        System.out.println("✅ Admin login successful: " + admin.getUsername());
                    }
                    break;

                case "TRAINER":
                    Trainer trainer = authDAO.authenticateTrainer(username, password);
                    if (trainer != null) {
                        loggedInUser = trainer;
                        loginSuccessful = true;
                        Session.getInstance().setCurrentUser(trainer, "TRAINER");
                        System.out.println("✅ Trainer login successful: " + trainer.getUsername());
                    }
                    break;

                case "MEMBER":
                    Member member = authDAO.authenticateMember(username, password);
                    if (member != null) {
                        loggedInUser = member;
                        loginSuccessful = true;
                        // Set both user object and complete session details
                        Session.getInstance().setCurrentUser(member, "MEMBER");
                        Session.getInstance().setSession(
                                member.getId(),
                                member.getName(),
                                "MEMBER",
                                member.getEmail()
                        );
                        System.out.println("✅ Member login successful: " + member.getUsername());
                        System.out.println("   Member ID: " + member.getId());
                        System.out.println("   Member Name: " + member.getName());
                        System.out.println("   Member Email: " + member.getEmail());
                    }
                    break;

                default:
                    ValidationUtil.showAlert("Error",
                            "Invalid user type selected!",
                            Alert.AlertType.ERROR);
                    return;
            }

            if (loginSuccessful) {
                System.out.println("\n=== LOGIN SUCCESS ===");
                System.out.println("User: " + username);
                System.out.println("Type: " + userType);
                System.out.println("Session created successfully!");
                System.out.println("=====================\n");

                loadDashboard(userType, event);

            } else {
                ValidationUtil.showAlert("Login Failed",
                        "Invalid username or password!\nPlease check your credentials and try again.",
                        Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("❌ Error during login: " + e.getMessage());
            e.printStackTrace();
            ValidationUtil.showAlert("Error",
                    "An error occurred during login: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void loadDashboard(String role, ActionEvent event) {
        try {
            String fxmlPath = "";

            switch (role) {
                case "ADMIN":
                    fxmlPath = "/fxml/admin/admin_dashboard.fxml";
                    System.out.println("📂 Loading ADMIN dashboard: " + fxmlPath);
                    break;
                case "TRAINER":
                    fxmlPath = "/fxml/trainer/trainer_dashboard.fxml";
                    System.out.println("📂 Loading TRAINER dashboard: " + fxmlPath);
                    break;
                case "MEMBER":
                    fxmlPath = "/fxml/member/member_dashboard.fxml";
                    System.out.println("📂 Loading MEMBER dashboard: " + fxmlPath);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown user type: " + role);
            }

            System.out.println("Loading dashboard from: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent dashboardRoot = loader.load();

            Stage window = (Stage) loginButton.getScene().getWindow();

            // Set a consistent starting size
            Scene scene = new Scene(dashboardRoot);
            window.setScene(scene);
            window.setWidth(1400);
            window.setHeight(850);
            window.centerOnScreen();

            window.setTitle(role + " Dashboard - FitConnect Pro");
            System.out.println("✅ Loaded " + role + " dashboard");

        } catch (IOException e) {
            System.err.println("❌ Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
            ValidationUtil.showAlert("Error",
                    "Could not load dashboard.\n\n" +
                            "Error details: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();

            ValidationUtil.showAlert("Error",
                    "Unexpected error: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleForgotPassword() {
        // Create custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Password Recovery");

        // Set the button types
        ButtonType resetButtonType = new ButtonType("Reset Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(resetButtonType, ButtonType.CANCEL);

        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");

        ComboBox<String> userTypeCombo = new ComboBox<>();
        userTypeCombo.getItems().addAll("ADMIN", "TRAINER", "MEMBER");
        userTypeCombo.setPromptText("Select User Type");
        userTypeCombo.setValue("MEMBER");

        grid.add(new Label("User Type:"), 0, 0);
        grid.add(userTypeCombo, 1, 0);
        grid.add(new Label("Username:"), 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("New Password:"), 0, 3);
        grid.add(newPasswordField, 1, 3);
        grid.add(new Label("Confirm Password:"), 0, 4);
        grid.add(confirmPasswordField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Request focus on username field by default
        javafx.application.Platform.runLater(() -> usernameField.requestFocus());

        // Show dialog and handle result
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == resetButtonType) {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String userType = userTypeCombo.getValue();

            // Validate inputs
            if (username.isEmpty() || email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                ValidationUtil.showAlert("Validation Error",
                        "All fields are required!",
                        Alert.AlertType.ERROR);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                ValidationUtil.showAlert("Validation Error",
                        "Passwords do not match!",
                        Alert.AlertType.ERROR);
                return;
            }

            if (newPassword.length() < 6) {
                ValidationUtil.showAlert("Validation Error",
                        "Password must be at least 6 characters long!",
                        Alert.AlertType.ERROR);
                return;
            }

            // Attempt to reset password
            try {
                boolean success = authDAO.resetPassword(username, email, newPassword, userType);

                if (success) {
                    ValidationUtil.showAlert("Success",
                            "Password has been reset successfully!\nYou can now login with your new password.",
                            Alert.AlertType.INFORMATION);
                } else {
                    ValidationUtil.showAlert("Reset Failed",
                            "Username and email do not match or user not found!",
                            Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                System.err.println("❌ Error resetting password: " + e.getMessage());
                e.printStackTrace();
                ValidationUtil.showAlert("Error",
                        "An error occurred while resetting password: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }
    }
}
