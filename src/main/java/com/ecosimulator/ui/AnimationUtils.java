package com.ecosimulator.ui;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Utility class for premium JavaFX animations
 * Provides smooth, modern, and professional animation effects
 * following Material Design and Apple HIG principles
 */
public class AnimationUtils {

    // Animation duration constants
    public static final Duration DURATION_FAST = Duration.millis(150);
    public static final Duration DURATION_NORMAL = Duration.millis(250);
    public static final Duration DURATION_SLOW = Duration.millis(400);
    public static final Duration DURATION_SCENE_TRANSITION = Duration.millis(350);
    
    // Custom interpolators for smooth animations
    public static final Interpolator EASE_OUT_CUBIC = Interpolator.SPLINE(0.33, 1, 0.68, 1);
    public static final Interpolator EASE_IN_OUT_CUBIC = Interpolator.SPLINE(0.65, 0, 0.35, 1);
    public static final Interpolator EASE_OUT_BACK = Interpolator.SPLINE(0.34, 1.56, 0.64, 1);
    public static final Interpolator SPRING = Interpolator.SPLINE(0.5, 1.5, 0.5, 1);

    private AnimationUtils() {
        // Private constructor to prevent instantiation
    }

    // ==========================================
    // SCENE TRANSITIONS
    // ==========================================

    /**
     * Fade in animation for scene transitions
     */
    public static FadeTransition fadeIn(Node node, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setInterpolator(EASE_OUT_CUBIC);
        return fade;
    }

    /**
     * Fade out animation for scene transitions
     */
    public static FadeTransition fadeOut(Node node, Duration duration) {
        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setInterpolator(EASE_OUT_CUBIC);
        return fade;
    }

    /**
     * Slide in from right animation
     */
    public static TranslateTransition slideInFromRight(Node node, Duration duration) {
        node.setTranslateX(300);
        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromX(300);
        slide.setToX(0);
        slide.setInterpolator(EASE_OUT_CUBIC);
        return slide;
    }

    /**
     * Slide in from left animation
     */
    public static TranslateTransition slideInFromLeft(Node node, Duration duration) {
        node.setTranslateX(-300);
        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromX(-300);
        slide.setToX(0);
        slide.setInterpolator(EASE_OUT_CUBIC);
        return slide;
    }

    /**
     * Slide in from bottom animation
     */
    public static TranslateTransition slideInFromBottom(Node node, Duration duration) {
        node.setTranslateY(100);
        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromY(100);
        slide.setToY(0);
        slide.setInterpolator(EASE_OUT_CUBIC);
        return slide;
    }

    /**
     * Scale and fade in animation (zoom effect)
     */
    public static ParallelTransition scaleAndFadeIn(Node node, Duration duration) {
        node.setOpacity(0);
        node.setScaleX(0.8);
        node.setScaleY(0.8);

        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(duration, node);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);

