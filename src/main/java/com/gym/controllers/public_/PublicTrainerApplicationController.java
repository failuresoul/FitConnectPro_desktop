package com.gym.controllers. public_;

import com.gym.dao.ApplicationDAO;
import com. gym.models.TrainerApplication;
import javafx.fxml.FXML;
import javafx.scene. control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class PublicTrainerApplicationController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private Spinner<Integer> ageSpinner;

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
    private TextArea coverLetterArea;

    @FXML
    private Button submitButton;

    @FXML
    private Button cancelButton;

    private ApplicationDAO applicationDAO;

    public PublicTrainerApplicationController() {
        applicationDAO = new ApplicationDAO();
    }

    @FXML
    public void initialize() {
        setupSpinners();
        setupEventHandlers();
    }

    private void setupSpinners() {
        SpinnerValueFactory<Integer> ageFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(18, 70, 25);
        ageSpinner. setValueFactory(ageFactory);

        SpinnerValueFactory<Integer> experienceFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0);
        experienceSpinner. setValueFactory(experienceFactory);
    }

    private void setupEventHandlers() {
        submitButton.setOnAction(event -> submitApplication());
        cancelButton.setOnAction(event -> closeWindow());
    }

    private void submitApplication() {
        if (! validateInputs()) {
            return;
        }

        TrainerApplication application = new TrainerApplication();
        application.setFullName(fullNameField.getText().trim());
        application.setEmail(emailField.getText().trim());
        application.setPhone(phoneField.getText().trim());
        application.setAge(ageSpinner.getValue());
        application. setEducation(educationField. getText().trim());
        application. setCertifications(certificationsField. getText().trim());
        application. setExperienceYears(experienceSpinner.getValue());
        application.setSpecializations(getSelectedSpecializations());
        application.setCoverLetter(coverLetterArea.getText().trim());

        boolean success = applicationDAO.submitApplication(application);

        if (success) {
            showAlert("Success", "Your application has been submitted successfully!\n\nYou will be notified via email once reviewed.", Alert.AlertType.INFORMATION);
            closeWindow();
        } else {
            showAlert("Error", "Failed to submit application. Please try again.", Alert.AlertType. ERROR);
        }
    }

    private boolean validateInputs() {
        if (fullNameField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Full name is required!", Alert.AlertType.ERROR);
            return false;
        }

        if (emailField.getText().trim().isEmpty() || !emailField.getText().contains("@")) {
            showAlert("Validation Error", "Valid email is required!", Alert.AlertType.ERROR);
            return false;
        }

        if (phoneField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Phone number is required!", Alert.AlertType.ERROR);
            return false;
        }

        if (educationField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Education is required!", Alert.AlertType.ERROR);
            return false;
        }

        if (certificationsField.getText().trim().isEmpty()) {
            showAlert("Validation Error", "Certifications are required!", Alert.AlertType.ERROR);
            return false;
        }

        if (getSelectedSpecializations().isEmpty()) {
            showAlert("Validation Error", "At least one specialization must be selected!", Alert.AlertType.ERROR);
            return false;
        }

        if (coverLetterArea. getText().trim().isEmpty()) {
            showAlert("Validation Error", "Cover letter is required!", Alert. AlertType.ERROR);
            return false;
        }

        return true;
    }

    private List<String> getSelectedSpecializations() {
        List<String> specializations = new ArrayList<>();

        if (strengthTrainingCheck. isSelected()) specializations.add("Strength Training");
        if (cardioCheck.isSelected()) specializations.add("Cardio");
        if (yogaCheck.isSelected()) specializations.add("Yoga");
        if (nutritionCheck.isSelected()) specializations.add("Nutrition");
        if (crossfitCheck.isSelected()) specializations.add("CrossFit");
        if (weightLossCheck.isSelected()) specializations.add("Weight Loss");
        if (muscleGainCheck.isSelected()) specializations.add("Muscle Gain");

        return specializations;
    }

    private void closeWindow() {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}