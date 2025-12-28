package com.gym.dao;

import com.gym.models.FriendRequest;
import com.gym.models.Member;
import com.gym.utils.DatabaseConnection;
import com.gym.utils.DatabaseInitializer;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SocialDAO {

    public SocialDAO() {
        // Initialize Friend_Requests table if it doesn't exist
        DatabaseInitializer.initializeFriendRequestsTable();
    }

    /**
     * Get all active members except the specified member
     */
    public List<Member> getAllActiveMembers(int excludeMemberId) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT m.*, mp.primary_goal " +
                    "FROM Members m " +
                    "LEFT JOIN Member_Profiles mp ON m.member_id = mp.member_id " +
                    "WHERE m.account_status = 'ACTIVE' AND m.member_id != ? " +
                    "ORDER BY m.full_name ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, excludeMemberId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                members.add(extractMemberFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return members;
    }

    /**
     * Search members by keyword (name or email)
     */
    public List<Member> searchMembers(String keyword, int excludeMemberId) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT m.*, mp.primary_goal " +
                    "FROM Members m " +
                    "LEFT JOIN Member_Profiles mp ON m.member_id = mp.member_id " +
                    "WHERE m.account_status = 'ACTIVE' " +
                    "AND m.member_id != ? " +
                    "AND (LOWER(m.full_name) LIKE LOWER(?) OR LOWER(m.email) LIKE LOWER(?)) " +
                    "ORDER BY m.full_name ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, excludeMemberId);
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                members.add(extractMemberFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return members;
    }

    /**
     * Send a friend request
     */
    public boolean sendFriendRequest(int senderId, int receiverId) {
        if (friendRequestExists(senderId, receiverId)) {
            return false;
        }

        String sql = "INSERT INTO Friend_Requests (sender_id, receiver_id, status, request_date) " +
                    "VALUES (?, ?, 'PENDING', ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receiverId);
            pstmt.setString(3, LocalDate.now().toString());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if a friend request already exists
     */
    private boolean friendRequestExists(int senderId, int receiverId) {
        String sql = "SELECT COUNT(*) FROM Friend_Requests " +
                    "WHERE (sender_id = ? AND receiver_id = ?) " +
                    "OR (sender_id = ? AND receiver_id = ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, senderId);
            pstmt.setInt(2, receiverId);
            pstmt.setInt(3, receiverId);
            pstmt.setInt(4, senderId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get list of friends for a member
     */
    public List<Member> getMyFriends(int memberId) {
        List<Member> friends = new ArrayList<>();
        String sql = "SELECT DISTINCT m.*, mp.primary_goal " +
                    "FROM Members m " +
                    "LEFT JOIN Member_Profiles mp ON m.member_id = mp.member_id " +
                    "WHERE m.member_id IN ( " +
                    "   SELECT CASE " +
                    "       WHEN sender_id = ? THEN receiver_id " +
                    "       ELSE sender_id " +
                    "   END " +
                    "   FROM Friend_Requests " +
                    "   WHERE (sender_id = ? OR receiver_id = ?) " +
                    "   AND status = 'ACCEPTED' " +
                    ") " +
                    "ORDER BY m.full_name ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            pstmt.setInt(2, memberId);
            pstmt.setInt(3, memberId);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                friends.add(extractMemberFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friends;
    }

    /**
     * Get received friend requests for a member
     */
    public List<FriendRequest> getReceivedRequests(int memberId) {
        List<FriendRequest> requests = new ArrayList<>();
        String sql = "SELECT fr.*, " +
                    "sender.full_name as sender_name, sender.email as sender_email " +
                    "FROM Friend_Requests fr " +
                    "JOIN Members sender ON fr.sender_id = sender.member_id " +
                    "WHERE fr.receiver_id = ? AND fr.status = 'PENDING' " +
                    "ORDER BY fr.request_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                FriendRequest request = new FriendRequest();
                request.setRequestId(rs.getInt("request_id"));
                request.setSenderId(rs.getInt("sender_id"));
                request.setReceiverId(rs.getInt("receiver_id"));
                request.setSenderName(rs.getString("sender_name"));
                request.setSenderEmail(rs.getString("sender_email"));
                request.setStatus(rs.getString("status"));

                String requestDate = rs.getString("request_date");
                if (requestDate != null && !requestDate.isEmpty()) {
                    request.setRequestDate(LocalDate.parse(requestDate));
                }

                requests.add(request);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Get sent friend requests for a member
     */
    public List<FriendRequest> getSentRequests(int memberId) {
        List<FriendRequest> requests = new ArrayList<>();
        String sql = "SELECT fr.*, " +
                    "receiver.full_name as receiver_name, receiver.email as receiver_email " +
                    "FROM Friend_Requests fr " +
                    "JOIN Members receiver ON fr.receiver_id = receiver.member_id " +
                    "WHERE fr.sender_id = ? AND fr.status = 'PENDING' " +
                    "ORDER BY fr.request_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                FriendRequest request = new FriendRequest();
                request.setRequestId(rs.getInt("request_id"));
                request.setSenderId(rs.getInt("sender_id"));
                request.setReceiverId(rs.getInt("receiver_id"));
                request.setReceiverName(rs.getString("receiver_name"));
                request.setReceiverEmail(rs.getString("receiver_email"));
                request.setStatus(rs.getString("status"));

                String requestDate = rs.getString("request_date");
                if (requestDate != null && !requestDate.isEmpty()) {
                    request.setRequestDate(LocalDate.parse(requestDate));
                }

                requests.add(request);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    /**
     * Accept a friend request
     */
    public boolean acceptFriendRequest(int requestId) {
        String sql = "UPDATE Friend_Requests SET status = 'ACCEPTED', response_date = ? " +
                    "WHERE request_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, LocalDate.now().toString());
            pstmt.setInt(2, requestId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reject a friend request
     */
    public boolean rejectFriendRequest(int requestId) {
        String sql = "UPDATE Friend_Requests SET status = 'REJECTED', response_date = ? " +
                    "WHERE request_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, LocalDate.now().toString());
            pstmt.setInt(2, requestId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Unfriend - remove friendship
     */
    public boolean unfriend(int memberId1, int memberId2) {
        String sql = "DELETE FROM Friend_Requests " +
                    "WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) " +
                    "AND status = 'ACCEPTED'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId1);
            pstmt.setInt(2, memberId2);
            pstmt.setInt(3, memberId2);
            pstmt.setInt(4, memberId1);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if two members are friends
     */
    public boolean areFriends(int memberId1, int memberId2) {
        String sql = "SELECT COUNT(*) FROM Friend_Requests " +
                    "WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) " +
                    "AND status = 'ACCEPTED'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, memberId1);
            pstmt.setInt(2, memberId2);
            pstmt.setInt(3, memberId2);
            pstmt.setInt(4, memberId1);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Extract Member object from ResultSet
     */
    private Member extractMemberFromResultSet(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setMemberId(rs.getInt("member_id"));
        member.setFullName(rs.getString("full_name"));
        member.setEmail(rs.getString("email"));
        member.setPhone(rs.getString("phone"));
        member.setGender(rs.getString("gender"));

        String dob = rs.getString("date_of_birth");
        if (dob != null && !dob.isEmpty()) {
            member.setDateOfBirth(LocalDate.parse(dob));
        }

        member.setMembershipType(rs.getString("membership_type"));

        String membershipStart = rs.getString("membership_start");
        if (membershipStart != null && !membershipStart.isEmpty()) {
            member.setMembershipStart(LocalDate.parse(membershipStart));
        }

        String membershipEnd = rs.getString("membership_end");
        if (membershipEnd != null && !membershipEnd.isEmpty()) {
            member.setMembershipEnd(LocalDate.parse(membershipEnd));
        }

        member.setAccountStatus(rs.getString("account_status"));

        try {
            String fitnessGoal = rs.getString("primary_goal");
            member.setFitnessGoal(fitnessGoal);
        } catch (SQLException e) {
            // Column might not exist
        }

        return member;
    }
}
