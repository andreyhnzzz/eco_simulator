package com.ecosimulator.ui;

import com.ecosimulator.core.Ecosystem;
import com.ecosimulator.model.Cell;

import javax.swing.*;
import java.awt.*;

/**
 * Panel that renders the 10x10 ecosystem grid
 */
public class EcosystemPanel extends JPanel {
    
    private static final int CELL_SIZE = 50;
    private Ecosystem ecosystem;
    
    public EcosystemPanel() {
        setPreferredSize(new Dimension(CELL_SIZE * 10 + 20, CELL_SIZE * 10 + 20));
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
        
        Cell[][] grid = ecosystem.getGrid();
        
        for (int i = 0; i < Ecosystem.GRID_SIZE; i++) {
            for (int j = 0; j < Ecosystem.GRID_SIZE; j++) {
                int x = j * CELL_SIZE + 10;
                int y = i * CELL_SIZE + 10;
                
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
                
                g2d.fillRect(x, y, CELL_SIZE - 2, CELL_SIZE - 2);
                
                // Draw cell border
                g2d.setColor(Color.GRAY);
                g2d.drawRect(x, y, CELL_SIZE - 2, CELL_SIZE - 2);
                
                // Draw icon/text
                if (!cell.isEmpty()) {
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    
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
                    int textX = x + (CELL_SIZE - fm.stringWidth(label)) / 2;
                    int textY = y + (CELL_SIZE + fm.getAscent()) / 2;
                    
                    g2d.drawString(label, textX, textY);
                }
            }
        }
    }
}
