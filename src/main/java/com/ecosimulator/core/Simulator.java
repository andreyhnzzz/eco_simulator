package com.ecosimulator.core;

import com.ecosimulator.model.*;

import java.util.*;

/**
 * Simulator class that implements the exact simulation rules per turn
 * Phases: Movement → Feeding → Hunger → Reproduction → End of Turn
 */
public class Simulator {
    private Ecosystem ecosystem;
    private int currentTurn;
    private Random random;
    private List<String> turnEvents;
    private Map<String, Integer> turnHistory;  // Turn -> event count
    
    public Simulator() {
        this.ecosystem = new Ecosystem();
        this.currentTurn = 0;
        this.random = new Random();
        this.turnEvents = new ArrayList<>();
        this.turnHistory = new HashMap<>();
    }
    
    /**
     * Load a scenario
     * @param scenarioPath path to scenario JSON file
     */
    public void loadScenario(String scenarioPath) throws java.io.IOException {
        ScenarioConfig config = ScenarioConfig.loadFromFile(scenarioPath);
        ecosystem.loadScenario(config);
        currentTurn = 0;
        turnEvents.clear();
        turnHistory.clear();
    }
    
    /**
     * Execute one turn of the simulation following exact rules
     */
    public void stepTurn() {
        currentTurn++;
        turnEvents.clear();
        turnEvents.add("Turn: " + currentTurn);
        
        List<Animal> deadAnimals = new ArrayList<>();
        List<Animal> newAnimals = new ArrayList<>();
        
        // Get a randomized order of animals
        List<Animal> animals = new ArrayList<>(ecosystem.getAnimals());
        Collections.shuffle(animals, random);
        
        // Phase 1: Movement
        for (Animal animal : animals) {
            if (deadAnimals.contains(animal)) continue;
            processMovement(animal, deadAnimals);
        }
        
        // Phase 2: Feeding (already handled in movement when predator catches prey)
        
        // Phase 3: Hunger - Check if predators are starving
        for (Animal animal : animals) {
            if (deadAnimals.contains(animal)) continue;
            if (animal instanceof Predator predator) {
                if (predator.isStarving(currentTurn)) {
                    deadAnimals.add(animal);
                    turnEvents.add(String.format("Predator at (%d,%d) died from starvation", 
                                                animal.getX(), animal.getY()));
                }
            }
        }
        
        // Phase 4: Reproduction
        for (Animal animal : animals) {
            if (deadAnimals.contains(animal)) continue;
            if (animal.canReproduce()) {
                Animal offspring = tryReproduce(animal);
                if (offspring != null) {
                    newAnimals.add(offspring);
                    turnEvents.add(String.format("%s at (%d,%d) reproduced -> offspring at (%d,%d)",
                                                animal.getType(), animal.getX(), animal.getY(),
                                                offspring.getX(), offspring.getY()));
                }
            }
        }
        
        // Remove dead animals
        for (Animal dead : deadAnimals) {
            ecosystem.removeAnimal(dead);
        }
        
        // Add new animals
        for (Animal newborn : newAnimals) {
            ecosystem.addAnimal(newborn);
        }
        
        // Age all remaining animals
        for (Animal animal : ecosystem.getAnimals()) {
            animal.age();
        }
        
        // Phase 5: End of Turn - Store statistics
        Map<String, Integer> counts = ecosystem.getCounts();
        turnHistory.put("turn_" + currentTurn, counts.get("prey") + counts.get("predator") + counts.get("third"));
    }
    
