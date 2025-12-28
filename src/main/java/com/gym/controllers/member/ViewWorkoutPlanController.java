package com.gym.controllers.member;

import com.gym.models.Member;
import com.gym.services.Session;
import com.gym.utils.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class ViewWorkoutPlanController {

    @FXML private DatePicker datePicker;
    @FXML private Label statusLabel;
    @FXML private VBox trainerInfoCard;
    @FXML private ImageView trainerPhoto;
    @FXML private Label trainerNameLabel;
    @FXML private Label focusAreaLabel;
    @FXML private Text instructionsText;
    @FXML private VBox exerciseListContainer;
    @FXML private Label exerciseCountLabel;
    @FXML private Label durationLabel;
    @FXML private Label caloriesLabel;
    @FXML private Button startWorkoutButton;
    @FXML private Button markCompleteButton;
    @FXML private Button backButton;

    private int currentPlanId = -1;

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> loadPlanForDate(newDate));

        // Set default trainer photo
        try {
            trainerPhoto.setImage(new Image(getClass().getResourceAsStream("/images/default_trainer.png")));
        } catch (Exception e) {
            System.out.println("Default trainer image not found");
        }

        loadPlanForDate(LocalDate.now());
    }

    private void loadPlanForDate(LocalDate date) {
        Member member = (Member) Session.getInstance().getCurrentUser();
        if (member == null) {
            showAlert("Error", "Session expired. Please login again.", Alert.AlertType.ERROR);
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT twp.plan_id, twp.focus_area, twp.total_duration, " +
                    "twp.expected_calories, twp.instructions, twp.status, " +
                    "t.full_name as trainer_name " +
                    "FROM Trainer_Workout_Plans twp " +
                    "JOIN Trainers t ON twp.trainer_id = t.trainer_id " +
                    "WHERE twp.member_id = ? AND twp.plan_date = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, member.getMemberId());
            pstmt.setString(2, date.toString());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                currentPlanId = rs.getInt("plan_id");
                trainerNameLabel.setText(rs.getString("trainer_name"));
                focusAreaLabel.setText(rs.getString("focus_area"));
                instructionsText.setText(rs.getString("instructions"));
                durationLabel.setText(rs.getInt("total_duration") + " min");
                caloriesLabel.setText(rs.getInt("expected_calories") + " kcal");

                String status = rs.getString("status");
                updateStatusLabel(status);

                if ("COMPLETED".equals(status)) {
                    markCompleteButton.setDisable(true);
                    markCompleteButton.setText("✓ Completed");
                    startWorkoutButton.setDisable(true);
                } else {
                    markCompleteButton.setDisable(false);
                    markCompleteButton.setText("✓ Mark as Complete");
                    startWorkoutButton.setDisable(false);
                }

                loadExercises(currentPlanId);
                trainerInfoCard.setVisible(true);

            } else {
                currentPlanId = -1;
                trainerNameLabel.setText("No Plan Available");
                focusAreaLabel.setText("");
                instructionsText.setText("No workout plan assigned for this date. Please contact your trainer.");
                durationLabel.setText("-- min");
                caloriesLabel.setText("-- kcal");
                exerciseCountLabel.setText("0 exercises");
                exerciseListContainer.getChildren().clear();
                startWorkoutButton.setDisable(true);
                markCompleteButton.setDisable(true);
                statusLabel.setText("NO PLAN");
                statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #95a5a6; -fx-padding: 5 15; -fx-background-radius: 15;");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load workout plan: " + e.getMessage(), Alert.AlertType.ERROR);
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

    private void updateStatusLabel(String status) {
        statusLabel.setText(status);
        switch (status) {
            case "COMPLETED":
                statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #4caf50; -fx-padding: 5 15; -fx-background-radius: 15;");
                break;
            case "IN_PROGRESS":
                statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #2196f3; -fx-padding: 5 15; -fx-background-radius: 15;");
                break;
            default:
                statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #ffc107; -fx-padding: 5 15; -fx-background-radius: 15;");
        }
    }

    private void loadExercises(int planId) {
        exerciseListContainer.getChildren().clear();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT el.exercise_name, tpe.sets, tpe.reps, tpe.weight, " +
                    "tpe.rest_seconds, tpe.trainer_notes " +
                    "FROM Trainer_Plan_Exercises tpe " +
                    "JOIN Exercises_Library el ON tpe.exercise_id = el.exercise_id " +
                    "WHERE tpe.plan_id = ? ORDER BY tpe.order_number";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, planId);
            rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                VBox card = createExerciseCard(
                        count,
                        rs.getString("exercise_name"),
                        rs.getInt("sets"),
                        rs.getString("reps"),
                        rs.getDouble("weight"),
                        rs.getInt("rest_seconds"),
                        rs.getString("trainer_notes")
                );
                exerciseListContainer.getChildren().add(card);
            }

            exerciseCountLabel.setText(count + " exercise" + (count != 1 ? "s" : ""));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load exercises: " + e.getMessage(), Alert.AlertType.ERROR);
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

    private VBox createExerciseCard(int index, String name, int sets, String reps, double weight, int restSeconds, String notes) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 18; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); " +
                "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");

        HBox header = new HBox(12);
        header.setStyle("-fx-alignment: CENTER_LEFT;");

        Label indexLabel = new Label(String.valueOf(index));
        indexLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-background-color: #3498db; -fx-padding: 5 10; -fx-background-radius: 15; -fx-min-width: 35; -fx-alignment: center;");

        CheckBox checkbox = new CheckBox();
        checkbox.setStyle("-fx-cursor: hand;");

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        header.getChildren().addAll(indexLabel, checkbox, nameLabel);

        HBox detailsBox = new HBox(20);
        detailsBox.setStyle("-fx-padding: 8 0 0 47;");

        VBox setsRepsBox = new VBox(3);
        Label setsLabel = new Label("Sets × Reps");
        setsLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d; -fx-font-weight: 600;");
        Label setsValue = new Label(sets + " × " + reps);
        setsValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        setsRepsBox.getChildren().addAll(setsLabel, setsValue);

        VBox weightBox = new VBox(3);
        Label weightLabel = new Label("Weight");
        weightLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d; -fx-font-weight: 600;");
        Label weightValue = new Label(weight + " kg");
        weightValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #e67e22;");
        weightBox.getChildren().addAll(weightLabel, weightValue);

        VBox restBox = new VBox(3);
        Label restLabel = new Label("Rest");
        restLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d; -fx-font-weight: 600;");
        Label restValue = new Label(restSeconds + "s");
        restValue.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        restBox.getChildren().addAll(restLabel, restValue);

        detailsBox.getChildren().addAll(setsRepsBox, weightBox, restBox);

        card.getChildren().addAll(header, detailsBox);

        if (notes != null && !notes.isEmpty()) {
            HBox notesBox = new HBox(8);
            notesBox.setStyle("-fx-padding: 8 0 0 47; -fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5;");
            Label notesIcon = new Label("💡");
            notesIcon.setStyle("-fx-font-size: 14px;");
            Label notesLabel = new Label(notes);
            notesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555; -fx-font-style: italic;");
            notesLabel.setWrapText(true);
            notesBox.getChildren().addAll(notesIcon, notesLabel);
            card.getChildren().add(notesBox);
        }

        return card;
    }

    @FXML
    private void startWorkout() {
        if (currentPlanId == -1) {
            showAlert("Error", "No workout plan selected.", Alert.AlertType.ERROR);
            return;
        }

        try {
            updatePlanStatus("IN_PROGRESS");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/member/execute_workout.fxml"));
            Parent root = loader.load();

            ExecuteWorkoutController controller = loader.getController();
            controller.setPlanId(currentPlanId);

            Stage stage = (Stage) startWorkoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Execute Workout");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to start workout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updatePlanStatus(String status) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "UPDATE Trainer_Workout_Plans SET status = ? WHERE plan_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, currentPlanId);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void markAsComplete() {
        if (currentPlanId == -1) {
            showAlert("Error", "No workout plan selected.", Alert.AlertType.ERROR);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Completion");
        confirmation.setHeaderText("Mark Workout as Complete");
        confirmation.setContentText("Are you sure you want to mark this workout as completed?");

        if (confirmation.showAndWait().get() != ButtonType.OK) {
            return;
        }

        Member member = (Member) Session.getInstance().getCurrentUser();
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            String updateSql = "UPDATE Trainer_Workout_Plans SET status = 'COMPLETED' WHERE plan_id = ?";
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setInt(1, currentPlanId);
            pstmt.executeUpdate();
            pstmt.close();

            String insertSql = "INSERT INTO Workouts (member_id, workout_date, workout_time, " +
                    "workout_type, total_duration, total_calories, created_at) " +
                    "SELECT member_id, plan_date, time('now'), focus_area, total_duration, " +
                    "expected_calories, datetime('now') FROM Trainer_Workout_Plans WHERE plan_id = ?";
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setInt(1, currentPlanId);
            pstmt.executeUpdate();

            conn.commit();

            showAlert("Success", "Workout marked as complete! Great job! 💪", Alert.AlertType.INFORMATION);
            loadPlanForDate(datePicker.getValue());

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            showAlert("Error", "Failed to mark workout as complete: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            try {
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
            showAlert("Error", "Failed to go back: " + e.getMessage(), Alert.AlertType.ERROR);
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

