package com.gym.dao;

import com.gym.models.Salary;
import com.gym.models. Trainer;
import com.gym. utils.DatabaseConnection;

import java.sql. Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java. sql.SQLException;
import java. time.LocalDate;
import java. time.LocalDateTime;
import java.util. ArrayList;
import java.util. List;

public class SalaryDAO {

    private TrainerDAO trainerDAO;

    public SalaryDAO() {
        this.trainerDAO = new TrainerDAO();
    }

    /**
     * Generate monthly salaries for all active trainers
     */
    public boolean generateMonthlySalaries(int month, int year, int processedByAdminId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Check if salaries already exist for this month/year
            String checkSql = "SELECT COUNT(*) as count FROM Trainer_Salaries WHERE month = ? AND year = ? ";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, month);
            pstmt.setInt(2, year);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("Salaries already generated for " + month + "/" + year);
                rs.close();
                pstmt.close();
                return false;
            }
            rs.close();
            pstmt.close();

            // Get all active trainers
            List<Trainer> trainers = trainerDAO. getAllTrainers();
            List<Trainer> activeTrainers = new ArrayList<>();

            for (Trainer trainer : trainers) {
                if ("ACTIVE".equals(trainer.getAccountStatus())) {
                    activeTrainers.add(trainer);
                }
            }

            if (activeTrainers.isEmpty()) {
                System.out. println("No active trainers found");
                return false;
            }

            // Insert salary records for each active trainer
            String insertSql = "INSERT INTO Trainer_Salaries (trainer_id, month, year, base_salary, " +
                    "bonus, deductions, net_salary, status, processed_by_admin_id, created_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(insertSql);

            int count = 0;
            for (Trainer trainer : activeTrainers) {
                double baseSalary = trainer.getSalary();
                double bonus = 0.0;
                double deductions = 0.0;
                double netSalary = baseSalary + bonus - deductions;

                pstmt.setInt(1, trainer. getTrainerId());
                pstmt. setInt(2, month);
                pstmt.setInt(3, year);
                pstmt.setDouble(4, baseSalary);
                pstmt.setDouble(5, bonus);
                pstmt.setDouble(6, deductions);
                pstmt.setDouble(7, netSalary);
                pstmt.setString(8, "PENDING");
                pstmt.setInt(9, processedByAdminId);
                pstmt.setString(10, LocalDateTime.now().toString());

                pstmt.addBatch();
                count++;
            }

            int[] results = pstmt.executeBatch();
            success = results.length == count;

            if (success) {
                System.out.println("Generated salaries for " + count + " trainers for " + month + "/" + year);
            }

        } catch (SQLException e) {
            System.err.println("Error generating monthly salaries: " + e.getMessage());
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

    /**
     * Get all salaries for a specific month and year
     */
    public List<Salary> getSalariesForMonth(int month, int year) {
        List<Salary> salaries = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT s.*, t.full_name as trainer_name " +
                    "FROM Trainer_Salaries s " +
                    "JOIN Trainers t ON s.trainer_id = t.trainer_id " +
                    "WHERE s.month = ? AND s.year = ?  " +
                    "ORDER BY t.full_name";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, month);
            pstmt. setInt(2, year);
            rs = pstmt.executeQuery();

            while (rs. next()) {
                Salary salary = extractSalaryFromResultSet(rs);
                salaries.add(salary);
            }

            System.out.println("Loaded " + salaries.size() + " salary records for " + month + "/" + year);

        } catch (SQLException e) {
            System.err.println("Error getting salaries for month:  " + e.getMessage());
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

        return salaries;
    }

    /**
     * Get salaries within a date range
     */
    public List<Salary> getSalariesByDateRange(LocalDate start, LocalDate end) {
        List<Salary> salaries = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection. getInstance().getConnection();

            String sql = "SELECT s. *, t.full_name as trainer_name " +
                    "FROM Trainer_Salaries s " +
                    "JOIN Trainers t ON s.trainer_id = t.trainer_id " +
                    "WHERE DATE(s.created_date) BETWEEN ? AND ? " +
                    "ORDER BY s.created_date DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, start.toString());
            pstmt.setString(2, end.toString());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Salary salary = extractSalaryFromResultSet(rs);
                salaries.add(salary);
            }

            System.out.println("Loaded " + salaries.size() + " salary records for date range");

        } catch (SQLException e) {
            System.err.println("Error getting salaries by date range: " + e.getMessage());
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

        return salaries;
    }

    /**
     * Get salaries for a specific trainer within a date range
     */
    public List<Salary> getSalariesByTrainer(int trainerId, LocalDate start, LocalDate end) {
        List<Salary> salaries = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection. getInstance().getConnection();

            String sql = "SELECT s. *, t.full_name as trainer_name " +
                    "FROM Trainer_Salaries s " +
                    "JOIN Trainers t ON s.trainer_id = t.trainer_id " +
                    "WHERE s. trainer_id = ? AND DATE(s.created_date) BETWEEN ? AND ? " +
                    "ORDER BY s.created_date DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trainerId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Salary salary = extractSalaryFromResultSet(rs);
                salaries.add(salary);
            }

            System.out.println("Loaded " + salaries.size() + " salary records for trainer ID:  " + trainerId);

        } catch (SQLException e) {
            System.err.println("Error getting salaries by trainer: " + e.getMessage());
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

        return salaries;
    }

    /**
     * Update salary status (PENDING to PAID)
     */
    public boolean updateSalaryStatus(int salaryId, String status, LocalDate paymentDate) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "UPDATE Trainer_Salaries SET status = ?, payment_date = ?, last_modified = ?  " +
                    "WHERE salary_id = ? ";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setString(2, paymentDate != null ? paymentDate.toString() : null);
            pstmt.setString(3, LocalDateTime.now().toString());
            pstmt. setInt(4, salaryId);

            int rowsAffected = pstmt.executeUpdate();
            success = rowsAffected > 0;

            if (success) {
                System.out.println("Updated salary status to " + status + " for salary ID:  " + salaryId);
            }

        } catch (SQLException e) {
            System.err. println("Error updating salary status: " + e.getMessage());
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

    /**
     * Update salary details (bonus and deductions)
     */
    public boolean updateSalaryDetails(int salaryId, double bonus, double deductions) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Get base salary
            String getSql = "SELECT base_salary FROM Trainer_Salaries WHERE salary_id = ?";
            pstmt = conn.prepareStatement(getSql);
            pstmt.setInt(1, salaryId);
            ResultSet rs = pstmt.executeQuery();

            double baseSalary = 0;
            if (rs.next()) {
                baseSalary = rs.getDouble("base_salary");
            }
            rs.close();
            pstmt.close();

            // Calculate net salary
            double netSalary = baseSalary + bonus - deductions;

            // Update salary
            String updateSql = "UPDATE Trainer_Salaries SET bonus = ?, deductions = ?, net_salary = ?, " +
                    "last_modified = ? WHERE salary_id = ?";

            pstmt = conn.prepareStatement(updateSql);
            pstmt.setDouble(1, bonus);
            pstmt.setDouble(2, deductions);
            pstmt. setDouble(3, netSalary);
            pstmt. setString(4, LocalDateTime. now().toString());
            pstmt.setInt(5, salaryId);

            int rowsAffected = pstmt. executeUpdate();
            success = rowsAffected > 0;

            if (success) {
                System.out.println("Updated salary details for salary ID: " + salaryId);
            }

        } catch (SQLException e) {
            System. err.println("Error updating salary details: " + e.getMessage());
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

    /**
     * Get total pending salaries (all trainers)
     */
    public double getTotalPendingSalaries() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        double total = 0.0;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT SUM(net_salary) as total FROM Trainer_Salaries WHERE status = 'PENDING'";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                total = rs.getDouble("total");
            }

        } catch (SQLException e) {
            System.err.println("Error getting total pending salaries: " + e.getMessage());
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

        return total;
    }

    /**
     * Get total paid salaries for a specific month/year
     */
    public double getTotalPaidSalaries(int month, int year) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        double total = 0.0;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT SUM(net_salary) as total FROM Trainer_Salaries " +
                    "WHERE status = 'PAID' AND month = ? AND year = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, month);
            pstmt. setInt(2, year);
            rs = pstmt.executeQuery();

            if (rs. next()) {
                total = rs.getDouble("total");
            }

        } catch (SQLException e) {
            System.err. println("Error getting total paid salaries: " + e.getMessage());
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

        return total;
    }

    /**
     * Get total salaries paid within a date range
     */
    public double getTotalSalariesPaidInRange(LocalDate start, LocalDate end) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        double total = 0.0;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT SUM(net_salary) as total FROM Trainer_Salaries " +
                    "WHERE status = 'PAID' AND DATE(created_date) BETWEEN ? AND ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, start.toString());
            pstmt.setString(2, end.toString());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                total = rs. getDouble("total");
            }

        } catch (SQLException e) {
            System.err.println("Error getting total salaries in range: " + e.getMessage());
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

        return total;
    }

    /**
     * Get salary by ID
     */
    public Salary getSalaryById(int salaryId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Salary salary = null;

        try {
            conn = DatabaseConnection. getInstance().getConnection();

            String sql = "SELECT s.*, t.full_name as trainer_name " +
                    "FROM Trainer_Salaries s " +
                    "JOIN Trainers t ON s.trainer_id = t.trainer_id " +
                    "WHERE s.salary_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, salaryId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                salary = extractSalaryFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err. println("Error getting salary by ID:  " + e.getMessage());
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

        return salary;
    }

    /**
     * Extract Salary object from ResultSet
     */
    private Salary extractSalaryFromResultSet(ResultSet rs) throws SQLException {
        Salary salary = new Salary();

        salary.setSalaryId(rs.getInt("salary_id"));
        salary.setTrainerId(rs.getInt("trainer_id"));
        salary.setTrainerName(rs.getString("trainer_name"));
        salary.setMonth(rs.getInt("month"));
        salary.setYear(rs.getInt("year"));
        salary.setBaseSalary(rs.getDouble("base_salary"));
        salary.setBonus(rs.getDouble("bonus"));
        salary.setDeductions(rs.getDouble("deductions"));
        salary.setNetSalary(rs.getDouble("net_salary"));
        salary.setStatus(rs.getString("status"));

        String paymentDateStr = rs.getString("payment_date");
        if (paymentDateStr != null && !paymentDateStr.isEmpty()) {
            salary.setPaymentDate(LocalDate.parse(paymentDateStr));
        }

        salary.setProcessedByAdminId(rs. getInt("processed_by_admin_id"));

        String createdDateStr = rs.getString("created_date");
        if (createdDateStr != null && !createdDateStr.isEmpty()) {
            if (createdDateStr.contains("T")) {
                salary. setCreatedDate(LocalDateTime.parse(createdDateStr));
            } else {
                salary.setCreatedDate(LocalDate.parse(createdDateStr).atStartOfDay());
            }
        }

        try {
            String lastModifiedStr = rs.getString("last_modified");
            if (lastModifiedStr != null && !lastModifiedStr.isEmpty()) {
                if (lastModifiedStr.contains("T")) {
                    salary. setLastModified(LocalDateTime. parse(lastModifiedStr));
                } else {
                    salary.setLastModified(LocalDate.parse(lastModifiedStr).atStartOfDay());
                }
            }
        } catch (SQLException e) {
            // Column might not exist
        }

        return salary;
    }
}