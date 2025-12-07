package com. gym.models;

import java. time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Workout {
    private int workoutId;
    private int memberId;
    private LocalDate workoutDate;
    private LocalTime workoutTime;
    private String workoutType;
    private int totalDuration;
    private int totalCalories;
    private int difficultyRating;
    private String notes;
    private LocalDateTime createdAt;

    public Workout() {
    }

    public Workout(int workoutId, int memberId, LocalDate workoutDate, LocalTime workoutTime,
                   String workoutType, int totalDuration, int totalCalories, int difficultyRating,
                   String notes, LocalDateTime createdAt) {
        this. workoutId = workoutId;
        this.memberId = memberId;
        this.workoutDate = workoutDate;
        this.workoutTime = workoutTime;
        this.workoutType = workoutType;
        this.totalDuration = totalDuration;
        this.totalCalories = totalCalories;
        this.difficultyRating = difficultyRating;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public int getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(int workoutId) {
        this.workoutId = workoutId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public LocalDate getWorkoutDate() {
        return workoutDate;
    }

    public void setWorkoutDate(LocalDate workoutDate) {
        this.workoutDate = workoutDate;
    }

    public LocalTime getWorkoutTime() {
        return workoutTime;
    }

    public void setWorkoutTime(LocalTime workoutTime) {
        this.workoutTime = workoutTime;
    }

    public String getWorkoutType() {
        return workoutType;
    }

    public void setWorkoutType(String workoutType) {
        this.workoutType = workoutType;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }

    public int getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(int totalCalories) {
        this.totalCalories = totalCalories;
    }

    public int getDifficultyRating() {
        return difficultyRating;
    }

    public void setDifficultyRating(int difficultyRating) {
        this.difficultyRating = difficultyRating;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Workout{" +
                "workoutId=" + workoutId +
                ", memberId=" + memberId +
                ", workoutDate=" + workoutDate +
                ", workoutTime=" + workoutTime +
                ", workoutType='" + workoutType + '\'' +
                ", totalDuration=" + totalDuration +
                ", totalCalories=" + totalCalories +
                ", difficultyRating=" + difficultyRating +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}