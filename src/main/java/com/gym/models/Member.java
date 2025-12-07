package com. gym.models;

import java. time.LocalDate;
import java.time.LocalDateTime;

public class Member {
    private int memberId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String profilePhoto;
    private String membershipType;
    private LocalDate membershipStart;
    private LocalDate membershipEnd;
    private String accountStatus;
    private int createdByAdminId;
    private LocalDateTime createdDate;
    private LocalDateTime lastLogin;

    public Member() {
    }

    public Member(int memberId, String username, String passwordHash, String fullName, String email,
                  String phone, LocalDate dateOfBirth, String gender, String profilePhoto,
                  String membershipType, LocalDate membershipStart, LocalDate membershipEnd,
                  String accountStatus, int createdByAdminId, LocalDateTime createdDate,
                  LocalDateTime lastLogin) {
        this.memberId = memberId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this. email = email;
        this. phone = phone;
        this. dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.profilePhoto = profilePhoto;
        this. membershipType = membershipType;
        this.membershipStart = membershipStart;
        this. membershipEnd = membershipEnd;
        this.accountStatus = accountStatus;
        this.createdByAdminId = createdByAdminId;
        this. createdDate = createdDate;
        this.lastLogin = lastLogin;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }

    public LocalDate getMembershipStart() {
        return membershipStart;
    }

    public void setMembershipStart(LocalDate membershipStart) {
        this.membershipStart = membershipStart;
    }

    public LocalDate getMembershipEnd() {
        return membershipEnd;
    }

    public void setMembershipEnd(LocalDate membershipEnd) {
        this.membershipEnd = membershipEnd;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public int getCreatedByAdminId() {
        return createdByAdminId;
    }

    public void setCreatedByAdminId(int createdByAdminId) {
        this.createdByAdminId = createdByAdminId;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this. createdDate = createdDate;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this. lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "Member{" +
                "memberId=" + memberId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender='" + gender + '\'' +
                ", profilePhoto='" + profilePhoto + '\'' +
                ", membershipType='" + membershipType + '\'' +
                ", membershipStart=" + membershipStart +
                ", membershipEnd=" + membershipEnd +
                ", accountStatus='" + accountStatus + '\'' +
                ", createdByAdminId=" + createdByAdminId +
                ", createdDate=" + createdDate +
                ", lastLogin=" + lastLogin +
                '}';
    }
}