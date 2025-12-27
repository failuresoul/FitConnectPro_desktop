package com.gym.services;

public class Session {
    private static Session instance;
    private Object currentUser;
    private String userType;
    private Integer userId;
    private String username;
    private String email;

    private Session() {
    }

    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    // Set user with auto-detection
    public void setCurrentUser(Object user) {
        this.currentUser = user;

        if (user != null) {
            String className = user.getClass().getSimpleName();
            if (className.equals("Trainer")) {
                this.userType = "TRAINER";
            } else if (className.equals("Member")) {
                this.userType = "MEMBER";
            } else if (className.equals("Admin")) {
                this.userType = "ADMIN";
            } else {
                this.userType = "UNKNOWN";
            }
            System.out.println("✅ Session set for " + className + " as " + this.userType);
        } else {
            this.userType = null;
        }
    }

    // Set user with explicit type
    public void setCurrentUser(Object user, String type) {
        this.currentUser = user;
        this.userType = type;
        System.out.println("✅ Session set for user type: " + type);
    }

    // NEW: Set session with ID, username, and email
    public void setSession(Integer userId, String username, String userType, String email) {
        this.userId = userId;
        this.username = username;
        this.userType = userType;
        this.email = email;
        System.out.println("✅ Complete session set for user: " + username + " (ID: " + userId + ")");
    }

    public Object getCurrentUser() {
        return currentUser;
    }

    public String getUserType() {
        return userType;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean isLoggedIn() {
        return currentUser != null || userId != null;
    }

    public boolean isMember() {
        return "MEMBER".equalsIgnoreCase(userType) || "member".equalsIgnoreCase(userType);
    }

    public boolean isTrainer() {
        return "TRAINER".equalsIgnoreCase(userType) || "trainer".equalsIgnoreCase(userType);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(userType) || "admin".equalsIgnoreCase(userType);
    }

    public void clearSession() {
        this.currentUser = null;
        this.userType = null;
        this.userId = null;
        this.username = null;
        this.email = null;
        System.out.println("✅ Session cleared successfully!");
    }

    public void logout() {
        clearSession();
    }
}
