package com.ecosimulator.service;

import com.ecosimulator.model.Corpse;
import com.ecosimulator.model.Creature;
import com.ecosimulator.model.Sex;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Event logging service for the simulation.
 * Records mating attempts, births, deaths, scavenging events, and mutations.
 * All entries are structured, timestamped, and human-readable.
 */
public class EventLogger {
    
    public enum EventType {
        MATING_SUCCESS("💕 Mating"),
        MATING_REJECTED("❌ Mating Rejected"),
        BIRTH("🐣 Birth"),
        DEATH_PREDATION("💀 Predation"),
        DEATH_STARVATION("💀 Starvation"),
        DEATH_OLD_AGE("💀 Old Age"),
        DEATH_THIRST("💀 Thirst"),
        DEATH_HUNGER("💀 Hunger"),
        SCAVENGING("🦴 Scavenging"),
        MUTATION_ACTIVATED("🧬 Mutation"),
        CORPSE_DECAY("💨 Decay"),
        WATER_CONSUMED("💧 Drink"),
        FOOD_CONSUMED("🍃 Eat"),
        MOVEMENT("🚶 Move");

        private final String displayName;

        EventType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static class LogEntry {
        private final LocalDateTime timestamp;
        private final int turn;
        private final EventType type;
        private final String message;
        private final String details;

        public LogEntry(int turn, EventType type, String message, String details) {
            this.timestamp = LocalDateTime.now();
            this.turn = turn;
            this.type = type;
            this.message = message;
            this.details = details;
        }

        public LocalDateTime getTimestamp() { return timestamp; }
        public int getTurn() { return turn; }
        public EventType getType() { return type; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }

        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            return String.format("[T%d %s] %s %s%s", 
                turn, 
                timestamp.format(formatter),
                type.name().replace("_", " "),
                message,
                details != null && !details.isEmpty() ? " - " + details : "");
        }

        /**
         * Get a formatted string for UI display
         */
        public String toDisplayString() {
            return String.format("[Turn %d] %s", turn, message);
        }
    }

    private final List<LogEntry> entries;
    private Consumer<LogEntry> onNewEntry;
    private int maxEntries;

    public EventLogger() {
        this(1000); // Default max entries
    }

    public EventLogger(int maxEntries) {
        this.entries = new CopyOnWriteArrayList<>();
        this.maxEntries = maxEntries;
    }

    /**
     * Set callback for new log entries
     */
    public void setOnNewEntry(Consumer<LogEntry> callback) {
        this.onNewEntry = callback;
    }

    /**
     * Add a new log entry
     */
    private void addEntry(LogEntry entry) {
        entries.add(entry);
        // Trim old entries if exceeding max
        while (entries.size() > maxEntries) {
            entries.remove(0);
        }
        if (onNewEntry != null) {
            onNewEntry.accept(entry);
        }
    }

    /**
     * Log a successful mating event
     */
    public void logMatingSuccess(int turn, Creature parent1, Creature parent2, Creature offspring) {
        String message = String.format("%s %s + %s → successful mating → offspring %s",
            parent1.getType().getDisplayName(),
            parent1.getIdString(),
            parent2.getIdString(),
            offspring.getIdString());
        String details = String.format("Parents: %s(%s) + %s(%s), Offspring: %s(%s)",
            parent1.getIdString(), parent1.getSex().getSymbol(),
            parent2.getIdString(), parent2.getSex().getSymbol(),
            offspring.getIdString(), offspring.getSex().getSymbol());
        addEntry(new LogEntry(turn, EventType.MATING_SUCCESS, message, details));
    }

    /**
     * Log a rejected mating attempt
     */
    public void logMatingRejected(int turn, Creature creature1, Creature creature2, String reason) {
        String message = String.format("%s %s + %s → rejected (%s)",
            creature1.getType().getDisplayName(),
            creature1.getIdString(),
            creature2.getIdString(),
            reason);
        addEntry(new LogEntry(turn, EventType.MATING_REJECTED, message, reason));
    }

