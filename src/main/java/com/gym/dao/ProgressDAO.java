package com.gym.dao;

import com.gym.models.BodyMeasurement;
import com. gym.models.ProgressReport;
import com.gym.utils.DatabaseConnection;

import java. sql.*;
import java.time. LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressDAO {

    public Map<String, Object> getClientProgress(int memberId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> progress = new HashMap<>();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // 1. Workout Completion Rate
            String workoutSql = "SELECT COUNT(*) as total_workouts FROM Workouts " +
                    "WHERE member_id = ? AND workout_date BETWEEN ? AND ?";

            PreparedStatement workoutStmt = conn.prepareStatement(workoutSql);
            workoutStmt.setInt(1, memberId);
            workoutStmt.setString(2, startDate.toString());
            workoutStmt.setString(3, endDate.toString());
            ResultSet workoutRs = workoutStmt.executeQuery();

            int totalWorkouts = 0;
            if (workoutRs. next()) {
                totalWorkouts = workoutRs.getInt("total_workouts");
            }
            workoutRs. close();
            workoutStmt.close();

            // Calculate expected workouts (assume 5 workouts per week)
            long daysBetween = endDate.toEpochDay() - startDate.toEpochDay() + 1;
            int expectedWorkouts = (int) (daysBetween / 7.0 * 5);
            if (expectedWorkouts == 0) expectedWorkouts = 1;

            double completionRate = Math.min(100.0, (totalWorkouts * 100.0 / expectedWorkouts));
            progress.put("workoutCompletionRate", completionRate);
            progress.put("totalWorkouts", totalWorkouts);

            // 2. Meals Logged Count
            String mealsSql = "SELECT COUNT(*) as total_meals FROM Meals " +
                    "WHERE member_id = ? AND meal_date BETWEEN ? AND ? ";

            PreparedStatement mealsStmt = conn.prepareStatement(mealsSql);
            mealsStmt.setInt(1, memberId);
            mealsStmt.setString(2, startDate.toString());
            mealsStmt.setString(3, endDate.toString());
            ResultSet mealsRs = mealsStmt.executeQuery();

            int mealsLogged = 0;
            if (mealsRs.next()) {
                mealsLogged = mealsRs.getInt("total_meals");
            }
            mealsRs.close();
            mealsStmt.close();

            progress.put("mealsLoggedCount", mealsLogged);

            // 3. Water Intake Compliance
            String waterSql = "SELECT COUNT(DISTINCT log_date) as days_met " +
                    "FROM Water_Logs " +
                    "WHERE member_id = ? AND log_date BETWEEN ? AND ?  " +
                    "GROUP BY log_date " +
                    "HAVING SUM(amount_ml) >= 2000";

            PreparedStatement waterStmt = conn.prepareStatement(waterSql);
            waterStmt.setInt(1, memberId);
            waterStmt.setString(2, startDate.toString());
            waterStmt.setString(3, endDate.toString());
            ResultSet waterRs = waterStmt.executeQuery();

            int daysMet = 0;
            while (waterRs. next()) {
                daysMet++;
            }
            waterRs.close();
            waterStmt.close();

            double waterCompliance = (daysMet * 100.0 / daysBetween);
            progress.put("waterIntakeCompliance", waterCompliance);
            progress.put("waterDaysMet", daysMet);
            progress.put("totalDays", (int)daysBetween);

            // 4. Weight Change
            List<BodyMeasurement> measurements = getWeightHistory(memberId, startDate, endDate);
            double weightChange = 0.0;

            if (measurements.size() >= 2) {
                double firstWeight = measurements.get(0).getWeight();
                double lastWeight = measurements.get(measurements.size() - 1).getWeight();
                weightChange = lastWeight - firstWeight;
            }

            progress.put("weightChange", weightChange);
            progress.put("measurements", measurements);

            // 5. Goal Achievement Summary
            String goalsSql = "SELECT COUNT(*) as total_goals, " +
                    "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed_goals " +
                    "FROM Goals " +
                    "WHERE member_id = ? AND start_date <= ? AND (end_date >= ? OR end_date IS NULL)";

            PreparedStatement goalsStmt = conn.prepareStatement(goalsSql);
            goalsStmt.setInt(1, memberId);
            goalsStmt.setString(2, endDate.toString());
            goalsStmt.setString(3, startDate.toString());
            ResultSet goalsRs = goalsStmt.executeQuery();

            int totalGoals = 0;
            int completedGoals = 0;
            if (goalsRs.next()) {
                totalGoals = goalsRs.getInt("total_goals");
                completedGoals = goalsRs.getInt("completed_goals");
            }
            goalsRs.close();
            goalsStmt.close();

            String goalSummary = completedGoals + " of " + totalGoals + " goals achieved";
            progress.put("goalAchievementSummary", goalSummary);
            progress.put("totalGoals", totalGoals);
            progress.put("completedGoals", completedGoals);

            System.out.println("✅ Loaded progress for member ID: " + memberId);

        } catch (SQLException e) {
            System.err.println("❌ Error loading client progress: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
        }

        return progress;
    }

    public List<BodyMeasurement> getWeightHistory(int memberId, LocalDate startDate, LocalDate endDate) {
        List<BodyMeasurement> measurements = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection. getInstance().getConnection();
            String sql = "SELECT * FROM Body_Measurements " +
                    "WHERE member_id = ? AND measurement_date BETWEEN ? AND ?  " +
                    "ORDER BY measurement_date ASC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, startDate.toString());
            pstmt.setString(3, endDate.toString());
            rs = pstmt.executeQuery();

            while (rs. next()) {
                BodyMeasurement measurement = new BodyMeasurement();
                measurement.setMeasurementId(rs. getInt("measurement_id"));
                measurement.setMemberId(rs.getInt("member_id"));
                measurement.setMeasurementDate(LocalDate.parse(rs.getString("measurement_date")));
                measurement.setWeight(rs.getDouble("weight"));
                measurement.setChest(rs.getDouble("chest"));
                measurement.setWaist(rs.getDouble("waist"));
                measurement.setHips(rs.getDouble("hips"));
                measurement.setArms(rs.getDouble("arms"));
                measurement.setLegs(rs.getDouble("legs"));
                measurement.setBodyFatPercentage(rs.getDouble("body_fat_percentage"));
                measurements.add(measurement);
            }

            System.out.println("✅ Loaded " + measurements.size() + " measurements");

        } catch (SQLException e) {
            System.err.println("❌ Error loading weight history: " + e.getMessage());
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

        return measurements;
    }

    public boolean saveWeeklyReport(ProgressReport report) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Check if report table exists, if not create it
            String createTableSql = "CREATE TABLE IF NOT EXISTS Member_Progress_Reports (" +
                    "report_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "trainer_id INTEGER NOT NULL, " +
                    "member_id INTEGER NOT NULL, " +
                    "report_date TEXT NOT NULL, " +
                    "start_date TEXT NOT NULL, " +
                    "end_date TEXT NOT NULL, " +
                    "workout_completion_rate REAL, " +
                    "meals_logged_count INTEGER, " +
                    "water_intake_compliance REAL, " +
                    "weight_change REAL, " +
                    "goal_achievement_summary TEXT, " +
                    "trainer_feedback TEXT, " +
                    "created_date TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (trainer_id) REFERENCES Trainers(trainer_id), " +
                    "FOREIGN KEY (member_id) REFERENCES Members(member_id))";

            Statement createStmt = conn.createStatement();
            createStmt.execute(createTableSql);
            createStmt.close();

            String sql = "INSERT INTO Member_Progress_Reports " +
                    "(trainer_id, member_id, report_date, start_date, end_date, " +
                    "workout_completion_rate, meals_logged_count, water_intake_compliance, " +
                    "weight_change, goal_achievement_summary, trainer_feedback, created_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, report.getTrainerId());
            pstmt.setInt(2, report.getMemberId());
            pstmt.setString(3, report.getReportDate().toString());
            pstmt.setString(4, report.getStartDate().toString());
            pstmt.setString(5, report.getEndDate().toString());
            pstmt. setDouble(6, report.getWorkoutCompletionRate());
            pstmt.setInt(7, report.getMealsLoggedCount());
            pstmt.setDouble(8, report. getWaterIntakeCompliance());
            pstmt.setDouble(9, report.getWeightChange());
            pstmt. setString(10, report.getGoalAchievementSummary());
            pstmt. setString(11, report.getTrainerFeedback());

            int result = pstmt.executeUpdate();
            System.out.println("✅ Weekly report saved successfully");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error saving weekly report:  " + e.getMessage());
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
}