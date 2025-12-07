package com.ecosimulator.simulation;

import com.ecosimulator.model.*;
import com.ecosimulator.persistence.SimulationPersistence;
import com.ecosimulator.service.EventLogger;
import com.ecosimulator.service.ReproductionManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Core simulation engine that manages the ecosystem grid and turn-based simulation.
 * Features sex-based reproduction, corpse mechanics, and scavenging behavior.
 */
public class SimulationEngine {
    private final SimulationConfig config;
    private final CellType[][] grid;
    private final List<Creature> creatures;
    private final Map<String, Creature> creaturePositionMap; // For O(1) lookup
    private final Map<String, Corpse> corpseMap; // Corpses on the grid
    private final Map<String, Integer> waterPositions; // Track water positions
    private final Map<String, Integer> foodPositions; // Track food positions
    private final SimulationStats stats;
    private final Random random;
    private boolean running;
    private boolean paused;
    
    // Persistence for saving ecosystem states to files
    private final SimulationPersistence persistence;
    private int extinctionTurn;
    
    // Event tracking for logging
    private StringBuilder turnEvents;
    private final EventLogger eventLogger;
    private final ReproductionManager reproductionManager;

    // Callbacks for UI updates
    private Runnable onGridUpdate;
    private Runnable onStatsUpdate;
    private Runnable onSimulationEnd;

    public SimulationEngine(SimulationConfig config) {
        this.config = config;
        this.grid = new CellType[config.getGridSize()][config.getGridSize()];
        this.creatures = new CopyOnWriteArrayList<>();
        this.creaturePositionMap = new ConcurrentHashMap<>();
        this.corpseMap = new ConcurrentHashMap<>();
        this.waterPositions = new ConcurrentHashMap<>();
        this.foodPositions = new ConcurrentHashMap<>();
        this.stats = new SimulationStats();
        this.random = new Random();
        this.running = false;
        this.paused = false;
        this.persistence = new SimulationPersistence();
        this.extinctionTurn = -1;
        this.turnEvents = new StringBuilder();
        this.eventLogger = new EventLogger();
        this.reproductionManager = new ReproductionManager(eventLogger);
        initializeGrid();
    }

