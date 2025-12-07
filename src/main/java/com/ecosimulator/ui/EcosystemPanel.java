package com.ecosimulator.ui;

import com.ecosimulator.core.Ecosystem;
import com.ecosimulator.model.Cell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Panel that renders the 10x10 ecosystem grid with dynamic scaling and zoom support
 */
public class EcosystemPanel extends JPanel {
    
    private static final int MIN_CELL_SIZE = 30;
    private static final int DEFAULT_CELL_SIZE = 50;
    private static final int GRID_MARGIN = 20; // Margin around the grid
    private static final double ZOOM_FACTOR = 1.1; // 10% zoom per scroll
    private static final double MIN_ZOOM = 0.5; // 50% minimum zoom
    private static final double MAX_ZOOM = 3.0; // 300% maximum zoom
    
    private Ecosystem ecosystem;
    private double zoomLevel = 1.0; // Current zoom level (1.0 = 100%)
    
    public EcosystemPanel() {
        setPreferredSize(new Dimension(
            DEFAULT_CELL_SIZE * Ecosystem.GRID_SIZE + GRID_MARGIN,
            DEFAULT_CELL_SIZE * Ecosystem.GRID_SIZE + GRID_MARGIN
        ));
        setBackground(Color.WHITE);
        
        // Add tooltip to inform users about zoom feature
        setToolTipText("Use mouse wheel to zoom in/out");
        
        // Add mouse wheel listener for zoom functionality
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Zoom in when scrolling up (negative rotation), zoom out when scrolling down (positive)
                if (e.getWheelRotation() < 0) {
                    // Zoom in
                    zoomLevel *= ZOOM_FACTOR;
                    if (zoomLevel > MAX_ZOOM) {
                        zoomLevel = MAX_ZOOM;
                    }
                } else {
                    // Zoom out
                    zoomLevel /= ZOOM_FACTOR;
                    if (zoomLevel < MIN_ZOOM) {
                        zoomLevel = MIN_ZOOM;
                    }
                }
                
                // Update panel size and repaint
                updateZoomSize();
            }
        });
    }
    
    /**
     * Update panel size based on current zoom level and refresh display
     */
    private void updateZoomSize() {
        // Update preferred size based on zoom level
        int zoomedWidth = (int) ((DEFAULT_CELL_SIZE * Ecosystem.GRID_SIZE + GRID_MARGIN) * zoomLevel);
        int zoomedHeight = (int) ((DEFAULT_CELL_SIZE * Ecosystem.GRID_SIZE + GRID_MARGIN) * zoomLevel);
        setPreferredSize(new Dimension(zoomedWidth, zoomedHeight));
        
        // Update tooltip to show current zoom level
        setToolTipText(String.format("Zoom: %.0f%% (Use mouse wheel to zoom)", zoomLevel * 100));
        
        // Notify parent container to update scrollbars
        revalidate();
        repaint();
    }
    
    /**
     * Get current zoom level
     * @return zoom level (1.0 = 100%)
     */
    public double getZoomLevel() {
        return zoomLevel;
    }
    
    /**
     * Set zoom level programmatically
     * @param level zoom level (MIN_ZOOM to MAX_ZOOM)
     */
    public void setZoomLevel(double level) {
        this.zoomLevel = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, level));
        updateZoomSize();
    }
    
    public void setEcosystem(Ecosystem ecosystem) {
        this.ecosystem = ecosystem;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (ecosystem == null) {
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Calculate dynamic cell size based on available space and zoom level
        int width = getWidth();
        int height = getHeight();
        int gridCols = Ecosystem.GRID_SIZE;
        int gridRows = Ecosystem.GRID_SIZE;
        
        // Calculate base cell size that fits in the available space
        // Guard against division by zero and ensure positive dimensions
        int baseCellSize = MIN_CELL_SIZE;
        if (gridCols > 0 && gridRows > 0 && width > GRID_MARGIN && height > GRID_MARGIN) {
            baseCellSize = Math.max(MIN_CELL_SIZE, Math.min(
                (width - GRID_MARGIN) / gridCols,
                (height - GRID_MARGIN) / gridRows
            ));
        }
        
        // Apply zoom level to cell size with minimum size constraint
        int cellSize = Math.max(MIN_CELL_SIZE, (int) (baseCellSize * zoomLevel));
        
        // Calculate starting position to center the grid
        int gridWidth = cellSize * gridCols;
        int gridHeight = cellSize * gridRows;
        int startX = Math.max(GRID_MARGIN / 2, (width - gridWidth) / 2);
        int startY = Math.max(GRID_MARGIN / 2, (height - gridHeight) / 2);
        
        Cell[][] grid = ecosystem.getGrid();
        
        for (int i = 0; i < Ecosystem.GRID_SIZE; i++) {
            for (int j = 0; j < Ecosystem.GRID_SIZE; j++) {
                int x = startX + j * cellSize;
                int y = startY + i * cellSize;
                
                Cell cell = grid[i][j];
                
                // Draw cell background
                if (cell.isEmpty()) {
                    g2d.setColor(new Color(240, 240, 240));
                } else {
                    String type = cell.getType();
                    switch (type) {
                        case "prey":
                            g2d.setColor(new Color(100, 200, 100));  // Green
                            break;
                        case "predator":
                            g2d.setColor(new Color(200, 100, 100));  // Red
                            break;
                        case "third":
                            g2d.setColor(new Color(100, 100, 200));  // Blue
                            break;
                        default:
                            g2d.setColor(Color.LIGHT_GRAY);
                    }
                }
                
                g2d.fillRect(x, y, cellSize - 2, cellSize - 2);
                
                // Draw cell border
                g2d.setColor(Color.GRAY);
                g2d.drawRect(x, y, cellSize - 2, cellSize - 2);
                
                // Draw icon/text
                if (!cell.isEmpty()) {
                    g2d.setColor(Color.WHITE);
                    // Scale font size based on cell size
                    int fontSize = Math.max(10, cellSize / 4);
                    g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
                    
                    String label = "";
                    String type = cell.getType();
                    switch (type) {
                        case "prey":
                            label = "P";
                            break;
                        case "predator":
                            label = "D";
                            break;
                        case "third":
                            label = "T";
                            break;
                    }
                    
                    FontMetrics fm = g2d.getFontMetrics();
                    int textX = x + (cellSize - fm.stringWidth(label)) / 2;
                    int textY = y + (cellSize + fm.getAscent()) / 2 - 2;
                    
                    g2d.drawString(label, textX, textY);
                }
            }
        }
    }
}
