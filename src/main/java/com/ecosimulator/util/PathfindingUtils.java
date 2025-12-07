package com.ecosimulator.util;

import com.ecosimulator.model.CellType;
import java.util.*;

/**
 * Utility class for pathfinding using Dijkstra's algorithm.
 * Provides efficient path calculation for creatures to find targets or flee.
 */
public class PathfindingUtils {
    
    /**
     * Node used in Dijkstra's algorithm pathfinding
     */
    private static class Node implements Comparable<Node> {
        int row;
        int col;
        int distance;
        Node parent;
        
        Node(int row, int col, int distance) {
            this.row = row;
            this.col = col;
            this.distance = distance;
            this.parent = null;
        }
        
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.distance, other.distance);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Node)) return false;
            Node other = (Node) obj;
            return row == other.row && col == other.col;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }
    
    /**
     * Find the next best move towards a target using Dijkstra's algorithm.
     * This creates a more natural hunting/fleeing behavior that is less efficient
     * than direct adjacent movement.
     * 
     * @param grid Current grid state
     * @param startRow Starting row
     * @param startCol Starting column
     * @param targetType Type of cell to find (e.g., PREY for predators, PREDATOR for prey)
     * @param maxDistance Maximum search distance (limits search space)
     * @param isFleeingMode If true, finds farthest cell from target; if false, finds closest
     * @return Next position [row, col] to move to, or null if no target found
     */
    public static int[] findNextMove(CellType[][] grid, int startRow, int startCol,
                                     CellType targetType, int maxDistance, boolean isFleeingMode) {
        int gridSize = grid.length;
        
        // Find all targets within max distance
        List<int[]> targets = findTargets(grid, startRow, startCol, targetType, maxDistance);
        
        if (targets.isEmpty()) {
            return null; // No targets found
        }
        
        // For fleeing mode, choose the farthest target as reference point
        int[] targetPos = isFleeingMode ? getFarthestTarget(startRow, startCol, targets) 
                                        : getClosestTarget(startRow, startCol, targets);
        
        // Use Dijkstra to find path to (or away from) target
        return dijkstraNextMove(grid, startRow, startCol, targetPos[0], targetPos[1], 
                               gridSize, isFleeingMode);
    }
    
    /**
     * Find all cells of a specific type within max distance
     * Note: Uses square iteration with Manhattan distance filter.
     * For typical grid sizes (25x25) and small max distances (5-12),
     * performance is adequate. Could optimize with diamond pattern for larger grids.
     */
    private static List<int[]> findTargets(CellType[][] grid, int startRow, int startCol,
                                          CellType targetType, int maxDistance) {
        List<int[]> targets = new ArrayList<>();
        int gridSize = grid.length;
        
        for (int row = Math.max(0, startRow - maxDistance); 
             row <= Math.min(gridSize - 1, startRow + maxDistance); row++) {
            for (int col = Math.max(0, startCol - maxDistance); 
                 col <= Math.min(gridSize - 1, startCol + maxDistance); col++) {
                if (grid[row][col] == targetType) {
                    int distance = Math.abs(row - startRow) + Math.abs(col - startCol);
                    if (distance <= maxDistance) {
                        targets.add(new int[]{row, col});
                    }
                }
            }
        }
        
        return targets;
    }
    
    /**
     * Get the closest target from a list
     */
    private static int[] getClosestTarget(int startRow, int startCol, List<int[]> targets) {
        int[] closest = targets.get(0);
        int minDist = manhattanDistance(startRow, startCol, closest[0], closest[1]);
        
        for (int[] target : targets) {
            int dist = manhattanDistance(startRow, startCol, target[0], target[1]);
            if (dist < minDist) {
                minDist = dist;
                closest = target;
            }
        }
        
        return closest;
    }
    
    /**
     * Get the farthest target from a list
     */
    private static int[] getFarthestTarget(int startRow, int startCol, List<int[]> targets) {
        int[] farthest = targets.get(0);
        int maxDist = manhattanDistance(startRow, startCol, farthest[0], farthest[1]);
        
        for (int[] target : targets) {
            int dist = manhattanDistance(startRow, startCol, target[0], target[1]);
            if (dist > maxDist) {
                maxDist = dist;
                farthest = target;
            }
        }
        
        return farthest;
    }
    
    /**
     * Calculate Manhattan distance between two points
     */
    private static int manhattanDistance(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2);
    }
    
    /**
     * Use Dijkstra's algorithm to find the next move towards or away from target.
     * This implementation uses a priority queue and explores cells systematically,
     * creating more realistic movement patterns that are less direct than simple
     * neighbor checking.
     */
    private static int[] dijkstraNextMove(CellType[][] grid, int startRow, int startCol,
                                         int targetRow, int targetCol, int gridSize,
                                         boolean isFleeingMode) {
        // Priority queue for Dijkstra
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<String> closedSet = new HashSet<>();
        Map<String, Node> nodeMap = new HashMap<>();
        
        // Start node
        Node startNode = new Node(startRow, startCol, 0);
        openSet.add(startNode);
        nodeMap.put(posKey(startRow, startCol), startNode);
        
        Node targetNode = null;
        
        // Dijkstra's algorithm
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            String currentKey = posKey(current.row, current.col);
            
            if (closedSet.contains(currentKey)) {
                continue;
            }
            closedSet.add(currentKey);
            
            // Check if we reached the target
            if (current.row == targetRow && current.col == targetCol) {
                targetNode = current;
                break;
            }
            
            // Explore neighbors (8 directions)
            for (int[] dir : getDirections()) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];
                
                if (!isValidPosition(newRow, newCol, gridSize)) {
                    continue;
                }
                
                String neighborKey = posKey(newRow, newCol);
                if (closedSet.contains(neighborKey)) {
                    continue;
                }
                
                // Calculate distance (diagonal moves cost sqrt(2) ≈ 1.4, straight moves cost 1)
                int moveCost = (dir[0] != 0 && dir[1] != 0) ? 14 : 10; // x10 for integer math
                int newDistance = current.distance + moveCost;
                
                Node neighbor = nodeMap.get(neighborKey);
                if (neighbor == null) {
                    neighbor = new Node(newRow, newCol, newDistance);
                    neighbor.parent = current;
                    nodeMap.put(neighborKey, neighbor);
                    openSet.add(neighbor);
                } else if (newDistance < neighbor.distance) {
                    // Note: remove() and add() on PriorityQueue is O(n).
                    // For larger grids, consider using an indexed priority queue.
                    // Current performance is adequate for typical grid sizes (25x25).
                    openSet.remove(neighbor);
                    neighbor.distance = newDistance;
                    neighbor.parent = current;
                    openSet.add(neighbor);
                }
            }
        }
        
        // If target not found or we're already at target, return null
        if (targetNode == null || targetNode.parent == null) {
            return null;
        }
        
        // Backtrack to find first move
        Node firstMove = targetNode;
        while (firstMove.parent != null && firstMove.parent.parent != null) {
            firstMove = firstMove.parent;
        }
        
        // For fleeing mode, move in opposite direction
        if (isFleeingMode) {
            return findFleeingMove(grid, startRow, startCol, 
                                  firstMove.row, firstMove.col, gridSize);
        }
        
        // Return first move towards target
        if (firstMove.parent != null) {
            return new int[]{firstMove.row, firstMove.col};
        }
        
        return null;
    }
    
    /**
     * Find a move in the opposite direction (for fleeing).
     * Enhanced to handle border cases - prey at borders should still be able to move.
     */
    private static int[] findFleeingMove(CellType[][] grid, int startRow, int startCol,
                                        int threatRow, int threatCol, int gridSize) {
        // Calculate direction away from threat
        int deltaRow = startRow - threatRow;
        int deltaCol = startCol - threatCol;
        
        // Normalize direction (keep it simple, move away)
        int moveRow = Integer.compare(deltaRow, 0);
        int moveCol = Integer.compare(deltaCol, 0);
        
        // Try to move in opposite direction
        int newRow = startRow + moveRow;
        int newCol = startCol + moveCol;
        
        if (isValidPosition(newRow, newCol, gridSize)) {
            return new int[]{newRow, newCol};
        }
        
        // If direct opposite not available, try perpendicular directions
        int[][] alternatives = {
            {startRow + moveCol, startCol + moveRow}, // Perpendicular 1
            {startRow - moveCol, startCol - moveRow}, // Perpendicular 2
            {startRow + moveRow, startCol},           // Just row
            {startRow, startCol + moveCol}            // Just col
        };
        
        for (int[] alt : alternatives) {
            if (isValidPosition(alt[0], alt[1], gridSize)) {
                return new int[]{alt[0], alt[1]};
            }
        }
        
        // If still stuck (corner case), try all 8 directions to find ANY valid move
        // This ensures prey at borders/corners don't get stuck
        for (int[] dir : getDirections()) {
            int newR = startRow + dir[0];
            int newC = startCol + dir[1];
            if (isValidPosition(newR, newC, gridSize)) {
                return new int[]{newR, newC};
            }
        }
        
        return null;
    }
    
    /**
     * Get all 8 directional movements
     */
    private static int[][] getDirections() {
        return new int[][]{
            {-1, 0},  // North
            {1, 0},   // South
            {0, -1},  // West
            {0, 1},   // East
            {-1, -1}, // Northwest
            {-1, 1},  // Northeast
            {1, -1},  // Southwest
            {1, 1}    // Southeast
        };
    }
    
    /**
     * Check if position is valid
     */
    private static boolean isValidPosition(int row, int col, int gridSize) {
        return row >= 0 && row < gridSize && col >= 0 && col < gridSize;
    }
    
    /**
     * Create a unique key for a position
     */
    private static String posKey(int row, int col) {
        return row + "," + col;
    }
}
