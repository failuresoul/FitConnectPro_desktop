package com.gym.models;

import java.time.LocalDateTime;
import java.util.List;

public class Trainer {
    private int trainerId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phone;
    private List<String> specializations;
    private int experienceYears;
    private String certifications;
    private int maxClients;
    private int currentClients;
    private String accountStatus;
    private double salary;
    private int hiredByAdminId;
    private LocalDateTime hireDate;
    private LocalDateTime lastLogin;

    // Constructor
    public Trainer() {
    }

    // Getters and Setters
    public int getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(int trainerId) {
        this.trainerId = trainerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getSpecializations() {
        return specializations;
    }

    public void setSpecializations(List<String> specializations) {
        this.specializations = specializations;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getCertifications() {
        return certifications;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    public int getMaxClients() {
        return maxClients;
    }

    public void setMaxClients(int maxClients) {
        this.maxClients = maxClients;
    }

    public int getCurrentClients() {
        return currentClients;
    }

    public void setCurrentClients(int currentClients) {
        this.currentClients = currentClients;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public int getHiredByAdminId() {
        return hiredByAdminId;
    }

    public void setHiredByAdminId(int hiredByAdminId) {
        this.hiredByAdminId = hiredByAdminId;
    }

    public LocalDateTime getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDateTime hireDate) {
        this.hireDate = hireDate;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "Trainer{" +
                "trainerId=" + trainerId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", specializations=" + specializations +
                ", experienceYears=" + experienceYears +
                ", certifications='" + certifications + '\'' +
                ", maxClients=" + maxClients +
                ", currentClients=" + currentClients +
                ", accountStatus='" + accountStatus + '\'' +
                ", salary=" + salary +
                ", hiredByAdminId=" + hiredByAdminId +
                ", hireDate=" + hireDate +
                '}';
    }
}