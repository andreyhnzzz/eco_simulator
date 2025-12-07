package com.ecosimulator.model;

/**
 * Abstract base class for all animals in the ecosystem
 */
public abstract class Animal {
    protected int x;
    protected int y;
    protected int energy;
    protected int lastTurnEated;
    
    public Animal(int x, int y) {
        this.x = x;
        this.y = y;
        this.energy = getInitialEnergy();
        this.lastTurnEated = 0;
    }
    
    /**
     * Get the initial energy for this animal type
     * @return initial energy value
     */
    protected abstract int getInitialEnergy();
    
    /**
     * Move the animal to a new position
     * @param newX new X coordinate
     * @param newY new Y coordinate
     */
    public void move(int newX, int newY) {
        this.x = newX;
        this.y = newY;
        this.energy--;  // Movement costs energy
    }
    
    /**
     * Feed the animal (increases energy)
     * @param amount energy to add
     * @param currentTurn current turn number
     */
    public void eat(int amount, int currentTurn) {
        this.energy += amount;
        this.lastTurnEated = currentTurn;
    }
    
    /**
     * Age the animal (called each turn)
     */
    public void age() {
        this.energy--;
    }
    
    /**
     * Check if animal is dead
     * @return true if energy is 0 or less
     */
    public boolean isDead() {
        return energy <= 0;
    }
    
    /**
     * Check if animal can reproduce
     * @return true if has enough energy
     */
    public abstract boolean canReproduce();
    
    /**
     * Reproduce (costs energy)
     */
    public void reproduce() {
        this.energy /= 2;
    }
    
    /**
     * Get the type name of this animal
     * @return type name (e.g., "prey", "predator", "third")
     */
    public abstract String getType();
    
    // Getters and setters
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int getEnergy() {
        return energy;
    }
    
    public void setEnergy(int energy) {
        this.energy = energy;
    }
    
    public int getLastTurnEated() {
        return lastTurnEated;
    }
    
    public void setLastTurnEated(int lastTurnEated) {
        this.lastTurnEated = lastTurnEated;
    }
}
