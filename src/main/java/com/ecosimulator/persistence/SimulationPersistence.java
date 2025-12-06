package com.ecosimulator.persistence;

import com.ecosimulator.model.CellType;
import com.ecosimulator.model.SimulationConfig;
import com.ecosimulator.model.SimulationStats;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Persistence class for saving simulation states to files.
 * Saves initial ecosystem configuration to ecosistema.txt
 * Saves all turn states to estado_turnos.txt
 * Implements AutoCloseable for proper resource management.
 */
public class SimulationPersistence implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(SimulationPersistence.class.getName());
    
    private static final String ECOSISTEMA_FILE = "ecosistema.txt";
    private static final String ESTADO_TURNOS_FILE = "estado_turnos.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private PrintWriter turnWriter;
    private boolean initialized;
    
    public SimulationPersistence() {
        this.initialized = false;
    }
    
    /**
     * Save the initial ecosystem configuration to ecosistema.txt
     * @param grid the initial grid state
     * @param config the simulation configuration
     * @param stats the initial statistics
     */
    public void saveInitialEcosystem(CellType[][] grid, SimulationConfig config, SimulationStats stats) {
        try (PrintWriter writer = new PrintWriter(
                new BufferedWriter(new FileWriter(ECOSISTEMA_FILE, StandardCharsets.UTF_8)))) {
            
            writer.println("===========================================");
            writer.println("   ECO SIMULATOR - INITIAL ECOSYSTEM");
            writer.println("===========================================");
            writer.println("Date: " + LocalDateTime.now().format(DATE_FORMAT));
            writer.println();
            
            // Configuration
            writer.println("--- CONFIGURATION ---");
            writer.println("Scenario: " + config.getScenario().getDisplayName());
            writer.println("Grid Size: " + config.getGridSize() + " x " + config.getGridSize());
            writer.println("Third Species Enabled: " + (config.isThirdSpeciesEnabled() ? "Yes" : "No"));
            writer.println("Mutations Enabled: " + (config.isMutationsEnabled() ? "Yes" : "No"));
            writer.println("Turn Delay: " + config.getTurnDelayMs() + " ms");
            writer.println("Max Turns: " + config.getMaxTurns());
            writer.println();
            
            // Initial population
            writer.println("--- INITIAL POPULATION ---");
            writer.println("Predators: " + stats.getPredatorCount());
            writer.println("Prey: " + stats.getPreyCount());
            writer.println("Third Species: " + stats.getThirdSpeciesCount());
            writer.println("Mutated: " + stats.getMutatedCount());
            writer.println("Total: " + stats.getTotalCreatures());
            writer.println();
            
            // Grid matrix
            writer.println("--- INITIAL GRID ---");
            writer.println("Legend: P=Predator, R=Prey, T=Third Species, .=Empty");
            writer.println();
            writeGrid(writer, grid);
            
            LOGGER.info("Initial ecosystem saved to " + ECOSISTEMA_FILE);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save initial ecosystem", e);
        }
    }
    
    /**
     * Initialize the turn states file for a new simulation
     * @param config the simulation configuration
     */
    public void initializeTurnLog(SimulationConfig config) {
        try {
            if (turnWriter != null) {
                turnWriter.close();
            }
            
            turnWriter = new PrintWriter(
                    new BufferedWriter(new FileWriter(ESTADO_TURNOS_FILE, StandardCharsets.UTF_8)));
            
            turnWriter.println("===========================================");
            turnWriter.println("   ECO SIMULATOR - TURN STATES LOG");
            turnWriter.println("===========================================");
            turnWriter.println("Date: " + LocalDateTime.now().format(DATE_FORMAT));
            turnWriter.println("Scenario: " + config.getScenario().getDisplayName());
            turnWriter.println("Third Species: " + (config.isThirdSpeciesEnabled() ? "Enabled" : "Disabled"));
            turnWriter.println("Mutations: " + (config.isMutationsEnabled() ? "Enabled" : "Disabled"));
            turnWriter.println();
            turnWriter.println("===========================================");
            turnWriter.flush();
            
            initialized = true;
            LOGGER.info("Turn log initialized: " + ESTADO_TURNOS_FILE);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to initialize turn log", e);
            initialized = false;
        }
    }
    
    /**
     * Log a turn's state to estado_turnos.txt
     * @param turn the turn number
     * @param grid the current grid state
     * @param stats the current statistics
     * @param events events that occurred this turn (movements, deaths, births, etc.)
     */
    public void logTurnState(int turn, CellType[][] grid, SimulationStats stats, String events) {
        if (!initialized || turnWriter == null) {
            LOGGER.warning("Turn log not initialized - cannot log turn " + turn);
            return;
        }
        
        try {
            turnWriter.println();
            turnWriter.println("--- TURN " + turn + " ---");
            turnWriter.println("Predators: " + stats.getPredatorCount() + 
                             " | Prey: " + stats.getPreyCount() + 
                             " | Third: " + stats.getThirdSpeciesCount() +
                             " | Mutated: " + stats.getMutatedCount());
            turnWriter.println("Births this turn: " + stats.getBirthsThisTurn() + 
                             " | Deaths this turn: " + stats.getDeathsThisTurn());
            
            if (events != null && !events.isEmpty()) {
                turnWriter.println("Events: " + events);
            }
            
            turnWriter.println();
            writeGrid(turnWriter, grid);
            turnWriter.flush();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to log turn state", e);
        }
    }
    
    /**
     * Finalize the turn log with final results
     * @param stats final statistics
     * @param extinctionTurn turn when extinction occurred (-1 if none)
     */
    public void finalizeTurnLog(SimulationStats stats, int extinctionTurn) {
        if (!initialized || turnWriter == null) {
            return;
        }
        
        try {
            turnWriter.println();
            turnWriter.println("===========================================");
            turnWriter.println("   SIMULATION COMPLETE");
            turnWriter.println("===========================================");
            turnWriter.println();
            turnWriter.println("--- FINAL RESULTS ---");
            turnWriter.println("Total Turns: " + stats.getTurn());
            turnWriter.println("Final Predators: " + stats.getPredatorCount());
            turnWriter.println("Final Prey: " + stats.getPreyCount());
            turnWriter.println("Final Third Species: " + stats.getThirdSpeciesCount());
            turnWriter.println("Final Mutated: " + stats.getMutatedCount());
            turnWriter.println("Total Creatures: " + stats.getTotalCreatures());
            
            if (extinctionTurn > 0) {
                turnWriter.println("Extinction occurred at turn: " + extinctionTurn);
            } else {
                turnWriter.println("No extinction occurred");
            }
            
            turnWriter.println();
            turnWriter.println("Result: " + stats.getWinner());
            turnWriter.println("===========================================");
            turnWriter.flush();
            
            LOGGER.info("Turn log finalized");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to finalize turn log", e);
        }
    }
    
    /**
     * Close the turn log file
     */
    public void close() {
        if (turnWriter != null) {
            turnWriter.close();
            turnWriter = null;
        }
        initialized = false;
    }
    
    /**
     * Write the grid matrix to a writer
     */
    private void writeGrid(PrintWriter writer, CellType[][] grid) {
        int size = grid.length;
        
        // Column numbers header
        writer.print("   ");
        for (int j = 0; j < size; j++) {
            writer.print((j % 10));
        }
        writer.println();
        
        // Top border
        writer.print("  +");
        for (int j = 0; j < size; j++) {
            writer.print("-");
        }
        writer.println("+");
        
        // Grid rows
        for (int i = 0; i < size; i++) {
            writer.printf("%2d|", i);
            for (int j = 0; j < size; j++) {
                char symbol = getCellSymbol(grid[i][j]);
                writer.print(symbol);
            }
            writer.println("|");
        }
        
        // Bottom border
        writer.print("  +");
        for (int j = 0; j < size; j++) {
            writer.print("-");
        }
        writer.println("+");
    }
    
    /**
     * Get a single character symbol for a cell type
     */
    private char getCellSymbol(CellType cellType) {
        return switch (cellType) {
            case PREDATOR -> 'P';
            case PREY -> 'R';
            case THIRD_SPECIES -> 'S';
            case CORPSE -> 'X';
            case WATER -> 'W';
            case FOOD -> 'F';
            case EMPTY -> '.';
        };
    }
    
    /**
     * Get the ecosistema.txt file path
     */
    public static Path getEcosistemaPath() {
        return Paths.get(ECOSISTEMA_FILE);
    }
    
    /**
     * Get the estado_turnos.txt file path
     */
    public static Path getEstadoTurnosPath() {
        return Paths.get(ESTADO_TURNOS_FILE);
    }
    
    /**
     * Check if the ecosistema.txt file exists
     */
    public static boolean ecosistemaExists() {
        return Files.exists(getEcosistemaPath());
    }
    
    /**
     * Check if the estado_turnos.txt file exists
     */
    public static boolean estadoTurnosExists() {
        return Files.exists(getEstadoTurnosPath());
    }
}
