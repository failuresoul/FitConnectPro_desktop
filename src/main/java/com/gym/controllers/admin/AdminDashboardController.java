package com.gym.controllers.admin;

import com.gym.dao.StatisticsDAO;
import com.gym. models.Admin;
import com. gym.services.Session;
import com.gym.utils.DateUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx. scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage. Stage;

import java.io. IOException;
import java.text. NumberFormat;
import java.util.Locale;

public class AdminDashboardController {

    @FXML
    private Button logoutButton;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button memberManagementBtn;

    @FXML
    private Button trainerManagementBtn;

    @FXML
    private Button salaryManagementBtn;

    @FXML
    private Button applicationsBtn;

    @FXML
    private Button reportsBtn;

    @FXML
    private VBox contentArea;

    @FXML
    private Label totalMembersLabel;

    @FXML
    private Label totalTrainersLabel;

    @FXML
    private Label pendingApplicationsLabel;

    @FXML
    private Label monthlyRevenueLabel;

    @FXML
    private TableView recentActivityTable;

    @FXML
    private Label loggedInAdminLabel;

    @FXML
    private Label currentDateLabel;

    private StatisticsDAO statisticsDAO;

    public AdminDashboardController() {
        statisticsDAO = new StatisticsDAO();
    }

    @FXML
    public void initialize() {
        loadAdminInfo();
        loadStatistics();
        setupEventHandlers();
    }

    private void loadAdminInfo() {
        try {
            Admin admin = (Admin) Session.getInstance().getCurrentUser();
            if (admin != null) {
                loggedInAdminLabel.setText(admin.getFullName());
            } else {
                loggedInAdminLabel. setText("Unknown Admin");
            }
            currentDateLabel.setText(DateUtil.formatDate(DateUtil.getCurrentDate()));
        } catch (Exception e) {
            System.err.println("Error loading admin info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStatistics() {
        try {
            int totalMembers = statisticsDAO.getTotalMembers();
            totalMembersLabel.setText(String.valueOf(totalMembers));

            int totalTrainers = statisticsDAO.getTotalTrainers();
            totalTrainersLabel.setText(String.valueOf(totalTrainers));

            int pendingApps = statisticsDAO.getPendingApplicationsCount();
            pendingApplicationsLabel.setText(String.valueOf(pendingApps));

            double revenue = statisticsDAO.getMonthlyRevenue();
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            monthlyRevenueLabel.setText(currencyFormat.format(revenue));

            System.out.println("=== STATISTICS LOADED ===");
            System.out.println("Total Members: " + totalMembers);
            System.out.println("Total Trainers: " + totalTrainers);
            System. out.println("Pending Applications: " + pendingApps);
            System.out.println("Monthly Revenue: " + currencyFormat. format(revenue));
            System. out.println("=========================");

        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e. getMessage());
            e.printStackTrace();

            totalMembersLabel.setText("0");
            totalTrainersLabel.setText("0");
            pendingApplicationsLabel.setText("0");
            monthlyRevenueLabel.setText("$0. 00");
        }
    }

    private void setupEventHandlers() {
        logoutButton. setOnAction(event -> handleLogout());
        dashboardBtn. setOnAction(event -> loadDashboardHome());
        memberManagementBtn.setOnAction(event -> loadMemberManagement());
        trainerManagementBtn.setOnAction(event -> loadTrainerManagement());
        salaryManagementBtn.setOnAction(event -> loadSalaryManagement());
        applicationsBtn.setOnAction(event -> loadApplications());
        reportsBtn.setOnAction(event -> loadReports());
    }

    private void handleLogout() {
        try {
            Session.getInstance().logout();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login. fxml"));
            Parent loginRoot = loader.load();

            Scene loginScene = new Scene(loginRoot);
            Stage window = (Stage) logoutButton.getScene(). getWindow();
            window.setScene(loginScene);
            window.setTitle("Premium Gym Management System - Login");
            window. centerOnScreen();
            window. show();

            System.out. println("Admin logged out successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Logout Error", "Could not logout: " + e. getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadDashboardHome() {
        System.out. println("Loading Dashboard Home - Reloading entire dashboard...");

        try {
            // Reload the entire dashboard scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/admin_dashboard.fxml"));
            Parent dashboardRoot = loader.load();

            Scene dashboardScene = new Scene(dashboardRoot);
            Stage window = (Stage) dashboardBtn.getScene().getWindow();
            window.setScene(dashboardScene);
            window.setTitle("Admin Dashboard - Premium Gym Management System");
            window.centerOnScreen();
            window.show();

            System.out.println("Dashboard home loaded successfully!");

        } catch (IOException e) {
            System.err.println("Error loading dashboard home: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not load dashboard home: " + e.getMessage(), Alert. AlertType.ERROR);
        }
    }

    private void loadMemberManagement() {
        System.out.println("Loading Member Registration.. .");
        loadView("/fxml/admin/member_registration.fxml");
        setActiveButton(memberManagementBtn);
    }

    private void loadTrainerManagement() {
        System. out.println("Trainer Management clicked");
        showAlert("Coming Soon", "Trainer Management feature is under development.", Alert.AlertType. INFORMATION);
        setActiveButton(trainerManagementBtn);
    }

    private void loadSalaryManagement() {
        System.out.println("Salary Management clicked");
        showAlert("Coming Soon", "Salary Management feature is under development.", Alert.AlertType.INFORMATION);
        setActiveButton(salaryManagementBtn);
    }

    private void loadApplications() {
        System.out.println("Applications clicked");
        showAlert("Coming Soon", "Applications feature is under development.", Alert.AlertType.INFORMATION);
        setActiveButton(applicationsBtn);
    }

    private void loadReports() {
        System.out.println("Reports clicked");
        showAlert("Coming Soon", "Reports feature is under development.", Alert.AlertType.INFORMATION);
        setActiveButton(reportsBtn);
    }

    private void loadView(String fxmlPath) {
        try {
            System.out.println("Loading view: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            System.out.println("View loaded successfully!");

        } catch (IOException e) {
            System. err.println("Error loading view: " + fxmlPath);
            e.printStackTrace();

            showAlert("Error",
                    "Could not load view: " + fxmlPath + "\n\nError: " + e. getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void setActiveButton(Button activeButton) {
        // Remove active class from all buttons
        dashboardBtn.getStyleClass().remove("active-nav");
        memberManagementBtn. getStyleClass().remove("active-nav");
        trainerManagementBtn.getStyleClass().remove("active-nav");
        salaryManagementBtn.getStyleClass().remove("active-nav");
        applicationsBtn.getStyleClass().remove("active-nav");
        reportsBtn.getStyleClass().remove("active-nav");

        // Add active class to the clicked button
        if (! activeButton.getStyleClass().contains("active-nav")) {
            activeButton.getStyleClass().add("active-nav");
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert. setContentText(message);
        alert.showAndWait();
    }
}