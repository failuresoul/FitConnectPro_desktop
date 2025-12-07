package com.gym.models;

import java.time.LocalDate;
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
    private LocalDate hireDate;
    private double monthlySalary;
    private String accountStatus;
    private int maxClients;
    private int addedByAdminId;
    private LocalDateTime createdDate;

    public Trainer() {
    }

    public Trainer(int trainerId, String username, String passwordHash, String fullName, String email,
                   String phone, List<String> specializations, int experienceYears, String certifications,
                   LocalDate hireDate, double monthlySalary, String accountStatus, int maxClients,
                   int addedByAdminId, LocalDateTime createdDate) {
        this.trainerId = trainerId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.specializations = specializations;
        this.experienceYears = experienceYears;
        this.certifications = certifications;
        this.hireDate = hireDate;
        this.monthlySalary = monthlySalary;
        this.accountStatus = accountStatus;
        this.maxClients = maxClients;
        this.addedByAdminId = addedByAdminId;
        this.createdDate = createdDate;
    }

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

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public double getMonthlySalary() {
        return monthlySalary;
    }

    public void setMonthlySalary(double monthlySalary) {
        this.monthlySalary = monthlySalary;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public int getMaxClients() {
        return maxClients;
    }

    public void setMaxClients(int maxClients) {
        this.maxClients = maxClients;
    }

    public int getAddedByAdminId() {
        return addedByAdminId;
    }

    public void setAddedByAdminId(int addedByAdminId) {
        this.addedByAdminId = addedByAdminId;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
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
                ", hireDate=" + hireDate +
                ", monthlySalary=" + monthlySalary +
                ", accountStatus='" + accountStatus + '\'' +
                ", maxClients=" + maxClients +
                ", addedByAdminId=" + addedByAdminId +
                ", createdDate=" + createdDate +
                '}';
    }
}