package com.gym.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MealPlan {
    private int mealPlanId;
    private int trainerId;
    private int memberId;
    private LocalDate planDate;
    private String mealType;
    private LocalTime mealTime;
    private String foods;
    private int totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFats;
    private String preparationInstructions;
    private LocalDateTime createdDate;

    public MealPlan() {
    }

    public MealPlan(int mealPlanId, int trainerId, int memberId, LocalDate planDate, String mealType,
                    LocalTime mealTime, String foods, int totalCalories, double totalProtein,
                    double totalCarbs, double totalFats, String preparationInstructions,
                    LocalDateTime createdDate) {
        this.mealPlanId = mealPlanId;
        this.trainerId = trainerId;
        this.memberId = memberId;
        this.planDate = planDate;
        this.mealType = mealType;
        this.mealTime = mealTime;
        this.foods = foods;
        this.totalCalories = totalCalories;
        this.totalProtein = totalProtein;
        this.totalCarbs = totalCarbs;
        this.totalFats = totalFats;
        this.preparationInstructions = preparationInstructions;
        this.createdDate = createdDate;
    }

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
        this. preparationInstructions = preparationInstructions;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this. createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "MealPlan{" +
                "mealPlanId=" + mealPlanId +
                ", trainerId=" + trainerId +
                ", memberId=" + memberId +
                ", planDate=" + planDate +
                ", mealType='" + mealType + '\'' +
                ", mealTime=" + mealTime +
                ", foods='" + foods + '\'' +
                ", totalCalories=" + totalCalories +
                ", totalProtein=" + totalProtein +
                ", totalCarbs=" + totalCarbs +
                ", totalFats=" + totalFats +
                ", preparationInstructions='" + preparationInstructions + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}