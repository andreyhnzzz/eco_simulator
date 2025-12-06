package com.ecosimulator;

import com.ecosimulator.model.*;
import com.ecosimulator.service.EventLogger;
import com.ecosimulator.service.ReproductionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for sex differentiation, reproduction rules, and corpse mechanics
 */
class SexReproductionTest {

    private EventLogger eventLogger;
    private ReproductionManager reproductionManager;

    @BeforeEach
    void setUp() {
        eventLogger = new EventLogger();
        reproductionManager = new ReproductionManager(eventLogger);
        reproductionManager.setCurrentTurn(1);
    }

    // Sex enum tests
    @Test
    void testSexEnumValues() {
        assertEquals(2, Sex.values().length);
        assertNotNull(Sex.MALE);
        assertNotNull(Sex.FEMALE);
    }

    @Test
    void testSexEnumProperties() {
        assertEquals("Male", Sex.MALE.getDisplayName());
        assertEquals("Female", Sex.FEMALE.getDisplayName());
        assertEquals("M", Sex.MALE.getSymbol());
        assertEquals("F", Sex.FEMALE.getSymbol());
        assertEquals("♂", Sex.MALE.getUnicodeSymbol());
        assertEquals("♀", Sex.FEMALE.getUnicodeSymbol());
    }

    // Creature sex tests
    @Test
    void testCreatureHasSex() {
        Creature creature = new Creature(CellType.PREDATOR, 0, 0);
        assertNotNull(creature.getSex());
        assertTrue(creature.getSex() == Sex.MALE || creature.getSex() == Sex.FEMALE);
    }

    @Test
    void testCreatureWithSpecificSex() {
        Creature malePredator = new Creature(CellType.PREDATOR, 0, 0, Sex.MALE);
        Creature femalePredator = new Creature(CellType.PREDATOR, 1, 1, Sex.FEMALE);
        
        assertEquals(Sex.MALE, malePredator.getSex());
        assertEquals(Sex.FEMALE, femalePredator.getSex());
    }

    @Test
    void testCreatureSexIsImmutable() {
        Creature creature = new Creature(CellType.PREY, 0, 0, Sex.MALE);
        Sex initialSex = creature.getSex();
        
        // Sex should remain the same - no setter available
        assertEquals(initialSex, creature.getSex());
    }

    @Test
    void testCreatureUniqueId() {
        Creature c1 = new Creature(CellType.PREDATOR, 0, 0);
        Creature c2 = new Creature(CellType.PREDATOR, 1, 1);
        
        assertNotEquals(c1.getId(), c2.getId());
    }

    @Test
    void testCreatureIdString() {
        Creature male = new Creature(CellType.PREDATOR, 0, 0, Sex.MALE);
        Creature female = new Creature(CellType.PREY, 1, 1, Sex.FEMALE);
        
        assertTrue(male.getIdString().startsWith("M-"));
        assertTrue(female.getIdString().startsWith("F-"));
    }

    // Maturity tests
    @Test
    void testCreatureStartsImmature() {
        Creature creature = new Creature(CellType.PREDATOR, 0, 0);
        assertFalse(creature.isMature());
    }

    @Test
    void testCreatureBecomesMatureAfterAge() {
        Creature creature = new Creature(CellType.PREDATOR, 0, 0);
        creature.setEnergy(100); // Ensure doesn't die
        
        // Age the creature 5 times (maturity threshold)
        for (int i = 0; i < 5; i++) {
            creature.age();
        }
        
        assertTrue(creature.isMature());
    }

    // Mating cooldown tests
    @Test
    void testCreatureStartsWithNoMatingCooldown() {
        Creature creature = new Creature(CellType.PREDATOR, 0, 0);
        assertTrue(creature.canMate());
        assertEquals(0, creature.getMatingCooldown());
    }

    @Test
    void testMatingCooldownAfterReproduction() {
        Creature creature = new Creature(CellType.PREDATOR, 0, 0);
        creature.startMatingCooldown();
        
        assertFalse(creature.canMate());
        assertTrue(creature.getMatingCooldown() > 0);
    }

