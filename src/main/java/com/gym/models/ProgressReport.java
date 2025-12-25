package com.gym.models;

import java.time.LocalDate;

public class ProgressReport {
    private int reportId;
    private int trainerId;
    private int memberId;
    private LocalDate reportDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private double workoutCompletionRate;
    private int mealsLoggedCount;
    private double waterIntakeCompliance;
    private double weightChange;
    private String goalAchievementSummary;
    private String trainerFeedback;
    private LocalDate createdDate;

    public ProgressReport() {
    }

    // Getters and Setters
    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public int getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(int trainerId) {
        this.trainerId = trainerId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getWorkoutCompletionRate() {
        return workoutCompletionRate;
    }

    public void setWorkoutCompletionRate(double workoutCompletionRate) {
        this.workoutCompletionRate = workoutCompletionRate;
    }

    public int getMealsLoggedCount() {
        return mealsLoggedCount;
    }

    public void setMealsLoggedCount(int mealsLoggedCount) {
        this.mealsLoggedCount = mealsLoggedCount;
    }

    public double getWaterIntakeCompliance() {
        return waterIntakeCompliance;
    }

    public void setWaterIntakeCompliance(double waterIntakeCompliance) {
        this.waterIntakeCompliance = waterIntakeCompliance;
    }

    public double getWeightChange() {
        return weightChange;
    }

    public void setWeightChange(double weightChange) {
        this.weightChange = weightChange;
    }

    public String getGoalAchievementSummary() {
        return goalAchievementSummary;
    }

    public void setGoalAchievementSummary(String goalAchievementSummary) {
        this.goalAchievementSummary = goalAchievementSummary;
    }

    public String getTrainerFeedback() {
        return trainerFeedback;
    }

    public void setTrainerFeedback(String trainerFeedback) {
        this.trainerFeedback = trainerFeedback;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
}