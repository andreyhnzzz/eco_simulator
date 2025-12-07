package com.ecosimulator.model;

import java.time.LocalDateTime;

/**
 * Stores the results of a single simulation run.
 * Used for tracking multiple consecutive simulations.
 */
public class SimulationResult {
    private final int simulationNumber;
    private final Scenario scenario;
    private final boolean hasThirdSpecies;
    private final boolean hasMutations;
    private final int gridSize;
    private final SimulationStats finalStats;
    private final int extinctionTurn;
    private final LocalDateTime timestamp;
    
    public SimulationResult(int simulationNumber, Scenario scenario, 
                           boolean hasThirdSpecies, boolean hasMutations,
                           int gridSize, SimulationStats finalStats, 
                           int extinctionTurn) {
        this.simulationNumber = simulationNumber;
        this.scenario = scenario;
        this.hasThirdSpecies = hasThirdSpecies;
        this.hasMutations = hasMutations;
        this.gridSize = gridSize;
        this.finalStats = finalStats;
        this.extinctionTurn = extinctionTurn;
        this.timestamp = LocalDateTime.now();
    }
    
    public int getSimulationNumber() {
        return simulationNumber;
    }
    
    public Scenario getScenario() {
        return scenario;
    }
    
    public boolean hasThirdSpecies() {
        return hasThirdSpecies;
    }
    
    public boolean hasMutations() {
        return hasMutations;
    }
    
    public int getGridSize() {
        return gridSize;
    }
    
    public SimulationStats getFinalStats() {
        return finalStats;
    }
    
    public int getExtinctionTurn() {
        return extinctionTurn;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getConfigDescription() {
        StringBuilder desc = new StringBuilder(scenario.getDisplayName());
        if (hasThirdSpecies) {
            desc.append(" + Tercera Especie");
        }
        if (hasMutations) {
            desc.append(" + Mutaciones");
        }
        return desc.toString();
    }
}
