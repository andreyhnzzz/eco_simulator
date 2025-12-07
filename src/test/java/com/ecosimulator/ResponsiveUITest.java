package com.ecosimulator;

import com.ecosimulator.core.Ecosystem;
import com.ecosimulator.ui.EcosystemPanel;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for responsive UI behavior
 */
public class ResponsiveUITest {

    @Test
    public void testEcosystemPanelScaling() {
        EcosystemPanel panel = new EcosystemPanel();
        
        // Test that panel has a preferred size
        Dimension preferredSize = panel.getPreferredSize();
        assertNotNull(preferredSize);
        assertTrue(preferredSize.width > 0);
        assertTrue(preferredSize.height > 0);
    }

    @Test
    public void testEcosystemPanelPaintWithDifferentSizes() {
        EcosystemPanel panel = new EcosystemPanel();
        Ecosystem ecosystem = new Ecosystem();
        panel.setEcosystem(ecosystem);
        
        // Test with different panel sizes to ensure dynamic scaling works
        // Small size
        panel.setSize(300, 300);
        assertDoesNotThrow(() -> panel.repaint());
        
        // Large size
        panel.setSize(800, 800);
        assertDoesNotThrow(() -> panel.repaint());
        
        // Very small size
        panel.setSize(100, 100);
        assertDoesNotThrow(() -> panel.repaint());
    }

    @Test
    public void testEcosystemPanelWithNullEcosystem() {
        EcosystemPanel panel = new EcosystemPanel();
        
        // Should handle null ecosystem gracefully when repaint is called
        assertDoesNotThrow(() -> panel.repaint());
    }

    @Test
    public void testZoomFunctionality() {
        EcosystemPanel panel = new EcosystemPanel();
        
        // Test initial zoom level
        assertEquals(1.0, panel.getZoomLevel(), 0.01);
        
        // Test zoom in
        panel.setZoomLevel(1.5);
        assertEquals(1.5, panel.getZoomLevel(), 0.01);
        
        // Test zoom out
        panel.setZoomLevel(0.8);
        assertEquals(0.8, panel.getZoomLevel(), 0.01);
        
        // Test zoom limits - max
        panel.setZoomLevel(5.0);
        assertEquals(3.0, panel.getZoomLevel(), 0.01); // Should be capped at 3.0
        
        // Test zoom limits - min
        panel.setZoomLevel(0.1);
        assertEquals(0.5, panel.getZoomLevel(), 0.01); // Should be capped at 0.5
    }
}
