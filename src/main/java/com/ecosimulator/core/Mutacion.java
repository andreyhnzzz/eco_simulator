package com.ecosimulator.core;

/**
 * Enumeration of possible mutations that can affect species behavior.
 */
public enum Mutacion {
    /**
     * Species can move 2 cells per turn instead of 1.
     */
    VELOCIDAD,
    
    /**
     * Prey reproduces every 1 turn (instead of 2).
     * Predators can reproduce without needing to eat.
     */
    FERTILIDAD,
    
    /**
     * Predators survive 5 turns without eating (instead of 3).
     */
    RESISTENCIA_HAMBRE
}
