package com.gym.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkoutPlan {
    private int planId;
    private int trainerId;
    private int memberId;
    private LocalDate planDate;
    private String focusArea;
    private int totalDuration;
    private int expectedCalories;
    private String instructions;
    private LocalDateTime createdDate;
    private String status;

    public WorkoutPlan() {
    }

    public WorkoutPlan(int planId, int trainerId, int memberId, LocalDate planDate, String focusArea,
                       int totalDuration, int expectedCalories, String instructions,
                       LocalDateTime createdDate, String status) {
        this.planId = planId;
        this.trainerId = trainerId;
        this.memberId = memberId;
        this.planDate = planDate;
        this.focusArea = focusArea;
        this.totalDuration = totalDuration;
        this.expectedCalories = expectedCalories;
        this.instructions = instructions;
        this.createdDate = createdDate;
        this.status = status;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
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

    public LocalDate getPlanDate() {
        return planDate;
    }

    public void setPlanDate(LocalDate planDate) {
        this.planDate = planDate;
    }

    public String getFocusArea() {
        return focusArea;
    }

    public void setFocusArea(String focusArea) {
        this.focusArea = focusArea;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(int totalDuration) {
        this. totalDuration = totalDuration;
    }

    public int getExpectedCalories() {
        return expectedCalories;
    }

    public void setExpectedCalories(int expectedCalories) {
        this.expectedCalories = expectedCalories;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "WorkoutPlan{" +
                "planId=" + planId +
                ", trainerId=" + trainerId +
                ", memberId=" + memberId +
                ", planDate=" + planDate +
                ", focusArea='" + focusArea + '\'' +
                ", totalDuration=" + totalDuration +
                ", expectedCalories=" + expectedCalories +
                ", instructions='" + instructions + '\'' +
                ", createdDate=" + createdDate +
                ", status='" + status + '\'' +
                '}';
    }
}