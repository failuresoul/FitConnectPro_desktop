package com.gym.dao;

import com.gym.models.Payment;
import com.gym.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public PaymentDAO() {
        createPaymentTable();
    }

    public void createPaymentTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS Payments (
                payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                user_type TEXT NOT NULL CHECK(user_type IN ('MEMBER', 'TRAINER')),
                payment_type TEXT NOT NULL CHECK(payment_type IN ('MEMBERSHIP', 'SALARY')),
                amount REAL NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING' CHECK(status IN ('PENDING', 'PAID', 'OVERDUE')),
                due_date TEXT NOT NULL,
                paid_date TEXT,
                payment_method TEXT,
                description TEXT,
                created_at TEXT DEFAULT (datetime('now'))
            )
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Payments table created/verified");
        } catch (SQLException e) {
            System.err.println("❌ Error creating Payments table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean addPayment(Payment payment) {
        String sql = """
            INSERT INTO Payments (user_id, user_type, payment_type, amount, status, due_date, description)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, payment.getUserId());
            pstmt.setString(2, payment.getUserType());
            pstmt.setString(3, payment.getPaymentType());
            pstmt.setDouble(4, payment.getAmount());
            pstmt.setString(5, payment.getStatus());
            pstmt.setString(6, payment.getDueDate().toString());
            pstmt.setString(7, payment.getDescription());

            int result = pstmt.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error adding payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Payment> getPendingPayments(int userId, String userType) {
        List<Payment> payments = new ArrayList<>();
        String sql = """
            SELECT * FROM Payments 
            WHERE user_id = ? AND user_type = ? AND status IN ('PENDING', 'OVERDUE')
            ORDER BY due_date ASC
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, userType);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                payments.add(extractPaymentFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting pending payments: " + e.getMessage());
            e.printStackTrace();
        }

        return payments;
    }

    public List<Payment> getPaidPayments(int userId, String userType) {
        List<Payment> payments = new ArrayList<>();
        String sql = """
            SELECT * FROM Payments 
            WHERE user_id = ? AND user_type = ? AND status = 'PAID'
            ORDER BY paid_date DESC
            LIMIT 20
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, userType);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                payments.add(extractPaymentFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting paid payments: " + e.getMessage());
            e.printStackTrace();
        }

        return payments;
    }

    public List<Payment> getAllPendingPayments() {
        List<Payment> payments = new ArrayList<>();
        String sql = """
            SELECT * FROM Payments 
            WHERE status IN ('PENDING', 'OVERDUE')
            ORDER BY due_date ASC
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                payments.add(extractPaymentFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting all pending payments: " + e.getMessage());
            e.printStackTrace();
        }

        return payments;
    }

    public boolean markPaymentAsPaid(int paymentId, String paymentMethod) {
        String sql = """
            UPDATE Payments 
            SET status = 'PAID', paid_date = datetime('now'), payment_method = ?
            WHERE payment_id = ?
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, paymentMethod);
            pstmt.setInt(2, paymentId);

            int result = pstmt.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error marking payment as paid: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void updateOverduePayments() {
        String sql = """
            UPDATE Payments 
            SET status = 'OVERDUE'
            WHERE status = 'PENDING' AND datetime(due_date) < datetime('now')
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            int updated = stmt.executeUpdate(sql);
            if (updated > 0) {
                System.out.println("✅ Updated " + updated + " overdue payments");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error updating overdue payments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void generateMonthlyPayments() {
        // Generate membership fees for members - removed status check since column doesn't exist
        String memberSql = """
            INSERT INTO Payments (user_id, user_type, payment_type, amount, status, due_date, description)
            SELECT 
                member_id,
                'MEMBER',
                'MEMBERSHIP',
                500.00,
                'PENDING',
                date('now', '+1 month'),
                'Monthly membership fee'
            FROM Members
            WHERE NOT EXISTS (
                SELECT 1 FROM Payments p 
                WHERE p.user_id = Members.member_id 
                AND p.user_type = 'MEMBER'
                AND p.payment_type = 'MEMBERSHIP'
                AND date(p.due_date) = date('now', '+1 month')
            )
        """;

        // Generate salaries for trainers - removed status check since column doesn't exist
        String trainerSql = """
            INSERT INTO Payments (user_id, user_type, payment_type, amount, status, due_date, description)
            SELECT 
                trainer_id,
                'TRAINER',
                'SALARY',
                3000.00,
                'PENDING',
                date('now', '+1 month'),
                'Monthly salary payment'
            FROM Trainers
            WHERE NOT EXISTS (
                SELECT 1 FROM Payments p 
                WHERE p.user_id = Trainers.trainer_id 
                AND p.user_type = 'TRAINER'
                AND p.payment_type = 'SALARY'
                AND date(p.due_date) = date('now', '+1 month')
            )
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            int membersAdded = stmt.executeUpdate(memberSql);
            int trainersAdded = stmt.executeUpdate(trainerSql);

            System.out.println("✅ Generated " + membersAdded + " membership payments");
            System.out.println("✅ Generated " + trainersAdded + " salary payments");

        } catch (SQLException e) {
            System.err.println("❌ Error generating monthly payments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getUnpaidCount(int userId, String userType) {
        String sql = """
            SELECT COUNT(*) as count FROM Payments 
            WHERE user_id = ? AND user_type = ? AND status IN ('PENDING', 'OVERDUE')
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, userType);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting unpaid count: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    private Payment extractPaymentFromResultSet(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setUserId(rs.getInt("user_id"));
        payment.setUserType(rs.getString("user_type"));
        payment.setPaymentType(rs.getString("payment_type"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setStatus(rs.getString("status"));

        String dueDateStr = rs.getString("due_date");
        if (dueDateStr != null && !dueDateStr.isEmpty()) {
            try {
                // Handle both date and datetime formats
                if (dueDateStr.contains("T")) {
                    payment.setDueDate(LocalDateTime.parse(dueDateStr));
                } else if (dueDateStr.contains(" ")) {
                    payment.setDueDate(LocalDateTime.parse(dueDateStr.replace(" ", "T")));
                } else {
                    // Just a date, add time
                    payment.setDueDate(LocalDateTime.parse(dueDateStr + "T00:00:00"));
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error parsing due_date: " + dueDateStr);
            }
        }

        String paidDateStr = rs.getString("paid_date");
        if (paidDateStr != null && !paidDateStr.isEmpty()) {
            try {
                if (paidDateStr.contains("T")) {
                    payment.setPaidDate(LocalDateTime.parse(paidDateStr));
                } else if (paidDateStr.contains(" ")) {
                    payment.setPaidDate(LocalDateTime.parse(paidDateStr.replace(" ", "T")));
                } else {
                    payment.setPaidDate(LocalDateTime.parse(paidDateStr + "T00:00:00"));
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error parsing paid_date: " + paidDateStr);
            }
        }

        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setDescription(rs.getString("description"));

        return payment;
    }
}
