package com.ecosimulator.ui;

import com.ecosimulator.simulation.Escenario;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * View for selecting the simulation scenario.
 */
public class ScenarioSelectionView {
    
    private final MainController controller;
    private final Scene scene;
    
    private ComboBox<Escenario> escenarioCombo;
    private Spinner<Integer> turnosSpinner;
    private Label descriptionLabel;

    public ScenarioSelectionView(MainController controller) {
        this.controller = controller;
        this.scene = createScene();
    }

    private Scene createScene() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        // Welcome
        Label welcomeLabel = new Label("Bienvenido, " + 
                (controller.getUsuarioActual() != null ? controller.getUsuarioActual().getNombre() : "Usuario"));
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label titleLabel = new Label("Seleccione un Escenario");
        titleLabel.setStyle("-fx-font-size: 16px;");

        // Scenario selection
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(15);
        form.setAlignment(Pos.CENTER);

        form.add(new Label("Escenario:"), 0, 0);
        escenarioCombo = new ComboBox<>();
        escenarioCombo.getItems().addAll(Escenario.getPredefinidos());
        escenarioCombo.setValue(Escenario.EQUILIBRADO);
        escenarioCombo.setOnAction(e -> updateDescription());
        form.add(escenarioCombo, 1, 0);

        form.add(new Label("Máximo de turnos:"), 0, 1);
        turnosSpinner = new Spinner<>(10, 500, 100, 10);
        turnosSpinner.setEditable(true);
        form.add(turnosSpinner, 1, 1);

        // Description
        descriptionLabel = new Label();
        descriptionLabel.setStyle("-fx-font-style: italic;");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(400);
        updateDescription();

        // Scenario details
        VBox detailsBox = new VBox(10);
        detailsBox.setAlignment(Pos.CENTER);
        detailsBox.setPadding(new Insets(15));
        detailsBox.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5;");
        
        Label detailsTitle = new Label("Detalles del Escenario");
        detailsTitle.setStyle("-fx-font-weight: bold;");
        
        detailsBox.getChildren().addAll(detailsTitle, descriptionLabel);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button startButton = new Button("Iniciar Simulación");
        startButton.setDefaultButton(true);
        startButton.setStyle("-fx-font-size: 14px;");
        startButton.setOnAction(e -> handleStart());

        Button logoutButton = new Button("Cerrar Sesión");
        logoutButton.setOnAction(e -> controller.showLoginView());

        buttonBox.getChildren().addAll(startButton, logoutButton);

        root.getChildren().addAll(welcomeLabel, titleLabel, form, detailsBox, buttonBox);

        return new Scene(root);
    }

    private void updateDescription() {
        Escenario selected = escenarioCombo.getValue();
        if (selected != null) {
            StringBuilder desc = new StringBuilder();
            desc.append("Configuración inicial:\n");
            desc.append("• Presas: ").append(selected.getPresasIniciales()).append("\n");
            desc.append("• Depredadores: ").append(selected.getDepredadoresIniciales()).append("\n");
            desc.append("• Carroñeros: ").append(selected.getCarroneroIniciales()).append("\n");
            desc.append("\nMatriz: 10×10 (100 celdas)");
            descriptionLabel.setText(desc.toString());
        }
    }

    private void handleStart() {
        Escenario escenario = escenarioCombo.getValue();
        int maxTurnos = turnosSpinner.getValue();
        
        controller.startSimulation(escenario, maxTurnos);
        controller.showSimulacionView();
    }

    public Scene getScene() {
        return scene;
    }
}
