package com.gym.controllers.trainer;

import com.gym.dao.TrainerDAO;
import com.gym.dao.WorkoutPlanDAO;
import com.gym.models.*;
import com.gym.services.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections. ObservableList;
import javafx.fxml.FXML;
import javafx.scene. control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

public class CreateWorkoutPlanController {

    @FXML private ComboBox<ClientDetails> clientComboBox;
    @FXML private DatePicker planDatePicker;
    @FXML private TextField focusAreaField;
    @FXML private ComboBox<Exercise> exerciseComboBox;
    @FXML private TextField setsField;
    @FXML private TextField repsField;
    @FXML private TextField weightField;
    @FXML private TextField restField;
    @FXML private TextField notesField;
    @FXML private Button addExerciseBtn;
    @FXML private TableView<PlanExercise> exercisesTable;
    @FXML private TableColumn<PlanExercise, String> exerciseNameColumn;
    @FXML private TableColumn<PlanExercise, Integer> setsColumn;
    @FXML private TableColumn<PlanExercise, String> repsColumn;
    @FXML private TableColumn<PlanExercise, Double> weightColumn;
    @FXML private TableColumn<PlanExercise, Integer> restColumn;
    @FXML private TableColumn<PlanExercise, String> notesColumn;
    @FXML private TableColumn<PlanExercise, Void> actionColumn;
    @FXML private TextArea instructionsArea;
    @FXML private Label durationLabel;
    @FXML private Label caloriesLabel;
    @FXML private Button assignBtn;
    @FXML private Button saveTemplateBtn;
    @FXML private Button cancelBtn;

    private TrainerDAO trainerDAO;
    private WorkoutPlanDAO workoutPlanDAO;
    private Trainer currentTrainer;
    private ObservableList<PlanExercise> exercisesList;

    public CreateWorkoutPlanController() {
        trainerDAO = new TrainerDAO();
        workoutPlanDAO = new WorkoutPlanDAO();
        exercisesList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        System.out.println("========================================");
        System.out.println("✅ CreateWorkoutPlanController initialized");
        System.out.println("========================================");

        currentTrainer = (Trainer) Session.getInstance().getCurrentUser();

        if (currentTrainer == null) {
            System.err.println("❌ No trainer in session!");
            return;
        }

        setupTableColumns();
        loadClients();
        loadExercises();
        setupEventHandlers();

        // Set default date to today
        if (planDatePicker != null) {
            planDatePicker.setValue(LocalDate.now());
        }
    }

