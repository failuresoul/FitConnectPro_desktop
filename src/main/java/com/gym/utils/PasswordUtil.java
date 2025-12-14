package com.gym.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    /**
     * Hash a password using BCrypt
     * @param plainPassword - the plain text password
     * @return hashed password
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt. gensalt());
    }

    /**
     * Check if a plain password matches a hashed password
     * @param plainPassword - the plain text password to check
     * @param hashedPassword - the hashed password to compare against
     * @return true if passwords match, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            System.err.println("Error checking password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verify if a password meets security requirements
     * @param password - the password to validate
     * @return true if password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        // Add more validation rules if needed
        return true;
    }
}