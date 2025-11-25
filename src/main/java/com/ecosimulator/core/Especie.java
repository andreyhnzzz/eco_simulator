package com.ecosimulator.core;

import java.util.EnumSet;
import java.util.Set;

/**
 * Abstract base class representing a species in the ecosystem.
 * Pure domain object with no I/O, GUI, or persistence dependencies.
 */
public abstract class Especie {
    protected int turnosSobrevividos;
    protected int turnosSinComer;
    protected final Set<Mutacion> mutaciones;
    protected final String tipo;

    protected Especie(String tipo) {
        this.tipo = tipo;
        this.turnosSobrevividos = 0;
        this.turnosSinComer = 0;
        this.mutaciones = EnumSet.noneOf(Mutacion.class);
    }

    protected Especie(String tipo, Set<Mutacion> mutaciones) {
        this.tipo = tipo;
        this.turnosSobrevividos = 0;
        this.turnosSinComer = 0;
        this.mutaciones = EnumSet.copyOf(mutaciones);
    }

    /**
     * Check if this species can reproduce based on its specific conditions.
     * @return true if the species can reproduce
     */
    public abstract boolean puedeReproducirse();

    /**
     * Create a new instance of the same species type.
     * @return a new species instance with turnosSobrevividos = 0
     */
    public abstract Especie reproducir();

    /**
     * Get the number of cells this species can move per turn.
     * @return 2 if has VELOCIDAD mutation, otherwise 1
     */
    public int getCeldasMovimiento() {
        return mutaciones.contains(Mutacion.VELOCIDAD) ? 2 : 1;
    }

    /**
     * Increment the survival counter at the end of each turn.
     */
    public void incrementarTurnosSobrevividos() {
        turnosSobrevividos++;
    }

    /**
     * Increment the hunger counter (for predators).
     */
    public void incrementarTurnosSinComer() {
        turnosSinComer++;
    }

    /**
     * Reset hunger counter (when species eats).
     */
    public void resetearTurnosSinComer() {
        turnosSinComer = 0;
    }

    public int getTurnosSobrevividos() {
        return turnosSobrevividos;
    }

    public int getTurnosSinComer() {
        return turnosSinComer;
    }

    public Set<Mutacion> getMutaciones() {
        return EnumSet.copyOf(mutaciones);
    }

    public String getTipo() {
        return tipo;
    }

    public void addMutacion(Mutacion mutacion) {
        mutaciones.add(mutacion);
    }

    public boolean hasMutacion(Mutacion mutacion) {
        return mutaciones.contains(mutacion);
    }
}
