package com.gym.models;

public class SalaryReport {
    private String trainerName;
    private int totalSalaries;
    private double totalAmount;
    private double averageSalary;
    private double totalBonus;
    private double totalDeductions;

    public SalaryReport() {
    }

    public SalaryReport(String trainerName, int totalSalaries, double totalAmount,
                        double averageSalary, double totalBonus, double totalDeductions) {
        this.trainerName = trainerName;
        this. totalSalaries = totalSalaries;
        this.totalAmount = totalAmount;
        this. averageSalary = averageSalary;
        this. totalBonus = totalBonus;
        this.totalDeductions = totalDeductions;
    }

    // Getters and Setters
    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public int getTotalSalaries() {
        return totalSalaries;
    }

    public void setTotalSalaries(int totalSalaries) {
        this.totalSalaries = totalSalaries;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getAverageSalary() {
        return averageSalary;
    }

    public void setAverageSalary(double averageSalary) {
        this.averageSalary = averageSalary;
    }

    public double getTotalBonus() {
        return totalBonus;
    }

    public void setTotalBonus(double totalBonus) {
        this.totalBonus = totalBonus;
    }

    public double getTotalDeductions() {
        return totalDeductions;
    }

    public void setTotalDeductions(double totalDeductions) {
        this.totalDeductions = totalDeductions;
    }
}