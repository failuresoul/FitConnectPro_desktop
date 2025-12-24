package com.gym.dao;

import com.gym.models.Trainer;
import com.gym.utils.DatabaseConnection;
import com.gym.utils. PasswordUtil;

import java.sql. Connection;
import java.sql. PreparedStatement;
import java. sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util. ArrayList;
import java.util. Arrays;
import java.util.List;
import com.gym.models.ClientDetails;
import com.gym.models.Member;

public class TrainerDAO {

    // Register a new trainer
    public boolean registerTrainer(Trainer trainer) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Check if username or email already exists
            String checkSql = "SELECT COUNT(*) as count FROM Trainers WHERE username = ? OR email = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, trainer.getUsername());
            pstmt.setString(2, trainer.getEmail());
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt("count") > 0) {
                System.err.println("❌ Username or email already exists!");
                return false;
            }

            rs.close();
            pstmt.close();

            // Hash password
            String hashedPassword = PasswordUtil.hashPassword(trainer.getPasswordHash());

            // Convert specializations list to comma-separated string
            String specializationsStr = String.join(",", trainer.getSpecializations());

            // Insert trainer
            String sql = "INSERT INTO Trainers (username, password_hash, full_name, email, phone, " +
                    "specializations, experience_years, certifications, max_clients, account_status, " +
                    "monthly_salary, added_by_admin_id, hire_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, trainer.getUsername());
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, trainer.getFullName());
            pstmt.setString(4, trainer.getEmail());
            pstmt.setString(5, trainer.getPhone());
            pstmt.setString(6, specializationsStr);
            pstmt.setInt(7, trainer.getExperienceYears());
            pstmt. setString(8, trainer.getCertifications());
            pstmt.setInt(9, trainer. getMaxClients());
            pstmt.setString(10, trainer.getAccountStatus());
            pstmt.setDouble(11, trainer.getSalary());
            pstmt.setInt(12, trainer.getHiredByAdminId());
            pstmt.setString(13, LocalDateTime.now().toString());

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

            if (success) {
                pstmt.close();
                pstmt = conn.prepareStatement("SELECT last_insert_rowid() as id");
                rs = pstmt.executeQuery();
                if (rs. next()) {
                    trainer. setTrainerId(rs.getInt("id"));
                }

                System.out.println("✅ Trainer registered successfully with ID: " + trainer.getTrainerId());
            }

        } catch (SQLException e) {
            System.err.println("❌ Error registering trainer: " + e.getMessage());
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

        return success;
    }

    // Get all trainers
    public List<Trainer> getAllTrainers() {
        List<Trainer> trainers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM Trainers ORDER BY trainer_id DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs. next()) {
                Trainer trainer = extractTrainerFromResultSet(rs);
                trainers.add(trainer);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting all trainers: " + e.getMessage());
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

        return trainers;
    }

    // Get trainer by ID
    public Trainer getTrainerById(int trainerId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Trainer trainer = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM Trainers WHERE trainer_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trainerId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                trainer = extractTrainerFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting trainer by ID: " + e. getMessage());
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

        return trainer;
    }

    // Get assigned clients count
    public int getAssignedClientsCount(int trainerId) {
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
                count = rs. getInt("count");
            }

        } catch (SQLException e) {
            // Silent fail - table might not exist yet
            count = 0;
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

    // Update trainer
    public boolean updateTrainer(Trainer trainer) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String specializationsStr = String.join(",", trainer.getSpecializations());

            String sql = "UPDATE Trainers SET full_name = ?, email = ?, phone = ?, " +
                    "specializations = ?, experience_years = ?, certifications = ?, " +
                    "max_clients = ?, account_status = ?, monthly_salary = ?  " +
                    "WHERE trainer_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, trainer. getFullName());
            pstmt.setString(2, trainer.getEmail());
            pstmt.setString(3, trainer.getPhone());
            pstmt.setString(4, specializationsStr);
            pstmt.setInt(5, trainer. getExperienceYears());
            pstmt.setString(6, trainer.getCertifications());
            pstmt.setInt(7, trainer.getMaxClients());
            pstmt. setString(8, trainer.getAccountStatus());
            pstmt.setDouble(9, trainer. getSalary());
            pstmt.setInt(10, trainer.getTrainerId());

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

            if (success) {
                System.out.println("✅ Trainer updated successfully:  " + trainer.getTrainerId());
            }

        } catch (SQLException e) {
            System.err.println("❌ Error updating trainer: " + e. getMessage());
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

    // Delete trainer
    public boolean deleteTrainer(int trainerId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "DELETE FROM Trainers WHERE trainer_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trainerId);

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

            if (success) {
                System.out.println("✅ Trainer deleted successfully: " + trainerId);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error deleting trainer: " + e.getMessage());
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

    // Helper method to extract trainer from ResultSet - FULLY SAFE VERSION
    private Trainer extractTrainerFromResultSet(ResultSet rs) throws SQLException {
        Trainer trainer = new Trainer();

        try {
            // Required fields
            trainer.setTrainerId(getIntSafe(rs, "trainer_id", 0));
            trainer.setUsername(getStringSafe(rs, "username", ""));
            trainer.setPasswordHash(getStringSafe(rs, "password_hash", ""));
            trainer.setFullName(getStringSafe(rs, "full_name", "Unknown"));
            trainer.setEmail(getStringSafe(rs, "email", ""));

            // Optional fields
            trainer. setPhone(getStringSafe(rs, "phone", ""));

            String specializationsStr = getStringSafe(rs, "specializations", "");
            if (specializationsStr != null && !specializationsStr.isEmpty()) {
                trainer.setSpecializations(Arrays.asList(specializationsStr.split(",")));
            } else {
                trainer.setSpecializations(new ArrayList<>());
            }

            trainer.setExperienceYears(getIntSafe(rs, "experience_years", 0));
            trainer.setCertifications(getStringSafe(rs, "certifications", ""));
            trainer.setMaxClients(getIntSafe(rs, "max_clients", 10));
            trainer.setCurrentClients(0); // Will be calculated separately
            trainer.setAccountStatus(getStringSafe(rs, "account_status", "ACTIVE"));
            trainer.setSalary(getDoubleSafe(rs, "monthly_salary", 0.0));
            trainer.setHiredByAdminId(getIntSafe(rs, "added_by_admin_id", 0));

            // Date fields - may not exist
            String hireDateStr = getStringSafe(rs, "hire_date", null);
            if (hireDateStr != null && !hireDateStr.isEmpty()) {
                try {
                    if (hireDateStr.contains("T")) {
                        trainer.setHireDate(LocalDateTime.parse(hireDateStr));
                    } else {
                        trainer.setHireDate(LocalDate.parse(hireDateStr).atStartOfDay());
                    }
                } catch (Exception e) {
                    trainer.setHireDate(null);
                }
            }

            // last_login may not exist in database - skip it
            trainer.setLastLogin(null);

        } catch (Exception e) {
            System.err.println("❌ Error extracting trainer data: " + e.getMessage());
            e.printStackTrace();
        }

        return trainer;
    }

    // Safe getter methods to handle missing columns
    private String getStringSafe(ResultSet rs, String columnName, String defaultValue) {
        try {
            String value = rs.getString(columnName);
            return (value != null) ? value : defaultValue;
        } catch (SQLException e) {
            return defaultValue;
        }
    }

    private int getIntSafe(ResultSet rs, String columnName, int defaultValue) {
        try {
            return rs.getInt(columnName);
        } catch (SQLException e) {
            return defaultValue;
        }
    }

    private double getDoubleSafe(ResultSet rs, String columnName, double defaultValue) {
        try {
            return rs.getDouble(columnName);
        } catch (SQLException e) {
            return defaultValue;
        }
    }
    // Add to existing TrainerDAO. java

    // Assign trainer to member
    public boolean assignTrainerToMember(int trainerId, int memberId, int adminId, LocalDate startDate) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // First, end any existing active assignments for this member
            String endExistingSql = "UPDATE Trainer_Member_Assignment SET status = 'ENDED', end_date = ? " +
                    "WHERE member_id = ? AND status = 'ACTIVE'";
            pstmt = conn.prepareStatement(endExistingSql);
            pstmt.setString(1, LocalDate.now().toString());
            pstmt.setInt(2, memberId);
            pstmt.executeUpdate();
            pstmt.close();

            // Insert new assignment
            String sql = "INSERT INTO Trainer_Member_Assignment (trainer_id, member_id, assignment_date, status, assigned_by_admin_id) " +
                    "VALUES (?, ?, ?, 'ACTIVE', ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trainerId);
            pstmt.setInt(2, memberId);
            pstmt.setString(3, startDate.toString());
            pstmt.setInt(4, adminId);

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

            if (success) {
                System.out.println("✅ Trainer " + trainerId + " assigned to member " + memberId);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error assigning trainer: " + e.getMessage());
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
    // Get available trainers (not at max capacity)
    public List<Trainer> getAvailableTrainers() {
        List<Trainer> availableTrainers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM Trainers WHERE account_status = 'ACTIVE' ORDER BY full_name";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Trainer trainer = extractTrainerFromResultSet(rs);

                // Check if trainer has capacity
                int currentClients = getAssignedClientsCount(trainer.getTrainerId());
                if (currentClients < trainer. getMaxClients()) {
                    availableTrainers.add(trainer);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting available trainers: " + e.getMessage());
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

        return availableTrainers;
    }
    public List<ClientDetails> getMyAssignedClients(int trainerId) {
        List<ClientDetails> clients = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection. getInstance().getConnection();

            String sql = "SELECT m.member_id, m.full_name, m.email, m.phone, " +
                    "tma.assignment_date, tma. status, " +  // FIXED
                    "m.membership_type, m.account_status, " +
                    "(SELECT MAX(workout_date) FROM Workouts w WHERE w.member_id = m. member_id) as last_workout " +
                    "FROM Trainer_Member_Assignment tma " +
                    "JOIN Members m ON tma.member_id = m.member_id " +
                    "WHERE tma.trainer_id = ? AND tma. status = 'ACTIVE' " +
                    "ORDER BY tma.assignment_date DESC";  // FIXED

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trainerId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ClientDetails client = new ClientDetails();
                client.setMemberId(rs.getInt("member_id"));
                client.setMemberName(rs.getString("full_name"));
                client.setEmail(rs.getString("email"));
                client.setPhone(rs.getString("phone"));
                client.setGoal("Not Set");

                String assignedDateStr = rs.getString("assignment_date");  // FIXED
                if (assignedDateStr != null && !assignedDateStr.isEmpty()) {
                    try {
                        client. setAssignmentDate(LocalDate.parse(assignedDateStr));
                    } catch (Exception e) {
                        client.setAssignmentDate(null);
                    }
                }

                String lastWorkoutStr = rs.getString("last_workout");
                if (lastWorkoutStr != null && ! lastWorkoutStr.isEmpty()) {
                    try {
                        client.setLastWorkoutDate(LocalDate.parse(lastWorkoutStr));
                    } catch (Exception e) {
                        client.setLastWorkoutDate(null);
                    }
                }

                client.setMembershipType(rs.getString("membership_type"));
                client.setAccountStatus(rs.getString("account_status"));
                client.setProgressPercentage(0.0);

                clients.add(client);
            }

            System.out.println("✅ Loaded " + clients.size() + " clients");

        } catch (SQLException e) {
            System.err.println("❌ Error:  " + e.getMessage());
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

        return clients;
    }

    public Member getClientDetails(int memberId) {
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
                member = new Member();
                member.setMemberId(rs.getInt("member_id"));
                member.setUsername(rs.getString("username"));
                member.setFullName(rs.getString("full_name"));
                member.setEmail(rs.getString("email"));
                member.setPhone(rs. getString("phone"));

                String dobStr = rs.getString("date_of_birth");
                if (dobStr != null && !dobStr.isEmpty()) {
                    try {
                        member.setDateOfBirth(LocalDate.parse(dobStr));
                    } catch (Exception e) {
                        member.setDateOfBirth(null);
                    }
                }

                member.setGender(rs.getString("gender"));
                member.setMembershipType(rs.getString("membership_type"));

                String startStr = rs.getString("membership_start");
                if (startStr != null && !startStr. isEmpty()) {
                    try {
                        member.setMembershipStart(LocalDate.parse(startStr));
                    } catch (Exception e) {
                        member. setMembershipStart(null);
                    }
                }

                String endStr = rs. getString("membership_end");
                if (endStr != null && ! endStr.isEmpty()) {
                    try {
                        member.setMembershipEnd(LocalDate. parse(endStr));
                    } catch (Exception e) {
                        member.setMembershipEnd(null);
                    }
                }

                member.setAccountStatus(rs.getString("account_status"));
            }

        } catch (SQLException e) {
            System.err.println("Error getting client details: " + e. getMessage());
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

        return member;
    }
}