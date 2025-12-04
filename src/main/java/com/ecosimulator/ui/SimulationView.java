package com.ecosimulator.ui;

import com.ecosimulator.auth.Session;
import com.ecosimulator.model.*;
import com.ecosimulator.report.PDFReportGenerator;
import com.ecosimulator.service.EmailService;
import com.ecosimulator.simulation.*;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.animation.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main simulation view controller with premium animations and visual effects
 * Features glassmorphism panels, smooth animations, and modern UI/UX
 */
public class SimulationView extends BorderPane {
    private static final Logger LOGGER = Logger.getLogger(SimulationView.class.getName());

    // UI Components
    private GridPane gridPane;
    private VBox controlPanel;
    private VBox statsPanel;
    private HBox statusPanel;
    private ComboBox<Scenario> scenarioComboBox;
    private CheckBox thirdSpeciesCheckBox;
    private CheckBox mutationsCheckBox;
    private Slider speedSlider;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
    private Button settingsButton;
    private Label turnLabel;
    private Label predatorLabel;
    private Label preyLabel;
    private Label thirdSpeciesLabel;
    private Label mutatedLabel;
    private Label statusLabel;
    private ProgressBar progressBar;

    // Simulation components
    private SimulationConfig config;
    private SimulationEngine engine;
    private SimulationRunner runner;
    private EmailService emailService;

    // Grid cells for animation
    private Rectangle[][] gridCells;
    
    // Track previous cell states for animations
    private CellType[][] previousGrid;
    private Map<String, Timeline> mutationAnimations = new HashMap<>();

    private static final int CELL_SIZE = 20;
    private static final int DEFAULT_GRID_SIZE = 25;

    public SimulationView() {
        this.config = new SimulationConfig().withGridSize(DEFAULT_GRID_SIZE);
        this.emailService = new EmailService();
        this.previousGrid = new CellType[DEFAULT_GRID_SIZE][DEFAULT_GRID_SIZE];
        
        // Initialize previous grid to EMPTY
        for (int i = 0; i < DEFAULT_GRID_SIZE; i++) {
            for (int j = 0; j < DEFAULT_GRID_SIZE; j++) {
                previousGrid[i][j] = CellType.EMPTY;
            }
        }
        
        initializeUI();
        initializeSimulation();
        applyStyles();
        playEntranceAnimations();
    }

    private void initializeUI() {
        setPadding(new Insets(20));
        
        // Create a layered background with subtle blur effect
        createBackgroundLayer();
        
        // Top - Controls with glassmorphism panel
        controlPanel = createControlPanel();
        setTop(controlPanel);
        
        // Center - Simulation Grid
        setCenter(createGridPanel());
        
        // Right - Statistics with glassmorphism
        statsPanel = createStatsPanel();
        setRight(statsPanel);
        
        // Bottom - Status
        statusPanel = createStatusPanel();
        setBottom(statusPanel);
    }

    /**
     * Creates a subtle background layer for visual depth
     */
    private void createBackgroundLayer() {
        // The main background gradient is applied via CSS
        getStyleClass().add("main-view");
    }

