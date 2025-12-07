package com.ecosimulator.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Report panel displaying statistics, turn counter, and extinction info
 */
public class ReportPanel extends JPanel {
    
    private JTextField turnField;
    private JTextField preyCountField;
    private JTextField predatorCountField;
    private JTextField thirdCountField;
    private JTextField extinctionField;
    private JTextArea statsArea;
    
    public ReportPanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Statistics"));
        setPreferredSize(new Dimension(250, 600));
        
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Turn counter
        gbc.gridx = 0;
        gbc.gridy = row;
        fieldsPanel.add(new JLabel("Turn:"), gbc);
        
        gbc.gridx = 1;
        turnField = new JTextField(10);
        turnField.setEditable(false);
        turnField.setText("0");
        fieldsPanel.add(turnField, gbc);
        
        // Prey count
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        fieldsPanel.add(new JLabel("Prey:"), gbc);
        
        gbc.gridx = 1;
        preyCountField = new JTextField(10);
        preyCountField.setEditable(false);
        preyCountField.setText("0");
        fieldsPanel.add(preyCountField, gbc);
        
        // Predator count
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        fieldsPanel.add(new JLabel("Predators:"), gbc);
        
        gbc.gridx = 1;
        predatorCountField = new JTextField(10);
        predatorCountField.setEditable(false);
        predatorCountField.setText("0");
        fieldsPanel.add(predatorCountField, gbc);
        
        // Third species count
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        fieldsPanel.add(new JLabel("Third Species:"), gbc);
        
        gbc.gridx = 1;
        thirdCountField = new JTextField(10);
        thirdCountField.setEditable(false);
        thirdCountField.setText("0");
        fieldsPanel.add(thirdCountField, gbc);
        
        // Extinction field
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        fieldsPanel.add(new JLabel("Extinction:"), gbc);
        
        gbc.gridx = 1;
        extinctionField = new JTextField(10);
        extinctionField.setEditable(false);
        extinctionField.setText("-");
        fieldsPanel.add(extinctionField, gbc);
        
        add(fieldsPanel, BorderLayout.NORTH);
        
        // Stats area
        statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(statsArea);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void updateTurn(int turn) {
        turnField.setText(String.valueOf(turn));
    }
    
    public void updateCounts(Map<String, Integer> counts) {
        preyCountField.setText(String.valueOf(counts.get("prey")));
        predatorCountField.setText(String.valueOf(counts.get("predator")));
        thirdCountField.setText(String.valueOf(counts.get("third")));
    }
    
    public void updateFinalStats(Map<String, Integer> initialCounts, 
                                 Map<String, Integer> finalCounts, 
                                 int extinctionTurn) {
        if (extinctionTurn > 0) {
            extinctionField.setText("Turn " + extinctionTurn);
        } else {
            extinctionField.setText("No extinction");
        }
        
        StringBuilder stats = new StringBuilder();
        stats.append("=== FINAL STATISTICS ===\n\n");
        stats.append("Initial Population:\n");
        stats.append("  Prey: ").append(initialCounts.get("prey")).append("\n");
        stats.append("  Predators: ").append(initialCounts.get("predator")).append("\n");
        stats.append("  Third: ").append(initialCounts.get("third")).append("\n\n");
        
        stats.append("Final Population:\n");
        stats.append("  Prey: ").append(finalCounts.get("prey")).append("\n");
        stats.append("  Predators: ").append(finalCounts.get("predator")).append("\n");
        stats.append("  Third: ").append(finalCounts.get("third")).append("\n\n");
        
        int totalInitial = initialCounts.get("prey") + initialCounts.get("predator") + initialCounts.get("third");
        int totalFinal = finalCounts.get("prey") + finalCounts.get("predator") + finalCounts.get("third");
        stats.append("Total Change: ");
        stats.append(totalFinal - totalInitial).append("\n");
        
        statsArea.setText(stats.toString());
    }
}
