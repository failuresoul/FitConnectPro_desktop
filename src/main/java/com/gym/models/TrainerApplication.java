package com.gym.models;

import java.time.LocalDateTime;

public class TrainerApplication {
    private int applicationId;
    private String fullName;
    private String email;
    private String phone;
    private int age;
    private String education;
    private String certifications;
    private int experienceYears;
    private String specializations;
    private String coverLetter;
    private LocalDateTime applicationDate;
    private String status;
    private int reviewedByAdminId;
    private LocalDateTime reviewDate;

    public TrainerApplication() {
    }

    public TrainerApplication(int applicationId, String fullName, String email, String phone, int age,
                              String education, String certifications, int experienceYears,
                              String specializations, String coverLetter, LocalDateTime applicationDate,
                              String status, int reviewedByAdminId, LocalDateTime reviewDate) {
        this. applicationId = applicationId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.age = age;
        this.education = education;
        this.certifications = certifications;
        this.experienceYears = experienceYears;
        this.specializations = specializations;
        this.coverLetter = coverLetter;
        this.applicationDate = applicationDate;
        this.status = status;
        this.reviewedByAdminId = reviewedByAdminId;
        this.reviewDate = reviewDate;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getCertifications() {
        return certifications;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getSpecializations() {
        return specializations;
    }

    public void setSpecializations(String specializations) {
        this.specializations = specializations;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getReviewedByAdminId() {
        return reviewedByAdminId;
    }

    public void setReviewedByAdminId(int reviewedByAdminId) {
        this. reviewedByAdminId = reviewedByAdminId;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this. reviewDate = reviewDate;
    }

    @Override
    public String toString() {
        return "TrainerApplication{" +
                "applicationId=" + applicationId +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", age=" + age +
                ", education='" + education + '\'' +
                ", certifications='" + certifications + '\'' +
                ", experienceYears=" + experienceYears +
                ", specializations='" + specializations + '\'' +
                ", applicationDate=" + applicationDate +
                ", status='" + status + '\'' +
                ", reviewedByAdminId=" + reviewedByAdminId +
                ", reviewDate=" + reviewDate +
                '}';
    }
}