package com.ecosimulator.ui;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 * Theme Manager for handling Dark Mode and Light Mode switching
 * Provides smooth animated transitions between themes
 */
public class ThemeManager {
    
    private static final String LIGHT_THEME_PATH = "/css/styles.css";
    private static final String DARK_THEME_PATH = "/css/dark-theme.css";
    
    private static boolean isDarkMode = false;
    private static final Duration TRANSITION_DURATION = Duration.millis(300);
    
    // Callbacks for theme change events
    private static Runnable onThemeChangeCallback;
    
    private ThemeManager() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Check if dark mode is currently active
     * @return true if dark mode is active
     */
    public static boolean isDarkMode() {
        return isDarkMode;
    }
    
    /**
     * Set the callback to be executed when theme changes
     * @param callback the callback to execute
     */
    public static void setOnThemeChangeCallback(Runnable callback) {
        onThemeChangeCallback = callback;
    }
    
    /**
     * Toggle between dark and light mode with smooth animation
     * @param scene the scene to apply the theme to
     */
    public static void toggleTheme(Scene scene) {
        toggleTheme(scene, null);
    }
    
    /**
     * Toggle between dark and light mode with smooth animation
     * @param scene the scene to apply the theme to
     * @param rootNode optional root node for additional animation effects
     */
    public static void toggleTheme(Scene scene, Node rootNode) {
        if (rootNode != null) {
            playThemeTransitionAnimation(rootNode, () -> {
                switchTheme(scene);
                isDarkMode = !isDarkMode;
                if (onThemeChangeCallback != null) {
                    onThemeChangeCallback.run();
                }
            });
        } else {
            switchTheme(scene);
            isDarkMode = !isDarkMode;
            if (onThemeChangeCallback != null) {
                onThemeChangeCallback.run();
            }
        }
    }
    
    /**
     * Apply dark mode to a scene
     * @param scene the scene to apply dark mode to
     */
    public static void applyDarkMode(Scene scene) {
        scene.getStylesheets().clear();
        java.net.URL darkResource = ThemeManager.class.getResource(DARK_THEME_PATH);
        if (darkResource != null) {
            String darkCss = darkResource.toExternalForm();
            scene.getStylesheets().add(darkCss);
            isDarkMode = true;
        } else {
            System.err.println("Dark theme CSS not found: " + DARK_THEME_PATH);
        }
    }
    
    /**
     * Apply light mode to a scene
     * @param scene the scene to apply light mode to
     */
    public static void applyLightMode(Scene scene) {
        scene.getStylesheets().clear();
        java.net.URL lightResource = ThemeManager.class.getResource(LIGHT_THEME_PATH);
        if (lightResource != null) {
            String lightCss = lightResource.toExternalForm();
            scene.getStylesheets().add(lightCss);
            isDarkMode = false;
        } else {
            System.err.println("Light theme CSS not found: " + LIGHT_THEME_PATH);
        }
    }
    
    /**
     * Apply dark mode to a scene with animation
     * @param scene the scene to apply dark mode to
     * @param rootNode the root node for animation
     */
    public static void applyDarkModeAnimated(Scene scene, Node rootNode) {
        if (!isDarkMode) {
            playThemeTransitionAnimation(rootNode, () -> {
                applyDarkMode(scene);
            });
        }
    }
    
    /**
     * Apply light mode to a scene with animation
     * @param scene the scene to apply light mode to
     * @param rootNode the root node for animation
     */
    public static void applyLightModeAnimated(Scene scene, Node rootNode) {
        if (isDarkMode) {
            playThemeTransitionAnimation(rootNode, () -> {
                applyLightMode(scene);
            });
        }
    }
    
    /**
     * Apply the current theme (light or dark) to a new scene
     * @param scene the scene to apply the current theme to
     */
    public static void applyCurrentTheme(Scene scene) {
        if (isDarkMode) {
            applyDarkMode(scene);
        } else {
            applyLightMode(scene);
        }
    }
    
    /**
     * Switch the theme stylesheets
     * @param scene the scene to switch themes on
     */
    private static void switchTheme(Scene scene) {
        scene.getStylesheets().clear();
        
        if (isDarkMode) {
            // Switch to light mode
            java.net.URL lightResource = ThemeManager.class.getResource(LIGHT_THEME_PATH);
            if (lightResource != null) {
                scene.getStylesheets().add(lightResource.toExternalForm());
            } else {
                System.err.println("Light theme CSS not found: " + LIGHT_THEME_PATH);
            }
        } else {
            // Switch to dark mode
            java.net.URL darkResource = ThemeManager.class.getResource(DARK_THEME_PATH);
            if (darkResource != null) {
                scene.getStylesheets().add(darkResource.toExternalForm());
            } else {
                System.err.println("Dark theme CSS not found: " + DARK_THEME_PATH);
            }
        }
    }
    
