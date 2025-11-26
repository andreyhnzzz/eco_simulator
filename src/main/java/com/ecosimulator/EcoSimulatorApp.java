package com.ecosimulator;

import com.ecosimulator.ui.SimulationView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main JavaFX Application for the Eco Simulator
 */
public class EcoSimulatorApp extends Application {

    private static final String APP_TITLE = "Eco Simulator - Simulador Ecológico";
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) {
        // Create main view
        SimulationView simulationView = new SimulationView();

        // Create scene with CSS styling
        Scene scene = new Scene(simulationView, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        
        // Load CSS styles
        String cssPath = getClass().getResource("/css/styles.css").toExternalForm();
        scene.getStylesheets().add(cssPath);

        // Configure stage
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(700);

        // Try to load application icon
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/app-icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
            System.out.println("Application icon not found, using default");
        }

        primaryStage.show();
    }

    @Override
    public void stop() {
        // Cleanup on application close
        System.out.println("Eco Simulator closing...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
