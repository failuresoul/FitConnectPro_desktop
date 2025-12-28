package com.gym.dao;

import com.gym.models.BodyMeasurement;
import com.gym.models.ProgressReport;
import com.gym.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class ProgressDAO {

    public List<BodyMeasurement> getWeightProgressData(int memberId, LocalDate start, LocalDate end) {
        List<BodyMeasurement> measurements = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM Body_Measurements WHERE member_id = ? AND measurement_date BETWEEN ? AND ? ORDER BY measurement_date";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                BodyMeasurement measurement = new BodyMeasurement();
                measurement.setMeasurementId(rs.getInt("measurement_id"));
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return measurements;
    }

    public Map<String, Integer> getWorkoutStatistics(int memberId, LocalDate start, LocalDate end) {
        Map<String, Integer> stats = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String totalSql = "SELECT COUNT(*) as total FROM Workouts WHERE member_id = ? AND workout_date BETWEEN ? AND ?";
            pstmt = conn.prepareStatement(totalSql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.put("totalWorkouts", rs.getInt("total"));
            }
            rs.close();
            pstmt.close();

            String caloriesSql = "SELECT COALESCE(SUM(total_calories), 0) as calories FROM Workouts WHERE member_id = ? AND workout_date BETWEEN ? AND ?";
            pstmt = conn.prepareStatement(caloriesSql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.put("totalCalories", rs.getInt("calories"));
            }
            rs.close();
            pstmt.close();

            String timeSql = "SELECT COALESCE(SUM(total_duration), 0) as duration FROM Workouts WHERE member_id = ? AND workout_date BETWEEN ? AND ?";
            pstmt = conn.prepareStatement(timeSql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.put("totalDuration", rs.getInt("duration"));
            }
            rs.close();
            pstmt.close();

            String typeSql = "SELECT workout_type, COUNT(*) as count FROM Workouts WHERE member_id = ? AND workout_date BETWEEN ? AND ? GROUP BY workout_type";
            pstmt = conn.prepareStatement(typeSql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                String type = rs.getString("workout_type");
                if (type != null && !type.isEmpty()) {
                    stats.put("type_" + type, rs.getInt("count"));
                }
            }
            rs.close();
            pstmt.close();

            String daySql = "SELECT strftime('%w', workout_date) as day, COUNT(*) as count FROM Workouts WHERE member_id = ? AND workout_date BETWEEN ? AND ? GROUP BY day";
            pstmt = conn.prepareStatement(daySql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int dayNum = rs.getInt("day");
                stats.put("day_" + dayNum, rs.getInt("count"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return stats;
    }

    public Map<String, Double> getNutritionStatistics(int memberId, LocalDate start, LocalDate end) {
        Map<String, Double> stats = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            long daysInRange = end.toEpochDay() - start.toEpochDay() + 1;

            String avgSql = "SELECT " +
                    "COALESCE(AVG(total_calories), 0) as avg_calories, " +
                    "COALESCE(AVG(total_protein), 0) as avg_protein, " +
                    "COALESCE(AVG(total_carbs), 0) as avg_carbs, " +
                    "COALESCE(AVG(total_fats), 0) as avg_fats, " +
                    "COUNT(DISTINCT meal_date) as logged_days " +
                    "FROM Meals WHERE member_id = ? AND meal_date BETWEEN ? AND ?";

            pstmt = conn.prepareStatement(avgSql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                stats.put("avgCalories", rs.getDouble("avg_calories"));
                stats.put("avgProtein", rs.getDouble("avg_protein"));
                stats.put("avgCarbs", rs.getDouble("avg_carbs"));
                stats.put("avgFats", rs.getDouble("avg_fats"));

                int loggedDays = rs.getInt("logged_days");
                double consistency = daysInRange > 0 ? (loggedDays * 100.0 / daysInRange) : 0;
                stats.put("loggingConsistency", consistency);
            }
            rs.close();
            pstmt.close();

            String totalSql = "SELECT " +
                    "COALESCE(SUM(total_protein), 0) as total_protein, " +
                    "COALESCE(SUM(total_carbs), 0) as total_carbs, " +
                    "COALESCE(SUM(total_fats), 0) as total_fats " +
                    "FROM Meals WHERE member_id = ? AND meal_date BETWEEN ? AND ?";

            pstmt = conn.prepareStatement(totalSql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                stats.put("totalProtein", rs.getDouble("total_protein"));
                stats.put("totalCarbs", rs.getDouble("total_carbs"));
                stats.put("totalFats", rs.getDouble("total_fats"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return stats;
    }

    public Map<String, Integer> getGoalAchievementRates(int memberId) {
        Map<String, Integer> rates = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
            String dailySql = "SELECT COUNT(*) as total, " +
                    "SUM(CASE WHEN current_value >= target_value THEN 1 ELSE 0 END) as achieved " +
                    "FROM Goals WHERE member_id = ? AND start_date >= ? AND goal_category = 'DAILY'";

            pstmt = conn.prepareStatement(dailySql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, thirtyDaysAgo.toString());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total");
                int achieved = rs.getInt("achieved");
                rates.put("dailyTotal", total);
                rates.put("dailyAchieved", achieved);
                rates.put("dailyRate", total > 0 ? (achieved * 100 / total) : 0);
            }
            rs.close();
            pstmt.close();

            LocalDate twelveWeeksAgo = LocalDate.now().minusWeeks(12);
            String weeklySql = "SELECT COUNT(*) as total, " +
                    "SUM(CASE WHEN current_value >= target_value THEN 1 ELSE 0 END) as achieved " +
                    "FROM Goals WHERE member_id = ? AND start_date >= ? AND goal_category = 'WEEKLY'";

            pstmt = conn.prepareStatement(weeklySql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, twelveWeeksAgo.toString());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total");
                int achieved = rs.getInt("achieved");
                rates.put("weeklyTotal", total);
                rates.put("weeklyAchieved", achieved);
                rates.put("weeklyRate", total > 0 ? (achieved * 100 / total) : 0);
            }
            rs.close();
            pstmt.close();

            LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
            String monthlySql = "SELECT COUNT(*) as total, " +
                    "SUM(CASE WHEN current_value >= target_value THEN 1 ELSE 0 END) as achieved " +
                    "FROM Goals WHERE member_id = ? AND start_date >= ? AND goal_category = 'MONTHLY'";

            pstmt = conn.prepareStatement(monthlySql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, sixMonthsAgo.toString());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total");
                int achieved = rs.getInt("achieved");
                rates.put("monthlyTotal", total);
                rates.put("monthlyAchieved", achieved);
                rates.put("monthlyRate", total > 0 ? (achieved * 100 / total) : 0);
            }
            rs.close();
            pstmt.close();

            String overallSql = "SELECT COUNT(*) as total, " +
                    "SUM(CASE WHEN status = 'ACHIEVED' THEN 1 ELSE 0 END) as achieved " +
                    "FROM Goals WHERE member_id = ?";

            pstmt = conn.prepareStatement(overallSql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total");
                int achieved = rs.getInt("achieved");
                rates.put("overallTotal", total);
                rates.put("overallAchieved", achieved);
                rates.put("overallRate", total > 0 ? (achieved * 100 / total) : 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return rates;
    }

    public List<Map<String, Object>> getAchievementTimeline(int memberId) {
        List<Map<String, Object>> timeline = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT goal_type, description, achievement_date, target_value, current_value " +
                    "FROM Goals WHERE member_id = ? AND status = 'ACHIEVED' " +
                    "ORDER BY achievement_date DESC LIMIT 20";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> achievement = new HashMap<>();
                achievement.put("goalType", rs.getString("goal_type"));
                achievement.put("description", rs.getString("description"));
                String achievementDateStr = rs.getString("achievement_date");
                if (achievementDateStr != null && !achievementDateStr.isEmpty()) {
                    achievement.put("achievementDate", LocalDate.parse(achievementDateStr));
                } else {
                    achievement.put("achievementDate", LocalDate.now());
                }
                achievement.put("targetValue", rs.getDouble("target_value"));
                achievement.put("currentValue", rs.getDouble("current_value"));
                timeline.add(achievement);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return timeline;
    }

    public Double getTargetWeight(int memberId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT target_weight FROM Member_Profiles WHERE member_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                double targetWeight = rs.getDouble("target_weight");
                return targetWeight > 0 ? targetWeight : null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // New method for trainer to view client progress
    public Map<String, Object> getClientProgress(int memberId, LocalDate start, LocalDate end) {
        Map<String, Object> progress = new HashMap<>();

        // Get workout statistics
        Map<String, Integer> workoutStats = getWorkoutStatistics(memberId, start, end);
        progress.put("workoutStats", workoutStats);

        // Get nutrition statistics
        Map<String, Double> nutritionStats = getNutritionStatistics(memberId, start, end);
        progress.put("nutritionStats", nutritionStats);

        // Get goal achievement rates
        Map<String, Integer> goalRates = getGoalAchievementRates(memberId);
        progress.put("goalRates", goalRates);

        // Calculate workout completion rate
        long totalDays = end.toEpochDay() - start.toEpochDay() + 1;
        int totalWorkouts = workoutStats.getOrDefault("totalWorkouts", 0);
        double workoutRate = totalDays > 0 ? (totalWorkouts * 100.0 / totalDays) : 0;
        progress.put("workoutCompletionRate", workoutRate);
        progress.put("totalWorkouts", totalWorkouts);
        progress.put("totalDays", (int) totalDays);

        // Calculate meals logged
        int mealsLogged = getMealsLoggedCount(memberId, start, end);
        progress.put("mealsLoggedCount", mealsLogged);

        // Calculate water intake compliance
        Map<String, Object> waterStats = getWaterIntakeCompliance(memberId, start, end);
        progress.put("waterIntakeCompliance", waterStats.getOrDefault("complianceRate", 0.0));
        progress.put("waterDaysMet", waterStats.getOrDefault("daysMet", 0));

        // Calculate weight change
        List<BodyMeasurement> measurements = getWeightProgressData(memberId, start, end);
        if (!measurements.isEmpty()) {
            double startWeight = measurements.get(0).getWeight();
            double currentWeight = measurements.get(measurements.size() - 1).getWeight();
            double weightChange = currentWeight - startWeight;
            progress.put("weightChange", weightChange);
        } else {
            progress.put("weightChange", 0.0);
        }

        // Goal achievement summary
        String goalSummary = generateGoalSummary(goalRates);
        progress.put("goalAchievementSummary", goalSummary);

        return progress;
    }

    // Helper method to get meals logged count
    private int getMealsLoggedCount(int memberId, LocalDate start, LocalDate end) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT COUNT(*) as count FROM Meals WHERE member_id = ? AND meal_date BETWEEN ? AND ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    // Helper method to get water intake compliance
    private Map<String, Object> getWaterIntakeCompliance(int memberId, LocalDate start, LocalDate end) {
        Map<String, Object> waterStats = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            long totalDays = end.toEpochDay() - start.toEpochDay() + 1;

            // Count days where water intake goal was met (assuming 2000ml as default goal)
            String sql = "SELECT COUNT(DISTINCT water_date) as days_met " +
                    "FROM Water_Intake WHERE member_id = ? AND water_date BETWEEN ? AND ? " +
                    "GROUP BY water_date HAVING SUM(amount_ml) >= 2000";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            rs = pstmt.executeQuery();

            int daysMet = 0;
            if (rs.next()) {
                daysMet = rs.getInt("days_met");
            }

            double complianceRate = totalDays > 0 ? (daysMet * 100.0 / totalDays) : 0;

            waterStats.put("daysMet", daysMet);
            waterStats.put("totalDays", (int) totalDays);
            waterStats.put("complianceRate", complianceRate);

        } catch (Exception e) {
            e.printStackTrace();
            waterStats.put("daysMet", 0);
            waterStats.put("totalDays", 0);
            waterStats.put("complianceRate", 0.0);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return waterStats;
    }

    // Helper method to generate goal achievement summary
    private String generateGoalSummary(Map<String, Integer> goalRates) {
        int overallRate = goalRates.getOrDefault("overallRate", 0);
        int overallTotal = goalRates.getOrDefault("overallTotal", 0);
        int overallAchieved = goalRates.getOrDefault("overallAchieved", 0);

        if (overallTotal == 0) {
            return "No goals set yet";
        }

        return String.format("Achieved %d of %d goals (%d%%)",
                overallAchieved, overallTotal, overallRate);
    }

    // Save weekly progress report
    public boolean saveWeeklyReport(ProgressReport report) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "INSERT INTO Progress_Reports " +
                    "(trainer_id, member_id, report_date, start_date, end_date, " +
                    "workout_completion_rate, meals_logged_count, water_intake_compliance, " +
                    "weight_change, goal_achievement_summary, trainer_feedback) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, report.getTrainerId());
            pstmt.setInt(2, report.getMemberId());
            pstmt.setString(3, report.getReportDate().toString());
            pstmt.setString(4, report.getStartDate().toString());
            pstmt.setString(5, report.getEndDate().toString());
            pstmt.setDouble(6, report.getWorkoutCompletionRate());
            pstmt.setInt(7, report.getMealsLoggedCount());
            pstmt.setDouble(8, report.getWaterIntakeCompliance());
            pstmt.setDouble(9, report.getWeightChange());
            pstmt.setString(10, report.getGoalAchievementSummary());
            pstmt.setString(11, report.getTrainerFeedback());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Alternative method that returns a list of progress data points
    public List<Map<String, Object>> getClientProgressList(int memberId, LocalDate start, LocalDate end) {
        List<Map<String, Object>> progressList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Combine data from multiple tables
            String sql = "SELECT " +
                    "bm.measurement_date as date, " +
                    "bm.weight, " +
                    "bm.body_fat_percentage, " +
                    "(SELECT COUNT(*) FROM Workouts w WHERE w.member_id = ? AND w.workout_date = bm.measurement_date) as workouts, " +
                    "(SELECT COALESCE(SUM(w.total_calories), 0) FROM Workouts w WHERE w.member_id = ? AND w.workout_date = bm.measurement_date) as calories_burned, " +
                    "(SELECT COALESCE(AVG(m.total_calories), 0) FROM Meals m WHERE m.member_id = ? AND m.meal_date = bm.measurement_date) as calories_consumed " +
                    "FROM Body_Measurements bm " +
                    "WHERE bm.member_id = ? AND bm.measurement_date BETWEEN ? AND ? " +
                    "ORDER BY bm.measurement_date";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, memberId);
            pstmt.setInt(3, memberId);
            pstmt.setInt(4, memberId);
            pstmt.setString(5, start.toString());
            pstmt.setString(6, end.toString());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> dataPoint = new HashMap<>();
                dataPoint.put("date", LocalDate.parse(rs.getString("date")));
                dataPoint.put("weight", rs.getDouble("weight"));
                dataPoint.put("bodyFatPercentage", rs.getDouble("body_fat_percentage"));
                dataPoint.put("workouts", rs.getInt("workouts"));
                dataPoint.put("caloriesBurned", rs.getInt("calories_burned"));
                dataPoint.put("caloriesConsumed", rs.getDouble("calories_consumed"));
                progressList.add(dataPoint);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return progressList;
    }

    // Get client's recent achievements
    public List<Map<String, Object>> getClientAchievements(int memberId, int limit) {
        List<Map<String, Object>> achievements = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT goal_type, description, achievement_date, target_value, current_value " +
                    "FROM Goals WHERE member_id = ? AND status = 'ACHIEVED' " +
                    "ORDER BY achievement_date DESC LIMIT ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> achievement = new HashMap<>();
                achievement.put("goalType", rs.getString("goal_type"));
                achievement.put("description", rs.getString("description"));
                String achievementDateStr = rs.getString("achievement_date");
                if (achievementDateStr != null && !achievementDateStr.isEmpty()) {
                    achievement.put("achievementDate", LocalDate.parse(achievementDateStr));
                } else {
                    achievement.put("achievementDate", LocalDate.now());
                }
                achievement.put("targetValue", rs.getDouble("target_value"));
                achievement.put("currentValue", rs.getDouble("current_value"));
                achievements.add(achievement);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return achievements;
    }

    // Get client's workout consistency over time
    public Map<String, Integer> getClientWorkoutConsistency(int memberId, LocalDate start, LocalDate end) {
        Map<String, Integer> consistency = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Calculate total possible workout days
            long totalDays = end.toEpochDay() - start.toEpochDay() + 1;

            // Get actual workout days
            String sql = "SELECT COUNT(DISTINCT workout_date) as workout_days " +
                    "FROM Workouts WHERE member_id = ? AND workout_date BETWEEN ? AND ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int workoutDays = rs.getInt("workout_days");
                consistency.put("totalDays", (int) totalDays);
                consistency.put("workoutDays", workoutDays);
                consistency.put("consistencyRate", totalDays > 0 ? (workoutDays * 100 / (int) totalDays) : 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return consistency;
    }
}

