package com.ecosimulator.core;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Scheduler class using javax.swing.Timer for automatic turn execution
 */
public class Scheduler {
    private Timer timer;
    private Simulator simulator;
    private int maxTurns;
    private int intervalMs;
    private Runnable onTurnComplete;
    private Runnable onSimulationEnd;
    
    public Scheduler(Simulator simulator) {
        this.simulator = simulator;
        this.maxTurns = 100;  // Default
        this.intervalMs = 1000;  // Default 1 second
    }
    
    /**
     * Start the automatic turn execution
     * @param intervalMs interval between turns in milliseconds
     * @param maxTurns maximum number of turns
     */
    public void start(int intervalMs, int maxTurns) {
        this.intervalMs = intervalMs;
        this.maxTurns = maxTurns;
        
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        
        timer = new Timer(intervalMs, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Execute one turn
                simulator.stepTurn();
                
                // Notify listeners
                if (onTurnComplete != null) {
                    onTurnComplete.run();
                }
                
                // Check stopping conditions
                if (simulator.getTurnNumber() >= maxTurns || simulator.isExtinct()) {
                    stop();
                    if (onSimulationEnd != null) {
                        onSimulationEnd.run();
                    }
                }
            }
        });
        
        timer.start();
    }
    
    /**
     * Stop the scheduler
     */
    public void stop() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }
    
    /**
     * Pause the scheduler
     */
    public void pause() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }
    
    /**
     * Resume the scheduler
     */
    public void resume() {
        if (timer != null && !timer.isRunning()) {
            timer.start();
        }
    }
    
    /**
     * Check if scheduler is running
     * @return true if running
     */
    public boolean isRunning() {
        return timer != null && timer.isRunning();
    }
    
    /**
     * Set callback for when a turn completes
     * @param callback the callback
     */
    public void setOnTurnComplete(Runnable callback) {
        this.onTurnComplete = callback;
    }
    
    /**
     * Set callback for when simulation ends
     * @param callback the callback
     */
    public void setOnSimulationEnd(Runnable callback) {
        this.onSimulationEnd = callback;
    }
    
    /**
     * Update the interval
     * @param intervalMs new interval in milliseconds
     */
    public void setInterval(int intervalMs) {
        this.intervalMs = intervalMs;
        if (timer != null && timer.isRunning()) {
            boolean wasRunning = true;
            timer.stop();
            timer.setDelay(intervalMs);
            if (wasRunning) {
                timer.start();
            }
        }
    }
    
    /**
     * Get current interval
     * @return interval in milliseconds
     */
    public int getInterval() {
        return intervalMs;
    }
    
    /**
     * Get maximum turns
     * @return max turns
     */
    public int getMaxTurns() {
        return maxTurns;
    }
}
