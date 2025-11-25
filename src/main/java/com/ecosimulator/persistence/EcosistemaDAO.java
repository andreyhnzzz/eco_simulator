package com.ecosimulator.persistence;

import com.ecosimulator.core.Ecosistema;
import com.ecosimulator.simulation.Escenario;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DAO for persisting ecosystem configuration.
 * Saves initial configuration once at the start of a scenario.
 */
public class EcosistemaDAO {
    
    private static final String DEFAULT_FILE = "ecosistema.txt";
    
    private final Path filePath;

    public EcosistemaDAO() {
        this(DEFAULT_FILE);
    }

    public EcosistemaDAO(String fileName) {
        this.filePath = Paths.get(fileName);
    }

    /**
     * Save the initial ecosystem configuration.
     * @param ecosistema the ecosystem to save
     * @param escenario the scenario configuration
     */
    public void guardarConfiguracionInicial(Ecosistema ecosistema, Escenario escenario) {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            
            writer.write("# Configuración del Ecosistema");
            writer.newLine();
            writer.write("# Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            writer.newLine();
            writer.newLine();
            
            writer.write("ESCENARIO=" + escenario.getNombre());
            writer.newLine();
            writer.write("PRESAS_INICIALES=" + escenario.getPresasIniciales());
            writer.newLine();
            writer.write("DEPREDADORES_INICIALES=" + escenario.getDepredadoresIniciales());
            writer.newLine();
            writer.write("CARRONEROS_INICIALES=" + escenario.getCarroneroIniciales());
            writer.newLine();
            writer.write("TAMANO_MATRIZ=" + Ecosistema.TAMANO);
            writer.newLine();
            writer.newLine();
            
            writer.write("# Estado inicial de la población");
            writer.newLine();
            var poblacion = ecosistema.poblacion();
            writer.write("PRESAS=" + poblacion.get("presas"));
            writer.newLine();
            writer.write("DEPREDADORES=" + poblacion.get("depredadores"));
            writer.newLine();
            writer.write("CARRONEROS=" + poblacion.get("carroneros"));
            writer.newLine();
            writer.write("OCUPACION=" + String.format("%.2f", ecosistema.porcentajeOcupacion() * 100) + "%");
            writer.newLine();
            
        } catch (IOException e) {
            System.err.println("Error saving ecosystem configuration: " + e.getMessage());
        }
    }

    /**
     * Load the ecosystem configuration from file.
     * @return the loaded scenario, or null if file doesn't exist
     */
    public Escenario cargarConfiguracion() {
        if (!Files.exists(filePath)) {
            return null;
        }
        
        try {
            String nombre = null;
            int presas = 0, depredadores = 0, carroneros = 0;
            
            for (String line : Files.readAllLines(filePath)) {
                if (line.startsWith("ESCENARIO=")) {
                    nombre = line.substring("ESCENARIO=".length());
                } else if (line.startsWith("PRESAS_INICIALES=")) {
                    presas = Integer.parseInt(line.substring("PRESAS_INICIALES=".length()));
                } else if (line.startsWith("DEPREDADORES_INICIALES=")) {
                    depredadores = Integer.parseInt(line.substring("DEPREDADORES_INICIALES=".length()));
                } else if (line.startsWith("CARRONEROS_INICIALES=")) {
                    carroneros = Integer.parseInt(line.substring("CARRONEROS_INICIALES=".length()));
                }
            }
            
            if (nombre != null) {
                return new Escenario(nombre, presas, depredadores, carroneros);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading ecosystem configuration: " + e.getMessage());
        }
        
        return null;
    }

    public Path getFilePath() {
        return filePath;
    }
}
