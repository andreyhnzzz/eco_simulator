package com.ecosimulator.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for core domain classes.
 */
class CoreTest {

    private Ecosistema ecosistema;

    @BeforeEach
    void setUp() {
        ecosistema = new Ecosistema();
    }

    // Tests for Presas
    @Test
    void presas_shouldReproduceEvery2Turns() {
        Presas presa = new Presas();
        assertFalse(presa.puedeReproducirse()); // turno 0
        
        presa.incrementarTurnosSobrevividos();
        assertFalse(presa.puedeReproducirse()); // turno 1
        
        presa.incrementarTurnosSobrevividos();
        assertTrue(presa.puedeReproducirse()); // turno 2
        
        presa.incrementarTurnosSobrevividos();
        assertFalse(presa.puedeReproducirse()); // turno 3
        
        presa.incrementarTurnosSobrevividos();
        assertTrue(presa.puedeReproducirse()); // turno 4
    }

    @Test
    void presas_withFertilidadMutation_shouldReproduceEveryTurn() {
        Presas presa = new Presas(EnumSet.of(Mutacion.FERTILIDAD));
        assertFalse(presa.puedeReproducirse()); // turno 0
        
        presa.incrementarTurnosSobrevividos();
        assertTrue(presa.puedeReproducirse()); // turno 1
        
        presa.incrementarTurnosSobrevividos();
        assertTrue(presa.puedeReproducirse()); // turno 2
    }

    @Test
    void presas_reproduce_shouldCreateNewPresa() {
        Presas presa = new Presas();
        Especie offspring = presa.reproducir();
        
        assertInstanceOf(Presas.class, offspring);
        assertEquals(0, offspring.getTurnosSobrevividos());
        assertEquals(Presas.TIPO, offspring.getTipo());
    }

    // Tests for Depredadores
    @Test
    void depredadores_shouldDieAfter3TurnsWithoutEating() {
        Depredadores depredador = new Depredadores();
        
        depredador.incrementarTurnosSinComer();
        assertFalse(depredador.deberiasMorirDeHambre()); // 1 turno
        
        depredador.incrementarTurnosSinComer();
        assertFalse(depredador.deberiasMorirDeHambre()); // 2 turnos
        
        depredador.incrementarTurnosSinComer();
        assertTrue(depredador.deberiasMorirDeHambre()); // 3 turnos
    }

    @Test
    void depredadores_withResistenciaMutation_shouldSurvive5TurnsWithoutEating() {
        Depredadores depredador = new Depredadores(EnumSet.of(Mutacion.RESISTENCIA_HAMBRE));
        
        for (int i = 0; i < 4; i++) {
            depredador.incrementarTurnosSinComer();
            assertFalse(depredador.deberiasMorirDeHambre());
        }
        
        depredador.incrementarTurnosSinComer();
        assertTrue(depredador.deberiasMorirDeHambre()); // 5 turnos
    }

    @Test
    void depredadores_shouldReproduceAfterEating() {
        Depredadores depredador = new Depredadores();
        
        // Cannot reproduce without eating
        depredador.incrementarTurnosSobrevividos();
        depredador.incrementarTurnosSobrevividos();
        assertFalse(depredador.puedeReproducirse());
        
        // Can reproduce after eating
        depredador.registrarComida(2);
        assertTrue(depredador.puedeReproducirse());
    }

    @Test
    void depredadores_withFertilidadMutation_canReproduceWithoutEating() {
        Depredadores depredador = new Depredadores(EnumSet.of(Mutacion.FERTILIDAD));
        
        depredador.incrementarTurnosSobrevividos();
        depredador.incrementarTurnosSobrevividos();
        assertTrue(depredador.puedeReproducirse());
    }

    // Tests for Carroneros
    @Test
    void carroneros_shouldReproduceAfterCleaning() {
        Carroneros carronero = new Carroneros();
        
        // Cannot reproduce without cleaning
        carronero.incrementarTurnosSobrevividos();
        carronero.incrementarTurnosSobrevividos();
        assertFalse(carronero.puedeReproducirse());
        
        // Can reproduce after cleaning
        carronero.registrarLimpieza(2);
        assertTrue(carronero.puedeReproducirse());
    }

