package com.gym.controllers.trainer;

import com.gym.dao.MealPlanDAO;
import com.gym.dao.TrainerDAO;
import com.gym.models.*;
import com.gym.services.Session;
import javafx.collections.FXCollections;
import javafx.collections. ObservableList;
import javafx.fxml.FXML;
import javafx.scene. control.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateMealPlanController {

    // Top controls
    @FXML private ComboBox<ClientDetails> clientComboBox;
    @FXML private DatePicker planDatePicker;

    // Breakfast tab
    @FXML private TextField breakfastTimeField;
    @FXML private ComboBox<Food> breakfastFoodComboBox;
    @FXML private TextField breakfastQuantityField;
    @FXML private Button addBreakfastBtn;
    @FXML private ListView<String> breakfastFoodsList;
    @FXML private TextArea breakfastInstructionsArea;
    @FXML private Label breakfastCaloriesLabel;
    @FXML private Label breakfastProteinLabel;
    @FXML private Label breakfastCarbsLabel;
    @FXML private Label breakfastFatsLabel;

    // Lunch tab
    @FXML private TextField lunchTimeField;
    @FXML private ComboBox<Food> lunchFoodComboBox;
    @FXML private TextField lunchQuantityField;
    @FXML private Button addLunchBtn;
    @FXML private ListView<String> lunchFoodsList;
    @FXML private TextArea lunchInstructionsArea;
    @FXML private Label lunchCaloriesLabel;
    @FXML private Label lunchProteinLabel;
    @FXML private Label lunchCarbsLabel;
    @FXML private Label lunchFatsLabel;

    // Dinner tab
    @FXML private TextField dinnerTimeField;
    @FXML private ComboBox<Food> dinnerFoodComboBox;
    @FXML private TextField dinnerQuantityField;
    @FXML private Button addDinnerBtn;
    @FXML private ListView<String> dinnerFoodsList;
    @FXML private TextArea dinnerInstructionsArea;
    @FXML private Label dinnerCaloriesLabel;
    @FXML private Label dinnerProteinLabel;
    @FXML private Label dinnerCarbsLabel;
    @FXML private Label dinnerFatsLabel;

    // Snacks tab
    @FXML private TextField snacksTimeField;
    @FXML private ComboBox<Food> snacksFoodComboBox;
    @FXML private TextField snacksQuantityField;
    @FXML private Button addSnacksBtn;
    @FXML private ListView<String> snacksFoodsList;
    @FXML private TextArea snacksInstructionsArea;
    @FXML private Label snacksCaloriesLabel;
    @FXML private Label snacksProteinLabel;
    @FXML private Label snacksCarbsLabel;
    @FXML private Label snacksFatsLabel;

    // Daily totals
    @FXML private Label dailyCaloriesLabel;
    @FXML private Label dailyProteinLabel;
    @FXML private Label dailyCarbsLabel;
    @FXML private Label dailyFatsLabel;

    // Action buttons
    @FXML private Button assignMealPlanBtn;
    @FXML private Button saveTemplateBtn;
    @FXML private Button clearBtn;

    private TrainerDAO trainerDAO;
    private MealPlanDAO mealPlanDAO;
    private Trainer currentTrainer;

    // Meal data storage
    private Map<String, List<FoodItem>> mealFoods = new HashMap<>();
    private Map<String, MealTotals> mealTotals = new HashMap<>();

    public CreateMealPlanController() {
        trainerDAO = new TrainerDAO();
        mealPlanDAO = new MealPlanDAO();

        // Initialize meal storage
        mealFoods.put("BREAKFAST", new ArrayList<>());
        mealFoods.put("LUNCH", new ArrayList<>());
        mealFoods.put("DINNER", new ArrayList<>());
        mealFoods.put("SNACK", new ArrayList<>());

        mealTotals. put("BREAKFAST", new MealTotals());
        mealTotals.put("LUNCH", new MealTotals());
        mealTotals. put("DINNER", new MealTotals());
        mealTotals.put("SNACK", new MealTotals());
    }

    @FXML
    public void initialize() {
        System.out.println("========================================");
        System.out.println("✅ CreateMealPlanController initialized");
        System.out.println("========================================");

        currentTrainer = (Trainer) Session.getInstance().getCurrentUser();

        if (currentTrainer == null) {
            System.err.println("❌ No trainer in session!");
            return;
        }

        loadClients();
        loadFoods();
        setupEventHandlers();

        // Set default date to today
        if (planDatePicker != null) {
            planDatePicker.setValue(LocalDate.now());
        }

        // Set default meal times
        if (breakfastTimeField != null) breakfastTimeField.setText("08:00");
        if (lunchTimeField != null) lunchTimeField.setText("13:00");
        if (dinnerTimeField != null) dinnerTimeField.setText("19:00");
        if (snacksTimeField != null) snacksTimeField.setText("16:00");
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
            System.err.println("❌ Error loading clients: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFoods() {
        try {
            List<Food> foods = mealPlanDAO.getAllFoods();
            ObservableList<Food> foodList = FXCollections.observableArrayList(foods);

            // Set same food list for all meal tabs
            breakfastFoodComboBox.setItems(foodList);
            lunchFoodComboBox.setItems(foodList);
            dinnerFoodComboBox.setItems(foodList);
            snacksFoodComboBox.setItems(foodList);

            System.out.println("✅ Loaded " + foods.size() + " foods");

        } catch (Exception e) {
            System.err.println("❌ Error loading foods: " + e. getMessage());
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        // Add food buttons
        if (addBreakfastBtn != null) {
            addBreakfastBtn.setOnAction(e -> addFoodToMeal("BREAKFAST", breakfastFoodComboBox,
                    breakfastQuantityField, breakfastFoodsList));
        }
        if (addLunchBtn != null) {
            addLunchBtn.setOnAction(e -> addFoodToMeal("LUNCH", lunchFoodComboBox,
                    lunchQuantityField, lunchFoodsList));
        }
        if (addDinnerBtn != null) {
            addDinnerBtn.setOnAction(e -> addFoodToMeal("DINNER", dinnerFoodComboBox,
                    dinnerQuantityField, dinnerFoodsList));
        }
        if (addSnacksBtn != null) {
            addSnacksBtn.setOnAction(e -> addFoodToMeal("SNACK", snacksFoodComboBox,
                    snacksQuantityField, snacksFoodsList));
        }

        // Action buttons
        if (assignMealPlanBtn != null) {
            assignMealPlanBtn.setOnAction(e -> assignMealPlan());
        }
        if (saveTemplateBtn != null) {
            saveTemplateBtn.setOnAction(e -> saveAsTemplate());
        }
        if (clearBtn != null) {
            clearBtn.setOnAction(e -> clearAll());
        }
    }

    private void addFoodToMeal(String mealType, ComboBox<Food> foodComboBox,
                               TextField quantityField, ListView<String> foodsList) {
        try {
            Food selectedFood = foodComboBox.getValue();
            if (selectedFood == null) {
                showAlert("Validation Error", "Please select a food", Alert.AlertType.WARNING);
                return;
            }

            String quantityText = quantityField.getText().trim();
            if (quantityText.isEmpty()) {
                showAlert("Validation Error", "Please enter quantity", Alert.AlertType. WARNING);
                return;
            }

            double quantity = Double.parseDouble(quantityText);

            // Create food item
            FoodItem item = new FoodItem(selectedFood, quantity);
            mealFoods.get(mealType).add(item);

            // Update ListView
            foodsList.getItems().add(item.toString());

            // Calculate totals
            calculateMealTotals(mealType);

            // Clear inputs
            foodComboBox.setValue(null);
            quantityField.clear();

            System.out.println("✅ Added " + selectedFood.getFoodName() + " to " + mealType);

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid quantity", Alert.AlertType.ERROR);
        }
    }

    private void calculateMealTotals(String mealType) {
        MealTotals totals = new MealTotals();

        for (FoodItem item : mealFoods. get(mealType)) {
            totals.calories += item.food.getCaloriesPerServing() * item.quantity;
            totals.protein += item.food. getProtein() * item.quantity;
            totals.carbs += item. food.getCarbs() * item.quantity;
            totals. fats += item.food.getFats() * item.quantity;
        }

        mealTotals.put(mealType, totals);

        // Round to 1 decimal place
        double protein = Math.round(totals. protein * 10.0) / 10.0;
        double carbs = Math.round(totals.carbs * 10.0) / 10.0;
        double fats = Math. round(totals.fats * 10.0) / 10.0;

        // Update labels
        switch (mealType) {
            case "BREAKFAST":
                breakfastCaloriesLabel.setText("Calories: " + (int)totals.calories);
                breakfastProteinLabel.setText("Protein: " + protein + "g");
                breakfastCarbsLabel.setText("Carbs: " + carbs + "g");
                breakfastFatsLabel.setText("Fats: " + fats + "g");
                break;
            case "LUNCH":
                lunchCaloriesLabel.setText("Calories: " + (int)totals.calories);
                lunchProteinLabel.setText("Protein: " + protein + "g");
                lunchCarbsLabel.setText("Carbs: " + carbs + "g");
                lunchFatsLabel.setText("Fats: " + fats + "g");
                break;
            case "DINNER":
                dinnerCaloriesLabel.setText("Calories: " + (int)totals.calories);
                dinnerProteinLabel. setText("Protein: " + protein + "g");
                dinnerCarbsLabel.setText("Carbs: " + carbs + "g");
                dinnerFatsLabel.setText("Fats: " + fats + "g");
                break;
            case "SNACK":
                snacksCaloriesLabel.setText("Calories: " + (int)totals.calories);
                snacksProteinLabel.setText("Protein: " + protein + "g");
                snacksCarbsLabel.setText("Carbs: " + carbs + "g");
                snacksFatsLabel.setText("Fats: " + fats + "g");
                break;
        }

        // Update daily totals
        updateDailyTotals();
    }

    private void updateDailyTotals() {
        double totalCals = 0, totalProtein = 0, totalCarbs = 0, totalFats = 0;

        for (MealTotals meal : mealTotals.values()) {
            totalCals += meal.calories;
            totalProtein += meal.protein;
            totalCarbs += meal.carbs;
            totalFats += meal.fats;
        }

        dailyCaloriesLabel. setText(String.valueOf((int)totalCals));
        dailyProteinLabel.setText(String.format("%.1f", totalProtein) + "g");
        dailyCarbsLabel.setText(String. format("%.1f", totalCarbs) + "g");
        dailyFatsLabel.setText(String.format("%.1f", totalFats) + "g");
    }

    private void assignMealPlan() {
        try {
            // Validation
            ClientDetails selectedClient = clientComboBox.getValue();
            if (selectedClient == null) {
                showAlert("Validation Error", "Please select a client", Alert.AlertType.WARNING);
                return;
            }

            LocalDate selectedDate = planDatePicker.getValue();
            if (selectedDate == null) {
                showAlert("Validation Error", "Please select a plan date", Alert.AlertType.WARNING);
                return;
            }

            // Check if at least one meal has foods
            boolean hasFood = false;
            for (List<FoodItem> foods : mealFoods.values()) {
                if (!foods.isEmpty()) {
                    hasFood = true;
                    break;
                }
            }

            if (!hasFood) {
                showAlert("Validation Error", "Please add at least one food to any meal", Alert.AlertType.WARNING);
                return;
            }

            // Create meal plans
            List<MealPlan> meals = new ArrayList<>();

            meals.add(createMealPlan("BREAKFAST", selectedClient, selectedDate,
                    breakfastTimeField.getText(), breakfastInstructionsArea.getText()));
            meals.add(createMealPlan("LUNCH", selectedClient, selectedDate,
                    lunchTimeField.getText(), lunchInstructionsArea.getText()));
            meals.add(createMealPlan("DINNER", selectedClient, selectedDate,
                    dinnerTimeField.getText(), dinnerInstructionsArea.getText()));
            meals.add(createMealPlan("SNACK", selectedClient, selectedDate,
                    snacksTimeField.getText(), snacksInstructionsArea.getText()));

            // Save to database
            boolean success = mealPlanDAO. createMealPlan(meals);

            if (success) {
                showAlert("Success",
                        "Meal plan assigned to " + selectedClient.getMemberName() + " successfully!",
                        Alert.AlertType.INFORMATION);
                clearAll();
            } else {
                showAlert("Error", "Failed to assign meal plan.  Please try again.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("❌ Error assigning meal plan:  " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "An error occurred:  " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private MealPlan createMealPlan(String mealType, ClientDetails client, LocalDate date,
                                    String timeText, String instructions) {
        MealPlan plan = new MealPlan();
        plan.setTrainerId(currentTrainer.getTrainerId());
        plan.setMemberId(client.getMemberId());
        plan.setPlanDate(date);
        plan.setMealType(mealType);

        // Parse time
        try {
            if (timeText != null && !timeText.trim().isEmpty()) {
                plan.setMealTime(LocalTime.parse(timeText. trim(), DateTimeFormatter.ofPattern("HH:mm")));
            }
        } catch (Exception e) {
            System.err.println("❌ Error parsing time:  " + timeText);
        }

        // Build foods string
        StringBuilder foodsBuilder = new StringBuilder();
        for (FoodItem item : mealFoods.get(mealType)) {
            if (foodsBuilder.length() > 0) foodsBuilder.append(", ");
            foodsBuilder.append(item.quantity).append("x ").append(item.food.getFoodName());
        }
        plan.setFoods(foodsBuilder.toString());

        // Set totals
        MealTotals totals = mealTotals.get(mealType);
        plan.setTotalCalories((int)totals.calories);
        plan.setTotalProtein(totals.protein);
        plan.setTotalCarbs(totals.carbs);
        plan.setTotalFats(totals.fats);
        plan.setPreparationInstructions(instructions);

        return plan;
    }

    private void saveAsTemplate() {
        showAlert("Coming Soon", "Save as Template feature will be available soon!", Alert.AlertType.INFORMATION);
    }

    private void clearAll() {
        clientComboBox.setValue(null);
        planDatePicker.setValue(LocalDate.now());

        // Clear all meals
        for (String mealType : mealFoods.keySet()) {
            mealFoods.get(mealType).clear();
            mealTotals.get(mealType).reset();
        }

        // Clear UI
        breakfastFoodsList.getItems().clear();
        lunchFoodsList.getItems().clear();
        dinnerFoodsList.getItems().clear();
        snacksFoodsList. getItems().clear();

        breakfastInstructionsArea.clear();
        lunchInstructionsArea.clear();
        dinnerInstructionsArea.clear();
        snacksInstructionsArea.clear();

        // Reset labels
        breakfastCaloriesLabel.setText("Calories: 0");
        breakfastProteinLabel.setText("Protein: 0g");
        breakfastCarbsLabel.setText("Carbs: 0g");
        breakfastFatsLabel.setText("Fats: 0g");

        lunchCaloriesLabel.setText("Calories: 0");
        lunchProteinLabel.setText("Protein: 0g");
        lunchCarbsLabel. setText("Carbs: 0g");
        lunchFatsLabel.setText("Fats: 0g");

        dinnerCaloriesLabel.setText("Calories: 0");
        dinnerProteinLabel.setText("Protein: 0g");
        dinnerCarbsLabel.setText("Carbs: 0g");
        dinnerFatsLabel.setText("Fats: 0g");

        snacksCaloriesLabel.setText("Calories: 0");
        snacksProteinLabel.setText("Protein: 0g");
        snacksCarbsLabel. setText("Carbs: 0g");
        snacksFatsLabel.setText("Fats: 0g");

        dailyCaloriesLabel.setText("0");
        dailyProteinLabel.setText("0g");
        dailyCarbsLabel.setText("0g");
        dailyFatsLabel.setText("0g");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner classes
    private static class FoodItem {
        Food food;
        double quantity;

        FoodItem(Food food, double quantity) {
            this.food = food;
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return quantity + "x " + food.getFoodName() + " (" +
                    (int)(food.getCaloriesPerServing() * quantity) + " cal)";
        }
    }

    private static class MealTotals {
        double calories = 0;
        double protein = 0;
        double carbs = 0;
        double fats = 0;

        void reset() {
            calories = protein = carbs = fats = 0;
        }
    }
}