    /**
     * Process movement for an animal following the exact rules
     */
    private void processMovement(Animal animal, List<Animal> deadAnimals) {
        int x = animal.getX();
        int y = animal.getY();
        List<Cell> neighbors = ecosystem.getNeighbors(x, y);
        Collections.shuffle(neighbors, random);
        
        if (animal instanceof Prey) {
            // Prey: Move to ONE adjacent EMPTY cell randomly
            for (Cell neighbor : neighbors) {
                if (neighbor.isEmpty()) {
                    int oldX = animal.getX();
                    int oldY = animal.getY();
                    // Find the coordinates of the neighbor cell
                    for (int i = 0; i < Ecosystem.GRID_SIZE; i++) {
                        for (int j = 0; j < Ecosystem.GRID_SIZE; j++) {
                            if (ecosystem.getCell(i, j) == neighbor) {
                                ecosystem.moveAnimal(animal, i, j);
                                turnEvents.add(String.format("Prey moved from (%d,%d) to (%d,%d)", 
                                                            oldX, oldY, i, j));
                                return;
                            }
                        }
                    }
                }
            }
            // If no empty cell found, prey stays in place
        } 
        else if (animal instanceof Predator predator) {
            // Predator: If adjacent prey exists, move to prey cell; otherwise move to empty cell
            boolean foundPrey = false;
            
            for (Cell neighbor : neighbors) {
                if (neighbor.hasAnimal() && neighbor.getAnimal() instanceof Prey prey) {
                    // Move to prey and eat it
                    int oldX = predator.getX();
                    int oldY = predator.getY();
                    
                    deadAnimals.add(prey);
                    
                    // Find coordinates
                    for (int i = 0; i < Ecosystem.GRID_SIZE; i++) {
                        for (int j = 0; j < Ecosystem.GRID_SIZE; j++) {
                            if (ecosystem.getCell(i, j) == neighbor) {
                                ecosystem.removeAnimal(prey);
                                ecosystem.moveAnimal(predator, i, j);
                                predator.eat(5, currentTurn);
                                turnEvents.add(String.format("Predator at (%d,%d) ate prey at (%d,%d)", 
                                                            oldX, oldY, i, j));
                                foundPrey = true;
                                break;
                            }
                        }
                        if (foundPrey) break;
                    }
                    if (foundPrey) break;
                }
            }
            
            if (!foundPrey) {
                // Move to random empty cell
                for (Cell neighbor : neighbors) {
                    if (neighbor.isEmpty()) {
                        int oldX = predator.getX();
                        int oldY = predator.getY();
                        
                        for (int i = 0; i < Ecosystem.GRID_SIZE; i++) {
                            for (int j = 0; j < Ecosystem.GRID_SIZE; j++) {
                                if (ecosystem.getCell(i, j) == neighbor) {
                                    ecosystem.moveAnimal(predator, i, j);
                                    turnEvents.add(String.format("Predator moved from (%d,%d) to (%d,%d)", 
                                                                oldX, oldY, i, j));
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
        else if (animal instanceof ThirdSpecies third) {
            // Third species: opportunistic, can hunt prey or weak predators
            for (Cell neighbor : neighbors) {
                if (neighbor.hasAnimal()) {
                    Animal target = neighbor.getAnimal();
                    if (target instanceof Prey || 
                        (target instanceof Predator p && p.getEnergy() < 5)) {
                        
                        int oldX = third.getX();
                        int oldY = third.getY();
                        
                        deadAnimals.add(target);
                        
                        for (int i = 0; i < Ecosystem.GRID_SIZE; i++) {
                            for (int j = 0; j < Ecosystem.GRID_SIZE; j++) {
                                if (ecosystem.getCell(i, j) == neighbor) {
                                    ecosystem.removeAnimal(target);
                                    ecosystem.moveAnimal(third, i, j);
                                    third.eat(4, currentTurn);
                                    turnEvents.add(String.format("ThirdSpecies at (%d,%d) hunted %s at (%d,%d)", 
                                                                oldX, oldY, target.getType(), i, j));
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            
            // If no target, move to empty cell
            for (Cell neighbor : neighbors) {
                if (neighbor.isEmpty()) {
                    int oldX = third.getX();
                    int oldY = third.getY();
                    
                    for (int i = 0; i < Ecosystem.GRID_SIZE; i++) {
                        for (int j = 0; j < Ecosystem.GRID_SIZE; j++) {
                            if (ecosystem.getCell(i, j) == neighbor) {
                                ecosystem.moveAnimal(third, i, j);
                                turnEvents.add(String.format("ThirdSpecies moved from (%d,%d) to (%d,%d)", 
                                                            oldX, oldY, i, j));
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Try to reproduce an animal
     */
    private Animal tryReproduce(Animal parent) {
        int x = parent.getX();
        int y = parent.getY();
        List<Cell> neighbors = ecosystem.getNeighbors(x, y);
        Collections.shuffle(neighbors, random);
        
        // Special rule for prey: must have survived 2 turns
        if (parent instanceof Prey prey) {
            if (prey.getTurnsSurvived() < 2) {
                return null;
            }
        }
        
        // Special rule for predator: must have eaten at least once in last 3 turns
        if (parent instanceof Predator predator) {
            if (!predator.hasEatenRecently(currentTurn)) {
                return null;
            }
        }
        
        // Find an empty adjacent cell
        for (Cell neighbor : neighbors) {
            if (neighbor.isEmpty()) {
                // Find coordinates and create offspring
                for (int i = 0; i < Ecosystem.GRID_SIZE; i++) {
                    for (int j = 0; j < Ecosystem.GRID_SIZE; j++) {
                        if (ecosystem.getCell(i, j) == neighbor) {
                            Animal offspring = createOffspring(parent, i, j);
                            if (offspring != null) {
                                parent.reproduce();  // Costs energy
                                return offspring;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Create offspring of the same type as parent
     */
    private Animal createOffspring(Animal parent, int x, int y) {
        if (parent instanceof Prey) {
            return new Prey(x, y);
        } else if (parent instanceof Predator) {
            return new Predator(x, y);
        } else if (parent instanceof ThirdSpecies) {
            return new ThirdSpecies(x, y);
        }
        return null;
    }
    
    /**
     * Start the simulation
     */
    public void start(int intervalMs, int maxTurns) {
        // This method signature is for compatibility
        // Actual timing will be handled by Scheduler
    }
    
    /**
     * Stop the simulation
     */
    public void stop() {
        // Handled by Scheduler
    }
    
    // Getters
    public int getTurnNumber() {
        return currentTurn;
    }
    
    public Map<String, Integer> getCounts() {
        return ecosystem.getCounts();
    }
    
    public List<String> getHistory() {
        return new ArrayList<>(turnEvents);
    }
    
    public Ecosystem getEcosystem() {
        return ecosystem;
    }
    
    public List<String> getTurnEvents() {
        return turnEvents;
    }
    
    public boolean isExtinct() {
        Map<String, Integer> counts = getCounts();
        int totalAnimals = counts.get("prey") + counts.get("predator") + counts.get("third");
        return totalAnimals == 0;
    }
}
