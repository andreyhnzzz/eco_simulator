package com.ecosimulator;

import com.ecosimulator.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the model classes
 */
class ModelTest {

    @Test
    void testCreatureInitialization() {
        Creature predator = new Creature(CellType.PREDATOR, 5, 5);
        assertEquals(CellType.PREDATOR, predator.getType());
        assertEquals(5, predator.getRow());
        assertEquals(5, predator.getCol());
        assertEquals(15, predator.getEnergy()); // Predators start with 15 energy (balanced)
        assertFalse(predator.isMutated());
    }

    @Test
    void testCreatureMovement() {
        Creature prey = new Creature(CellType.PREY, 0, 0);
        int initialEnergy = prey.getEnergy();
        
        prey.move(1, 1);
        
        assertEquals(1, prey.getRow());
        assertEquals(1, prey.getCol());
        assertEquals(initialEnergy - 1, prey.getEnergy(), "Movement should cost energy");
    }

    @Test
    void testCreatureEating() {
        Creature predator = new Creature(CellType.PREDATOR, 0, 0);
        int initialEnergy = predator.getEnergy();
        
        predator.eat(10);
        
        assertEquals(initialEnergy + 10, predator.getEnergy());
    }

    @Test
    void testCreatureDeath() {
        Creature creature = new Creature(CellType.PREY, 0, 0);
        creature.setEnergy(0);
        
        assertTrue(creature.isDead());
    }

    @Test
    void testCreatureMutation() {
        Creature creature = new Creature(CellType.PREDATOR, 0, 0);
        assertFalse(creature.isMutated());
        assertEquals(1.0, creature.getMutationBonus());
        
        creature.mutate();
        
        assertTrue(creature.isMutated());
        // Mutation bonus should be one of the three types: 1.3, 1.4, or 1.5
        assertTrue(creature.getMutationBonus() >= 1.3 && creature.getMutationBonus() <= 1.5,
                "Mutation bonus should be between 1.3 and 1.5");
        assertNotEquals(MutationType.NONE, creature.getMutationType());
    }

    @Test
    void testCreatureReproduction() {
        Creature creature = new Creature(CellType.PREDATOR, 0, 0);
        creature.setEnergy(40);
        
        assertTrue(creature.canReproduce(), "Should be able to reproduce with enough energy");
        
        creature.reproduce();
        
        assertEquals(20, creature.getEnergy(), "Energy should be halved after reproduction");
    }

    @Test
    void testSimulationConfigDefaults() {
        SimulationConfig config = new SimulationConfig();
        
        assertEquals(Scenario.BALANCED, config.getScenario());
        assertFalse(config.isThirdSpeciesEnabled());
        assertFalse(config.isMutationsEnabled());
        assertEquals(20, config.getGridSize());
        assertEquals(1000, config.getTurnDelayMs()); // Slower pace: 1 second default
        assertEquals(200, config.getMaxTurns());
    }

    @Test
    void testSimulationConfigBuilder() {
        SimulationConfig config = new SimulationConfig()
            .withScenario(Scenario.PREDATOR_DOMINANT)
            .withThirdSpecies(true)
            .withMutations(true)
            .withGridSize(30)
            .withTurnDelay(1000);
        
        assertEquals(Scenario.PREDATOR_DOMINANT, config.getScenario());
        assertTrue(config.isThirdSpeciesEnabled());
        assertTrue(config.isMutationsEnabled());
        assertEquals(30, config.getGridSize());
        assertEquals(1000, config.getTurnDelayMs());
    }

    @Test
    void testScenarioPercentages() {
        SimulationConfig balanced = new SimulationConfig().withScenario(Scenario.BALANCED);
        SimulationConfig predatorDom = new SimulationConfig().withScenario(Scenario.PREDATOR_DOMINANT);
        SimulationConfig preyDom = new SimulationConfig().withScenario(Scenario.PREY_DOMINANT);
        
        // Balanced
        assertEquals(0.10, balanced.getPredatorPercentage());
        assertEquals(0.20, balanced.getPreyPercentage());
        
        // Predator Dominant
        assertEquals(0.20, predatorDom.getPredatorPercentage());
        assertEquals(0.10, predatorDom.getPreyPercentage());
        
        // Prey Dominant
        assertEquals(0.05, preyDom.getPredatorPercentage());
        assertEquals(0.30, preyDom.getPreyPercentage());
    }

    @Test
    void testThirdSpeciesPercentages() {
        SimulationConfig config = new SimulationConfig()
            .withScenario(Scenario.BALANCED)
            .withThirdSpecies(true);
        
        assertEquals(0.10, config.getThirdSpeciesPercentage());
        
        config.setThirdSpeciesEnabled(false);
        assertEquals(0.0, config.getThirdSpeciesPercentage());
    }

    @Test
    void testSimulationStats() {
        SimulationStats stats = new SimulationStats();
        
        assertEquals(0, stats.getTurn());
        assertEquals(0, stats.getPredatorCount());
        assertEquals(0, stats.getPreyCount());
        assertEquals(0, stats.getThirdSpeciesCount());
        assertEquals(0, stats.getTotalCreatures());
    }

    @Test
    void testSimulationStatsUpdate() {
        SimulationStats stats = new SimulationStats();
        stats.setPredatorCount(10);
        stats.setPreyCount(20);
        stats.setThirdSpeciesCount(5);
        
        assertEquals(35, stats.getTotalCreatures());
    }

    @Test
    void testSimulationStatsExtinction() {
        SimulationStats stats = new SimulationStats();
        stats.setPredatorCount(0);
        stats.setPreyCount(10);
        
        assertTrue(stats.isExtinct(), "Should be extinct when predators are 0");
        
        stats.setPredatorCount(5);
        stats.setPreyCount(0);
        
        assertTrue(stats.isExtinct(), "Should be extinct when prey are 0");
        
        stats.setPreyCount(5);
        assertFalse(stats.isExtinct(), "Should not be extinct when both exist");
    }

    @Test
    void testSimulationStatsWinner() {
        SimulationStats stats = new SimulationStats();
        
        stats.setPredatorCount(0);
        stats.setPreyCount(0);
        assertTrue(stats.getWinner().contains("Extinction"));
        
        stats.setPredatorCount(0);
        stats.setPreyCount(10);
        assertTrue(stats.getWinner().contains("Prey"));
        
        stats.setPredatorCount(10);
        stats.setPreyCount(0);
        assertTrue(stats.getWinner().contains("Predator"));
        
        stats.setPreyCount(10);
        assertEquals("Ongoing", stats.getWinner());
    }

    @Test
    void testCellTypeProperties() {
        assertEquals("Predator", CellType.PREDATOR.getDisplayName());
        assertEquals("P", CellType.PREDATOR.getSymbol());
        assertEquals("#D32F2F", CellType.PREDATOR.getColor());
        
        assertEquals("Prey", CellType.PREY.getDisplayName());
        assertEquals("R", CellType.PREY.getSymbol());
        assertEquals("#1976D2", CellType.PREY.getColor());
    }

    @Test
    void testScenarioProperties() {
        assertEquals("Equilibrado", Scenario.BALANCED.getDisplayName());
        assertEquals("Depredadores Dominantes", Scenario.PREDATOR_DOMINANT.getDisplayName());
        assertEquals("Presas Dominantes", Scenario.PREY_DOMINANT.getDisplayName());
    }
}
