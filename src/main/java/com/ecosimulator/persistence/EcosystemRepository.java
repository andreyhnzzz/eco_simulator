package com.ecosimulator.persistence;

import com.ecosimulator.core.Ecosystem;
import com.ecosimulator.model.Cell;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Repository for ecosystem persistence
 * Manages ecosystem.txt, turn_state.txt, and simulation logs
 */
public class EcosystemRepository {
    private static final String ECOSYSTEM_FILE = "ecosystem.txt";
    private static final String TURN_STATE_FILE = "turn_state.txt";
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    private final Path ecosystemPath;
    private final Path turnStatePath;
    private final Gson gson;
    
    public EcosystemRepository() {
        this.ecosystemPath = Paths.get(ECOSYSTEM_FILE);
        this.turnStatePath = Paths.get(TURN_STATE_FILE);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    /**
     * Save the initial ecosystem state to ecosystem.txt (JSON format)
     * @param ecosystem the ecosystem to save
     * @param scenarioName name of the scenario
     */
    public void saveInitialEcosystem(Ecosystem ecosystem, String scenarioName) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("scenario", scenarioName);
            json.addProperty("gridSize", Ecosystem.GRID_SIZE);
            json.addProperty("timestamp", LocalDateTime.now().format(ISO_FORMAT));
            
            JsonArray cellsArray = new JsonArray();
            Cell[][] grid = ecosystem.getGrid();
            
            for (int i = 0; i < Ecosystem.GRID_SIZE; i++) {
                for (int j = 0; j < Ecosystem.GRID_SIZE; j++) {
                    Cell cell = grid[i][j];
                    if (!cell.isEmpty()) {
                        JsonObject cellObj = new JsonObject();
                        cellObj.addProperty("type", cell.getType());
                        cellObj.addProperty("x", i);
                        cellObj.addProperty("y", j);
                        cellsArray.add(cellObj);
                    }
                }
            }
            
            json.add("cells", cellsArray);
            
            String jsonString = gson.toJson(json);
            Files.write(ecosystemPath, jsonString.getBytes(StandardCharsets.UTF_8),
                       StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
        } catch (IOException e) {
            System.err.println("Error saving ecosystem: " + e.getMessage());
        }
    }
    
    /**
     * Initialize the turn state log file
     */
    public void initializeTurnLog() {
        try {
            String header = "=== ECOSYSTEM SIMULATION LOG ===\n" +
                          "Started: " + LocalDateTime.now().format(ISO_FORMAT) + "\n\n";
            Files.write(turnStatePath, header.getBytes(StandardCharsets.UTF_8),
                       StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error initializing turn log: " + e.getMessage());
        }
    }
    
    /**
     * Append turn state to turn_state.txt
     * Format:
     * Turn: N
     * ISO Timestamp
     * Counts: prey, predators, thirdSpecies, empty
     * Turn events (log lines)
     * [Optional: grid snapshot]
     * 
     * @param turnNumber current turn number
     * @param counts map of creature counts
     * @param events list of turn events
     * @param ecosystem the ecosystem (for optional grid snapshot)
     */
    public void appendTurnState(int turnNumber, Map<String, Integer> counts, 
                                java.util.List<String> events, Ecosystem ecosystem) {
        try {
            StringBuilder block = new StringBuilder();
            block.append("----------------------------------------\n");
            block.append("Turn: ").append(turnNumber).append("\n");
            block.append(LocalDateTime.now().format(ISO_FORMAT)).append("\n");
            block.append(String.format("Counts: prey=%d, predators=%d, thirdSpecies=%d, empty=%d\n",
                                     counts.getOrDefault("prey", 0),
                                     counts.getOrDefault("predator", 0),
                                     counts.getOrDefault("third", 0),
                                     counts.getOrDefault("empty", 0)));
            
            block.append("Turn events:\n");
            for (String event : events) {
                block.append("  - ").append(event).append("\n");
            }
            
            // Optional: Add grid snapshot
            block.append("Grid snapshot:\n");
            Cell[][] grid = ecosystem.getGrid();
            for (int i = 0; i < Ecosystem.GRID_SIZE; i++) {
                block.append("  ");
                for (int j = 0; j < Ecosystem.GRID_SIZE; j++) {
                    Cell cell = grid[i][j];
                    if (cell.isEmpty()) {
                        block.append(".");
                    } else {
                        String type = cell.getType();
                        switch (type) {
                            case "prey" -> block.append("r");  // prey
                            case "predator" -> block.append("P");  // Predator
                            case "third" -> block.append("T");  // Third
                            default -> block.append("?");
                        }
                    }
                    block.append(" ");
                }
                block.append("\n");
            }
            
            block.append("\n");
            
            Files.write(turnStatePath, block.toString().getBytes(StandardCharsets.UTF_8),
                       StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            
        } catch (IOException e) {
            System.err.println("Error appending turn state: " + e.getMessage());
        }
    }
    
    /**
     * Finalize the turn log with summary
     * @param totalTurns total number of turns executed
     * @param finalCounts final counts of creatures
     * @param extinctionTurn turn when extinction occurred (-1 if no extinction)
     */
    public void finalizeTurnLog(int totalTurns, Map<String, Integer> finalCounts, int extinctionTurn) {
        try {
            StringBuilder footer = new StringBuilder();
            footer.append("========================================\n");
            footer.append("SIMULATION COMPLETE\n");
            footer.append("Ended: ").append(LocalDateTime.now().format(ISO_FORMAT)).append("\n");
            footer.append("Total turns: ").append(totalTurns).append("\n");
            footer.append(String.format("Final counts: prey=%d, predators=%d, thirdSpecies=%d\n",
                                      finalCounts.getOrDefault("prey", 0),
                                      finalCounts.getOrDefault("predator", 0),
                                      finalCounts.getOrDefault("third", 0)));
            
            if (extinctionTurn > 0) {
                footer.append("Extinction occurred at turn: ").append(extinctionTurn).append("\n");
            } else {
                footer.append("No extinction occurred\n");
            }
            
            footer.append("========================================\n");
            
            Files.write(turnStatePath, footer.toString().getBytes(StandardCharsets.UTF_8),
                       StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            
        } catch (IOException e) {
            System.err.println("Error finalizing turn log: " + e.getMessage());
        }
    }
    
    /**
     * Get the ecosystem file path
     * @return the path
     */
    public Path getEcosystemPath() {
        return ecosystemPath;
    }
    
    /**
     * Get the turn state file path
     * @return the path
     */
    public Path getTurnStatePath() {
        return turnStatePath;
    }
}
