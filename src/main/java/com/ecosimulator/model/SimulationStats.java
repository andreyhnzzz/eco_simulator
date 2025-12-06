package com.ecosimulator.model;

/**
 * Statistics for the current simulation state including sex ratios
 */
public class SimulationStats {
    private int turn;
    private int predatorCount;
    private int preyCount;
    private int thirdSpeciesCount;
    private int mutatedCount;
    private int birthsThisTurn;
    private int deathsThisTurn;
    private int corpseCount;
    private int waterCount;
    private int foodCount;
    private int totalWaterConsumed;
    private int totalFoodConsumed;
    
    // Sex counts per species
    private int predatorMaleCount;
    private int predatorFemaleCount;
    private int preyMaleCount;
    private int preyFemaleCount;
    private int thirdSpeciesMaleCount;
    private int thirdSpeciesFemaleCount;
    
    // Mutation status per species
    private boolean predatorHasMutations;
    private boolean preyHasMutations;
    private boolean thirdSpeciesHasMutations;

    public SimulationStats() {
        reset();
    }

    public void reset() {
        this.turn = 0;
        this.predatorCount = 0;
        this.preyCount = 0;
        this.thirdSpeciesCount = 0;
        this.mutatedCount = 0;
        this.birthsThisTurn = 0;
        this.deathsThisTurn = 0;
        this.corpseCount = 0;
        this.waterCount = 0;
        this.foodCount = 0;
        this.totalWaterConsumed = 0;
        this.totalFoodConsumed = 0;
        
        // Reset sex counts
        this.predatorMaleCount = 0;
        this.predatorFemaleCount = 0;
        this.preyMaleCount = 0;
        this.preyFemaleCount = 0;
        this.thirdSpeciesMaleCount = 0;
        this.thirdSpeciesFemaleCount = 0;
        
        // Reset mutation status
        this.predatorHasMutations = false;
        this.preyHasMutations = false;
        this.thirdSpeciesHasMutations = false;
    }

    public void nextTurn() {
        this.turn++;
        this.birthsThisTurn = 0;
        this.deathsThisTurn = 0;
    }

    public void recordBirth() {
        this.birthsThisTurn++;
    }

    public void recordDeath() {
        this.deathsThisTurn++;
    }

    public int getTotalCreatures() {
        return predatorCount + preyCount + thirdSpeciesCount;
    }

    public boolean isExtinct() {
        return predatorCount == 0 || preyCount == 0;
    }

    public String getWinner() {
        if (predatorCount == 0 && preyCount == 0) {
            return "Extinction - No survivors";
        }
        if (predatorCount == 0) {
            return "Prey win - Predators extinct";
        }
        if (preyCount == 0) {
            return "Predators win - Prey extinct";
        }
        return "Ongoing";
    }

    /**
     * Calculate dominance index for a species (0.0 to 1.0)
     * Based on population, birth rate relative to total
     */
    public double getDominanceIndex(CellType species) {
        int total = getTotalCreatures();
        if (total == 0) return 0.0;
        
        int count = switch (species) {
            case PREDATOR -> predatorCount;
            case PREY -> preyCount;
            case THIRD_SPECIES -> thirdSpeciesCount;
            default -> 0;
        };
        
        return (double) count / total;
    }

    /**
     * Get sex ratio for a species (male / total)
     * Returns 0.5 if no creatures of that species
     */
    public double getSexRatio(CellType species) {
        int males;
        int total;
        switch (species) {
            case PREDATOR -> {
                males = predatorMaleCount;
                total = predatorCount;
            }
            case PREY -> {
                males = preyMaleCount;
                total = preyCount;
            }
            case THIRD_SPECIES -> {
                males = thirdSpeciesMaleCount;
                total = thirdSpeciesCount;
            }
            default -> {
                return 0.5;
            }
        }
        return total > 0 ? (double) males / total : 0.5;
    }

    // Getters and Setters
    public int getTurn() { return turn; }
    public void setTurn(int turn) { this.turn = turn; }

    public int getPredatorCount() { return predatorCount; }
    public void setPredatorCount(int count) { this.predatorCount = count; }

    public int getPreyCount() { return preyCount; }
    public void setPreyCount(int count) { this.preyCount = count; }

    public int getThirdSpeciesCount() { return thirdSpeciesCount; }
    public void setThirdSpeciesCount(int count) { this.thirdSpeciesCount = count; }

    public int getMutatedCount() { return mutatedCount; }
    public void setMutatedCount(int count) { this.mutatedCount = count; }

    public int getBirthsThisTurn() { return birthsThisTurn; }
    public int getDeathsThisTurn() { return deathsThisTurn; }

    public int getCorpseCount() { return corpseCount; }
    public void setCorpseCount(int count) { this.corpseCount = count; }

    public int getWaterCount() { return waterCount; }
    public void setWaterCount(int count) { this.waterCount = count; }

    public int getFoodCount() { return foodCount; }
    public void setFoodCount(int count) { this.foodCount = count; }

    public int getTotalWaterConsumed() { return totalWaterConsumed; }
    public void incrementWaterConsumed() { this.totalWaterConsumed++; }

    public int getTotalFoodConsumed() { return totalFoodConsumed; }
    public void incrementFoodConsumed() { this.totalFoodConsumed++; }

    // Sex count getters and setters
    public int getPredatorMaleCount() { return predatorMaleCount; }
    public void setPredatorMaleCount(int count) { this.predatorMaleCount = count; }
    
    public int getPredatorFemaleCount() { return predatorFemaleCount; }
    public void setPredatorFemaleCount(int count) { this.predatorFemaleCount = count; }
    
    public int getPreyMaleCount() { return preyMaleCount; }
    public void setPreyMaleCount(int count) { this.preyMaleCount = count; }
    
    public int getPreyFemaleCount() { return preyFemaleCount; }
    public void setPreyFemaleCount(int count) { this.preyFemaleCount = count; }
    
    public int getThirdSpeciesMaleCount() { return thirdSpeciesMaleCount; }
    public void setThirdSpeciesMaleCount(int count) { this.thirdSpeciesMaleCount = count; }
    
    public int getThirdSpeciesFemaleCount() { return thirdSpeciesFemaleCount; }
    public void setThirdSpeciesFemaleCount(int count) { this.thirdSpeciesFemaleCount = count; }

    // Mutation status getters and setters
    public boolean isPredatorHasMutations() { return predatorHasMutations; }
    public void setPredatorHasMutations(boolean hasMutations) { this.predatorHasMutations = hasMutations; }
    
    public boolean isPreyHasMutations() { return preyHasMutations; }
    public void setPreyHasMutations(boolean hasMutations) { this.preyHasMutations = hasMutations; }
    
    public boolean isThirdSpeciesHasMutations() { return thirdSpeciesHasMutations; }
    public void setThirdSpeciesHasMutations(boolean hasMutations) { this.thirdSpeciesHasMutations = hasMutations; }

    @Override
    public String toString() {
        return String.format("Turn %d: P=%d(M:%d/F:%d), R=%d(M:%d/F:%d), S=%d(M:%d/F:%d), Mut=%d, Corpses=%d", 
            turn, 
            predatorCount, predatorMaleCount, predatorFemaleCount,
            preyCount, preyMaleCount, preyFemaleCount,
            thirdSpeciesCount, thirdSpeciesMaleCount, thirdSpeciesFemaleCount,
            mutatedCount, corpseCount);
    }
}
