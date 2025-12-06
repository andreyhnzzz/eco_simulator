package com.ecosimulator.model;

/**
 * Represents a corpse in the simulation - the remains of a dead creature.
 * Corpses can be consumed by scavengers or will decay after a timeout.
 */
public class Corpse {
    private static final int DEFAULT_DECAY_TIME = 10; // Turns until corpse decays
    
    private final long originalCreatureId;
    private final CellType originalType;
    private final Sex originalSex;
    private int row;
    private int col;
    private int decayTimer;
    private int nutritionalValue;

    /**
     * Creates a corpse from a dead creature
     */
    public Corpse(Creature deadCreature) {
        this.originalCreatureId = deadCreature.getId();
        this.originalType = deadCreature.getType();
        this.originalSex = deadCreature.getSex();
        this.row = deadCreature.getRow();
        this.col = deadCreature.getCol();
        this.decayTimer = DEFAULT_DECAY_TIME;
        this.nutritionalValue = calculateNutritionalValue(deadCreature);
    }

    /**
     * Calculate nutritional value based on the creature's type
     */
    private int calculateNutritionalValue(Creature creature) {
        return switch (creature.getType()) {
            case PREDATOR -> 8;  // Predator corpses are more nutritious
            case PREY -> 5;
            case THIRD_SPECIES -> 4;
            default -> 3;
        };
    }

    /**
     * Process one turn of decay
     * @return true if corpse has fully decayed
     */
    public boolean decay() {
        decayTimer--;
        return decayTimer <= 0;
    }

    /**
     * Consume the corpse and return the energy gained
     */
    public int consume() {
        int value = nutritionalValue;
        nutritionalValue = 0;
        decayTimer = 0;
        return value;
    }

    /**
     * Check if corpse has fully decayed
     */
    public boolean isDecayed() {
        return decayTimer <= 0;
    }

    // Getters
    public long getOriginalCreatureId() { return originalCreatureId; }
    public CellType getOriginalType() { return originalType; }
    public Sex getOriginalSex() { return originalSex; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getDecayTimer() { return decayTimer; }
    public int getNutritionalValue() { return nutritionalValue; }

    /**
     * Get position key for corpse map lookup
     */
    public String getPositionKey() {
        return row + "," + col;
    }

    /**
     * Get a human-readable identifier for the corpse
     */
    public String getIdString() {
        return "Corpse-" + originalCreatureId + "(" + originalType.getSymbol() + ")";
    }

    @Override
    public String toString() {
        return String.format("Corpse[%s %s-%d at (%d,%d) decay:%d value:%d]",
            originalType.getDisplayName(), originalSex.getSymbol(), originalCreatureId,
            row, col, decayTimer, nutritionalValue);
    }
}
