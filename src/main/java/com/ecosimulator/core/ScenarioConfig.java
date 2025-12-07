package com.ecosimulator.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and parses scenario configuration from JSON files
 */
public class ScenarioConfig {
    
    public static class CellData {
        public String type;
        public int x;
        public int y;
        
        public CellData(String type, int x, int y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }
    
    private String name;
    private String description;
    private int gridSize;
    private List<CellData> cells;
    
    public ScenarioConfig() {
        this.cells = new ArrayList<>();
    }
    
    /**
     * Load a scenario from a JSON file
     * @param filePath path to the JSON file
     * @return ScenarioConfig object
     * @throws IOException if file cannot be read
     */
    public static ScenarioConfig loadFromFile(String filePath) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            
            ScenarioConfig config = new ScenarioConfig();
            config.name = json.get("name").getAsString();
            config.description = json.get("description").getAsString();
            config.gridSize = json.get("gridSize").getAsInt();
            
            JsonArray cellsArray = json.getAsJsonArray("cells");
            for (JsonElement element : cellsArray) {
                JsonObject cellObj = element.getAsJsonObject();
                String type = cellObj.get("type").getAsString();
                int x = cellObj.get("x").getAsInt();
                int y = cellObj.get("y").getAsInt();
                config.cells.add(new CellData(type, x, y));
            }
            
            return config;
        }
    }
    
    /**
     * Get list of available scenario files
     * @return array of scenario file paths
     */
    public static String[] getAvailableScenarios() {
        return new String[] {
            "data/scenarios/balanced_scenario.json",
            "data/scenarios/dominant_predator_scenario.json",
            "data/scenarios/dominant_prey_scenario.json"
        };
    }
    
    /**
     * Get display names for scenarios
     * @return array of scenario display names
     */
    public static String[] getScenarioNames() {
        return new String[] {
            "Balanced",
            "Dominant Predator",
            "Dominant Prey"
        };
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getGridSize() {
        return gridSize;
    }
    
    public List<CellData> getCells() {
        return cells;
    }
}
