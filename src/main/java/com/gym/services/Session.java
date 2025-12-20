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

    public void setCurrentUser(Object user, String type) {
        this.currentUser = user;
        this.userType = type;
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
        System.out.println("Session cleared successfully!");
    }
}