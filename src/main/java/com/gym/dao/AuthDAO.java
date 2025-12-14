package com.gym.dao;

import com.gym.models.Admin;
import com.gym.models.Member;
import com.gym.models.Trainer;
import com. gym.utils.DatabaseConnection;
import com.gym.utils. PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql. SQLException;
import java.time. LocalDate;
import java.time.LocalDateTime;
import java. util.Arrays;

public class AuthDAO {

    public Admin authenticateAdmin(String username, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Admin admin = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM Admins WHERE username = ? ";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (PasswordUtil.checkPassword(password, storedHash)) {
                    admin = new Admin();
                    admin.setAdminId(rs.getInt("admin_id"));
                    admin.setUsername(rs.getString("username"));
                    admin.setPasswordHash(storedHash);
                    admin. setFullName(rs.getString("full_name"));
                    admin.setEmail(rs.getString("email"));
                    admin.setPhone(rs.getString("phone"));
                    admin.setRole(rs.getString("role"));
                    admin.setAccountStatus(rs.getString("account_status"));

                    String createdDateStr = rs.getString("created_date");
                    if (createdDateStr != null) {
                        admin.setCreatedDate(LocalDateTime.parse(createdDateStr));
                    }

                    String lastLoginStr = rs.getString("last_login");
                    if (lastLoginStr != null) {
                        admin.setLastLogin(LocalDateTime.parse(lastLoginStr));
                    }

                    updateLastLogin(conn, admin.getAdminId(), "Admins", "admin_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating admin: " + e.getMessage());
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

        return admin;
    }

    public Trainer authenticateTrainer(String username, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Trainer trainer = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM Trainers WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt. executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (PasswordUtil.checkPassword(password, storedHash)) {
                    trainer = new Trainer();
                    trainer.setTrainerId(rs.getInt("trainer_id"));
                    trainer.setUsername(rs.getString("username"));
                    trainer.setPasswordHash(storedHash);
                    trainer.setFullName(rs.getString("full_name"));
                    trainer.setEmail(rs.getString("email"));
                    trainer. setPhone(rs.getString("phone"));

                    String specializationsStr = rs.getString("specializations");
                    if (specializationsStr != null && !specializationsStr.isEmpty()) {
                        trainer.setSpecializations(Arrays.asList(specializationsStr.split(",")));
                    }

                    trainer.setExperienceYears(rs.getInt("experience_years"));
                    trainer.setCertifications(rs.getString("certifications"));
                    trainer.setMaxClients(rs.getInt("max_clients"));
                    trainer.setCurrentClients(rs. getInt("current_clients"));
                    trainer.setAccountStatus(rs.getString("account_status"));
                    trainer.setSalary(rs.getDouble("salary"));
                    trainer.setHiredByAdminId(rs.getInt("hired_by_admin_id"));

                    String hireDateStr = rs.getString("hire_date");
                    if (hireDateStr != null) {
                        // Check if it's a date or datetime format
                        if (hireDateStr.contains("T")) {
                            trainer.setHireDate(LocalDateTime.parse(hireDateStr));
                        } else {
                            // Convert LocalDate to LocalDateTime at start of day
                            trainer.setHireDate(LocalDate.parse(hireDateStr).atStartOfDay());
                        }
                    }

                    String lastLoginStr = rs.getString("last_login");
                    if (lastLoginStr != null) {
                        if (lastLoginStr.contains("T")) {
                            trainer.setLastLogin(LocalDateTime.parse(lastLoginStr));
                        } else {
                            trainer.setLastLogin(LocalDate.parse(lastLoginStr).atStartOfDay());
                        }
                    }

                    updateLastLogin(conn, trainer.getTrainerId(), "Trainers", "trainer_id");
                }
            }
        } catch (SQLException e) {
            System.err. println("Error authenticating trainer: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e. printStackTrace();
            }
        }

        return trainer;
    }

    public Member authenticateMember(String username, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Member member = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM Members WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (PasswordUtil. checkPassword(password, storedHash)) {
                    member = new Member();
                    member.setMemberId(rs.getInt("member_id"));
                    member.setUsername(rs.getString("username"));
                    member. setPasswordHash(storedHash);
                    member.setFullName(rs.getString("full_name"));
                    member.setEmail(rs.getString("email"));
                    member.setPhone(rs.getString("phone"));

                    String dobStr = rs.getString("date_of_birth");
                    if (dobStr != null) {
                        member.setDateOfBirth(LocalDate.parse(dobStr));
                    }

                    member.setGender(rs.getString("gender"));
                    member.setMembershipType(rs.getString("membership_type"));

                    String startStr = rs.getString("membership_start");
                    if (startStr != null) {
                        member.setMembershipStart(LocalDate.parse(startStr));
                    }

                    String endStr = rs.getString("membership_end");
                    if (endStr != null) {
                        member.setMembershipEnd(LocalDate.parse(endStr));
                    }

                    member.setAccountStatus(rs.getString("account_status"));
                    member.setCreatedByAdminId(rs.getInt("created_by_admin_id"));

                    String createdDateStr = rs.getString("created_date");
                    if (createdDateStr != null) {
                        if (createdDateStr.contains("T")) {
                            member. setCreatedDate(LocalDateTime. parse(createdDateStr));
                        } else {
                            member.setCreatedDate(LocalDate.parse(createdDateStr).atStartOfDay());
                        }
                    }

                    String lastLoginStr = rs. getString("last_login");
                    if (lastLoginStr != null) {
                        if (lastLoginStr.contains("T")) {
                            member.setLastLogin(LocalDateTime.parse(lastLoginStr));
                        } else {
                            member.setLastLogin(LocalDate.parse(lastLoginStr).atStartOfDay());
                        }
                    }

                    updateLastLogin(conn, member.getMemberId(), "Members", "member_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating member: " + e.getMessage());
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

    private void updateLastLogin(Connection conn, int userId, String tableName, String idColumn) {
        PreparedStatement pstmt = null;
        try {
            String sql = "UPDATE " + tableName + " SET last_login = ?  WHERE " + idColumn + " = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}