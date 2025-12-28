package com.gym.dao;

import com.gym.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class MemberDashboardDAO {

    public Map<String, Object> getTrainerInfo(int memberId) {
        Map<String, Object> trainerInfo = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT t.trainer_id, t.full_name, t.specializations " +
                    "FROM Trainer_Member_Assignment tma " +
                    "JOIN Trainers t ON tma.trainer_id = t.trainer_id " +
                    "WHERE tma.member_id = ? AND tma.status = 'ACTIVE' " +
                    "ORDER BY tma.assigned_date DESC LIMIT 1";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                trainerInfo.put("id", rs.getInt("trainer_id"));
                trainerInfo.put("name", rs.getString("full_name"));
                trainerInfo.put("specialization", rs.getString("specializations"));
            }

        } catch (Exception e) {
            System.err.println("Error getting trainer info: " + e.getMessage());
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

        return trainerInfo;
    }

    public List<Map<String, Object>> getTodaysGoals(int memberId) {
        List<Map<String, Object>> goals = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String today = LocalDate.now().toString();

            // Get trainer daily goals for today
            String sql = "SELECT workout_duration, calorie_target, water_intake_ml, " +
                    "calorie_limit, protein_target, carbs_target, fats_target " +
                    "FROM Trainer_Daily_Goals " +
                    "WHERE member_id = ? AND goal_date = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, today);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // Workout Duration Goal
                if (rs.getInt("workout_duration") > 0) {
                    Map<String, Object> goal = new HashMap<>();
                    goal.put("goalType", "Workout Duration");
                    goal.put("targetValue", (double) rs.getInt("workout_duration"));
                    goal.put("currentValue", getCurrentWorkoutMinutes(memberId, today));
                    goal.put("unit", "min");
                    goals.add(goal);
                }

                // Calorie Burn Goal
                if (rs.getInt("calorie_target") > 0) {
                    Map<String, Object> goal = new HashMap<>();
                    goal.put("goalType", "Calories Burned");
                    goal.put("targetValue", (double) rs.getInt("calorie_target"));
                    goal.put("currentValue", getCurrentCaloriesBurned(memberId, today));
                    goal.put("unit", "kcal");
                    goals.add(goal);
                }

                // Water Intake Goal
                if (rs.getInt("water_intake_ml") > 0) {
                    Map<String, Object> goal = new HashMap<>();
                    goal.put("goalType", "Water Intake");
                    goal.put("targetValue", (double) rs.getInt("water_intake_ml"));
                    goal.put("currentValue", getCurrentWaterIntake(memberId, today));
                    goal.put("unit", "ml");
                    goals.add(goal);
                }

                // Calorie Intake Goal (Limit)
                if (rs.getInt("calorie_limit") > 0) {
                    Map<String, Object> goal = new HashMap<>();
                    goal.put("goalType", "Calorie Intake");
                    goal.put("targetValue", (double) rs.getInt("calorie_limit"));
                    goal.put("currentValue", getCurrentCalorieIntake(memberId, today));
                    goal.put("unit", "kcal");
                    goals.add(goal);
                }
            }

        } catch (Exception e) {
            System.err.println("Error getting today's goals: " + e.getMessage());
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

        return goals;
    }

    private double getCurrentWorkoutMinutes(int memberId, String date) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT COALESCE(SUM(total_duration), 0) as total FROM Workouts " +
                    "WHERE member_id = ? AND workout_date = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, date);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
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
        return 0.0;
    }

    private double getCurrentCaloriesBurned(int memberId, String date) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT COALESCE(SUM(total_calories), 0) as total FROM Workouts " +
                    "WHERE member_id = ? AND workout_date = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, date);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
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
        return 0.0;
    }

    private double getCurrentWaterIntake(int memberId, String date) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT COALESCE(SUM(amount_ml), 0) as total FROM Water_Logs " +
                    "WHERE member_id = ? AND log_date = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, date);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
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
        return 0.0;
    }

    private double getCurrentCalorieIntake(int memberId, String date) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT COALESCE(SUM(total_calories), 0) as total FROM Meals " +
                    "WHERE member_id = ? AND meal_date = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, date);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
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
        return 0.0;
    }

    public List<Map<String, Object>> getTodaysWorkout(int memberId) {
        List<Map<String, Object>> exercises = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String today = LocalDate.now().toString();

            String sql = "SELECT el.exercise_name, tpe.sets, tpe.reps, tpe.rest_seconds " +
                    "FROM Trainer_Workout_Plans twp " +
                    "JOIN Trainer_Plan_Exercises tpe ON twp.plan_id = tpe.plan_id " +
                    "JOIN Exercises_Library el ON tpe.exercise_id = el.exercise_id " +
                    "WHERE twp.member_id = ? AND twp.plan_date = ? " +
                    "ORDER BY tpe.order_number";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, today);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> exercise = new HashMap<>();
                exercise.put("name", rs.getString("exercise_name"));
                exercise.put("sets", rs.getInt("sets"));
                exercise.put("reps", rs.getString("reps"));
                exercise.put("duration", rs.getInt("rest_seconds") / 60); // Convert to minutes
                exercises.add(exercise);
            }

        } catch (Exception e) {
            System.err.println("Error getting today's workout: " + e.getMessage());
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

        return exercises;
    }

    public List<Map<String, Object>> getTodaysMealPlan(int memberId) {
        List<Map<String, Object>> meals = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String today = LocalDate.now().toString();

            // First, get trainer meal plans
            String trainerSql = "SELECT meal_type, foods, total_calories, 'PLANNED' as source " +
                    "FROM Trainer_Meal_Plans " +
                    "WHERE member_id = ? AND plan_date = ? " +
                    "ORDER BY meal_time";

            pstmt = conn.prepareStatement(trainerSql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, today);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> meal = new HashMap<>();
                meal.put("mealType", rs.getString("meal_type"));
                meal.put("foodItems", rs.getString("foods"));
                meal.put("calories", rs.getInt("total_calories"));
                meal.put("source", "Trainer Plan");
                meals.add(meal);
            }
            rs.close();
            pstmt.close();

            // Then, get logged meals
            String loggedSql = "SELECT m.meal_type, m.total_calories, m.meal_time, " +
                    "GROUP_CONCAT(fd.food_name, ', ') as foods " +
                    "FROM Meals m " +
                    "LEFT JOIN Meal_Items mi ON m.meal_id = mi.meal_id " +
                    "LEFT JOIN Foods_Database fd ON mi.food_id = fd.food_id " +
                    "WHERE m.member_id = ? AND m.meal_date = ? " +
                    "GROUP BY m.meal_id " +
                    "ORDER BY m.meal_time";

            pstmt = conn.prepareStatement(loggedSql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, today);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> meal = new HashMap<>();
                meal.put("mealType", rs.getString("meal_type"));
                meal.put("foodItems", rs.getString("foods") != null ? rs.getString("foods") : "Custom meal");
                meal.put("calories", rs.getInt("total_calories"));
                meal.put("source", "Logged");
                meals.add(meal);
            }

        } catch (Exception e) {
            System.err.println("Error getting today's meal plan: " + e.getMessage());
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

        return meals;
    }

    public Map<String, Object> getQuickStats(int memberId) {
        Map<String, Object> stats = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Get workout streak
            stats.put("streak", calculateWorkoutStreak(memberId));

            // Get total workouts
            String workoutSql = "SELECT COUNT(*) as total FROM Workouts WHERE member_id = ?";
            pstmt = conn.prepareStatement(workoutSql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.put("totalWorkouts", rs.getInt("total"));
            }
            rs.close();
            pstmt.close();

            // Get current weight
            String weightSql = "SELECT current_weight FROM Member_Profiles WHERE member_id = ?";
            pstmt = conn.prepareStatement(weightSql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.put("weight", rs.getDouble("current_weight"));
            }

        } catch (Exception e) {
            System.err.println("Error getting quick stats: " + e.getMessage());
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

    private int calculateWorkoutStreak(int memberId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int streak = 0;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            // Use date() function to extract only date part
            String sql = "SELECT DISTINCT date(workout_date) as workout_date FROM Workouts " +
                    "WHERE member_id = ? ORDER BY date(workout_date) DESC LIMIT 30";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();

            LocalDate lastDate = LocalDate.now();
            boolean foundToday = false;

            while (rs.next()) {
                String dateStr = rs.getString("workout_date");
                LocalDate workoutDate = LocalDate.parse(dateStr);

                if (!foundToday && workoutDate.equals(LocalDate.now())) {
                    foundToday = true;
                    streak = 1;
                    lastDate = workoutDate;
                } else if (foundToday && workoutDate.equals(lastDate.minusDays(1))) {
                    streak++;
                    lastDate = workoutDate;
                } else if (foundToday) {
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Error calculating workout streak: " + e.getMessage());
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

        return streak;
    }

    public List<Map<String, Object>> getRecentActivities(int memberId) {
        List<Map<String, Object>> activities = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT activity_description, timestamp " +
                    "FROM Social_Activities " +
                    "WHERE member_id = ? " +
                    "ORDER BY timestamp DESC LIMIT 5";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("description", rs.getString("activity_description"));
                activity.put("date", rs.getString("timestamp"));
                activities.add(activity);
            }

        } catch (Exception e) {
            System.err.println("Error getting recent activities: " + e.getMessage());
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

        return activities;
    }
}
