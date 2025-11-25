package com.ecosimulator.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the ecosystem with a 10x10 matrix of cells.
 * Pure domain object with no I/O, GUI, or persistence dependencies.
 */
public class Ecosistema {
    
    public static final int TAMANO = 10;
    
    private final Celda[][] matriz;

    public Ecosistema() {
        matriz = new Celda[TAMANO][TAMANO];
        inicializarMatriz();
    }

    private void inicializarMatriz() {
        for (int i = 0; i < TAMANO; i++) {
            for (int j = 0; j < TAMANO; j++) {
                matriz[i][j] = new Celda(i, j);
            }
        }
    }

    /**
     * Get the cell at the specified position.
     * @param fila row index (0-9)
     * @param columna column index (0-9)
     * @return the cell, or null if out of bounds
     */
    public Celda getCelda(int fila, int columna) {
        if (fila < 0 || fila >= TAMANO || columna < 0 || columna >= TAMANO) {
            return null;
        }
        return matriz[fila][columna];
    }

    /**
     * Get the full matrix of cells.
     * @return the 10x10 matrix
     */
    public Celda[][] getMatriz() {
        return matriz;
    }

    /**
     * Get population count for each species type.
     * @return map with species type as key and count as value
     */
    public Map<String, Integer> poblacion() {
        Map<String, Integer> poblacion = new HashMap<>();
        poblacion.put(Presas.TIPO, 0);
        poblacion.put(Depredadores.TIPO, 0);
        poblacion.put(Carroneros.TIPO, 0);

        for (int i = 0; i < TAMANO; i++) {
            for (int j = 0; j < TAMANO; j++) {
                Especie habitante = matriz[i][j].getHabitante();
                if (habitante != null) {
                    String tipo = habitante.getTipo();
                    poblacion.put(tipo, poblacion.getOrDefault(tipo, 0) + 1);
                }
            }
        }
        return poblacion;
    }

    /**
     * Check if there's an extinction (a species has 0 individuals and can't recover).
     * @return true if any species has gone extinct
     */
    public boolean hayExtincion() {
        Map<String, Integer> pob = poblacion();
        // Presas or depredadores at 0 means extinction (carroñeros at 0 is acceptable)
        return pob.get(Presas.TIPO) == 0 || pob.get(Depredadores.TIPO) == 0;
    }

    /**
     * Get the species that has gone extinct.
     * @return the extinct species type, or null if none
     */
    public String especieExtinta() {
        Map<String, Integer> pob = poblacion();
        if (pob.get(Presas.TIPO) == 0) {
            return Presas.TIPO;
        }
        if (pob.get(Depredadores.TIPO) == 0) {
            return Depredadores.TIPO;
        }
        return null;
    }

    /**
     * Calculate the occupation percentage of the ecosystem.
     * @return percentage of non-empty cells (0.0 to 1.0)
     */
    public double porcentajeOcupacion() {
        int ocupadas = 0;
        for (int i = 0; i < TAMANO; i++) {
            for (int j = 0; j < TAMANO; j++) {
                if (!matriz[i][j].estaVacia()) {
                    ocupadas++;
                }
            }
        }
        return (double) ocupadas / (TAMANO * TAMANO);
    }

    /**
     * Get all adjacent cells to a given position.
     * @param fila row index
     * @param columna column index
     * @return list of adjacent cells (up to 8)
     */
    public List<Celda> getCeldasAdyacentes(int fila, int columna) {
        List<Celda> adyacentes = new ArrayList<>();
        int[] df = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dc = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            int nf = fila + df[i];
            int nc = columna + dc[i];
            Celda celda = getCelda(nf, nc);
            if (celda != null) {
                adyacentes.add(celda);
            }
        }
        return adyacentes;
    }

    /**
     * Get empty adjacent cells.
     * @param fila row index
     * @param columna column index
     * @return list of empty adjacent cells
     */
    public List<Celda> getCeldasVaciasAdyacentes(int fila, int columna) {
        return getCeldasAdyacentes(fila, columna).stream()
                .filter(Celda::estaVacia)
                .toList();
    }

    /**
     * Get adjacent cells with corpses.
     * @param fila row index
     * @param columna column index
     * @return list of adjacent cells with corpses
     */
    public List<Celda> getCeldasConCadaverAdyacentes(int fila, int columna) {
        return getCeldasAdyacentes(fila, columna).stream()
                .filter(Celda::tieneCadaver)
                .toList();
    }

    /**
     * Get adjacent cells with prey.
     * @param fila row index
     * @param columna column index
     * @return list of adjacent cells with prey
     */
    public List<Celda> getCeldasConPresaAdyacentes(int fila, int columna) {
        return getCeldasAdyacentes(fila, columna).stream()
                .filter(c -> c.getHabitante() instanceof Presas)
                .toList();
    }

    /**
     * Clear all corpse markers from the ecosystem.
     */
    public void limpiarMarcadoresCadaveres() {
        for (int i = 0; i < TAMANO; i++) {
            for (int j = 0; j < TAMANO; j++) {
                matriz[i][j].setTieneCadaver(false);
            }
        }
    }

    /**
     * Get all cells with species of a given type.
     * @param tipo the species type
     * @return list of cells with that species
     */
    public List<Celda> getCeldasConEspecie(String tipo) {
        List<Celda> celdas = new ArrayList<>();
        for (int i = 0; i < TAMANO; i++) {
            for (int j = 0; j < TAMANO; j++) {
                Especie habitante = matriz[i][j].getHabitante();
                if (habitante != null && habitante.getTipo().equals(tipo)) {
                    celdas.add(matriz[i][j]);
                }
            }
        }
        return celdas;
    }

    /**
     * Get all non-empty cells.
     * @return list of occupied cells
     */
    public List<Celda> getCeldasOcupadas() {
        List<Celda> celdas = new ArrayList<>();
        for (int i = 0; i < TAMANO; i++) {
            for (int j = 0; j < TAMANO; j++) {
                if (!matriz[i][j].estaVacia()) {
                    celdas.add(matriz[i][j]);
                }
            }
        }
        return celdas;
    }

    /**
     * Get all empty cells.
     * @return list of empty cells
     */
    public List<Celda> getCeldasVacias() {
        List<Celda> celdas = new ArrayList<>();
        for (int i = 0; i < TAMANO; i++) {
            for (int j = 0; j < TAMANO; j++) {
                if (matriz[i][j].estaVacia()) {
                    celdas.add(matriz[i][j]);
                }
            }
        }
        return celdas;
    }
}
