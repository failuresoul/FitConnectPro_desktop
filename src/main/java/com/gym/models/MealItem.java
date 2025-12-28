package com.gym.models;

public class MealItem {
    private int mealItemId;
    private int mealId;
    private int foodId;
    private double quantity;
    private String unit;
    private int calculatedCalories;

    public int getMealItemId() {
        return mealItemId;
    }

    public void setMealItemId(int mealItemId) {
        this.mealItemId = mealItemId;
    }

    public int getMealId() {
        return mealId;
    }

    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getCalculatedCalories() {
        return calculatedCalories;
    }

    public void setCalculatedCalories(int calculatedCalories) {
        this.calculatedCalories = calculatedCalories;
    }
}

