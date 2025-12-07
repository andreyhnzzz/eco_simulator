package com.ecosimulator.model;

/**
 * Predator animal in the ecosystem
 * Hunts prey and dies if doesn't eat for 3 turns
 */
public class Predator extends Animal {
    private static final int INITIAL_ENERGY = 15;
    private static final int REPRODUCTION_THRESHOLD = 25;
    private static final int STARVATION_TURNS = 3;
    
    public Predator(int x, int y) {
        super(x, y);
    }
    
    @Override
    protected int getInitialEnergy() {
        return INITIAL_ENERGY;
    }
    
    @Override
    public boolean canReproduce() {
        return energy >= REPRODUCTION_THRESHOLD;
    }
    
    @Override
    public String getType() {
        return "predator";
    }
    
    /**
     * Check if predator is starving (hasn't eaten in 3 turns)
     * @param currentTurn current turn number
     * @return true if starving
     */
    public boolean isStarving(int currentTurn) {
        return (currentTurn - lastTurnEated) >= STARVATION_TURNS;
    }
    
    /**
     * Check if predator has eaten recently (within last 3 turns)
     * @param currentTurn current turn number
     * @return true if has eaten recently
     */
    public boolean hasEatenRecently(int currentTurn) {
        return (currentTurn - lastTurnEated) < STARVATION_TURNS;
    }
}