    /**
     * Log a birth event
     */
    public void logBirth(int turn, Creature offspring, Creature parent1, Creature parent2) {
        String message = String.format("🐣 %s %s born to parents %s & %s",
            offspring.getType().getDisplayName(),
            offspring.getIdString(),
            parent1.getIdString(),
            parent2.getIdString());
        String details = String.format("Species: %s, Sex: %s, ID: %d",
            offspring.getType().getDisplayName(),
            offspring.getSex().getDisplayName(),
            offspring.getId());
        addEntry(new LogEntry(turn, EventType.BIRTH, message, details));
    }

    /**
     * Log a death by predation
     */
    public void logDeathByPredation(int turn, Creature victim, Creature predator) {
        String message = String.format("💀 %s %s killed by %s %s",
            victim.getType().getDisplayName(),
            victim.getIdString(),
            predator.getType().getDisplayName(),
            predator.getIdString());
        String details = String.format("Victim: %s (%s), Predator: %s",
            victim.getIdString(), victim.getSex().getDisplayName(), predator.getIdString());
        addEntry(new LogEntry(turn, EventType.DEATH_PREDATION, message, details));
    }

    /**
     * Log a death by starvation
     */
    public void logDeathByStarvation(int turn, Creature creature) {
        String message = String.format("💀 %s %s died of starvation",
            creature.getType().getDisplayName(),
            creature.getIdString());
        String details = String.format("Species: %s, Sex: %s, Age: %d turns",
            creature.getType().getDisplayName(),
            creature.getSex().getDisplayName(),
            creature.getAge());
        addEntry(new LogEntry(turn, EventType.DEATH_STARVATION, message, details));
    }

    /**
     * Log a death by old age (not currently used but available)
     */
    public void logDeathByOldAge(int turn, Creature creature) {
        String message = String.format("💀 %s %s died of old age",
            creature.getType().getDisplayName(),
            creature.getIdString());
        String details = String.format("Species: %s, Sex: %s, Age: %d turns",
            creature.getType().getDisplayName(),
            creature.getSex().getDisplayName(),
            creature.getAge());
        addEntry(new LogEntry(turn, EventType.DEATH_OLD_AGE, message, details));
    }

    /**
     * Log a scavenging event
     */
    public void logScavenging(int turn, Creature scavenger, Corpse corpse) {
        String message = String.format("🦴 %s %s consumed %s",
            scavenger.getType().getDisplayName(),
            scavenger.getIdString(),
            corpse.getIdString());
        String details = String.format("Scavenger: %s (%s), Corpse: %s (original: %s %s)",
            scavenger.getIdString(), scavenger.getSex().getDisplayName(),
            corpse.getOriginalCreatureId(),
            corpse.getOriginalType().getDisplayName(),
            corpse.getOriginalSex().getDisplayName());
        addEntry(new LogEntry(turn, EventType.SCAVENGING, message, details));
    }

    /**
     * Log a mutation activation
     */
    public void logMutationActivated(int turn, Creature creature) {
        String message = String.format("🧬 %s %s mutated: %s",
            creature.getType().getDisplayName(),
            creature.getIdString(),
            creature.getMutationType().getDisplayName());
        String details = String.format("Species: %s, Sex: %s, Mutation: %s",
            creature.getType().getDisplayName(),
            creature.getSex().getDisplayName(),
            creature.getMutationType().toString());
        addEntry(new LogEntry(turn, EventType.MUTATION_ACTIVATED, message, details));
    }

    /**
     * Log a corpse decay event
     */
    public void logCorpseDecay(int turn, Corpse corpse) {
        String message = String.format("💨 %s decayed",
            corpse.getIdString());
        addEntry(new LogEntry(turn, EventType.CORPSE_DECAY, message, null));
    }

