package com.gym.dao;

import com.gym.models.Exercise;
import com.gym.models.PlanExercise;
import com.gym.models.WorkoutPlan;
import com.gym.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java. util.ArrayList;
import java.util.List;

public class WorkoutPlanDAO {

    public boolean createWorkoutPlan(WorkoutPlan plan, List<PlanExercise> exercises) {
        Connection conn = null;
        PreparedStatement planStmt = null;
        PreparedStatement exerciseStmt = null;
        PreparedStatement lastIdStmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Insert workout plan
            String planSql = "INSERT INTO Trainer_Workout_Plans " +
                    "(trainer_id, member_id, plan_date, focus_area, total_duration, " +
                    "expected_calories, instructions, created_date, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now'), 'ASSIGNED')";

            planStmt = conn.prepareStatement(planSql);
            planStmt.setInt(1, plan.getTrainerId());
            planStmt. setInt(2, plan.getMemberId());
            planStmt.setString(3, plan. getPlanDate().toString());
            planStmt.setString(4, plan.getFocusArea());
            planStmt.setInt(5, plan.getTotalDuration());
            planStmt.setInt(6, plan.getExpectedCalories());
            planStmt.setString(7, plan.getInstructions());

            int affectedRows = planStmt.executeUpdate();

            if (affectedRows == 0) {
                conn. rollback();
                return false;
            }

            // FIXED: Get generated plan ID using SQLite's last_insert_rowid()
            lastIdStmt = conn.prepareStatement("SELECT last_insert_rowid()");
            rs = lastIdStmt.executeQuery();

            int planId = 0;
            if (rs.next()) {
                planId = rs.getInt(1);
            } else {
                conn.rollback();
                return false;
            }

            // Insert exercises
            String exerciseSql = "INSERT INTO Trainer_Plan_Exercises " +
                    "(plan_id, exercise_id, sets, reps, weight, rest_seconds, " +
                    "trainer_notes, order_number) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            exerciseStmt = conn.prepareStatement(exerciseSql);

            for (int i = 0; i < exercises.size(); i++) {
                PlanExercise exercise = exercises.get(i);
                exerciseStmt.setInt(1, planId);
                exerciseStmt.setInt(2, exercise.getExerciseId());
                exerciseStmt.setInt(3, exercise.getSets());
                exerciseStmt. setString(4, exercise.getReps());
                exerciseStmt.setDouble(5, exercise.getWeight());
                exerciseStmt.setInt(6, exercise.getRestSeconds());
                exerciseStmt.setString(7, exercise. getTrainerNotes());
                exerciseStmt.setInt(8, i + 1); // Order number
                exerciseStmt.addBatch();
            }

            exerciseStmt.executeBatch();

            conn.commit(); // Commit transaction
            System.out.println("✅ Workout plan created successfully with ID: " + planId);
            return true;

        } catch (SQLException e) {
            System. err.println("❌ Error creating workout plan: " + e. getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (lastIdStmt != null) lastIdStmt.close();
                if (planStmt != null) planStmt.close();
                if (exerciseStmt != null) exerciseStmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    DatabaseConnection.getInstance().releaseConnection(conn);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Exercise> getAllExercises() {
        List<Exercise> exercises = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM Exercises_Library ORDER BY exercise_name";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Exercise exercise = new Exercise();
                exercise. setExerciseId(rs. getInt("exercise_id"));
                exercise.setExerciseName(rs.getString("exercise_name"));
                exercise.setCategory(rs.getString("category"));
                exercise.setMuscleGroup(rs.getString("muscle_group"));
                exercise.setEquipmentNeeded(rs.getString("equipment_needed"));
                exercise.setDifficultyLevel(rs.getString("difficulty_level"));
                exercise.setInstructions(rs.getString("instructions"));
                exercise.setVideoLink(rs.getString("video_link"));
                exercises.add(exercise);
            }

            System.out.println("✅ Loaded " + exercises.size() + " exercises from library");

        } catch (SQLException e) {
            System.err.println("❌ Error loading exercises: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) DatabaseConnection.getInstance().releaseConnection(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return exercises;
    }

    public List<WorkoutPlan> getTrainerPlans(int trainerId) {
        List<WorkoutPlan> plans = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection. getInstance().getConnection();
            String sql = "SELECT * FROM Trainer_Workout_Plans WHERE trainer_id = ? ORDER BY plan_date DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, trainerId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                WorkoutPlan plan = new WorkoutPlan();
                plan.setPlanId(rs.getInt("plan_id"));
                plan. setTrainerId(rs.getInt("trainer_id"));
                plan.setMemberId(rs.getInt("member_id"));

                String planDateStr = rs.getString("plan_date");
                if (planDateStr != null && !planDateStr.isEmpty()) {
                    plan.setPlanDate(LocalDate. parse(planDateStr));
                }

                plan.setFocusArea(rs.getString("focus_area"));
                plan.setTotalDuration(rs.getInt("total_duration"));
                plan.setExpectedCalories(rs.getInt("expected_calories"));
                plan.setInstructions(rs.getString("instructions"));
                plan.setStatus(rs.getString("status"));

                String createdDateStr = rs.getString("created_date");
                if (createdDateStr != null && ! createdDateStr.isEmpty()) {
                    plan.setCreatedDate(LocalDate.parse(createdDateStr. substring(0, 10)));
                }

                plans.add(plan);
            }

            System.out.println("✅ Loaded " + plans.size() + " workout plans for trainer ID: " + trainerId);

        } catch (SQLException e) {
            System.err.println("❌ Error loading trainer plans: " + e.getMessage());
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

        return plans;
    }
}