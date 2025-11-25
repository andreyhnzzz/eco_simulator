package com.ecosimulator.core;

/**
 * Represents a cell in the ecosystem matrix.
 * A cell can hold one species (habitante) and track if there's a corpse.
 */
public class Celda {
    private Especie habitante;
    private boolean tieneCadaver;
    private final int fila;
    private final int columna;

    public Celda(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
        this.habitante = null;
        this.tieneCadaver = false;
    }

    public Especie getHabitante() {
        return habitante;
    }

    public void setHabitante(Especie habitante) {
        this.habitante = habitante;
    }

    public boolean tieneCadaver() {
        return tieneCadaver;
    }

    public void setTieneCadaver(boolean tieneCadaver) {
        this.tieneCadaver = tieneCadaver;
    }

    public boolean estaVacia() {
        return habitante == null;
    }

    public void limpiar() {
        this.habitante = null;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    /**
     * Mark the cell as having a corpse and remove the inhabitant.
     */
    public void marcarMuerte() {
        this.habitante = null;
        this.tieneCadaver = true;
    }

    /**
     * Clean the corpse from the cell (used by scavengers).
     */
    public void limpiarCadaver() {
        this.tieneCadaver = false;
    }
}
