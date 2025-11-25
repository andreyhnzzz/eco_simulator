package com.ecosimulator.ui;

import com.ecosimulator.core.*;
import com.ecosimulator.simulation.MotorDeSimulacion;
import com.ecosimulator.simulation.SimulationObserver;
import com.ecosimulator.simulation.Escenario;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * View for displaying the simulation in real-time.
 * Subscribes to MotorDeSimulacion events for updates.
 */
public class SimulacionView implements SimulationObserver {
    
    private final MainController controller;
    private final Scene scene;
    
    private GridPane gridPane;
    private Label turnoLabel;
    private Label presasLabel;
    private Label depredadoresLabel;
    private Label carroñerosLabel;
    private Label ocupacionLabel;
    private Label statusLabel;
    private Button playButton;
    private Button stepButton;
    private Slider speedSlider;
    
    private ScheduledExecutorService executor;
    private boolean isPlaying;
    private static final int CELL_SIZE = 40;

    public SimulacionView(MainController controller) {
        this.controller = controller;
        this.scene = createScene();
        this.isPlaying = false;
        
        // Register as observer
        MotorDeSimulacion motor = controller.getMotor();
        if (motor != null) {
            motor.addObserver(this);
            updateGrid(motor.getEcosistema());
            updateStats(0, motor.getEcosistema().poblacion(), motor.getEcosistema().porcentajeOcupacion());
        }
    }

