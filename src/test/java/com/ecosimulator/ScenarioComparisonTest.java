package com.ecosimulator;

import com.ecosimulator.model.Scenario;
import com.ecosimulator.simulation.ScenarioComparison;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ScenarioComparison class
 */
class ScenarioComparisonTest {
    
    @Test
    void testSimulationResultCreation() {
        List<Integer> predHist = Arrays.asList(10, 9, 8, 7);
        List<Integer> preyHist = Arrays.asList(20, 19, 18, 17);
        
        ScenarioComparison.SimulationResult result = new ScenarioComparison.SimulationResult(
            Scenario.BALANCED,
            false,
            false,
            50,
            7,
            17,
            0,
            -1,
            predHist,
            preyHist
        );
        
        assertEquals(Scenario.BALANCED, result.getScenario());
        assertFalse(result.isThirdSpeciesEnabled());
        assertFalse(result.isMutationsEnabled());
        assertEquals(50, result.getTotalTurns());
        assertEquals(7, result.getFinalPredators());
        assertEquals(17, result.getFinalPrey());
        assertEquals(0, result.getFinalThirdSpecies());
        assertEquals(-1, result.getExtinctionTurn());
        assertEquals(24, result.getTotalOccupancy());
        assertTrue(result.isEquilibriumMaintained());
    }
    
    @Test
    void testSimulationResultWithExtinction() {
        List<Integer> predHist = Arrays.asList(10, 5, 0);
        List<Integer> preyHist = Arrays.asList(20, 25, 30);
        
        ScenarioComparison.SimulationResult result = new ScenarioComparison.SimulationResult(
            Scenario.PREY_DOMINANT,
            true,
            true,
            30,
            0,
            30,
            5,
            25,
            predHist,
            preyHist
        );
        
        assertEquals(Scenario.PREY_DOMINANT, result.getScenario());
        assertTrue(result.isThirdSpeciesEnabled());
        assertTrue(result.isMutationsEnabled());
        assertEquals(25, result.getExtinctionTurn());
        assertFalse(result.isEquilibriumMaintained());
        assertEquals(35, result.getTotalOccupancy());
    }
    
    @Test
    void testConfigDescription() {
        List<Integer> predHist = Arrays.asList(10);
        List<Integer> preyHist = Arrays.asList(20);
        
        // Scenario only
        ScenarioComparison.SimulationResult result1 = new ScenarioComparison.SimulationResult(
            Scenario.BALANCED, false, false, 10, 5, 10, 0, -1, predHist, preyHist);
        assertEquals("Equilibrado", result1.getConfigDescription());
        
        // With third species
        ScenarioComparison.SimulationResult result2 = new ScenarioComparison.SimulationResult(
            Scenario.BALANCED, true, false, 10, 5, 10, 3, -1, predHist, preyHist);
        assertTrue(result2.getConfigDescription().contains("Third Species"));
        
        // With mutations
        ScenarioComparison.SimulationResult result3 = new ScenarioComparison.SimulationResult(
            Scenario.BALANCED, false, true, 10, 5, 10, 0, -1, predHist, preyHist);
        assertTrue(result3.getConfigDescription().contains("Mutations"));
        
        // With both
        ScenarioComparison.SimulationResult result4 = new ScenarioComparison.SimulationResult(
            Scenario.BALANCED, true, true, 10, 5, 10, 3, -1, predHist, preyHist);
        assertTrue(result4.getConfigDescription().contains("Third Species"));
        assertTrue(result4.getConfigDescription().contains("Mutations"));
    }
    
    @Test
    void testComparisonAnalysis() {
        List<Integer> predHist1 = Arrays.asList(10, 10, 10);
        List<Integer> preyHist1 = Arrays.asList(20, 20, 20);
        List<Integer> predHist2 = Arrays.asList(15, 10, 0);
        List<Integer> preyHist2 = Arrays.asList(10, 15, 25);
        
        ScenarioComparison.SimulationResult result1 = new ScenarioComparison.SimulationResult(
            Scenario.BALANCED, false, false, 50, 10, 20, 0, -1, predHist1, preyHist1);
        
        ScenarioComparison.SimulationResult result2 = new ScenarioComparison.SimulationResult(
            Scenario.PREDATOR_DOMINANT, false, false, 30, 0, 25, 0, 25, predHist2, preyHist2);
        
        ScenarioComparison.ComparisonAnalysis analysis = 
            new ScenarioComparison.ComparisonAnalysis(Arrays.asList(result1, result2));
        
        assertNotNull(analysis.getResults());
        assertEquals(2, analysis.getResults().size());
        assertNotNull(analysis.getMostStable());
        assertNotNull(analysis.getFastestExtinction());
        assertNotNull(analysis.getAnalysisReport());
        assertTrue(analysis.getAnalysisReport().length() > 0);
    }
    
    @Test
    void testGenerateSummaryTable() {
        List<Integer> predHist = Arrays.asList(10);
        List<Integer> preyHist = Arrays.asList(20);
        
        ScenarioComparison.SimulationResult result = new ScenarioComparison.SimulationResult(
            Scenario.BALANCED, false, false, 50, 10, 20, 0, -1, predHist, preyHist);
        
        ScenarioComparison.ComparisonAnalysis analysis = 
            new ScenarioComparison.ComparisonAnalysis(Arrays.asList(result));
        
        String table = ScenarioComparison.generateSummaryTable(analysis);
        
        assertNotNull(table);
        assertTrue(table.contains("Configuration"));
        assertTrue(table.contains("Equilibrado"));
    }
    
    @Test
    void testRunComparisonSimulations() {
        // Run a quick comparison with small grid and few turns
        ScenarioComparison.ComparisonAnalysis analysis = 
            ScenarioComparison.runComparisonSimulations(8, 10);
        
        assertNotNull(analysis);
        // 3 scenarios × 4 modes = 12 results
        assertEquals(12, analysis.getResults().size());
        assertNotNull(analysis.getAnalysisReport());
    }
}