    /**
     * Play theme transition animation (fade out -> swap -> fade in)
     * @param node the node to animate
     * @param onSwap callback to execute during the transition
     */
    private static void playThemeTransitionAnimation(Node node, Runnable onSwap) {
        // Fade out
        FadeTransition fadeOut = new FadeTransition(TRANSITION_DURATION.divide(2), node);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.7);
        fadeOut.setInterpolator(Interpolator.EASE_IN);
        
        // Fade in
        FadeTransition fadeIn = new FadeTransition(TRANSITION_DURATION.divide(2), node);
        fadeIn.setFromValue(0.7);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);
        
        // Execute swap during transition
        fadeOut.setOnFinished(e -> {
            if (onSwap != null) {
                onSwap.run();
            }
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    /**
     * Play an enhanced theme transition with scale effect
     * @param node the node to animate
     * @param onSwap callback to execute during the transition
     */
    public static void playEnhancedThemeTransition(Node node, Runnable onSwap) {
        // Combined fade and scale for more premium effect
        FadeTransition fadeOut = new FadeTransition(TRANSITION_DURATION.divide(2), node);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.8);
        
        ScaleTransition scaleOut = new ScaleTransition(TRANSITION_DURATION.divide(2), node);
        scaleOut.setToX(0.98);
        scaleOut.setToY(0.98);
        
        ParallelTransition transitionOut = new ParallelTransition(fadeOut, scaleOut);
        transitionOut.setInterpolator(Interpolator.EASE_IN);
        
        // Reverse animation
        FadeTransition fadeIn = new FadeTransition(TRANSITION_DURATION.divide(2), node);
        fadeIn.setFromValue(0.8);
        fadeIn.setToValue(1.0);
        
        ScaleTransition scaleIn = new ScaleTransition(TRANSITION_DURATION.divide(2), node);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        
        ParallelTransition transitionIn = new ParallelTransition(fadeIn, scaleIn);
        transitionIn.setInterpolator(Interpolator.EASE_OUT);
        
        transitionOut.setOnFinished(e -> {
            if (onSwap != null) {
                onSwap.run();
            }
            transitionIn.play();
        });
        
        transitionOut.play();
    }
    
    /**
     * Get the theme toggle button text based on current theme
     * @return the button text (emoji + label)
     */
    public static String getThemeToggleText() {
        return isDarkMode ? "☀️ Light Mode" : "🌙 Dark Mode";
    }
    
    /**
     * Get the current theme name
     * @return "Dark" or "Light"
     */
    public static String getCurrentThemeName() {
        return isDarkMode ? "Dark" : "Light";
    }
    
    /**
     * Get color for species based on current theme
     * @param species the species type
     * @return the hex color string
     */
    public static String getSpeciesColor(String species) {
        if (isDarkMode) {
            switch (species.toLowerCase()) {
                case "predator":
                    return "#FF5252";
                case "prey":
                    return "#448AFF";
                case "third":
                case "scavenger":
                    return "#FFAB40";
                case "mutated":
                    return "#E040FB";
                case "empty":
                default:
                    return "#1B263B";
            }
        } else {
            switch (species.toLowerCase()) {
                case "predator":
                    return "#D32F2F";
                case "prey":
                    return "#1976D2";
                case "third":
                case "scavenger":
                    return "#FF9800";
                case "mutated":
                    return "#9C27B0";
                case "empty":
                default:
                    return "#2E7D32";
            }
        }
    }
    
    /**
     * Get the background color for the current theme
     * @return the hex color string
     */
    public static String getBackgroundColor() {
        return isDarkMode ? "#0F0F0F" : "#FAFAFA";
    }
    
    /**
     * Get the surface color for the current theme
     * @return the hex color string
     */
    public static String getSurfaceColor() {
        return isDarkMode ? "#1E1E1E" : "#FFFFFF";
    }
    
    /**
     * Get the primary text color for the current theme
     * @return the hex color string
     */
    public static String getTextPrimaryColor() {
        return isDarkMode ? "#FFFFFF" : "#212121";
    }
    
    /**
     * Get the secondary text color for the current theme
     * @return the hex color string
     */
    public static String getTextSecondaryColor() {
        return isDarkMode ? "#B3B3B3" : "#757575";
    }
    
    /**
     * Get the accent color for the current theme
     * @return the hex color string
     */
    public static String getAccentColor() {
        return isDarkMode ? "#00E5FF" : "#4CAF50";
    }
}
