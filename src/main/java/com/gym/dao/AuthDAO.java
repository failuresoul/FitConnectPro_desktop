package com.gym.dao;

import com.gym.models.Admin;
import com.gym.models.Member;
import com.gym.models.Trainer;
import com. gym.utils.DatabaseConnection;
import com.gym.utils. PasswordUtil;

import java.sql. Connection;
import java.sql. PreparedStatement;
import java. sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util. Arrays;

public class AuthDAO {

    public Admin authenticateAdmin(String username, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Admin admin = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT admin_id, username, password_hash, full_name, email, phone, role, " +
                    "created_date, last_login FROM Admins WHERE username = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");

                if (PasswordUtil.verifyPassword(password, storedHash)) {
                    admin = new Admin();
                    admin.setAdminId(rs.getInt("admin_id"));
                    admin. setUsername(rs.getString("username"));
                    admin.setPasswordHash(storedHash);
                    admin.setFullName(rs.getString("full_name"));
                    admin.setEmail(rs.getString("email"));
                    admin.setPhone(rs.getString("phone"));
                    admin. setRole(rs.getString("role"));

                    String createdDateStr = rs.getString("created_date");
                    if (createdDateStr != null) {
                        admin.setCreatedDate(LocalDateTime.parse(createdDateStr. replace(" ", "T")));
                    }

                    String lastLoginStr = rs.getString("last_login");
                    if (lastLoginStr != null) {
                        admin.setLastLogin(LocalDateTime.parse(lastLoginStr.replace(" ", "T")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error authenticating admin", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Update last login in a separate transaction
        if (admin != null) {
            updateAdminLastLogin(admin. getAdminId());
        }

        return admin;
    }

    private void updateAdminLastLogin(int adminId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "UPDATE Admins SET last_login = datetime('now', 'localtime') WHERE admin_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, adminId);
            pstmt. executeUpdate();
        } catch (SQLException e) {
            // Log the error but don't throw - login should still succeed
            System.err.println("Warning: Could not update last login time: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Trainer authenticateTrainer(String username, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Trainer trainer = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT trainer_id, username, password_hash, full_name, email, phone, " +
                    "specializations, experience_years, certifications, hire_date, monthly_salary, " +
                    "account_status, max_clients, added_by_admin_id, created_date " +
                    "FROM Trainers WHERE username = ?";

            pstmt = conn. prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String accountStatus = rs.getString("account_status");

                if (!"ACTIVE".equalsIgnoreCase(accountStatus)) {
                    return null;
                }

                String storedHash = rs.getString("password_hash");

                if (PasswordUtil.verifyPassword(password, storedHash)) {
                    trainer = new Trainer();
                    trainer.setTrainerId(rs.getInt("trainer_id"));
                    trainer.setUsername(rs.getString("username"));
                    trainer. setPasswordHash(storedHash);
                    trainer.setFullName(rs.getString("full_name"));
                    trainer.setEmail(rs.getString("email"));
                    trainer.setPhone(rs.getString("phone"));

                    String specializationsStr = rs.getString("specializations");
                    if (specializationsStr != null && !specializationsStr.isEmpty()) {
                        trainer.setSpecializations(Arrays. asList(specializationsStr.split(",")));
                    }

                    trainer.setExperienceYears(rs.getInt("experience_years"));
                    trainer.setCertifications(rs.getString("certifications"));

                    String hireDateStr = rs.getString("hire_date");
                    if (hireDateStr != null) {
                        trainer.setHireDate(LocalDate.parse(hireDateStr));
                    }

                    trainer.setMonthlySalary(rs. getDouble("monthly_salary"));
                    trainer.setAccountStatus(accountStatus);
                    trainer. setMaxClients(rs.getInt("max_clients"));
                    trainer.setAddedByAdminId(rs.getInt("added_by_admin_id"));

                    String createdDateStr = rs.getString("created_date");
                    if (createdDateStr != null) {
                        trainer.setCreatedDate(LocalDateTime.parse(createdDateStr.replace(" ", "T")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error authenticating trainer", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Update last login in a separate transaction
        if (trainer != null) {
            updateTrainerLastLogin(trainer.getTrainerId());
        }

        return trainer;
    }

    private void updateTrainerLastLogin(int trainerId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection. getInstance().getConnection();

            // Note: Trainers table doesn't have last_login column, using created_date as workaround
            // In production, you should add a last_login column to Trainers table
            String sql = "UPDATE Trainers SET created_date = datetime('now', 'localtime') WHERE trainer_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trainerId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // Log the error but don't throw - login should still succeed
            System.err. println("Warning: Could not update trainer last login time: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e. printStackTrace();
            }
        }
    }

    public Member authenticateMember(String username, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Member member = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT member_id, username, password_hash, full_name, email, phone, " +
                    "date_of_birth, gender, profile_photo, membership_type, membership_start, " +
                    "membership_end, account_status, created_by_admin_id, created_date, last_login " +
                    "FROM Members WHERE username = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String accountStatus = rs.getString("account_status");

                if (!"ACTIVE".equalsIgnoreCase(accountStatus)) {
                    return null;
                }

                String membershipEndStr = rs.getString("membership_end");
                if (membershipEndStr != null) {
                    LocalDate membershipEnd = LocalDate. parse(membershipEndStr);
                    if (membershipEnd.isBefore(LocalDate.now())) {
                        return null;
                    }
                }

                String storedHash = rs. getString("password_hash");

                if (PasswordUtil. verifyPassword(password, storedHash)) {
                    member = new Member();
                    member. setMemberId(rs.getInt("member_id"));
                    member.setUsername(rs. getString("username"));
                    member.setPasswordHash(storedHash);
                    member.setFullName(rs.getString("full_name"));
                    member.setEmail(rs.getString("email"));
                    member.setPhone(rs.getString("phone"));

                    String dobStr = rs.getString("date_of_birth");
                    if (dobStr != null) {
                        member.setDateOfBirth(LocalDate.parse(dobStr));
                    }

                    member.setGender(rs.getString("gender"));
                    member.setProfilePhoto(rs.getString("profile_photo"));
                    member.setMembershipType(rs.getString("membership_type"));

                    String membershipStartStr = rs.getString("membership_start");
                    if (membershipStartStr != null) {
                        member.setMembershipStart(LocalDate.parse(membershipStartStr));
                    }

                    if (membershipEndStr != null) {
                        member.setMembershipEnd(LocalDate.parse(membershipEndStr));
                    }

                    member.setAccountStatus(accountStatus);
                    member.setCreatedByAdminId(rs. getInt("created_by_admin_id"));

                    String createdDateStr = rs.getString("created_date");
                    if (createdDateStr != null) {
                        member.setCreatedDate(LocalDateTime.parse(createdDateStr. replace(" ", "T")));
                    }

                    String lastLoginStr = rs. getString("last_login");
                    if (lastLoginStr != null) {
                        member.setLastLogin(LocalDateTime.parse(lastLoginStr.replace(" ", "T")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error authenticating member", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Update last login in a separate transaction
        if (member != null) {
            updateMemberLastLogin(member.getMemberId());
        }

        return member;
    }

    private void updateMemberLastLogin(int memberId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection. getInstance().getConnection();

            String sql = "UPDATE Members SET last_login = datetime('now', 'localtime') WHERE member_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt. setInt(1, memberId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // Log the error but don't throw - login should still succeed
            System.err.println("Warning: Could not update member last login time: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt. close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean changePassword(int userId, String userType, String newPassword) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            String sql = "";

            switch (userType. toUpperCase()) {
                case "ADMIN":
                    sql = "UPDATE Admins SET password_hash = ? WHERE admin_id = ?";
                    break;
                case "TRAINER":
                    sql = "UPDATE Trainers SET password_hash = ? WHERE trainer_id = ?";
                    break;
                case "MEMBER":
                    sql = "UPDATE Members SET password_hash = ? WHERE member_id = ?";
                    break;
                default:
                    throw new IllegalArgumentException("Invalid user type: " + userType);
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error changing password", e);
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
}