        ParallelTransition parallel = new ParallelTransition(fade, scale);
        parallel.setInterpolator(EASE_OUT_CUBIC);
        return parallel;
    }

    /**
     * Combined slide up and fade in for elegant entrance
     */
    public static ParallelTransition slideUpAndFadeIn(Node node, Duration duration) {
        node.setOpacity(0);
        node.setTranslateY(30);

        FadeTransition fade = new FadeTransition(duration, node);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(duration, node);
        slide.setFromY(30);
        slide.setToY(0);

        ParallelTransition parallel = new ParallelTransition(fade, slide);
        parallel.setInterpolator(EASE_OUT_CUBIC);
        return parallel;
    }

    // ==========================================
    // BUTTON ANIMATIONS
    // ==========================================

    /**
     * Apply hover animations to a button (scale up + glow effect)
     */
    public static void applyButtonHoverAnimation(Button button) {
        // Store original effect if any
        var originalEffect = button.getEffect();
        
        button.setOnMouseEntered(e -> {
            // Scale animation
            ScaleTransition scale = new ScaleTransition(DURATION_FAST, button);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.setInterpolator(EASE_OUT_CUBIC);
            
            // Add subtle glow
            DropShadow glow = new DropShadow();
            glow.setRadius(20);
            glow.setSpread(0.3);
            glow.setColor(Color.rgb(76, 175, 80, 0.4));
            button.setEffect(glow);
            
            scale.play();
        });
        
        button.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(DURATION_FAST, button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.setInterpolator(EASE_OUT_CUBIC);
            
            // Restore original effect
            button.setEffect(originalEffect);
            
            scale.play();
        });
    }

    /**
     * Button press animation (slight shrink + shadow change)
     */
    public static void applyButtonPressAnimation(Button button) {
        button.setOnMousePressed(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(80), button);
            scale.setToX(0.95);
            scale.setToY(0.95);
            scale.setInterpolator(Interpolator.EASE_IN);
            scale.play();
        });
        
        button.setOnMouseReleased(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.setInterpolator(EASE_OUT_BACK);
            scale.play();
        });
    }

    /**
     * Button click ripple-like feedback animation
     */
    public static void playButtonClickAnimation(Button button) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(80), button);
        scaleDown.setToX(0.92);
        scaleDown.setToY(0.92);
        
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), button);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);
        scaleUp.setInterpolator(EASE_OUT_BACK);
        
        SequentialTransition sequence = new SequentialTransition(scaleDown, scaleUp);
        sequence.play();
    }

    // ==========================================
    // CELL ANIMATIONS (Ecosystem Grid)
    // ==========================================

    /**
     * Cell spawn animation - fade in with scale
     */
    public static void playCellSpawnAnimation(Rectangle cell) {
        cell.setOpacity(0);
        cell.setScaleX(0.3);
        cell.setScaleY(0.3);

        FadeTransition fade = new FadeTransition(DURATION_NORMAL, cell);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(DURATION_NORMAL, cell);
        scale.setFromX(0.3);
        scale.setFromY(0.3);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(EASE_OUT_BACK);

        ParallelTransition parallel = new ParallelTransition(fade, scale);
        parallel.play();
    }

    /**
     * Cell death animation - fade out with shrink
     */
    public static void playCellDeathAnimation(Rectangle cell, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(DURATION_NORMAL, cell);
        fade.setFromValue(1);
        fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(DURATION_NORMAL, cell);
        scale.setFromX(1);
        scale.setFromY(1);
        scale.setToX(0.3);
        scale.setToY(0.3);
        scale.setInterpolator(EASE_IN_OUT_CUBIC);

        ParallelTransition parallel = new ParallelTransition(fade, scale);
        parallel.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        parallel.play();
    }

    /**
     * Cell movement animation - smooth translate
     */
    public static void playCellMoveAnimation(Rectangle cell, double toX, double toY) {
        TranslateTransition move = new TranslateTransition(Duration.millis(180), cell);
        move.setToX(toX);
        move.setToY(toY);
        move.setInterpolator(EASE_IN_OUT_CUBIC);
        move.play();
    }

    /**
     * Reproduction pulse effect - glowing pulse animation
     */
    public static void playReproductionPulseAnimation(Rectangle cell) {
        // Create a subtle pulse effect
        ScaleTransition pulseScale = new ScaleTransition(Duration.millis(300), cell);
        pulseScale.setFromX(1.0);
        pulseScale.setFromY(1.0);
        pulseScale.setToX(1.3);
        pulseScale.setToY(1.3);
        pulseScale.setAutoReverse(true);
        pulseScale.setCycleCount(2);
        pulseScale.setInterpolator(EASE_IN_OUT_CUBIC);

        // Add glow effect during pulse
        Glow glow = new Glow(0);
        cell.setEffect(glow);
        
        Timeline glowTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0)),
            new KeyFrame(Duration.millis(300), new KeyValue(glow.levelProperty(), 0.8)),
            new KeyFrame(Duration.millis(600), new KeyValue(glow.levelProperty(), 0))
        );
        
        ParallelTransition parallel = new ParallelTransition(pulseScale, glowTimeline);
        parallel.setOnFinished(e -> cell.setEffect(null));
        parallel.play();
    }

    /**
     * Mutation glow effect - persistent subtle pulsing glow
     */
    public static Timeline createMutationGlowAnimation(Node node) {
        Glow glow = new Glow(0.3);
        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setSpread(0.4);
        shadow.setColor(Color.rgb(156, 39, 176, 0.8)); // Purple mutation color
        shadow.setInput(glow);
        node.setEffect(shadow);

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(glow.levelProperty(), 0.3),
                new KeyValue(shadow.radiusProperty(), 8)),
            new KeyFrame(Duration.millis(800), 
                new KeyValue(glow.levelProperty(), 0.6),
                new KeyValue(shadow.radiusProperty(), 12)),
            new KeyFrame(Duration.millis(1600), 
                new KeyValue(glow.levelProperty(), 0.3),
                new KeyValue(shadow.radiusProperty(), 8))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }

    // ==========================================
    // UI ELEMENT ANIMATIONS
    // ==========================================

    /**
     * Pulsing highlight for important UI elements
     */
    public static Timeline createPulsingHighlight(Node node, Color highlightColor) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(0);
        shadow.setSpread(0);
        shadow.setColor(highlightColor);
        node.setEffect(shadow);

        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(shadow.radiusProperty(), 0),
                new KeyValue(shadow.spreadProperty(), 0)),
            new KeyFrame(Duration.millis(600),
                new KeyValue(shadow.radiusProperty(), 15),
                new KeyValue(shadow.spreadProperty(), 0.3)),
            new KeyFrame(Duration.millis(1200),
                new KeyValue(shadow.radiusProperty(), 0),
                new KeyValue(shadow.spreadProperty(), 0))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }

    /**
     * Shake animation for error feedback
     */
    public static void playShakeAnimation(Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> node.setTranslateX(0));
        shake.play();
    }

    /**
     * Success checkmark animation - bounce effect
     */
    public static void playSuccessAnimation(Node node) {
        node.setScaleX(0);
        node.setScaleY(0);
        
        ScaleTransition scale = new ScaleTransition(DURATION_NORMAL, node);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(EASE_OUT_BACK);
        scale.play();
    }

    /**
     * Stats counter animation - counting up effect
     */
    public static Timeline createCounterAnimation(javafx.scene.control.Label label, 
                                                   int fromValue, int toValue, 
                                                   Duration duration) {
        Timeline timeline = new Timeline();
        int steps = 30;
        double stepDuration = duration.toMillis() / steps;
        
        for (int i = 0; i <= steps; i++) {
            final int step = i;
            int value = fromValue + (int) ((toValue - fromValue) * 
                EASE_OUT_CUBIC.interpolate(0, 1, (double) step / steps));
            
            KeyFrame keyFrame = new KeyFrame(Duration.millis(stepDuration * step),
                e -> label.setText(String.valueOf(value)));
            timeline.getKeyFrames().add(keyFrame);
        }
        
        return timeline;
    }

    // ==========================================
    // PANEL AND CONTAINER ANIMATIONS
    // ==========================================

    /**
     * Card elevation animation on hover
     */
    public static void applyCardHoverAnimation(Pane card) {
        DropShadow normalShadow = new DropShadow();
        normalShadow.setRadius(10);
        normalShadow.setOffsetY(4);
        normalShadow.setColor(Color.rgb(0, 0, 0, 0.1));
        card.setEffect(normalShadow);
        
        card.setOnMouseEntered(e -> {
            Timeline elevate = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(normalShadow.radiusProperty(), 10, EASE_OUT_CUBIC),
                    new KeyValue(normalShadow.offsetYProperty(), 4, EASE_OUT_CUBIC),
                    new KeyValue(card.translateYProperty(), 0, EASE_OUT_CUBIC)),
                new KeyFrame(DURATION_FAST,
                    new KeyValue(normalShadow.radiusProperty(), 20, EASE_OUT_CUBIC),
                    new KeyValue(normalShadow.offsetYProperty(), 12, EASE_OUT_CUBIC),
                    new KeyValue(card.translateYProperty(), -4, EASE_OUT_CUBIC))
            );
            elevate.play();
        });
        
        card.setOnMouseExited(e -> {
            Timeline lower = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(normalShadow.radiusProperty(), 20, EASE_OUT_CUBIC),
                    new KeyValue(normalShadow.offsetYProperty(), 12, EASE_OUT_CUBIC),
                    new KeyValue(card.translateYProperty(), -4, EASE_OUT_CUBIC)),
                new KeyFrame(DURATION_FAST,
                    new KeyValue(normalShadow.radiusProperty(), 10, EASE_OUT_CUBIC),
                    new KeyValue(normalShadow.offsetYProperty(), 4, EASE_OUT_CUBIC),
                    new KeyValue(card.translateYProperty(), 0, EASE_OUT_CUBIC))
            );
            lower.play();
        });
    }

    /**
     * Staggered entrance animation for list items
     */
    public static void playStaggeredEntranceAnimation(Node... nodes) {
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            node.setOpacity(0);
            node.setTranslateY(20);
            
            PauseTransition delay = new PauseTransition(Duration.millis(50 * i));
            delay.setOnFinished(e -> {
                ParallelTransition entrance = slideUpAndFadeIn(node, DURATION_NORMAL);
                entrance.play();
            });
            delay.play();
        }
    }

    // ==========================================
    // CHART ANIMATIONS
    // ==========================================

    /**
     * Pie chart intro animation - rotate and fade in
     */
    public static ParallelTransition createChartIntroAnimation(Node chart) {
        chart.setOpacity(0);
        chart.setRotate(-15);
        chart.setScaleX(0.8);
        chart.setScaleY(0.8);

        FadeTransition fade = new FadeTransition(DURATION_SLOW, chart);
        fade.setFromValue(0);
        fade.setToValue(1);

        RotateTransition rotate = new RotateTransition(DURATION_SLOW, chart);
        rotate.setFromAngle(-15);
        rotate.setToAngle(0);

        ScaleTransition scale = new ScaleTransition(DURATION_SLOW, chart);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);

        ParallelTransition parallel = new ParallelTransition(fade, rotate, scale);
        parallel.setInterpolator(EASE_OUT_CUBIC);
        return parallel;
    }

    // ==========================================
    // LOADING ANIMATIONS
    // ==========================================

    /**
     * Create a rotating loading spinner animation
     */
    public static RotateTransition createSpinnerAnimation(Node spinner) {
        RotateTransition rotate = new RotateTransition(Duration.seconds(1), spinner);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        return rotate;
    }

    /**
     * Fade in loading overlay
     */
    public static FadeTransition showLoadingOverlay(Pane overlay) {
        overlay.setVisible(true);
        return fadeIn(overlay, DURATION_FAST);
    }

    /**
     * Fade out loading overlay
     */
    public static FadeTransition hideLoadingOverlay(Pane overlay) {
        FadeTransition fade = fadeOut(overlay, DURATION_FAST);
        fade.setOnFinished(e -> overlay.setVisible(false));
        return fade;
    }
}
