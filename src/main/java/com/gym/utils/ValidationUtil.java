package com.gym.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_REGEX = "^[0-9]{10,15}$";
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$";

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Pattern.compile(EMAIL_REGEX). matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone. trim().isEmpty()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        return Pattern.compile(PHONE_REGEX).matcher(cleanPhone).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return Pattern.compile(PASSWORD_REGEX).matcher(password).matches();
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim(). isEmpty();
    }

    public static void showAlert(String title, String message, AlertType type) {
        try {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert. setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err. println("Error showing alert: " + e.getMessage());
        }
    }
}