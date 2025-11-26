package com.ecosimulator.simulation;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

/**
 * Timer-based simulation runner that executes turns automatically
 */
public class SimulationRunner {
    private final SimulationEngine engine;
    private Timeline timeline;
    private int turnDelayMs;

    public SimulationRunner(SimulationEngine engine) {
        this.engine = engine;
        this.turnDelayMs = engine.getConfig().getTurnDelayMs();
        setupTimeline();
    }

    /**
     * Setup the JavaFX timeline for automatic turn execution
     */
    private void setupTimeline() {
        timeline = new Timeline(new KeyFrame(
            Duration.millis(turnDelayMs),
            event -> {
                if (engine.isRunning() && !engine.isPaused()) {
                    Platform.runLater(engine::executeTurn);
                }
            }
        ));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    /**
     * Start the automatic simulation
     */
    public void start() {
        engine.start();
        timeline.play();
    }

    /**
     * Pause the simulation
     */
    public void pause() {
        engine.pause();
        timeline.pause();
    }

    /**
     * Resume the simulation
     */
    public void resume() {
        engine.resume();
        timeline.play();
    }

    /**
     * Stop the simulation completely
     */
    public void stop() {
        engine.stop();
        timeline.stop();
    }

    /**
     * Reset the simulation
     */
    public void reset() {
        stop();
        engine.reset();
        setupTimeline();
    }

    /**
     * Update the turn delay speed
     */
    public void setTurnDelay(int delayMs) {
        this.turnDelayMs = delayMs;
        boolean wasPlaying = timeline.getStatus() == Animation.Status.RUNNING;
        timeline.stop();
        setupTimeline();
        if (wasPlaying && engine.isRunning()) {
            timeline.play();
        }
    }

    /**
     * Get current turn delay
     */
    public int getTurnDelay() {
        return turnDelayMs;
    }

    /**
     * Check if simulation is running
     */
    public boolean isRunning() {
        return engine.isRunning();
    }

    /**
     * Check if simulation is paused
     */
    public boolean isPaused() {
        return engine.isPaused();
    }

    /**
     * Get the simulation engine
     */
    public SimulationEngine getEngine() {
        return engine;
    }
}
