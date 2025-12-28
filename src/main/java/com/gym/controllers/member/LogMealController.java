package com.gym.controllers.member;

import com.gym.dao.MealDAO;
import com.gym.dao.MealPlanDAO;
import com.gym.models.Food;
import com.gym.models.Meal;
import com.gym.models.MealItem;
import com.gym.models.Member;
import com.gym.services.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class LogMealController {

    @FXML private Button backButton;
    @FXML private ComboBox<String> mealTypeComboBox;
    @FXML private TextField mealTimeField;
    @FXML private TextField foodSearchField;
    @FXML private TableView<Food> foodsTableView;
    @FXML private TableColumn<Food, String> foodNameColumn;
    @FXML private TableColumn<Food, String> servingSizeColumn;
    @FXML private TableColumn<Food, Integer> caloriesColumn;
    @FXML private TableColumn<Food, Double> proteinColumn;
    @FXML private TableColumn<Food, Double> carbsColumn;
    @FXML private TableColumn<Food, Double> fatsColumn;
    @FXML private TableColumn<Food, Void> addColumn;
    @FXML private VBox mealItemsContainer;
    @FXML private Label totalCaloriesLabel;
    @FXML private Label totalProteinLabel;
    @FXML private Label totalCarbsLabel;
    @FXML private Label totalFatsLabel;
    @FXML private TextArea notesTextArea;
    @FXML private Button saveMealButton;

    private MealPlanDAO mealPlanDAO;
    private MealDAO mealDAO;
    private List<MealItemData> currentMealItems = new ArrayList<>();

    private static class MealItemData {
        Food food;
        double quantity;

        MealItemData(Food food, double quantity) {
            this.food = food;
            this.quantity = quantity;
        }
    }

    @FXML
    public void initialize() {
        mealPlanDAO = new MealPlanDAO();
        mealDAO = new MealDAO();

        mealTypeComboBox.getItems().addAll("BREAKFAST", "LUNCH", "DINNER", "SNACK");
        mealTypeComboBox.setValue("LUNCH");

        mealTimeField.setText(LocalTime.now().toString().substring(0, 5));

        setupFoodsTable();
        loadAllFoods();

        foodSearchField.textProperty().addListener((obs, oldVal, newVal) -> searchFoods(newVal));
    }

    private void setupFoodsTable() {
        foodNameColumn.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        servingSizeColumn.setCellValueFactory(new PropertyValueFactory<>("servingSize"));
        caloriesColumn.setCellValueFactory(new PropertyValueFactory<>("caloriesPerServing"));
        proteinColumn.setCellValueFactory(new PropertyValueFactory<>("protein"));
        carbsColumn.setCellValueFactory(new PropertyValueFactory<>("carbs"));
        fatsColumn.setCellValueFactory(new PropertyValueFactory<>("fats"));

        addColumn.setCellFactory(param -> new TableCell<>() {
            private final Button addButton = new Button("+");

            {
                addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                addButton.setOnAction(event -> {
                    Food food = getTableView().getItems().get(getIndex());
                    addFoodToMeal(food);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : addButton);
            }
        });
    }

    private void loadAllFoods() {
        List<Food> foods = mealPlanDAO.getAllFoods();
        foodsTableView.getItems().setAll(foods);
    }

    private void searchFoods(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadAllFoods();
        } else {
            List<Food> foods = mealPlanDAO.searchFoods(keyword.trim());
            foodsTableView.getItems().setAll(foods);
        }
    }

    private void addFoodToMeal(Food food) {
        TextInputDialog dialog = new TextInputDialog("1.0");
        dialog.setTitle("Add Food");
        dialog.setHeaderText("Add " + food.getFoodName());
        dialog.setContentText("Quantity:");

        dialog.showAndWait().ifPresent(quantity -> {
            try {
                double qty = Double.parseDouble(quantity);
                if (qty > 0) {
                    currentMealItems.add(new MealItemData(food, qty));
                    updateMealItemsDisplay();
                    updateTotals();
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid number for quantity.", Alert.AlertType.ERROR);
            }
        });
    }

    private void updateMealItemsDisplay() {
        mealItemsContainer.getChildren().clear();

        for (int i = 0; i < currentMealItems.size(); i++) {
            MealItemData item = currentMealItems.get(i);
            HBox itemBox = createMealItemBox(item, i);
            mealItemsContainer.getChildren().add(itemBox);
        }
    }

    private HBox createMealItemBox(MealItemData item, int index) {
        HBox box = new HBox(15);
        box.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 12; -fx-background-radius: 6; -fx-alignment: CENTER_LEFT;");

        VBox infoBox = new VBox(5);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);

        Label nameLabel = new Label(item.food.getFoodName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        double calories = item.food.getCaloriesPerServing() * item.quantity;
        double protein = item.food.getProtein() * item.quantity;
        double carbs = item.food.getCarbs() * item.quantity;
        double fats = item.food.getFats() * item.quantity;

        Label detailsLabel = new Label(String.format("Qty: %.1f | Cal: %.0f | P: %.1fg | C: %.1fg | F: %.1fg",
                item.quantity, calories, protein, carbs, fats));
        detailsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

        infoBox.getChildren().addAll(nameLabel, detailsLabel);

        Button removeButton = new Button("✕");
        removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 10;");
        removeButton.setOnAction(e -> removeMealItem(index));

        box.getChildren().addAll(infoBox, removeButton);
        return box;
    }

    private void removeMealItem(int index) {
        currentMealItems.remove(index);
        updateMealItemsDisplay();
        updateTotals();
    }

    private void updateTotals() {
        double totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFats = 0;

        for (MealItemData item : currentMealItems) {
            totalCalories += item.food.getCaloriesPerServing() * item.quantity;
            totalProtein += item.food.getProtein() * item.quantity;
            totalCarbs += item.food.getCarbs() * item.quantity;
            totalFats += item.food.getFats() * item.quantity;
        }

        totalCaloriesLabel.setText(String.format("%.0f kcal", totalCalories));
        totalProteinLabel.setText(String.format("%.1f g", totalProtein));
        totalCarbsLabel.setText(String.format("%.1f g", totalCarbs));
        totalFatsLabel.setText(String.format("%.1f g", totalFats));
    }

    @FXML
    private void saveMeal() {
        if (currentMealItems.isEmpty()) {
            showAlert("Error", "Please add at least one food item to the meal.", Alert.AlertType.ERROR);
            return;
        }

        String mealType = mealTypeComboBox.getValue();
        String mealTimeStr = mealTimeField.getText();

        try {
            LocalTime mealTime = LocalTime.parse(mealTimeStr);
            Member member = (Member) Session.getInstance().getCurrentUser();

            double totalCalories = 0;
            double totalProtein = 0;
            double totalCarbs = 0;
            double totalFats = 0;

            for (MealItemData item : currentMealItems) {
                totalCalories += item.food.getCaloriesPerServing() * item.quantity;
                totalProtein += item.food.getProtein() * item.quantity;
                totalCarbs += item.food.getCarbs() * item.quantity;
                totalFats += item.food.getFats() * item.quantity;
            }

            Meal meal = new Meal();
            meal.setMemberId(member.getMemberId());
            meal.setMealType(mealType);
            meal.setMealDate(LocalDate.now());
            meal.setMealTime(mealTime);
            meal.setTotalCalories((int) totalCalories);
            meal.setTotalProtein(totalProtein);
            meal.setTotalCarbs(totalCarbs);
            meal.setTotalFats(totalFats);
            meal.setNotes(notesTextArea.getText());

            List<MealItem> mealItems = new ArrayList<>();
            for (MealItemData itemData : currentMealItems) {
                MealItem mealItem = new MealItem();
                mealItem.setFoodId(itemData.food.getFoodId());
                mealItem.setQuantity(itemData.quantity);
                mealItem.setUnit(itemData.food.getServingSize());
                mealItem.setCalculatedCalories((int) (itemData.food.getCaloriesPerServing() * itemData.quantity));
                mealItems.add(mealItem);
            }

            boolean success = mealDAO.logMeal(meal, mealItems);

            if (success) {
                showAlert("Success", "Meal logged successfully! 🎉", Alert.AlertType.INFORMATION);
                handleBack();
            } else {
                showAlert("Error", "Failed to log meal. Please try again.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("Error saving meal: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to save meal: " + e.getMessage(), Alert.AlertType.ERROR);
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