    @Test
    void testMatingCooldownDecreasesWithAge() {
        Creature creature = new Creature(CellType.PREDATOR, 0, 0);
        creature.setEnergy(100);
        creature.startMatingCooldown();
        
        int initialCooldown = creature.getMatingCooldown();
        creature.age();
        
        assertEquals(initialCooldown - 1, creature.getMatingCooldown());
    }

    // ReproductionManager tests
    @Test
    void testCanMateRequiresOppositesSex() {
        Creature male = new Creature(CellType.PREDATOR, 0, 0, Sex.MALE);
        Creature female = new Creature(CellType.PREDATOR, 0, 1, Sex.FEMALE);
        Creature male2 = new Creature(CellType.PREDATOR, 1, 0, Sex.MALE);
        
        // Make them mature
        male.setEnergy(100);
        female.setEnergy(100);
        male2.setEnergy(100);
        for (int i = 0; i < 5; i++) {
            male.age();
            female.age();
            male2.age();
        }
        
        // Reset energy for reproduction
        male.setEnergy(30);
        female.setEnergy(30);
        male2.setEnergy(30);
        
        // Opposite sex should work
        assertNull(reproductionManager.canMate(male, female));
        
        // Same sex should fail
        String reason = reproductionManager.canMate(male, male2);
        assertNotNull(reason);
        assertEquals("same sex", reason);
    }

    @Test
    void testCanMateRequiresSameSpecies() {
        Creature predator = new Creature(CellType.PREDATOR, 0, 0, Sex.MALE);
        Creature prey = new Creature(CellType.PREY, 0, 1, Sex.FEMALE);
        
        // Make them mature
        predator.setEnergy(100);
        prey.setEnergy(100);
        for (int i = 0; i < 5; i++) {
            predator.age();
            prey.age();
        }
        predator.setEnergy(30);
        prey.setEnergy(20);
        
        String reason = reproductionManager.canMate(predator, prey);
        assertNotNull(reason);
        assertEquals("different species", reason);
    }

    @Test
    void testCanMateRequiresMaturity() {
        Creature mature = new Creature(CellType.PREDATOR, 0, 0, Sex.MALE);
        Creature immature = new Creature(CellType.PREDATOR, 0, 1, Sex.FEMALE);
        
        // Only make one mature
        mature.setEnergy(100);
        for (int i = 0; i < 5; i++) {
            mature.age();
        }
        mature.setEnergy(30);
        immature.setEnergy(30);
        
        String reason = reproductionManager.canMate(mature, immature);
        assertNotNull(reason);
        assertTrue(reason.contains("immature"));
    }

    @Test
    void testCanMateRequiresSufficientEnergy() {
        Creature wellFed = new Creature(CellType.PREDATOR, 0, 0, Sex.MALE);
        Creature hungry = new Creature(CellType.PREDATOR, 0, 1, Sex.FEMALE);
        
        // Make both mature
        wellFed.setEnergy(100);
        hungry.setEnergy(100);
        for (int i = 0; i < 5; i++) {
            wellFed.age();
            hungry.age();
        }
        
        // Give one enough energy, not the other
        wellFed.setEnergy(30);
        hungry.setEnergy(5);
        
        String reason = reproductionManager.canMate(wellFed, hungry);
        assertNotNull(reason);
        assertTrue(reason.contains("insufficient energy"));
    }

    @Test
    void testSuccessfulReproduction() {
        Creature male = new Creature(CellType.PREY, 0, 0, Sex.MALE);
        Creature female = new Creature(CellType.PREY, 0, 1, Sex.FEMALE);
        
        // Make both mature and well-fed
        male.setEnergy(100);
        female.setEnergy(100);
        for (int i = 0; i < 5; i++) {
            male.age();
            female.age();
        }
        male.setEnergy(20);
        female.setEnergy(20);
        
        Creature offspring = reproductionManager.reproduce(male, female, 1, 1, false);
        
        assertNotNull(offspring);
        assertEquals(CellType.PREY, offspring.getType());
        assertNotNull(offspring.getSex());
        assertEquals(1, offspring.getRow());
        assertEquals(1, offspring.getCol());
        
        // Parents should be in cooldown
        assertFalse(male.canMate());
        assertFalse(female.canMate());
    }