    private void setupTableColumns() {
        exerciseNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData. getValue().getExerciseName()));

        setsColumn.setCellValueFactory(new PropertyValueFactory<>("sets"));
        repsColumn.setCellValueFactory(new PropertyValueFactory<>("reps"));
        weightColumn. setCellValueFactory(new PropertyValueFactory<>("weight"));
        restColumn.setCellValueFactory(new PropertyValueFactory<>("restSeconds"));
        notesColumn. setCellValueFactory(new PropertyValueFactory<>("trainerNotes"));

        // Add Remove button column
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");

            {
                removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 15;");
                removeBtn.setOnAction(e -> {
                    PlanExercise exercise = getTableView().getItems().get(getIndex());
                    removeExercise(exercise);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeBtn);
                }
            }
        });

        exercisesTable.setItems(exercisesList);
    }

    private void loadClients() {
        if (currentTrainer == null) return;

        try {
            List<ClientDetails> clients = trainerDAO.getMyAssignedClients(currentTrainer.getTrainerId());
            clientComboBox.setItems(FXCollections.observableArrayList(clients));

            // Set custom display
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

            System.out.println("✅ Loaded " + clients.size() + " clients into ComboBox");

        } catch (Exception e) {
            System. err.println("❌ Error loading clients: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadExercises() {
        try {
            List<Exercise> exercises = workoutPlanDAO.getAllExercises();
            exerciseComboBox.setItems(FXCollections.observableArrayList(exercises));

            // Set custom display
            exerciseComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Exercise item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getExerciseName() + " (" + item.getMuscleGroup() + ")");
                    }
                }
            });

            exerciseComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Exercise item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getExerciseName());
                    }
                }
            });

            System. out.println("✅ Loaded " + exercises.size() + " exercises into ComboBox");

        } catch (Exception e) {
            System.err.println("❌ Error loading exercises: " + e. getMessage());
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        if (addExerciseBtn != null) {
            addExerciseBtn. setOnAction(e -> addExercise());
        }

        if (assignBtn != null) {
            assignBtn.setOnAction(e -> assignPlan());
        }

        if (saveTemplateBtn != null) {
            saveTemplateBtn. setOnAction(e -> saveAsTemplate());
        }

        if (cancelBtn != null) {
            cancelBtn.setOnAction(e -> clearForm());
        }

        // Listen for changes in exercise list to recalculate totals
        exercisesList.addListener((javafx.collections.ListChangeListener<PlanExercise>) c -> calculateTotals());
    }

    private void addExercise() {
        try {
            Exercise selectedExercise = exerciseComboBox.getValue();
            if (selectedExercise == null) {
                showAlert("Validation Error", "Please select an exercise", Alert.AlertType.WARNING);
                return;
            }

            String setsText = setsField.getText().trim();
            String repsText = repsField.getText().trim();
            String weightText = weightField.getText().trim();
            String restText = restField.getText().trim();
            String notes = notesField.getText().trim();

            if (setsText.isEmpty() || repsText.isEmpty()) {
                showAlert("Validation Error", "Sets and Reps are required", Alert.AlertType.WARNING);
                return;
            }

            int sets = Integer.parseInt(setsText);
            double weight = weightText.isEmpty() ? 0.0 : Double.parseDouble(weightText);
            int rest = restText.isEmpty() ? 60 : Integer.parseInt(restText);

            PlanExercise planExercise = new PlanExercise(
                    selectedExercise.getExerciseId(),
                    selectedExercise.getExerciseName(),
                    sets,
                    repsText,
                    weight,
                    rest,
                    notes
            );

            exercisesList.add(planExercise);

            // Clear fields
            exerciseComboBox.setValue(null);
            setsField.clear();
            repsField.clear();
            weightField. clear();
            restField.clear();
            notesField.clear();

            System.out.println("✅ Added exercise: " + selectedExercise.getExerciseName());

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for sets, weight, and rest", Alert.AlertType.ERROR);
        }
    }

    private void removeExercise(PlanExercise exercise) {
        exercisesList.remove(exercise);
        System.out.println("✅ Removed exercise: " + exercise.getExerciseName());
    }

    private void calculateTotals() {
        int totalDuration = 0;
        int totalCalories = 0;

        for (PlanExercise exercise :  exercisesList) {
            // Estimate:  each set takes 1 minute + rest time
            int exerciseDuration = exercise.getSets() * (60 + exercise.getRestSeconds()) / 60;
            totalDuration += exerciseDuration;

            // Estimate: 5 calories per set (simple estimation)
            totalCalories += exercise.getSets() * 5;
        }

        if (durationLabel != null) {
            durationLabel.setText(totalDuration + " min");
        }

        if (caloriesLabel != null) {
            caloriesLabel.setText(totalCalories + " kcal");
        }
    }

    private void assignPlan() {
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

            String focusArea = focusAreaField.getText().trim();
            if (focusArea.isEmpty()) {
                showAlert("Validation Error", "Please enter a focus area", Alert.AlertType.WARNING);
                return;
            }

            if (exercisesList.isEmpty()) {
                showAlert("Validation Error", "Please add at least one exercise", Alert.AlertType.WARNING);
                return;
            }

            // Create WorkoutPlan object
            WorkoutPlan plan = new WorkoutPlan();
            plan.setTrainerId(currentTrainer.getTrainerId());
            plan.setMemberId(selectedClient.getMemberId());
            plan.setPlanDate(selectedDate);
            plan.setFocusArea(focusArea);

            String durationText = durationLabel.getText().replace(" min", "").trim();
            plan.setTotalDuration(Integer.parseInt(durationText));

            String caloriesText = caloriesLabel.getText().replace(" kcal", "").trim();
            plan.setExpectedCalories(Integer.parseInt(caloriesText));

            plan.setInstructions(instructionsArea.getText().trim());
            plan.setStatus("ASSIGNED");

            // Save to database
            boolean success = workoutPlanDAO. createWorkoutPlan(plan, exercisesList);

            if (success) {
                showAlert("Success", "Workout plan assigned to " + selectedClient.getMemberName() + " successfully!", Alert.AlertType.INFORMATION);
                clearForm();
            } else {
                showAlert("Error", "Failed to assign workout plan.  Please try again.", Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            System.err.println("❌ Error assigning plan: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "An error occurred:  " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void saveAsTemplate() {
        showAlert("Coming Soon", "Save as Template feature will be available soon!", Alert.AlertType. INFORMATION);
    }

    private void clearForm() {
        clientComboBox.setValue(null);
        planDatePicker.setValue(LocalDate.now());
        focusAreaField.clear();
        exerciseComboBox.setValue(null);
        setsField.clear();
        repsField.clear();
        weightField.clear();
        restField.clear();
        notesField.clear();
        instructionsArea.clear();
        exercisesList.clear();
        durationLabel.setText("0 min");
        caloriesLabel.setText("0 kcal");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}