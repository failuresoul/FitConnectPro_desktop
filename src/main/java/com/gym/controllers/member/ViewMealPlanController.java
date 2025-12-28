package com.gym.controllers.member;

import com.gym.dao.MealPlanDAO;
import com.gym.models.Member;
import com.gym.models.MealPlan;
import com.gym.services.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ViewMealPlanController {

    @FXML private Button backButton;
    @FXML private DatePicker datePicker;
    @FXML private TabPane mealTabPane;
    @FXML private VBox breakfastContainer;
    @FXML private VBox lunchContainer;
    @FXML private VBox dinnerContainer;
    @FXML private VBox snacksContainer;
    @FXML private Label totalCaloriesLabel;
    @FXML private Label totalProteinLabel;
    @FXML private Label totalCarbsLabel;
    @FXML private Label totalFatsLabel;

    private MealPlanDAO mealPlanDAO;

    @FXML
    public void initialize() {
        try {
            mealPlanDAO = new MealPlanDAO();
            datePicker.setValue(LocalDate.now());
            datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null) {
                    loadMealPlans(newDate);
                }
            });
            loadMealPlans(LocalDate.now());
        } catch (Exception e) {
            System.err.println("Error initializing ViewMealPlanController: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to initialize meal plan view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadMealPlans(LocalDate date) {
        try {
            Member member = (Member) Session.getInstance().getCurrentUser();
            if (member == null) {
                showAlert("Error", "Session expired. Please login again.", Alert.AlertType.ERROR);
                return;
            }

            List<MealPlan> mealPlans = mealPlanDAO.getMemberMealPlansForDate(member.getMemberId(), date);

            breakfastContainer.getChildren().clear();
            lunchContainer.getChildren().clear();
            dinnerContainer.getChildren().clear();
            snacksContainer.getChildren().clear();

            double totalCalories = 0;
            double totalProtein = 0;
            double totalCarbs = 0;
            double totalFats = 0;

            for (MealPlan meal : mealPlans) {
                VBox mealCard = createMealCard(meal);

                String mealType = meal.getMealType() != null ? meal.getMealType().toUpperCase() : "";

                switch (mealType) {
                    case "BREAKFAST":
                        breakfastContainer.getChildren().add(mealCard);
                        break;
                    case "LUNCH":
                        lunchContainer.getChildren().add(mealCard);
                        break;
                    case "DINNER":
                        dinnerContainer.getChildren().add(mealCard);
                        break;
                    case "SNACK":
                    case "SNACKS":
                        snacksContainer.getChildren().add(mealCard);
                        break;
                }

                totalCalories += meal.getTotalCalories();
                totalProtein += meal.getTotalProtein();
                totalCarbs += meal.getTotalCarbs();
                totalFats += meal.getTotalFats();
            }

            if (breakfastContainer.getChildren().isEmpty()) {
                breakfastContainer.getChildren().add(createNoMealLabel());
            }
            if (lunchContainer.getChildren().isEmpty()) {
                lunchContainer.getChildren().add(createNoMealLabel());
            }
            if (dinnerContainer.getChildren().isEmpty()) {
                dinnerContainer.getChildren().add(createNoMealLabel());
            }
            if (snacksContainer.getChildren().isEmpty()) {
                snacksContainer.getChildren().add(createNoMealLabel());
            }

            totalCaloriesLabel.setText(String.format("%.0f kcal", totalCalories));
            totalProteinLabel.setText(String.format("%.1f g", totalProtein));
            totalCarbsLabel.setText(String.format("%.1f g", totalCarbs));
            totalFatsLabel.setText(String.format("%.1f g", totalFats));

        } catch (Exception e) {
            System.err.println("Error loading meal plans: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load meal plans: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private VBox createMealCard(MealPlan meal) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8;");

        String timeText = "Not specified";
        if (meal.getMealTime() != null) {
            try {
                timeText = meal.getMealTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
            } catch (Exception e) {
                timeText = meal.getMealTime().toString();
            }
        }

        Label timeLabel = new Label("⏰ " + timeText);
        timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d; -fx-font-weight: 600;");

        Label foodsLabel = new Label("🍴 " + (meal.getFoods() != null ? meal.getFoods() : "No foods specified"));
        foodsLabel.setWrapText(true);
        foodsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        HBox nutritionBox = new HBox(15);
        nutritionBox.setStyle("-fx-padding: 10 0 0 0;");

        Label caloriesLabel = new Label(String.format("🔥 %d cal", meal.getTotalCalories()));
        caloriesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        Label proteinLabel = new Label(String.format("💪 %.1fg protein", meal.getTotalProtein()));
        proteinLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #27ae60;");

        Label carbsLabel = new Label(String.format("🌾 %.1fg carbs", meal.getTotalCarbs()));
        carbsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db;");

        Label fatsLabel = new Label(String.format("🥑 %.1fg fats", meal.getTotalFats()));
        fatsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #f39c12;");

        nutritionBox.getChildren().addAll(caloriesLabel, proteinLabel, carbsLabel, fatsLabel);

        card.getChildren().addAll(timeLabel, foodsLabel, nutritionBox);

        if (meal.getPreparationInstructions() != null && !meal.getPreparationInstructions().isEmpty()) {
            Separator separator = new Separator();
            Label instructionsLabel = new Label("📝 Instructions: " + meal.getPreparationInstructions());
            instructionsLabel.setWrapText(true);
            instructionsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #495057; -fx-font-style: italic; -fx-padding: 10 0 0 0;");
            card.getChildren().addAll(separator, instructionsLabel);
        }

        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-padding: 10 0 0 0;");

        Button markEatenBtn = new Button("✓ Mark as Eaten");
        markEatenBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        markEatenBtn.setOnAction(e -> markAsEaten(meal));

        Button logDifferentBtn = new Button("📝 Log Different Meal");
        logDifferentBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        logDifferentBtn.setOnAction(e -> logDifferentMeal());

        buttonBox.getChildren().addAll(markEatenBtn, logDifferentBtn);
        card.getChildren().add(buttonBox);

        return card;
    }

    private Label createNoMealLabel() {
        Label label = new Label("No meal plan assigned for this time");
        label.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic; -fx-font-size: 13px;");
        return label;
    }

    private void markAsEaten(MealPlan meal) {
        try {
            Member member = (Member) Session.getInstance().getCurrentUser();
            if (member == null) {
                showAlert("Error", "Session expired. Please login again.", Alert.AlertType.ERROR);
                return;
            }

            boolean success = mealPlanDAO.markMealAsEaten(member.getMemberId(), meal);

            if (success) {
                showAlert("Success", "Meal logged successfully! 🎉", Alert.AlertType.INFORMATION);
                loadMealPlans(datePicker.getValue());
            } else {
                showAlert("Error", "Failed to log meal. Please try again.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            System.err.println("Error marking meal as eaten: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to log meal: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void logDifferentMeal() {
        showAlert("Coming Soon", "Custom meal logging feature will be available soon!", Alert.AlertType.INFORMATION);
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
            System.err.println("Error navigating back: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to navigate back: " + e.getMessage(), Alert.AlertType.ERROR);
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
