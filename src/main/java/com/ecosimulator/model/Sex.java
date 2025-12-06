package com.ecosimulator.model;

/**
 * Represents the biological sex of a creature in the simulation.
 * Sex is assigned at birth or initialization and cannot change.
 */
public enum Sex {
    MALE("Male", "M", "♂"),
    FEMALE("Female", "F", "♀");

    private final String displayName;
    private final String symbol;
    private final String unicodeSymbol;

    Sex(String displayName, String symbol, String unicodeSymbol) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.unicodeSymbol = unicodeSymbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getUnicodeSymbol() {
        return unicodeSymbol;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
