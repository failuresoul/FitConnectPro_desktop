package com.gym.dao;

import com.gym.models.WaterLog;
import com.gym.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaterLogDAO {

    public boolean logWater(int memberId, int amountMl, LocalDateTime time) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Get current daily total
            String getTotalSql = "SELECT COALESCE(SUM(amount_ml), 0) as daily_total " +
                    "FROM Water_Logs WHERE member_id = ? AND log_date = ?";
            pstmt = conn.prepareStatement(getTotalSql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, time.toLocalDate().toString());
            rs = pstmt.executeQuery();

            int currentTotal = 0;
            if (rs.next()) {
                currentTotal = rs.getInt("daily_total");
            }
            pstmt.close();
            rs.close();

            int newTotal = currentTotal + amountMl;

            // Insert new log
            String insertSql = "INSERT INTO Water_Logs (member_id, log_date, amount_ml, log_time, daily_total) " +
                    "VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, time.toLocalDate().toString());
            pstmt.setInt(3, amountMl);
            pstmt.setString(4, time.toLocalTime().toString());
            pstmt.setInt(5, newTotal);
            pstmt.executeUpdate();

            conn.commit();
            return true;

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    DatabaseConnection.getInstance().releaseConnection(conn);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getTodayWaterTotal(int memberId, LocalDate date) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT COALESCE(SUM(amount_ml), 0) as total " +
                    "FROM Water_Logs WHERE member_id = ? AND log_date = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, date.toString());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public List<WaterLog> getTodayLogs(int memberId, LocalDate date) {
        List<WaterLog> logs = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT water_log_id, amount_ml, log_time, daily_total " +
                    "FROM Water_Logs WHERE member_id = ? AND log_date = ? ORDER BY log_time DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, date.toString());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                WaterLog log = new WaterLog();
                log.setWaterLogId(rs.getInt("water_log_id"));
                log.setMemberId(memberId);
                log.setLogDate(date);
                log.setAmountMl(rs.getInt("amount_ml"));
                log.setLogTime(LocalTime.parse(rs.getString("log_time")));
                log.setDailyTotal(rs.getInt("daily_total"));
                logs.add(log);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return logs;
    }

    public boolean deleteWaterLog(int logId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "DELETE FROM Water_Logs WHERE water_log_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, logId);
            int affected = pstmt.executeUpdate();

            return affected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Map<LocalDate, Integer> getLast7DaysWater(int memberId) {
        Map<LocalDate, Integer> weeklyData = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT log_date, COALESCE(SUM(amount_ml), 0) as total " +
                    "FROM Water_Logs WHERE member_id = ? AND log_date >= date('now', '-6 days') " +
                    "GROUP BY log_date ORDER BY log_date";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                LocalDate date = LocalDate.parse(rs.getString("log_date"));
                int total = rs.getInt("total");
                weeklyData.put(date, total);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return weeklyData;
    }

    public int getTrainerDailyGoal(int memberId, LocalDate date) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT water_intake_ml FROM Trainer_Daily_Goals " +
                    "WHERE member_id = ? AND goal_date = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, date.toString());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("water_intake_ml");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}

