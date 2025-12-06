package com.ecosimulator.simulation;

import com.ecosimulator.model.*;
import com.ecosimulator.persistence.SimulationPersistence;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Core simulation engine that manages the ecosystem grid and turn-based simulation
 */
public class SimulationEngine {
    private final SimulationConfig config;
    private final CellType[][] grid;
    private final List<Creature> creatures;
    private final Map<String, Creature> creaturePositionMap; // For O(1) lookup
    private final SimulationStats stats;
    private final Random random;
    private boolean running;
    private boolean paused;
    
    // Persistence for saving ecosystem states to files
    private final SimulationPersistence persistence;
    private int extinctionTurn;
    
    // Event tracking for logging
    private StringBuilder turnEvents;

    // Callbacks for UI updates
    private Runnable onGridUpdate;
    private Runnable onStatsUpdate;
    private Runnable onSimulationEnd;

    public SimulationEngine(SimulationConfig config) {
        this.config = config;
        this.grid = new CellType[config.getGridSize()][config.getGridSize()];
        this.creatures = new CopyOnWriteArrayList<>();
        this.creaturePositionMap = new ConcurrentHashMap<>();
        this.stats = new SimulationStats();
        this.random = new Random();
        this.running = false;
        this.paused = false;
        this.persistence = new SimulationPersistence();
        this.extinctionTurn = -1;
        this.turnEvents = new StringBuilder();
        initializeGrid();
    }

    /**
     * Get position key for the creature map
     */
    private String positionKey(int row, int col) {
        return row + "," + col;
    }

