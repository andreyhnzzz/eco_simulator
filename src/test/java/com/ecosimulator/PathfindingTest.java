package com.ecosimulator;

import com.ecosimulator.model.CellType;
import com.ecosimulator.util.PathfindingUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for the Dijkstra pathfinding implementation
 */
public class PathfindingTest {
    
    @Test
    public void testFindNextMoveTowardsPrey() {
        // Create a simple 5x5 grid
        CellType[][] grid = new CellType[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                grid[i][j] = CellType.EMPTY;
            }
        }
        
        // Place predator at (0, 0)
        grid[0][0] = CellType.PREDATOR;
        
        // Place prey at (3, 3)
        grid[3][3] = CellType.PREY;
        
        // Find next move from (0,0) towards prey
        int[] nextMove = PathfindingUtils.findNextMove(grid, 0, 0, CellType.PREY, 10, false);
        
        assertNotNull(nextMove, "Should find a path to prey");
        
        // The move should be closer to (3, 3) than (0, 0)
        int initialDistance = Math.abs(0 - 3) + Math.abs(0 - 3); // 6
        int newDistance = Math.abs(nextMove[0] - 3) + Math.abs(nextMove[1] - 3);
        
        assertTrue(newDistance < initialDistance, 
                  "Next move should be closer to prey than starting position");
    }
    
    @Test
    public void testFindNextMoveFleeFromPredator() {
        // Create a simple 5x5 grid
        CellType[][] grid = new CellType[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                grid[i][j] = CellType.EMPTY;
            }
        }
        
        // Place prey at (2, 2) - center
        grid[2][2] = CellType.PREY;
        
        // Place predator at (1, 1)
        grid[1][1] = CellType.PREDATOR;
        
        // Find next move from (2,2) to flee from predator (fleeing mode = true)
        int[] nextMove = PathfindingUtils.findNextMove(grid, 2, 2, CellType.PREDATOR, 5, true);
        
        assertNotNull(nextMove, "Should find a fleeing direction");
        
        // The move should be farther from predator at (1, 1)
        int initialDistance = Math.abs(2 - 1) + Math.abs(2 - 1); // 2
        int newDistance = Math.abs(nextMove[0] - 1) + Math.abs(nextMove[1] - 1);
        
        assertTrue(newDistance >= initialDistance, 
                  "Next move should be farther or same distance from predator");
    }
    
    @Test
    public void testNoPathWhenNoTarget() {
        // Create a simple 5x5 grid with no prey
        CellType[][] grid = new CellType[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                grid[i][j] = CellType.EMPTY;
            }
        }
        
        grid[0][0] = CellType.PREDATOR;
        
        // Try to find prey (none exists)
        int[] nextMove = PathfindingUtils.findNextMove(grid, 0, 0, CellType.PREY, 10, false);
        
        assertNull(nextMove, "Should return null when no target found");
    }
    
    @Test
    public void testFindNextMoveTowardsCorpse() {
        // Create a simple 5x5 grid
        CellType[][] grid = new CellType[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                grid[i][j] = CellType.EMPTY;
            }
        }
        
        // Place scavenger at (0, 0)
        grid[0][0] = CellType.THIRD_SPECIES;
        
        // Place corpse at (2, 2)
        grid[2][2] = CellType.CORPSE;
        
        // Find next move from (0,0) towards corpse
        int[] nextMove = PathfindingUtils.findNextMove(grid, 0, 0, CellType.CORPSE, 10, false);
        
        assertNotNull(nextMove, "Should find a path to corpse");
        
        // The move should be closer to (2, 2) than (0, 0)
        int initialDistance = Math.abs(0 - 2) + Math.abs(0 - 2); // 4
        int newDistance = Math.abs(nextMove[0] - 2) + Math.abs(nextMove[1] - 2);
        
        assertTrue(newDistance < initialDistance, 
                  "Next move should be closer to corpse than starting position");
    }
    
    @Test
    public void testRangeLimit() {
        // Create a 10x10 grid
        CellType[][] grid = new CellType[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                grid[i][j] = CellType.EMPTY;
            }
        }
        
        // Place predator at (0, 0)
        grid[0][0] = CellType.PREDATOR;
        
        // Place prey at (9, 9) - far away
        grid[9][9] = CellType.PREY;
        
        // Try to find prey with limited range (should fail)
        int[] nextMove = PathfindingUtils.findNextMove(grid, 0, 0, CellType.PREY, 5, false);
        
        assertNull(nextMove, "Should return null when target is out of range");
        
        // Try with longer range (should succeed)
        nextMove = PathfindingUtils.findNextMove(grid, 0, 0, CellType.PREY, 20, false);
        
        assertNotNull(nextMove, "Should find path when target is within range");
    }
    
    @Test
    public void testFleeFromPredatorAtBorder() {
        // Test that prey at borders can still flee from predators
        // This addresses the issue where prey get stuck at corners/edges
        CellType[][] grid = new CellType[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                grid[i][j] = CellType.EMPTY;
            }
        }
        
        // Place prey at corner (0, 0)
        grid[0][0] = CellType.PREY;
        
        // Place predator nearby at (1, 1)
        grid[1][1] = CellType.PREDATOR;
        
        // Prey should be able to find a move even from corner
        int[] nextMove = PathfindingUtils.findNextMove(grid, 0, 0, CellType.PREDATOR, 5, true);
        
        assertNotNull(nextMove, "Prey at corner should still be able to move when fleeing");
        
        // Verify the move is valid (within bounds)
        assertTrue(nextMove[0] >= 0 && nextMove[0] < 5, "Move row should be within grid");
        assertTrue(nextMove[1] >= 0 && nextMove[1] < 5, "Move col should be within grid");
    }
    
    @Test
    public void testFleeFromPredatorAtEdge() {
        // Test that prey at edges can still flee from predators
        CellType[][] grid = new CellType[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                grid[i][j] = CellType.EMPTY;
            }
        }
        
        // Place prey at edge (0, 2)
        grid[0][2] = CellType.PREY;
        
        // Place predator below at (1, 2)
        grid[1][2] = CellType.PREDATOR;
        
        // Prey should be able to find a move even from edge
        int[] nextMove = PathfindingUtils.findNextMove(grid, 0, 2, CellType.PREDATOR, 5, true);
        
        assertNotNull(nextMove, "Prey at edge should still be able to move when fleeing");
        
        // Verify the move is valid (within bounds)
        assertTrue(nextMove[0] >= 0 && nextMove[0] < 5, "Move row should be within grid");
        assertTrue(nextMove[1] >= 0 && nextMove[1] < 5, "Move col should be within grid");
    }
}
