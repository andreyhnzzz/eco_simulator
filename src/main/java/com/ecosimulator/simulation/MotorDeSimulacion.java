package com.ecosimulator.simulation;

import com.ecosimulator.core.*;

import java.util.*;

/**
 * Simulation engine that orchestrates the ecosystem rules.
 * Decoupled from I/O, GUI, and persistence - only orchestrates rules.
 */
public class MotorDeSimulacion {
    
    private final Ecosistema ecosistema;
    private final Escenario escenario;
    private final int maxTurnos;
    private final List<SimulationObserver> observers;
    private final List<EstadoTurno> historial;
    private final Random random;
    
    private int turnoActual;
    private boolean simulacionActiva;
    private String especieExtinta;
    private int turnoExtincion;

    public MotorDeSimulacion(Ecosistema ecosistema, Escenario escenario, int maxTurnos) {
        this.ecosistema = ecosistema;
        this.escenario = escenario;
        this.maxTurnos = maxTurnos;
        this.observers = new ArrayList<>();
        this.historial = new ArrayList<>();
        this.random = new Random();
        this.turnoActual = 0;
        this.simulacionActiva = false;
        this.especieExtinta = null;
        this.turnoExtincion = -1;
    }

    /**
     * Add an observer to be notified of simulation events.
     * @param observer the observer to add
     */
    public void addObserver(SimulationObserver observer) {
        observers.add(observer);
    }

    /**
     * Remove an observer.
     * @param observer the observer to remove
     */
    public void removeObserver(SimulationObserver observer) {
        observers.remove(observer);
    }

    /**
     * Initialize the ecosystem with the scenario configuration.
     */
    public void inicializarEcosistema() {
        // Get all empty cells
        List<Celda> celdasVacias = new ArrayList<>();
        for (int i = 0; i < Ecosistema.TAMANO; i++) {
            for (int j = 0; j < Ecosistema.TAMANO; j++) {
                celdasVacias.add(ecosistema.getCelda(i, j));
            }
        }
        Collections.shuffle(celdasVacias, random);

        int index = 0;
        
        // Place presas
        for (int i = 0; i < escenario.getPresasIniciales() && index < celdasVacias.size(); i++) {
            celdasVacias.get(index++).setHabitante(new Presas());
        }
        
        // Place depredadores
        for (int i = 0; i < escenario.getDepredadoresIniciales() && index < celdasVacias.size(); i++) {
            celdasVacias.get(index++).setHabitante(new Depredadores());
        }
        
        // Place carroneros
        for (int i = 0; i < escenario.getCarroneroIniciales() && index < celdasVacias.size(); i++) {
            celdasVacias.get(index++).setHabitante(new Carroneros());
        }
    }

    /**
     * Run the full simulation.
     */
    public void ejecutar() {
        simulacionActiva = true;
        notifySimulationStart();

        while (simulacionActiva && turnoActual < maxTurnos) {
            ejecutarTurno();
            
            if (ecosistema.hayExtincion()) {
                especieExtinta = ecosistema.especieExtinta();
                turnoExtincion = turnoActual;
                notifyExtinction(especieExtinta, turnoExtincion);
                simulacionActiva = false;
            }
        }

        notifySimulationEnd();
    }

    /**
     * Execute a single turn with all rules in strict order.
     */
    public void ejecutarTurno() {
        turnoActual++;

        // 1. Mark corpses: clear tieneCadaver on all cells
        // Note: we don't clear corpses here, only at the start of next turn
        // to give scavengers a chance to eat them
        
        // 2. Movement (using temporary copy to avoid read/write conflicts)
        ejecutarMovimiento();
        
        // 3. Feeding is handled during movement for predators
        
        // 4. Deaths
        ejecutarMuertes();
        
        // 5. Reproduction
        ejecutarReproduccion();
        
        // 6. Update survival counters and finalize turn for all species
        finalizarTurnoEspecies();
        
        // 7. Record state and notify observers
        Map<String, Integer> poblacion = ecosistema.poblacion();
        double ocupacion = ecosistema.porcentajeOcupacion();
        EstadoTurno estado = new EstadoTurno(turnoActual, poblacion, ocupacion);
        historial.add(estado);
        
        notifyTurnEnd(turnoActual, poblacion, ocupacion);
        
        // 8. Clean corpse markers for next turn (scavengers had their chance)
        ecosistema.limpiarMarcadoresCadaveres();
    }

