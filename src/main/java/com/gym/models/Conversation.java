package com.gym.models;

import java.time.LocalDateTime;

public class Conversation {
    private int userId;
    private String userName;
    private String userType; // "MEMBER" or "TRAINER"
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int unreadCount;

    public Conversation() {
    }

    public Conversation(int userId, String userName, String userType,
                       String lastMessage, LocalDateTime lastMessageTime, int unreadCount) {
        this.userId = userId;
        this.userName = userName;
        this.userType = userType;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}

