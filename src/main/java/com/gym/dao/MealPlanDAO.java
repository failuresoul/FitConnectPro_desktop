package com.gym.dao;

import com.gym.models.Food;
import com.gym.models.MealPlan;
import com.gym.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MealPlanDAO {

    public boolean createMealPlan(List<MealPlan> meals) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Start transaction

            String sql = "INSERT INTO Trainer_Meal_Plans " +
                    "(trainer_id, member_id, plan_date, meal_type, meal_time, foods, " +
                    "total_calories, total_protein, total_carbs, total_fats, " +
                    "preparation_instructions, created_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'))";

            pstmt = conn.prepareStatement(sql);

            for (MealPlan meal : meals) {
                pstmt.setInt(1, meal.getTrainerId());
                pstmt.setInt(2, meal.getMemberId());
                pstmt.setString(3, meal.getPlanDate().toString());
                pstmt.setString(4, meal. getMealType());
                pstmt.setString(5, meal.getMealTime() != null ? meal.getMealTime().toString() : null);
                pstmt.setString(6, meal.getFoods());
                pstmt. setInt(7, meal.getTotalCalories());
                pstmt.setDouble(8, meal.getTotalProtein());
                pstmt.setDouble(9, meal.getTotalCarbs());
                pstmt.setDouble(10, meal.getTotalFats());
                pstmt. setString(11, meal.getPreparationInstructions());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit();

            System.out.println("✅ Created " + results.length + " meal plans successfully");
            return results.length == meals.size();

        } catch (SQLException e) {
            System.err.println("❌ Error creating meal plans: " + e.getMessage());
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
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    DatabaseConnection.getInstance().releaseConnection(conn);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Food> getAllFoods() {
        List<Food> foods = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM Foods_Database ORDER BY food_name";

            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Food food = new Food();
                food. setFoodId(rs.getInt("food_id"));
                food.setFoodName(rs.getString("food_name"));
                food.setServingSize(rs.getString("serving_size"));
                food.setCaloriesPerServing(rs.getInt("calories_per_serving"));
                food.setProtein(rs.getDouble("protein"));
                food.setCarbs(rs. getDouble("carbs"));
                food.setFats(rs. getDouble("fats"));
                food.setCategory(rs.getString("category"));
                food.setGymRecommended(rs. getInt("is_gym_recommended") == 1);
                foods.add(food);
            }

            System.out.println("✅ Loaded " + foods.size() + " foods from database");

        } catch (SQLException e) {
            System.err.println("❌ Error loading foods: " + e.getMessage());
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

        return foods;
    }

    public List<Food> searchFoods(String keyword) {
        List<Food> foods = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT * FROM Foods_Database WHERE food_name LIKE ? ORDER BY food_name";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + keyword + "%");
            rs = pstmt.executeQuery();

            while (rs. next()) {
                Food food = new Food();
                food.setFoodId(rs.getInt("food_id"));
                food.setFoodName(rs. getString("food_name"));
                food.setServingSize(rs.getString("serving_size"));
                food.setCaloriesPerServing(rs.getInt("calories_per_serving"));
                food.setProtein(rs. getDouble("protein"));
                food.setCarbs(rs.getDouble("carbs"));
                food.setFats(rs.getDouble("fats"));
                food.setCategory(rs.getString("category"));
                food.setGymRecommended(rs.getInt("is_gym_recommended") == 1);
                foods. add(food);
            }

            System.out.println("✅ Found " + foods.size() + " foods matching:  " + keyword);

        } catch (SQLException e) {
            System.err.println("❌ Error searching foods: " + e. getMessage());
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

        return foods;
    }
}