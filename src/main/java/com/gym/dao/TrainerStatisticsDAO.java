package com.gym.dao;

import com.gym.models.Message;
import com.gym.utils.DatabaseConnection;

import java. sql.Connection;
import java. sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TrainerStatisticsDAO {

    public int getMyClientsCount(int trainerId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT COUNT(*) as count FROM Trainer_Member_Assignment " +
                    "WHERE trainer_id = ? AND status = 'ACTIVE'";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trainerId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("Error getting clients count: " + e.getMessage());
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

    public int getTodayCompletedWorkouts(int trainerId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT COUNT(*) as count FROM Workouts w " +
                    "JOIN Trainer_Member_Assignment tma ON w.member_id = tma.member_id " +
                    "WHERE tma.trainer_id = ?  AND DATE(w.workout_date) = DATE('now')";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trainerId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("count");
            }

        } catch (SQLException e) {
            System. err.println("Error getting today's workouts: " + e.getMessage());
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

    public int getPendingWorkoutPlans(int trainerId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT COUNT(*) as count FROM Trainer_Workout_Plans " +
                    "WHERE trainer_id = ? AND status = 'PENDING'";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trainerId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("Error getting pending plans: " + e.getMessage());
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

    public List<Message> getRecentMessages(int trainerId, int limit) {
        List<Message> messages = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT m.*, mem.full_name as sender_name " +
                    "FROM Trainer_Member_Messages m " +
                    "LEFT JOIN Members mem ON m.sender_id = mem.member_id AND m.sender_type = 'MEMBER' " +
                    "WHERE m.receiver_id = ? " +
                    "ORDER BY m.sent_date DESC LIMIT ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trainerId);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Message message = new Message();
                message.setMessageId(rs. getInt("message_id"));
                message.setSenderId(rs.getInt("sender_id"));
                message.setReceiverId(rs.getInt("receiver_id"));
                message.setSenderType(rs.getString("sender_type"));
                message.setSenderName(rs.getString("sender_name"));
                message.setMessageText(rs.getString("message_text"));

                String sentDateStr = rs.getString("sent_date");
                if (sentDateStr != null) {
                    try {
                        message.setSentAt(LocalDateTime.parse(sentDateStr.replace(" ", "T")));
                    } catch (Exception e) {
                        message.setSentAt(null);
                    }
                }

                message.setRead(rs.getInt("read_status") == 1);

                messages.add(message);
            }

        } catch (SQLException e) {
            System.err.println("Error getting recent messages: " + e.getMessage());
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

        return messages;
    }
}