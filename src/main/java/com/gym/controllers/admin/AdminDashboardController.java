package com.gym.controllers.admin;

import com.gym.dao.StatisticsDAO;
import com.gym.dao.PaymentDAO;
import com.gym.models.Admin;
import com.gym.services.Session;
import com.gym.utils.DateUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Currency;

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
    private Button reportsBtn;

    @FXML
    private VBox contentArea;

    @FXML
    private Label totalMembersLabel;

    @FXML
    private Label totalTrainersLabel;

    @FXML
    private Label monthlyRevenueLabel;

    @FXML
    private TableView recentActivityTable;

    @FXML
    private Label loggedInAdminLabel;

    @FXML
    private Label currentDateLabel;

    // Quick Action Buttons
    @FXML
    private Button addMemberButton;

    @FXML
    private Button addTrainerButton;

    @FXML
    private Button assignTrainerButton;

    private StatisticsDAO statisticsDAO;
    private PaymentDAO paymentDAO;

    public AdminDashboardController() {
        statisticsDAO = new StatisticsDAO();
        paymentDAO = new PaymentDAO();
    }

    @FXML
    public void initialize() {
        System.out.println("AdminDashboardController initialized");
        loadAdminInfo();
        loadStatistics();
        setupEventHandlers();
        autoGenerateMonthlyPayments();
    }

    private void loadAdminInfo() {
        try {
            Admin admin = (Admin) Session.getInstance().getCurrentUser();
            if (admin != null && loggedInAdminLabel != null) {
                loggedInAdminLabel.setText(admin.getFullName());
            } else if (loggedInAdminLabel != null) {
                loggedInAdminLabel.setText("Unknown Admin");
            }

            if (currentDateLabel != null) {
                currentDateLabel.setText(DateUtil.formatDate(DateUtil.getCurrentDate()));
            }
        } catch (Exception e) {
            System.err.println("Error loading admin info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStatistics() {
        try {
            int totalMembers = statisticsDAO.getTotalMembers();
            if (totalMembersLabel != null) {
                totalMembersLabel.setText(String.valueOf(totalMembers));
            }

            int totalTrainers = statisticsDAO.getTotalTrainers();
            if (totalTrainersLabel != null) {
                totalTrainersLabel.setText(String.valueOf(totalTrainers));
            }

            double revenue = statisticsDAO.getMonthlyRevenue();
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            currencyFormat.setCurrency(Currency.getInstance("BDT"));
            if (monthlyRevenueLabel != null) {
                monthlyRevenueLabel.setText(currencyFormat.format(revenue));
            }

            System.out.println("=== STATISTICS LOADED ===");
            System.out.println("Total Members: " + totalMembers);
            System.out.println("Total Trainers: " + totalTrainers);
            System.out.println("Monthly Revenue: " + currencyFormat.format(revenue));
            System.out.println("=========================");

        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();

            if (totalMembersLabel != null) totalMembersLabel.setText("0");
            if (totalTrainersLabel != null) totalTrainersLabel.setText("0");
            if (monthlyRevenueLabel != null) monthlyRevenueLabel.setText("$0.00");
        }
    }

    private void setupEventHandlers() {
        // Navigation buttons
        if (logoutButton != null) {
            logoutButton.setOnAction(event -> handleLogout());
        }

        if (dashboardBtn != null) {
            dashboardBtn.setOnAction(event -> loadDashboardHome());
        }

        if (memberManagementBtn != null) {
            memberManagementBtn.setOnAction(event -> loadMemberManagement());
        }

        if (trainerManagementBtn != null) {
            trainerManagementBtn.setOnAction(event -> loadTrainerManagement());
        }

        if (salaryManagementBtn != null) {
            salaryManagementBtn.setOnAction(event -> loadSalaryManagement());
        }

        if (reportsBtn != null) {
            reportsBtn.setOnAction(event -> loadReports());
        }

        // Quick Action Buttons
        if (addMemberButton != null) {
            addMemberButton.setOnAction(event -> openMemberRegistration());
        }

        if (addTrainerButton != null) {
            addTrainerButton.setOnAction(event -> loadTrainerRegistration());
        }

        if (assignTrainerButton != null) {
            assignTrainerButton.setOnAction(event -> openAssignTrainerDialog());
        }
    }

    private void handleLogout() {
        try {
            Session.getInstance().logout();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginRoot = loader.load();

            Scene loginScene = new Scene(loginRoot);
            Stage window = (Stage) logoutButton.getScene().getWindow();
            window. setScene(loginScene);
            window. setTitle("Premium Gym Management System - Login");
            window.centerOnScreen();
            window.show();

            System.out.println("Admin logged out successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Logout Error", "Could not logout:  " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadDashboardHome() {
        System.out.println("Loading Dashboard Home - Reloading entire dashboard...");

        try {
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
            showAlert("Error", "Could not load dashboard home: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadMemberManagement() {
        System.out.println("Loading Member Management...");
        loadView("/fxml/admin/member_management.fxml");
        if (memberManagementBtn != null) {
            setActiveButton(memberManagementBtn);
        }
    }

    private void loadTrainerRegistration() {
        System.out.println("Loading Trainer Registration.. .");
        loadView("/fxml/admin/trainer_registration.fxml");
    }

    private void loadTrainerManagement() {
        System.out.println("Loading Trainer Management...");
        loadView("/fxml/admin/trainer_management.fxml");
        if (trainerManagementBtn != null) {
            setActiveButton(trainerManagementBtn);
        }
    }

    private void loadSalaryManagement() {
        System.out.println("Loading Salary Management.. .");
        loadView("/fxml/admin/salary_management.fxml");
        if (salaryManagementBtn != null) {
            setActiveButton(salaryManagementBtn);
        }
    }

    private void loadReports() {
        System.out.println("Loading Reports...");
        loadView("/fxml/admin/salary_reports.fxml");
        if (reportsBtn != null) {
            setActiveButton(reportsBtn);
        }
    }

    private void loadView(String fxmlPath) {
        try {
            System. out.println("Loading view: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
                System.out.println("View loaded successfully!");
            }

        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlPath);
            e.printStackTrace();

            showAlert("Error",
                    "Could not load view: " + fxmlPath + "\n\nError: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private void setActiveButton(Button activeButton) {
        // Remove active class from all buttons
        if (dashboardBtn != null) dashboardBtn.getStyleClass().remove("active-nav");
        if (memberManagementBtn != null) memberManagementBtn.getStyleClass().remove("active-nav");
        if (trainerManagementBtn != null) trainerManagementBtn.getStyleClass().remove("active-nav");
        if (salaryManagementBtn != null) salaryManagementBtn.getStyleClass().remove("active-nav");
        if (reportsBtn != null) reportsBtn.getStyleClass().remove("active-nav");

        // Add active class to the clicked button
        if (activeButton != null && !activeButton.getStyleClass().contains("active-nav")) {
            activeButton.getStyleClass().add("active-nav");
        }
    }

    private void openAssignTrainerDialog() {
        try {
            System.out.println("Opening Assign Trainer dialog.. .");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/assign_trainer_dialog.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage. setTitle("Assign Trainer to Member");
            stage.setScene(new Scene(root, 600, 500));
            stage. setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage. showAndWait();

            System.out.println("Assign trainer dialog closed");

        } catch (IOException e) {
            System. err.println("Error opening assign trainer dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open assign trainer dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openMemberRegistration() {
        try {
            System.out.println("Opening Member Registration dialog...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/member_registration.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Member");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadStatistics();
            System.out.println("Member registration dialog closed");

        } catch (IOException e) {
            System.err.println("Error opening member registration dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open member registration dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void autoGenerateMonthlyPayments() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                paymentDAO.generateMonthlyPayments();
                System.out.println("✅ Monthly payments check completed");
            } catch (Exception e) {
                System.err.println("❌ Error generating monthly payments: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
