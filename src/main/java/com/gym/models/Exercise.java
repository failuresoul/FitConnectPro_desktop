package com.gym.models;

public class Exercise {
    private int exerciseId;
    private String exerciseName;
    private String category;
    private String muscleGroup;
    private String equipmentNeeded;
    private String difficultyLevel;
    private String instructions;
    private String videoLink;

    public Exercise() {
    }

    public Exercise(int exerciseId, String exerciseName, String category, String muscleGroup) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.category = category;
        this.muscleGroup = muscleGroup;
    }

    // Getters and Setters
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public String getEquipmentNeeded() {
        return equipmentNeeded;
    }

    public void setEquipmentNeeded(String equipmentNeeded) {
        this.equipmentNeeded = equipmentNeeded;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getVideoLink() {
        return videoLink;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }

    @Override
    public String toString() {
        return exerciseName + " (" + muscleGroup + ")";
    }
}