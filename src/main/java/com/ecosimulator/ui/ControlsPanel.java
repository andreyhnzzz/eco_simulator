package com.ecosimulator.ui;

import com.ecosimulator.core.ScenarioConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Control panel with scenario selector, options, and start/stop buttons
 */
public class ControlsPanel extends JPanel {
    
    private JComboBox<String> scenarioCombo;
    private JCheckBox thirdSpeciesCheckbox;
    private JCheckBox mutationsCheckbox;
    private JSpinner maxTurnsSpinner;
    private JSlider intervalSlider;
    private JLabel intervalLabel;
    private JButton startButton;
    private JButton stopButton;
    
    private StartStopListener listener;
    
    public interface StartStopListener {
        void onStart(String scenarioPath, int maxTurns, int intervalMs, 
                    boolean enableThirdSpecies, boolean enableMutations);
        void onStop();
    }
    
    public ControlsPanel() {
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Simulation Controls"));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Scenario selector
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Scenario:"), gbc);
        
        gbc.gridx = 1;
        String[] scenarioNames = ScenarioConfig.getScenarioNames();
        scenarioCombo = new JComboBox<>(scenarioNames);
        formPanel.add(scenarioCombo, gbc);
        
        // Extensions checkboxes
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Extensions:"), gbc);
        
        JPanel extensionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thirdSpeciesCheckbox = new JCheckBox("Third Species");
        mutationsCheckbox = new JCheckBox("Mutations");
        extensionsPanel.add(thirdSpeciesCheckbox);
        extensionsPanel.add(mutationsCheckbox);
        
        gbc.gridx = 1;
        formPanel.add(extensionsPanel, gbc);
        
        // Max turns spinner
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Max Turns:"), gbc);
        
        gbc.gridx = 1;
        maxTurnsSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 10));
        formPanel.add(maxTurnsSpinner, gbc);
        
        // Interval slider
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        formPanel.add(new JLabel("Interval (ms):"), gbc);
        
        JPanel sliderPanel = new JPanel(new BorderLayout());
        intervalSlider = new JSlider(100, 2000, 1000);
        intervalSlider.setMajorTickSpacing(500);
        intervalSlider.setMinorTickSpacing(100);
        intervalSlider.setPaintTicks(true);
        intervalSlider.setPaintLabels(true);
        
        intervalLabel = new JLabel("1000 ms");
        intervalSlider.addChangeListener(e -> {
            intervalLabel.setText(intervalSlider.getValue() + " ms");
        });
        
        sliderPanel.add(intervalSlider, BorderLayout.CENTER);
        sliderPanel.add(intervalLabel, BorderLayout.EAST);
        
        gbc.gridx = 1;
        formPanel.add(sliderPanel, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        startButton = new JButton("Start Simulation");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleStart();
            }
        });
        buttonPanel.add(startButton);
        
        stopButton = new JButton("Stop Simulation");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleStop();
            }
        });
        buttonPanel.add(stopButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void handleStart() {
        if (listener != null) {
            int selectedIndex = scenarioCombo.getSelectedIndex();
            String[] scenarioPaths = ScenarioConfig.getAvailableScenarios();
            String scenarioPath = scenarioPaths[selectedIndex];
            
            int maxTurns = (Integer) maxTurnsSpinner.getValue();
            int intervalMs = intervalSlider.getValue();
            boolean enableThirdSpecies = thirdSpeciesCheckbox.isSelected();
            boolean enableMutations = mutationsCheckbox.isSelected();
            
            listener.onStart(scenarioPath, maxTurns, intervalMs, enableThirdSpecies, enableMutations);
        }
    }
    
    private void handleStop() {
        if (listener != null) {
            listener.onStop();
        }
    }
    
    public void setStartStopListener(StartStopListener listener) {
        this.listener = listener;
    }
    
    public void setSimulationRunning(boolean running) {
        startButton.setEnabled(!running);
        stopButton.setEnabled(running);
        scenarioCombo.setEnabled(!running);
        thirdSpeciesCheckbox.setEnabled(!running);
        mutationsCheckbox.setEnabled(!running);
        maxTurnsSpinner.setEnabled(!running);
        intervalSlider.setEnabled(!running);
    }
}
