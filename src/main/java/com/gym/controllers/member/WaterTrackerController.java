package com.gym.controllers.member;

import com.gym.dao.WaterLogDAO;
import com.gym.models.Member;
import com.gym.models.WaterLog;
import com.gym.services.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class WaterTrackerController {

    @FXML private Button backButton;
    @FXML private Circle progressCircle;
    @FXML private Label currentAmountLabel;
    @FXML private Label goalAmountLabel;
    @FXML private Label percentageLabel;
    @FXML private Label trainerGoalLabel;
    @FXML private TextField customAmountField;
    @FXML private ListView<String> todayLogsListView;
    @FXML private BarChart<String, Number> weeklyChart;

    private WaterLogDAO waterLogDAO;
    private int dailyGoal = 3500; // Default goal
    private int currentTotal = 0;

    @FXML
    public void initialize() {
        waterLogDAO = new WaterLogDAO();
        loadDailyGoal();
        loadTodayData();
        loadWeeklyChart();
    }

    private void loadDailyGoal() {
        Member member = (Member) Session.getInstance().getCurrentUser();
        int goal = waterLogDAO.getTrainerDailyGoal(member.getMemberId(), LocalDate.now());
        if (goal > 0) {
            dailyGoal = goal;
        }
        goalAmountLabel.setText("/ " + dailyGoal + " ml");
        trainerGoalLabel.setText("Goal set by Trainer: " + dailyGoal + "ml");
    }

    private void loadTodayData() {
        Member member = (Member) Session.getInstance().getCurrentUser();
        currentTotal = waterLogDAO.getTodayWaterTotal(member.getMemberId(), LocalDate.now());
        updateProgressCircle();
        loadTodayLogs();
    }

    private void updateProgressCircle() {
        currentAmountLabel.setText(currentTotal + " ml");

        double percentage = (double) currentTotal / dailyGoal * 100;
        if (percentage > 100) percentage = 100;

        percentageLabel.setText(String.format("%.0f%%", percentage));

        // Update circle progress (circumference = 2 * PI * radius = 628.318)
        double circumference = 628.318;
        double offset = circumference - (circumference * percentage / 100);
        progressCircle.setStrokeDashOffset(offset);
    }

    private void loadTodayLogs() {
        Member member = (Member) Session.getInstance().getCurrentUser();
        List<WaterLog> logs = waterLogDAO.getTodayLogs(member.getMemberId(), LocalDate.now());

        todayLogsListView.getItems().clear();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (WaterLog log : logs) {
            String logText = log.getLogTime().format(timeFormatter) + " - " + log.getAmountMl() + " ml";
            todayLogsListView.getItems().add(logText);
        }
    }

    private void loadWeeklyChart() {
        Member member = (Member) Session.getInstance().getCurrentUser();
        Map<LocalDate, Integer> weeklyData = waterLogDAO.getLast7DaysWater(member.getMemberId());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Water Intake");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateStr = date.format(dateFormatter);
            int amount = weeklyData.getOrDefault(date, 0);
            series.getData().add(new XYChart.Data<>(dateStr, amount));
        }

        weeklyChart.getData().clear();
        weeklyChart.getData().add(series);
    }

    @FXML
    private void addWater250() {
        addWater(250);
    }

    @FXML
    private void addWater500() {
        addWater(500);
    }

    @FXML
    private void addWater750() {
        addWater(750);
    }

    @FXML
    private void addCustomWater() {
        try {
            String input = customAmountField.getText().trim();
            if (input.isEmpty()) {
                showAlert("Error", "Please enter an amount.", Alert.AlertType.ERROR);
                return;
            }

            int amount = Integer.parseInt(input);
            if (amount <= 0 || amount > 5000) {
                showAlert("Error", "Please enter a valid amount (1-5000 ml).", Alert.AlertType.ERROR);
                return;
            }

            addWater(amount);
            customAmountField.clear();

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid number.", Alert.AlertType.ERROR);
        }
    }

    private void addWater(int amount) {
        Member member = (Member) Session.getInstance().getCurrentUser();
        boolean success = waterLogDAO.logWater(member.getMemberId(), amount, LocalDateTime.now());

        if (success) {
            currentTotal += amount;
            updateProgressCircle();
            loadTodayLogs();
            loadWeeklyChart();

            // Check if goal achieved
            if (currentTotal >= dailyGoal && (currentTotal - amount) < dailyGoal) {
                showAlert("Congratulations!",
                    "🎉 You've reached your daily water intake goal!\nGreat job staying hydrated! 💧",
                    Alert.AlertType.INFORMATION);
            }
        } else {
            showAlert("Error", "Failed to log water intake.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void setReminder() {
        showAlert("Reminder", "Reminder feature will be available in the next update!", Alert.AlertType.INFORMATION);
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

