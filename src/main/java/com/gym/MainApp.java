package com.gym;

import com.gym.utils.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx. scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Initializing Premium Gym Management System...");

            // Initialize database
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            dbConnection.initializeDatabase();
            System.out.println("Database initialized successfully!");

            // Insert sample admin data
            dbConnection.insertSampleData();
            System.out.println("Sample data inserted successfully!");

            // Load login FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root, 800, 600);

            // Set stage properties
            primaryStage.setTitle("Premium Gym Management System - Login");
            primaryStage. setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();

            // Show stage
            primaryStage. show();

            System.out.println("Application started successfully!");
            System.out.println("\n=== DEFAULT LOGIN CREDENTIALS ===");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
            System.out.println("User Type: Admin");
            System.out.println("==================================\n");

        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();

            // Show error dialog if JavaFX is initialized
            try {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR
                );
                alert.setTitle("Application Error");
                alert.setHeaderText("Failed to Start Application");
                alert.setContentText("Error: " + e.getMessage() +
                        "\n\nPlease check the console for more details.");
                alert.showAndWait();
            } catch (Exception alertError) {
                System.err.println("Could not display error dialog: " + alertError.getMessage());
            }
        }
    }

    @Override
    public void stop() {
        try {
            System.out.println("Shutting down application...");

            // Close all database connections
            DatabaseConnection.getInstance().closeAllConnections();
            System.out.println("Database connections closed successfully!");

            System.out.println("Application shut down successfully!");
        } catch (Exception e) {
            System. err.println("Error during shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}