    /**
     * Get position key for the creature/corpse map
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
        corpseMap.clear();
        waterPositions.clear();
        foodPositions.clear();
        stats.reset();
        eventLogger.clear();
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
        
        // Calculate resource counts (water and food)
        int waterCount = (int) (totalCells * 0.08); // 8% water sources
        int foodCount = (int) (totalCells * 0.12);  // 12% food sources

        // Create list of all positions and shuffle
        List<int[]> positions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                positions.add(new int[]{i, j});
            }
        }
        Collections.shuffle(positions, random);

        int posIndex = 0;
        
        // Place water sources first
        for (int i = 0; i < waterCount && posIndex < positions.size(); i++) {
            int[] pos = positions.get(posIndex++);
            grid[pos[0]][pos[1]] = CellType.WATER;
            waterPositions.put(positionKey(pos[0], pos[1]), 1);
        }
        
        // Place food sources
        for (int i = 0; i < foodCount && posIndex < positions.size(); i++) {
            int[] pos = positions.get(posIndex++);
            grid[pos[0]][pos[1]] = CellType.FOOD;
            foodPositions.put(positionKey(pos[0], pos[1]), 1);
        }

        // Place predators with random sex
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

        // Place prey with random sex
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

        // Place third species (scavengers) if enabled
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
        reproductionManager.setCurrentTurn(stats.getTurn());
        turnEvents = new StringBuilder();
        List<Creature> newCreatures = new ArrayList<>();
        List<Creature> deadCreatures = new ArrayList<>();
        List<Corpse> consumedCorpses = new ArrayList<>();

        // Process corpse decay first
        processCorpseDecay(consumedCorpses);

        // Shuffle creatures for random order processing
        List<Creature> shuffled = new ArrayList<>(creatures);
        Collections.shuffle(shuffled, random);

        for (Creature creature : shuffled) {
            if (creature.isDead() || deadCreatures.contains(creature)) continue;

            // Age the creature (also decreases mating cooldown, increases hunger/thirst)
            creature.age();

            // Check if creature died
            if (creature.isDead()) {
                deadCreatures.add(creature);
                stats.recordDeath();
                
                // Determine cause of death
                if (creature.isDyingFromThirst()) {
                    eventLogger.logDeathByThirst(stats.getTurn(), creature);
                    turnEvents.append(creature.getIdString()).append(" died (thirst). ");
                } else if (creature.isDyingFromHunger()) {
                    eventLogger.logDeathByHunger(stats.getTurn(), creature);
                    turnEvents.append(creature.getIdString()).append(" died (hunger). ");
                } else {
                    eventLogger.logDeathByStarvation(stats.getTurn(), creature);
                    turnEvents.append(creature.getIdString()).append(" died (starvation). ");
                }
                continue;
            }

            // Try to find food, scavenge, or move
            processCreatureAction(creature, newCreatures, deadCreatures, consumedCorpses);

            // Try to reproduce with sex-based mating
            if (creature.canReproduce() && creature.isMature() && creature.canMate() && !creature.isDead()) {
                Creature offspring = tryReproduce(creature, newCreatures);
                if (offspring != null) {
                    newCreatures.add(offspring);
                    stats.recordBirth();
                    turnEvents.append(creature.getIdString()).append(" reproduced → ")
                             .append(offspring.getIdString()).append(". ");
                }
            }
        }

        // Create corpses from dead creatures
        for (Creature dead : deadCreatures) {
            createCorpse(dead);
            creatures.remove(dead);
            creaturePositionMap.remove(positionKey(dead.getRow(), dead.getCol()));
        }

        // Remove consumed corpses
        for (Corpse corpse : consumedCorpses) {
            corpseMap.remove(corpse.getPositionKey());
            grid[corpse.getRow()][corpse.getCol()] = CellType.EMPTY;
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
     * Process corpse decay and removal
     */
    private void processCorpseDecay(List<Corpse> consumedCorpses) {
        List<Corpse> decayedCorpses = new ArrayList<>();
        for (Corpse corpse : corpseMap.values()) {
            if (corpse.decay()) {
                decayedCorpses.add(corpse);
                eventLogger.logCorpseDecay(stats.getTurn(), corpse);
            }
        }
        // Remove fully decayed corpses
        for (Corpse corpse : decayedCorpses) {
            corpseMap.remove(corpse.getPositionKey());
            if (grid[corpse.getRow()][corpse.getCol()] == CellType.CORPSE) {
                grid[corpse.getRow()][corpse.getCol()] = CellType.EMPTY;
            }
        }
    }

    /**
     * Create a corpse from a dead creature
     */
    private void createCorpse(Creature dead) {
        Corpse corpse = new Corpse(dead);
        corpseMap.put(corpse.getPositionKey(), corpse);
        grid[dead.getRow()][dead.getCol()] = CellType.CORPSE;
    }

    /**
     * Process creature movement, hunting/eating, and scavenging
     */
    private void processCreatureAction(Creature creature, List<Creature> newCreatures, 
                                        List<Creature> deadCreatures, List<Corpse> consumedCorpses) {
        int row = creature.getRow();
        int col = creature.getCol();
        
        // Get movement range based on creature type and energy
        int movementRange = getMovementRange(creature);
        List<int[]> neighbors = getNeighborsWithinRange(row, col, movementRange);
        Collections.shuffle(neighbors, random);

        // Priority 1: Check if creature is critically thirsty (>70) - seek water immediately
        if (creature.getThirst() > 70) {
            if (seekAndConsumeResource(creature, neighbors, CellType.WATER, row, col)) {
                return;
            }
        }

        // Priority 2: Check if creature is critically hungry (>70) - seek food
        if (creature.getHunger() > 70) {
            // Predators hunt prey when hungry
            if (creature.getType() == CellType.PREDATOR) {
                if (huntPrey(creature, neighbors, deadCreatures, row, col)) {
                    return;
                }
            }
            // Prey and scavengers seek food resources
            else if (creature.getType() == CellType.PREY) {
                if (seekAndConsumeResource(creature, neighbors, CellType.FOOD, row, col)) {
                    return;
                }
            }
        }

        // Priority 3: Scavengers prioritize corpses when not critically hungry/thirsty
        if (creature.getType() == CellType.THIRD_SPECIES) {
            if (processScavengerAction(creature, neighbors, consumedCorpses)) {
                return;
            }
        }

        // Priority 4: Normal hunting/eating behavior
        CellType targetType = getTargetType(creature.getType());
        if (targetType != null) {
            for (int[] neighbor : neighbors) {
                CellType cellType = grid[neighbor[0]][neighbor[1]];
                if (cellType == targetType) {
                    Creature prey = findCreatureAt(neighbor[0], neighbor[1]);
                    if (prey != null && !deadCreatures.contains(prey)) {
                        deadCreatures.add(prey);
                        stats.recordDeath();
                        eventLogger.logDeathByPredation(stats.getTurn(), prey, creature);
                        creaturePositionMap.remove(positionKey(prey.getRow(), prey.getCol()));
                        
                        moveCreature(creature, neighbor[0], neighbor[1], row, col);
                        creature.eat((int)(5 * creature.getMutationBonus()));
                        creature.eatFood(); // Also reduces hunger
                        turnEvents.append(creature.getIdString()).append(" hunted ")
                                 .append(prey.getIdString()).append(". ");
                        return;
                    }
                }
            }
        }

        // Priority 5: Opportunistically consume nearby resources
        if (creature.getThirst() > 30) {
            if (seekAndConsumeResource(creature, neighbors, CellType.WATER, row, col)) {
                return;
            }
        }
        if (creature.getHunger() > 30 && creature.getType() == CellType.PREY) {
            if (seekAndConsumeResource(creature, neighbors, CellType.FOOD, row, col)) {
                return;
            }
        }

        // Priority 6: Move to empty cell
        for (int[] neighbor : neighbors) {
            CellType cellType = grid[neighbor[0]][neighbor[1]];
            if (cellType == CellType.EMPTY) {
                moveCreature(creature, neighbor[0], neighbor[1], row, col);
                return;
            }
        }
    }

