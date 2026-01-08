package com.gym.controllers.trainer;

import com.gym.dao.TrainerStatisticsDAO;
import com.gym.models.Message;
import com.gym.models.Trainer;
import com.gym.services.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TrainerDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    @FXML private Button dashboardBtn;
    @FXML private Button myClientsBtn;
    @FXML private Button createWorkoutBtn;
    @FXML private Button createMealBtn;
    @FXML private Button setGoalsBtn;
    @FXML private Button progressReportsBtn;
    @FXML private Button messagesBtn;
    @FXML private Button profileBtn;
    @FXML private VBox contentArea;
    @FXML private Label totalClientsLabel;
    @FXML private Label todayWorkoutsLabel;
    @FXML private Label pendingPlansLabel;
    @FXML private Button quickCreateWorkoutBtn;
    @FXML private Button quickCreateMealBtn;
    @FXML private Button quickViewClientsBtn;
    @FXML private TableView<Message> recentMessagesTable;
    @FXML private TableColumn<Message, String> messageFromColumn;
    @FXML private TableColumn<Message, String> messageTextColumn;
    @FXML private TableColumn<Message, String> messageDateColumn;

    private TrainerStatisticsDAO statisticsDAO;
    private Trainer currentTrainer;

    public TrainerDashboardController() {
        statisticsDAO = new TrainerStatisticsDAO();
    }

    @FXML
    public void initialize() {
        System.out.println("========================================");
        System.out.println("✅ TrainerDashboardController initialized");
        System.out.println("========================================");

        loadTrainerInfo();
        loadStatistics();
        setupTableColumns();
        setupEventHandlers();
    }

    private void loadTrainerInfo() {
        currentTrainer = (Trainer) Session.getInstance().getCurrentUser();
        if (currentTrainer != null) {
            welcomeLabel.setText("Welcome, " + currentTrainer.getFullName());
            System.out.println("✅ Loaded trainer: " + currentTrainer.getFullName());
            System.out.println("✅ Trainer ID: " + currentTrainer.getTrainerId());
        } else {
            System.err.println("❌ No trainer found in session!");
        }
    }

    private void loadStatistics() {
        if (currentTrainer == null) {
            System.err.println("❌ Cannot load statistics - no trainer in session");
            return;
        }

        try {
            int totalClients = statisticsDAO.getMyClientsCount(currentTrainer.getTrainerId());
            if (totalClientsLabel != null) {
                totalClientsLabel.setText(String.valueOf(totalClients));
            }
            System.out.println("📊 Total clients: " + totalClients);

            int todayWorkouts = statisticsDAO.getTodayCompletedWorkouts(currentTrainer.getTrainerId());
            if (todayWorkoutsLabel != null) {
                todayWorkoutsLabel.setText(String.valueOf(todayWorkouts));
            }
            System.out.println("📊 Today's workouts: " + todayWorkouts);

            int pendingPlans = statisticsDAO.getPendingWorkoutPlans(currentTrainer.getTrainerId());
            if (pendingPlansLabel != null) {
                pendingPlansLabel.setText(String.valueOf(pendingPlans));
            }
            System.out.println("📊 Pending plans: " + pendingPlans);

            loadRecentMessages();
        } catch (Exception e) {
            System.err.println("❌ Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        if (messageFromColumn == null || messageTextColumn == null || messageDateColumn == null) {
            System.err.println("⚠️ Table columns not initialized");
            return;
        }

        messageFromColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSenderName()));

        messageTextColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMessageText()));

        messageDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getSentAt() != null) {
                String formatted = cellData.getValue().getSentAt()
                        .format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
                return new SimpleStringProperty(formatted);
            }
            return new SimpleStringProperty("N/A");
        });
    }

    private void loadRecentMessages() {
        if (currentTrainer == null) return;
        if (recentMessagesTable == null) {
            System.err.println("⚠️ Recent messages table not initialized");
            return;
        }

        try {
            List<Message> messages = statisticsDAO.getRecentMessages(currentTrainer.getTrainerId(), 5);
            ObservableList<Message> messagesList = FXCollections.observableArrayList(messages);
            recentMessagesTable.setItems(messagesList);
            System.out.println("💬 Loaded " + messages.size() + " recent messages");
        } catch (Exception e) {
            System.err.println("❌ Error loading messages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        if (logoutButton != null) {
            logoutButton.setOnAction(e -> handleLogout());
        }

        if (dashboardBtn != null) {
            dashboardBtn.setOnAction(e -> loadDashboardHome());
        }

        if (myClientsBtn != null) {
            myClientsBtn.setOnAction(e -> loadMyClients());
        }

        if (createWorkoutBtn != null) {
            createWorkoutBtn.setOnAction(e -> loadCreateWorkoutPlan());
        }

        if (createMealBtn != null) {
            createMealBtn.setOnAction(e -> loadCreateMealPlan());
        }

        if (setGoalsBtn != null) {
            setGoalsBtn.setOnAction(e -> loadSetDailyGoals());
        }

        if (progressReportsBtn != null) {
            progressReportsBtn.setOnAction(e -> loadProgressReports());
        }

        if (messagesBtn != null) {
            messagesBtn.setOnAction(e -> loadMessages());
        }

        if (profileBtn != null) {
            profileBtn.setOnAction(e -> loadProfile());
        }

        if (quickCreateWorkoutBtn != null) {
            quickCreateWorkoutBtn.setOnAction(e -> loadCreateWorkoutPlan());
        }

        if (quickCreateMealBtn != null) {
            quickCreateMealBtn.setOnAction(e -> loadCreateMealPlan());
        }

        if (quickViewClientsBtn != null) {
            quickViewClientsBtn.setOnAction(e -> loadMyClients());
        }
    }

    private void handleLogout() {
        try {
            Session.getInstance().logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginRoot = loader.load();

            Stage window = (Stage) logoutButton.getScene().getWindow();
            Scene currentScene = window.getScene();
            currentScene.setRoot(loginRoot);
            window.setTitle("Premium Gym Management System - Login");
            window.centerOnScreen();

            System.out.println("✅ Trainer logged out successfully!");
        } catch (IOException e) {
            System.err.println("❌ Logout error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDashboardHome() {
        System.out.println("⟳ Reloading dashboard home...");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/trainer/trainer_dashboard.fxml"));
            Parent dashboardRoot = loader.load();

            Stage window = (Stage) dashboardBtn.getScene().getWindow();
            Scene currentScene = window.getScene();
            currentScene.setRoot(dashboardRoot);
            window.setTitle("Trainer Dashboard - Premium Gym Management System");

            System.out.println("✅ Dashboard reloaded!");

        } catch (IOException e) {
            System.err.println("❌ Error reloading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMyClients() {
        System.out.println("📂 Loading My Clients...");
        loadView("/fxml/trainer/my_clients.fxml");
    }

    private void loadCreateWorkoutPlan() {
        System.out.println("📂 Loading Create Workout Plan...");
        loadView("/fxml/trainer/create_workout_plan.fxml");
    }

    private void loadCreateMealPlan() {
        System.out.println("📂 Loading Create Meal Plan...");
        loadView("/fxml/trainer/create_meal_plan.fxml");
    }

    private void loadSetDailyGoals() {
        System.out.println("📂 Loading Set Daily Goals...");
        loadView("/fxml/trainer/set_daily_goals.fxml");
    }

    private void loadProgressReports() {
        System.out.println("📂 Loading Progress Reports...");
        loadView("/fxml/trainer/progress_reports.fxml");
    }

    private void loadMessages() {
        System.out.println("📂 Loading Messages...");
        loadView("/fxml/trainer/messages.fxml");
    }

    private void loadProfile() {
        System.out.println("📂 Loading Trainer Profile...");
        loadView("/fxml/trainer/trainer_profile.fxml");
    }

    @FXML
    private void handleMessages() {
        loadView("/fxml/trainer/messages.fxml", "Messages");
    }

    @FXML
    private void handleMyProfile() {
        loadView("/fxml/trainer/trainer_profile.fxml", "My Profile");
    }

    private void loadView(String fxmlPath, String title) {
        try {
            System.out.println("📂 Loading " + title + "...");
            System.out.println("🔄 Loading view: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

            System.out.println("✅ View loaded successfully: " + fxmlPath);
        } catch (Exception e) {
            System.err.println("❌ Error loading view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            System.out.println("🔄 Loading view: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);
                System.out.println("✅ View loaded successfully: " + fxmlPath);
            } else {
                System.err.println("❌ contentArea is NULL!");
            }

        } catch (IOException e) {
            System.err.println("❌ Error loading view: " + fxmlPath);
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load view");
            alert.setContentText(fxmlPath + "\n\n" + e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showComingSoon(String feature, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(feature);
        alert.setHeaderText("Coming Soon!");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
