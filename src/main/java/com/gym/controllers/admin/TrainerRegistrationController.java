package com.gym.controllers. admin;

import com.gym.dao.TrainerDAO;
import com.gym.models.Admin;
import com.gym.models.Trainer;
import com. gym.services.Session;
import com.gym.utils.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.security.SecureRandom;
import java. time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TrainerRegistrationController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField educationField;

    @FXML
    private TextField certificationsField;

    @FXML
    private Spinner<Integer> experienceSpinner;

    @FXML
    private CheckBox strengthTrainingCheck;

    @FXML
    private CheckBox cardioCheck;

    @FXML
    private CheckBox yogaCheck;

    @FXML
    private CheckBox nutritionCheck;

    @FXML
    private CheckBox crossfitCheck;

    @FXML
    private CheckBox weightLossCheck;

    @FXML
    private CheckBox muscleGainCheck;

    @FXML
    private TextField salaryField;

    @FXML
    private Spinner<Integer> maxClientsSpinner;

    @FXML
    private ComboBox<String> accountStatusComboBox;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button registerButton;

    @FXML
    private Button clearButton;

    private TrainerDAO trainerDAO;
    private String generatedPassword;

    public TrainerRegistrationController() {
        trainerDAO = new TrainerDAO();
    }

    @FXML
    public void initialize() {
        setupSpinners();
        setupComboBox();
        setupEventHandlers();
        setupAutoGeneration();

        // Clear form on initialization
        clearForm();
    }

    private void setupSpinners() {
        SpinnerValueFactory<Integer> experienceFactory =
                new SpinnerValueFactory. IntegerSpinnerValueFactory(0, 50, 0);
        experienceSpinner. setValueFactory(experienceFactory);

        SpinnerValueFactory<Integer> maxClientsFactory =
                new SpinnerValueFactory. IntegerSpinnerValueFactory(1, 50, 10);
        maxClientsSpinner.setValueFactory(maxClientsFactory);
    }

    private void setupComboBox() {
        accountStatusComboBox.getItems().clear();
        accountStatusComboBox.getItems().addAll("ACTIVE", "PROBATION");
    }

    private void setupEventHandlers() {
        registerButton.setOnAction(event -> handleRegisterTrainer());
        clearButton.setOnAction(event -> clearForm());
    }

    private void setupAutoGeneration() {
        fullNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                usernameField.setText(generateUsername(newVal));
            } else {
                usernameField.clear();
            }
        });
    }

    private void handleRegisterTrainer() {
        try {
            if (! validateInputs()) {
                return;
            }

            // Generate password
            generatedPassword = generatePassword();
            passwordField.setText(generatedPassword);

            // Create trainer object
            Trainer trainer = createTrainerFromInputs();

            System.out.println("🔄 Attempting to register trainer:  " + trainer.getUsername() + " (" + trainer.getEmail() + ")");

            // Register trainer
            boolean success = trainerDAO.registerTrainer(trainer);

            if (success) {
                showCredentialsDialog(trainer);
                clearForm();

                // Close the window after successful registration
                Stage stage = (Stage) registerButton.getScene().getWindow();
                stage. close();
            } else {
                ValidationUtil.showAlert("Registration Failed",
                        "Could not register trainer.\n\n" +
                                "Possible reasons:\n" +
                                "• Username '" + trainer.getUsername() + "' already exists\n" +
                                "• Email '" + trainer.getEmail() + "' already exists\n\n" +
                                "Please use a different name or email.",
                        Alert.AlertType.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            ValidationUtil. showAlert("Error",
                    "An error occurred during registration:  " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private boolean validateInputs() {
        if (! ValidationUtil.isNotEmpty(fullNameField.getText())) {
            ValidationUtil.showAlert("Validation Error", "Full name is required!", Alert.AlertType.ERROR);
            fullNameField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isValidEmail(emailField.getText())) {
            ValidationUtil.showAlert("Validation Error", "Valid email is required!", Alert.AlertType.ERROR);
            emailField. requestFocus();
            return false;
        }

        if (! ValidationUtil.isNotEmpty(phoneField.getText())) {
            ValidationUtil.showAlert("Validation Error", "Phone number is required!", Alert.AlertType.ERROR);
            phoneField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isNotEmpty(educationField.getText())) {
            ValidationUtil. showAlert("Validation Error", "Education is required!", Alert.AlertType.ERROR);
            educationField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isNotEmpty(certificationsField.getText())) {
            ValidationUtil.showAlert("Validation Error", "Certifications are required!", Alert.AlertType.ERROR);
            certificationsField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isNotEmpty(salaryField.getText())) {
            ValidationUtil.showAlert("Validation Error", "Salary is required!", Alert.AlertType.ERROR);
            salaryField.requestFocus();
            return false;
        }

        try {
            double salary = Double.parseDouble(salaryField.getText());
            if (salary < 0) {
                ValidationUtil.showAlert("Validation Error", "Salary cannot be negative!", Alert.AlertType.ERROR);
                salaryField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            ValidationUtil.showAlert("Validation Error", "Salary must be a valid number!", Alert.AlertType.ERROR);
            salaryField.requestFocus();
            return false;
        }

        if (accountStatusComboBox.getValue() == null) {
            ValidationUtil.showAlert("Validation Error", "Account status is required!", Alert.AlertType.ERROR);
            accountStatusComboBox.requestFocus();
            return false;
        }

        List<String> specializations = getSelectedSpecializations();
        if (specializations.isEmpty()) {
            ValidationUtil.showAlert("Validation Error", "At least one specialization must be selected!", Alert.AlertType. ERROR);
            return false;
        }

        return true;
    }

    private Trainer createTrainerFromInputs() {
        Trainer trainer = new Trainer();

        trainer.setUsername(usernameField.getText().trim());
        trainer.setPasswordHash(generatedPassword);
        trainer.setFullName(fullNameField.getText().trim());
        trainer.setEmail(emailField.getText().trim().toLowerCase());
        trainer.setPhone(phoneField.getText().trim());
        trainer.setSpecializations(getSelectedSpecializations());
        trainer.setExperienceYears(experienceSpinner.getValue());
        trainer.setCertifications(certificationsField.getText().trim());
        trainer.setMaxClients(maxClientsSpinner.getValue());
        trainer.setAccountStatus(accountStatusComboBox.getValue());
        trainer.setSalary(Double.parseDouble(salaryField. getText().trim()));

        Admin admin = (Admin) Session.getInstance().getCurrentUser();
        if (admin != null) {
            trainer.setHiredByAdminId(admin.getAdminId());
        }

        trainer.setHireDate(LocalDateTime.now());

        return trainer;
    }

    private List<String> getSelectedSpecializations() {
        List<String> specializations = new ArrayList<>();

        if (strengthTrainingCheck. isSelected()) specializations.add("Strength Training");
        if (cardioCheck.isSelected()) specializations.add("Cardio");
        if (yogaCheck. isSelected()) specializations.add("Yoga");
        if (nutritionCheck.isSelected()) specializations.add("Nutrition");
        if (crossfitCheck.isSelected()) specializations.add("CrossFit");
        if (weightLossCheck.isSelected()) specializations.add("Weight Loss");
        if (muscleGainCheck.isSelected()) specializations.add("Muscle Gain");

        return specializations;
    }

    private String generateUsername(String fullName) {
        String[] parts = fullName.trim().toLowerCase().split("\\s+");
        String baseUsername = "";

        if (parts. length >= 2) {
            baseUsername = parts[0] + "." + parts[parts.length - 1];
        } else if (parts.length == 1) {
            baseUsername = parts[0];
        }

        // Add random number to make it more unique
        int random = (int) (Math.random() * 100);
        return baseUsername + random;
    }

    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(10);

        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    private void showCredentialsDialog(Trainer trainer) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("✅ Trainer Registered Successfully");
        alert.setHeaderText("Trainer has been registered!");
        alert.setContentText(
                "═══════════════════════════════\n" +
                        "TRAINER DETAILS\n" +
                        "═══════════════════════════════\n\n" +
                        "Name:  " + trainer.getFullName() + "\n" +
                        "Email: " + trainer.getEmail() + "\n" +
                        "Phone: " + trainer.getPhone() + "\n\n" +
                        "USERNAME: " + trainer.getUsername() + "\n" +
                        "PASSWORD: " + generatedPassword + "\n\n" +
                        "Specializations: " + String.join(", ", trainer.getSpecializations()) + "\n" +
                        "Experience:  " + trainer.getExperienceYears() + " years\n" +
                        "Salary: $" + String.format("%.2f", trainer.getSalary()) + "\n" +
                        "Status: " + trainer.getAccountStatus() + "\n\n" +
                        "⚠️  Please save these credentials!\n" +
                        "The password cannot be recovered."
        );

        alert.showAndWait();
    }

    private void clearForm() {
        fullNameField.clear();
        emailField.clear();
        phoneField.clear();
        educationField.clear();
        certificationsField.clear();

        if (experienceSpinner. getValueFactory() != null) {
            experienceSpinner.getValueFactory().setValue(0);
        }

        strengthTrainingCheck.setSelected(false);
        cardioCheck.setSelected(false);
        yogaCheck.setSelected(false);
        nutritionCheck.setSelected(false);
        crossfitCheck.setSelected(false);
        weightLossCheck.setSelected(false);
        muscleGainCheck.setSelected(false);

        salaryField.clear();

        if (maxClientsSpinner. getValueFactory() != null) {
            maxClientsSpinner. getValueFactory().setValue(10);
        }

        accountStatusComboBox. setValue(null);
        usernameField.clear();
        passwordField.clear();

        System.out.println("🔄 Form cleared and reset");
    }
}