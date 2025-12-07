package com.gym.controllers. admin;

import com.gym. dao.MemberDAO;
import com.gym.models.Admin;
import com.gym.models.Member;
import com.gym.services.Session;
import com.gym.utils.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene. control.*;

import java.security.SecureRandom;
import java. time.LocalDate;
import java. time.LocalDateTime;
import java.util.Random;

public class MemberRegistrationController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private DatePicker dobPicker;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private TextField heightField;

    @FXML
    private TextField weightField;

    @FXML
    private TextArea medicalNotesArea;

    @FXML
    private TextField emergencyContactField;

    @FXML
    private ComboBox<String> membershipTypeComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private Spinner<Integer> durationSpinner;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button registerButton;

    @FXML
    private Button clearButton;

    private MemberDAO memberDAO;
    private String generatedPassword;

    public MemberRegistrationController() {
        memberDAO = new MemberDAO();
    }

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupSpinner();
        setupEventHandlers();
        setupAutoGeneration();
    }

    private void setupComboBoxes() {
        // Gender options
        genderComboBox. getItems().addAll("Male", "Female", "Other");

        // Membership types with prices
        membershipTypeComboBox.getItems().addAll(
                "BASIC - $30/month",
                "PREMIUM - $50/month",
                "ELITE - $80/month"
        );
    }

    private void setupSpinner() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 24, 12);
        durationSpinner. setValueFactory(valueFactory);
    }

    private void setupEventHandlers() {
        registerButton.setOnAction(event -> handleRegisterMember());
        clearButton.setOnAction(event -> clearForm());
    }

    private void setupAutoGeneration() {
        // Auto-generate username when full name changes
        fullNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim(). isEmpty()) {
                usernameField.setText(generateUsername(newVal));
            } else {
                usernameField.clear();
            }
        });
    }

    private void handleRegisterMember() {
        try {
            // Validate inputs
            if (!validateInputs()) {
                return;
            }

            // Generate password
            generatedPassword = generatePassword();
            passwordField.setText(generatedPassword);

            // Create member object
            Member member = createMemberFromInputs();

            // Register member
            boolean success = memberDAO.registerMember(member);

            if (success) {
                showCredentialsDialog(member);
                clearForm();
            } else {
                ValidationUtil.showAlert("Registration Failed",
                        "Could not register member. Username or email may already exist.",
                        Alert.AlertType. ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
            ValidationUtil. showAlert("Error",
                    "An error occurred during registration: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    private boolean validateInputs() {
        if (! ValidationUtil.isNotEmpty(fullNameField.getText())) {
            ValidationUtil.showAlert("Validation Error", "Full name is required!", Alert.AlertType.ERROR);
            return false;
        }

        if (!ValidationUtil. isValidEmail(emailField.getText())) {
            ValidationUtil. showAlert("Validation Error", "Valid email is required!", Alert.AlertType.ERROR);
            return false;
        }

        if (!ValidationUtil.isNotEmpty(phoneField.getText())) {
            ValidationUtil.showAlert("Validation Error", "Phone number is required!", Alert.AlertType. ERROR);
            return false;
        }

        if (dobPicker.getValue() == null) {
            ValidationUtil.showAlert("Validation Error", "Date of birth is required!", Alert.AlertType.ERROR);
            return false;
        }

        if (genderComboBox.getValue() == null) {
            ValidationUtil. showAlert("Validation Error", "Gender is required!", Alert.AlertType.ERROR);
            return false;
        }

        if (membershipTypeComboBox.getValue() == null) {
            ValidationUtil.showAlert("Validation Error", "Membership type is required!", Alert.AlertType.ERROR);
            return false;
        }

        if (startDatePicker.getValue() == null) {
            ValidationUtil.showAlert("Validation Error", "Start date is required!", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private Member createMemberFromInputs() {
        Member member = new Member();

        member.setUsername(usernameField.getText());
        member.setPasswordHash(generatedPassword); // Will be hashed in DAO
        member.setFullName(fullNameField.getText());
        member.setEmail(emailField. getText());
        member.setPhone(phoneField.getText());
        member.setDateOfBirth(dobPicker.getValue());
        member.setGender(genderComboBox.getValue());

        // Extract membership type (remove price info)
        String membershipType = membershipTypeComboBox.getValue(). split(" - ")[0];
        member.setMembershipType(membershipType);

        member.setMembershipStart(startDatePicker.getValue());

        // Calculate end date
        LocalDate endDate = startDatePicker.getValue(). plusMonths(durationSpinner.getValue());
        member.setMembershipEnd(endDate);

        member.setAccountStatus("ACTIVE");

        // Get current admin ID
        Admin admin = (Admin) Session.getInstance().getCurrentUser();
        if (admin != null) {
            member.setCreatedByAdminId(admin.getAdminId());
        }

        member.setCreatedDate(LocalDateTime.now());

        return member;
    }

    private String generateUsername(String fullName) {
        String[] parts = fullName.trim().toLowerCase().split("\\s+");
        if (parts.length >= 2) {
            return parts[0] + "." + parts[parts.length - 1];
        } else if (parts.length == 1) {
            return parts[0];
        }
        return "";
    }

    private String generatePassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    private void showCredentialsDialog(Member member) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Member Registered Successfully");
        alert.setHeaderText("Member has been registered!");
        alert.setContentText(
                "Member Details:\n\n" +
                        "Name: " + member.getFullName() + "\n" +
                        "Email: " + member.getEmail() + "\n" +
                        "Username: " + member.getUsername() + "\n" +
                        "Password: " + generatedPassword + "\n" +
                        "Membership: " + member.getMembershipType() + "\n" +
                        "Valid Until: " + member.getMembershipEnd() + "\n\n" +
                        "Please save these credentials!\n" +
                        "(Mock) Credentials have been sent to the member's email."
        );

        alert.showAndWait();
    }

    private void clearForm() {
        fullNameField.clear();
        emailField.clear();
        phoneField.clear();
        dobPicker.setValue(null);
        genderComboBox.setValue(null);
        heightField.clear();
        weightField.clear();
        medicalNotesArea.clear();
        emergencyContactField.clear();
        membershipTypeComboBox.setValue(null);
        startDatePicker.setValue(null);
        durationSpinner.getValueFactory().setValue(12);
        usernameField.clear();
        passwordField.clear();
    }
}