package com.ecosimulator.model;

/**
 * Represents a creature in the simulation with position, energy, and mutation status
 */
public class Creature {
    private int row;
    private int col;
    private CellType type;
    private int energy;
    private boolean mutated;
    private int age;
    private double mutationBonus; // Speed/strength bonus from mutation

    public Creature(CellType type, int row, int col) {
        this.type = type;
        this.row = row;
        this.col = col;
        this.energy = getInitialEnergy(type);
        this.mutated = false;
        this.age = 0;
        this.mutationBonus = 1.0;
    }

    private int getInitialEnergy(CellType type) {
        return switch (type) {
            case PREDATOR -> 15;  // Reduced from 20
            case PREY -> 10;
            case THIRD_SPECIES -> 12;
            default -> 0;
        };
    }

    public void move(int newRow, int newCol) {
        this.row = newRow;
        this.col = newCol;
        this.energy -= 1;
    }

    public void eat(int energyGain) {
        this.energy += energyGain;
    }

    public void age() {
        this.age++;
        this.energy -= 1;
    }

    public boolean isDead() {
        return energy <= 0;
    }

    public boolean canReproduce() {
        int threshold = switch (type) {
            case PREDATOR -> 25;  // Reduced from 30 to balance with lower energy gain
            case PREY -> 12;      // Reduced from 15 for faster prey reproduction
            case THIRD_SPECIES -> 18;
            default -> Integer.MAX_VALUE;
        };
        return energy >= threshold;
    }

    public void reproduce() {
        this.energy /= 2;
    }

    public void mutate() {
        this.mutated = true;
        this.mutationBonus = 1.5; // 50% bonus to efficiency
    }

    // Getters and setters
    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }

    public CellType getType() { return type; }
    public void setType(CellType type) { this.type = type; }

    public int getEnergy() { return energy; }
    public void setEnergy(int energy) { this.energy = energy; }

    public boolean isMutated() { return mutated; }
    public void setMutated(boolean mutated) { this.mutated = mutated; }

    public int getAge() { return age; }

    public double getMutationBonus() { return mutationBonus; }

    @Override
    public String toString() {
        return String.format("%s at (%d,%d) E:%d %s", 
            type.getDisplayName(), row, col, energy, mutated ? "[M]" : "");
    }
}
