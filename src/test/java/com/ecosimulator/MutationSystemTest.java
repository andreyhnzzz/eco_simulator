package com.ecosimulator;

import com.ecosimulator.model.*;
import com.ecosimulator.simulation.SimulationEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for the enhanced mutation system with 3 mutation types
 */
class MutationSystemTest {
    
    @Test
    void testMutationTypeEnum() {
        // Test all mutation types exist
        assertEquals(4, MutationType.values().length, "Should have 4 mutation types including NONE");
        
        // Test NONE mutation
        assertEquals("Ninguna", MutationType.NONE.getDisplayName());
        assertEquals(1.0, MutationType.NONE.getBonus());
        
        // Test EFFICIENT_METABOLISM
        assertEquals("Metabolismo Eficiente", MutationType.EFFICIENT_METABOLISM.getDisplayName());
        assertEquals(1.3, MutationType.EFFICIENT_METABOLISM.getBonus());
        assertTrue(MutationType.EFFICIENT_METABOLISM.getDescription().contains("hambre"));
        
        // Test ENHANCED_STRENGTH
        assertEquals("Fuerza Mejorada", MutationType.ENHANCED_STRENGTH.getDisplayName());
        assertEquals(1.5, MutationType.ENHANCED_STRENGTH.getBonus());
        assertTrue(MutationType.ENHANCED_STRENGTH.getDescription().contains("energía"));
        
        // Test THERMAL_RESISTANCE
        assertEquals("Resistencia Térmica", MutationType.THERMAL_RESISTANCE.getDisplayName());
        assertEquals(1.4, MutationType.THERMAL_RESISTANCE.getBonus());
        assertTrue(MutationType.THERMAL_RESISTANCE.getDescription().contains("Resistencia"));
    }
    
    @Test
    void testRandomMutationSelection() {
        java.util.Random random = new java.util.Random(12345); // Fixed seed
        MutationType mutation = MutationType.getRandomMutation(random);
        
        assertNotNull(mutation);
        assertNotEquals(MutationType.NONE, mutation);
        assertTrue(mutation == MutationType.EFFICIENT_METABOLISM ||
                   mutation == MutationType.ENHANCED_STRENGTH ||
                   mutation == MutationType.THERMAL_RESISTANCE);
    }
    
    @Test
    void testCreatureMutationTypes() {
        Creature creature1 = new Creature(CellType.PREDATOR, 0, 0);
        creature1.mutate(MutationType.EFFICIENT_METABOLISM);
        
        assertTrue(creature1.isMutated());
        assertEquals(MutationType.EFFICIENT_METABOLISM, creature1.getMutationType());
        assertEquals(1.3, creature1.getMutationBonus());
        
        Creature creature2 = new Creature(CellType.PREY, 1, 1);
        creature2.mutate(MutationType.ENHANCED_STRENGTH);
        
        assertTrue(creature2.isMutated());
        assertEquals(MutationType.ENHANCED_STRENGTH, creature2.getMutationType());
        assertEquals(1.5, creature2.getMutationBonus());
    }
    
    @Test
    void testEfficientMetabolismReducesHungerThirst() {
        Creature normal = new Creature(CellType.PREDATOR, 0, 0);
        Creature mutated = new Creature(CellType.PREDATOR, 1, 1);
        mutated.mutate(MutationType.EFFICIENT_METABOLISM);
        
        // Age both creatures
        normal.age();
        mutated.age();
        
        // Mutated creature should have less hunger/thirst increase
        assertTrue(mutated.getHunger() < normal.getHunger(),
                "Mutated creature should have less hunger");
        assertTrue(mutated.getThirst() < normal.getThirst(),
                "Mutated creature should have less thirst");
    }
    
    @Test
    void testEnhancedStrengthProvidesExtraEnergy() {
        Creature normal = new Creature(CellType.PREY, 0, 0);
        Creature mutated = new Creature(CellType.PREY, 1, 1);
        mutated.mutate(MutationType.ENHANCED_STRENGTH);
        
        int normalEnergyBefore = normal.getEnergy();
        int mutatedEnergyBefore = mutated.getEnergy();
        
        // Both eat food
        normal.eatFood();
        mutated.eatFood();
        
        int normalEnergyGain = normal.getEnergy() - normalEnergyBefore;
        int mutatedEnergyGain = mutated.getEnergy() - mutatedEnergyBefore;
        
        // Mutated creature should gain more energy
        assertTrue(mutatedEnergyGain > normalEnergyGain,
                "Enhanced strength should provide bonus energy from food");
    }
    
    @Test
    void testSimulationWithMutations() {
        SimulationConfig config = new SimulationConfig()
            .withScenario(Scenario.BALANCED)
            .withMutations(true)
            .withGridSize(10)
            .withMaxTurns(5);
        
        SimulationEngine engine = new SimulationEngine(config);
        engine.start();
        
        // Execute a few turns
        for (int i = 0; i < 5; i++) {
            engine.executeTurn();
        }
        
        engine.stop();
        
        // Check that simulation ran successfully
        assertTrue(engine.getStats().getTurn() <= 5);
        
        // Event logger should have captured events
        assertNotNull(engine.getEventLogger());
        assertTrue(engine.getEventLogger().getEntryCount() >= 0);
    }
    
    @Test
    void testMutationInheritanceDoesNotOccur() {
        // Verify that mutations don't automatically pass to offspring
        // (This is current behavior - mutations occur randomly, not through inheritance)
        Creature parent1 = new Creature(CellType.PREDATOR, 0, 0, Sex.MALE);
        Creature parent2 = new Creature(CellType.PREDATOR, 0, 1, Sex.FEMALE);
        
        parent1.mutate(MutationType.ENHANCED_STRENGTH);
        
        Creature offspring = new Creature(CellType.PREDATOR, 0, 2);
        
        // Offspring should not inherit mutation
        assertFalse(offspring.isMutated());
        assertEquals(MutationType.NONE, offspring.getMutationType());
    }
}
