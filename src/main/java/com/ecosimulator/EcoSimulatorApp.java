package com.ecosimulator;

import com.ecosimulator.auth.Session;
import com.ecosimulator.ui.LoginView;
import com.ecosimulator.ui.SimulationView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main JavaFX Application for the Eco Simulator
 * Includes login/registration flow before accessing the simulation
 */
public class EcoSimulatorApp extends Application {

    private static final String APP_TITLE = "Eco Simulator - Simulador Ecológico";
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Configure stage
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(650);

        // Try to load application icon
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/app-icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
            System.out.println("Application icon not found, using default");
        }

        // Show login view first
        showLoginView();
        primaryStage.show();
    }

    /**
     * Show the login/registration view
     */
    private void showLoginView() {
        LoginView loginView = new LoginView(primaryStage, this::showSimulationView);
        Scene loginScene = loginView.createScene();
        primaryStage.setScene(loginScene);
        primaryStage.setWidth(500);
        primaryStage.setHeight(650);
        primaryStage.centerOnScreen();
    }

    /**
     * Show the main simulation view after successful login
     */
    private void showSimulationView() {
        if (!Session.isLoggedIn()) {
            System.err.println("Cannot show simulation view - user not logged in");
            return;
        }

        // Create main view
        SimulationView simulationView = new SimulationView();

        // Create scene with CSS styling
        Scene scene = new Scene(simulationView, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        
        // Load CSS styles
        try {
            String cssPath = getClass().getResource("/css/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Could not load CSS styles: " + e.getMessage());
        }

        // Update stage
        primaryStage.setScene(scene);
        primaryStage.setWidth(DEFAULT_WIDTH);
        primaryStage.setHeight(DEFAULT_HEIGHT);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(700);
        primaryStage.centerOnScreen();
        primaryStage.setTitle(APP_TITLE + " - " + Session.getUser().getName());
    }

    @Override
    public void stop() {
        // Cleanup on application close
        Session.logout();
        System.out.println("Eco Simulator closing...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
