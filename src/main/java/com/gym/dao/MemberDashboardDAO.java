package com.gym.dao;

import com.gym.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberDashboardDAO {

    // Get trainer info for member
    public Map<String, Object> getTrainerInfo(int memberId) {
        Map<String, Object> trainerInfo = new HashMap<>();
        String query = "SELECT t.trainer_id, t.full_name, t.email, t.specializations " +
                "FROM trainers t " +
                "INNER JOIN Trainer_Member_Assignment tma ON t.trainer_id = tma.trainer_id " +
                "WHERE tma.member_id = ? AND tma.status = 'ACTIVE' " +
                "ORDER BY tma.assigned_date DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                trainerInfo.put("id", rs.getInt("trainer_id"));
                trainerInfo.put("name", rs.getString("full_name"));
                trainerInfo.put("email", rs.getString("email"));
                trainerInfo.put("specialization", rs.getString("specializations"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading trainer info: " + e.getMessage());
        }
        return trainerInfo;
    }

    // Get today's goals
    public List<Map<String, Object>> getTodaysGoals(int memberId) {
        List<Map<String, Object>> goals = new ArrayList<>();
        String query = "SELECT workout_duration, calorie_target, water_intake_ml, " +
                "calorie_limit, protein_target, carbs_target, fats_target " +
                "FROM Trainer_Daily_Goals " +
                "WHERE member_id = ? AND goal_date = DATE('now')";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Water intake goal
                if (rs.getInt("water_intake_ml") > 0) {
                    Map<String, Object> waterGoal = new HashMap<>();
                    waterGoal.put("goalType", "Water Intake");
                    waterGoal.put("targetValue", rs.getDouble("water_intake_ml") / 1000.0); // Convert ml to L
                    waterGoal.put("currentValue", 0.0);
                    waterGoal.put("unit", "L");
                    goals.add(waterGoal);
                }

                // Calorie target goal
                if (rs.getInt("calorie_target") > 0) {
                    Map<String, Object> calorieGoal = new HashMap<>();
                    calorieGoal.put("goalType", "Calorie Target");
                    calorieGoal.put("targetValue", (double) rs.getInt("calorie_target"));
                    calorieGoal.put("currentValue", 0.0);
                    calorieGoal.put("unit", "kcal");
                    goals.add(calorieGoal);
                }

                // Protein target
                if (rs.getInt("protein_target") > 0) {
                    Map<String, Object> proteinGoal = new HashMap<>();
                    proteinGoal.put("goalType", "Protein");
                    proteinGoal.put("targetValue", (double) rs.getInt("protein_target"));
                    proteinGoal.put("currentValue", 0.0);
                    proteinGoal.put("unit", "g");
                    goals.add(proteinGoal);
                }

                // Workout duration
                if (rs.getInt("workout_duration") > 0) {
                    Map<String, Object> workoutGoal = new HashMap<>();
                    workoutGoal.put("goalType", "Workout Duration");
                    workoutGoal.put("targetValue", (double) rs.getInt("workout_duration"));
                    workoutGoal.put("currentValue", 0.0);
                    workoutGoal.put("unit", "min");
                    goals.add(workoutGoal);
                }
            }

            System.out.println("✅ Found " + goals.size() + " goals for today");
        } catch (SQLException e) {
            System.err.println("Error loading today's goals: " + e.getMessage());
            e.printStackTrace();
        }
        return goals;
    }

    // Get today's workout plan
    public List<Map<String, Object>> getTodaysWorkout(int memberId) {
        List<Map<String, Object>> exercises = new ArrayList<>();
        String query = "SELECT el.exercise_name, tpe.sets, tpe.reps, tpe.weight, tpe.trainer_notes " +
                "FROM Trainer_Workout_Plans twp " +
                "INNER JOIN Trainer_Plan_Exercises tpe ON twp.plan_id = tpe.plan_id " +
                "INNER JOIN Exercises_Library el ON tpe.exercise_id = el.exercise_id " +
                "WHERE twp.member_id = ? AND twp.plan_date = DATE('now') " +
                "ORDER BY tpe.order_number";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> exercise = new HashMap<>();
                exercise.put("name", rs.getString("exercise_name") != null ? rs.getString("exercise_name") : "");
                exercise.put("sets", rs.getInt("sets"));
                exercise.put("reps", rs.getString("reps")); // Changed from getInt to getString
                exercise.put("weight", rs.getDouble("weight"));
                exercise.put("notes", rs.getString("trainer_notes") != null ? rs.getString("trainer_notes") : "");
                exercise.put("duration", 0); // Add default duration
                exercises.add(exercise);
            }

            rs.close();
            System.out.println("✅ Dashboard loaded " + exercises.size() + " exercises for today");
        } catch (SQLException e) {
            System.err.println("Error loading today's workout: " + e.getMessage());
            e.printStackTrace();
        }
        return exercises;
    }

    // Get today's meal plan
    public List<Map<String, Object>> getTodaysMealPlan(int memberId) {
        List<Map<String, Object>> meals = new ArrayList<>();
        String query = "SELECT meal_type, foods, total_calories, total_protein, total_carbs, total_fats " +
                "FROM Trainer_Meal_Plans " +
                "WHERE member_id = ? AND plan_date = DATE('now') " +
                "ORDER BY CASE meal_type " +
                "WHEN 'Breakfast' THEN 1 " +
                "WHEN 'Lunch' THEN 2 " +
                "WHEN 'Dinner' THEN 3 " +
                "WHEN 'Snack' THEN 4 END";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> meal = new HashMap<>();
                meal.put("mealType", rs.getString("meal_type") != null ? rs.getString("meal_type") : "");
                meal.put("foodItems", rs.getString("foods") != null ? rs.getString("foods") : "");
                meal.put("calories", rs.getInt("total_calories"));
                meal.put("protein", rs.getDouble("total_protein"));
                meal.put("carbs", rs.getDouble("total_carbs"));
                meal.put("fats", rs.getDouble("total_fats"));
                meals.add(meal);
            }

            rs.close();
            System.out.println("✅ Found " + meals.size() + " meals for today");
        } catch (SQLException e) {
            System.err.println("Error loading today's meal plan: " + e.getMessage());
            e.printStackTrace();
        }
        return meals;
    }

    // Get quick stats
    public Map<String, Object> getQuickStats(int memberId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("streak", 0);
        stats.put("totalWorkouts", 0);
        stats.put("weight", 0.0);

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // Get total workouts
            String workoutQuery = "SELECT COUNT(*) as total FROM Workouts WHERE member_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(workoutQuery)) {
                stmt.setInt(1, memberId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.put("totalWorkouts", rs.getInt("total"));
                    }
                }
            }

            // Get current weight from Body_Measurements
            String weightQuery = "SELECT weight FROM Body_Measurements " +
                    "WHERE member_id = ? ORDER BY measurement_date DESC LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(weightQuery)) {
                stmt.setInt(1, memberId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.put("weight", rs.getDouble("weight"));
                    }
                }
            }

            // Calculate streak
            String streakQuery = "SELECT JULIANDAY('now') - JULIANDAY(MAX(workout_date)) as days_since_last " +
                    "FROM Workouts WHERE member_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(streakQuery)) {
                stmt.setInt(1, memberId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int daysSince = rs.getInt("days_since_last");
                        stats.put("streak", daysSince <= 1 ? 1 : 0);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error loading quick stats: " + e.getMessage());
        }

        return stats;
    }

    // Get recent activities
    public List<Map<String, Object>> getRecentActivities(int memberId) {
        List<Map<String, Object>> activities = new ArrayList<>();
        String query = "SELECT activity_type, activity_description, timestamp " +
                "FROM Social_Activities " +
                "WHERE member_id = ? " +
                "ORDER BY timestamp DESC LIMIT 10";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, memberId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("type", rs.getString("activity_type") != null ? rs.getString("activity_type") : "");
                activity.put("description",
                        rs.getString("activity_description") != null ? rs.getString("activity_description") : "");
                activity.put("date", rs.getTimestamp("timestamp"));
                activities.add(activity);
            }
        } catch (SQLException e) {
            System.err.println("Error loading recent activities: " + e.getMessage());
        }
        return activities;
    }

}
