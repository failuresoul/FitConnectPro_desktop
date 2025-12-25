package com.gym.dao;

import com.gym.models.Message;
import com.gym.utils.DatabaseConnection;

import java. sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public List<Message> getConversation(int trainerId, int memberId) {
        List<Message> messages = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT m.*, " +
                    "t.full_name as trainer_name, " +
                    "mem.full_name as member_name " +
                    "FROM Trainer_Member_Messages m " +
                    "LEFT JOIN Trainers t ON m.sender_id = t.trainer_id AND m.sender_type = 'TRAINER' " +
                    "LEFT JOIN Members mem ON m.sender_id = mem.member_id AND m.sender_type = 'MEMBER' " +
                    "WHERE (m.sender_id = ? AND m.receiver_id = ?  AND m.sender_type = 'TRAINER') " +
                    "   OR (m.sender_id = ? AND m.receiver_id = ?  AND m.sender_type = 'MEMBER') " +
                    "ORDER BY m.sent_date ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trainerId);
            pstmt.setInt(2, memberId);
            pstmt.setInt(3, memberId);
            pstmt. setInt(4, trainerId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Message message = new Message();
                message.setMessageId(rs.getInt("message_id"));
                message.setSenderId(rs.getInt("sender_id"));
                message.setReceiverId(rs.getInt("receiver_id"));
                message.setSenderType(rs.getString("sender_type"));
                message.setMessageText(rs.getString("message_text"));

                String sentDateStr = rs.getString("sent_date");
                if (sentDateStr != null) {
                    message.setSentDate(LocalDateTime.parse(sentDateStr. replace(" ", "T")));
                }

                message.setReadStatus(rs. getInt("read_status") == 1);

                String readDateStr = rs.getString("read_date");
                if (readDateStr != null) {
                    message.setReadDate(LocalDateTime.parse(readDateStr.replace(" ", "T")));
                }

                messages.add(message);
            }

            System.out.println("✅ Loaded " + messages.size() + " messages");

        } catch (SQLException e) {
            System.err.println("❌ Error loading messages: " + e.getMessage());
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

    public boolean sendMessage(Message message) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "INSERT INTO Trainer_Member_Messages " +
                    "(sender_id, receiver_id, sender_type, message_text, sent_date, read_status) " +
                    "VALUES (?, ?, ?, ?, datetime('now'), 0)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, message.getSenderId());
            pstmt.setInt(2, message.getReceiverId());
            pstmt.setString(3, message.getSenderType());
            pstmt.setString(4, message.getMessageText());

            int result = pstmt.executeUpdate();
            System.out.println("✅ Message sent successfully");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error sending message: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public int getUnreadCount(int trainerId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT COUNT(*) as unread_count " +
                    "FROM Trainer_Member_Messages " +
                    "WHERE receiver_id = ? AND sender_type = 'MEMBER' AND read_status = 0";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trainerId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("unread_count");
            }

        } catch (SQLException e) {
            System.err. println("❌ Error getting unread count: " + e. getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection. getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    public boolean markAsRead(int trainerId, int memberId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection. getInstance().getConnection();
            String sql = "UPDATE Trainer_Member_Messages " +
                    "SET read_status = 1, read_date = datetime('now') " +
                    "WHERE receiver_id = ? AND sender_id = ? AND sender_type = 'MEMBER' AND read_status = 0";

            pstmt = conn. prepareStatement(sql);
            pstmt.setInt(1, trainerId);
            pstmt. setInt(2, memberId);

            int result = pstmt.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error marking messages as read: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection. getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}