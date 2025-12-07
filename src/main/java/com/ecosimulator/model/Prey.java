package com.ecosimulator.model;

/**
 * Prey animal in the ecosystem
 * Moves to empty cells and reproduces after surviving 2 turns
 */
public class Prey extends Animal {
    private static final int INITIAL_ENERGY = 10;
    private static final int REPRODUCTION_THRESHOLD = 12;
    private int turnsSurvived;
    
    public Prey(int x, int y) {
        super(x, y);
        this.turnsSurvived = 0;
    }
    
    @Override
    protected int getInitialEnergy() {
        return INITIAL_ENERGY;
    }
    
    @Override
    public boolean canReproduce() {
        return energy >= REPRODUCTION_THRESHOLD && turnsSurvived >= 2;
    }
    
    @Override
    public String getType() {
        return "prey";
    }
    
    @Override
    public void age() {
        super.age();
        turnsSurvived++;
    }
    
    public int getTurnsSurvived() {
        return turnsSurvived;
    }
}
