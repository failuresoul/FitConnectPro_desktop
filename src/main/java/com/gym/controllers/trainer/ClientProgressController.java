package com.gym.controllers.trainer;

import com.gym.dao.ProgressDAO;
import com.gym.dao.TrainerDAO;
import com.gym.models.*;
import com.gym.services.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ClientProgressController {

    @FXML private ComboBox<ClientDetails> clientComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button loadProgressBtn;

    // Progress Display
    @FXML private ProgressBar workoutProgressBar;
    @FXML private Label workoutCompletionLabel;
    @FXML private Label workoutDetailsLabel;

    @FXML private Label mealsLoggedLabel;

    @FXML private ProgressBar waterProgressBar;
    @FXML private Label waterComplianceLabel;
    @FXML private Label waterDetailsLabel;

    @FXML private Label weightChangeLabel;
    @FXML private Label goalSummaryLabel;

    @FXML private TextArea trainerFeedbackArea;
    @FXML private Button sendReportBtn;

    private TrainerDAO trainerDAO;
    private ProgressDAO progressDAO;
    private Trainer currentTrainer;
    private Map<String, Object> currentProgress;

    public ClientProgressController() {
        trainerDAO = new TrainerDAO();
        progressDAO = new ProgressDAO();
    }

    @FXML
    public void initialize() {
        System.out.println("========================================");
        System.out.println("✅ ClientProgressController initialized");
        System.out.println("========================================");

        currentTrainer = (Trainer) Session.getInstance().getCurrentUser();

        if (currentTrainer == null) {
            System.err.println("❌ No trainer in session!");
            return;
        }

        loadClients();
        setupDatePickers();
        setupEventHandlers();
    }

    private void loadClients() {
        if (currentTrainer == null) return;

        try {
            List<ClientDetails> clients = trainerDAO.getMyAssignedClients(currentTrainer.getTrainerId());
            clientComboBox.setItems(FXCollections.observableArrayList(clients));

            // Custom display
            clientComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(ClientDetails item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getMemberName() + " - " + item.getGoal());
                    }
                }
            });

            clientComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(ClientDetails item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getMemberName());
                    }
                }
            });

            System.out.println("✅ Loaded " + clients.size() + " clients");

        } catch (Exception e) {
            System.err.println("❌ Error loading clients: " + e. getMessage());
            e.printStackTrace();
        }
    }

    private void setupDatePickers() {
        // Default:  last 7 days
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(6));
    }

    private void setupEventHandlers() {
        if (loadProgressBtn != null) {
            loadProgressBtn.setOnAction(e -> loadClientProgress());
        }
        if (sendReportBtn != null) {
            sendReportBtn. setOnAction(e -> generateWeeklyReport());
        }
    }

    private void loadClientProgress() {
        try {
            ClientDetails selectedClient = clientComboBox.getValue();
            if (selectedClient == null) {
                showAlert("Validation Error", "Please select a client", Alert.AlertType.WARNING);
                return;
            }

            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                showAlert("Validation Error", "Please select date range", Alert.AlertType.WARNING);
                return;
            }

            if (startDate.isAfter(endDate)) {
                showAlert("Validation Error", "Start date must be before end date", Alert.AlertType.WARNING);
                return;
            }

            // Load progress data
            currentProgress = progressDAO.getClientProgress(selectedClient.getMemberId(), startDate, endDate);

            // Display progress
            displayProgress();

            System.out.println("✅ Loaded progress for " + selectedClient.getMemberName());

        } catch (Exception e) {
            System.err.println("❌ Error loading progress: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load progress data", Alert.AlertType.ERROR);
        }
    }

    private void displayProgress() {
        if (currentProgress == null || currentProgress.isEmpty()) {
            showAlert("No Data", "No progress data found for selected period", Alert.AlertType.INFORMATION);
            return;
        }

        // 1. Workout Completion Rate
        double workoutRate = getDoubleValue(currentProgress, "workoutCompletionRate", 0.0);
        int totalWorkouts = getIntValue(currentProgress, "totalWorkouts", 0);

        workoutProgressBar.setProgress(workoutRate / 100.0);
        workoutCompletionLabel.setText(String.format("%.0f%%", workoutRate));
        workoutDetailsLabel.setText(totalWorkouts + " workouts completed");

        // 2. Meals Logged
        int mealsLogged = getIntValue(currentProgress, "mealsLoggedCount", 0);
        mealsLoggedLabel.setText(mealsLogged + " meals logged");

        // 3. Water Intake Compliance
        double waterCompliance = getDoubleValue(currentProgress, "waterIntakeCompliance", 0.0);
        int waterDaysMet = getIntValue(currentProgress, "waterDaysMet", 0);
        int totalDays = getIntValue(currentProgress, "totalDays", 1);

        waterProgressBar.setProgress(waterCompliance / 100.0);
        waterComplianceLabel.setText(String.format("%.0f%%", waterCompliance));
        waterDetailsLabel.setText(waterDaysMet + " of " + totalDays + " days met goal");

        // 4. Weight Change
        double weightChange = getDoubleValue(currentProgress, "weightChange", 0.0);
        String weightChangeText;
        if (weightChange > 0) {
            weightChangeText = String.format("+%.1f kg", weightChange);
        } else if (weightChange < 0) {
            weightChangeText = String.format("%.1f kg", weightChange);
        } else {
            weightChangeText = "No change";
        }
        weightChangeLabel.setText(weightChangeText);

        // 5. Goal Achievement Summary
        String goalSummary = (String) currentProgress.getOrDefault("goalAchievementSummary", "No goals tracked");
        goalSummaryLabel.setText(goalSummary);
    }

    // Helper methods for safe type conversion
    private double getDoubleValue(Map<String, Object> map, String key, double defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private void generateWeeklyReport() {
        try {
            ClientDetails selectedClient = clientComboBox.getValue();
            if (selectedClient == null) {
                showAlert("Validation Error", "Please select a client", Alert.AlertType.WARNING);
                return;
            }

            if (currentProgress == null || currentProgress.isEmpty()) {
                showAlert("Validation Error", "Please load progress data first", Alert.AlertType.WARNING);
                return;
            }

            String feedback = trainerFeedbackArea.getText().trim();
            if (feedback.isEmpty()) {
                showAlert("Validation Error", "Please enter trainer feedback", Alert.AlertType.WARNING);
                return;
            }

            // Create progress report
            ProgressReport report = new ProgressReport();
            report.setTrainerId(currentTrainer.getTrainerId());
            report.setMemberId(selectedClient.getMemberId());
            report.setReportDate(LocalDate.now());
            report.setStartDate(startDatePicker.getValue());
            report.setEndDate(endDatePicker.getValue());
            report.setWorkoutCompletionRate(getDoubleValue(currentProgress, "workoutCompletionRate", 0.0));
            report.setMealsLoggedCount(getIntValue(currentProgress, "mealsLoggedCount", 0));
            report.setWaterIntakeCompliance(getDoubleValue(currentProgress, "waterIntakeCompliance", 0.0));
            report.setWeightChange(getDoubleValue(currentProgress, "weightChange", 0.0));
            report.setGoalAchievementSummary((String) currentProgress.getOrDefault("goalAchievementSummary", "No summary"));
            report.setTrainerFeedback(feedback);

            // Save report
            boolean success = progressDAO.saveWeeklyReport(report);

            if (success) {
                showAlert("Success",
                        "Weekly report sent to " + selectedClient.getMemberName() + " successfully!",
                        Alert.AlertType.INFORMATION);
                trainerFeedbackArea.clear();
            } else {
                showAlert("Error", "Failed to send weekly report", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("❌ Error generating report: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "An error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
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

