package com.ecosimulator.reporting;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.*;
import java.nio.file.*;

/**
 * PDF report generator using PDFBox and JFreeChart.
 */
public class ReportePDFGenerator {
    
    private static final int CHART_WIDTH = 500;
    private static final int CHART_HEIGHT = 300;
    
    private final AnalisisEscenarios analisis;
    private final String nombreEscenario;

    public ReportePDFGenerator(AnalisisEscenarios analisis, String nombreEscenario) {
        this.analisis = analisis;
        this.nombreEscenario = nombreEscenario;
    }

    /**
     * Generate the PDF report.
     * @param outputPath path for the output PDF file
     * @return true if successful
     */
    public boolean generarReporte(String outputPath) {
        try (PDDocument document = new PDDocument()) {
            
            // Page 1: Summary
            addSummaryPage(document);
            
            // Page 2: Population chart
            addPopulationChartPage(document);
            
            // Page 3: Occupation chart
            addOccupationChartPage(document);
            
            document.save(outputPath);
            return true;
            
        } catch (IOException e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            return false;
        }
    }

    private void addSummaryPage(PDDocument document) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        try (PDPageContentStream content = new PDPageContentStream(document, page)) {
            float y = 750;
            float margin = 50;
            
            // Title
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 20);
            content.newLineAtOffset(margin, y);
            content.showText("Reporte de Simulacion - Ecosistema");
            content.endText();
            y -= 40;
            
            // Scenario
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            content.newLineAtOffset(margin, y);
            content.showText("Escenario: " + sanitizeText(nombreEscenario));
            content.endText();
            y -= 30;
            
            // Summary data
            String[] lines = {
                    "Total de turnos: " + analisis.getTotalTurnos(),
                    "Ocupacion promedio: " + String.format("%.2f%%", analisis.getOcupacionPromedio() * 100),
                    "Ocupacion final: " + String.format("%.2f%%", analisis.getOcupacionFinal() * 100),
                    "",
                    "Poblacion final:",
            };
            
            for (String line : lines) {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(margin, y);
                content.showText(sanitizeText(line));
                content.endText();
                y -= 20;
            }
            
            var poblacionFinal = analisis.getPoblacionFinal();
            if (poblacionFinal != null) {
                String[] poblacionLines = {
                        "  - Presas: " + poblacionFinal.getPresas(),
                        "  - Depredadores: " + poblacionFinal.getDepredadores(),
                        "  - Carroneros: " + poblacionFinal.getCarroneros(),
                };
                for (String line : poblacionLines) {
                    content.beginText();
                    content.setFont(PDType1Font.HELVETICA, 12);
                    content.newLineAtOffset(margin, y);
                    content.showText(sanitizeText(line));
                    content.endText();
                    y -= 20;
                }
            }
            
            y -= 10;
            
            // Stability
            String[] stabilityLines = {
                    "Estabilidad (desviacion estandar):",
                    "  - Presas: " + String.format("%.2f", analisis.getEstabilidadPresas()),
                    "  - Depredadores: " + String.format("%.2f", analisis.getEstabilidadDepredadores()),
            };
            
            for (String line : stabilityLines) {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(margin, y);
                content.showText(sanitizeText(line));
                content.endText();
                y -= 20;
            }
            
            y -= 20;
            
            // Extinction info
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            content.newLineAtOffset(margin, y);
            if (analisis.huboExtincion()) {
                content.showText("EXTINCION: " + sanitizeText(analisis.getEspecieExtinta()) + 
                        " en turno " + analisis.getTurnoExtincion());
            } else {
                content.showText("Sin extincion");
            }
            content.endText();
        }
    }

    private void addPopulationChartPage(PDDocument document) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        // Create population chart
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        XYSeries presasSeries = new XYSeries("Presas");
        XYSeries depredadoresSeries = new XYSeries("Depredadores");
        XYSeries carronerosSeries = new XYSeries("Carroneros");
        
        int[] presas = analisis.getPresasPorTurno();
        int[] depredadores = analisis.getDepredadoresPorTurno();
        int[] carroneros = analisis.getCarronerosPorTurno();
        
        for (int i = 0; i < presas.length; i++) {
            presasSeries.add(i + 1, presas[i]);
            depredadoresSeries.add(i + 1, depredadores[i]);
            carronerosSeries.add(i + 1, carroneros[i]);
        }
        
        dataset.addSeries(presasSeries);
        dataset.addSeries(depredadoresSeries);
        dataset.addSeries(carronerosSeries);
        
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Poblacion por Turno",
                "Turno",
                "Poblacion",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        // Set colors
        chart.getXYPlot().getRenderer().setSeriesPaint(0, new Color(0, 150, 0)); // Green for prey
        chart.getXYPlot().getRenderer().setSeriesPaint(1, new Color(200, 0, 0)); // Red for predators
        chart.getXYPlot().getRenderer().setSeriesPaint(2, new Color(100, 100, 100)); // Gray for scavengers
        
        // Save chart as image
        Path tempChart = Files.createTempFile("chart_population", ".png");
        ChartUtils.saveChartAsPNG(tempChart.toFile(), chart, CHART_WIDTH, CHART_HEIGHT);
        
        // Add to PDF
        addChartToPage(document, page, tempChart, "Grafico: Poblacion por especie a lo largo del tiempo");
        
        Files.deleteIfExists(tempChart);
    }

    private void addOccupationChartPage(PDDocument document) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        
        // Create occupation chart
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries ocupacionSeries = new XYSeries("Ocupacion");
        
        double[] ocupacion = analisis.getOcupacionPorTurno();
        for (int i = 0; i < ocupacion.length; i++) {
            ocupacionSeries.add(i + 1, ocupacion[i] * 100);
        }
        
        dataset.addSeries(ocupacionSeries);
        
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Ocupacion del Ecosistema",
                "Turno",
                "Ocupacion (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        
        chart.getXYPlot().getRenderer().setSeriesPaint(0, new Color(0, 100, 200));
        
        // Save chart as image
        Path tempChart = Files.createTempFile("chart_occupation", ".png");
        ChartUtils.saveChartAsPNG(tempChart.toFile(), chart, CHART_WIDTH, CHART_HEIGHT);
        
        // Add to PDF
        addChartToPage(document, page, tempChart, "Grafico: Porcentaje de ocupacion del ecosistema");
        
        Files.deleteIfExists(tempChart);
    }

    private void addChartToPage(PDDocument document, PDPage page, Path chartPath, String title) 
            throws IOException {
        try (PDPageContentStream content = new PDPageContentStream(document, page)) {
            float margin = 50;
            float y = 750;
            
            // Title
            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            content.newLineAtOffset(margin, y);
            content.showText(sanitizeText(title));
            content.endText();
            
            // Image
            PDImageXObject image = PDImageXObject.createFromFile(chartPath.toString(), document);
            content.drawImage(image, margin, y - CHART_HEIGHT - 50, CHART_WIDTH, CHART_HEIGHT);
        }
    }

    /**
     * Sanitize text to remove characters that PDFBox cannot encode with basic fonts.
     * Note: PDType1Font (standard 14 fonts) do not support accented characters.
     * This method replaces Spanish accented characters with their non-accented equivalents
     * to ensure PDF generation works without requiring custom font embedding.
     */
    private String sanitizeText(String text) {
        if (text == null) return "";
        // Replace accented characters with non-accented equivalents
        return text
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u")
                .replace("Á", "A").replace("É", "E").replace("Í", "I")
                .replace("Ó", "O").replace("Ú", "U")
                .replace("ñ", "n").replace("Ñ", "N")
                .replace("ü", "u").replace("Ü", "U");
    }
}
