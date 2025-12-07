package com.ecosimulator.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregates results from multiple consecutive simulation runs.
 * Used for generating comprehensive multi-page PDF reports.
 */
public class MultiSimulationReport {
    private final List<SimulationResult> results;
    private LocalDateTime reportTimestamp;
    
    public MultiSimulationReport() {
        this.results = new ArrayList<>();
        this.reportTimestamp = LocalDateTime.now();
    }
    
    /**
     * Add a simulation result to the report
     */
    public void addSimulation(SimulationResult result) {
        results.add(result);
    }
    
    /**
     * Get all simulation results
     */
    public List<SimulationResult> getResults() {
        return Collections.unmodifiableList(results);
    }
    
    /**
     * Get the number of simulations in this report
     */
    public int getSimulationCount() {
        return results.size();
    }
    
    /**
     * Get the timestamp when this report was created
     */
    public LocalDateTime getReportTimestamp() {
        return reportTimestamp;
    }
    
    /**
     * Check if the report is empty
     */
    public boolean isEmpty() {
        return results.isEmpty();
    }
    
    /**
     * Clear all simulation results and reset timestamp
     */
    public void clear() {
        results.clear();
        this.reportTimestamp = LocalDateTime.now();
    }
    
    /**
     * Get summary statistics across all simulations
     */
    public String getSummary() {
        if (isEmpty()) {
            return "No simulations in report";
        }
        
        int totalTurns = 0;
        int totalCreatures = 0;
        int extinctions = 0;
        
        for (SimulationResult result : results) {
            totalTurns += result.getFinalStats().getTurn();
            totalCreatures += result.getFinalStats().getTotalCreatures();
            if (result.getExtinctionTurn() > 0) {
                extinctions++;
            }
        }
        
        return String.format("Total Simulations: %d\nAvg Turns: %.1f\nExtinctions: %d",
                           results.size(),
                           (double) totalTurns / results.size(),
                           extinctions);
    }
}
