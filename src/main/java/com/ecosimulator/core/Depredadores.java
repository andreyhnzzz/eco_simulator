package com.ecosimulator.core;

import java.util.Set;

/**
 * Predator species implementation.
 * - Dies if turnosSinComer == 3 (or 5 with RESISTENCIA_HAMBRE mutation).
 * - Reproduces if ate at least once in last 3 turns (or without eating if has FERTILIDAD mutation).
 */
public class Depredadores extends Especie {
    
    public static final String TIPO = "depredadores";
    
    private int vecesComidasUltimos3Turnos;
    private int turnoUltimaComida;

    public Depredadores() {
        super(TIPO);
        this.vecesComidasUltimos3Turnos = 0;
        this.turnoUltimaComida = -1;
    }

    public Depredadores(Set<Mutacion> mutaciones) {
        super(TIPO, mutaciones);
        this.vecesComidasUltimos3Turnos = 0;
        this.turnoUltimaComida = -1;
    }

    @Override
    public boolean puedeReproducirse() {
        // With FERTILIDAD mutation, can reproduce without needing to eat
        if (hasMutacion(Mutacion.FERTILIDAD)) {
            return turnosSobrevividos >= 2;
        }
        // Normal condition: ate at least once in last 3 turns
        return vecesComidasUltimos3Turnos >= 1 && turnosSobrevividos >= 2;
    }

    @Override
    public Especie reproducir() {
        return new Depredadores(this.mutaciones);
    }

    /**
     * Check if this predator should die from hunger.
     * @return true if should die
     */
    public boolean deberiasMorirDeHambre() {
        int turnosMaxSinComer = hasMutacion(Mutacion.RESISTENCIA_HAMBRE) ? 5 : 3;
        return turnosSinComer >= turnosMaxSinComer;
    }

    /**
     * Register that this predator ate in the current turn.
     * @param turnoActual the current turn number
     */
    public void registrarComida(int turnoActual) {
        turnoUltimaComida = turnoActual;
        turnosSinComer = 0;
        actualizarVecesComidas(turnoActual);
    }

    /**
     * Update the count of times eaten in the last 3 turns.
     * @param turnoActual current turn number
     */
    private void actualizarVecesComidas(int turnoActual) {
        // Simplified tracking: if ate this turn, count it
        if (turnoUltimaComida == turnoActual) {
            vecesComidasUltimos3Turnos++;
            // Cap at reasonable value
            if (vecesComidasUltimos3Turnos > 3) {
                vecesComidasUltimos3Turnos = 3;
            }
        }
    }

    /**
     * Called at end of turn to decay the eating counter if didn't eat this turn.
     * @param turnoActual current turn number
     */
    public void finalizarTurno(int turnoActual) {
        // If didn't eat this turn, decay the counter
        if (turnoUltimaComida != turnoActual && vecesComidasUltimos3Turnos > 0) {
            vecesComidasUltimos3Turnos--;
        }
    }

    public int getVecesComidasUltimos3Turnos() {
        return vecesComidasUltimos3Turnos;
    }
}
