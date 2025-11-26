package com.ecosimulator.model;

/**
 * Defines the three simulation scenarios
 */
public enum Scenario {
    BALANCED("Equilibrado", "Equal distribution of predators and prey"),
    PREDATOR_DOMINANT("Depredadores Dominantes", "More predators than prey"),
    PREY_DOMINANT("Presas Dominantes", "More prey than predators");

    private final String displayName;
    private final String description;

    Scenario(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
