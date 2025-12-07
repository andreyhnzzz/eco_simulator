package com.ecosimulator.model;

/**
 * Configuration settings for the simulation
 */
public class SimulationConfig {
    private Scenario scenario;
    private boolean thirdSpeciesEnabled;
    private boolean mutationsEnabled;
    private int gridSize;
    private int turnDelayMs; // Milliseconds between turns
    private int maxTurns;

    // Default configuration
    public SimulationConfig() {
        this.scenario = Scenario.BALANCED;
        this.thirdSpeciesEnabled = false;
        this.mutationsEnabled = false;
        this.gridSize = 20;
        this.turnDelayMs = 1000; // 1 second between turns (slower pace)
        this.maxTurns = 200;
    }

    // Copy constructor
    public SimulationConfig(SimulationConfig other) {
        this.scenario = other.scenario;
        this.thirdSpeciesEnabled = other.thirdSpeciesEnabled;
        this.mutationsEnabled = other.mutationsEnabled;
        this.gridSize = other.gridSize;
        this.turnDelayMs = other.turnDelayMs;
        this.maxTurns = other.maxTurns;
    }

    // Builder-style setters
    public SimulationConfig withScenario(Scenario scenario) {
        this.scenario = scenario;
        return this;
    }

    public SimulationConfig withThirdSpecies(boolean enabled) {
        this.thirdSpeciesEnabled = enabled;
        return this;
    }

    public SimulationConfig withMutations(boolean enabled) {
        this.mutationsEnabled = enabled;
        return this;
    }

    public SimulationConfig withGridSize(int size) {
        this.gridSize = size;
        return this;
    }

    public SimulationConfig withTurnDelay(int delayMs) {
        this.turnDelayMs = delayMs;
        return this;
    }

    public SimulationConfig withMaxTurns(int maxTurns) {
        this.maxTurns = maxTurns;
        return this;
    }

    // Getters
    public Scenario getScenario() { return scenario; }
    public boolean isThirdSpeciesEnabled() { return thirdSpeciesEnabled; }
    public boolean isMutationsEnabled() { return mutationsEnabled; }
    public int getGridSize() { return gridSize; }
    public int getTurnDelayMs() { return turnDelayMs; }
    public int getMaxTurns() { return maxTurns; }

    // Setters
    public void setScenario(Scenario scenario) { this.scenario = scenario; }
    public void setThirdSpeciesEnabled(boolean enabled) { this.thirdSpeciesEnabled = enabled; }
    public void setMutationsEnabled(boolean enabled) { this.mutationsEnabled = enabled; }
    public void setGridSize(int gridSize) { this.gridSize = gridSize; }
    public void setTurnDelayMs(int turnDelayMs) { this.turnDelayMs = turnDelayMs; }
    public void setMaxTurns(int maxTurns) { this.maxTurns = maxTurns; }

    /**
     * Get initial predator percentage based on scenario
     */
    public double getPredatorPercentage() {
        return switch (scenario) {
            case BALANCED -> 0.10;           // 10% predators
            case PREDATOR_DOMINANT -> 0.15;  // 15% predators (reduced from 20%)
            case PREY_DOMINANT -> 0.05;      // 5% predators
        };
    }

    /**
     * Get initial prey percentage based on scenario
     */
    public double getPreyPercentage() {
        return switch (scenario) {
            case BALANCED -> 0.20;           // 20% prey
            case PREDATOR_DOMINANT -> 0.10;  // 10% prey
            case PREY_DOMINANT -> 0.25;      // 25% prey (reduced from 30%)
        };
    }

    /**
     * Get initial third species percentage (if enabled)
     */
    public double getThirdSpeciesPercentage() {
        if (!thirdSpeciesEnabled) return 0.0;
        return switch (scenario) {
            case BALANCED -> 0.10;
            case PREDATOR_DOMINANT -> 0.08;
            case PREY_DOMINANT -> 0.12;
        };
    }

    @Override
    public String toString() {
        return String.format("Config[%s, thirdSpecies=%b, mutations=%b, grid=%dx%d, delay=%dms]",
            scenario.getDisplayName(), thirdSpeciesEnabled, mutationsEnabled, 
            gridSize, gridSize, turnDelayMs);
    }
}