    // Corpse tests
    @Test
    void testCorpseCreation() {
        Creature creature = new Creature(CellType.PREDATOR, 5, 5, Sex.MALE);
        Corpse corpse = new Corpse(creature);
        
        assertEquals(creature.getId(), corpse.getOriginalCreatureId());
        assertEquals(CellType.PREDATOR, corpse.getOriginalType());
        assertEquals(Sex.MALE, corpse.getOriginalSex());
        assertEquals(5, corpse.getRow());
        assertEquals(5, corpse.getCol());
        assertTrue(corpse.getNutritionalValue() > 0);
    }

    @Test
    void testCorpseDecay() {
        Creature creature = new Creature(CellType.PREY, 0, 0);
        Corpse corpse = new Corpse(creature);
        
        int initialDecay = corpse.getDecayTimer();
        assertFalse(corpse.isDecayed());
        
        // Decay once
        boolean decayed = corpse.decay();
        assertFalse(decayed);
        assertEquals(initialDecay - 1, corpse.getDecayTimer());
        
        // Decay until fully decayed
        while (!corpse.decay()) {
            // Keep decaying
        }
        assertTrue(corpse.isDecayed());
    }

    @Test
    void testCorpseConsumption() {
        Creature creature = new Creature(CellType.PREDATOR, 0, 0);
        Corpse corpse = new Corpse(creature);
        
        int value = corpse.consume();
        assertTrue(value > 0);
        assertEquals(0, corpse.getNutritionalValue());
        assertTrue(corpse.isDecayed());
    }

    @Test
    void testCorpseNutritionalValueByType() {
        Corpse predatorCorpse = new Corpse(new Creature(CellType.PREDATOR, 0, 0));
        Corpse preyCorpse = new Corpse(new Creature(CellType.PREY, 0, 0));
        
        // Predator corpses should be more nutritious
        assertTrue(predatorCorpse.getNutritionalValue() > preyCorpse.getNutritionalValue());
    }

    // CellType tests for CORPSE
    @Test
    void testCellTypeCorpse() {
        assertEquals("Corpse", CellType.CORPSE.getDisplayName());
        assertEquals("X", CellType.CORPSE.getSymbol());
        assertFalse(CellType.CORPSE.isLiving());
    }

    @Test
    void testCellTypeLivingCheck() {
        assertTrue(CellType.PREDATOR.isLiving());
        assertTrue(CellType.PREY.isLiving());
        assertTrue(CellType.THIRD_SPECIES.isLiving());
        assertFalse(CellType.EMPTY.isLiving());
        assertFalse(CellType.CORPSE.isLiving());
    }

    // EventLogger tests
    @Test
    void testEventLoggerRecordsEvents() {
        Creature male = new Creature(CellType.PREDATOR, 0, 0, Sex.MALE);
        Creature female = new Creature(CellType.PREDATOR, 0, 1, Sex.FEMALE);
        
        eventLogger.logDeathByStarvation(1, male);
        
        assertEquals(1, eventLogger.getEntryCount());
        var entries = eventLogger.getEntries();
        assertFalse(entries.isEmpty());
        assertEquals(EventLogger.EventType.DEATH_STARVATION, entries.get(0).getType());
    }

    @Test
    void testEventLoggerFiltersByType() {
        Creature c1 = new Creature(CellType.PREDATOR, 0, 0);
        Creature c2 = new Creature(CellType.PREY, 1, 1);
        
        eventLogger.logDeathByStarvation(1, c1);
        eventLogger.logMutationActivated(1, c2);
        
        var starvationEvents = eventLogger.getEntriesByType(EventLogger.EventType.DEATH_STARVATION);
        var mutationEvents = eventLogger.getEntriesByType(EventLogger.EventType.MUTATION_ACTIVATED);
        
        assertEquals(1, starvationEvents.size());
        assertEquals(1, mutationEvents.size());
    }
}
