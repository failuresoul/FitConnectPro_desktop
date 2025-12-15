package com.gym.dao;

import com.gym.models.Member;
import com.gym.utils.DatabaseConnection;
import com.gym.utils.PasswordUtil;

import java.sql. Connection;
import java.sql. PreparedStatement;
import java. sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    // Register a new member
    public boolean registerMember(Member member) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Check if username or email already exists
            String checkSql = "SELECT COUNT(*) as count FROM Members WHERE username = ? OR email = ? ";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, member.getUsername());
            pstmt.setString(2, member.getEmail());
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt("count") > 0) {
                System.err.println("❌ Username or email already exists!");
                return false;
            }

            rs.close();
            pstmt.close();

            // Hash password
            String hashedPassword = PasswordUtil.hashPassword(member.getPasswordHash());

            // Insert member
            String sql = "INSERT INTO Members (username, password_hash, full_name, email, phone, " +
                    "date_of_birth, gender, membership_type, membership_start, membership_end, " +
                    "account_status, created_by_admin_id, created_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, member.getUsername());
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, member.getFullName());
            pstmt.setString(4, member.getEmail());
            pstmt.setString(5, member.getPhone());
            pstmt.setString(6, member.getDateOfBirth() != null ? member.getDateOfBirth().toString() : null);
            pstmt.setString(7, member.getGender());
            pstmt.setString(8, member.getMembershipType());
            pstmt.setString(9, member.getMembershipStart() != null ? member.getMembershipStart().toString() : null);
            pstmt.setString(10, member.getMembershipEnd() != null ? member.getMembershipEnd().toString() : null);
            pstmt.setString(11, "ACTIVE");
            pstmt.setInt(12, member.getCreatedByAdminId());
            pstmt. setString(13, LocalDateTime. now().toString());

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

            if (success) {
                // Get the last inserted member ID using SQLite's last_insert_rowid()
                pstmt.close();
                pstmt = conn.prepareStatement("SELECT last_insert_rowid() as id");
                rs = pstmt. executeQuery();
                if (rs.next()) {
                    member.setMemberId(rs. getInt("id"));
                }

                System.out.println("✅ Member registered successfully with ID:  " + member.getMemberId());
            }

        } catch (SQLException e) {
            System. err.println("❌ Error registering member: " + e. getMessage());
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

        return success;
    }

    // Get assigned trainer ID for a member
    public Integer getAssignedTrainerId(int memberId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Integer trainerId = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT trainer_id FROM Trainer_Member_Assignment " +
                    "WHERE member_id = ? AND status = 'ACTIVE' " +
                    "ORDER BY assignment_date DESC LIMIT 1";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                trainerId = rs.getInt("trainer_id");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting assigned trainer:  " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return trainerId;
    }

    // Get unassigned members (or all members for reassignment option)
    public List<Member> getUnassignedMembers() {
        List<Member> members = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Get all active members (both assigned and unassigned)
            // You can change this to only get unassigned if needed
            String sql = "SELECT * FROM Members WHERE account_status = 'ACTIVE' ORDER BY full_name";

            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Member member = extractMemberFromResultSet(rs);
                members.add(member);
            }

            System.out.println("✅ Loaded " + members.size() + " members for assignment");

        } catch (SQLException e) {
            System.err.println("❌ Error getting unassigned members: " + e.getMessage());
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

        return members;
    }

    // Get all members
    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM Members ORDER BY member_id DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                members.add(extractMemberFromResultSet(rs));
            }

            System.out.println("✅ Loaded " + members. size() + " total members");

        } catch (SQLException e) {
            System.err.println("❌ Error getting all members: " + e.getMessage());
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

        return members;
    }

    // Get member by ID
    public Member getMemberById(int memberId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Member member = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM Members WHERE member_id = ? ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                member = extractMemberFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting member by ID:  " + e.getMessage());
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

        return member;
    }

    // Update member
    public boolean updateMember(Member member) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "UPDATE Members SET full_name = ?, email = ?, phone = ?, " +
                    "gender = ?, membership_type = ?, membership_start = ?, membership_end = ?, " +
                    "account_status = ?  WHERE member_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, member.getFullName());
            pstmt.setString(2, member.getEmail());
            pstmt.setString(3, member.getPhone());
            pstmt.setString(4, member.getGender());
            pstmt.setString(5, member.getMembershipType());
            pstmt.setString(6, member.getMembershipStart() != null ? member.getMembershipStart().toString() : null);
            pstmt. setString(7, member.getMembershipEnd() != null ? member.getMembershipEnd().toString() : null);
            pstmt.setString(8, member. getAccountStatus());
            pstmt.setInt(9, member.getMemberId());

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

            if (success) {
                System.out.println("✅ Member updated successfully:  " + member.getMemberId());
            }

        } catch (SQLException e) {
            System.err.println("❌ Error updating member: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return success;
    }

    // Delete member
    public boolean deleteMember(int memberId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "DELETE FROM Members WHERE member_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

            if (success) {
                System.out.println("✅ Member deleted successfully: " + memberId);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error deleting member: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return success;
    }

    // Helper method to extract member from ResultSet
    private Member extractMemberFromResultSet(ResultSet rs) throws SQLException {
        Member member = new Member();

        member.setMemberId(rs. getInt("member_id"));
        member.setUsername(rs.getString("username"));
        member.setPasswordHash(rs.getString("password_hash"));
        member.setFullName(rs.getString("full_name"));
        member.setEmail(rs.getString("email"));
        member.setPhone(rs. getString("phone"));

        // Handle date of birth
        String dobStr = rs.getString("date_of_birth");
        if (dobStr != null && !dobStr.isEmpty()) {
            member.setDateOfBirth(LocalDate.parse(dobStr));
        }

        member.setGender(rs.getString("gender"));
        member.setMembershipType(rs.getString("membership_type"));

        // Handle membership dates
        String startStr = rs.getString("membership_start");
        if (startStr != null && !startStr.isEmpty()) {
            member.setMembershipStart(LocalDate.parse(startStr));
        }

        String endStr = rs.getString("membership_end");
        if (endStr != null && !endStr.isEmpty()) {
            member.setMembershipEnd(LocalDate.parse(endStr));
        }

        member.setAccountStatus(rs.getString("account_status"));
        member.setCreatedByAdminId(rs.getInt("created_by_admin_id"));

        // Handle created date
        String createdStr = rs.getString("created_date");
        if (createdStr != null && !createdStr. isEmpty()) {
            if (createdStr.contains("T")) {
                member. setCreatedDate(LocalDateTime. parse(createdStr));
            } else {
                member.setCreatedDate(LocalDate.parse(createdStr).atStartOfDay());
            }
        }

        // Handle last login
        try {
            String lastLoginStr = rs.getString("last_login");
            if (lastLoginStr != null && !lastLoginStr.isEmpty()) {
                if (lastLoginStr.contains("T")) {
                    member.setLastLogin(LocalDateTime.parse(lastLoginStr));
                } else {
                    member.setLastLogin(LocalDate.parse(lastLoginStr).atStartOfDay());
                }
            }
        } catch (SQLException e) {
            // Column might not exist
            member.setLastLogin(null);
        }

        return member;
    }
}