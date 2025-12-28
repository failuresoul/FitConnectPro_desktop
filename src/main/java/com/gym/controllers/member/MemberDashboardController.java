package com.gym.controllers.member;

import com.gym.dao.MemberDashboardDAO;
import com.gym.models.Member;
import com.gym.services.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class MemberDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label trainerNameLabel;
    @FXML
    private Label trainerSpecLabel;
    @FXML
    private Button messageTrainerBtn;
    @FXML
    private VBox goalsContainer;
    @FXML
    private VBox workoutContainer;
    @FXML
    private VBox mealContainer;
    @FXML
    private Label streakLabel;
    @FXML
    private Label totalWorkoutsLabel;
    @FXML
    private Label weightLabel;
    @FXML
    private VBox activityFeedContainer;
    @FXML
    private Button startWorkoutBtn;
    @FXML
    private Button viewMealDetailsBtn;
    @FXML
    private Button friendsBtn;
    @FXML
    private Button logoutBtn;

    private MemberDashboardDAO dashboardDAO;
    private int memberId;
    private String memberName;
    private Integer trainerId;

    @FXML
    public void initialize() {
        dashboardDAO = new MemberDashboardDAO();
        loadMemberInfo();
        loadDateInfo();
        loadAllDashboardData();
    }

    private void loadMemberInfo() {
        Session session = Session.getInstance();
        memberId = session.getUserId() != null ? session.getUserId() : 0;
        memberName = session.getUsername();

        if (memberName != null && !memberName.isEmpty()) {
            welcomeLabel.setText("Welcome back, " + memberName + "! 👋");
        } else {
            welcomeLabel.setText("Welcome back! 👋");
        }
    }

    private void loadDateInfo() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        dateLabel.setText(today.format(formatter));
    }

    private void loadAllDashboardData() {
        // Load in background to avoid UI freeze
        new Thread(() -> {
            try {
                loadTrainerInfo();
                loadTodaysGoals();
                loadTodaysWorkout();
                loadTodaysMealPlan();
                loadQuickStats();
                loadRecentActivities();
            } catch (Exception e) {
                System.err.println("Error loading dashboard data: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void loadTrainerInfo() {
        Map<String, Object> trainerInfo = dashboardDAO.getTrainerInfo(memberId);

        Platform.runLater(() -> {
            if (!trainerInfo.isEmpty()) {
                trainerId = (Integer) trainerInfo.get("id");
                trainerNameLabel.setText((String) trainerInfo.get("name"));
                trainerSpecLabel.setText((String) trainerInfo.get("specialization"));
                messageTrainerBtn.setDisable(false);
            } else {
                trainerNameLabel.setText("Not Assigned");
                trainerSpecLabel.setText("");
                messageTrainerBtn.setDisable(true);
            }
        });
    }

    private void loadTodaysGoals() {
        List<Map<String, Object>> goals = dashboardDAO.getTodaysGoals(memberId);

        Platform.runLater(() -> {
            goalsContainer.getChildren().clear();

            if (goals.isEmpty()) {
                Label noGoalsLabel = new Label("No goals set for today");
                noGoalsLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-style: italic;");
                goalsContainer.getChildren().add(noGoalsLabel);
                return;
            }

            for (Map<String, Object> goal : goals) {
                VBox goalBox = new VBox(8);
                goalBox.setStyle("-fx-background-color: #0c0707; -fx-padding: 12; -fx-background-radius: 8;");

                Label goalLabel = new Label((String) goal.get("goalType"));
                goalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                ProgressBar progressBar = new ProgressBar();
                progressBar.setPrefWidth(Double.MAX_VALUE);
                progressBar.setPrefHeight(8);

                double target = (Double) goal.get("targetValue");
                double current = (Double) goal.get("currentValue");
                double progress = target > 0 ? current / target : 0;
                progressBar.setProgress(Math.min(progress, 1.0));

                String unit = (String) goal.get("unit");
                Label progressLabel = new Label(String.format("%.0f / %.0f %s (%.0f%%)",
                        current, target, unit, progress * 100));
                progressLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ffffff;");

                goalBox.getChildren().addAll(goalLabel, progressBar, progressLabel);
                goalsContainer.getChildren().add(goalBox);
            }
        });
    }

    private void loadTodaysWorkout() {
        List<Map<String, Object>> exercises = dashboardDAO.getTodaysWorkout(memberId);

        Platform.runLater(() -> {
            workoutContainer.getChildren().clear();

            if (exercises.isEmpty()) {
                Label noWorkoutLabel = new Label("No workout plan for today");
                noWorkoutLabel.setStyle("-fx-text-fill: #000000; -fx-font-style: italic;");
                workoutContainer.getChildren().add(noWorkoutLabel);
                startWorkoutBtn.setDisable(true);
                return;
            }

            for (Map<String, Object> exercise : exercises) {
                HBox exerciseBox = new HBox(10);
                exerciseBox.setStyle("-fx-background-color: #030405; -fx-padding: 10; -fx-background-radius: 6;");

                String name = (String) exercise.get("name");
                int sets = (Integer) exercise.get("sets");
                String reps = (String) exercise.get("reps"); // Changed from Integer to String
                int duration = (Integer) exercise.get("duration");

                String details = sets > 0 ? String.format("%s - %d sets × %s reps", name, sets, reps)
                        : String.format("%s - %d minutes", name, duration);

                Label exerciseLabel = new Label("• " + details);
                exerciseLabel.setWrapText(true);
                exerciseLabel.setStyle("-fx-font-size: 12px;");

                exerciseBox.getChildren().add(exerciseLabel);
                workoutContainer.getChildren().add(exerciseBox);
            }

            startWorkoutBtn.setDisable(false);
        });
    }

    private void loadTodaysMealPlan() {
        try {
            Member member = (Member) Session.getInstance().getCurrentUser();
            List<Map<String, Object>> meals = dashboardDAO.getTodaysMealPlan(member.getMemberId());

            mealContainer.getChildren().clear();

            if (meals.isEmpty()) {
                Label noMealsLabel = new Label("No meals planned for today");
                noMealsLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
                mealContainer.getChildren().add(noMealsLabel);
            } else {
                for (Map<String, Object> meal : meals) {
                    VBox mealCard = new VBox(5);
                    mealCard.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5;");

                    HBox headerBox = new HBox(10);
                    headerBox.setStyle("-fx-alignment: CENTER_LEFT;");

                    Label typeLabel = new Label(meal.get("mealType").toString());
                    typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");

                    Label sourceLabel = new Label("(" + meal.get("source").toString() + ")");
                    sourceLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

                    Label caloriesLabel = new Label(meal.get("calories") + " kcal");
                    caloriesLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    headerBox.getChildren().addAll(typeLabel, sourceLabel, spacer, caloriesLabel);

                    Label foodsLabel = new Label(meal.get("foodItems").toString());
                    foodsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
                    foodsLabel.setWrapText(true);

                    mealCard.getChildren().addAll(headerBox, foodsLabel);
                    mealContainer.getChildren().add(mealCard);
                }
            }

            System.out.println("✅ Today's meal plan loaded: " + meals.size() + " meals");

        } catch (Exception e) {
            System.err.println("❌ Error loading today's meal plan: " + e.getMessage());
            e.printStackTrace();

            Label errorLabel = new Label("Error loading meal plan");
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
            mealContainer.getChildren().clear();
            mealContainer.getChildren().add(errorLabel);
        }
    }

    private void loadQuickStats() {
        Map<String, Object> stats = dashboardDAO.getQuickStats(memberId);

        Platform.runLater(() -> {
            streakLabel.setText(String.valueOf(stats.getOrDefault("streak", 0)));
            totalWorkoutsLabel.setText(String.valueOf(stats.getOrDefault("totalWorkouts", 0)));

            Object weightObj = stats.get("weight");
            if (weightObj != null) {
                double weight = (Double) weightObj;
                weightLabel.setText(weight > 0 ? String.format("%.1f kg", weight) : "N/A");
            } else {
                weightLabel.setText("N/A");
            }
        });
    }

    private void loadRecentActivities() {
        List<Map<String, Object>> activities = dashboardDAO.getRecentActivities(memberId);

        Platform.runLater(() -> {
            activityFeedContainer.getChildren().clear();

            if (activities.isEmpty()) {
                Label noActivityLabel = new Label("No recent activities");
                noActivityLabel.setStyle("-fx-text-fill: #050505; -fx-font-style: italic;");
                activityFeedContainer.getChildren().add(noActivityLabel);
                return;
            }

            for (Map<String, Object> activity : activities) {
                HBox activityBox = new HBox(15);
                activityBox.setStyle("-fx-background-color: #000000; -fx-padding: 12; -fx-background-radius: 6;");

                VBox contentBox = new VBox(3);
                Label descLabel = new Label((String) activity.get("description"));
                descLabel.setWrapText(true);
                descLabel.setStyle("-fx-font-size: 12px;");

                Label dateLabel = new Label(activity.get("date").toString());
                dateLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 10px;");

                contentBox.getChildren().addAll(descLabel, dateLabel);
                activityBox.getChildren().add(contentBox);

                activityFeedContainer.getChildren().add(activityBox);
            }
        });
    }

    // Action Handlers
    @FXML
    private void handleMessageTrainer() {
        showAlert(Alert.AlertType.INFORMATION, "Message Trainer",
                "Messaging feature coming soon!\nYou can contact your trainer at their email.");
    }

    @FXML
    private void handleStartWorkout() {
        // Navigate to workout plans view
        showWorkoutPlans();
    }

    @FXML
    private void handleViewMealDetails() {
        showMealPlans();
    }

    @FXML
    private void handleMemberDirectory() {
        try {
            System.out.println("✅ Navigated to Member Directory view");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/member_directory.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Member Directory - FitConnect Pro");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load Member Directory: " + e.getMessage());
        }
    }

    @FXML
    private void handleFriends() {
        try {
            System.out.println("✅ Navigated to Friends view");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/friends.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Friends - FitConnect Pro");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load Friends page: " + e.getMessage());
        }
    }

    @FXML
    private void showFriends() {
        // Delegate to handleFriends
        handleFriends();
    }

    @FXML
    private void showMessages() {
        handleMessages();
    }

    // Navigation Methods
    @FXML
    private void showDashboard() {
        // Already on dashboard
    }

    @FXML
    private void showWorkoutPlans() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/view_workout_plan.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();

            // Reuse existing scene to maintain window size
            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);

            stage.setTitle("FitConnectPro - My Workout Plans");
            System.out.println("✅ Navigated to Workout Plans view");
        } catch (Exception e) {
            System.err.println("❌ Error loading workout plans view: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to load workout plans view: " + e.getMessage());
        }
    }

    @FXML
    private void showLogWorkout() {
        showAlert(Alert.AlertType.INFORMATION, "Log Workout", "Feature coming soon!");
    }

    @FXML
    private void showMealPlans() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/view_meal_plan.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();

            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);

            stage.setTitle("FitConnectPro - My Meal Plans");
            System.out.println("✅ Navigated to Meal Plans view");
        } catch (Exception e) {
            System.err.println("❌ Error loading meal plans view: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to load meal plans view: " + e.getMessage());
        }
    }

    @FXML
    private void showLogMeal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/log_meal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();

            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);

            stage.setTitle("FitConnectPro - Log Meal");
            System.out.println("✅ Navigated to Log Meal view");
        } catch (Exception e) {
            System.err.println("❌ Error loading log meal view: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to load log meal view: " + e.getMessage());
        }
    }

    @FXML
    private void showWaterTracker() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/water_tracker.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();

            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);

            stage.setTitle("FitConnectPro - Water Tracker");
            System.out.println("✅ Navigated to Water Tracker view");
        } catch (Exception e) {
            System.err.println("❌ Error loading water tracker view: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to load water tracker: " + e.getMessage());
        }
    }

    @FXML
    private void showProgress() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/member_progress.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();

            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);

            stage.setTitle("FitConnectPro - My Progress");
            System.out.println("✅ Navigated to Progress view");
        } catch (Exception e) {
            System.err.println("❌ Error loading progress view: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to load progress view: " + e.getMessage());
        }
    }

    @FXML
    private void showSocial() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/member_directory.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();

            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);

            stage.setTitle("FitConnectPro - Member Directory");
            System.out.println("✅ Navigated to Member Directory view");
        } catch (Exception e) {
            System.err.println("❌ Error loading member directory view: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to load member directory: " + e.getMessage());
        }
    }

    @FXML
    private void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/member_profile.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();

            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);

            stage.setTitle("FitConnectPro - My Profile");
            System.out.println("✅ Navigated to Profile view");
        } catch (Exception e) {
            System.err.println("❌ Error loading profile view: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to load profile view: " + e.getMessage());
        }
    }

    @FXML
    private void handleMessages() {
        System.out.println("📂 Loading Messages view...");
        loadView("/fxml/member/messages.fxml", "Messages");
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("FitConnectPro - Login");
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadView(String fxmlPath, String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();

            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);

            stage.setTitle("FitConnectPro - " + viewName);
            System.out.println("✅ Navigated to " + viewName + " view");
        } catch (Exception e) {
            System.err.println("❌ Error loading " + viewName + " view: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to load " + viewName + " view: " + e.getMessage());
        }
    }
}
