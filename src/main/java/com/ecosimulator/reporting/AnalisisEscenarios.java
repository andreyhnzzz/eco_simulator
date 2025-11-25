package com.ecosimulator.reporting;

import com.ecosimulator.persistence.EstadoTurnoDAO;
import com.ecosimulator.simulation.EstadoTurno;

import java.util.List;

/**
 * Analysis class for scenario statistics.
 * Reads from estado_turnos.txt and calculates statistics.
 */
public class AnalisisEscenarios {
    
    private final List<EstadoTurno> historial;
    private final int turnoExtincion;
    private final String especieExtinta;

    public AnalisisEscenarios(EstadoTurnoDAO dao) {
        this.historial = dao.leerHistorial();
        this.turnoExtincion = dao.getTurnoExtincion();
        this.especieExtinta = dao.getEspecieExtinta();
    }

    public AnalisisEscenarios(List<EstadoTurno> historial, int turnoExtincion, String especieExtinta) {
        this.historial = historial;
        this.turnoExtincion = turnoExtincion;
        this.especieExtinta = especieExtinta;
    }

    /**
     * Get the total number of turns in the simulation.
     * @return total turns
     */
    public int getTotalTurnos() {
        return historial.size();
    }

    /**
     * Get the turn when extinction occurred.
     * @return extinction turn, or -1 if no extinction
     */
    public int getTurnoExtincion() {
        return turnoExtincion;
    }

    /**
     * Get the species that went extinct.
     * @return extinct species, or null if none
     */
    public String getEspecieExtinta() {
        return especieExtinta;
    }

    /**
     * Get the final population counts.
     * @return final population state, or null if no history
     */
    public EstadoTurno getPoblacionFinal() {
        if (historial.isEmpty()) {
            return null;
        }
        return historial.get(historial.size() - 1);
    }

    /**
     * Calculate average occupation percentage.
     * @return average occupation
     */
    public double getOcupacionPromedio() {
        if (historial.isEmpty()) {
            return 0.0;
        }
        return historial.stream()
                .mapToDouble(EstadoTurno::getOcupacion)
                .average()
                .orElse(0.0);
    }

    /**
     * Get the final occupation percentage.
     * @return final occupation
     */
    public double getOcupacionFinal() {
        if (historial.isEmpty()) {
            return 0.0;
        }
        return historial.get(historial.size() - 1).getOcupacion();
    }

    /**
     * Calculate population stability (standard deviation of prey population).
     * Lower values indicate more stability.
     * @return stability metric
     */
    public double getEstabilidadPresas() {
        return calculateStandardDeviation(historial.stream()
                .mapToDouble(EstadoTurno::getPresas)
                .toArray());
    }

    /**
     * Calculate population stability for predators.
     * @return stability metric
     */
    public double getEstabilidadDepredadores() {
        return calculateStandardDeviation(historial.stream()
                .mapToDouble(EstadoTurno::getDepredadores)
                .toArray());
    }

    /**
     * Calculate population stability for scavengers.
     * @return stability metric
     */
    public double getEstabilidadCarroneros() {
        return calculateStandardDeviation(historial.stream()
                .mapToDouble(EstadoTurno::getCarroneros)
                .toArray());
    }

    private double calculateStandardDeviation(double[] values) {
        if (values.length == 0) {
            return 0.0;
        }
        
        double mean = 0;
        for (double value : values) {
            mean += value;
        }
        mean /= values.length;
        
        double variance = 0;
        for (double value : values) {
            variance += Math.pow(value - mean, 2);
        }
        variance /= values.length;
        
        return Math.sqrt(variance);
    }

    /**
     * Get prey population over time.
     * @return array of prey counts per turn
     */
    public int[] getPresasPorTurno() {
        return historial.stream()
                .mapToInt(EstadoTurno::getPresas)
                .toArray();
    }

    /**
     * Get predator population over time.
     * @return array of predator counts per turn
     */
    public int[] getDepredadoresPorTurno() {
        return historial.stream()
                .mapToInt(EstadoTurno::getDepredadores)
                .toArray();
    }

    /**
     * Get scavenger population over time.
     * @return array of scavenger counts per turn
     */
    public int[] getCarronerosPorTurno() {
        return historial.stream()
                .mapToInt(EstadoTurno::getCarroneros)
                .toArray();
    }

    /**
     * Get occupation percentage over time.
     * @return array of occupation percentages per turn
     */
    public double[] getOcupacionPorTurno() {
        return historial.stream()
                .mapToDouble(EstadoTurno::getOcupacion)
                .toArray();
    }

    /**
     * Get the full history of turn states.
     * @return list of turn states
     */
    public List<EstadoTurno> getHistorial() {
        return historial;
    }

    /**
     * Check if there was an extinction.
     * @return true if extinction occurred
     */
    public boolean huboExtincion() {
        return turnoExtincion > 0;
    }

    /**
     * Generate a text summary of the analysis.
     * @return summary string
     */
    public String generarResumen() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RESUMEN DE SIMULACIÓN ===\n");
        sb.append("Total de turnos: ").append(getTotalTurnos()).append("\n");
        sb.append("Ocupación promedio: ").append(String.format("%.2f%%", getOcupacionPromedio() * 100)).append("\n");
        sb.append("Ocupación final: ").append(String.format("%.2f%%", getOcupacionFinal() * 100)).append("\n");
        
        EstadoTurno poblacionFinal = getPoblacionFinal();
        if (poblacionFinal != null) {
            sb.append("Población final:\n");
            sb.append("  - Presas: ").append(poblacionFinal.getPresas()).append("\n");
            sb.append("  - Depredadores: ").append(poblacionFinal.getDepredadores()).append("\n");
            sb.append("  - Carroñeros: ").append(poblacionFinal.getCarroneros()).append("\n");
        }
        
        sb.append("Estabilidad (desv. est.):\n");
        sb.append("  - Presas: ").append(String.format("%.2f", getEstabilidadPresas())).append("\n");
        sb.append("  - Depredadores: ").append(String.format("%.2f", getEstabilidadDepredadores())).append("\n");
        
        if (huboExtincion()) {
            sb.append("EXTINCIÓN: ").append(especieExtinta).append(" en turno ").append(turnoExtincion).append("\n");
        } else {
            sb.append("Sin extinción\n");
        }
        
        return sb.toString();
    }
}
