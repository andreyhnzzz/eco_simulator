package com.ecosimulator.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AnimationUtils validation methods
 */
class AnimationUtilsTest {

    @Test
    void testValidateSplineControlPointValidValues() {
        // Valid boundary values
        assertEquals(0.0, AnimationUtils.validateSplineControlPoint(0.0, "x1"));
        assertEquals(1.0, AnimationUtils.validateSplineControlPoint(1.0, "y1"));
        assertEquals(0.5, AnimationUtils.validateSplineControlPoint(0.5, "x2"));
        assertEquals(0.33, AnimationUtils.validateSplineControlPoint(0.33, "y2"));
    }

    @Test
    void testValidateSplineControlPointInvalidValueAboveOne() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> AnimationUtils.validateSplineControlPoint(1.5, "y1")
        );
        assertTrue(exception.getMessage().contains("y1"));
        assertTrue(exception.getMessage().contains("[0.0, 1.0]"));
        assertTrue(exception.getMessage().contains("1.5"));
    }

    @Test
    void testValidateSplineControlPointInvalidValueBelowZero() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> AnimationUtils.validateSplineControlPoint(-0.1, "x1")
        );
        assertTrue(exception.getMessage().contains("x1"));
        assertTrue(exception.getMessage().contains("[0.0, 1.0]"));
        assertTrue(exception.getMessage().contains("-0.1"));
    }

    @Test
    void testCreateValidatedSplineWithValidValues() {
        // Should not throw exception for valid values
        assertDoesNotThrow(() -> 
            AnimationUtils.createValidatedSpline(0.33, 1.0, 0.68, 1.0)
        );
    }

    @Test
    void testCreateValidatedSplineWithInvalidX1() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> AnimationUtils.createValidatedSpline(-0.1, 0.5, 0.5, 0.5)
        );
        assertTrue(exception.getMessage().contains("x1"));
    }

    @Test
    void testCreateValidatedSplineWithInvalidY1() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> AnimationUtils.createValidatedSpline(0.5, 1.5, 0.5, 0.5)
        );
        assertTrue(exception.getMessage().contains("y1"));
    }

    @Test
    void testCreateValidatedSplineWithInvalidX2() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> AnimationUtils.createValidatedSpline(0.5, 0.5, 1.1, 0.5)
        );
        assertTrue(exception.getMessage().contains("x2"));
    }

    @Test
    void testCreateValidatedSplineWithInvalidY2() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> AnimationUtils.createValidatedSpline(0.5, 0.5, 0.5, -0.5)
        );
        assertTrue(exception.getMessage().contains("y2"));
    }
}
