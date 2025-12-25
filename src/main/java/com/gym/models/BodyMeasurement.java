package com.gym.models;

import java.time.LocalDate;

public class BodyMeasurement {
    private int measurementId;
    private int memberId;
    private LocalDate measurementDate;
    private double weight;
    private double chest;
    private double waist;
    private double hips;
    private double arms;
    private double legs;
    private double bodyFatPercentage;

    public BodyMeasurement() {
    }

    // Getters and Setters
    public int getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(int measurementId) {
        this.measurementId = measurementId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public LocalDate getMeasurementDate() {
        return measurementDate;
    }

    public void setMeasurementDate(LocalDate measurementDate) {
        this.measurementDate = measurementDate;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getChest() {
        return chest;
    }

    public void setChest(double chest) {
        this.chest = chest;
    }

    public double getWaist() {
        return waist;
    }

    public void setWaist(double waist) {
        this.waist = waist;
    }

    public double getHips() {
        return hips;
    }

    public void setHips(double hips) {
        this.hips = hips;
    }

    public double getArms() {
        return arms;
    }

    public void setArms(double arms) {
        this.arms = arms;
    }

    public double getLegs() {
        return legs;
    }

    public void setLegs(double legs) {
        this.legs = legs;
    }

    public double getBodyFatPercentage() {
        return bodyFatPercentage;
    }

    public void setBodyFatPercentage(double bodyFatPercentage) {
        this.bodyFatPercentage = bodyFatPercentage;
    }
}