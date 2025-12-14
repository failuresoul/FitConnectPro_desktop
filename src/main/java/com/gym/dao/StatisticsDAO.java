package com.gym.dao;

import com.gym.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql. SQLException;
import java.time.LocalDate;

public class StatisticsDAO {

    public int getTotalMembers() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT COUNT(*) as total FROM Members WHERE account_status = 'ACTIVE'";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("total");
            }
        } catch (SQLException e) {
            System. err.println("Error getting total members: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return count;
    }

    public int getTotalTrainers() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT COUNT(*) as total FROM Trainers WHERE account_status = 'ACTIVE'";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs. next()) {
                count = rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err. println("Error getting total trainers: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return count;
    }

    public int getPendingApplicationsCount() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT COUNT(*) as total FROM Trainer_Applications WHERE status = 'PENDING'";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("total");
            }
        } catch (SQLException e) {
            System. err.println("Error getting pending applications: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs. close();
                if (pstmt != null) pstmt. close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return count;
    }

    public double getMonthlyRevenue() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        double revenue = 0.0;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Get current date
            LocalDate now = LocalDate.now();

            // Calculate revenue from ALL active memberships where:
            // membership_start <= today AND membership_end >= today
            // This means they are currently paying members
            String sql = "SELECT " +
                    "SUM(CASE " +
                    "    WHEN membership_type = 'BASIC' THEN 500 " +
                    "    WHEN membership_type = 'STANDARD' THEN 800 " +
                    "    WHEN membership_type = 'PREMIUM' THEN 1500 " +
                    "    WHEN membership_type = 'ELITE' THEN 3000 " +
                    "    ELSE 0 " +
                    "END) as total_revenue " +
                    "FROM Members " +
                    "WHERE account_status = 'ACTIVE' " +
                    "AND date(membership_start) <= date(?) " +
                    "AND date(membership_end) >= date(?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, now.toString());
            pstmt.setString(2, now.toString());
            rs = pstmt.executeQuery();

            if (rs. next()) {
                revenue = rs.getDouble("total_revenue");
            }

            System.out.println("Revenue Calculation Debug:");
            System.out.println("Current Date: " + now);
            System.out.println("Calculated Revenue: $" + revenue);

        } catch (SQLException e) {
            System.err.println("Error getting monthly revenue: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return revenue;
    }
}