package com.gym.dao;

import com.gym.models.Meal;
import com.gym.models.MealItem;
import com.gym.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealDAO {

    public boolean logMeal(Meal meal, List<MealItem> items) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            String insertMealSql = "INSERT INTO Meals (member_id, meal_type, meal_date, meal_time, " +
                    "total_calories, total_protein, total_carbs, total_fats, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(insertMealSql);
            pstmt.setInt(1, meal.getMemberId());
            pstmt.setString(2, meal.getMealType());
            pstmt.setString(3, meal.getMealDate().toString());
            pstmt.setString(4, meal.getMealTime().toString());
            pstmt.setInt(5, meal.getTotalCalories());
            pstmt.setDouble(6, meal.getTotalProtein());
            pstmt.setDouble(7, meal.getTotalCarbs());
            pstmt.setDouble(8, meal.getTotalFats());
            pstmt.setString(9, meal.getNotes());
            pstmt.executeUpdate();
            pstmt.close();

            pstmt = conn.prepareStatement("SELECT last_insert_rowid() as meal_id");
            rs = pstmt.executeQuery();
            int mealId = 0;
            if (rs.next()) {
                mealId = rs.getInt("meal_id");
            }
            pstmt.close();
            rs.close();

            for (MealItem item : items) {
                String insertItemSql = "INSERT INTO Meal_Items (meal_id, food_id, quantity, unit, calculated_calories) " +
                        "VALUES (?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(insertItemSql);
                pstmt.setInt(1, mealId);
                pstmt.setInt(2, item.getFoodId());
                pstmt.setDouble(3, item.getQuantity());
                pstmt.setString(4, item.getUnit());
                pstmt.setInt(5, item.getCalculatedCalories());
                pstmt.executeUpdate();
                pstmt.close();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    DatabaseConnection.getInstance().releaseConnection(conn);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<Meal> getMealsForDate(int memberId, LocalDate date) {
        List<Meal> meals = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT * FROM Meals WHERE member_id = ? AND meal_date = ? ORDER BY meal_time";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, date.toString());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Meal meal = new Meal();
                meal.setMealId(rs.getInt("meal_id"));
                meal.setMemberId(rs.getInt("member_id"));
                meal.setMealType(rs.getString("meal_type"));
                meal.setMealDate(LocalDate.parse(rs.getString("meal_date")));
                meal.setMealTime(java.time.LocalTime.parse(rs.getString("meal_time")));
                meal.setTotalCalories(rs.getInt("total_calories"));
                meal.setTotalProtein(rs.getDouble("total_protein"));
                meal.setTotalCarbs(rs.getDouble("total_carbs"));
                meal.setTotalFats(rs.getDouble("total_fats"));
                meal.setNotes(rs.getString("notes"));
                meals.add(meal);
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

        return meals;
    }

    public Map<String, Double> getDailyNutritionTotals(int memberId, LocalDate date) {
        Map<String, Double> totals = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();

            String sql = "SELECT COALESCE(SUM(total_calories), 0) as calories, " +
                    "COALESCE(SUM(total_protein), 0) as protein, " +
                    "COALESCE(SUM(total_carbs), 0) as carbs, " +
                    "COALESCE(SUM(total_fats), 0) as fats " +
                    "FROM Meals WHERE member_id = ? AND meal_date = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setString(2, date.toString());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                totals.put("calories", rs.getDouble("calories"));
                totals.put("protein", rs.getDouble("protein"));
                totals.put("carbs", rs.getDouble("carbs"));
                totals.put("fats", rs.getDouble("fats"));
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

        return totals;
    }
}

