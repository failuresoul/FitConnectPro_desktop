package com.gym.controllers.member;

import com.gym.dao.ProgressDAO;
import com.gym.models.Member;
import com.gym.services.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class MemberProgressController {

    @FXML private Button backButton;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TabPane progressTabPane;

    // Workout Stats Tab
    @FXML private Label totalWorkoutsLabel;
    @FXML private Label avgWorkoutsPerWeekLabel;
    @FXML private Label totalCaloriesBurnedLabel;
    @FXML private Label totalActiveTimeLabel;
    @FXML private PieChart workoutTypePieChart;
    @FXML private BarChart<String, Number> workoutFrequencyChart;

    // Nutrition Analysis Tab
    @FXML private Label avgDailyCaloriesLabel;
    @FXML private Label avgDailyProteinLabel;
    @FXML private Label avgDailyCarbsLabel;
    @FXML private Label avgDailyFatsLabel;
    @FXML private Label loggingConsistencyLabel;
    @FXML private PieChart macrosPieChart;
    @FXML private BarChart<String, Number> nutritionGoalsChart;

    // Goal Achievement Tab
    @FXML private Label dailyGoalRateLabel;
    @FXML private Label weeklyGoalRateLabel;
    @FXML private Label monthlyGoalRateLabel;
    @FXML private Label overallProgressLabel;
    @FXML private VBox achievementTimelineContainer;

    private ProgressDAO progressDAO;
    private int memberId;

    @FXML
    public void initialize() {
        progressDAO = new ProgressDAO();
        Member member = (Member) Session.getInstance().getCurrentUser();
        memberId = member.getMemberId();

        // Set default date range (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));

        loadAllProgressData();
    }

    @FXML
    private void handleRefresh() {
        loadAllProgressData();
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

    private void loadAllProgressData() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null) {
            showAlert("Error", "Please select both start and end dates.", Alert.AlertType.ERROR);
            return;
        }

        if (start.isAfter(end)) {
            showAlert("Error", "Start date must be before end date.", Alert.AlertType.ERROR);
            return;
        }

        loadWorkoutStats(start, end);
        loadNutritionAnalysis(start, end);
        loadGoalAchievement();
    }

    private void loadWorkoutStats(LocalDate start, LocalDate end) {
        Map<String, Integer> stats = progressDAO.getWorkoutStatistics(memberId, start, end);

        int totalWorkouts = stats.getOrDefault("totalWorkouts", 0);
        int totalCalories = stats.getOrDefault("totalCalories", 0);
        int totalDuration = stats.getOrDefault("totalDuration", 0);

        long weeks = (end.toEpochDay() - start.toEpochDay()) / 7;
        double avgPerWeek = weeks > 0 ? (double) totalWorkouts / weeks : totalWorkouts;

        totalWorkoutsLabel.setText(String.valueOf(totalWorkouts));
        avgWorkoutsPerWeekLabel.setText(String.format("%.1f", avgPerWeek));
        totalCaloriesBurnedLabel.setText(String.valueOf(totalCalories));
        totalActiveTimeLabel.setText(totalDuration + " min");

        // Workout Type Pie Chart
        workoutTypePieChart.getData().clear();
        stats.entrySet().stream()
                .filter(e -> e.getKey().startsWith("type_"))
                .forEach(e -> {
                    String type = e.getKey().substring(5);
                    workoutTypePieChart.getData().add(new PieChart.Data(type, e.getValue()));
                });

        if (workoutTypePieChart.getData().isEmpty()) {
            workoutTypePieChart.getData().add(new PieChart.Data("No Data", 1));
        }

        // Workout Frequency Bar Chart
        XYChart.Series<String, Number> frequencySeries = new XYChart.Series<>();
        frequencySeries.setName("Workouts");

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            int count = stats.getOrDefault("day_" + i, 0);
            frequencySeries.getData().add(new XYChart.Data<>(days[i], count));
        }

        workoutFrequencyChart.getData().clear();
        workoutFrequencyChart.getData().add(frequencySeries);
    }

    private void loadNutritionAnalysis(LocalDate start, LocalDate end) {
        Map<String, Double> stats = progressDAO.getNutritionStatistics(memberId, start, end);

        double avgCalories = stats.getOrDefault("avgCalories", 0.0);
        double avgProtein = stats.getOrDefault("avgProtein", 0.0);
        double avgCarbs = stats.getOrDefault("avgCarbs", 0.0);
        double avgFats = stats.getOrDefault("avgFats", 0.0);
        double consistency = stats.getOrDefault("loggingConsistency", 0.0);

        avgDailyCaloriesLabel.setText(String.format("%.0f kcal", avgCalories));
        avgDailyProteinLabel.setText(String.format("%.1f g", avgProtein));
        avgDailyCarbsLabel.setText(String.format("%.1f g", avgCarbs));
        avgDailyFatsLabel.setText(String.format("%.1f g", avgFats));
        loggingConsistencyLabel.setText(String.format("%.0f %%", consistency));

        // Macronutrient Distribution Pie Chart
        double totalProtein = stats.getOrDefault("totalProtein", 0.0);
        double totalCarbs = stats.getOrDefault("totalCarbs", 0.0);
        double totalFats = stats.getOrDefault("totalFats", 0.0);

        macrosPieChart.getData().clear();
        if (totalProtein > 0 || totalCarbs > 0 || totalFats > 0) {
            macrosPieChart.getData().add(new PieChart.Data("Protein", totalProtein * 4)); // 4 cal/g
            macrosPieChart.getData().add(new PieChart.Data("Carbs", totalCarbs * 4)); // 4 cal/g
            macrosPieChart.getData().add(new PieChart.Data("Fats", totalFats * 9)); // 9 cal/g
        } else {
            macrosPieChart.getData().add(new PieChart.Data("No Data", 1));
        }

        // Nutrition Goals Comparison Bar Chart
        XYChart.Series<String, Number> actualSeries = new XYChart.Series<>();
        actualSeries.setName("Actual");
        actualSeries.getData().add(new XYChart.Data<>("Calories", avgCalories));
        actualSeries.getData().add(new XYChart.Data<>("Protein", avgProtein));
        actualSeries.getData().add(new XYChart.Data<>("Carbs", avgCarbs));
        actualSeries.getData().add(new XYChart.Data<>("Fats", avgFats));

        // Default goals for comparison (can be fetched from database)
        XYChart.Series<String, Number> goalSeries = new XYChart.Series<>();
        goalSeries.setName("Goals");
        goalSeries.getData().add(new XYChart.Data<>("Calories", 2000.0));
        goalSeries.getData().add(new XYChart.Data<>("Protein", 150.0));
        goalSeries.getData().add(new XYChart.Data<>("Carbs", 200.0));
        goalSeries.getData().add(new XYChart.Data<>("Fats", 60.0));

        nutritionGoalsChart.getData().clear();
        nutritionGoalsChart.getData().addAll(actualSeries, goalSeries);
    }

    private void loadGoalAchievement() {
        Map<String, Integer> rates = progressDAO.getGoalAchievementRates(memberId);

        int dailyRate = rates.getOrDefault("dailyRate", 0);
        int weeklyRate = rates.getOrDefault("weeklyRate", 0);
        int monthlyRate = rates.getOrDefault("monthlyRate", 0);
        int overallRate = rates.getOrDefault("overallRate", 0);

        dailyGoalRateLabel.setText(dailyRate + " %");
        weeklyGoalRateLabel.setText(weeklyRate + " %");
        monthlyGoalRateLabel.setText(monthlyRate + " %");
        overallProgressLabel.setText(overallRate + " %");

        // Load achievement timeline
        List<Map<String, Object>> timeline = progressDAO.getAchievementTimeline(memberId);
        achievementTimelineContainer.getChildren().clear();

        if (timeline.isEmpty()) {
            Label noAchievements = new Label("No achievements yet. Keep working towards your goals! 💪");
            noAchievements.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            achievementTimelineContainer.getChildren().add(noAchievements);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        for (Map<String, Object> achievement : timeline) {
            HBox achievementBox = new HBox(15);
            achievementBox.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 15; -fx-background-radius: 8;");

            VBox contentBox = new VBox(5);
            HBox.setHgrow(contentBox, javafx.scene.layout.Priority.ALWAYS);

            Label typeLabel = new Label("🏆 " + achievement.get("goalType"));
            typeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

            Label descLabel = new Label((String) achievement.get("description"));
            descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #424242;");
            descLabel.setWrapText(true);

            LocalDate date = (LocalDate) achievement.get("achievementDate");
            Label dateLabel = new Label("Achieved on " + date.format(formatter));
            dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

            contentBox.getChildren().addAll(typeLabel, descLabel, dateLabel);
            achievementBox.getChildren().add(contentBox);

            achievementTimelineContainer.getChildren().add(achievementBox);
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

