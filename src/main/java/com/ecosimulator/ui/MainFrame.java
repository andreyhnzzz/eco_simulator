package com.ecosimulator.ui;

import com.ecosimulator.core.*;
import com.ecosimulator.model.User;
import com.ecosimulator.persistence.EcosystemRepository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main application frame containing the simulation controls and display
 */
public class MainFrame extends JFrame {
    
    // UI dimension constants for responsive layout
    private static final int REPORT_PANEL_WIDTH = 300;
    private static final int UI_MARGIN = 40;
    private static final double ECOSYSTEM_HEIGHT_RATIO = 0.7;
    private static final int TOP_BOTTOM_MARGIN = 80;
    
    private User currentUser;
    private Simulator simulator;
    private Scheduler scheduler;
    private EcosystemRepository repository;
    
    private EcosystemPanel ecosystemPanel;
    private ControlsPanel controlsPanel;
    private ReportPanel reportPanel;
    
    private Map<String, Integer> initialCounts;
    private List<Integer> preyHistory;
    private List<Integer> predatorHistory;
    private List<Integer> thirdHistory;
    
    public MainFrame(User user) {
        this.currentUser = user;
        this.simulator = new Simulator();
        this.scheduler = new Scheduler(simulator);
        this.repository = new EcosystemRepository();
        
        this.preyHistory = new ArrayList<>();
        this.predatorHistory = new ArrayList<>();
        this.thirdHistory = new ArrayList<>();
        
        initializeUI();
        setupCallbacks();
    }
    
    private void initializeUI() {
        setTitle("Eco Simulator - " + currentUser.getNombre());
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Maximize window for better initial layout
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Main panel with border layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel: User info
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Usuario: " + currentUser.getNombre() + " (" + currentUser.getCedula() + ")"));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Center: Split pane with ecosystem and controls
        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerSplitPane.setResizeWeight(0.7);
        
        // Ecosystem panel (top)
        ecosystemPanel = new EcosystemPanel();
        JScrollPane ecosystemScrollPane = new JScrollPane(ecosystemPanel);
        ecosystemScrollPane.setPreferredSize(new Dimension(800, 600));
        centerSplitPane.setTopComponent(ecosystemScrollPane);
        
        // Controls panel (bottom)
        controlsPanel = new ControlsPanel();
        controlsPanel.setStartStopListener(new ControlsPanel.StartStopListener() {
            @Override
            public void onStart(String scenarioPath, int maxTurns, int intervalMs, 
                              boolean enableThirdSpecies, boolean enableMutations) {
                startSimulation(scenarioPath, maxTurns, intervalMs);
            }
            
            @Override
            public void onStop() {
                stopSimulation();
            }
        });
        centerSplitPane.setBottomComponent(controlsPanel);
        
        mainPanel.add(centerSplitPane, BorderLayout.CENTER);
        
        // Right panel: Report/stats with fixed width
        reportPanel = new ReportPanel();
        JScrollPane reportScrollPane = new JScrollPane(reportPanel);
        reportScrollPane.setPreferredSize(new Dimension(REPORT_PANEL_WIDTH, 600));
        mainPanel.add(reportScrollPane, BorderLayout.EAST);
        
        add(mainPanel);
        
        // Add component listener for window resize handling
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Get current window dimensions
                int windowWidth = getWidth();
                int windowHeight = getHeight();
                
                // Update ecosystem panel size (subtract space for borders and report panel)
                int availableWidth = windowWidth - REPORT_PANEL_WIDTH - UI_MARGIN;
                int availableHeight = (int) (windowHeight * ECOSYSTEM_HEIGHT_RATIO) - TOP_BOTTOM_MARGIN;
                
                ecosystemScrollPane.setPreferredSize(new Dimension(availableWidth, availableHeight));
                
                // Update report panel height
                reportScrollPane.setPreferredSize(new Dimension(REPORT_PANEL_WIDTH, windowHeight - TOP_BOTTOM_MARGIN));
                
                // Revalidate main panel (propagates to children) and repaint once
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });
    }
    
    private void setupCallbacks() {
        // Callback for when a turn completes
        scheduler.setOnTurnComplete(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateDisplay();
                    }
                });
            }
        });
        
        // Callback for when simulation ends
        scheduler.setOnSimulationEnd(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        finishSimulation();
                    }
                });
            }
        });
    }
    
    private void startSimulation(String scenarioPath, int maxTurns, int intervalMs) {
        try {
            // Load scenario
            simulator.loadScenario(scenarioPath);
            
            // Save initial state
            String scenarioName = new java.io.File(scenarioPath).getName().replace(".json", "");
            initialCounts = simulator.getCounts();
            repository.saveInitialEcosystem(simulator.getEcosystem(), scenarioName);
            repository.initializeTurnLog();
            
            // Clear history
            preyHistory.clear();
            predatorHistory.clear();
            thirdHistory.clear();
            
            // Update display
            ecosystemPanel.setEcosystem(simulator.getEcosystem());
            ecosystemPanel.repaint();
            
            // Start scheduler
            scheduler.start(intervalMs, maxTurns);
            
            // Update controls
            controlsPanel.setSimulationRunning(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error starting simulation: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void stopSimulation() {
        scheduler.stop();
        controlsPanel.setSimulationRunning(false);
        finishSimulation();
    }
    
    private void updateDisplay() {
        // Update ecosystem panel
        ecosystemPanel.repaint();
        
        // Update statistics
        Map<String, Integer> counts = simulator.getCounts();
        preyHistory.add(counts.get("prey"));
        predatorHistory.add(counts.get("predator"));
        thirdHistory.add(counts.get("third"));
        
        // Update report panel
        reportPanel.updateTurn(simulator.getTurnNumber());
        reportPanel.updateCounts(counts);
        
        // Log turn state
        repository.appendTurnState(simulator.getTurnNumber(), counts, 
                                  simulator.getTurnEvents(), simulator.getEcosystem());
    }
    
    private void finishSimulation() {
        controlsPanel.setSimulationRunning(false);
        
        Map<String, Integer> finalCounts = simulator.getCounts();
        int extinctionTurn = simulator.isExtinct() ? simulator.getTurnNumber() : -1;
        
        // Finalize log
        repository.finalizeTurnLog(simulator.getTurnNumber(), finalCounts, extinctionTurn);
        
        // Update report with final data
        reportPanel.updateFinalStats(initialCounts, finalCounts, extinctionTurn);
        
        // Show completion message
        String message = "Simulation completed after " + simulator.getTurnNumber() + " turns.";
        if (extinctionTurn > 0) {
            message += "\nExtinction occurred at turn " + extinctionTurn + ".";
        }
        
        JOptionPane.showMessageDialog(this, message, "Simulation Complete", 
                                     JOptionPane.INFORMATION_MESSAGE);
    }
}
