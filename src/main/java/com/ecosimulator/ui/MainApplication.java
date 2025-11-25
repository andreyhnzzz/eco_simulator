package com.ecosimulator.ui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main JavaFX application entry point.
 */
public class MainApplication extends Application {
    
    private MainController mainController;

    @Override
    public void start(Stage primaryStage) {
        mainController = new MainController(primaryStage);
        mainController.showLoginView();
    }

    @Override
    public void stop() {
        if (mainController != null) {
            mainController.cleanup();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
