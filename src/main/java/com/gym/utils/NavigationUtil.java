package com.gym.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigationUtil {

    /**
     * Navigate to a new view while maintaining current window size
     */
    public static void navigateTo(Stage stage, String fxmlPath, String title) throws Exception {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
        Parent root = loader.load();

        // Reuse existing scene to maintain window size
        Scene currentScene = stage.getScene();
        currentScene.setRoot(root);

        stage.setTitle(title);
    }

    /**
     * Navigate with a new scene (for initial load or when size change is needed)
     */
    public static void navigateWithNewScene(Stage stage, String fxmlPath, String title,
                                           double width, double height) throws Exception {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.centerOnScreen();
        stage.setTitle(title);
    }
}

