package com.ecosimulator.ui;

import com.ecosimulator.model.*;
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
import javafx.animation.*;
import javafx.util.Duration;

/**
 * Main simulation view controller
 */
public class SimulationView extends BorderPane {
    // UI Components
    private GridPane gridPane;
    private ComboBox<Scenario> scenarioComboBox;
    private CheckBox thirdSpeciesCheckBox;
    private CheckBox mutationsCheckBox;
    private Slider speedSlider;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
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

    private static final int CELL_SIZE = 20;
    private static final int DEFAULT_GRID_SIZE = 25;

    public SimulationView() {
        this.config = new SimulationConfig().withGridSize(DEFAULT_GRID_SIZE);
        this.emailService = new EmailService();
        
        initializeUI();
        initializeSimulation();
        applyStyles();
    }

    private void initializeUI() {
        setPadding(new Insets(20));
        
        // Top - Controls
        setTop(createControlPanel());
        
        // Center - Simulation Grid
        setCenter(createGridPanel());
        
        // Right - Statistics
        setRight(createStatsPanel());
        
        // Bottom - Status
        setBottom(createStatusPanel());
    }

    private VBox createControlPanel() {
        VBox controlPanel = new VBox(15);
        controlPanel.setPadding(new Insets(10, 10, 20, 10));
        controlPanel.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label("🌿 Eco Simulator 🌿");
        titleLabel.getStyleClass().add("title-label");

        // Scenario selection
        HBox scenarioBox = new HBox(10);
        scenarioBox.setAlignment(Pos.CENTER);
        
        Label scenarioLabel = new Label("Escenario:");
        scenarioLabel.getStyleClass().add("control-label");
        
        scenarioComboBox = new ComboBox<>();
        scenarioComboBox.getItems().addAll(Scenario.values());
        scenarioComboBox.setValue(Scenario.BALANCED);
        scenarioComboBox.getStyleClass().add("combo-box-custom");
        scenarioComboBox.setOnAction(e -> updateConfig());
        
        scenarioBox.getChildren().addAll(scenarioLabel, scenarioComboBox);

        // Extensions (checkboxes)
        HBox extensionsBox = new HBox(30);
        extensionsBox.setAlignment(Pos.CENTER);
        
        thirdSpeciesCheckBox = new CheckBox("Tercer Especie 🦎");
        thirdSpeciesCheckBox.getStyleClass().add("extension-checkbox");
        thirdSpeciesCheckBox.setOnAction(e -> updateConfig());
        
        mutationsCheckBox = new CheckBox("Mutaciones 🧬");
        mutationsCheckBox.getStyleClass().add("extension-checkbox");
        mutationsCheckBox.setOnAction(e -> updateConfig());
        
        extensionsBox.getChildren().addAll(thirdSpeciesCheckBox, mutationsCheckBox);

        // Speed control
        HBox speedBox = new HBox(10);
        speedBox.setAlignment(Pos.CENTER);
        
        Label speedLabel = new Label("Velocidad:");
        speedLabel.getStyleClass().add("control-label");
        
        speedSlider = new Slider(100, 2000, 500);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(500);
        speedSlider.setPrefWidth(200);
        speedSlider.getStyleClass().add("speed-slider");
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (runner != null) {
                runner.setTurnDelay(newVal.intValue());
            }
        });
        
        Label speedValueLabel = new Label("500ms");
        speedValueLabel.getStyleClass().add("speed-value");
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            speedValueLabel.setText(newVal.intValue() + "ms"));
        
        speedBox.getChildren().addAll(speedLabel, speedSlider, speedValueLabel);

        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        startButton = new Button("▶ Iniciar");
        startButton.getStyleClass().add("action-button");
        startButton.getStyleClass().add("start-button");
        startButton.setOnAction(e -> startSimulation());
        
        pauseButton = new Button("⏸ Pausar");
        pauseButton.getStyleClass().add("action-button");
        pauseButton.getStyleClass().add("pause-button");
        pauseButton.setDisable(true);
        pauseButton.setOnAction(e -> togglePause());
        
        resetButton = new Button("🔄 Reiniciar");
        resetButton.getStyleClass().add("action-button");
        resetButton.getStyleClass().add("reset-button");
        resetButton.setOnAction(e -> resetSimulation());
        
        buttonBox.getChildren().addAll(startButton, pauseButton, resetButton);

        controlPanel.getChildren().addAll(titleLabel, scenarioBox, extensionsBox, speedBox, buttonBox);
        return controlPanel;
    }

    private ScrollPane createGridPanel() {
        gridPane = new GridPane();
        gridPane.setHgap(1);
        gridPane.setVgap(1);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setPadding(new Insets(10));
        gridPane.getStyleClass().add("grid-panel");
        
        gridCells = new Rectangle[DEFAULT_GRID_SIZE][DEFAULT_GRID_SIZE];
        
        for (int i = 0; i < DEFAULT_GRID_SIZE; i++) {
            for (int j = 0; j < DEFAULT_GRID_SIZE; j++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setFill(Color.web("#2E7D32")); // Forest green
                cell.setStroke(Color.web("#1B5E20"));
                cell.setStrokeWidth(0.5);
                cell.setArcWidth(3);
                cell.setArcHeight(3);
                gridCells[i][j] = cell;
                gridPane.add(cell, j, i);
            }
        }
        
        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("grid-scroll");
        
        return scrollPane;
    }

    private VBox createStatsPanel() {
        VBox statsPanel = new VBox(15);
        statsPanel.setPadding(new Insets(20));
        statsPanel.setAlignment(Pos.TOP_CENTER);
        statsPanel.setPrefWidth(200);
        statsPanel.getStyleClass().add("stats-panel");

        Label statsTitle = new Label("📊 Estadísticas");
        statsTitle.getStyleClass().add("stats-title");

        turnLabel = new Label("Turno: 0");
        turnLabel.getStyleClass().add("stat-label");

        Separator sep1 = new Separator();

        predatorLabel = new Label("🐺 Depredadores: 0");
        predatorLabel.getStyleClass().add("stat-label");
        predatorLabel.setStyle("-fx-text-fill: #D32F2F;");

        preyLabel = new Label("🐰 Presas: 0");
        preyLabel.getStyleClass().add("stat-label");
        preyLabel.setStyle("-fx-text-fill: #1976D2;");

        thirdSpeciesLabel = new Label("🦎 Tercer Especie: 0");
        thirdSpeciesLabel.getStyleClass().add("stat-label");
        thirdSpeciesLabel.setStyle("-fx-text-fill: #FF9800;");

        mutatedLabel = new Label("🧬 Mutados: 0");
        mutatedLabel.getStyleClass().add("stat-label");
        mutatedLabel.setStyle("-fx-text-fill: #9C27B0;");

        Separator sep2 = new Separator();

        // Legend
        Label legendTitle = new Label("📍 Leyenda");
        legendTitle.getStyleClass().add("legend-title");

        VBox legendBox = new VBox(8);
        legendBox.getChildren().addAll(
            createLegendItem("🐺 Depredador", "#D32F2F"),
            createLegendItem("🐰 Presa", "#1976D2"),
            createLegendItem("🦎 Tercer Especie", "#FF9800"),
            createLegendItem("🌿 Vacío", "#2E7D32")
        );

        statsPanel.getChildren().addAll(
            statsTitle, turnLabel, sep1,
            predatorLabel, preyLabel, thirdSpeciesLabel, mutatedLabel,
            sep2, legendTitle, legendBox
        );

        return statsPanel;
    }

    private HBox createLegendItem(String text, String color) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Rectangle colorBox = new Rectangle(15, 15);
        colorBox.setFill(Color.web(color));
        colorBox.setArcWidth(3);
        colorBox.setArcHeight(3);
        
        Label label = new Label(text);
        label.getStyleClass().add("legend-label");
        
        item.getChildren().addAll(colorBox, label);
        return item;
    }

    private HBox createStatusPanel() {
        HBox statusPanel = new HBox(20);
        statusPanel.setPadding(new Insets(15));
        statusPanel.setAlignment(Pos.CENTER);
        statusPanel.getStyleClass().add("status-panel");

        statusLabel = new Label("Listo para iniciar simulación");
        statusLabel.getStyleClass().add("status-label");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.getStyleClass().add("progress-bar-custom");

        statusPanel.getChildren().addAll(statusLabel, progressBar);
        return statusPanel;
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

        updateGridView();
        updateStatsView();
        
        statusLabel.setText("Configuración: " + selectedScenario.getDisplayName() + 
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
                statusLabel.setText("Simulación en progreso...");
                animateButton(startButton);
            }
        }
    }

    private void togglePause() {
        if (runner != null) {
            if (runner.isPaused()) {
                runner.resume();
                pauseButton.setText("⏸ Pausar");
                statusLabel.setText("Simulación en progreso...");
            } else {
                runner.pause();
                pauseButton.setText("▶ Reanudar");
                statusLabel.setText("Simulación pausada");
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
        updateConfig();
        statusLabel.setText("Simulación reiniciada - Lista para iniciar");
    }

    private void updateGridView() {
        Platform.runLater(() -> {
            if (engine == null) return;
            
            CellType[][] grid = engine.getGrid();
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[i].length; j++) {
                    CellType cellType = grid[i][j];
                    Color color = Color.web(cellType.getColor());
                    
                    // Check if creature at this position is mutated using O(1) lookup
                    boolean isMutated = engine.isCreatureMutatedAt(i, j);
                    
                    gridCells[i][j].setFill(color);
                    
                    // Add glow effect for mutated creatures
                    if (isMutated) {
                        Glow glow = new Glow(0.8);
                        gridCells[i][j].setEffect(glow);
                    } else {
                        gridCells[i][j].setEffect(null);
                    }
                }
            }
        });
    }

    private void updateStatsView() {
        Platform.runLater(() -> {
            if (engine == null) return;
            
            SimulationStats stats = engine.getStats();
            turnLabel.setText("Turno: " + stats.getTurn());
            predatorLabel.setText("🐺 Depredadores: " + stats.getPredatorCount());
            preyLabel.setText("🐰 Presas: " + stats.getPreyCount());
            thirdSpeciesLabel.setText("🦎 Tercer Especie: " + stats.getThirdSpeciesCount());
            mutatedLabel.setText("🧬 Mutados: " + stats.getMutatedCount());
            
            // Update progress bar
            double progress = (double) stats.getTurn() / config.getMaxTurns();
            progressBar.setProgress(progress);
        });
    }

    private void onSimulationEnd() {
        Platform.runLater(() -> {
            SimulationStats stats = engine.getStats();
            String result = stats.getWinner();
            
            statusLabel.setText("Simulación terminada: " + result);
            startButton.setDisable(false);
            pauseButton.setDisable(true);
            scenarioComboBox.setDisable(false);
            thirdSpeciesCheckBox.setDisable(false);
            mutationsCheckBox.setDisable(false);

            // Show end dialog
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
            alert.showAndWait();
        });
    }

    private void animateButton(Button button) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
        scale.setToX(1.1);
        scale.setToY(1.1);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

    private void applyStyles() {
        getStyleClass().add("main-view");
        
        // Apply drop shadow to the grid
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10);
        dropShadow.setOffsetX(3);
        dropShadow.setOffsetY(3);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));
        gridPane.setEffect(dropShadow);
    }

    /**
     * Get the email service for external configuration
     */
    public EmailService getEmailService() {
        return emailService;
    }
}
