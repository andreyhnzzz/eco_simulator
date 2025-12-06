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
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.ImageView;
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
 * Main simulation view controller with premium animations and glassmorphism effects
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
    private Button themeToggleButton;
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
    
    // Grid cell containers (StackPane) for icons overlay
    private StackPane[][] gridCellContainers;
    
    // Icons displayed on grid cells
    private ImageView[][] gridCellIcons;
    
    // Track previous cell states for animations
    private CellType[][] previousGrid;
    private Map<String, Timeline> mutationAnimations = new HashMap<>();

    private static final int CELL_SIZE = 20;
    private static final int DEFAULT_GRID_SIZE = 25;

    public SimulationView() {
        this.config = new SimulationConfig().withGridSize(DEFAULT_GRID_SIZE);
        this.emailService = new EmailService();
        this.previousGrid = new CellType[DEFAULT_GRID_SIZE][DEFAULT_GRID_SIZE];
        
        // Preload icons
        IconManager.preloadIcons();
        
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
        scenarioComboBox.setTooltip(new Tooltip("Seleccione el escenario inicial"));
        
        scenarioBox.getChildren().addAll(scenarioLabel, scenarioComboBox);

        // Extensions (checkboxes) with enhanced styling
        HBox extensionsBox = new HBox(35);
        extensionsBox.setAlignment(Pos.CENTER);
        
        thirdSpeciesCheckBox = new CheckBox("Tercer Especie 🦎");
        thirdSpeciesCheckBox.getStyleClass().add("extension-checkbox");
        thirdSpeciesCheckBox.setOnAction(e -> updateConfig());
        
        mutationsCheckBox = new CheckBox("Mutaciones 🧬");
        mutationsCheckBox.getStyleClass().add("extension-checkbox");
        mutationsCheckBox.setOnAction(e -> updateConfig());
        
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

        Button compareButton = createAnimatedButton("📊 Comparar", "compare-button");
        compareButton.setOnAction(e -> {
            AnimationUtils.playButtonClickAnimation(compareButton);
            showScenarioComparison();
        });

        settingsButton = createAnimatedButton("⚙ Email", "settings-button");
        settingsButton.setOnAction(e -> {
            AnimationUtils.playButtonClickAnimation(settingsButton);
            openSmtpSettings();
        });
        
        buttonBox.getChildren().addAll(startButton, pauseButton, resetButton, compareButton, settingsButton);

        // Theme toggle button
        themeToggleButton = new Button(ThemeManager.getThemeToggleText());
        themeToggleButton.getStyleClass().add("theme-toggle-button");
        themeToggleButton.setOnAction(e -> {
            AnimationUtils.playButtonClickAnimation(themeToggleButton);
            toggleTheme();
        });
        AnimationUtils.applyButtonHoverAnimation(themeToggleButton);

        // Top row with title and theme toggle
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER);
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        
        topRow.getChildren().addAll(spacer1, titleBox, spacer2, themeToggleButton);

        panel.getChildren().addAll(topRow, scenarioBox, extensionsBox, speedBox, buttonBox);
        return panel;
    }
    
    private void toggleTheme() {
        if (getScene() != null) {
            ThemeManager.playEnhancedThemeTransition(this, () -> {
                ThemeManager.toggleTheme(getScene(), null);
                themeToggleButton.setText(ThemeManager.getThemeToggleText());
                updateGridColors();
            });
        }
    }
    
    private void updateGridColors() {
        // Update grid cell colors based on current theme
        if (engine != null) {
            CellType[][] grid = engine.getGrid();
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[i].length; j++) {
                    CellType cellType = grid[i][j];
                    String colorStr = ThemeManager.isDarkMode() ? 
                        getDarkModeColor(cellType) : cellType.getColor();
                    gridCells[i][j].setFill(Color.web(colorStr));
                }
            }
        }
    }
    
    private String getDarkModeColor(CellType cellType) {
        switch (cellType) {
            case PREDATOR:
                return "#FF5252";
            case PREY:
                return "#448AFF";
            case THIRD_SPECIES:
                return "#FFAB40";
            case EMPTY:
            default:
                return "#1B263B";
        }
    }
    
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
        gridCellContainers = new StackPane[DEFAULT_GRID_SIZE][DEFAULT_GRID_SIZE];
        gridCellIcons = new ImageView[DEFAULT_GRID_SIZE][DEFAULT_GRID_SIZE];
        
        for (int i = 0; i < DEFAULT_GRID_SIZE; i++) {
            for (int j = 0; j < DEFAULT_GRID_SIZE; j++) {
                // Create cell background
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setFill(Color.web("#2E7D32")); // Forest green
                cell.setStroke(Color.web("#1B5E20"));
                cell.setStrokeWidth(0.5);
                cell.setArcWidth(4);
                cell.setArcHeight(4);
                gridCells[i][j] = cell;
                
                // Create container for cell + icon overlay
                StackPane cellContainer = new StackPane();
                cellContainer.getChildren().add(cell);
                cellContainer.setAlignment(Pos.CENTER);
                cellContainer.setMinSize(CELL_SIZE, CELL_SIZE);
                cellContainer.setMaxSize(CELL_SIZE, CELL_SIZE);
                gridCellContainers[i][j] = cellContainer;
                
                gridPane.add(cellContainer, j, i);
            }
        }
        
        StackPane gridContainer = new StackPane(gridPane);
        gridContainer.setPadding(new Insets(10));
        
        ScrollPane scrollPane = new ScrollPane(gridContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("grid-scroll");
        
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

        predatorLabel = new Label("🐺 Depredadores: 0");
        predatorLabel.getStyleClass().addAll("stat-label", "stat-predator");
        
        preyLabel = new Label("🐰 Presas: 0");
        preyLabel.getStyleClass().addAll("stat-label", "stat-prey");

        thirdSpeciesLabel = new Label("🦎 Tercer Especie: 0");
        thirdSpeciesLabel.getStyleClass().addAll("stat-label", "stat-third-species");

        mutatedLabel = new Label("🧬 Mutados: 0");
        mutatedLabel.getStyleClass().addAll("stat-label", "stat-mutated");

        Separator sep2 = new Separator();

        Label legendTitle = new Label("📍 Leyenda");
        legendTitle.getStyleClass().add("legend-title");

        VBox legendBox = new VBox(10);
        legendBox.getChildren().addAll(
            createLegendItemWithIcon(IconManager.PREDATOR, "Depredador", "#D32F2F"),
            createLegendItemWithIcon(IconManager.PREY, "Presa", "#1976D2"),
            createLegendItemWithIcon(IconManager.SCAVENGER, "Tercer Especie", "#FF9800"),
            createLegendItemWithIcon(IconManager.TERRAIN, "Vacío", "#2E7D32"),
            createLegendItemWithIcon(IconManager.MUTATION, "Mutación", "#9C27B0")
        );

        panel.getChildren().addAll(
            statsTitle, turnLabel, sep1,
            predatorLabel, preyLabel, thirdSpeciesLabel, mutatedLabel,
            sep2, legendTitle, legendBox
        );

        return panel;
    }

    private HBox createLegendItemWithIcon(String iconName, String text, String color) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        
        // Try to get icon, fallback to colored rectangle
        ImageView icon = IconManager.getIconView(iconName, 20);
        if (icon != null) {
            item.getChildren().add(icon);
        } else {
            Rectangle colorBox = new Rectangle(18, 18);
            colorBox.setFill(Color.web(color));
            colorBox.setArcWidth(4);
            colorBox.setArcHeight(4);
            colorBox.setStroke(Color.web(color).darker());
            colorBox.setStrokeWidth(1);
            
            DropShadow shadow = new DropShadow();
            shadow.setRadius(3);
            shadow.setOffsetY(1);
            shadow.setColor(Color.rgb(0, 0, 0, 0.2));
            colorBox.setEffect(shadow);
            
            item.getChildren().add(colorBox);
        }
        
        Label label = new Label(text);
        label.getStyleClass().add("legend-label");
        label.setStyle("-fx-text-fill: " + color + ";");
        
        item.getChildren().add(label);
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

        stopMutationAnimations();
        updateGridView();
        updateStatsView();
        
        statusLabel.setText("✨ " + selectedScenario.getDisplayName() + 
            (thirdSpecies ? " + Tercer Especie" : "") +
            (mutations ? " + Mutaciones" : ""));
    }
    
    private void stopMutationAnimations() {
        mutationAnimations.values().forEach(Timeline::stop);
        mutationAnimations.clear();
    }
    
    /**
     * Clear all icons from the grid
     */
    private void clearAllGridIcons() {
        for (int i = 0; i < DEFAULT_GRID_SIZE; i++) {
            for (int j = 0; j < DEFAULT_GRID_SIZE; j++) {
                ImageView icon = gridCellIcons[i][j];
                if (icon != null) {
                    gridCellContainers[i][j].getChildren().remove(icon);
                    gridCellIcons[i][j] = null;
                }
            }
        }
    }

    private void startSimulation() {
        if (runner != null && !runner.isRunning()) {
            runner.start();
            startButton.setDisable(true);
            pauseButton.setDisable(false);
            scenarioComboBox.setDisable(true);
            thirdSpeciesCheckBox.setDisable(true);
            mutationsCheckBox.setDisable(true);
            statusLabel.setText("🔄 Simulación en progreso...");
        }
    }

    private void togglePause() {
        if (runner != null) {
            if (runner.isPaused()) {
                runner.resume();
                pauseButton.setText("⏸ Pausar");
                statusLabel.setText("🔄 Simulación en progreso...");
            } else {
                runner.pause();
                pauseButton.setText("▶ Reanudar");
                statusLabel.setText("⏸ Simulación pausada");
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
        clearAllGridIcons();
        updateConfig();
        statusLabel.setText("🔄 Simulación reiniciada - Lista para iniciar");
        playGridResetAnimation();
    }
    
    private void playGridResetAnimation() {
        for (int i = 0; i < DEFAULT_GRID_SIZE; i++) {
            for (int j = 0; j < DEFAULT_GRID_SIZE; j++) {
                final Rectangle cell = gridCells[i][j];
                final int delay = (i + j) * 10;
                
                PauseTransition pause = new PauseTransition(Duration.millis(delay));
                pause.setOnFinished(e -> {
                    cell.setScaleX(0.8);
                    cell.setScaleY(0.8);
                    ScaleTransition scale = new ScaleTransition(Duration.millis(150), cell);
                    scale.setToX(1.0);
                    scale.setToY(1.0);
                    scale.setInterpolator(AnimationUtils.EASE_OUT_BACK);
                    scale.play();
                });
                pause.play();
            }
        }
    }

    private void updateGridView() {
        Platform.runLater(() -> {
            if (engine == null) return;
            
            // Cache theme mode to avoid repeated calls in the loop
            boolean isDarkMode = ThemeManager.isDarkMode();
            
            CellType[][] grid = engine.getGrid();
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[i].length; j++) {
                    CellType cellType = grid[i][j];
                    CellType prevType = previousGrid[i][j];
                    String colorStr = isDarkMode ? 
                        getDarkModeColor(cellType) : cellType.getColor();
                    Color color = Color.web(colorStr);
                    Rectangle cell = gridCells[i][j];
                    StackPane container = gridCellContainers[i][j];
                    
                    boolean isMutated = engine.isCreatureMutatedAt(i, j);
                    String posKey = i + "," + j;
                    
                    // Animate cell state changes
                    if (cellType != prevType) {
                        if (prevType == CellType.EMPTY && cellType != CellType.EMPTY) {
                            playCellSpawnAnimation(cell, color);
                        } else if (prevType != CellType.EMPTY && cellType == CellType.EMPTY) {
                            playCellDeathAnimation(cell);
                        } else {
                            playReproductionEffect(cell, color);
                        }
                        
                        // Update icon when cell type changes (only if icon type differs)
                        String prevIconName = getIconNameForCellType(prevType);
                        String newIconName = getIconNameForCellType(cellType);
                        if (!java.util.Objects.equals(prevIconName, newIconName)) {
                            updateCellIcon(i, j, cellType, container);
                        }
                    } else {
                        cell.setFill(color);
                    }
                    
                    // Handle mutation glow
                    if (isMutated && !mutationAnimations.containsKey(posKey)) {
                        Glow glow = new Glow(0.6);
                        DropShadow shadow = new DropShadow();
                        shadow.setRadius(6);
                        shadow.setSpread(0.4);
                        shadow.setColor(Color.rgb(156, 39, 176, 0.8));
                        shadow.setInput(glow);
                        cell.setEffect(shadow);
                        
                        Timeline pulseTimeline = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.4)),
                            new KeyFrame(Duration.millis(600), new KeyValue(glow.levelProperty(), 0.8)),
                            new KeyFrame(Duration.millis(1200), new KeyValue(glow.levelProperty(), 0.4))
                        );
                        pulseTimeline.setCycleCount(Animation.INDEFINITE);
                        pulseTimeline.play();
                        mutationAnimations.put(posKey, pulseTimeline);
                    } else if (!isMutated) {
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
     * Updates the icon displayed on a grid cell based on cell type
     */
    private void updateCellIcon(int row, int col, CellType cellType, StackPane container) {
        // Remove existing icon if present
        ImageView existingIcon = gridCellIcons[row][col];
        if (existingIcon != null) {
            container.getChildren().remove(existingIcon);
            gridCellIcons[row][col] = null;
        }
        
        // Get appropriate icon for cell type
        String iconName = getIconNameForCellType(cellType);
        if (iconName != null) {
            ImageView icon = IconManager.getIconView(iconName, CELL_SIZE - 4);
            if (icon != null) {
                icon.setPreserveRatio(true);
                icon.setSmooth(true);
                gridCellIcons[row][col] = icon;
                container.getChildren().add(icon);
            }
        }
    }
    
    /**
     * Returns the icon name for a given cell type
     */
    private String getIconNameForCellType(CellType cellType) {
        switch (cellType) {
            case PREDATOR:
                return IconManager.PREDATOR;
            case PREY:
                return IconManager.PREY;
            case THIRD_SPECIES:
                return IconManager.SCAVENGER;
            case EMPTY:
            default:
                return null; // No icon for empty cells
        }
    }
    
    private void playCellSpawnAnimation(Rectangle cell, Color targetColor) {
        cell.setFill(targetColor);
        cell.setScaleX(0.3);
        cell.setScaleY(0.3);
        cell.setOpacity(0);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), cell);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(AnimationUtils.EASE_OUT_BACK);
        
        FadeTransition fade = new FadeTransition(Duration.millis(150), cell);
        fade.setToValue(1.0);
        
        new ParallelTransition(scale, fade).play();
    }
    
    private void playCellDeathAnimation(Rectangle cell) {
        Color emptyColor = Color.web(CellType.EMPTY.getColor());
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), cell);
        scale.setToX(0.3);
        scale.setToY(0.3);
        
        FadeTransition fade = new FadeTransition(Duration.millis(150), cell);
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
    
    private void playReproductionEffect(Rectangle cell, Color targetColor) {
        cell.setFill(targetColor);
        ScaleTransition pulse = new ScaleTransition(Duration.millis(100), cell);
        pulse.setToX(1.2);
        pulse.setToY(1.2);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();
    }

    private void updateStatsView() {
        Platform.runLater(() -> {
            if (engine == null) return;
            
            SimulationStats stats = engine.getStats();
            animateStatLabel(turnLabel, "Turno: " + stats.getTurn());
            animateStatLabel(predatorLabel, "🐺 Depredadores: " + stats.getPredatorCount());
            animateStatLabel(preyLabel, "🐰 Presas: " + stats.getPreyCount());
            animateStatLabel(thirdSpeciesLabel, "🦎 Tercer Especie: " + stats.getThirdSpeciesCount());
            animateStatLabel(mutatedLabel, "🧬 Mutados: " + stats.getMutatedCount());
            
            double progress = (double) stats.getTurn() / config.getMaxTurns();
            animateProgressBar(progress);
        });
    }
    
    private void animateStatLabel(Label label, String newText) {
        if (!label.getText().equals(newText)) {
            label.setText(newText);
            ScaleTransition pulse = new ScaleTransition(Duration.millis(80), label);
            pulse.setToX(1.05);
            pulse.setToY(1.05);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(2);
            pulse.play();
        }
    }
    
    private void animateProgressBar(double targetProgress) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(150),
                new KeyValue(progressBar.progressProperty(), targetProgress, AnimationUtils.EASE_OUT_CUBIC))
        );
        timeline.play();
    }

    private void onSimulationEnd() {
        Platform.runLater(() -> {
            SimulationStats stats = engine.getStats();
            String result = stats.getWinner();
            
            statusLabel.setText("🏆 Simulación terminada: " + result);
            startButton.setDisable(false);
            pauseButton.setDisable(true);
            scenarioComboBox.setDisable(false);
            thirdSpeciesCheckBox.setDisable(false);
            mutationsCheckBox.setDisable(false);

            stopMutationAnimations();
            
            // Get extinction turn from engine
            int extinctionTurn = engine.getExtinctionTurn();
            
            // Show premium results screen
            if (getScene() != null && getScene().getWindow() instanceof Stage) {
                Stage owner = (Stage) getScene().getWindow();
                ResultsScreen.showResultsDialog(owner, stats, DEFAULT_GRID_SIZE, extinctionTurn, emailService);
            }
            
            // Also generate and send report in background
            generateAndSendReport(stats);
        });
    }

    private void generateAndSendReport(SimulationStats stats) {
        String reportFilename = PDFReportGenerator.getDefaultFilename();
        Path reportsDir = Paths.get("reports");
        
        try {
            if (!Files.exists(reportsDir)) {
                Files.createDirectories(reportsDir);
            }
            
            String reportPath = reportsDir.resolve(reportFilename).toString();
            int extinctionTurn = (stats.getPredatorCount() == 0 || stats.getPreyCount() == 0) ? stats.getTurn() : -1;
            
            PDFReportGenerator.generateSimpleReport(reportPath, stats.getTurn(), stats, DEFAULT_GRID_SIZE, extinctionTurn);
            
            File reportFile = new File(reportPath);
            LOGGER.info("PDF report generated: " + reportPath);
            
            if (Session.isLoggedIn()) {
                var currentUser = Session.getUser();
                if (currentUser != null && currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                    String userEmail = currentUser.getEmail();
                    String subject = "Eco Simulator - Simulation Report";
                    String body = "Hello " + currentUser.getName() + ",\n\nAttached is your simulation report.\n\nBest regards,\nEco Simulator";
                    
                    boolean emailSent = emailService.sendReport(userEmail, reportFile, subject, body);
                    showEmailNotification(emailSent, userEmail, reportPath);
                } else {
                    showNotification("📄 Report Saved", "Report saved to: " + reportPath, Alert.AlertType.INFORMATION);
                }
            } else {
                showNotification("📄 Report Saved", "Report saved to: " + reportPath, Alert.AlertType.INFORMATION);
            }
            
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to generate PDF report", e);
            showNotification("⚠️ Report Generation Failed", "Could not generate PDF report: " + e.getMessage(), Alert.AlertType.WARNING);
        }
    }

    private void showEmailNotification(boolean success, String email, String reportPath) {
        if (success) {
            showNotification("📧 Email Sent", "Report sent to: " + email + "\nLocal copy: " + reportPath, Alert.AlertType.INFORMATION);
        } else {
            showNotification("📧 Email Failed", "Could not send email. Report saved locally.", Alert.AlertType.WARNING);
        }
    }

    private void showNotification(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }

    private void openSmtpSettings() {
        Stage stage = (Stage) getScene().getWindow();
        SMTPSettingsController.showDialog(stage, emailService);
    }

    /**
     * Show the Scenario Comparison dialog with analysis of all scenarios
     */
    private void showScenarioComparison() {
        if (runner != null && runner.isRunning()) {
            showNotification("Simulation Running", 
                "Please stop the current simulation before running comparison.", 
                Alert.AlertType.WARNING);
            return;
        }

        statusLabel.setText("⏳ Running scenario comparison...");
        
        // Run comparison in background thread to avoid UI freeze
        new Thread(() -> {
            try {
                // Run comparison simulations (50 turns each, 15x15 grid for speed)
                ScenarioComparison.ComparisonAnalysis analysis = 
                    ScenarioComparison.runComparisonSimulations(15, 50);
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    statusLabel.setText("✅ Comparison complete");
                    showComparisonResults(analysis);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Comparison failed");
                    showNotification("Error", "Failed to run comparison: " + e.getMessage(), 
                        Alert.AlertType.ERROR);
                });
                LOGGER.log(Level.WARNING, "Scenario comparison failed", e);
            }
        }).start();
    }

    /**
     * Display the scenario comparison results in a dialog
     */
    private void showComparisonResults(ScenarioComparison.ComparisonAnalysis analysis) {
        Stage dialog = new Stage();
        dialog.initOwner((Stage) getScene().getWindow());
        dialog.setTitle("📊 Scenario Comparison Results");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        // Title
        Label titleLabel = new Label("🔬 Scenario Comparison Analysis");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Summary table
        Label tableTitle = new Label("📋 Results Summary");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextArea summaryTable = new TextArea(ScenarioComparison.generateSummaryTable(analysis));
        summaryTable.setEditable(false);
        summaryTable.setPrefRowCount(15);
        summaryTable.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");

        // Detailed analysis
        Label analysisTitle = new Label("📈 Detailed Analysis");
        analysisTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextArea analysisArea = new TextArea(analysis.getAnalysisReport());
        analysisArea.setEditable(false);
        analysisArea.setPrefRowCount(20);
        analysisArea.setWrapText(true);
        analysisArea.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        // Key findings
        VBox findingsBox = new VBox(8);
        findingsBox.setAlignment(Pos.CENTER_LEFT);
        Label findingsTitle = new Label("🏆 Key Findings");
        findingsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        if (analysis.getMostStable() != null) {
            Label mostStable = new Label("• Most Stable: " + analysis.getMostStable().getConfigDescription());
            mostStable.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 13px;");
            findingsBox.getChildren().add(mostStable);
        }
        if (analysis.getHighestOccupancy() != null) {
            Label highest = new Label("• Highest Occupancy: " + analysis.getHighestOccupancy().getConfigDescription() +
                " (" + analysis.getHighestOccupancy().getTotalOccupancy() + " creatures)");
            highest.setStyle("-fx-text-fill: #2196F3; -fx-font-size: 13px;");
            findingsBox.getChildren().add(highest);
        }
        if (analysis.getFastestExtinction() != null) {
            Label fastest = new Label("• Fastest Extinction: " + analysis.getFastestExtinction().getConfigDescription() +
                " (turn " + analysis.getFastestExtinction().getExtinctionTurn() + ")");
            fastest.setStyle("-fx-text-fill: #F44336; -fx-font-size: 13px;");
            findingsBox.getChildren().add(fastest);
        }

        // Close button
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialog.close());
        closeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 8 20;");

        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox(15, titleLabel, findingsTitle, findingsBox, 
                                new Separator(), tableTitle, summaryTable,
                                new Separator(), analysisTitle, analysisArea);
        content.setPadding(new Insets(10));
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);

        root.getChildren().addAll(scrollPane, closeButton);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 700, 600);
        ThemeManager.applyCurrentTheme(scene);
        dialog.setScene(scene);
        dialog.show();
    }

    private void applyStyles() {
        getStyleClass().add("main-view");
        
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(15);
        dropShadow.setOffsetY(8);
        dropShadow.setSpread(0.1);
        dropShadow.setColor(Color.rgb(27, 94, 32, 0.4));
        gridPane.setEffect(dropShadow);
    }
    
    private void playEntranceAnimations() {
        controlPanel.setOpacity(0);
        controlPanel.setTranslateY(-30);
        
        PauseTransition delay1 = new PauseTransition(Duration.millis(100));
        delay1.setOnFinished(e -> AnimationUtils.slideUpAndFadeIn(controlPanel, AnimationUtils.DURATION_NORMAL).play());
        delay1.play();
        
        statsPanel.setOpacity(0);
        statsPanel.setTranslateX(50);
        
        PauseTransition delay2 = new PauseTransition(Duration.millis(200));
        delay2.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(AnimationUtils.DURATION_NORMAL, statsPanel);
            fade.setToValue(1);
            TranslateTransition slide = new TranslateTransition(AnimationUtils.DURATION_NORMAL, statsPanel);
            slide.setToX(0);
            slide.setInterpolator(AnimationUtils.EASE_OUT_CUBIC);
            new ParallelTransition(fade, slide).play();
        });
        delay2.play();
        
        statusPanel.setOpacity(0);
        statusPanel.setTranslateY(30);
        
        PauseTransition delay3 = new PauseTransition(Duration.millis(300));
        delay3.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(AnimationUtils.DURATION_NORMAL, statusPanel);
            fade.setToValue(1);
            TranslateTransition slide = new TranslateTransition(AnimationUtils.DURATION_NORMAL, statusPanel);
            slide.setToY(0);
            slide.setInterpolator(AnimationUtils.EASE_OUT_CUBIC);
            new ParallelTransition(fade, slide).play();
        });
        delay3.play();
        
        PauseTransition gridDelay = new PauseTransition(Duration.millis(350));
        gridDelay.setOnFinished(e -> playGridEntranceAnimation());
        gridDelay.play();
    }
    
    private void playGridEntranceAnimation() {
        for (int i = 0; i < DEFAULT_GRID_SIZE; i++) {
            for (int j = 0; j < DEFAULT_GRID_SIZE; j++) {
                final Rectangle cell = gridCells[i][j];
                final int delay = (i + j) * 6;
                
                cell.setOpacity(0);
                cell.setScaleX(0.5);
                cell.setScaleY(0.5);
                
                PauseTransition pause = new PauseTransition(Duration.millis(delay));
                pause.setOnFinished(event -> {
                    FadeTransition fade = new FadeTransition(Duration.millis(100), cell);
                    fade.setToValue(1.0);
                    ScaleTransition scale = new ScaleTransition(Duration.millis(100), cell);
                    scale.setToX(1.0);
                    scale.setToY(1.0);
                    scale.setInterpolator(AnimationUtils.EASE_OUT_CUBIC);
                    new ParallelTransition(fade, scale).play();
                });
                pause.play();
            }
        }
    }

    public EmailService getEmailService() {
        return emailService;
    }
}