    /**
     * Initialize the grid based on the scenario configuration
     */
    public void initializeGrid() {
        creatures.clear();
        creaturePositionMap.clear();
        stats.reset();
        int size = config.getGridSize();
        int totalCells = size * size;

        // Clear grid
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = CellType.EMPTY;
            }
        }

        // Calculate creature counts
        int predatorCount = (int) (totalCells * config.getPredatorPercentage());
        int preyCount = (int) (totalCells * config.getPreyPercentage());
        int thirdSpeciesCount = (int) (totalCells * config.getThirdSpeciesPercentage());

        // Create list of all positions and shuffle
        List<int[]> positions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                positions.add(new int[]{i, j});
            }
        }
        Collections.shuffle(positions, random);

        int posIndex = 0;

        // Place predators
        for (int i = 0; i < predatorCount && posIndex < positions.size(); i++) {
            int[] pos = positions.get(posIndex++);
            grid[pos[0]][pos[1]] = CellType.PREDATOR;
            Creature creature = new Creature(CellType.PREDATOR, pos[0], pos[1]);
            if (config.isMutationsEnabled() && random.nextDouble() < 0.1) {
                creature.mutate();
            }
            creatures.add(creature);
            creaturePositionMap.put(positionKey(pos[0], pos[1]), creature);
        }

        // Place prey
        for (int i = 0; i < preyCount && posIndex < positions.size(); i++) {
            int[] pos = positions.get(posIndex++);
            grid[pos[0]][pos[1]] = CellType.PREY;
            Creature creature = new Creature(CellType.PREY, pos[0], pos[1]);
            if (config.isMutationsEnabled() && random.nextDouble() < 0.1) {
                creature.mutate();
            }
            creatures.add(creature);
            creaturePositionMap.put(positionKey(pos[0], pos[1]), creature);
        }

        // Place third species if enabled
        if (config.isThirdSpeciesEnabled()) {
            for (int i = 0; i < thirdSpeciesCount && posIndex < positions.size(); i++) {
                int[] pos = positions.get(posIndex++);
                grid[pos[0]][pos[1]] = CellType.THIRD_SPECIES;
                Creature creature = new Creature(CellType.THIRD_SPECIES, pos[0], pos[1]);
                if (config.isMutationsEnabled() && random.nextDouble() < 0.1) {
                    creature.mutate();
                }
                creatures.add(creature);
                creaturePositionMap.put(positionKey(pos[0], pos[1]), creature);
            }
        }

        updateStats();
        if (onGridUpdate != null) onGridUpdate.run();
        if (onStatsUpdate != null) onStatsUpdate.run();
    }

    /**
     * Execute one turn of the simulation
     */
    public void executeTurn() {
        if (!running || paused) return;

        stats.nextTurn();
        turnEvents = new StringBuilder();
        List<Creature> newCreatures = new ArrayList<>();
        List<Creature> deadCreatures = new ArrayList<>();

        // Shuffle creatures for random order processing
        List<Creature> shuffled = new ArrayList<>(creatures);
        Collections.shuffle(shuffled, random);

        for (Creature creature : shuffled) {
            if (creature.isDead() || deadCreatures.contains(creature)) continue;

            // Age the creature
            creature.age();

            // Check if creature died of old age/starvation
            if (creature.isDead()) {
                deadCreatures.add(creature);
                stats.recordDeath();
                turnEvents.append(creature.getType().getDisplayName())
                         .append(" died (starvation). ");
                continue;
            }

            // Try to find food or move
            processCreatureAction(creature, newCreatures, deadCreatures);

            // Try to reproduce
            if (creature.canReproduce() && !creature.isDead()) {
                Creature offspring = tryReproduce(creature);
                if (offspring != null) {
                    newCreatures.add(offspring);
                    stats.recordBirth();
                    turnEvents.append(creature.getType().getDisplayName())
                             .append(" reproduced. ");
                }
            }
        }

        // Remove dead creatures
        for (Creature dead : deadCreatures) {
            creatures.remove(dead);
            creaturePositionMap.remove(positionKey(dead.getRow(), dead.getCol()));
            grid[dead.getRow()][dead.getCol()] = CellType.EMPTY;
        }

        // Add new creatures
        creatures.addAll(newCreatures);
        for (Creature newCreature : newCreatures) {
            creaturePositionMap.put(positionKey(newCreature.getRow(), newCreature.getCol()), newCreature);
        }

        // Apply random mutations if enabled
        if (config.isMutationsEnabled()) {
            applyRandomMutations();
        }

        updateStats();
        
        // Log turn state to file
        persistence.logTurnState(stats.getTurn(), grid, stats, turnEvents.toString());
        
        // Track extinction turn
        if (extinctionTurn < 0 && stats.isExtinct()) {
            extinctionTurn = stats.getTurn();
        }

        if (onGridUpdate != null) onGridUpdate.run();
        if (onStatsUpdate != null) onStatsUpdate.run();

        // Check for simulation end conditions
        if (stats.isExtinct() || stats.getTurn() >= config.getMaxTurns()) {
            stop();
            if (onSimulationEnd != null) onSimulationEnd.run();
        }
    }

    /**
     * Process creature movement and hunting/eating
     */
    private void processCreatureAction(Creature creature, List<Creature> newCreatures, 
                                        List<Creature> deadCreatures) {
        int row = creature.getRow();
        int col = creature.getCol();
        List<int[]> neighbors = getNeighbors(row, col);
        Collections.shuffle(neighbors, random);

        CellType targetType = getTargetType(creature.getType());

        // Look for food first
        for (int[] neighbor : neighbors) {
            CellType cellType = grid[neighbor[0]][neighbor[1]];
            if (cellType == targetType) {
                // Hunt/eat
                Creature prey = findCreatureAt(neighbor[0], neighbor[1]);
                if (prey != null && !deadCreatures.contains(prey)) {
                    deadCreatures.add(prey);
                    stats.recordDeath();
                    creaturePositionMap.remove(positionKey(prey.getRow(), prey.getCol()));
                    grid[prey.getRow()][prey.getCol()] = CellType.EMPTY;
                    
                    // Move to prey's position and gain energy
                    creaturePositionMap.remove(positionKey(row, col));
                    grid[row][col] = CellType.EMPTY;
                    creature.move(neighbor[0], neighbor[1]);
                    grid[neighbor[0]][neighbor[1]] = creature.getType();
                    creaturePositionMap.put(positionKey(neighbor[0], neighbor[1]), creature);
                    // Reduced energy gain from 8 to 5 to nerf predators
                    creature.eat((int)(5 * creature.getMutationBonus()));
                    return;
                }
            }
        }

        // If prey, eat vegetation (gain energy - increased from 2 to 3)
        if (creature.getType() == CellType.PREY) {
            creature.eat(3);
        }

        // Third species can eat both (opportunistic)
        if (creature.getType() == CellType.THIRD_SPECIES) {
            creature.eat(1);
        }

        // Move to empty cell
        for (int[] neighbor : neighbors) {
            if (grid[neighbor[0]][neighbor[1]] == CellType.EMPTY) {
                creaturePositionMap.remove(positionKey(row, col));
                grid[row][col] = CellType.EMPTY;
                creature.move(neighbor[0], neighbor[1]);
                grid[neighbor[0]][neighbor[1]] = creature.getType();
                creaturePositionMap.put(positionKey(neighbor[0], neighbor[1]), creature);
                return;
            }
        }
    }

    /**
     * Get the target food type for a creature
     */
    private CellType getTargetType(CellType creatureType) {
        return switch (creatureType) {
            case PREDATOR -> CellType.PREY;
            case PREY -> null; // Prey eats vegetation (always available)
            case THIRD_SPECIES -> random.nextBoolean() ? CellType.PREY : CellType.PREDATOR;
            default -> null;
        };
    }

    /**
     * Try to reproduce a creature
     */
    private Creature tryReproduce(Creature parent) {
        int row = parent.getRow();
        int col = parent.getCol();
        List<int[]> neighbors = getNeighbors(row, col);
        Collections.shuffle(neighbors, random);

        for (int[] neighbor : neighbors) {
            if (grid[neighbor[0]][neighbor[1]] == CellType.EMPTY) {
                parent.reproduce();
                Creature offspring = new Creature(parent.getType(), neighbor[0], neighbor[1]);
                
                // Inherit mutation with some probability
                if (parent.isMutated() && random.nextDouble() < 0.7) {
                    offspring.mutate();
                }
                
                grid[neighbor[0]][neighbor[1]] = offspring.getType();
                return offspring;
            }
        }
        return null;
    }

    /**
     * Apply random mutations to creatures
     */
    private void applyRandomMutations() {
        for (Creature creature : creatures) {
            if (!creature.isMutated() && random.nextDouble() < 0.02) { // 2% chance per turn
                creature.mutate();
            }
        }
    }

    /**
     * Get valid neighboring cells
     */
    private List<int[]> getNeighbors(int row, int col) {
        List<int[]> neighbors = new ArrayList<>();
        int size = config.getGridSize();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                neighbors.add(new int[]{newRow, newCol});
            }
        }
        return neighbors;
    }

    /**
     * Find creature at specific position using O(1) lookup
     */
    private Creature findCreatureAt(int row, int col) {
        return creaturePositionMap.get(positionKey(row, col));
    }

    /**
     * Check if creature at position is mutated (for UI rendering)
     */
    public boolean isCreatureMutatedAt(int row, int col) {
        Creature creature = creaturePositionMap.get(positionKey(row, col));
        return creature != null && creature.isMutated();
    }

    /**
     * Update statistics based on current state
     */
    private void updateStats() {
        int predators = 0, prey = 0, third = 0, mutated = 0;
        
        for (Creature creature : creatures) {
            switch (creature.getType()) {
                case PREDATOR -> predators++;
                case PREY -> prey++;
                case THIRD_SPECIES -> third++;
                default -> {}
            }
            if (creature.isMutated()) mutated++;
        }
        
        stats.setPredatorCount(predators);
        stats.setPreyCount(prey);
        stats.setThirdSpeciesCount(third);
        stats.setMutatedCount(mutated);
    }

    // Control methods
    public void start() {
        this.running = true;
        this.paused = false;
        this.extinctionTurn = -1;
        
        // Save initial ecosystem and initialize turn log
        persistence.saveInitialEcosystem(grid, config, stats);
        persistence.initializeTurnLog(config);
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;
    }

    public void stop() {
        // Guard against multiple stop calls - only finalize if was running
        boolean wasRunning = this.running;
        this.running = false;
        this.paused = false;
        
        // Only finalize turn log if simulation was actually running
        if (wasRunning) {
            persistence.finalizeTurnLog(stats, extinctionTurn);
            persistence.close();
        }
    }

    public void reset() {
        stop();
        this.extinctionTurn = -1;
        initializeGrid();
    }

    // State checks
    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }

    // Getters
    public CellType[][] getGrid() { return grid; }
    public List<Creature> getCreatures() { return creatures; }
    public SimulationStats getStats() { return stats; }
    public SimulationConfig getConfig() { return config; }
    public int getExtinctionTurn() { return extinctionTurn; }

    // Callback setters
    public void setOnGridUpdate(Runnable callback) { this.onGridUpdate = callback; }
    public void setOnStatsUpdate(Runnable callback) { this.onStatsUpdate = callback; }
    public void setOnSimulationEnd(Runnable callback) { this.onSimulationEnd = callback; }
}
