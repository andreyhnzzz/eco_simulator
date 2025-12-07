package com.ecosimulator.model;

/**
 * Third species in the ecosystem (scavenger/opportunistic hunter)
 * Can hunt both prey and weak predators
 */
public class ThirdSpecies extends Animal {
    private static final int INITIAL_ENERGY = 12;
    private static final int REPRODUCTION_THRESHOLD = 18;
    
    public ThirdSpecies(int x, int y) {
        super(x, y);
    }
    
    @Override
    protected int getInitialEnergy() {
        return INITIAL_ENERGY;
    }
    
    @Override
    public boolean canReproduce() {
        return energy >= REPRODUCTION_THRESHOLD;
    }
    
    @Override
    public String getType() {
        return "third";
    }
}
