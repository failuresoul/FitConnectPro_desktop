package com.gym. models;

import java.time. LocalDate;
import java.time.LocalTime;

public class Meal {
    private int mealId;
    private int memberId;
    private String mealType;
    private LocalDate mealDate;
    private LocalTime mealTime;
    private int totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFats;
    private String notes;

    public Meal() {
    }

    public Meal(int mealId, int memberId, String mealType, LocalDate mealDate, LocalTime mealTime,
                int totalCalories, double totalProtein, double totalCarbs, double totalFats, String notes) {
        this.mealId = mealId;
        this.memberId = memberId;
        this.mealType = mealType;
        this.mealDate = mealDate;
        this.mealTime = mealTime;
        this.totalCalories = totalCalories;
        this.totalProtein = totalProtein;
        this.totalCarbs = totalCarbs;
        this.totalFats = totalFats;
        this.notes = notes;
    }

    public int getMealId() {
        return mealId;
    }

    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
    }

    public LocalTime getMealTime() {
        return mealTime;
    }

    public void setMealTime(LocalTime mealTime) {
        this.mealTime = mealTime;
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
        this. totalProtein = totalProtein;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Meal{" +
                "mealId=" + mealId +
                ", memberId=" + memberId +
                ", mealType='" + mealType + '\'' +
                ", mealDate=" + mealDate +
                ", mealTime=" + mealTime +
                ", totalCalories=" + totalCalories +
                ", totalProtein=" + totalProtein +
                ", totalCarbs=" + totalCarbs +
                ", totalFats=" + totalFats +
                ", notes='" + notes + '\'' +
                '}';
    }
}