package com.gym. models;

import java.time. LocalDate;

public class Goal {
    private int goalId;
    private int memberId;
    private String goalType;
    private String goalCategory;
    private String description;
    private double targetValue;
    private double currentValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDate achievementDate;

    public Goal() {
    }

    public Goal(int goalId, int memberId, String goalType, String goalCategory, String description,
                double targetValue, double currentValue, LocalDate startDate, LocalDate endDate,
                String status, LocalDate achievementDate) {
        this.goalId = goalId;
        this.memberId = memberId;
        this.goalType = goalType;
        this.goalCategory = goalCategory;
        this.description = description;
        this.targetValue = targetValue;
        this.currentValue = currentValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.achievementDate = achievementDate;
    }

    public int getGoalId() {
        return goalId;
    }

    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public String getGoalCategory() {
        return goalCategory;
    }

    public void setGoalCategory(String goalCategory) {
        this.goalCategory = goalCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(double targetValue) {
        this.targetValue = targetValue;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getAchievementDate() {
        return achievementDate;
    }

    public void setAchievementDate(LocalDate achievementDate) {
        this.achievementDate = achievementDate;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "goalId=" + goalId +
                ", memberId=" + memberId +
                ", goalType='" + goalType + '\'' +
                ", goalCategory='" + goalCategory + '\'' +
                ", description='" + description + '\'' +
                ", targetValue=" + targetValue +
                ", currentValue=" + currentValue +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                ", achievementDate=" + achievementDate +
                '}';
    }
}