    private Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top: Title and stats
        VBox topBox = new VBox(10);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10));

        Label titleLabel = new Label("Simulación del Ecosistema");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        turnoLabel = new Label("Turno: 0");
        presasLabel = new Label("Presas: 0");
        presasLabel.setStyle("-fx-text-fill: green;");
        depredadoresLabel = new Label("Depredadores: 0");
        depredadoresLabel.setStyle("-fx-text-fill: red;");
        carroñerosLabel = new Label("Carroñeros: 0");
        carroñerosLabel.setStyle("-fx-text-fill: gray;");
        ocupacionLabel = new Label("Ocupación: 0%");

        statsBox.getChildren().addAll(turnoLabel, presasLabel, depredadoresLabel, carroñerosLabel, ocupacionLabel);
        topBox.getChildren().addAll(titleLabel, statsBox);
        root.setTop(topBox);

        // Center: Grid
        gridPane = new GridPane();
        gridPane.setHgap(2);
        gridPane.setVgap(2);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setStyle("-fx-background-color: #333;");
        gridPane.setPadding(new Insets(5));
        initializeGrid();

        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        root.setCenter(scrollPane);

        // Bottom: Controls
        VBox controlBox = new VBox(10);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(10));

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        playButton = new Button("▶ Reproducir");
        playButton.setOnAction(e -> togglePlay());

        stepButton = new Button("⏭ Paso");
        stepButton.setOnAction(e -> executeStep());

        Button stopButton = new Button("⏹ Detener");
        stopButton.setOnAction(e -> stopSimulation());

        Button reportButton = new Button("📊 Ver Reporte");
        reportButton.setOnAction(e -> showReport());

        buttonBox.getChildren().addAll(playButton, stepButton, stopButton, reportButton);

        // Speed control
        HBox speedBox = new HBox(10);
        speedBox.setAlignment(Pos.CENTER);
        Label speedLabel = new Label("Velocidad:");
        speedSlider = new Slider(100, 2000, 500);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(500);
        Label speedValueLabel = new Label("500ms");
        speedSlider.valueProperty().addListener((obs, old, newVal) -> 
                speedValueLabel.setText(newVal.intValue() + "ms"));
        speedBox.getChildren().addAll(speedLabel, speedSlider, speedValueLabel);

        statusLabel = new Label("Listo para iniciar");
        statusLabel.setStyle("-fx-font-style: italic;");

        // Legend
        HBox legendBox = createLegend();

        controlBox.getChildren().addAll(buttonBox, speedBox, legendBox, statusLabel);
        root.setBottom(controlBox);

        return new Scene(root);
    }

    private void initializeGrid() {
        for (int i = 0; i < Ecosistema.TAMANO; i++) {
            for (int j = 0; j < Ecosistema.TAMANO; j++) {
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                cell.setFill(Color.WHITE);
                cell.setStroke(Color.LIGHTGRAY);
                cell.setArcWidth(5);
                cell.setArcHeight(5);
                gridPane.add(cell, j, i);
            }
        }
    }

    private HBox createLegend() {
        HBox legend = new HBox(15);
        legend.setAlignment(Pos.CENTER);

        legend.getChildren().addAll(
                createLegendItem("Vacía", Color.WHITE),
                createLegendItem("Presa", Color.LIMEGREEN),
                createLegendItem("Depredador", Color.CRIMSON),
                createLegendItem("Carroñero", Color.SLATEGRAY),
                createLegendItem("Cadáver", Color.SADDLEBROWN)
        );

        return legend;
    }

    private HBox createLegendItem(String label, Color color) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER);
        Rectangle rect = new Rectangle(15, 15, color);
        rect.setStroke(Color.BLACK);
        item.getChildren().addAll(rect, new Label(label));
        return item;
    }

    private void updateGrid(Ecosistema ecosistema) {
        Platform.runLater(() -> {
            for (int i = 0; i < Ecosistema.TAMANO; i++) {
                for (int j = 0; j < Ecosistema.TAMANO; j++) {
                    Celda celda = ecosistema.getCelda(i, j);
                    Rectangle rect = (Rectangle) gridPane.getChildren().get(i * Ecosistema.TAMANO + j);
                    
                    if (celda.getHabitante() != null) {
                        Especie esp = celda.getHabitante();
                        if (esp instanceof Presas) {
                            rect.setFill(Color.LIMEGREEN);
                        } else if (esp instanceof Depredadores) {
                            rect.setFill(Color.CRIMSON);
                        } else if (esp instanceof Carroneros) {
                            rect.setFill(Color.SLATEGRAY);
                        }
                    } else if (celda.tieneCadaver()) {
                        rect.setFill(Color.SADDLEBROWN);
                    } else {
                        rect.setFill(Color.WHITE);
                    }
                }
            }
        });
    }

    private void updateStats(int turno, Map<String, Integer> poblacion, double ocupacion) {
        Platform.runLater(() -> {
            turnoLabel.setText("Turno: " + turno);
            presasLabel.setText("Presas: " + poblacion.getOrDefault("presas", 0));
            depredadoresLabel.setText("Depredadores: " + poblacion.getOrDefault("depredadores", 0));
            carroñerosLabel.setText("Carroñeros: " + poblacion.getOrDefault("carroneros", 0));
            ocupacionLabel.setText(String.format("Ocupación: %.0f%%", ocupacion * 100));
        });
    }

    private void togglePlay() {
        if (isPlaying) {
            pauseSimulation();
        } else {
            playSimulation();
        }
    }

    private void playSimulation() {
        isPlaying = true;
        playButton.setText("⏸ Pausar");
        stepButton.setDisable(true);
        statusLabel.setText("Simulación en curso...");

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            MotorDeSimulacion motor = controller.getMotor();
            if (motor != null && !motor.getEcosistema().hayExtincion() && 
                    motor.getTurnoActual() < motor.getMaxTurnos()) {
                controller.runSimulationStep();
            } else {
                Platform.runLater(this::stopSimulation);
            }
        }, 0, (long) speedSlider.getValue(), TimeUnit.MILLISECONDS);
    }

    private void pauseSimulation() {
        isPlaying = false;
        playButton.setText("▶ Reproducir");
        stepButton.setDisable(false);
        statusLabel.setText("Simulación pausada");
        
        if (executor != null) {
            executor.shutdown();
        }
    }

    private void executeStep() {
        MotorDeSimulacion motor = controller.getMotor();
        if (motor != null && !motor.getEcosistema().hayExtincion() && 
                motor.getTurnoActual() < motor.getMaxTurnos()) {
            controller.runSimulationStep();
        } else {
            statusLabel.setText("Simulación finalizada");
        }
    }

    private void stopSimulation() {
        pauseSimulation();
        MotorDeSimulacion motor = controller.getMotor();
        if (motor != null) {
            motor.detener();
        }
        statusLabel.setText("Simulación detenida");
    }

    private void showReport() {
        pauseSimulation();
        controller.showReporteView();
    }

    // SimulationObserver implementation
    @Override
    public void onTurnEnd(int turno, Ecosistema ecosistema, Map<String, Integer> poblacion, double ocupacion) {
        updateGrid(ecosistema);
        updateStats(turno, poblacion, ocupacion);
    }

    @Override
    public void onExtinction(String especie, int turno) {
        Platform.runLater(() -> {
            statusLabel.setText("¡EXTINCIÓN! " + especie + " se extinguió en turno " + turno);
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            pauseSimulation();
        });
    }

    @Override
    public void onSimulationEnd(int turnoFinal, Ecosistema ecosistema) {
        Platform.runLater(() -> {
            statusLabel.setText("Simulación finalizada en turno " + turnoFinal);
            pauseSimulation();
        });
    }

    public Scene getScene() {
        return scene;
    }
}