    /**
     * Execute movement phase for all species.
     */
    private void ejecutarMovimiento() {
        // Create a snapshot of current positions to avoid conflicts
        List<MovimientoPendiente> movimientos = new ArrayList<>();
        
        // Process all cells
        for (int i = 0; i < Ecosistema.TAMANO; i++) {
            for (int j = 0; j < Ecosistema.TAMANO; j++) {
                Celda celdaOrigen = ecosistema.getCelda(i, j);
                Especie habitante = celdaOrigen.getHabitante();
                
                if (habitante == null) continue;
                
                Celda celdaDestino = determinarCeldaDestino(celdaOrigen, habitante);
                
                if (celdaDestino != null && celdaDestino != celdaOrigen) {
                    movimientos.add(new MovimientoPendiente(celdaOrigen, celdaDestino, habitante));
                }
            }
        }
        
        // Apply all movements
        for (MovimientoPendiente mov : movimientos) {
            aplicarMovimiento(mov);
        }
    }

    /**
     * Determine the destination cell for a species.
     */
    private Celda determinarCeldaDestino(Celda origen, Especie especie) {
        int fila = origen.getFila();
        int columna = origen.getColumna();

        if (especie instanceof Presas) {
            // Prey: move to random empty adjacent cell
            List<Celda> vacias = ecosistema.getCeldasVaciasAdyacentes(fila, columna);
            if (!vacias.isEmpty()) {
                return vacias.get(random.nextInt(vacias.size()));
            }
        } else if (especie instanceof Depredadores) {
            // Predator: if adjacent prey exists, go there; otherwise empty cell
            List<Celda> conPresa = ecosistema.getCeldasConPresaAdyacentes(fila, columna);
            if (!conPresa.isEmpty()) {
                return conPresa.get(random.nextInt(conPresa.size()));
            }
            List<Celda> vacias = ecosistema.getCeldasVaciasAdyacentes(fila, columna);
            if (!vacias.isEmpty()) {
                return vacias.get(random.nextInt(vacias.size()));
            }
        } else if (especie instanceof Carroneros) {
            // Scavenger: only move to cells with corpses
            List<Celda> conCadaver = ecosistema.getCeldasConCadaverAdyacentes(fila, columna);
            if (!conCadaver.isEmpty()) {
                return conCadaver.get(random.nextInt(conCadaver.size()));
            }
            // Scavengers don't move if no corpse nearby
        }
        
        return null;
    }

    /**
     * Apply a pending movement.
     */
    private void aplicarMovimiento(MovimientoPendiente mov) {
        Celda origen = mov.origen;
        Celda destino = mov.destino;
        Especie especie = mov.especie;
        
        // Check if destination is still valid (might have changed during processing)
        if (destino.getHabitante() != null && !(especie instanceof Depredadores && destino.getHabitante() instanceof Presas)) {
            // Destination occupied by non-prey or non-valid target
            return;
        }
        
        // Handle feeding for predators
        if (especie instanceof Depredadores depredador && destino.getHabitante() instanceof Presas) {
            // Predator eats prey
            destino.marcarMuerte(); // Mark corpse
            depredador.registrarComida(turnoActual);
        } else if (especie instanceof Carroneros carronero && destino.tieneCadaver()) {
            // Scavenger cleans corpse
            destino.limpiarCadaver();
            carronero.registrarLimpieza(turnoActual);
        }
        
        // Move species
        origen.limpiar();
        destino.setHabitante(especie);
    }

