package com.ecosimulator.core;

import com.ecosimulator.model.*;

import java.util.*;

/**
 * Manages the 10x10 ecosystem grid and animal populations
 */
public class Ecosystem {
    public static final int GRID_SIZE = 10;
    
    private Cell[][] grid;
    private List<Animal> animals;
    private Random random;
    
    public Ecosystem() {
        this.grid = new Cell[GRID_SIZE][GRID_SIZE];
        this.animals = new ArrayList<>();
        this.random = new Random();
        initializeGrid();
    }
    
    /**
     * Initialize empty grid
     */
    private void initializeGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = new Cell();
            }
        }
    }
    
    /**
     * Load a scenario configuration
     * @param config the scenario configuration
     */
    public void loadScenario(ScenarioConfig config) {
        // Clear existing
        initializeGrid();
        animals.clear();
        
        // Place animals according to configuration
        for (ScenarioConfig.CellData cellData : config.getCells()) {
            int x = cellData.x;
            int y = cellData.y;
            String type = cellData.type;
            
            if (x < 0 || x >= GRID_SIZE || y < 0 || y >= GRID_SIZE) {
                continue;  // Skip invalid positions
            }
            
            Animal animal = createAnimal(type, x, y);
            if (animal != null) {
                animals.add(animal);
                grid[x][y].setAnimal(animal);
            }
        }
    }
    
    /**
     * Create an animal of the specified type
     * @param type animal type ("prey", "predator", "third")
     * @param x x coordinate
     * @param y y coordinate
     * @return the created animal
     */
    private Animal createAnimal(String type, int x, int y) {
        return switch (type.toLowerCase()) {
            case "prey" -> new Prey(x, y);
            case "predator" -> new Predator(x, y);
            case "third", "third_species" -> new ThirdSpecies(x, y);
            default -> null;
        };
    }
    
    /**
     * Get cell at position
     * @param x x coordinate
     * @param y y coordinate
     * @return the cell, or null if out of bounds
     */
    public Cell getCell(int x, int y) {
        if (x < 0 || x >= GRID_SIZE || y < 0 || y >= GRID_SIZE) {
            return null;
        }
        return grid[x][y];
    }
    
    /**
     * Get all animals in the ecosystem
     * @return list of animals
     */
    public List<Animal> getAnimals() {
        return animals;
    }
    
    /**
     * Get neighboring cells (N, S, E, W)
     * @param x x coordinate
     * @param y y coordinate
     * @return list of neighboring cells
     */
    public List<Cell> getNeighbors(int x, int y) {
        List<Cell> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            Cell cell = getCell(newX, newY);
            if (cell != null) {
                neighbors.add(cell);
            }
        }
        return neighbors;
    }
    
    /**
     * Get all neighboring cells including diagonals
     * @param x x coordinate
     * @param y y coordinate
     * @return list of all neighbors
     */
    public List<Cell> getAllNeighbors(int x, int y) {
        List<Cell> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, 
                             {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            Cell cell = getCell(newX, newY);
            if (cell != null) {
                neighbors.add(cell);
            }
        }
        return neighbors;
    }
    
    /**
     * Add a new animal to the ecosystem
     * @param animal the animal to add
     * @return true if added successfully
     */
    public boolean addAnimal(Animal animal) {
        int x = animal.getX();
        int y = animal.getY();
        Cell cell = getCell(x, y);
        if (cell != null && cell.isEmpty()) {
            animals.add(animal);
            cell.setAnimal(animal);
            return true;
        }
        return false;
    }
    
    /**
     * Remove an animal from the ecosystem
     * @param animal the animal to remove
     */
    public void removeAnimal(Animal animal) {
        animals.remove(animal);
        int x = animal.getX();
        int y = animal.getY();
        Cell cell = getCell(x, y);
        if (cell != null && cell.getAnimal() == animal) {
            cell.clear();
        }
    }
    
    /**
     * Move an animal to a new position
     * @param animal the animal to move
     * @param newX new x coordinate
     * @param newY new y coordinate
     * @return true if move was successful
     */
    public boolean moveAnimal(Animal animal, int newX, int newY) {
        Cell targetCell = getCell(newX, newY);
        if (targetCell == null || !targetCell.isEmpty()) {
            return false;
        }
        
        // Clear old position
        Cell oldCell = getCell(animal.getX(), animal.getY());
        if (oldCell != null) {
            oldCell.clear();
        }
        
        // Update animal position
        animal.move(newX, newY);
        
        // Set new position
        targetCell.setAnimal(animal);
        return true;
    }
    
    /**
     * Get counts of each animal type
     * @return map of type to count
     */
    public Map<String, Integer> getCounts() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("prey", 0);
        counts.put("predator", 0);
        counts.put("third", 0);
        counts.put("empty", 0);
        
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Cell cell = grid[i][j];
                if (cell.isEmpty()) {
                    counts.put("empty", counts.get("empty") + 1);
                } else {
                    String type = cell.getType();
                    counts.put(type, counts.getOrDefault(type, 0) + 1);
                }
            }
        }
        
        return counts;
    }
    
    /**
     * Get the grid
     * @return the grid
     */
    public Cell[][] getGrid() {
        return grid;
    }
    
    /**
     * Clear the entire ecosystem
     */
    public void clear() {
        initializeGrid();
        animals.clear();
    }
}