    private VBox createControlPanel() {
        VBox panel = new VBox(18);
        panel.setPadding(new Insets(20, 20, 25, 20));
        panel.setAlignment(Pos.CENTER);
        panel.getStyleClass().addAll("glass-panel", "control-panel");

        // Title with enhanced styling
        Label titleLabel = new Label("🌿 Eco Simulator 🌿");
        titleLabel.getStyleClass().add("title-label");
        
        // Subtitle
        Label subtitleLabel = new Label("Simulador Ecológico Interactivo");
        subtitleLabel.getStyleClass().add("subtitle-label");

        // Scenario selection with enhanced styling
        HBox scenarioBox = new HBox(12);
        scenarioBox.setAlignment(Pos.CENTER);
        
        Label scenarioLabel = new Label("Escenario:");
        scenarioLabel.getStyleClass().add("control-label");
        
        scenarioComboBox = new ComboBox<>();
        scenarioComboBox.getItems().addAll(Scenario.values());
        scenarioComboBox.setValue(Scenario.BALANCED);
        scenarioComboBox.getStyleClass().add("combo-box-custom");
        scenarioComboBox.setOnAction(e -> updateConfig());
        
        // Add tooltip
        scenarioComboBox.setTooltip(new Tooltip("Seleccione el escenario inicial de la simulación"));
        
        scenarioBox.getChildren().addAll(scenarioLabel, scenarioComboBox);

        // Extensions (checkboxes) with enhanced styling
        HBox extensionsBox = new HBox(35);
        extensionsBox.setAlignment(Pos.CENTER);
        
        thirdSpeciesCheckBox = new CheckBox("Tercer Especie 🦎");
        thirdSpeciesCheckBox.getStyleClass().add("extension-checkbox");
        thirdSpeciesCheckBox.setOnAction(e -> updateConfig());
        thirdSpeciesCheckBox.setTooltip(new Tooltip("Añade una tercera especie al ecosistema"));
        
        mutationsCheckBox = new CheckBox("Mutaciones 🧬");
        mutationsCheckBox.getStyleClass().add("extension-checkbox");
        mutationsCheckBox.setOnAction(e -> updateConfig());
        mutationsCheckBox.setTooltip(new Tooltip("Habilita mutaciones genéticas aleatorias"));
        
        extensionsBox.getChildren().addAll(thirdSpeciesCheckBox, mutationsCheckBox);

        // Speed control with enhanced slider
        HBox speedBox = new HBox(12);
        speedBox.setAlignment(Pos.CENTER);
        
        Label speedLabel = new Label("Velocidad:");
        speedLabel.getStyleClass().add("control-label");
        
        speedSlider = new Slider(500, 3000, 1000);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(500);
        speedSlider.setPrefWidth(220);
        speedSlider.getStyleClass().add("speed-slider");
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (runner != null) {
                runner.setTurnDelay(newVal.intValue());
            }
        });
        
        Label speedValueLabel = new Label("1000ms");
        speedValueLabel.getStyleClass().add("speed-value");
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            speedValueLabel.setText(newVal.intValue() + "ms"));
        
        speedBox.getChildren().addAll(speedLabel, speedSlider, speedValueLabel);

        // Action buttons with premium styling and animations
        HBox buttonBox = new HBox(18);
        buttonBox.setAlignment(Pos.CENTER);
        
        startButton = createAnimatedButton("▶ Iniciar", "start-button");
        startButton.setOnAction(e -> {
            AnimationUtils.playButtonClickAnimation(startButton);
            startSimulation();
        });
        
        pauseButton = createAnimatedButton("⏸ Pausar", "pause-button");
        pauseButton.setDisable(true);
        pauseButton.setOnAction(e -> {
            AnimationUtils.playButtonClickAnimation(pauseButton);
            togglePause();
        });
        
        resetButton = createAnimatedButton("🔄 Reiniciar", "reset-button");
        resetButton.setOnAction(e -> {
            AnimationUtils.playButtonClickAnimation(resetButton);
            resetSimulation();
        });

        settingsButton = createAnimatedButton("⚙ Email", "settings-button");
        settingsButton.setOnAction(e -> {
            AnimationUtils.playButtonClickAnimation(settingsButton);
            openSmtpSettings();
        });
        
        buttonBox.getChildren().addAll(startButton, pauseButton, resetButton, settingsButton);

        panel.getChildren().addAll(titleLabel, subtitleLabel, scenarioBox, extensionsBox, speedBox, buttonBox);
        return panel;
    }
    
    /**
     * Creates a button with hover and press animations
     */
    private Button createAnimatedButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("action-button", styleClass);
        AnimationUtils.applyButtonHoverAnimation(button);
        return button;
    }

    private ScrollPane createGridPanel() {
        gridPane = new GridPane();
        gridPane.setHgap(1);
        gridPane.setVgap(1);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(12));
        gridPane.getStyleClass().add("grid-panel");
        
        gridCells = new Rectangle[DEFAULT_GRID_SIZE][DEFAULT_GRID_SIZE];
        
        for (int i = 0; i < DEFAULT_GRID_SIZE; i++) {
            for (int j = 0; j < DEFAULT_GRID_SIZE; j++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setFill(Color.web("#2E7D32")); // Forest green
                cell.setStroke(Color.web("#1B5E20"));
                cell.setStrokeWidth(0.5);
                cell.setArcWidth(4);
                cell.setArcHeight(4);
                
                // Add subtle hover effect to cells
                final int row = i;
                final int col = j;
                cell.setOnMouseEntered(e -> {
                    if (engine != null && engine.getGrid()[row][col] != CellType.EMPTY) {
                        cell.setScaleX(1.15);
                        cell.setScaleY(1.15);
                    }
                });
                cell.setOnMouseExited(e -> {
                    cell.setScaleX(1.0);
                    cell.setScaleY(1.0);
                });
                
                gridCells[i][j] = cell;
                gridPane.add(cell, j, i);
            }
        }
        
        // Wrap in a StackPane for better centering
        StackPane gridContainer = new StackPane(gridPane);
        gridContainer.setPadding(new Insets(10));
        
        ScrollPane scrollPane = new ScrollPane(gridContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("grid-scroll");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        return scrollPane;
    }

    private VBox createStatsPanel() {
        VBox panel = new VBox(16);
        panel.setPadding(new Insets(22));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPrefWidth(220);
        panel.setMinWidth(200);
        panel.getStyleClass().addAll("stats-panel", "glass-panel");

        Label statsTitle = new Label("📊 Estadísticas");
        statsTitle.getStyleClass().add("stats-title");

        turnLabel = new Label("Turno: 0");
        turnLabel.getStyleClass().add("stat-label");
        turnLabel.setStyle("-fx-font-size: 16px;");

        Separator sep1 = new Separator();
        sep1.getStyleClass().add("separator");

        // Stats with colored icons
        predatorLabel = new Label("🐺 Depredadores: 0");
        predatorLabel.getStyleClass().addAll("stat-label", "stat-predator");
        
        preyLabel = new Label("🐰 Presas: 0");
        preyLabel.getStyleClass().addAll("stat-label", "stat-prey");

        thirdSpeciesLabel = new Label("🦎 Tercer Especie: 0");
        thirdSpeciesLabel.getStyleClass().addAll("stat-label", "stat-third-species");

        mutatedLabel = new Label("🧬 Mutados: 0");
        mutatedLabel.getStyleClass().addAll("stat-label", "stat-mutated");

        Separator sep2 = new Separator();
        sep2.getStyleClass().add("separator");

        // Legend with enhanced styling
        Label legendTitle = new Label("📍 Leyenda");
        legendTitle.getStyleClass().add("legend-title");

        VBox legendBox = new VBox(10);
        legendBox.getStyleClass().add("legend-box");
        legendBox.getChildren().addAll(
            createLegendItem("Depredador", "#D32F2F", "🐺"),
            createLegendItem("Presa", "#1976D2", "🐰"),
            createLegendItem("Tercer Especie", "#FF9800", "🦎"),
            createLegendItem("Vacío", "#2E7D32", "🌿")
        );

        panel.getChildren().addAll(
            statsTitle, turnLabel, sep1,
            predatorLabel, preyLabel, thirdSpeciesLabel, mutatedLabel,
            sep2, legendTitle, legendBox
        );

        return panel;
    }

    private HBox createLegendItem(String text, String color, String emoji) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("legend-item");
        
        Rectangle colorBox = new Rectangle(18, 18);
        colorBox.setFill(Color.web(color));
        colorBox.setArcWidth(4);
        colorBox.setArcHeight(4);
        colorBox.setStroke(Color.web(color).darker());
        colorBox.setStrokeWidth(1);
        
        // Add subtle shadow to legend color boxes
        DropShadow shadow = new DropShadow();
        shadow.setRadius(3);
        shadow.setOffsetY(1);
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        colorBox.setEffect(shadow);
        
        Label label = new Label(emoji + " " + text);
        label.getStyleClass().add("legend-label");
        
        item.getChildren().addAll(colorBox, label);
        return item;
    }

    private HBox createStatusPanel() {
        HBox panel = new HBox(25);
        panel.setPadding(new Insets(16, 20, 16, 20));
        panel.setAlignment(Pos.CENTER);
        panel.getStyleClass().addAll("status-panel", "glass-panel");

        statusLabel = new Label("✨ Listo para iniciar simulación");
        statusLabel.getStyleClass().add("status-label");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(250);
        progressBar.setPrefHeight(12);
        progressBar.getStyleClass().add("progress-bar-custom");

        // Add a turn counter next to progress
        Label progressLabel = new Label("0%");
        progressLabel.getStyleClass().add("speed-value");
        progressBar.progressProperty().addListener((obs, oldVal, newVal) -> {
            int percentage = (int) (newVal.doubleValue() * 100);
            progressLabel.setText(percentage + "%");
        });

        panel.getChildren().addAll(statusLabel, progressBar, progressLabel);
        return panel;
    }

    private void initializeSimulation() {
        updateConfig();
    }

    private void updateConfig() {
        Scenario selectedScenario = scenarioComboBox.getValue();
        boolean thirdSpecies = thirdSpeciesCheckBox.isSelected();
        boolean mutations = mutationsCheckBox.isSelected();

        config = new SimulationConfig()
            .withScenario(selectedScenario)
            .withThirdSpecies(thirdSpecies)
            .withMutations(mutations)
            .withGridSize(DEFAULT_GRID_SIZE)
            .withTurnDelay((int) speedSlider.getValue());

        engine = new SimulationEngine(config);
        engine.setOnGridUpdate(this::updateGridView);
        engine.setOnStatsUpdate(this::updateStatsView);
        engine.setOnSimulationEnd(this::onSimulationEnd);

        runner = new SimulationRunner(engine);

        // Clear any existing mutation animations
        mutationAnimations.values().forEach(Timeline::stop);
        mutationAnimations.clear();

        updateGridView();
        updateStatsView();
        
        statusLabel.setText("✨ " + selectedScenario.getDisplayName() + 
            (thirdSpecies ? " + Tercer Especie" : "") +
            (mutations ? " + Mutaciones" : ""));
    }

    private void startSimulation() {
        if (runner != null) {
            if (!runner.isRunning()) {
                runner.start();
                startButton.setDisable(true);
                pauseButton.setDisable(false);
                scenarioComboBox.setDisable(true);
                thirdSpeciesCheckBox.setDisable(true);
                mutationsCheckBox.setDisable(true);
                statusLabel.setText("🔄 Simulación en progreso...");
                statusLabel.getStyleClass().add("status-label-success");
            }
        }
    }

    private void togglePause() {
        if (runner != null) {
            if (runner.isPaused()) {
                runner.resume();
                pauseButton.setText("⏸ Pausar");
                statusLabel.setText("🔄 Simulación en progreso...");
                statusLabel.getStyleClass().removeAll("status-label-warning");
                statusLabel.getStyleClass().add("status-label-success");
            } else {
                runner.pause();
                pauseButton.setText("▶ Reanudar");
                statusLabel.setText("⏸ Simulación pausada");
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-warning");
            }
        }
    }

    private void resetSimulation() {
        if (runner != null) {
            runner.reset();
        }
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        pauseButton.setText("⏸ Pausar");
        scenarioComboBox.setDisable(false);
        thirdSpeciesCheckBox.setDisable(false);
        mutationsCheckBox.setDisable(false);
        
        // Clear status classes
        statusLabel.getStyleClass().removeAll("status-label-success", "status-label-warning", "status-label-error");
        
        updateConfig();
        statusLabel.setText("🔄 Simulación reiniciada - Lista para iniciar");
        
        // Play a subtle reset animation on the grid
        playGridResetAnimation();
    }

    /**
     * Plays a ripple-like reset animation across the grid
     */
    private void playGridResetAnimation() {
        for (int i = 0; i < DEFAULT_GRID_SIZE; i++) {
            for (int j = 0; j < DEFAULT_GRID_SIZE; j++) {
                final Rectangle cell = gridCells[i][j];
                final int delay = (i + j) * 15; // Diagonal wave effect
                
                PauseTransition pause = new PauseTransition(Duration.millis(delay));
                pause.setOnFinished(e -> {
                    cell.setScaleX(0.8);
                    cell.setScaleY(0.8);
                    cell.setOpacity(0.5);
                    
                    ScaleTransition scale = new ScaleTransition(Duration.millis(200), cell);
                    scale.setToX(1.0);
                    scale.setToY(1.0);
                    scale.setInterpolator(AnimationUtils.EASE_OUT_BACK);
                    
                    FadeTransition fade = new FadeTransition(Duration.millis(200), cell);
                    fade.setToValue(1.0);
                    
                    new ParallelTransition(scale, fade).play();
                });
                pause.play();
            }
        }
    }

    private void updateGridView() {
        Platform.runLater(() -> {
            if (engine == null) return;
            
            CellType[][] grid = engine.getGrid();
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[i].length; j++) {
                    CellType cellType = grid[i][j];
                    CellType prevType = previousGrid[i][j];
                    Color color = Color.web(cellType.getColor());
                    Rectangle cell = gridCells[i][j];
                    
                    // Check if creature at this position is mutated using O(1) lookup
                    boolean isMutated = engine.isCreatureMutatedAt(i, j);
                    String posKey = i + "," + j;
                    
                    // Animate cell state changes
                    if (cellType != prevType) {
                        if (prevType == CellType.EMPTY && cellType != CellType.EMPTY) {
                            // Spawn animation
                            playCellSpawnAnimation(cell, color);
                        } else if (prevType != CellType.EMPTY && cellType == CellType.EMPTY) {
                            // Death animation
                            playCellDeathAnimation(cell);
                        } else {
                            // Type change (e.g., reproduction)
                            playReproductionEffect(cell, color);
                        }
                    } else {
                        cell.setFill(color);
                    }
                    
                    // Handle mutation glow animation
                    if (isMutated) {
                        if (!mutationAnimations.containsKey(posKey)) {
                            Glow glow = new Glow(0.6);
                            DropShadow shadow = new DropShadow();
                            shadow.setRadius(6);
                            shadow.setSpread(0.4);
                            shadow.setColor(Color.rgb(156, 39, 176, 0.8));
                            shadow.setInput(glow);
                            cell.setEffect(shadow);
                            
                            // Create pulsing animation for mutated creatures
                            Timeline pulseTimeline = new Timeline(
                                new KeyFrame(Duration.ZERO, 
                                    new KeyValue(glow.levelProperty(), 0.4)),
                                new KeyFrame(Duration.millis(600), 
                                    new KeyValue(glow.levelProperty(), 0.8)),
                                new KeyFrame(Duration.millis(1200), 
                                    new KeyValue(glow.levelProperty(), 0.4))
                            );
                            pulseTimeline.setCycleCount(Animation.INDEFINITE);
                            pulseTimeline.play();
                            mutationAnimations.put(posKey, pulseTimeline);
                        }
                    } else {
                        // Remove mutation animation if creature is no longer mutated
                        Timeline existingAnimation = mutationAnimations.remove(posKey);
                        if (existingAnimation != null) {
                            existingAnimation.stop();
                        }
                        if (prevType == cellType) {
                            cell.setEffect(null);
                        }
                    }
                    
                    previousGrid[i][j] = cellType;
                }
            }
        });
    }
    
    /**
     * Play spawn animation for a new creature
     */
    private void playCellSpawnAnimation(Rectangle cell, Color targetColor) {
        cell.setFill(targetColor);
        cell.setScaleX(0.3);
        cell.setScaleY(0.3);
        cell.setOpacity(0);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(250), cell);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(AnimationUtils.EASE_OUT_BACK);
        
        FadeTransition fade = new FadeTransition(Duration.millis(200), cell);
        fade.setToValue(1.0);
        
        new ParallelTransition(scale, fade).play();
    }
    
    /**
     * Play death animation for a creature
     */
    private void playCellDeathAnimation(Rectangle cell) {
        Color emptyColor = Color.web(CellType.EMPTY.getColor());
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), cell);
        scale.setToX(0.3);
        scale.setToY(0.3);
        
        FadeTransition fade = new FadeTransition(Duration.millis(200), cell);
        fade.setToValue(0.3);
        
        ParallelTransition death = new ParallelTransition(scale, fade);
        death.setOnFinished(e -> {
            cell.setFill(emptyColor);
            cell.setScaleX(1.0);
            cell.setScaleY(1.0);
            cell.setOpacity(1.0);
            cell.setEffect(null);
        });
        death.play();
    }
    
    /**
     * Play reproduction pulse effect
     */
    private void playReproductionEffect(Rectangle cell, Color targetColor) {
        cell.setFill(targetColor);
        
        ScaleTransition pulse = new ScaleTransition(Duration.millis(150), cell);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.25);
        pulse.setToY(1.25);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.setInterpolator(AnimationUtils.EASE_IN_OUT_CUBIC);
        pulse.play();
    }

    private void updateStatsView() {
        Platform.runLater(() -> {
            if (engine == null) return;
            
            SimulationStats stats = engine.getStats();
            
            // Animate stat updates with subtle scale effect
            animateStatLabel(turnLabel, "Turno: " + stats.getTurn());
            animateStatLabel(predatorLabel, "🐺 Depredadores: " + stats.getPredatorCount());
            animateStatLabel(preyLabel, "🐰 Presas: " + stats.getPreyCount());
            animateStatLabel(thirdSpeciesLabel, "🦎 Tercer Especie: " + stats.getThirdSpeciesCount());
            animateStatLabel(mutatedLabel, "🧬 Mutados: " + stats.getMutatedCount());
            
            // Update progress bar with smooth animation
            double progress = (double) stats.getTurn() / config.getMaxTurns();
            animateProgressBar(progress);
        });
    }
    
    /**
     * Animate stat label update with subtle pulse
     */
    private void animateStatLabel(Label label, String newText) {
        if (!label.getText().equals(newText)) {
            label.setText(newText);
            ScaleTransition pulse = new ScaleTransition(Duration.millis(100), label);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.08);
            pulse.setToY(1.08);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(2);
            pulse.play();
        }
    }
    
    /**
     * Animate progress bar smoothly
     */
    private void animateProgressBar(double targetProgress) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(200),
                new KeyValue(progressBar.progressProperty(), targetProgress, AnimationUtils.EASE_OUT_CUBIC))
        );
        timeline.play();
    }

    private void onSimulationEnd() {
        Platform.runLater(() -> {
            SimulationStats stats = engine.getStats();
            String result = stats.getWinner();
            
            // Update status with appropriate styling
            statusLabel.setText("🏆 Simulación terminada: " + result);
            statusLabel.getStyleClass().removeAll("status-label-success", "status-label-warning");
            
            startButton.setDisable(false);
            pauseButton.setDisable(true);
            scenarioComboBox.setDisable(false);
            thirdSpeciesCheckBox.setDisable(false);
            mutationsCheckBox.setDisable(false);

            // Stop all mutation animations
            mutationAnimations.values().forEach(Timeline::stop);
            mutationAnimations.clear();

            // Generate PDF report and send via email
            generateAndSendReport(stats);

            // Show end dialog with animation
            showEndDialog(stats, result);
        });
    }
    
    /**
     * Show simulation end dialog with animation
     */
    private void showEndDialog(SimulationStats stats, String result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Simulación Completada");
        alert.setHeaderText("🏆 Resultado Final");
        alert.setContentText(String.format(
            "Turno final: %d\n" +
            "Depredadores: %d\n" +
            "Presas: %d\n" +
            "Tercer Especie: %d\n" +
            "Criaturas mutadas: %d\n\n" +
            "Resultado: %s",
            stats.getTurn(),
            stats.getPredatorCount(),
            stats.getPreyCount(),
            stats.getThirdSpeciesCount(),
            stats.getMutatedCount(),
            result
        ));
        
        // Style the dialog
        alert.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/styles.css").toExternalForm()
        );
        
        alert.showAndWait();
    }

    /**
     * Generate PDF report and attempt to send it via email.
     * Shows non-blocking notification on success or failure.
     */
    private void generateAndSendReport(SimulationStats stats) {
        // Generate PDF report
        String reportFilename = PDFReportGenerator.getDefaultFilename();
        Path reportsDir = Paths.get("reports");
        
        try {
            if (!Files.exists(reportsDir)) {
                Files.createDirectories(reportsDir);
            }
            
            String reportPath = reportsDir.resolve(reportFilename).toString();
            
            // Find extinction turn if applicable
            int extinctionTurn = -1;
            if (stats.getPredatorCount() == 0 || stats.getPreyCount() == 0) {
                extinctionTurn = stats.getTurn();
            }
            
            PDFReportGenerator.generateSimpleReport(
                reportPath,
                stats.getTurn(),
                stats,
                DEFAULT_GRID_SIZE,
                extinctionTurn
            );
            
            File reportFile = new File(reportPath);
            LOGGER.info("PDF report generated: " + reportPath);
            
            // Check if user is logged in and has email
            if (Session.isLoggedIn()) {
                var currentUser = Session.getUser();
                if (currentUser != null && currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                    String userEmail = currentUser.getEmail();
                    String subject = "Eco Simulator - Simulation Report";
                    String body = "Hello " + currentUser.getName() + ",\n\n" +
                                 "Attached is your simulation report.\n\n" +
                                 "Simulation Results:\n" +
                                 "- Final Turn: " + stats.getTurn() + "\n" +
                                 "- Predators: " + stats.getPredatorCount() + "\n" +
                                 "- Prey: " + stats.getPreyCount() + "\n" +
                             "- Third Species: " + stats.getThirdSpeciesCount() + "\n" +
                             "- Result: " + stats.getWinner() + "\n\n" +
                             "Best regards,\nEco Simulator";
                
                // Attempt to send email
                boolean emailSent = emailService.sendReport(userEmail, reportFile, subject, body);
                
                // Show non-blocking notification
                showEmailNotification(emailSent, userEmail, reportPath);
                } else {
                    LOGGER.info("User not logged in or no email configured. Report saved locally only.");
                    showNotification("📄 Report Saved", 
                        "Report saved to: " + reportPath + "\n" +
                        "Login and configure email to send reports automatically.",
                        Alert.AlertType.INFORMATION);
                }
            } else {
                LOGGER.info("User not logged in. Report saved locally only.");
                showNotification("📄 Report Saved", 
                    "Report saved to: " + reportPath + "\n" +
                    "Login to enable email sending.",
                    Alert.AlertType.INFORMATION);
            }
            
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to generate PDF report", e);
            showNotification("⚠️ Report Generation Failed", 
                "Could not generate PDF report: " + e.getMessage(),
                Alert.AlertType.WARNING);
        }
    }

    /**
     * Show notification about email send status.
     */
    private void showEmailNotification(boolean success, String email, String reportPath) {
        if (success) {
            showNotification("📧 Email Sent", 
                "Report sent to: " + email + "\n" +
                "Local copy: " + reportPath,
                Alert.AlertType.INFORMATION);
        } else {
            String fallbackDir = EmailService.getFallbackDirectory();
            showNotification("📧 Email Failed", 
                "Could not send email to: " + email + "\n" +
                "Report saved locally:\n" +
                "- Original: " + reportPath + "\n" +
                "- Fallback: " + fallbackDir + "/\n\n" +
                "Check SMTP settings or try again later.",
                Alert.AlertType.WARNING);
        }
    }

    /**
     * Show a non-blocking notification alert.
     */
    private void showNotification(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show(); // Non-blocking
        });
    }

    /**
     * Open SMTP settings dialog.
     */
    private void openSmtpSettings() {
        Stage stage = (Stage) getScene().getWindow();
        SMTPSettingsController.showDialog(stage, emailService);
    }

    private void applyStyles() {
        getStyleClass().add("main-view");
        
        // Apply premium drop shadow to the grid
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(15);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(8);
        dropShadow.setSpread(0.1);
        dropShadow.setColor(Color.rgb(27, 94, 32, 0.4));
        gridPane.setEffect(dropShadow);
    }
    
    /**
     * Play entrance animations for all UI panels
     */
    private void playEntranceAnimations() {
        // Stagger the entrance of different panels
        
        // Control panel slides in from top
        controlPanel.setOpacity(0);
        controlPanel.setTranslateY(-30);
        
        PauseTransition delay1 = new PauseTransition(Duration.millis(100));
        delay1.setOnFinished(e -> {
            ParallelTransition entrance = AnimationUtils.slideUpAndFadeIn(controlPanel, AnimationUtils.DURATION_NORMAL);
            entrance.play();
        });
        delay1.play();
        
        // Stats panel slides in from right
        statsPanel.setOpacity(0);
        statsPanel.setTranslateX(50);
        
        PauseTransition delay2 = new PauseTransition(Duration.millis(200));
        delay2.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(AnimationUtils.DURATION_NORMAL, statsPanel);
            fade.setFromValue(0);
            fade.setToValue(1);
            
            TranslateTransition slide = new TranslateTransition(AnimationUtils.DURATION_NORMAL, statsPanel);
            slide.setFromX(50);
            slide.setToX(0);
            slide.setInterpolator(AnimationUtils.EASE_OUT_CUBIC);
            
            new ParallelTransition(fade, slide).play();
        });
        delay2.play();
        
        // Status panel slides in from bottom
        statusPanel.setOpacity(0);
        statusPanel.setTranslateY(30);
        
        PauseTransition delay3 = new PauseTransition(Duration.millis(300));
        delay3.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(AnimationUtils.DURATION_NORMAL, statusPanel);
            fade.setFromValue(0);
            fade.setToValue(1);
            
            TranslateTransition slide = new TranslateTransition(AnimationUtils.DURATION_NORMAL, statusPanel);
            slide.setFromY(30);
            slide.setToY(0);
            slide.setInterpolator(AnimationUtils.EASE_OUT_CUBIC);
            
            new ParallelTransition(fade, slide).play();
        });
        delay3.play();
        
        // Grid cells cascade entrance animation
        PauseTransition gridDelay = new PauseTransition(Duration.millis(350));
        gridDelay.setOnFinished(e -> playGridEntranceAnimation());
        gridDelay.play();
    }
    
    /**
     * Play a cascading entrance animation for grid cells
     */
    private void playGridEntranceAnimation() {
        for (int i = 0; i < DEFAULT_GRID_SIZE; i++) {
            for (int j = 0; j < DEFAULT_GRID_SIZE; j++) {
                final Rectangle cell = gridCells[i][j];
                final int delay = (i + j) * 8; // Diagonal wave effect
                
                cell.setOpacity(0);
                cell.setScaleX(0.5);
                cell.setScaleY(0.5);
                
                PauseTransition pause = new PauseTransition(Duration.millis(delay));
                pause.setOnFinished(event -> {
                    FadeTransition fade = new FadeTransition(Duration.millis(150), cell);
                    fade.setToValue(1.0);
                    
                    ScaleTransition scale = new ScaleTransition(Duration.millis(150), cell);
                    scale.setToX(1.0);
                    scale.setToY(1.0);
                    scale.setInterpolator(AnimationUtils.EASE_OUT_CUBIC);
                    
                    new ParallelTransition(fade, scale).play();
                });
                pause.play();
            }
        }
    }

    /**
     * Get the email service for external configuration
     */
    public EmailService getEmailService() {
        return emailService;
    }
}
