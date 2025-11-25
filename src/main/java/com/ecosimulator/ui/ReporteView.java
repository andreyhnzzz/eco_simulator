package com.ecosimulator.ui;

import com.ecosimulator.reporting.AnalisisEscenarios;
import com.ecosimulator.simulation.EstadoTurno;
import com.ecosimulator.simulation.MotorDeSimulacion;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * View for displaying simulation reports and statistics.
 */
public class ReporteView {
    
    private final MainController controller;
    private final Scene scene;
    
    private TextArea reportTextArea;
    private Label statusLabel;

    public ReporteView(MainController controller) {
        this.controller = controller;
        this.scene = createScene();
        loadReport();
    }

    private Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Top: Title
        VBox topBox = new VBox(10);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(10));

        Label titleLabel = new Label("Reporte de Simulación");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        String escenarioNombre = controller.getEscenarioActual() != null ? 
                controller.getEscenarioActual().getNombre() : "Sin escenario";
        Label escenarioLabel = new Label("Escenario: " + escenarioNombre);
        escenarioLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic;");

        topBox.getChildren().addAll(titleLabel, escenarioLabel);
        root.setTop(topBox);

        // Center: Report text
        reportTextArea = new TextArea();
        reportTextArea.setEditable(false);
        reportTextArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        reportTextArea.setWrapText(true);
        root.setCenter(reportTextArea);

        // Bottom: Controls
        VBox bottomBox = new VBox(10);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setPadding(new Insets(10));

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button generatePdfButton = new Button("📄 Generar PDF");
        generatePdfButton.setOnAction(e -> generatePdf());

        Button sendEmailButton = new Button("📧 Enviar por Correo");
        sendEmailButton.setOnAction(e -> sendEmail());

        Button backButton = new Button("🔙 Volver a Simulación");
        backButton.setOnAction(e -> controller.showSimulacionView());

        Button newSimulationButton = new Button("🔄 Nueva Simulación");
        newSimulationButton.setOnAction(e -> controller.showScenarioSelection());

        buttonBox.getChildren().addAll(generatePdfButton, sendEmailButton, backButton, newSimulationButton);

        statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-style: italic;");

        bottomBox.getChildren().addAll(buttonBox, statusLabel);
        root.setBottom(bottomBox);

        return new Scene(root);
    }

    private void loadReport() {
        MotorDeSimulacion motor = controller.getMotor();
        if (motor == null) {
            reportTextArea.setText("No hay datos de simulación disponibles.");
            return;
        }

        AnalisisEscenarios analisis = new AnalisisEscenarios(
                motor.getHistorial(),
                motor.getTurnoExtincion(),
                motor.getEspecieExtinta()
        );

        StringBuilder report = new StringBuilder();
        report.append(analisis.generarResumen());
        report.append("\n\n");
        report.append("=== HISTORIAL DE POBLACIÓN ===\n");
        report.append(String.format("%-8s %-10s %-14s %-12s %-12s%n", 
                "Turno", "Presas", "Depredadores", "Carroñeros", "Ocupación"));
        report.append("-".repeat(60)).append("\n");

        for (EstadoTurno estado : analisis.getHistorial()) {
            report.append(String.format("%-8d %-10d %-14d %-12d %-12.0f%%%n",
                    estado.getTurno(),
                    estado.getPresas(),
                    estado.getDepredadores(),
                    estado.getCarroneros(),
                    estado.getOcupacion() * 100
            ));
        }

        reportTextArea.setText(report.toString());
    }

    private void generatePdf() {
        statusLabel.setText("Generando PDF...");
        
        String pdfPath = controller.generateReport();
        
        if (pdfPath != null) {
            statusLabel.setText("PDF generado: " + pdfPath);
            statusLabel.setStyle("-fx-text-fill: green;");
            
            // Ask if user wants to save to a different location
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("PDF Generado");
            alert.setHeaderText("El reporte se ha generado exitosamente");
            alert.setContentText("¿Desea guardar una copia en otra ubicación?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Guardar PDF");
                    fileChooser.setInitialFileName("reporte_simulacion.pdf");
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
                    );
                    
                    File file = fileChooser.showSaveDialog(controller.getPrimaryStage());
                    if (file != null) {
                        try {
                            java.nio.file.Files.copy(
                                    new File(pdfPath).toPath(),
                                    file.toPath(),
                                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                            );
                            statusLabel.setText("PDF guardado en: " + file.getAbsolutePath());
                        } catch (Exception ex) {
                            statusLabel.setText("Error al guardar: " + ex.getMessage());
                            statusLabel.setStyle("-fx-text-fill: red;");
                        }
                    }
                }
            });
        } else {
            statusLabel.setText("Error al generar PDF");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void sendEmail() {
        statusLabel.setText("Enviando correo...");
        
        String pdfPath = controller.generateReport();
        if (pdfPath == null) {
            statusLabel.setText("Error: No se pudo generar el PDF");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        boolean sent = controller.sendReportByEmail(pdfPath);
        
        if (sent) {
            statusLabel.setText("Correo enviado exitosamente");
            statusLabel.setStyle("-fx-text-fill: green;");
        } else {
            statusLabel.setText("No se pudo enviar el correo. PDF guardado localmente: " + pdfPath);
            statusLabel.setStyle("-fx-text-fill: orange;");
        }
    }

    public Scene getScene() {
        return scene;
    }
}
