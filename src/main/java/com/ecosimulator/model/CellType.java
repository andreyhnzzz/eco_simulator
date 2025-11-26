package com.ecosimulator.model;

/**
 * Represents the type of cell in the simulation grid
 */
public enum CellType {
    EMPTY("Empty", " ", "#2E7D32"),      // Forest green background
    PREDATOR("Predator", "P", "#D32F2F"), // Red
    PREY("Prey", "R", "#1976D2"),          // Blue (Prey/Rabbit)
    THIRD_SPECIES("Third Species", "T", "#FF9800"); // Orange

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
}
