package com. gym.services;

public class Session {
    private static Session instance;
    private Object currentUser;
    private String userType;

    private Session() {
    }

    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    // ADDED: Single parameter method (auto-detects user type)
    public void setCurrentUser(Object user) {
        this.currentUser = user;

        // Auto-detect user type from class name
        if (user != null) {
            String className = user.getClass().getSimpleName();
            if (className.equals("Trainer")) {
                this.userType = "TRAINER";
            } else if (className. equals("Member")) {
                this.userType = "MEMBER";
            } else if (className. equals("Admin")) {
                this.userType = "ADMIN";
            } else {
                this.userType = "UNKNOWN";
            }
            System.out.println("✅ Session set for " + className + " as " + this.userType);
        } else {
            this.userType = null;
        }
    }

    // EXISTING: Two parameter method (explicit user type)
    public void setCurrentUser(Object user, String type) {
        this.currentUser = user;
        this.userType = type;
        System. out.println("✅ Session set for user type: " + type);
    }

    public Object getCurrentUser() {
        return currentUser;
    }

    public String getUserType() {
        return userType;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        this.currentUser = null;
        this.userType = null;
        System.out.println("✅ Session cleared successfully!");
    }
}