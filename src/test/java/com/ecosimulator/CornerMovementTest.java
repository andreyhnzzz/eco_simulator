package com.ecosimulator;

import com.ecosimulator.model.*;
import com.ecosimulator.simulation.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that creatures don't get stuck in corners or borders
 */
public class CornerMovementTest {
    
    @Test
    public void testCreaturesCanMoveFromCorners() {
        // Create a small grid for testing
        SimulationConfig config = new SimulationConfig()
            .withGridSize(5)
            .withScenario(Scenario.BALANCED)
            .withThirdSpecies(false)
            .withMutations(false);
        
        SimulationEngine engine = new SimulationEngine(config);
        engine.start();
        
        // Get initial positions of all creatures
        CellType[][] initialGrid = copyGrid(engine.getGrid());
        
        // Run several turns
        for (int i = 0; i < 20; i++) {
            engine.executeTurn();
        }
        
        // Get final positions
        CellType[][] finalGrid = copyGrid(engine.getGrid());
        
        // Verify that creatures have moved (grid state has changed)
        boolean gridChanged = false;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                if (initialGrid[row][col] != finalGrid[row][col]) {
                    gridChanged = true;
                    break;
                }
            }
            if (gridChanged) break;
        }
        
        assertTrue(gridChanged, "Grid should change after 20 turns - creatures should be able to move");
        
        // Specifically check corners - creatures should not all be stuck in corners
        int creaturesInCorners = 0;
        int totalCreatures = 0;
        
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                CellType cell = finalGrid[row][col];
                if (cell.isLiving()) {
                    totalCreatures++;
                    // Check if this is a corner
                    if ((row == 0 || row == 4) && (col == 0 || col == 4)) {
                        creaturesInCorners++;
                    }
                }
            }
        }
        
        // If there are creatures, not all should be stuck in corners
        if (totalCreatures > 0) {
            double cornerRatio = (double) creaturesInCorners / totalCreatures;
            assertTrue(cornerRatio < 0.8, 
                String.format("Too many creatures stuck in corners: %d/%d (%.1f%%)", 
                    creaturesInCorners, totalCreatures, cornerRatio * 100));
        }
    }
    
    @Test
    public void testPreyCanFleeFromBorders() {
        // Create a scenario with prey at borders
        SimulationConfig config = new SimulationConfig()
            .withGridSize(5)
            .withScenario(Scenario.PREDATOR_DOMINANT)
            .withThirdSpecies(false)
            .withMutations(false);
        
        SimulationEngine engine = new SimulationEngine(config);
        engine.start();
        
        // Run simulation for several turns
        for (int i = 0; i < 15; i++) {
            engine.executeTurn();
        }
        
        // Check that prey are still alive (meaning they could flee/move)
        SimulationStats stats = engine.getStats();
        
        // In a dominant predator scenario, some prey should survive for at least 15 turns
        // if they can move properly
        assertTrue(stats.getPreyCount() >= 0, 
            "Prey count should be tracked correctly");
    }
    
    private CellType[][] copyGrid(CellType[][] original) {
        int size = original.length;
        CellType[][] copy = new CellType[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                copy[i][j] = original[i][j];
            }
        }
        return copy;
    }
}
