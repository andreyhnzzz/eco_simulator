package com.ecosimulator;

import com.ecosimulator.auth.Session;
import com.ecosimulator.ui.AnimationUtils;
import com.ecosimulator.ui.LoginView;
import com.ecosimulator.ui.SimulationView;
import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Main JavaFX Application for the Eco Simulator
 * Includes login/registration flow with smooth scene transitions
 * Features premium animations and modern visual effects
 */
public class EcoSimulatorApp extends Application {

    private static final String APP_TITLE = "Eco Simulator - Simulador Ecológico";
    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 850;

    private Stage primaryStage;
    private StackPane rootContainer;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Configure stage
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(700);

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
     * Show the login/registration view with animation
     */
    private void showLoginView() {
        LoginView loginView = new LoginView(primaryStage, this::transitionToSimulationView);
        Scene loginScene = loginView.createScene();
        primaryStage.setScene(loginScene);
        primaryStage.setWidth(520);
        primaryStage.setHeight(720);
        primaryStage.centerOnScreen();
    }

    /**
     * Transition to simulation view with animation
     */
    private void transitionToSimulationView() {
        if (!Session.isLoggedIn()) {
            System.err.println("Cannot show simulation view - user not logged in");
            return;
        }

        // Create main simulation view
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

        // Apply the new scene with transition effect
        simulationView.setOpacity(0);
        primaryStage.setScene(scene);
        primaryStage.setWidth(DEFAULT_WIDTH);
        primaryStage.setHeight(DEFAULT_HEIGHT);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(750);
        primaryStage.centerOnScreen();
        primaryStage.setTitle(APP_TITLE + " - " + Session.getUser().getName());
        
        // Fade in the simulation view
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), simulationView);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(AnimationUtils.EASE_OUT_CUBIC);
        fadeIn.play();
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