    // Tests for Celda
    @Test
    void celda_shouldTrackHabitanteAndCadaver() {
        Celda celda = new Celda(0, 0);
        
        assertTrue(celda.estaVacia());
        assertFalse(celda.tieneCadaver());
        
        celda.setHabitante(new Presas());
        assertFalse(celda.estaVacia());
        
        celda.marcarMuerte();
        assertTrue(celda.estaVacia());
        assertTrue(celda.tieneCadaver());
        
        celda.limpiarCadaver();
        assertFalse(celda.tieneCadaver());
    }

    // Tests for Ecosistema
    @Test
    void ecosistema_shouldInitializeWith10x10Matrix() {
        assertEquals(10, Ecosistema.TAMANO);
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertNotNull(ecosistema.getCelda(i, j));
            }
        }
    }

    @Test
    void ecosistema_shouldReturnNullForOutOfBoundsCells() {
        assertNull(ecosistema.getCelda(-1, 0));
        assertNull(ecosistema.getCelda(0, -1));
        assertNull(ecosistema.getCelda(10, 0));
        assertNull(ecosistema.getCelda(0, 10));
    }

    @Test
    void ecosistema_poblacion_shouldCountSpeciesCorrectly() {
        ecosistema.getCelda(0, 0).setHabitante(new Presas());
        ecosistema.getCelda(0, 1).setHabitante(new Presas());
        ecosistema.getCelda(1, 0).setHabitante(new Depredadores());
        ecosistema.getCelda(2, 0).setHabitante(new Carroneros());
        
        Map<String, Integer> pob = ecosistema.poblacion();
        
        assertEquals(2, pob.get(Presas.TIPO));
        assertEquals(1, pob.get(Depredadores.TIPO));
        assertEquals(1, pob.get(Carroneros.TIPO));
    }

    @Test
    void ecosistema_hayExtincion_shouldDetectExtinction() {
        // Empty ecosystem is considered extinction (both at 0)
        assertTrue(ecosistema.hayExtincion());
        
        // Add only prey - predators are 0, so extinction
        ecosistema.getCelda(0, 0).setHabitante(new Presas());
        assertTrue(ecosistema.hayExtincion()); // No predators = extinction
        
        // Add a predator - now both exist, no extinction
        ecosistema.getCelda(1, 0).setHabitante(new Depredadores());
        assertFalse(ecosistema.hayExtincion()); // Both exist
        
        // Remove prey - now only predators, prey extinction
        ecosistema.getCelda(0, 0).limpiar();
        assertTrue(ecosistema.hayExtincion()); // No presas = extinction
    }

    @Test
    void ecosistema_porcentajeOcupacion_shouldCalculateCorrectly() {
        assertEquals(0.0, ecosistema.porcentajeOcupacion());
        
        ecosistema.getCelda(0, 0).setHabitante(new Presas());
        assertEquals(0.01, ecosistema.porcentajeOcupacion(), 0.001);
        
        for (int i = 0; i < 10; i++) {
            ecosistema.getCelda(i, 0).setHabitante(new Presas());
        }
        assertEquals(0.10, ecosistema.porcentajeOcupacion(), 0.001);
    }

    @Test
    void ecosistema_getCeldasAdyacentes_shouldReturnCorrectCells() {
        // Corner cell (0,0) should have 3 adjacent cells
        assertEquals(3, ecosistema.getCeldasAdyacentes(0, 0).size());
        
        // Edge cell should have 5 adjacent cells
        assertEquals(5, ecosistema.getCeldasAdyacentes(0, 5).size());
        
        // Center cell should have 8 adjacent cells
        assertEquals(8, ecosistema.getCeldasAdyacentes(5, 5).size());
    }

    @Test
    void ecosistema_getCeldasVaciasAdyacentes_shouldFilterCorrectly() {
        ecosistema.getCelda(0, 1).setHabitante(new Presas());
        ecosistema.getCelda(1, 0).setHabitante(new Depredadores());
        
        // (0,0) has 3 adjacent, but 2 are occupied
        assertEquals(1, ecosistema.getCeldasVaciasAdyacentes(0, 0).size());
    }

    // Tests for Mutacion
    @Test
    void mutacion_velocidad_shouldDoubleMovement() {
        Presas presaNormal = new Presas();
        assertEquals(1, presaNormal.getCeldasMovimiento());
        
        Presas presaVeloz = new Presas(EnumSet.of(Mutacion.VELOCIDAD));
        assertEquals(2, presaVeloz.getCeldasMovimiento());
    }
}
