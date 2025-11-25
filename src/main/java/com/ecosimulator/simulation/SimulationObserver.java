package com.ecosimulator.simulation;

import com.ecosimulator.core.Ecosistema;

import java.util.Map;

/**
 * Observer interface for simulation events.
 * Allows decoupling of GUI and persistence from the simulation engine.
 */
public interface SimulationObserver {
    
    /**
     * Called at the end of each turn.
     * @param turno the turn number that just ended
     * @param ecosistema the current state of the ecosystem
     * @param poblacion the current population counts
     * @param ocupacion the current occupation percentage
     */
    void onTurnEnd(int turno, Ecosistema ecosistema, Map<String, Integer> poblacion, double ocupacion);

    /**
     * Called when an extinction event occurs.
     * @param especie the species that went extinct
     * @param turno the turn when extinction occurred
     */
    void onExtinction(String especie, int turno);

    /**
     * Called when the simulation starts.
     * @param ecosistema the initial state of the ecosystem
     * @param escenario the scenario being simulated
     */
    default void onSimulationStart(Ecosistema ecosistema, Escenario escenario) {
        // Default empty implementation
    }

    /**
     * Called when the simulation ends (either by max turns or extinction).
     * @param turnoFinal the final turn number
     * @param ecosistema the final state of the ecosystem
     */
    default void onSimulationEnd(int turnoFinal, Ecosistema ecosistema) {
        // Default empty implementation
    }
}
