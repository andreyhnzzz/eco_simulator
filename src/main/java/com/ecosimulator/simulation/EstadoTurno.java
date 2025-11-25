package com.ecosimulator.simulation;

import com.ecosimulator.core.Ecosistema;

import java.util.Map;

/**
 * Represents the state of a single turn in the simulation.
 * Used for history tracking and analysis.
 */
public class EstadoTurno {
    
    private final int turno;
    private final Map<String, Integer> poblacion;
    private final double ocupacion;

    public EstadoTurno(int turno, Map<String, Integer> poblacion, double ocupacion) {
        this.turno = turno;
        this.poblacion = Map.copyOf(poblacion);
        this.ocupacion = ocupacion;
    }

    public int getTurno() {
        return turno;
    }

    public Map<String, Integer> getPoblacion() {
        return poblacion;
    }

    public double getOcupacion() {
        return ocupacion;
    }

    public int getPresas() {
        return poblacion.getOrDefault("presas", 0);
    }

    public int getDepredadores() {
        return poblacion.getOrDefault("depredadores", 0);
    }

    public int getCarroneros() {
        return poblacion.getOrDefault("carroneros", 0);
    }

    @Override
    public String toString() {
        return String.format("%d|presas:%d|depredadores:%d|carroneros:%d|ocupacion:%.0f%%",
                turno, getPresas(), getDepredadores(), getCarroneros(), ocupacion * 100);
    }

    /**
     * Parse a state from a string representation.
     * @param linea the string representation
     * @return the parsed state, or null if invalid
     */
    public static EstadoTurno fromString(String linea) {
        try {
            String[] partes = linea.split("\\|");
            int turno = Integer.parseInt(partes[0]);
            int presas = Integer.parseInt(partes[1].split(":")[1]);
            int depredadores = Integer.parseInt(partes[2].split(":")[1]);
            int carroneros = Integer.parseInt(partes[3].split(":")[1]);
            String ocupacionStr = partes[4].split(":")[1].replace("%", "");
            double ocupacion = Double.parseDouble(ocupacionStr) / 100.0;

            Map<String, Integer> poblacion = Map.of(
                    "presas", presas,
                    "depredadores", depredadores,
                    "carroneros", carroneros
            );

            return new EstadoTurno(turno, poblacion, ocupacion);
        } catch (Exception e) {
            return null;
        }
    }
}
