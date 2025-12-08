# Movement Optimization Documentation

## Problem Statement
The simulation was experiencing issues where creatures could jump multiple cells in a single turn, leading to illogical movement patterns. This violated the intended design where creatures should only move 1 cell per turn in 8 possible directions.

## Root Cause Analysis

### Movement Range Implementation
Previously, the `getMovementRange()` method in `SimulationEngine.java` returned different values:
- Predators: 1 cell
- Prey: 2 cells
- Scavengers (Third Species): 2 cells

This allowed prey and scavengers to potentially move 2 cells in a single turn, which could result in illogical jumps.

### Example of the Issue
From the problem statement:
```
[Turn 18] 🚶 Predator M-700 moved (17,24) → (16,24) | H:80 T:66 E:9
[Turn 18] 🚶 Predator F-686 moved (24,9) → (24,8) | H:55 T:60 E:19
[Turn 18] 🚶 Predator M-707 moved (9,18) → (9,19) | H:55 T:44 E:9
```

Creatures were occasionally jumping more than 1 cell, which violated the Dijkstra pathfinding algorithm constraints.

## Solution Implemented

### 1. Movement Range Restriction
**File**: `src/main/java/com/ecosimulator/simulation/SimulationEngine.java`

```java
/**
 * Get movement range for a creature based on its type and energy
 * All creatures move 1 cell per turn in 8 directions to prevent illogical multi-cell jumps
 */
private int getMovementRange(Creature creature) {
    // All creatures move 1 cell per turn to prevent illogical jumps
    // Movement is restricted to the 8 adjacent cells (N, S, E, W, NE, NW, SE, SW)
    return 1;
}
```

**Impact**: All creatures now uniformly move only 1 cell per turn, ensuring consistent and predictable movement patterns.

### 2. Pathfinding Safety Check
**File**: `src/main/java/com/ecosimulator/util/PathfindingUtils.java`

Added validation to ensure the Dijkstra algorithm only returns adjacent moves:

```java
// Return first move towards target (ensure it's only 1 cell away)
if (firstMove.parent != null) {
    int rowDiff = Math.abs(firstMove.row - startRow);
    int colDiff = Math.abs(firstMove.col - startCol);
    // Safety check: only allow moves to adjacent cells (max 1 cell in any direction)
    if (rowDiff <= 1 && colDiff <= 1) {
        return new int[]{firstMove.row, firstMove.col};
    }
}
```

**Impact**: Double safety check to prevent any multi-cell jumps even if pathfinding logic has bugs.

### 3. Scavenger Movement Enhancement
**File**: `src/main/java/com/ecosimulator/simulation/SimulationEngine.java`

Added Priority 8 for scavengers to prevent them from staying in the same cell:

```java
// Priority 8: Scavengers seek corpses (unconditional but with threshold to reduce pathfinding overhead)
if (creature.getType() == CellType.THIRD_SPECIES && creature.getHunger() > 10) {
    if (processScavengerActionWithPathfinding(creature, consumedCorpses, row, col, neighbors)) {
        return;
    }
}
```

**Impact**: Scavengers now have the same "prevention from staying still" behavior as prey and predators.

## Performance Considerations

### Memory Optimization
The mutation system was already optimized with:
1. Conditional checks before applying mutations (`if (config.isMutationsEnabled())`)
2. Efficient use of ConcurrentHashMap for creature position lookups
3. Thresholds (>10) for resource-seeking to reduce pathfinding overhead

### Pathfinding Optimization
The Dijkstra implementation uses:
1. Priority queues for efficient node exploration
2. Closed set to avoid revisiting nodes
3. Limited search ranges to reduce computation:
   - Predators: 5 cells search range
   - Scavengers: 12 cells search range
   - General resources: 10 cells search range

### UI Optimization
The mutation checkbox in the UI is lightweight and doesn't cause lag:
- Simple boolean toggle
- No heavy computations on change
- Disabled during simulation runtime

## Testing Results

All 105 tests pass successfully:
- SimulationEngineTest: 12 tests
- PathfindingTest: 7 tests
- Other tests: 86 tests

## Benefits

1. **Predictable Movement**: Creatures always move exactly 1 cell per turn
2. **Realistic Behavior**: Movement follows the 8-directional grid pattern
3. **Better Balance**: Equal movement capabilities across species
4. **Performance**: Reduced pathfinding overhead with thresholds
5. **Consistency**: Scavengers now have the same anti-stuttering logic as other species

## Future Considerations

- Consider adding mutation types that affect movement speed (within the 1-cell constraint)
- Monitor performance with very large grids (>50x50)
- Add telemetry to track average pathfinding computation time
