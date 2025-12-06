package com.ecosimulator;

import com.ecosimulator.model.*;
import com.ecosimulator.persistence.SimulationPersistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SimulationPersistence class
 */
class SimulationPersistenceTest {
    
    private SimulationPersistence persistence;
    
    @BeforeEach
    void setUp() {
        persistence = new SimulationPersistence();
    }
    
    @AfterEach
    void tearDown() throws IOException {
        persistence.close();
        // Clean up test files
        Files.deleteIfExists(SimulationPersistence.getEcosistemaPath());
        Files.deleteIfExists(SimulationPersistence.getEstadoTurnosPath());
    }
    
    @Test
    void testSaveInitialEcosystem() {
        CellType[][] grid = new CellType[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                grid[i][j] = CellType.EMPTY;
            }
        }
        grid[0][0] = CellType.PREDATOR;
        grid[2][2] = CellType.PREY;
        grid[4][4] = CellType.THIRD_SPECIES;
        
        SimulationConfig config = new SimulationConfig()
            .withScenario(Scenario.BALANCED)
            .withGridSize(5)
            .withThirdSpecies(true)
            .withMutations(false);
        
        SimulationStats stats = new SimulationStats();
        stats.setPredatorCount(1);
        stats.setPreyCount(1);
        stats.setThirdSpeciesCount(1);
        
        persistence.saveInitialEcosystem(grid, config, stats);
        
        assertTrue(SimulationPersistence.ecosistemaExists(), 
            "ecosistema.txt should be created");
    }
    
    @Test
    void testInitializeAndLogTurnState() {
        SimulationConfig config = new SimulationConfig()
            .withScenario(Scenario.BALANCED)
            .withGridSize(5);
        
        persistence.initializeTurnLog(config);
        
        CellType[][] grid = new CellType[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                grid[i][j] = CellType.EMPTY;
            }
        }
        grid[0][0] = CellType.PREDATOR;
        grid[2][2] = CellType.PREY;
        
        SimulationStats stats = new SimulationStats();
        stats.setPredatorCount(1);
        stats.setPreyCount(1);
        
        persistence.logTurnState(1, grid, stats, "Test event");
        persistence.finalizeTurnLog(stats, -1);
        persistence.close();
        
        assertTrue(SimulationPersistence.estadoTurnosExists(),
            "estado_turnos.txt should be created");
    }
    
    @Test
    void testFilePaths() {
        Path ecosistemaPath = SimulationPersistence.getEcosistemaPath();
        Path estadoTurnosPath = SimulationPersistence.getEstadoTurnosPath();
        
        assertNotNull(ecosistemaPath);
        assertNotNull(estadoTurnosPath);
        assertTrue(ecosistemaPath.toString().contains("ecosistema"));
        assertTrue(estadoTurnosPath.toString().contains("estado_turnos"));
    }
}
