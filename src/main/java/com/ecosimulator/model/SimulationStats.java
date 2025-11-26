package com.ecosimulator.model;

/**
 * Statistics for the current simulation state
 */
public class SimulationStats {
    private int turn;
    private int predatorCount;
    private int preyCount;
    private int thirdSpeciesCount;
    private int mutatedCount;
    private int birthsThisTurn;
    private int deathsThisTurn;

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
            return "Preys win - Predators extinct";
        }
        if (preyCount == 0) {
            return "Predators win - Preys extinct";
        }
        return "Ongoing";
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

    @Override
    public String toString() {
        return String.format("Turn %d: P=%d, R=%d, T=%d, M=%d", 
            turn, predatorCount, preyCount, thirdSpeciesCount, mutatedCount);
    }
}
