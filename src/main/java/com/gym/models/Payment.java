package com.gym.models;

import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private int userId;
    private String userType; // MEMBER or TRAINER
    private String paymentType; // MEMBERSHIP or SALARY
    private double amount;
    private String status; // PENDING, PAID, OVERDUE
    private LocalDateTime dueDate;
    private LocalDateTime paidDate;
    private String paymentMethod;
    private String description;

    // Constructors
    public Payment() {}

    public Payment(int userId, String userType, String paymentType, double amount,
                   String status, LocalDateTime dueDate) {
        this.userId = userId;
        this.userType = userType;
        this.paymentType = paymentType;
        this.amount = amount;
        this.status = status;
        this.dueDate = dueDate;
    }

    // Getters and Setters
    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDateTime paidDate) {
        this.paidDate = paidDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

