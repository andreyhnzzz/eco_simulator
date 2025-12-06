package com.ecosimulator.model;

/**
 * Represents the type of cell in the simulation grid
 */
public enum CellType {
    EMPTY("Empty", " ", "#2E7D32"),           // Forest green background
    PREDATOR("Predator", "P", "#D32F2F"),     // Red
    PREY("Prey", "R", "#1976D2"),              // Blue (Prey/Rabbit)
    THIRD_SPECIES("Scavenger", "S", "#FF9800"), // Orange (renamed from Third Species)
    CORPSE("Corpse", "X", "#4A148C"),         // Dark purple for corpses
    WATER("Water", "W", "#2196F3"),           // Blue for water
    FOOD("Food", "F", "#8BC34A");             // Light green for food/vegetation

    private final String displayName;
    private final String symbol;
    private final String color;

    CellType(String displayName, String symbol, String color) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getColor() {
        return color;
    }

    /**
     * Check if this cell type represents a living creature
     */
    public boolean isLiving() {
        return this == PREDATOR || this == PREY || this == THIRD_SPECIES;
    }

    /**
     * Check if this cell type represents a resource (water or food)
     */
    public boolean isResource() {
        return this == WATER || this == FOOD;
    }
}
