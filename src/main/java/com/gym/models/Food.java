package com.gym.models;

public class Food {
    private int foodId;
    private String foodName;
    private String servingSize;
    private int caloriesPerServing;
    private double protein;
    private double carbs;
    private double fats;
    private String category;
    private boolean isGymRecommended;

    public Food() {
    }

    public Food(int foodId, String foodName, String servingSize, int caloriesPerServing,
                double protein, double carbs, double fats, String category) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.servingSize = servingSize;
        this.caloriesPerServing = caloriesPerServing;
        this.protein = protein;
        this.carbs = carbs;
        this.fats = fats;
        this.category = category;
    }

    // Getters and Setters
    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getServingSize() {
        return servingSize;
    }

    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
    }

    public int getCaloriesPerServing() {
        return caloriesPerServing;
    }

    public void setCaloriesPerServing(int caloriesPerServing) {
        this.caloriesPerServing = caloriesPerServing;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getFats() {
        return fats;
    }

    public void setFats(double fats) {
        this.fats = fats;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isGymRecommended() {
        return isGymRecommended;
    }

    public void setGymRecommended(boolean gymRecommended) {
        isGymRecommended = gymRecommended;
    }

    @Override
    public String toString() {
        return foodName + " (" + servingSize + ") - " + caloriesPerServing + " cal";
    }
}