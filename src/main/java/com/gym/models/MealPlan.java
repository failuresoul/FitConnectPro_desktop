package com.gym.models;

import java.time.LocalDate;
import java. time.LocalTime;

public class MealPlan {
    private int mealPlanId;
    private int trainerId;
    private int memberId;
    private LocalDate planDate;
    private String mealType; // BREAKFAST, LUNCH, DINNER, SNACK
    private LocalTime mealTime;
    private String foods; // Comma-separated list
    private int totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFats;
    private String preparationInstructions;
    private LocalDate createdDate;

    public MealPlan() {
    }

    // Getters and Setters
    public int getMealPlanId() {
        return mealPlanId;
    }

    public void setMealPlanId(int mealPlanId) {
        this.mealPlanId = mealPlanId;
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

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public LocalTime getMealTime() {
        return mealTime;
    }

    public void setMealTime(LocalTime mealTime) {
        this.mealTime = mealTime;
    }

    public String getFoods() {
        return foods;
    }

    public void setFoods(String foods) {
        this.foods = foods;
    }

    public int getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(int totalCalories) {
        this.totalCalories = totalCalories;
    }

    public double getTotalProtein() {
        return totalProtein;
    }

    public void setTotalProtein(double totalProtein) {
        this.totalProtein = totalProtein;
    }

    public double getTotalCarbs() {
        return totalCarbs;
    }

    public void setTotalCarbs(double totalCarbs) {
        this.totalCarbs = totalCarbs;
    }

    public double getTotalFats() {
        return totalFats;
    }

    public void setTotalFats(double totalFats) {
        this.totalFats = totalFats;
    }

    public String getPreparationInstructions() {
        return preparationInstructions;
    }

    public void setPreparationInstructions(String preparationInstructions) {
        this.preparationInstructions = preparationInstructions;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }
}