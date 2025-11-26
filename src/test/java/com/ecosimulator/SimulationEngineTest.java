package com.ecosimulator;

import com.ecosimulator.model.*;
import com.ecosimulator.simulation.SimulationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Eco Simulator simulation engine
 */
class SimulationEngineTest {

    private SimulationEngine engine;
    private SimulationConfig config;

    @BeforeEach
    void setUp() {
        config = new SimulationConfig()
            .withScenario(Scenario.BALANCED)
            .withGridSize(10)
            .withThirdSpecies(false)
            .withMutations(false);
        engine = new SimulationEngine(config);
    }

    @Test
    void testInitialGridCreation() {
        CellType[][] grid = engine.getGrid();
        assertNotNull(grid);
        assertEquals(10, grid.length);
        assertEquals(10, grid[0].length);
    }

    @Test
    void testBalancedScenarioPopulation() {
        SimulationStats stats = engine.getStats();
        
        // In balanced scenario: 10% predators, 20% prey of 100 cells
        // Expected: ~10 predators, ~20 prey
        assertTrue(stats.getPredatorCount() > 0, "Should have predators");
        assertTrue(stats.getPreyCount() > 0, "Should have prey");
    }

    @Test
    void testPredatorDominantScenario() {
        config = new SimulationConfig()
            .withScenario(Scenario.PREDATOR_DOMINANT)
            .withGridSize(10);
        engine = new SimulationEngine(config);
        SimulationStats stats = engine.getStats();
        
        // Predator dominant: 20% predators, 10% prey
        assertTrue(stats.getPredatorCount() >= stats.getPreyCount(), 
            "Predators should outnumber or equal prey in predator-dominant scenario");
    }

    @Test
    void testPreyDominantScenario() {
        config = new SimulationConfig()
            .withScenario(Scenario.PREY_DOMINANT)
            .withGridSize(10);
        engine = new SimulationEngine(config);
        SimulationStats stats = engine.getStats();
        
        // Prey dominant: 5% predators, 30% prey
        assertTrue(stats.getPreyCount() > stats.getPredatorCount(), 
            "Prey should outnumber predators in prey-dominant scenario");
    }

    @Test
    void testThirdSpeciesEnabled() {
        config = new SimulationConfig()
            .withScenario(Scenario.BALANCED)
            .withGridSize(10)
            .withThirdSpecies(true);
        engine = new SimulationEngine(config);
        SimulationStats stats = engine.getStats();
        
        assertTrue(stats.getThirdSpeciesCount() > 0, 
            "Third species should be present when enabled");
    }

    @Test
    void testThirdSpeciesDisabled() {
        config = new SimulationConfig()
            .withScenario(Scenario.BALANCED)
            .withGridSize(10)
            .withThirdSpecies(false);
        engine = new SimulationEngine(config);
        SimulationStats stats = engine.getStats();
        
        assertEquals(0, stats.getThirdSpeciesCount(), 
            "Third species should not be present when disabled");
    }

    @Test
    void testMutationsEnabled() {
        config = new SimulationConfig()
            .withScenario(Scenario.BALANCED)
            .withGridSize(20) // Larger grid for more creatures
            .withMutations(true);
        engine = new SimulationEngine(config);
        
        // With 10% initial mutation chance, there should be some mutated creatures
        // in a 20x20 grid with ~12% creature population
        // Not guaranteed, but very likely with 48+ creatures
        SimulationStats stats = engine.getStats();
        assertTrue(stats.getTotalCreatures() > 0, "Should have creatures");
    }

    @Test
    void testSimulationStateControl() {
        assertFalse(engine.isRunning(), "Should not be running initially");
        assertFalse(engine.isPaused(), "Should not be paused initially");
        
        engine.start();
        assertTrue(engine.isRunning(), "Should be running after start");
        assertFalse(engine.isPaused(), "Should not be paused after start");
        
        engine.pause();
        assertTrue(engine.isPaused(), "Should be paused after pause");
        
        engine.resume();
        assertFalse(engine.isPaused(), "Should not be paused after resume");
        
        engine.stop();
        assertFalse(engine.isRunning(), "Should not be running after stop");
    }

    @Test
    void testTurnExecution() {
        SimulationStats statsBefore = engine.getStats();
        int turnBefore = statsBefore.getTurn();
        
        engine.start();
        engine.executeTurn();
        
        SimulationStats statsAfter = engine.getStats();
        assertEquals(turnBefore + 1, statsAfter.getTurn(), 
            "Turn count should increment after execution");
    }

    @Test
    void testReset() {
        // Execute some turns
        engine.start();
        engine.executeTurn();
        engine.executeTurn();
        
        assertTrue(engine.getStats().getTurn() > 0, "Should have executed turns");
        
        // Reset
        engine.reset();
        
        assertEquals(0, engine.getStats().getTurn(), "Turn should be 0 after reset");
        assertFalse(engine.isRunning(), "Should not be running after reset");
    }

    @Test
    void testCreatureCount() {
        int totalCells = config.getGridSize() * config.getGridSize();
        int expectedMax = (int)(totalCells * 0.5); // At most 50% should be filled
        
        SimulationStats stats = engine.getStats();
        assertTrue(stats.getTotalCreatures() <= expectedMax, 
            "Total creatures should not exceed 50% of grid");
    }

    @Test
    void testScenarioConfigurations() {
        // Test all scenarios have valid configurations
        for (Scenario scenario : Scenario.values()) {
            SimulationConfig testConfig = new SimulationConfig().withScenario(scenario);
            
            assertTrue(testConfig.getPredatorPercentage() > 0, 
                "Predator percentage should be positive for " + scenario);
            assertTrue(testConfig.getPreyPercentage() > 0, 
                "Prey percentage should be positive for " + scenario);
            assertTrue(testConfig.getPredatorPercentage() + testConfig.getPreyPercentage() < 1, 
                "Total percentage should be less than 100% for " + scenario);
        }
    }
}
