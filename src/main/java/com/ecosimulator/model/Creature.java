package com.ecosimulator.model;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a creature in the simulation with position, energy, mutation status, and sex.
 * Sex is an immutable property assigned at birth/initialization.
 */
public class Creature {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    private static final Random RANDOM = new Random();
    
    // Maturity age threshold (turns required to become mature)
    private static final int MATURITY_AGE = 5;
    
    // Mating cooldown period (turns after mating before can mate again)
    private static final int MATING_COOLDOWN = 3;
    
    private final long id;
    private int row;
    private int col;
    private CellType type;
    private int energy;
    private boolean mutated;
    private int age;
    private double mutationBonus; // Speed/strength bonus from mutation
    private final Sex sex; // Immutable sex attribute
    private int matingCooldown; // Turns remaining before can mate again
    private int hunger; // Hunger level (0-100, dies at 100)
    private int thirst; // Thirst level (0-100, dies at 100)

    /**
     * Creates a new creature with randomly assigned sex
     */
    public Creature(CellType type, int row, int col) {
        this(type, row, col, RANDOM.nextBoolean() ? Sex.MALE : Sex.FEMALE);
    }

    /**
     * Creates a new creature with specified sex
     */
    public Creature(CellType type, int row, int col, Sex sex) {
        this.id = ID_GENERATOR.getAndIncrement();
        this.type = type;
        this.row = row;
        this.col = col;
        this.energy = getInitialEnergy(type);
        this.mutated = false;
        this.age = 0;
        this.mutationBonus = 1.0;
        this.sex = sex;
        this.matingCooldown = 0;
        this.hunger = 0; // Start with no hunger
        this.thirst = 0; // Start with no thirst
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
        // Increase hunger and thirst each turn
        this.hunger += getHungerRate();
        this.thirst += getThirstRate();
        // Cap at 100
        if (this.hunger > 100) this.hunger = 100;
        if (this.thirst > 100) this.thirst = 100;
        // Decrease mating cooldown
        if (matingCooldown > 0) {
            matingCooldown--;
        }
    }

    private int getHungerRate() {
        // Predators get hungrier faster
        return switch (type) {
            case PREDATOR -> 15;
            case PREY -> 10;
            case THIRD_SPECIES -> 12;
            default -> 10;
        };
    }

    private int getThirstRate() {
        // All creatures get thirsty at similar rates
        return switch (type) {
            case PREDATOR -> 12;
            case PREY -> 10;
            case THIRD_SPECIES -> 10;
            default -> 10;
        };
    }

    public boolean isDead() {
        return energy <= 0 || hunger >= 100 || thirst >= 100;
    }

    /**
     * Check if creature died from thirst
     */
    public boolean isDyingFromThirst() {
        return thirst >= 100;
    }

    /**
     * Check if creature died from hunger
     */
    public boolean isDyingFromHunger() {
        return hunger >= 100;
    }

    /**
     * Drink water to reduce thirst
     */
    public void drink() {
        this.thirst = Math.max(0, this.thirst - 50);
    }

    /**
     * Eat food to reduce hunger (for prey eating vegetation)
     */
    public void eatFood() {
        this.hunger = Math.max(0, this.hunger - 40);
    }

    /**
     * Check if creature is mature (old enough to reproduce)
     */
    public boolean isMature() {
        return age >= MATURITY_AGE;
    }

    /**
     * Check if creature can mate (considering cooldown)
     */
    public boolean canMate() {
        return matingCooldown == 0;
    }

    /**
     * Start mating cooldown after successful reproduction
     */
    public void startMatingCooldown() {
        this.matingCooldown = MATING_COOLDOWN;
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
    public long getId() { return id; }

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

    public Sex getSex() { return sex; }

    public int getMatingCooldown() { return matingCooldown; }

    public int getHunger() { return hunger; }
    public void setHunger(int hunger) { this.hunger = hunger; }

    public int getThirst() { return thirst; }
    public void setThirst(int thirst) { this.thirst = thirst; }

    /**
     * Get the unique identifier string (e.g., "M-103" or "F-45")
     */
    public String getIdString() {
        return sex.getSymbol() + "-" + id;
    }

    @Override
    public String toString() {
        return String.format("%s %s-%d at (%d,%d) E:%d %s", 
            type.getDisplayName(), sex.getSymbol(), id, row, col, energy, mutated ? "[M]" : "");
    }
}