    /**
     * Log a death by thirst
     */
    public void logDeathByThirst(int turn, Creature creature) {
        String message = String.format("💀 %s %s died of thirst",
            creature.getType().getDisplayName(),
            creature.getIdString());
        String details = String.format("Species: %s, Sex: %s, Age: %d turns, Thirst: %d",
            creature.getType().getDisplayName(),
            creature.getSex().getDisplayName(),
            creature.getAge(),
            creature.getThirst());
        addEntry(new LogEntry(turn, EventType.DEATH_THIRST, message, details));
    }

    /**
     * Log a death by hunger
     */
    public void logDeathByHunger(int turn, Creature creature) {
        String message = String.format("💀 %s %s died of hunger",
            creature.getType().getDisplayName(),
            creature.getIdString());
        String details = String.format("Species: %s, Sex: %s, Age: %d turns, Hunger: %d",
            creature.getType().getDisplayName(),
            creature.getSex().getDisplayName(),
            creature.getAge(),
            creature.getHunger());
        addEntry(new LogEntry(turn, EventType.DEATH_HUNGER, message, details));
    }

    /**
     * Log water consumption
     */
    public void logWaterConsumed(int turn, Creature creature, int row, int col) {
        String message = String.format("💧 %s %s drank water at (%d,%d) - Thirst: %d → %d",
            creature.getType().getDisplayName(),
            creature.getIdString(),
            row, col,
            Math.min(100, creature.getThirst() + 50), // Before drinking
            creature.getThirst()); // After drinking
        String details = String.format("Thirst reduced, Species: %s",
            creature.getType().getDisplayName());
        addEntry(new LogEntry(turn, EventType.WATER_CONSUMED, message, details));
    }

    /**
     * Log food consumption
     */
    public void logFoodConsumed(int turn, Creature creature, int row, int col) {
        String message = String.format("🍃 %s %s ate food at (%d,%d) - Hunger: %d → %d",
            creature.getType().getDisplayName(),
            creature.getIdString(),
            row, col,
            Math.min(100, creature.getHunger() + 40), // Before eating
            creature.getHunger()); // After eating
        String details = String.format("Hunger reduced, Species: %s",
            creature.getType().getDisplayName());
        addEntry(new LogEntry(turn, EventType.FOOD_CONSUMED, message, details));
    }
    
    /**
     * Log creature movement with hunger and thirst status
     */
    public void logMovement(int turn, Creature creature, int fromRow, int fromCol, int toRow, int toCol) {
        String message = String.format("🚶 %s %s moved (%d,%d) → (%d,%d) | H:%d T:%d E:%d",
            creature.getType().getDisplayName(),
            creature.getIdString(),
            fromRow, fromCol, toRow, toCol,
            creature.getHunger(),
            creature.getThirst(),
            creature.getEnergy());
        String details = String.format("Hunger: %d, Thirst: %d, Energy: %d",
            creature.getHunger(), creature.getThirst(), creature.getEnergy());
        addEntry(new LogEntry(turn, EventType.MOVEMENT, message, details));
    }

    /**
     * Get all log entries
     */
    public List<LogEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    /**
     * Get the most recent entries
     */
    public List<LogEntry> getRecentEntries(int count) {
        int start = Math.max(0, entries.size() - count);
        return new ArrayList<>(entries.subList(start, entries.size()));
    }

    /**
     * Get entries for a specific turn
     */
    public List<LogEntry> getEntriesForTurn(int turn) {
        return entries.stream()
            .filter(e -> e.getTurn() == turn)
            .toList();
    }

    /**
     * Get entries by event type
     */
    public List<LogEntry> getEntriesByType(EventType type) {
        return entries.stream()
            .filter(e -> e.getType() == type)
            .toList();
    }

    /**
     * Clear all entries
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Get total entry count
     */
    public int getEntryCount() {
        return entries.size();
    }
}
