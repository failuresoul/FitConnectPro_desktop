package com.gym;

import javafx. application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gym Management System");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}