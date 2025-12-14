package com.gym.controllers;

import com.gym.dao.AuthDAO;
import com. gym.models.Admin;
import com.gym.models.Member;
import com.gym.models. Trainer;
import com.gym.services.Session;
import com.gym.utils.ValidationUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx. scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private RadioButton adminRadio;

    @FXML
    private RadioButton trainerRadio;

    @FXML
    private RadioButton memberRadio;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private ToggleGroup userTypeGroup;

    private AuthDAO authDAO;

    public LoginController() {
        authDAO = new AuthDAO();
    }

    @FXML
    public void initialize() {
        trainerRadio. setSelected(true);

        loginButton.setOnAction(event -> handleLogin(event));
        forgotPasswordLink.setOnAction(event -> handleForgotPassword());
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (! ValidationUtil.isNotEmpty(username)) {
                ValidationUtil.showAlert("Validation Error",
                        "Username cannot be empty!",
                        Alert.AlertType. ERROR);
                return;
            }

            if (!ValidationUtil. isNotEmpty(password)) {
                ValidationUtil.showAlert("Validation Error",
                        "Password cannot be empty!",
                        Alert.AlertType.ERROR);
                return;
            }

            RadioButton selectedRadio = (RadioButton) userTypeGroup.getSelectedToggle();
            String userType = selectedRadio.getText(). toUpperCase();

            boolean loginSuccessful = false;
            Object loggedInUser = null;

            switch (userType) {
                case "ADMIN":
                    Admin admin = authDAO. authenticateAdmin(username, password);
                    if (admin != null) {
                        loggedInUser = admin;
                        loginSuccessful = true;
                        Session.getInstance().setCurrentUser(admin, "ADMIN");
                    }
                    break;

                case "TRAINER":
                    Trainer trainer = authDAO.authenticateTrainer(username, password);
                    if (trainer != null) {
                        loggedInUser = trainer;
                        loginSuccessful = true;
                        Session.getInstance().setCurrentUser(trainer, "TRAINER");
                    }
                    break;

                case "MEMBER":
                    Member member = authDAO.authenticateMember(username, password);
                    if (member != null) {
                        loggedInUser = member;
                        loginSuccessful = true;
                        Session.getInstance(). setCurrentUser(member, "MEMBER");
                    }
                    break;

                default:
                    ValidationUtil.showAlert("Error",
                            "Invalid user type selected!",
                            Alert.AlertType.ERROR);
                    return;
            }

            if (loginSuccessful) {
                ValidationUtil.showAlert("Login Successful",
                        "Welcome " + username + "!\n\n" +
                                "User Type: " + userType + "\n" +
                                "Authentication completed successfully.",
                        Alert.AlertType.INFORMATION);

                loadDashboard(event, userType);

                System.out.println("\n=== LOGIN SUCCESS ===");
                System.out.println("User: " + username);
                System.out.println("Type: " + userType);
                System.out.println("Session created successfully!");
                System. out.println("=====================\n");
            } else {
                ValidationUtil.showAlert("Login Failed",
                        "Invalid username or password!\nPlease check your credentials and try again.",
                        Alert. AlertType.ERROR);
            }

        } catch (Exception e) {
            ValidationUtil.showAlert("Error",
                    "An error occurred during login: " + e.getMessage(),
                    Alert. AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void loadDashboard(ActionEvent event, String userType) {
        try {
            String fxmlPath = "";

            switch (userType) {
                case "ADMIN":
                    fxmlPath = "/fxml/admin/admin_dashboard.fxml";
                    break;
                case "TRAINER":
                    fxmlPath = "/fxml/trainer/trainer_dashboard.fxml";
                    break;
                case "MEMBER":
                    fxmlPath = "/fxml/member/member_dashboard.fxml";
                    break;
            }

            System.out.println("Loading dashboard from: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent dashboardRoot = loader.load();

            Scene dashboardScene = new Scene(dashboardRoot);
            Stage window = (Stage) ((Node) event. getSource()).getScene().getWindow();
            window.setScene(dashboardScene);
            window. setTitle(userType + " Dashboard - Premium Gym Management System");
            window.centerOnScreen();
            window.show();

        } catch (IOException e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();

            ValidationUtil.showAlert("Error",
                    "Could not load dashboard.\n\n" +
                            "Please ensure the dashboard file exists at:\n" +
                            "src/main/resources/fxml/admin/admin_dashboard.fxml\n\n" +
                            "Error details: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleForgotPassword() {
        Alert alert = new Alert(Alert. AlertType.INFORMATION);
        alert.setTitle("Forgot Password");
        alert.setHeaderText("Password Recovery");
        alert. setContentText("Please contact the system administrator to reset your password.\n\n" +
                "Admin Email: fitconnectpro@gymsystem.com\n" +
                "Phone: 01746109666");
        alert.showAndWait();
    }
}