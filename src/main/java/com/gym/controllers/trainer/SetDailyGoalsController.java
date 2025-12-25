package com.gym. controllers.trainer;

import com. gym.dao.DailyGoalDAO;
import com.gym.dao.TrainerDAO;
import com.gym.models.ClientDetails;
import com.gym. models. Trainer;
import com.gym. models.TrainerDailyGoal;
import com.gym. services.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;

public class SetDailyGoalsController {

    @FXML private ComboBox<ClientDetails> clientComboBox;
    @FXML private DatePicker goalDatePicker;
    @FXML private Spinner<Integer> workoutDurationSpinner;
    @FXML private Spinner<Integer> calorieTargetSpinner;
    @FXML private Spinner<Integer> waterIntakeSpinner;
    @FXML private Spinner<Integer> calorieLimitSpinner;
    @FXML private TextField proteinField;
    @FXML private TextField carbsField;
    @FXML private TextField fatsField;
    @FXML private TextArea instructionsArea;
    @FXML private Button water2000Btn;
    @FXML private Button water2500Btn;
    @FXML private Button water3000Btn;
    @FXML private Button water3500Btn;
    @FXML private Button assignBtn;
    @FXML private Button setWeekBtn;
    @FXML private Button clearBtn;

    private TrainerDAO trainerDAO;
    private DailyGoalDAO dailyGoalDAO;
    private Trainer currentTrainer;

    public SetDailyGoalsController() {
        trainerDAO = new TrainerDAO();
        dailyGoalDAO = new DailyGoalDAO();
    }

    @FXML
    public void initialize() {
        System.out.println("========================================");
        System.out.println("✅ SetDailyGoalsController initialized");
        System.out.println("========================================");

        currentTrainer = (Trainer) Session.getInstance().getCurrentUser();

        if (currentTrainer == null) {
            System.err.println("❌ No trainer in session!");
            return;
        }

        setupSpinners();
        loadClients();
        setupEventHandlers();

        // Set default date to today
        if (goalDatePicker != null) {
            goalDatePicker.setValue(LocalDate.now());
        }
    }

