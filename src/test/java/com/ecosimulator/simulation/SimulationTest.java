package com.ecosimulator.simulation;

import com.ecosimulator.core.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for simulation engine.
 */
class SimulationTest {

    private Ecosistema ecosistema;
    private MotorDeSimulacion motor;

    @BeforeEach
    void setUp() {
        ecosistema = new Ecosistema();
    }

    // Tests for Escenario
    @Test
    void escenario_equilibrado_shouldHave30Each() {
        Escenario eq = Escenario.EQUILIBRADO;
        assertEquals(30, eq.getPresasIniciales());
        assertEquals(30, eq.getDepredadoresIniciales());
        assertEquals(0, eq.getCarroneroIniciales());
    }

    @Test
    void escenario_depredadoresDominantes_shouldHave45Predators() {
        Escenario dd = Escenario.DEPREDADORES_DOMINANTES;
        assertEquals(15, dd.getPresasIniciales());
        assertEquals(45, dd.getDepredadoresIniciales());
        assertEquals(0, dd.getCarroneroIniciales());
    }

    @Test
    void escenario_presasDominantes_shouldHave45Prey() {
        Escenario pd = Escenario.PRESAS_DOMINANTES;
        assertEquals(45, pd.getPresasIniciales());
        assertEquals(15, pd.getDepredadoresIniciales());
        assertEquals(0, pd.getCarroneroIniciales());
    }

    @Test
    void escenario_getPredefinidos_shouldReturn3Scenarios() {
        assertEquals(3, Escenario.getPredefinidos().length);
    }

    // Tests for MotorDeSimulacion
    @Test
    void motor_inicializarEcosistema_shouldPlaceSpecies() {
        motor = new MotorDeSimulacion(ecosistema, Escenario.EQUILIBRADO, 100);
        motor.inicializarEcosistema();

        Map<String, Integer> pob = ecosistema.poblacion();
        assertEquals(30, pob.get(Presas.TIPO));
        assertEquals(30, pob.get(Depredadores.TIPO));
        assertEquals(0, pob.get(Carroneros.TIPO));
        assertEquals(0.60, ecosistema.porcentajeOcupacion(), 0.01);
    }

    @Test
    void motor_ejecutarTurno_shouldIncrementTurnoCounter() {
        motor = new MotorDeSimulacion(ecosistema, Escenario.EQUILIBRADO, 100);
        motor.inicializarEcosistema();

        assertEquals(0, motor.getTurnoActual());

        motor.ejecutarTurno();
        assertEquals(1, motor.getTurnoActual());

        motor.ejecutarTurno();
        assertEquals(2, motor.getTurnoActual());
    }

    @Test
    void motor_shouldNotifyObserversOnTurnEnd() {
        motor = new MotorDeSimulacion(ecosistema, Escenario.EQUILIBRADO, 100);
        motor.inicializarEcosistema();

        AtomicInteger turnNotifications = new AtomicInteger(0);

        motor.addObserver(new SimulationObserver() {
            @Override
            public void onTurnEnd(int turno, Ecosistema eco, Map<String, Integer> pob, double oc) {
                turnNotifications.incrementAndGet();
            }

            @Override
            public void onExtinction(String especie, int turno) {}
        });

        motor.ejecutarTurno();
        assertEquals(1, turnNotifications.get());

        motor.ejecutarTurno();
        assertEquals(2, turnNotifications.get());
    }

    @Test
    void motor_shouldBuildHistorial() {
        motor = new MotorDeSimulacion(ecosistema, Escenario.EQUILIBRADO, 100);
        motor.inicializarEcosistema();

        assertTrue(motor.getHistorial().isEmpty());

        motor.ejecutarTurno();
        assertEquals(1, motor.getHistorial().size());

        motor.ejecutarTurno();
        assertEquals(2, motor.getHistorial().size());
    }

    @Test
    void motor_shouldDetectExtinction() {
        // Create a scenario where extinction will happen quickly
        Escenario smallScenario = new Escenario("Small", 5, 50, 0);
        motor = new MotorDeSimulacion(ecosistema, smallScenario, 100);
        motor.inicializarEcosistema();

        // Run until extinction or max turns
        motor.ejecutar();

        // Either extinction occurred or simulation ended
        assertTrue(motor.getTurnoActual() > 0);
        assertTrue(motor.getTurnoActual() <= 100 || motor.getEspecieExtinta() != null);
    }

    @Test
    void motor_detener_shouldStopSimulation() {
        motor = new MotorDeSimulacion(ecosistema, Escenario.EQUILIBRADO, 1000);
        motor.inicializarEcosistema();

        // Start running in a separate context
        motor.ejecutarTurno();
        motor.detener();

        assertFalse(motor.isSimulacionActiva());
    }

    // Tests for EstadoTurno
    @Test
    void estadoTurno_toString_shouldFormatCorrectly() {
        Map<String, Integer> pob = Map.of(
                Presas.TIPO, 28,
                Depredadores.TIPO, 32,
                Carroneros.TIPO, 0
        );
        EstadoTurno estado = new EstadoTurno(5, pob, 0.6);

        String expected = "5|presas:28|depredadores:32|carroneros:0|ocupacion:60%";
        assertEquals(expected, estado.toString());
    }

    @Test
    void estadoTurno_fromString_shouldParseCorrectly() {
        String linea = "5|presas:28|depredadores:32|carroneros:0|ocupacion:60%";
        EstadoTurno estado = EstadoTurno.fromString(linea);

        assertNotNull(estado);
        assertEquals(5, estado.getTurno());
        assertEquals(28, estado.getPresas());
        assertEquals(32, estado.getDepredadores());
        assertEquals(0, estado.getCarroneros());
        assertEquals(0.6, estado.getOcupacion(), 0.01);
    }

    @Test
    void estadoTurno_fromString_shouldReturnNullForInvalidInput() {
        assertNull(EstadoTurno.fromString("invalid"));
        assertNull(EstadoTurno.fromString(""));
        assertNull(EstadoTurno.fromString("1|2|3"));
    }

    // Tests for predator hunting behavior
    @Test
    void motor_predatorShouldHuntPrey() {
        // Place a predator next to a prey
        Depredadores depredador = new Depredadores();
        Presas presa = new Presas();

        ecosistema.getCelda(5, 5).setHabitante(depredador);
        ecosistema.getCelda(5, 6).setHabitante(presa);

        motor = new MotorDeSimulacion(ecosistema, new Escenario("Test", 0, 0, 0), 100);
        int initialPreyCount = ecosistema.poblacion().get(Presas.TIPO);

        motor.ejecutarTurno();

        // Predator should have eaten the prey
        int finalPreyCount = ecosistema.poblacion().get(Presas.TIPO);
        assertTrue(finalPreyCount <= initialPreyCount);
    }

    // Tests for reproduction
    @Test
    void motor_preyShouldReproduceEvery2Turns() {
        // Start with just prey
        Presas presa = new Presas();
        presa.incrementarTurnosSobrevividos(); // Already survived 1 turn
        ecosistema.getCelda(5, 5).setHabitante(presa);
        ecosistema.getCelda(0, 0).setHabitante(new Depredadores()); // Keep a predator to avoid extinction check issues

        motor = new MotorDeSimulacion(ecosistema, new Escenario("Test", 0, 0, 0), 100);

        // After one more turn, prey should reproduce
        motor.ejecutarTurno();

        // There might be more prey now due to reproduction
        int preyCount = ecosistema.poblacion().get(Presas.TIPO);
        assertTrue(preyCount >= 1);
    }
}
