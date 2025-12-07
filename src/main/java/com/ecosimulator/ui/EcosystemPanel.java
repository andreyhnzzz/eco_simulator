package com.ecosimulator.ui;

import com.ecosimulator.core.Ecosystem;
import com.ecosimulator.model.Cell;

import javax.swing.*;
import java.awt.*;

/**
 * Panel that renders the 10x10 ecosystem grid with dynamic scaling
 */
public class EcosystemPanel extends JPanel {
    
    private static final int MIN_CELL_SIZE = 30;
    private static final int DEFAULT_CELL_SIZE = 50;
    private static final int GRID_MARGIN = 20; // Margin around the grid
    private Ecosystem ecosystem;
    
    public EcosystemPanel() {
        setPreferredSize(new Dimension(
            DEFAULT_CELL_SIZE * Ecosystem.GRID_SIZE + GRID_MARGIN,
            DEFAULT_CELL_SIZE * Ecosystem.GRID_SIZE + GRID_MARGIN
        ));
        setBackground(Color.WHITE);
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
        
        // Calculate dynamic cell size based on available space
        int width = getWidth();
        int height = getHeight();
        int gridCols = Ecosystem.GRID_SIZE;
        int gridRows = Ecosystem.GRID_SIZE;
        
        // Calculate cell size that fits in the available space
        // Guard against division by zero and ensure positive dimensions
        int cellSize = MIN_CELL_SIZE;
        if (gridCols > 0 && gridRows > 0 && width > GRID_MARGIN && height > GRID_MARGIN) {
            cellSize = Math.max(MIN_CELL_SIZE, Math.min(
                (width - GRID_MARGIN) / gridCols,
                (height - GRID_MARGIN) / gridRows
            ));
        }
        
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
