package com.gym.controllers.member;

import com.gym.models.Member;
import com.gym.services.Session;
import com.gym.utils.DatabaseConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ExecuteWorkoutController {

    @FXML private Button backButton;
    @FXML private Label workoutTimerLabel;
    @FXML private Label progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label exerciseNameLabel;
    @FXML private Hyperlink demonstrationLink;
    @FXML private Label targetSetsLabel;
    @FXML private Label targetRepsLabel;
    @FXML private Label targetWeightLabel;
    @FXML private Label targetRestLabel;
    @FXML private TextField actualSetsField;
    @FXML private TextField actualWeightField;
    @FXML private TextField actualRepsField;
    @FXML private Label restTimerLabel;
    @FXML private Button startRestButton;
    @FXML private Button stopRestButton;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button finishButton;
    @FXML private HBox navigationBox;

    private int currentPlanId;
    private List<ExerciseData> exercises = new ArrayList<>();
    private int currentExerciseIndex = 0;
    private Timeline workoutTimer;
    private Timeline restTimer;
    private int workoutSeconds = 0;
    private int restSeconds = 0;
    private LocalTime workoutStartTime;

    private static class ExerciseData {
        int exerciseId;
        String name;
        int sets;
        String reps;
        double weight;
        int restSeconds;
        String notes;
        int actualSets;
        double actualWeight;
        String actualReps;

        ExerciseData(int exerciseId, String name, int sets, String reps, double weight, int restSeconds, String notes) {
            this.exerciseId = exerciseId;
            this.name = name;
            this.sets = sets;
            this.reps = reps;
            this.weight = weight;
            this.restSeconds = restSeconds;
            this.notes = notes;
        }
    }

    @FXML
    public void initialize() {
        workoutStartTime = LocalTime.now();
        startWorkoutTimer();
    }

    public void setPlanId(int planId) {
        this.currentPlanId = planId;
        loadExercises();
        if (!exercises.isEmpty()) {
            displayCurrentExercise();
        } else {
            Platform.runLater(() -> showAlert("Error", "No exercises found for this workout plan.", Alert.AlertType.ERROR));
        }
    }

    private void startWorkoutTimer() {
        workoutTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            workoutSeconds++;
            int hours = workoutSeconds / 3600;
            int minutes = (workoutSeconds % 3600) / 60;
            int seconds = workoutSeconds % 60;
            workoutTimerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        }));
        workoutTimer.setCycleCount(Timeline.INDEFINITE);
        workoutTimer.play();
    }

    private void loadExercises() {
        exercises.clear();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT tpe.exercise_id, el.exercise_name, tpe.sets, tpe.reps, " +
                    "tpe.weight, tpe.rest_seconds, tpe.trainer_notes " +
                    "FROM Trainer_Plan_Exercises tpe " +
                    "JOIN Exercises_Library el ON tpe.exercise_id = el.exercise_id " +
                    "WHERE tpe.plan_id = ? ORDER BY tpe.order_number";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentPlanId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                exercises.add(new ExerciseData(
                        rs.getInt("exercise_id"),
                        rs.getString("exercise_name"),
                        rs.getInt("sets"),
                        rs.getString("reps"),
                        rs.getDouble("weight"),
                        rs.getInt("rest_seconds"),
                        rs.getString("trainer_notes")
                ));
            }

            System.out.println("Loaded " + exercises.size() + " exercises for plan " + currentPlanId);

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert("Error", "Failed to load exercises: " + e.getMessage(), Alert.AlertType.ERROR));
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void displayCurrentExercise() {
        if (exercises.isEmpty()) {
            exerciseNameLabel.setText("No exercises available");
            return;
        }

        ExerciseData exercise = exercises.get(currentExerciseIndex);

        exerciseNameLabel.setText(exercise.name);
        targetSetsLabel.setText(String.valueOf(exercise.sets));
        targetRepsLabel.setText(exercise.reps);
        targetWeightLabel.setText(exercise.weight + " kg");
        targetRestLabel.setText(exercise.restSeconds + "s");

        demonstrationLink.setText("No video available");
        demonstrationLink.setDisable(true);

        actualSetsField.setText(exercise.actualSets > 0 ? String.valueOf(exercise.actualSets) : "");
        actualWeightField.setText(exercise.actualWeight > 0 ? String.valueOf(exercise.actualWeight) : "");
        actualRepsField.setText(exercise.actualReps != null ? exercise.actualReps : "");

        progressLabel.setText("Exercise " + (currentExerciseIndex + 1) + " of " + exercises.size());
        progressBar.setProgress((double) (currentExerciseIndex + 1) / exercises.size());

        previousButton.setDisable(currentExerciseIndex == 0);

        // Show/hide next button and finish button based on position
        if (currentExerciseIndex == exercises.size() - 1) {
            // Last exercise - show finish button, hide next button
            nextButton.setVisible(false);
            nextButton.setManaged(false);
            finishButton.setVisible(true);
            finishButton.setManaged(true);
        } else {
            // Not last exercise - show next button, hide finish button
            nextButton.setVisible(true);
            nextButton.setManaged(true);
            finishButton.setVisible(false);
            finishButton.setManaged(false);
        }

        System.out.println("Displaying exercise: " + exercise.name + " (" + (currentExerciseIndex + 1) + " of " + exercises.size() + ")");
        System.out.println("Next button visible: " + nextButton.isVisible() + ", Finish button visible: " + finishButton.isVisible());
    }

    private void saveCurrentExerciseData() {
        if (exercises.isEmpty()) return;

        ExerciseData exercise = exercises.get(currentExerciseIndex);

        try {
            String setsText = actualSetsField.getText().trim();
            String weightText = actualWeightField.getText().trim();

            exercise.actualSets = setsText.isEmpty() ? 0 : Integer.parseInt(setsText);
            exercise.actualWeight = weightText.isEmpty() ? 0.0 : Double.parseDouble(weightText);
            exercise.actualReps = actualRepsField.getText().trim();
        } catch (NumberFormatException e) {
            Platform.runLater(() -> showAlert("Error", "Please enter valid numbers for sets and weight.", Alert.AlertType.ERROR));
        }
    }

    @FXML
    private void previousExercise() {
        if (currentExerciseIndex > 0) {
            saveCurrentExerciseData();
            currentExerciseIndex--;
            displayCurrentExercise();
            resetRestTimer();
        }
    }

    @FXML
    private void nextExercise() {
        if (currentExerciseIndex < exercises.size() - 1) {
            saveCurrentExerciseData();
            currentExerciseIndex++;
            displayCurrentExercise();
            resetRestTimer();
        }
    }

    @FXML
    private void startRestTimer() {
        if (exercises.isEmpty()) return;

        restSeconds = exercises.get(currentExerciseIndex).restSeconds;
        startRestButton.setDisable(true);
        stopRestButton.setDisable(false);

        restTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            restSeconds--;
            int minutes = restSeconds / 60;
            int seconds = restSeconds % 60;
            restTimerLabel.setText(String.format("%02d:%02d", minutes, seconds));

            if (restSeconds <= 0) {
                stopRestTimer();
                // Use Platform.runLater to avoid showAndWait during animation
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Rest Complete");
                    alert.setHeaderText(null);
                    alert.setContentText("Time to continue your workout! 💪");
                    alert.show();
                });
            }
        }));
        restTimer.setCycleCount(Timeline.INDEFINITE);
        restTimer.play();
    }

    @FXML
    private void stopRestTimer() {
        if (restTimer != null) {
            restTimer.stop();
        }
        resetRestTimer();
    }

    private void resetRestTimer() {
        if (restTimer != null) {
            restTimer.stop();
        }
        restTimerLabel.setText("00:00");
        startRestButton.setDisable(false);
        stopRestButton.setDisable(true);
    }

    @FXML
    private void finishWorkout() {
        saveCurrentExerciseData();

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Finish Workout");
        confirmation.setHeaderText("Complete Workout?");
        confirmation.setContentText("Are you sure you want to finish this workout?");

        if (confirmation.showAndWait().get() != ButtonType.OK) {
            return;
        }

        Member member = (Member) Session.getInstance().getCurrentUser();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            int totalDuration = workoutSeconds / 60;
            int totalCalories = calculateCalories();

            String insertWorkoutSql = "INSERT INTO Workouts (member_id, workout_date, workout_time, " +
                    "workout_type, total_duration, total_calories, created_at) VALUES (?, date('now'), ?, " +
                    "(SELECT focus_area FROM Trainer_Workout_Plans WHERE plan_id = ?), ?, ?, datetime('now'))";
            pstmt = conn.prepareStatement(insertWorkoutSql);
            pstmt.setInt(1, member.getMemberId());
            pstmt.setString(2, workoutStartTime.toString());
            pstmt.setInt(3, currentPlanId);
            pstmt.setInt(4, totalDuration);
            pstmt.setInt(5, totalCalories);
            pstmt.executeUpdate();
            pstmt.close();

            // Get the last inserted workout_id using SQLite's last_insert_rowid()
            pstmt = conn.prepareStatement("SELECT last_insert_rowid() as workout_id");
            rs = pstmt.executeQuery();
            int workoutId = 0;
            if (rs.next()) {
                workoutId = rs.getInt("workout_id");
            }
            pstmt.close();
            rs.close();

            // Insert exercises - use correct column names: sets, reps, weight
            for (ExerciseData exercise : exercises) {
                String insertExerciseSql = "INSERT INTO Workout_Exercises (workout_id, exercise_id, " +
                        "sets, reps, weight) VALUES (?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(insertExerciseSql);
                pstmt.setInt(1, workoutId);
                pstmt.setInt(2, exercise.exerciseId);
                pstmt.setInt(3, exercise.actualSets > 0 ? exercise.actualSets : exercise.sets);
                pstmt.setString(4, exercise.actualReps != null && !exercise.actualReps.isEmpty() ? exercise.actualReps : exercise.reps);
                pstmt.setDouble(5, exercise.actualWeight > 0 ? exercise.actualWeight : exercise.weight);
                pstmt.executeUpdate();
                pstmt.close();
            }

            // Update plan status to COMPLETED
            String updatePlanSql = "UPDATE Trainer_Workout_Plans SET status = 'COMPLETED' WHERE plan_id = ?";
            pstmt = conn.prepareStatement(updatePlanSql);
            pstmt.setInt(1, currentPlanId);
            pstmt.executeUpdate();
            pstmt.close();

            conn.commit();

            if (workoutTimer != null) workoutTimer.stop();
            if (restTimer != null) restTimer.stop();

            showCompletionSummary(totalDuration, totalCalories);

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            showAlert("Error", "Failed to finish workout: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    DatabaseConnection.getInstance().releaseConnection(conn);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int calculateCalories() {
        int totalCalories = 0;
        for (ExerciseData exercise : exercises) {
            int sets = exercise.actualSets > 0 ? exercise.actualSets : exercise.sets;
            totalCalories += sets * 15;
        }
        return totalCalories;
    }

    private void showCompletionSummary(int duration, int calories) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Workout Complete!");
        alert.setHeaderText("🎉 Congratulations!");
        alert.setContentText(String.format(
                "Great job completing your workout!\n\n" +
                "Duration: %d minutes\n" +
                "Calories Burned: %d kcal\n" +
                "Exercises Completed: %d\n\n" +
                "Keep up the great work! 💪",
                duration, calories, exercises.size()
        ));
        alert.showAndWait();
        handleBack();
    }

    @FXML
    private void handleBack() {
        if (workoutTimer != null) workoutTimer.stop();
        if (restTimer != null) restTimer.stop();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/view_workout_plan.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("View Workout Plan");
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