    /**
     * Seek and consume a resource (water or food)
     */
    private boolean seekAndConsumeResource(Creature creature, List<int[]> neighbors, 
                                          CellType resourceType, int currentRow, int currentCol) {
        for (int[] neighbor : neighbors) {
            if (grid[neighbor[0]][neighbor[1]] == resourceType) {
                // Move to resource and consume it
                moveCreature(creature, neighbor[0], neighbor[1], currentRow, currentCol);
                
                if (resourceType == CellType.WATER) {
                    int thirstBefore = creature.getThirst();
                    creature.drink();
                    stats.incrementWaterConsumed();
                    eventLogger.logWaterConsumed(stats.getTurn(), creature, neighbor[0], neighbor[1], thirstBefore);
                    turnEvents.append(creature.getIdString()).append(" drank water at (")
                             .append(neighbor[0]).append(",").append(neighbor[1]).append("). ");
                    // Respawn water after consumption
                    grid[neighbor[0]][neighbor[1]] = CellType.WATER;
                } else if (resourceType == CellType.FOOD) {
                    int hungerBefore = creature.getHunger();
                    creature.eatFood();
                    stats.incrementFoodConsumed();
                    eventLogger.logFoodConsumed(stats.getTurn(), creature, neighbor[0], neighbor[1], hungerBefore);
                    turnEvents.append(creature.getIdString()).append(" ate food at (")
                             .append(neighbor[0]).append(",").append(neighbor[1]).append("). ");
                    // Respawn food after consumption
                    grid[neighbor[0]][neighbor[1]] = CellType.FOOD;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Hunt prey for predators
     */
    private boolean huntPrey(Creature predator, List<int[]> neighbors, 
                            List<Creature> deadCreatures, int currentRow, int currentCol) {
        for (int[] neighbor : neighbors) {
            if (grid[neighbor[0]][neighbor[1]] == CellType.PREY) {
                Creature prey = findCreatureAt(neighbor[0], neighbor[1]);
                if (prey != null && !deadCreatures.contains(prey)) {
                    deadCreatures.add(prey);
                    stats.recordDeath();
                    eventLogger.logDeathByPredation(stats.getTurn(), prey, predator);
                    creaturePositionMap.remove(positionKey(prey.getRow(), prey.getCol()));
                    
                    moveCreature(predator, neighbor[0], neighbor[1], currentRow, currentCol);
                    predator.eat((int)(5 * predator.getMutationBonus()));
                    predator.eatFood(); // Hunting also reduces hunger significantly
                    turnEvents.append(predator.getIdString()).append(" hunted ")
                             .append(prey.getIdString()).append(". ");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Move creature from current position to new position
     */
    private void moveCreature(Creature creature, int newRow, int newCol, int oldRow, int oldCol) {
        CellType cellAtDestination = grid[newRow][newCol];
        
        creaturePositionMap.remove(positionKey(oldRow, oldCol));
        
        // Restore resource if creature was on one
        if (waterPositions.containsKey(positionKey(oldRow, oldCol))) {
            grid[oldRow][oldCol] = CellType.WATER;
        } else if (foodPositions.containsKey(positionKey(oldRow, oldCol))) {
            grid[oldRow][oldCol] = CellType.FOOD;
        } else {
            grid[oldRow][oldCol] = CellType.EMPTY;
        }
        
        creature.move(newRow, newCol);
        grid[newRow][newCol] = creature.getType();
        creaturePositionMap.put(positionKey(newRow, newCol), creature);
        
        // Log movement with hunger/thirst status
        eventLogger.logMovement(stats.getTurn(), creature, oldRow, oldCol, newRow, newCol);
    }

    /**
     * Process scavenger-specific behavior - prioritize finding and eating corpses
     */
    private boolean processScavengerAction(Creature scavenger, List<int[]> neighbors, 
                                           List<Corpse> consumedCorpses) {
        int row = scavenger.getRow();
        int col = scavenger.getCol();

        // Look for corpses in neighboring cells
        for (int[] neighbor : neighbors) {
            if (grid[neighbor[0]][neighbor[1]] == CellType.CORPSE) {
                Corpse corpse = corpseMap.get(positionKey(neighbor[0], neighbor[1]));
                if (corpse != null && !consumedCorpses.contains(corpse)) {
                    // Consume the corpse
                    int energyGain = corpse.consume();
                    scavenger.eat((int)(energyGain * scavenger.getMutationBonus()));
                    consumedCorpses.add(corpse);
                    eventLogger.logScavenging(stats.getTurn(), scavenger, corpse);
                    turnEvents.append(scavenger.getIdString()).append(" scavenged ")
                             .append(corpse.getIdString()).append(". ");

                    // Move to corpse position
                    creaturePositionMap.remove(positionKey(row, col));
                    grid[row][col] = CellType.EMPTY;
                    scavenger.move(neighbor[0], neighbor[1]);
                    grid[neighbor[0]][neighbor[1]] = scavenger.getType();
                    creaturePositionMap.put(positionKey(neighbor[0], neighbor[1]), scavenger);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the target food type for a creature
     */
    private CellType getTargetType(CellType creatureType) {
        return switch (creatureType) {
            case PREDATOR -> CellType.PREY;
            case PREY -> null; // Prey eats vegetation (always available)
            case THIRD_SPECIES -> null; // Scavengers eat corpses (handled separately)
            default -> null;
        };
    }

    /**
     * Try to reproduce using sex-based mating rules
     */
    private Creature tryReproduce(Creature parent, List<Creature> newCreatures) {
        int row = parent.getRow();
        int col = parent.getCol();
        List<int[]> neighbors = getNeighbors(row, col);
        Collections.shuffle(neighbors, random);

        // Find a mate of opposite sex, same species, in neighboring cells
        Creature mate = findMate(parent, neighbors);
        
        if (mate == null) {
            return null; // No suitable mate found
        }

        // Find an empty cell for offspring
        for (int[] neighbor : neighbors) {
            if (grid[neighbor[0]][neighbor[1]] == CellType.EMPTY) {
                // Use ReproductionManager for proper mating validation
                Creature offspring = reproductionManager.reproduce(
                    parent, mate, neighbor[0], neighbor[1], config.isMutationsEnabled());
                
                if (offspring != null) {
                    grid[neighbor[0]][neighbor[1]] = offspring.getType();
                    return offspring;
                }
                break;
            }
        }
        return null;
    }

    /**
     * Find a suitable mate (opposite sex, same species, mature, can mate) in neighboring cells
     */
    private Creature findMate(Creature seeker, List<int[]> neighbors) {
        for (int[] neighbor : neighbors) {
            Creature candidate = findCreatureAt(neighbor[0], neighbor[1]);
            if (candidate != null) {
                // Check if this is a valid mate
                String rejectionReason = reproductionManager.canMate(seeker, candidate);
                if (rejectionReason == null) {
                    return candidate;
                }
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
                eventLogger.logMutationActivated(stats.getTurn(), creature);
            }
        }
    }

    /**
     * Get movement range for a creature based on its type and energy
     */
    private int getMovementRange(Creature creature) {
        // Base movement range by creature type
        int baseRange = switch (creature.getType()) {
            case PREDATOR -> 2;  // Predators can move 2 cells
            case PREY -> 2;      // Prey can move 2 cells
            case THIRD_SPECIES -> 3;  // Scavengers can move 3 cells (faster)
            default -> 1;
        };
        
        // Reduce range if energy is very low (less than 5)
        if (creature.getEnergy() < 5) {
            return Math.max(1, baseRange - 1);
        }
        
        return baseRange;
    }
    
    /**
     * Get valid neighboring cells (default 1 cell distance in all 8 directions)
     */
    private List<int[]> getNeighbors(int row, int col) {
        return getNeighborsWithinRange(row, col, 1);
    }
    
    /**
     * Get valid neighboring cells within a specified range
     * Range determines how many cells away a creature can move in each direction
     * 
     * Note: This uses a square pattern (Chebyshev distance) rather than circular/Manhattan distance.
     * This is intentional for grid-based movement, allowing creatures to move to any cell
     * within the square boundary, including diagonals, which is more natural for grid navigation.
     */
    private List<int[]> getNeighborsWithinRange(int row, int col, int range) {
        List<int[]> neighbors = new ArrayList<>();
        int size = config.getGridSize();
        
        // Generate all positions within the range in 8 directions (square pattern)
        for (int dr = -range; dr <= range; dr++) {
            for (int dc = -range; dc <= range; dc++) {
                // Skip the center position (0,0)
                if (dr == 0 && dc == 0) continue;
                
                int newRow = row + dr;
                int newCol = col + dc;
                
                // Check if position is within grid bounds
                if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                    neighbors.add(new int[]{newRow, newCol});
                }
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
     * Get the creature at a specific position
     */
    public Creature getCreatureAt(int row, int col) {
        return creaturePositionMap.get(positionKey(row, col));
    }

    /**
     * Get the corpse at a specific position
     */
    public Corpse getCorpseAt(int row, int col) {
        return corpseMap.get(positionKey(row, col));
    }

    /**
     * Update statistics based on current state including sex ratios
     */
    private void updateStats() {
        int predators = 0, prey = 0, third = 0, mutated = 0;
        int predatorMales = 0, predatorFemales = 0;
        int preyMales = 0, preyFemales = 0;
        int thirdMales = 0, thirdFemales = 0;
        boolean predatorHasMutations = false;
        boolean preyHasMutations = false;
        boolean thirdHasMutations = false;
        
        for (Creature creature : creatures) {
            switch (creature.getType()) {
                case PREDATOR -> {
                    predators++;
                    if (creature.getSex() == Sex.MALE) predatorMales++;
                    else predatorFemales++;
                    if (creature.isMutated()) predatorHasMutations = true;
                }
                case PREY -> {
                    prey++;
                    if (creature.getSex() == Sex.MALE) preyMales++;
                    else preyFemales++;
                    if (creature.isMutated()) preyHasMutations = true;
                }
                case THIRD_SPECIES -> {
                    third++;
                    if (creature.getSex() == Sex.MALE) thirdMales++;
                    else thirdFemales++;
                    if (creature.isMutated()) thirdHasMutations = true;
                }
                default -> {}
            }
            if (creature.isMutated()) mutated++;
        }
        
        stats.setPredatorCount(predators);
        stats.setPreyCount(prey);
        stats.setThirdSpeciesCount(third);
        stats.setMutatedCount(mutated);
        stats.setCorpseCount(corpseMap.size());
        stats.setWaterCount(waterPositions.size());
        stats.setFoodCount(foodPositions.size());
        
        // Update sex counts
        stats.setPredatorMaleCount(predatorMales);
        stats.setPredatorFemaleCount(predatorFemales);
        stats.setPreyMaleCount(preyMales);
        stats.setPreyFemaleCount(preyFemales);
        stats.setThirdSpeciesMaleCount(thirdMales);
        stats.setThirdSpeciesFemaleCount(thirdFemales);
        
        // Update mutation status per species
        stats.setPredatorHasMutations(predatorHasMutations);
        stats.setPreyHasMutations(preyHasMutations);
        stats.setThirdSpeciesHasMutations(thirdHasMutations);
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
    public EventLogger getEventLogger() { return eventLogger; }
    public Map<String, Corpse> getCorpseMap() { return corpseMap; }

    // Callback setters
    public void setOnGridUpdate(Runnable callback) { this.onGridUpdate = callback; }
    public void setOnStatsUpdate(Runnable callback) { this.onStatsUpdate = callback; }
    public void setOnSimulationEnd(Runnable callback) { this.onSimulationEnd = callback; }
}
