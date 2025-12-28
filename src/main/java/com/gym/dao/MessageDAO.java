package com.gym.dao;

import com.gym.models.Conversation;
import com.gym.models.Message;
import com.gym.utils.DatabaseConnection;

import java. sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public List<Conversation> getConversations(int memberId) {
        List<Conversation> conversations = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Simplified query that works with SQLite
            String sql = """
                WITH ConversationPartners AS (
                    SELECT DISTINCT
                        CASE 
                            WHEN sender_id = ? AND sender_type = 'MEMBER' THEN receiver_id
                            ELSE sender_id
                        END as partner_id
                    FROM Trainer_Member_Messages
                    WHERE sender_id = ? OR receiver_id = ?
                )
                SELECT 
                    cp.partner_id,
                    'TRAINER' as partner_type,
                    t.full_name as partner_name,
                    (SELECT message_text 
                     FROM Trainer_Member_Messages 
                     WHERE (sender_id = ? AND receiver_id = cp.partner_id) 
                        OR (sender_id = cp.partner_id AND receiver_id = ?)
                     ORDER BY sent_date DESC 
                     LIMIT 1) as last_message,
                    (SELECT sent_date 
                     FROM Trainer_Member_Messages 
                     WHERE (sender_id = ? AND receiver_id = cp.partner_id) 
                        OR (sender_id = cp.partner_id AND receiver_id = ?)
                     ORDER BY sent_date DESC 
                     LIMIT 1) as last_message_time,
                    (SELECT COUNT(*) 
                     FROM Trainer_Member_Messages 
                     WHERE receiver_id = ? 
                       AND sender_id = cp.partner_id 
                       AND sender_type = 'TRAINER' 
                       AND read_status = 0) as unread_count
                FROM ConversationPartners cp
                LEFT JOIN Trainers t ON cp.partner_id = t.trainer_id
                WHERE cp.partner_id IS NOT NULL
                ORDER BY last_message_time DESC
            """;

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, memberId);
            pstmt.setInt(3, memberId);
            pstmt.setInt(4, memberId);
            pstmt.setInt(5, memberId);
            pstmt.setInt(6, memberId);
            pstmt.setInt(7, memberId);
            pstmt.setInt(8, memberId);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Conversation conv = new Conversation();
                conv.setUserId(rs.getInt("partner_id"));
                conv.setUserType(rs.getString("partner_type"));
                conv.setUserName(rs.getString("partner_name"));
                conv.setLastMessage(rs.getString("last_message"));

                String lastMsgTime = rs.getString("last_message_time");
                if (lastMsgTime != null) {
                    conv.setLastMessageTime(LocalDateTime.parse(lastMsgTime.replace(" ", "T")));
                }

                conv.setUnreadCount(rs.getInt("unread_count"));
                conversations.add(conv);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error loading conversations: " + e.getMessage());
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

        return conversations;
    }

    public List<Message> getConversation(int trainerId, int memberId, String trainerType) {
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
                    "WHERE (m.sender_id = ? AND m.receiver_id = ? AND m.sender_type = 'TRAINER') " +
                    "   OR (m.sender_id = ? AND m.receiver_id = ? AND m.sender_type = 'MEMBER') " +
                    "ORDER BY m.sent_date ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trainerId);
            pstmt.setInt(2, memberId);
            pstmt.setInt(3, memberId);
            pstmt.setInt(4, trainerId);

            System.out.println("📧 Loading messages between Trainer ID: " + trainerId + " and Member ID: " + memberId);

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
                    message.setSentAt(LocalDateTime.parse(sentDateStr.replace(" ", "T")));
                }

                message.setRead(rs.getInt("read_status") == 1);

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

    public boolean sendMessage(int senderId, int receiverId, String senderType, String receiverType, String messageText) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "INSERT INTO Trainer_Member_Messages " +
                    "(sender_id, receiver_id, sender_type, message_text, sent_date, read_status) " +
                    "VALUES (?, ?, ?, ?, datetime('now'), 0)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receiverId);
            pstmt.setString(3, senderType);
            pstmt.setString(4, messageText);

            int result = pstmt.executeUpdate();
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

    public boolean markAsRead(int messageId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "UPDATE Trainer_Member_Messages " +
                    "SET read_status = 1, read_date = datetime('now') " +
                    "WHERE message_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, messageId);

            int result = pstmt.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error marking message as read: " + e.getMessage());
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

    public boolean markConversationAsRead(int memberId, int trainerId, String trainerType) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "UPDATE Trainer_Member_Messages " +
                    "SET read_status = 1, read_date = datetime('now') " +
                    "WHERE receiver_id = ? AND sender_id = ? AND sender_type = 'TRAINER' AND read_status = 0";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, trainerId);

            int result = pstmt.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error marking conversation as read: " + e.getMessage());
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

    // Update existing getUnreadCount to work for members
    public int getUnreadCount(int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT COUNT(*) as unread_count " +
                    "FROM Trainer_Member_Messages " +
                    "WHERE receiver_id = ? AND read_status = 0";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("unread_count");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting unread count: " + e.getMessage());
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

        return 0;
    }
}