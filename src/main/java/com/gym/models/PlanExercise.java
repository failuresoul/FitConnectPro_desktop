package com.gym.models;

public class PlanExercise {
    private int planExerciseId;
    private int planId;
    private int exerciseId;
    private String exerciseName; // For display
    private int sets;
    private String reps; // Can be "10-12" or "15"
    private double weight;
    private int restSeconds;
    private String trainerNotes;
    private int orderNumber;

    public PlanExercise() {
    }
    private boolean completed; // Add this field

    // Existing constructors and getters/setters...

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    public PlanExercise(int exerciseId, String exerciseName, int sets, String reps,
                        double weight, int restSeconds, String trainerNotes) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
        this.restSeconds = restSeconds;
        this.trainerNotes = trainerNotes;
    }

    // Getters and Setters
    public int getPlanExerciseId() {
        return planExerciseId;
    }

    public void setPlanExerciseId(int planExerciseId) {
        this.planExerciseId = planExerciseId;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public String getReps() {
        return reps;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getRestSeconds() {
        return restSeconds;
    }

    public void setRestSeconds(int restSeconds) {
        this.restSeconds = restSeconds;
    }

    public String getTrainerNotes() {
        return trainerNotes;
    }

    public void setTrainerNotes(String trainerNotes) {
        this.trainerNotes = trainerNotes;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }
}