    /**
     * Execute death phase - predators that haven't eaten.
     */
    private void ejecutarMuertes() {
        for (int i = 0; i < Ecosistema.TAMANO; i++) {
            for (int j = 0; j < Ecosistema.TAMANO; j++) {
                Celda celda = ecosistema.getCelda(i, j);
                Especie habitante = celda.getHabitante();
                
                if (habitante instanceof Depredadores depredador) {
                    if (depredador.deberiasMorirDeHambre()) {
                        celda.marcarMuerte();
                    }
                }
            }
        }
    }

    /**
     * Execute reproduction phase.
     */
    private void ejecutarReproduccion() {
        List<ReproduccionPendiente> reproducciones = new ArrayList<>();
        
        for (int i = 0; i < Ecosistema.TAMANO; i++) {
            for (int j = 0; j < Ecosistema.TAMANO; j++) {
                Celda celda = ecosistema.getCelda(i, j);
                Especie habitante = celda.getHabitante();
                
                if (habitante != null && habitante.puedeReproducirse()) {
                    List<Celda> vacias = ecosistema.getCeldasVaciasAdyacentes(i, j);
                    if (!vacias.isEmpty()) {
                        Celda destino = vacias.get(random.nextInt(vacias.size()));
                        reproducciones.add(new ReproduccionPendiente(habitante, destino));
                    }
                }
            }
        }
        
        // Apply all reproductions
        for (ReproduccionPendiente rep : reproducciones) {
            if (rep.destino.estaVacia()) {
                rep.destino.setHabitante(rep.padre.reproducir());
            }
        }
    }

    /**
     * Finalize turn for all species - increment counters.
     */
    private void finalizarTurnoEspecies() {
        for (int i = 0; i < Ecosistema.TAMANO; i++) {
            for (int j = 0; j < Ecosistema.TAMANO; j++) {
                Especie habitante = ecosistema.getCelda(i, j).getHabitante();
                if (habitante != null) {
                    habitante.incrementarTurnosSobrevividos();
                    
                    if (habitante instanceof Depredadores depredador) {
                        depredador.incrementarTurnosSinComer();
                        depredador.finalizarTurno(turnoActual);
                    } else if (habitante instanceof Carroneros carronero) {
                        carronero.finalizarTurno(turnoActual);
                    }
                }
            }
        }
    }

    // Observer notification methods
    private void notifyTurnEnd(int turno, Map<String, Integer> poblacion, double ocupacion) {
        for (SimulationObserver observer : observers) {
            try {
                observer.onTurnEnd(turno, ecosistema, poblacion, ocupacion);
            } catch (Exception e) {
                // Don't fail if an observer fails
                System.err.println("Observer error: " + e.getMessage());
            }
        }
    }

    private void notifyExtinction(String especie, int turno) {
        for (SimulationObserver observer : observers) {
            try {
                observer.onExtinction(especie, turno);
            } catch (Exception e) {
                System.err.println("Observer error: " + e.getMessage());
            }
        }
    }

    private void notifySimulationStart() {
        for (SimulationObserver observer : observers) {
            try {
                observer.onSimulationStart(ecosistema, escenario);
            } catch (Exception e) {
                System.err.println("Observer error: " + e.getMessage());
            }
        }
    }

    private void notifySimulationEnd() {
        for (SimulationObserver observer : observers) {
            try {
                observer.onSimulationEnd(turnoActual, ecosistema);
            } catch (Exception e) {
                System.err.println("Observer error: " + e.getMessage());
            }
        }
    }

    // Getters
    public Ecosistema getEcosistema() {
        return ecosistema;
    }

    public Escenario getEscenario() {
        return escenario;
    }

    public int getTurnoActual() {
        return turnoActual;
    }

    public boolean isSimulacionActiva() {
        return simulacionActiva;
    }

    public List<EstadoTurno> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    public String getEspecieExtinta() {
        return especieExtinta;
    }

    public int getTurnoExtincion() {
        return turnoExtincion;
    }

    public int getMaxTurnos() {
        return maxTurnos;
    }

    /**
     * Stop the simulation.
     */
    public void detener() {
        simulacionActiva = false;
    }

    // Helper classes for pending operations
    private record MovimientoPendiente(Celda origen, Celda destino, Especie especie) {}
    private record ReproduccionPendiente(Especie padre, Celda destino) {}
}
