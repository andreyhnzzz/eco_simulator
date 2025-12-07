package com.ecosimulator.model;

/**
 * Types of mutations that can occur in creatures.
 * Each mutation type provides different advantages.
 */
public enum MutationType {
    /**
     * No mutation - standard creature
     */
    NONE("Ninguna", "Sin mutación", 1.0),
    
    /**
     * Enhanced metabolism - reduces hunger/thirst rates
     */
    EFFICIENT_METABOLISM("Metabolismo Eficiente", "Resistencia al hambre y sed", 1.3),
    
    /**
     * Enhanced strength - more energy from food
     */
    ENHANCED_STRENGTH("Fuerza Mejorada", "Mayor ganancia de energía al comer", 1.5),
    
    /**
     * Thermal resistance - better survival in extreme conditions
     */
    THERMAL_RESISTANCE("Resistencia Térmica", "Resistencia a condiciones extremas", 1.4);
    
    private final String displayName;
    private final String description;
    private final double bonus;
    
    MutationType(String displayName, String description, double bonus) {
        this.displayName = displayName;
        this.description = description;
        this.bonus = bonus;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public double getBonus() {
        return bonus;
    }
    
    /**
     * Get a random mutation type (excluding NONE)
     */
    public static MutationType getRandomMutation() {
        MutationType[] mutations = {EFFICIENT_METABOLISM, ENHANCED_STRENGTH, THERMAL_RESISTANCE};
        return mutations[(int) (Math.random() * mutations.length)];
    }
    
    @Override
    public String toString() {
        return displayName + " (" + description + ")";
    }
}