    private void setupSpinners() {
        // Workout Duration:  0-300 minutes, step 5, default 60
        workoutDurationSpinner.setValueFactory(
                new SpinnerValueFactory. IntegerSpinnerValueFactory(0, 300, 60, 5)
        );

        // Calorie Target: 0-2000 kcal, step 50, default 500
        calorieTargetSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 2000, 500, 50)
        );

        // Water Intake: 0-5000 ml, step 100, default 2000
        waterIntakeSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5000, 2000, 100)
        );

        // Calorie Limit: 1000-5000 kcal, step 100, default 2000
        calorieLimitSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1000, 5000, 2000, 100)
        );

        // Make spinners editable
        workoutDurationSpinner.setEditable(true);
        calorieTargetSpinner.setEditable(true);
        waterIntakeSpinner. setEditable(true);
        calorieLimitSpinner.setEditable(true);
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
                        setText(item. getMemberName());
                    }
                }
            });

            System.out.println("✅ Loaded " + clients.size() + " clients");

        } catch (Exception e) {
            System.err.println("❌ Error loading clients: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        // Water intake preset buttons
        if (water2000Btn != null) {
            water2000Btn.setOnAction(e -> waterIntakeSpinner.getValueFactory().setValue(2000));
        }
        if (water2500Btn != null) {
            water2500Btn.setOnAction(e -> waterIntakeSpinner. getValueFactory().setValue(2500));
        }
        if (water3000Btn != null) {
            water3000Btn.setOnAction(e -> waterIntakeSpinner.getValueFactory().setValue(3000));
        }
        if (water3500Btn != null) {
            water3500Btn.setOnAction(e -> waterIntakeSpinner.getValueFactory().setValue(3500));
        }

        // Action buttons
        if (assignBtn != null) {
            assignBtn. setOnAction(e -> assignGoals());
        }
        if (setWeekBtn != null) {
            setWeekBtn.setOnAction(e -> setForWeek());
        }
        if (clearBtn != null) {
            clearBtn.setOnAction(e -> clearForm());
        }
    }

    private void assignGoals() {
        try {
            // Validation
            ClientDetails selectedClient = clientComboBox.getValue();
            if (selectedClient == null) {
                showAlert("Validation Error", "Please select a client", Alert.AlertType.WARNING);
                return;
            }

            LocalDate selectedDate = goalDatePicker.getValue();
            if (selectedDate == null) {
                showAlert("Validation Error", "Please select a goal date", Alert.AlertType.WARNING);
                return;
            }

            String proteinText = proteinField.getText().trim();
            String carbsText = carbsField.getText().trim();
            String fatsText = fatsField.getText().trim();

            if (proteinText.isEmpty() || carbsText.isEmpty() || fatsText.isEmpty()) {
                showAlert("Validation Error", "Please fill in all macro targets", Alert.AlertType.WARNING);
                return;
            }

            // Create goal object
            TrainerDailyGoal goal = new TrainerDailyGoal();
            goal.setTrainerId(currentTrainer.getTrainerId());
            goal.setMemberId(selectedClient.getMemberId());
            goal.setGoalDate(selectedDate);
            goal.setWorkoutDuration(workoutDurationSpinner.getValue());
            goal.setCalorieTarget(calorieTargetSpinner.getValue());
            goal.setWaterIntakeMl(waterIntakeSpinner.getValue());
            goal.setCalorieLimit(calorieLimitSpinner.getValue());
            goal.setProteinTarget(Integer.parseInt(proteinText));
            goal.setCarbsTarget(Integer.parseInt(carbsText));
            goal.setFatsTarget(Integer.parseInt(fatsText));
            goal.setSpecialInstructions(instructionsArea.getText().trim());

            // Save to database
            boolean success = dailyGoalDAO. setDailyGoals(goal);

            if (success) {
                showAlert("Success",
                        "Daily goals set for " + selectedClient.getMemberName() + " on " + selectedDate,
                        Alert.AlertType.INFORMATION);
                clearForm();
            } else {
                showAlert("Error", "Failed to set daily goals.  Please try again.", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for macro targets", Alert.AlertType.ERROR);
        } catch (Exception e) {
            System.err.println("❌ Error assigning goals: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "An error occurred:  " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setForWeek() {
        try {
            // Validation
            ClientDetails selectedClient = clientComboBox.getValue();
            if (selectedClient == null) {
                showAlert("Validation Error", "Please select a client", Alert.AlertType.WARNING);
                return;
            }

            LocalDate selectedDate = goalDatePicker.getValue();
            if (selectedDate == null) {
                showAlert("Validation Error", "Please select a start date", Alert.AlertType.WARNING);
                return;
            }

            String proteinText = proteinField.getText().trim();
            String carbsText = carbsField.getText().trim();
            String fatsText = fatsField.getText().trim();

            if (proteinText.isEmpty() || carbsText.isEmpty() || fatsText.isEmpty()) {
                showAlert("Validation Error", "Please fill in all macro targets", Alert.AlertType.WARNING);
                return;
            }

            // Confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Weekly Goals");
            confirmAlert.setHeaderText("Set goals for 7 days? ");
            confirmAlert.setContentText("This will create/update goals from " + selectedDate +
                    " to " + selectedDate.plusDays(6));

            if (confirmAlert.showAndWait().get() != ButtonType.OK) {
                return;
            }

            // Create base goal object
            TrainerDailyGoal baseGoal = new TrainerDailyGoal();
            baseGoal.setTrainerId(currentTrainer.getTrainerId());
            baseGoal. setMemberId(selectedClient. getMemberId());
            baseGoal.setGoalDate(selectedDate);
            baseGoal. setWorkoutDuration(workoutDurationSpinner.getValue());
            baseGoal.setCalorieTarget(calorieTargetSpinner.getValue());
            baseGoal.setWaterIntakeMl(waterIntakeSpinner.getValue());
            baseGoal.setCalorieLimit(calorieLimitSpinner.getValue());
            baseGoal.setProteinTarget(Integer.parseInt(proteinText));
            baseGoal.setCarbsTarget(Integer. parseInt(carbsText));
            baseGoal.setFatsTarget(Integer.parseInt(fatsText));
            baseGoal.setSpecialInstructions(instructionsArea.getText().trim());

            // Save for 7 days
            boolean success = dailyGoalDAO.setGoalsForWeek(baseGoal, 7);

            if (success) {
                showAlert("Success",
                        "Goals set for " + selectedClient.getMemberName() + " for 7 days starting " + selectedDate,
                        Alert.AlertType.INFORMATION);
                clearForm();
            } else {
                showAlert("Error", "Failed to set weekly goals. Please try again.", Alert.AlertType.ERROR);
            }

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for macro targets", Alert.AlertType.ERROR);
        } catch (Exception e) {
            System. err.println("❌ Error setting weekly goals: " + e. getMessage());
            e.printStackTrace();
            showAlert("Error", "An error occurred: " + e. getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void clearForm() {
        clientComboBox.setValue(null);
        goalDatePicker.setValue(LocalDate.now());
        workoutDurationSpinner.getValueFactory().setValue(60);
        calorieTargetSpinner.getValueFactory().setValue(500);
        waterIntakeSpinner.getValueFactory().setValue(2000);
        calorieLimitSpinner.getValueFactory().setValue(2000);
        proteinField.clear();
        carbsField.clear();
        fatsField.clear();
        instructionsArea.clear();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}