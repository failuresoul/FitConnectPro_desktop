package com.gym.dao;

import com.gym.models.Member;
import com.gym.services.Session;
import com.gym.utils.DatabaseConnection;
import com.gym.utils. PasswordUtil;

import java.sql. Connection;
import java.sql. PreparedStatement;
import java. sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MemberDAO {

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
                System.err.println("Username or email already exists!");
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
            pstmt. setString(8, member.getMembershipType());
            pstmt.setString(9, member.getMembershipStart() != null ? member.getMembershipStart(). toString() : null);
            pstmt.setString(10, member.getMembershipEnd() != null ? member.getMembershipEnd().toString() : null);
            pstmt.setString(11, "ACTIVE");
            pstmt. setInt(12, member.getCreatedByAdminId());
            pstmt.setString(13, LocalDateTime.now(). toString());

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

            if (success) {
                // Get the last inserted member ID using SQLite's last_insert_rowid()
                pstmt. close();
                pstmt = conn.prepareStatement("SELECT last_insert_rowid() as id");
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    member.setMemberId(rs.getInt("id"));
                }

                System.out.println("Member registered successfully with ID: " + member.getMemberId());
            }

        } catch (SQLException e) {
            System.err.println("Error registering member: " + e.getMessage());
            e. printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return success;
    }
}