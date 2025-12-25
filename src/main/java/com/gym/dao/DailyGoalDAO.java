package com.gym.dao;

import com.gym.models.TrainerDailyGoal;
import com.gym.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;

public class DailyGoalDAO {

    public boolean setDailyGoals(TrainerDailyGoal goal) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            // Check if goal already exists for this date
            String checkSql = "SELECT trainer_goal_id FROM Trainer_Daily_Goals " +
                    "WHERE member_id = ? AND goal_date = ?";

            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, goal.getMemberId());
            pstmt.setString(2, goal.getGoalDate().toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs. next()) {
                // Update existing goal
                int existingGoalId = rs.getInt("trainer_goal_id");
                rs.close();
                pstmt.close();

                String updateSql = "UPDATE Trainer_Daily_Goals SET " +
                        "workout_duration = ?, calorie_target = ?, water_intake_ml = ?, " +
                        "calorie_limit = ?, protein_target = ?, carbs_target = ?, " +
                        "fats_target = ?, special_instructions = ?  " +
                        "WHERE trainer_goal_id = ?";

                pstmt = conn. prepareStatement(updateSql);
                pstmt.setInt(1, goal.getWorkoutDuration());
                pstmt.setInt(2, goal. getCalorieTarget());
                pstmt.setInt(3, goal.getWaterIntakeMl());
                pstmt.setInt(4, goal. getCalorieLimit());
                pstmt.setInt(5, goal.getProteinTarget());
                pstmt.setInt(6, goal.getCarbsTarget());
                pstmt. setInt(7, goal.getFatsTarget());
                pstmt.setString(8, goal. getSpecialInstructions());
                pstmt.setInt(9, existingGoalId);

                int updated = pstmt.executeUpdate();
                System.out.println("✅ Updated daily goal for member ID: " + goal.getMemberId() + " on " + goal.getGoalDate());
                return updated > 0;

            } else {
                // Insert new goal
                rs.close();
                pstmt.close();

                String insertSql = "INSERT INTO Trainer_Daily_Goals " +
                        "(trainer_id, member_id, goal_date, workout_duration, calorie_target, " +
                        "water_intake_ml, calorie_limit, protein_target, carbs_target, fats_target, " +
                        "special_instructions, created_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))";

                pstmt = conn.prepareStatement(insertSql);
                pstmt.setInt(1, goal.getTrainerId());
                pstmt.setInt(2, goal. getMemberId());
                pstmt.setString(3, goal.getGoalDate().toString());
                pstmt.setInt(4, goal.getWorkoutDuration());
                pstmt.setInt(5, goal. getCalorieTarget());
                pstmt.setInt(6, goal.getWaterIntakeMl());
                pstmt.setInt(7, goal. getCalorieLimit());
                pstmt.setInt(8, goal.getProteinTarget());
                pstmt.setInt(9, goal.getCarbsTarget());
                pstmt. setInt(10, goal.getFatsTarget());
                pstmt.setString(11, goal. getSpecialInstructions());

                int inserted = pstmt. executeUpdate();
                System.out.println("✅ Created daily goal for member ID: " + goal.getMemberId() + " on " + goal.getGoalDate());
                return inserted > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error setting daily goals: " + e.getMessage());
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

    public boolean setGoalsForWeek(TrainerDailyGoal baseGoal, int days) {
        Connection conn = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Start transaction

            LocalDate startDate = baseGoal.getGoalDate();
            int successCount = 0;

            for (int i = 0; i < days; i++) {
                TrainerDailyGoal dayGoal = new TrainerDailyGoal();
                dayGoal.setTrainerId(baseGoal.getTrainerId());
                dayGoal.setMemberId(baseGoal.getMemberId());
                dayGoal.setGoalDate(startDate.plusDays(i));
                dayGoal.setWorkoutDuration(baseGoal.getWorkoutDuration());
                dayGoal.setCalorieTarget(baseGoal.getCalorieTarget());
                dayGoal.setWaterIntakeMl(baseGoal.getWaterIntakeMl());
                dayGoal.setCalorieLimit(baseGoal.getCalorieLimit());
                dayGoal.setProteinTarget(baseGoal.getProteinTarget());
                dayGoal.setCarbsTarget(baseGoal.getCarbsTarget());
                dayGoal.setFatsTarget(baseGoal.getFatsTarget());
                dayGoal.setSpecialInstructions(baseGoal.getSpecialInstructions());

                if (setDailyGoals(dayGoal)) {
                    successCount++;
                }
            }

            conn.commit();
            System.out.println("✅ Set goals for " + successCount + " days");
            return successCount == days;

        } catch (SQLException e) {
            System.err.println("❌ Error setting weekly goals: " + e.getMessage());
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
                if (conn != null) {
                    conn.setAutoCommit(true);
                    DatabaseConnection.getInstance().releaseConnection(conn);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public TrainerDailyGoal getGoalsForMemberDate(int memberId, LocalDate date) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM Trainer_Daily_Goals WHERE member_id = ? AND goal_date = ? ";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, date. toString());
            rs = pstmt.executeQuery();

            if (rs. next()) {
                TrainerDailyGoal goal = new TrainerDailyGoal();
                goal.setTrainerGoalId(rs.getInt("trainer_goal_id"));
                goal.setTrainerId(rs.getInt("trainer_id"));
                goal. setMemberId(rs.getInt("member_id"));
                goal.setGoalDate(LocalDate.parse(rs.getString("goal_date")));
                goal.setWorkoutDuration(rs.getInt("workout_duration"));
                goal.setCalorieTarget(rs.getInt("calorie_target"));
                goal. setWaterIntakeMl(rs.getInt("water_intake_ml"));
                goal.setCalorieLimit(rs.getInt("calorie_limit"));
                goal.setProteinTarget(rs.getInt("protein_target"));
                goal.setCarbsTarget(rs.getInt("carbs_target"));
                goal. setFatsTarget(rs.getInt("fats_target"));
                goal.setSpecialInstructions(rs.getString("special_instructions"));

                String createdDateStr = rs.getString("created_date");
                if (createdDateStr != null && !createdDateStr.isEmpty()) {
                    goal.setCreatedDate(LocalDate.parse(createdDateStr. substring(0, 10)));
                }

                return goal;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching daily goals: " + e.getMessage());
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

        return null;
    }
}