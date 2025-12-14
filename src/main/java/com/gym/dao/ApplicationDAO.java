package com.gym.dao;

import com.gym.models.TrainerApplication;
import com.gym.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java. util.ArrayList;
import java. util.Arrays;
import java.util.List;

public class ApplicationDAO {

    // Get all pending applications
    public List<TrainerApplication> getPendingApplications() {
        List<TrainerApplication> applications = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection. getInstance().getConnection();
            String sql = "SELECT * FROM Trainer_Applications WHERE status = 'PENDING' ORDER BY application_date DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                applications.add(extractApplicationFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting pending applications: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return applications;
    }

    // Get application by ID
    public TrainerApplication getApplicationById(int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        TrainerApplication application = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM Trainer_Applications WHERE application_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt. setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs. next()) {
                application = extractApplicationFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting application by ID: " + e. getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return application;
    }

    // Approve application
    public boolean approveApplication(int applicationId, int reviewedByAdminId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "UPDATE Trainer_Applications SET status = 'APPROVED', " +
                    "reviewed_by_admin_id = ?, review_date = ?  WHERE application_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, reviewedByAdminId);
            pstmt.setString(2, LocalDateTime.now().toString());
            pstmt. setInt(3, applicationId);

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

            if (success) {
                System.out.println("✅ Application approved:  " + applicationId);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error approving application: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, conn);
        }

        return success;
    }

    // Reject application
    public boolean rejectApplication(int applicationId, int reviewedByAdminId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "UPDATE Trainer_Applications SET status = 'REJECTED', " +
                    "reviewed_by_admin_id = ?, review_date = ? WHERE application_id = ?";

            pstmt = conn. prepareStatement(sql);
            pstmt.setInt(1, reviewedByAdminId);
            pstmt.setString(2, LocalDateTime.now().toString());
            pstmt. setInt(3, applicationId);

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

            if (success) {
                System. out.println("✅ Application rejected: " + applicationId);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error rejecting application: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, conn);
        }

        return success;
    }

    // Submit new application
    public boolean submitApplication(TrainerApplication application) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String specializationsStr = String.join(",", application. getSpecializations());

            String sql = "INSERT INTO Trainer_Applications (full_name, email, phone, age, education, " +
                    "certifications, experience_years, specializations, cover_letter, " +
                    "application_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, application.getFullName());
            pstmt.setString(2, application.getEmail());
            pstmt.setString(3, application.getPhone());
            pstmt.setInt(4, application.getAge());
            pstmt.setString(5, application.getEducation());
            pstmt.setString(6, application.getCertifications());
            pstmt.setInt(7, application.getExperienceYears());
            pstmt.setString(8, specializationsStr);
            pstmt.setString(9, application. getCoverLetter());
            pstmt.setString(10, LocalDateTime.now().toString());

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

            if (success) {
                System.out. println("✅ Application submitted successfully!");
            }

        } catch (SQLException e) {
            System. err.println("❌ Error submitting application: " + e. getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, pstmt, conn);
        }

        return success;
    }

    // Extract application from ResultSet
    private TrainerApplication extractApplicationFromResultSet(ResultSet rs) throws SQLException {
        TrainerApplication app = new TrainerApplication();

        app.setApplicationId(rs.getInt("application_id"));
        app.setFullName(rs.getString("full_name"));
        app.setEmail(rs.getString("email"));
        app.setPhone(rs.getString("phone"));
        app.setAge(rs.getInt("age"));
        app.setEducation(rs.getString("education"));
        app.setCertifications(rs. getString("certifications"));
        app.setExperienceYears(rs.getInt("experience_years"));

        String specializationsStr = rs.getString("specializations");
        if (specializationsStr != null && !specializationsStr.isEmpty()) {
            app.setSpecializations(Arrays. asList(specializationsStr.split(",")));
        } else {
            app.setSpecializations(new ArrayList<>());
        }

        app.setCoverLetter(rs.getString("cover_letter"));
        app.setStatus(rs.getString("status"));

        String appDateStr = rs.getString("application_date");
        if (appDateStr != null) {
            app.setApplicationDate(LocalDateTime. parse(appDateStr));
        }

        try {
            int reviewedBy = rs.getInt("reviewed_by_admin_id");
            app.setReviewedByAdminId(reviewedBy);
        } catch (SQLException e) {
            app.setReviewedByAdminId(null);
        }

        String reviewDateStr = rs.getString("review_date");
        if (reviewDateStr != null) {
            app.setReviewDate(LocalDateTime.parse(reviewDateStr));
        }

        return app;
    }

    // Close resources helper
    private void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}