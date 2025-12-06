package com.ecosimulator.service;

import com.ecosimulator.model.CellType;
import com.ecosimulator.model.Creature;
import com.ecosimulator.model.Sex;

import java.util.List;
import java.util.Random;

/**
 * Manages reproduction logic for creatures in the simulation.
 * Enforces rules for sex-based mating:
 * - Only male + female of same species can mate
 * - Both must be mature
 * - Both must have sufficient energy
 * - Both must not be in mating cooldown
 * - Must be within proximity threshold
 */
public class ReproductionManager {
    
    private static final Random RANDOM = new Random();
    
    // Proximity threshold for mating (cells distance)
    private static final int MATING_PROXIMITY = 1;
    
    private final EventLogger eventLogger;
    private int currentTurn;

    public ReproductionManager(EventLogger eventLogger) {
        this.eventLogger = eventLogger;
        this.currentTurn = 0;
    }

    /**
     * Set the current turn for logging purposes
     */
    public void setCurrentTurn(int turn) {
        this.currentTurn = turn;
    }

    /**
     * Check if two creatures can mate
     * @return null if they can mate, or a rejection reason string if not
     */
    public String canMate(Creature creature1, Creature creature2) {
        // Same creature check
        if (creature1.getId() == creature2.getId()) {
            return "same creature";
        }

        // Same species check
        if (creature1.getType() != creature2.getType()) {
            return "different species";
        }

        // Sex check - must be opposite sexes
        if (creature1.getSex() == creature2.getSex()) {
            return "same sex";
        }

        // Maturity check
        if (!creature1.isMature()) {
            return creature1.getIdString() + " is immature";
        }
        if (!creature2.isMature()) {
            return creature2.getIdString() + " is immature";
        }

        // Energy check
        if (!creature1.canReproduce()) {
            return creature1.getIdString() + " has insufficient energy";
        }
        if (!creature2.canReproduce()) {
            return creature2.getIdString() + " has insufficient energy";
        }

        // Mating cooldown check
        if (!creature1.canMate()) {
            return creature1.getIdString() + " is in mating cooldown";
        }
        if (!creature2.canMate()) {
            return creature2.getIdString() + " is in mating cooldown";
        }

        // Both creatures are alive check
        if (creature1.isDead() || creature2.isDead()) {
            return "one or both creatures are dead";
        }

        // Proximity check
        if (!isWithinProximity(creature1, creature2)) {
            return "not within proximity";
        }

        return null; // Can mate
    }

    /**
     * Check if two creatures are within mating proximity
     */
    private boolean isWithinProximity(Creature c1, Creature c2) {
        int rowDiff = Math.abs(c1.getRow() - c2.getRow());
        int colDiff = Math.abs(c1.getCol() - c2.getCol());
        return rowDiff <= MATING_PROXIMITY && colDiff <= MATING_PROXIMITY;
    }

    /**
     * Find a suitable mate for a creature from a list of candidates
     * @param seeker The creature looking for a mate
     * @param candidates List of potential mates
     * @return A suitable mate, or null if none found
     */
    public Creature findMate(Creature seeker, List<Creature> candidates) {
        for (Creature candidate : candidates) {
            String rejectionReason = canMate(seeker, candidate);
            if (rejectionReason == null) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Attempt to reproduce between two creatures
     * @param parent1 First parent
     * @param parent2 Second parent
     * @param offspringRow Row position for offspring
     * @param offspringCol Column position for offspring
     * @param inheritMutation Whether the offspring can inherit mutation
     * @return The offspring creature, or null if reproduction failed
     */
    public Creature reproduce(Creature parent1, Creature parent2, 
                              int offspringRow, int offspringCol, boolean inheritMutation) {
        // Validate mating
        String rejectionReason = canMate(parent1, parent2);
        if (rejectionReason != null) {
            if (eventLogger != null) {
                eventLogger.logMatingRejected(currentTurn, parent1, parent2, rejectionReason);
            }
            return null;
        }

        // Randomly assign sex to offspring (50/50)
        Sex offspringSex = RANDOM.nextBoolean() ? Sex.MALE : Sex.FEMALE;

        // Create offspring
        Creature offspring = new Creature(parent1.getType(), offspringRow, offspringCol, offspringSex);

        // Handle mutation inheritance
        if (inheritMutation && (parent1.isMutated() || parent2.isMutated())) {
            // 70% chance to inherit mutation if either parent is mutated
            if (RANDOM.nextDouble() < 0.7) {
                offspring.mutate();
            }
        }

        // Both parents consume energy and start cooldown
        parent1.reproduce();
        parent1.startMatingCooldown();
        parent2.reproduce();
        parent2.startMatingCooldown();

        // Log the events
        if (eventLogger != null) {
            eventLogger.logMatingSuccess(currentTurn, parent1, parent2, offspring);
            eventLogger.logBirth(currentTurn, offspring, parent1, parent2);
        }

        return offspring;
    }

    /**
     * Check if a creature should prioritize seeking a mate
     * Based on energy level and whether eligible mates exist nearby
     */
    public boolean shouldSeekMate(Creature creature) {
        // Must be mature and not in cooldown
        if (!creature.isMature() || !creature.canMate()) {
            return false;
        }

        // Must have enough energy to reproduce
        if (!creature.canReproduce()) {
            return false;
        }

        // Scavengers prioritize corpses over mating
        if (creature.getType() == CellType.THIRD_SPECIES) {
            return false; // Scavengers handle mating separately
        }

        return true;
    }

    /**
     * Get the preferred direction to move towards a potential mate
     * @return int array [rowDelta, colDelta] or null if no direction preferred
     */
    public int[] getDirectionTowardsMate(Creature seeker, List<Creature> potentialMates) {
        Creature nearestMate = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Creature candidate : potentialMates) {
            // Quick checks without full validation
            if (candidate.getType() != seeker.getType()) continue;
            if (candidate.getSex() == seeker.getSex()) continue;
            if (!candidate.isMature() || !candidate.canMate()) continue;

            double distance = calculateDistance(seeker, candidate);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestMate = candidate;
            }
        }

        if (nearestMate == null) {
            return null;
        }

        // Calculate direction
        int rowDelta = Integer.compare(nearestMate.getRow(), seeker.getRow());
        int colDelta = Integer.compare(nearestMate.getCol(), seeker.getCol());

        return new int[]{rowDelta, colDelta};
    }

    private double calculateDistance(Creature c1, Creature c2) {
        int rowDiff = c1.getRow() - c2.getRow();
        int colDiff = c1.getCol() - c2.getCol();
        return Math.sqrt(rowDiff * rowDiff + colDiff * colDiff);
    }
}
