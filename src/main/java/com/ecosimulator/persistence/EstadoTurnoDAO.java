package com.ecosimulator.persistence;

import com.ecosimulator.core.Ecosistema;
import com.ecosimulator.simulation.Escenario;
import com.ecosimulator.simulation.EstadoTurno;
import com.ecosimulator.simulation.SimulationObserver;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

/**
 * DAO for persisting turn states to file.
 * Implements SimulationObserver to automatically save state on each turn.
 */
public class EstadoTurnoDAO implements SimulationObserver {
    
    private static final String DEFAULT_FILE = "estado_turnos.txt";
    
    private final Path filePath;
    private int turnoExtincion;
    private String especieExtinta;

    public EstadoTurnoDAO() {
        this(DEFAULT_FILE);
    }

    public EstadoTurnoDAO(String fileName) {
        this.filePath = Paths.get(fileName);
        this.turnoExtincion = -1;
        this.especieExtinta = null;
    }

    @Override
    public void onSimulationStart(Ecosistema ecosistema, Escenario escenario) {
        // Clear the file at the start of a new simulation
        try {
            Files.deleteIfExists(filePath);
            Files.createFile(filePath);
        } catch (IOException e) {
            System.err.println("Error initializing state file: " + e.getMessage());
        }
    }

    @Override
    public void onTurnEnd(int turno, Ecosistema ecosistema, Map<String, Integer> poblacion, double ocupacion) {
        EstadoTurno estado = new EstadoTurno(turno, poblacion, ocupacion);
        try {
            appendToFile(estado.toString());
        } catch (IOException e) {
            System.err.println("Error writing turn state: " + e.getMessage());
        }
    }

    @Override
    public void onExtinction(String especie, int turno) {
        this.especieExtinta = especie;
        this.turnoExtincion = turno;
        try {
            appendToFile("EXTINCION|" + especie + "|turno:" + turno);
        } catch (IOException e) {
            System.err.println("Error writing extinction event: " + e.getMessage());
        }
    }

    @Override
    public void onSimulationEnd(int turnoFinal, Ecosistema ecosistema) {
        try {
            appendToFile("FIN|turno:" + turnoFinal);
        } catch (IOException e) {
            System.err.println("Error writing simulation end: " + e.getMessage());
        }
    }

    private void appendToFile(String line) throws IOException {
        Files.writeString(filePath, line + System.lineSeparator(), 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * Read all turn states from file.
     * @return list of turn states
     */
    public java.util.List<EstadoTurno> leerHistorial() {
        java.util.List<EstadoTurno> historial = new java.util.ArrayList<>();
        try {
            if (Files.exists(filePath)) {
                for (String line : Files.readAllLines(filePath)) {
                    if (!line.startsWith("EXTINCION") && !line.startsWith("FIN")) {
                        EstadoTurno estado = EstadoTurno.fromString(line);
                        if (estado != null) {
                            historial.add(estado);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading state file: " + e.getMessage());
        }
        return historial;
    }

    public int getTurnoExtincion() {
        return turnoExtincion;
    }

    public String getEspecieExtinta() {
        return especieExtinta;
    }

    public Path getFilePath() {
        return filePath;
    }
}
