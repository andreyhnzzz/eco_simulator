package com.ecosimulator.core;

import java.util.Set;

/**
 * Prey species implementation.
 * - Reproduces every 2 turns survived (or every 1 turn with FERTILIDAD mutation).
 * - Does not eat (turnosSinComer does not apply).
 */
public class Presas extends Especie {
    
    public static final String TIPO = "presas";

    public Presas() {
        super(TIPO);
    }

    public Presas(Set<Mutacion> mutaciones) {
        super(TIPO, mutaciones);
    }

    @Override
    public boolean puedeReproducirse() {
        int turnosParaReproducirse = hasMutacion(Mutacion.FERTILIDAD) ? 1 : 2;
        return turnosSobrevividos > 0 && turnosSobrevividos % turnosParaReproducirse == 0;
    }

    @Override
    public Especie reproducir() {
        return new Presas(this.mutaciones);
    }
}
