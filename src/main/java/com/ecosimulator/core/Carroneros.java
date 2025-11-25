package com.ecosimulator.core;

import java.util.Set;

/**
 * Scavenger species implementation (third species).
 * - Only moves to cells with corpses (where a prey died the previous turn).
 * - Reproduces if cleaned at least 1 cell in 2 turns.
 * - Does not interact with living species.
 */
public class Carroneros extends Especie {
    
    public static final String TIPO = "carroneros";
    
    private int celdasLimpiadasUltimos2Turnos;
    private int turnoUltimaLimpieza;

    public Carroneros() {
        super(TIPO);
        this.celdasLimpiadasUltimos2Turnos = 0;
        this.turnoUltimaLimpieza = -1;
    }

    public Carroneros(Set<Mutacion> mutaciones) {
        super(TIPO, mutaciones);
        this.celdasLimpiadasUltimos2Turnos = 0;
        this.turnoUltimaLimpieza = -1;
    }

    @Override
    public boolean puedeReproducirse() {
        // Reproduces if cleaned at least 1 cell in last 2 turns
        // With FERTILIDAD mutation, can reproduce every turn after first
        if (hasMutacion(Mutacion.FERTILIDAD)) {
            return turnosSobrevividos >= 1;
        }
        return celdasLimpiadasUltimos2Turnos >= 1 && turnosSobrevividos >= 2;
    }

    @Override
    public Especie reproducir() {
        return new Carroneros(this.mutaciones);
    }

    /**
     * Register that this scavenger cleaned a corpse cell.
     * @param turnoActual the current turn number
     */
    public void registrarLimpieza(int turnoActual) {
        turnoUltimaLimpieza = turnoActual;
        celdasLimpiadasUltimos2Turnos++;
        if (celdasLimpiadasUltimos2Turnos > 2) {
            celdasLimpiadasUltimos2Turnos = 2;
        }
    }

    /**
     * Called at end of turn to decay the cleaning counter if didn't clean this turn.
     * @param turnoActual current turn number
     */
    public void finalizarTurno(int turnoActual) {
        // If didn't clean this turn, decay the counter
        if (turnoUltimaLimpieza != turnoActual && celdasLimpiadasUltimos2Turnos > 0) {
            celdasLimpiadasUltimos2Turnos--;
        }
    }

    public int getCeldasLimpiadasUltimos2Turnos() {
        return celdasLimpiadasUltimos2Turnos;
    }
}
