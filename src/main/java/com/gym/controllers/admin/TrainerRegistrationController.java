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
import java. util.ArrayList;
import java. util.List;

public class TrainerRegistrationController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField educationField;
    @FXML private TextField certificationsField;
    @FXML private Spinner<Integer> experienceSpinner;
    @FXML private CheckBox strengthTrainingCheck;
    @FXML private CheckBox cardioCheck;
    @FXML private CheckBox yogaCheck;
    @FXML private CheckBox nutritionCheck;
    @FXML private CheckBox crossfitCheck;
    @FXML private CheckBox weightLossCheck;
    @FXML private CheckBox muscleGainCheck;
    @FXML private TextField salaryField;
    @FXML private Spinner<Integer> maxClientsSpinner;
    @FXML private ComboBox<String> accountStatusComboBox;
    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private Button registerButton;
    @FXML private Button clearButton;

    private TrainerDAO trainerDAO;
    private String generatedPassword;

    public TrainerRegistrationController() {
        trainerDAO = new TrainerDAO();
    }

    @FXML
    public void initialize() {
        try {
            setupSpinners();
            setupComboBox();
            setupEventHandlers();
            setupAutoGeneration();
            clearForm();
        } catch (Exception e) {
            System.err.println("Error during initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSpinners() {
        try {
            if (experienceSpinner != null) {
                SpinnerValueFactory<Integer> experienceFactory =
                        new SpinnerValueFactory. IntegerSpinnerValueFactory(0, 50, 0);
                experienceSpinner. setValueFactory(experienceFactory);
            }

            if (maxClientsSpinner != null) {
                SpinnerValueFactory<Integer> maxClientsFactory =
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 10);
                maxClientsSpinner.setValueFactory(maxClientsFactory);
            }
        } catch (Exception e) {
            System.err.println("Error setting up spinners: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupComboBox() {
        try {
            if (accountStatusComboBox != null) {
                accountStatusComboBox.getItems().clear();
                accountStatusComboBox.getItems().addAll("ACTIVE", "PROBATION");
            }
        } catch (Exception e) {
            System.err.println("Error setting up combo box: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        try {
            if (registerButton != null) {
                registerButton.setOnAction(event -> {
                    try {
                        handleRegisterTrainer();
                    } catch (Exception e) {
                        System.err.println("Error in register handler: " + e.getMessage());
                        e.printStackTrace();
                        showAlert("Error", "Registration failed: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                });
            }

            if (clearButton != null) {
                clearButton.setOnAction(event -> {
                    try {
                        clearForm();
                    } catch (Exception e) {
                        System.err.println("Error in clear handler: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error setting up event handlers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupAutoGeneration() {
        try {
            if (fullNameField != null && usernameField != null) {
                fullNameField.textProperty().addListener((obs, oldVal, newVal) -> {
                    try {
                        if (newVal != null && ! newVal.trim().isEmpty()) {
                            usernameField.setText(generateUsername(newVal));
                        } else {
                            usernameField.clear();
                        }
                    } catch (Exception e) {
                        System.err.println("Error generating username: " + e. getMessage());
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error setting up auto generation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleRegisterTrainer() {
        try {
            if (! validateInputs()) {
                return;
            }

            // Generate password
            generatedPassword = generatePassword();
            if (passwordField != null) {
                passwordField.setText(generatedPassword);
            }

            // Create trainer object
            Trainer trainer = createTrainerFromInputs();

            System.out.println("🔄 Attempting to register trainer:  " + trainer.getUsername() + " (" + trainer.getEmail() + ")");

            // Register trainer
            boolean success = trainerDAO.registerTrainer(trainer);

            if (success) {
                System.out.println("✅ Trainer registered successfully with ID: " + trainer.getTrainerId());
                showCredentialsDialog(trainer);
                clearForm();

                // DON'T close the window - just show success message
                // Removing the stage. close() prevents the crash

            } else {
                System.out.println("❌ Username or email already exists!");
                showAlert("Registration Failed",
                        "Could not register trainer.\n\n" +
                                "Possible reasons:\n" +
                                "• Username '" + trainer.getUsername() + "' already exists\n" +
                                "• Email '" + trainer.getEmail() + "' already exists\n\n" +
                                "Please use a different name or email.",
                        Alert.AlertType. ERROR);
            }

        } catch (Exception e) {
            System.err.println("❌ Error during registration: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error",
                    "An error occurred during registration:  " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private boolean validateInputs() {
        try {
            if (! ValidationUtil.isNotEmpty(fullNameField.getText())) {
                showAlert("Validation Error", "Full name is required!", Alert.AlertType.ERROR);
                fullNameField.requestFocus();
                return false;
            }

            if (!ValidationUtil.isValidEmail(emailField.getText())) {
                showAlert("Validation Error", "Valid email is required!", Alert.AlertType.ERROR);
                emailField. requestFocus();
                return false;
            }

            if (! ValidationUtil.isNotEmpty(phoneField.getText())) {
                showAlert("Validation Error", "Phone number is required!", Alert.AlertType.ERROR);
                phoneField.requestFocus();
                return false;
            }

            if (!ValidationUtil.isNotEmpty(educationField.getText())) {
                showAlert("Validation Error", "Education is required!", Alert.AlertType.ERROR);
                educationField.requestFocus();
                return false;
            }

            if (!ValidationUtil.isNotEmpty(certificationsField.getText())) {
                showAlert("Validation Error", "Certifications are required!", Alert. AlertType.ERROR);
                certificationsField.requestFocus();
                return false;
            }

            if (!ValidationUtil.isNotEmpty(salaryField.getText())) {
                showAlert("Validation Error", "Salary is required!", Alert. AlertType.ERROR);
                salaryField.requestFocus();
                return false;
            }

            try {
                double salary = Double.parseDouble(salaryField.getText());
                if (salary < 0) {
                    showAlert("Validation Error", "Salary cannot be negative!", Alert.AlertType. ERROR);
                    salaryField.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Validation Error", "Salary must be a valid number!", Alert.AlertType.ERROR);
                salaryField.requestFocus();
                return false;
            }

            if (accountStatusComboBox.getValue() == null) {
                showAlert("Validation Error", "Account status is required!", Alert.AlertType.ERROR);
                accountStatusComboBox.requestFocus();
                return false;
            }

            List<String> specializations = getSelectedSpecializations();
            if (specializations.isEmpty()) {
                showAlert("Validation Error", "At least one specialization must be selected!", Alert.AlertType. ERROR);
                return false;
            }

            return true;

        } catch (Exception e) {
            System.err.println("Error during validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
        trainer.setSalary(Double.parseDouble(salaryField.getText().trim()));

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
        if (cardioCheck. isSelected()) specializations.add("Cardio");
        if (yogaCheck.isSelected()) specializations.add("Yoga");
        if (nutritionCheck.isSelected()) specializations.add("Nutrition");
        if (crossfitCheck.isSelected()) specializations.add("CrossFit");
        if (weightLossCheck. isSelected()) specializations.add("Weight Loss");
        if (muscleGainCheck.isSelected()) specializations.add("Muscle Gain");

        return specializations;
    }

    private String generateUsername(String fullName) {
        try {
            String[] parts = fullName.trim().toLowerCase().split("\\s+");
            String baseUsername = "";

            if (parts. length >= 2) {
                baseUsername = parts[0] + "." + parts[parts.length - 1];
            } else if (parts.length == 1) {
                baseUsername = parts[0];
            }

            int random = (int) (Math.random() * 100);
            return baseUsername + random;
        } catch (Exception e) {
            System.err.println("Error generating username: " + e.getMessage());
            return "trainer" + (int)(Math.random() * 1000);
        }
    }

    private String generatePassword() {
        try {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
            SecureRandom random = new SecureRandom();
            StringBuilder password = new StringBuilder(10);

            for (int i = 0; i < 10; i++) {
                password. append(chars.charAt(random. nextInt(chars.length())));
            }

            return password.toString();
        } catch (Exception e) {
            System. err.println("Error generating password: " + e.getMessage());
            return "TempPass123!";
        }
    }

    private void showCredentialsDialog(Trainer trainer) {
        try {
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
                            "Experience: " + trainer.getExperienceYears() + " years\n" +
                            "Salary: $" + String.format("%.2f", trainer.getSalary()) + "\n" +
                            "Status: " + trainer.getAccountStatus() + "\n\n" +
                            "⚠️ Please save these credentials!\n" +
                            "The password cannot be recovered."
            );

            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing credentials dialog: " + e. getMessage());
            e.printStackTrace();
        }
    }

    private void clearForm() {
        try {
            if (fullNameField != null) fullNameField.clear();
            if (emailField != null) emailField.clear();
            if (phoneField != null) phoneField.clear();
            if (educationField != null) educationField.clear();
            if (certificationsField != null) certificationsField.clear();

            if (experienceSpinner != null && experienceSpinner.getValueFactory() != null) {
                experienceSpinner.getValueFactory().setValue(0);
            }

            if (strengthTrainingCheck != null) strengthTrainingCheck.setSelected(false);
            if (cardioCheck != null) cardioCheck.setSelected(false);
            if (yogaCheck != null) yogaCheck.setSelected(false);
            if (nutritionCheck != null) nutritionCheck.setSelected(false);
            if (crossfitCheck != null) crossfitCheck.setSelected(false);
            if (weightLossCheck != null) weightLossCheck.setSelected(false);
            if (muscleGainCheck != null) muscleGainCheck.setSelected(false);

            if (salaryField != null) salaryField.clear();

            if (maxClientsSpinner != null && maxClientsSpinner.getValueFactory() != null) {
                maxClientsSpinner.getValueFactory().setValue(10);
            }

            if (accountStatusComboBox != null) accountStatusComboBox.setValue(null);
            if (usernameField != null) usernameField.clear();
            if (passwordField != null) passwordField.clear();

            System.out.println("🔄 Form cleared and reset");
        } catch (Exception e) {
            System.err.println("Error clearing form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert. AlertType type) {
        try {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert. setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error showing alert: " + e.getMessage());
            e.printStackTrace();
        }
    }
}