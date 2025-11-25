package com.ecosimulator.simulation;

/**
 * DTO representing a scenario configuration for the simulation.
 */
public class Escenario {
    
    private final String nombre;
    private final int presasIniciales;
    private final int depredadoresIniciales;
    private final int carroneroIniciales;

    public Escenario(String nombre, int presasIniciales, int depredadoresIniciales, int carroneroIniciales) {
        this.nombre = nombre;
        this.presasIniciales = presasIniciales;
        this.depredadoresIniciales = depredadoresIniciales;
        this.carroneroIniciales = carroneroIniciales;
    }

    // Predefined scenarios
    public static final Escenario EQUILIBRADO = new Escenario("Equilibrado", 30, 30, 0);
    public static final Escenario DEPREDADORES_DOMINANTES = new Escenario("Depredadores Dominantes", 15, 45, 0);
    public static final Escenario PRESAS_DOMINANTES = new Escenario("Presas Dominantes", 45, 15, 0);

    public String getNombre() {
        return nombre;
    }

    public int getPresasIniciales() {
        return presasIniciales;
    }

    public int getDepredadoresIniciales() {
        return depredadoresIniciales;
    }

    public int getCarroneroIniciales() {
        return carroneroIniciales;
    }

    /**
     * Get all predefined scenarios.
     * @return array of predefined scenarios
     */
    public static Escenario[] getPredefinidos() {
        return new Escenario[] {EQUILIBRADO, DEPREDADORES_DOMINANTES, PRESAS_DOMINANTES};
    }

    @Override
    public String toString() {
        return nombre + " (Presas: " + presasIniciales + 
               ", Depredadores: " + depredadoresIniciales + 
               ", Carroñeros: " + carroneroIniciales + ")";
    }
}
