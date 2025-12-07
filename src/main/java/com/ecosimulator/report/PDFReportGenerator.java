package com.ecosimulator.report;

import com.ecosimulator.model.MultiSimulationReport;
import com.ecosimulator.model.SimulationResult;
import com.ecosimulator.model.SimulationStats;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates PDF reports for simulation results
 * Uses Apache PDFBox for PDF creation and embeds JFreeChart images
 */
public class PDFReportGenerator {

    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 15;

    /**
     * Generate a complete simulation report PDF
     * @param outputPath path where the PDF will be saved
     * @param totalTurns total number of turns executed
     * @param stats final simulation statistics
     * @param gridSize size of the simulation grid
     * @param extinctionTurn turn when extinction occurred (-1 if no extinction)
     * @throws IOException if PDF generation fails
     */
    public static void generateReport(
            String outputPath,
            int totalTurns,
            SimulationStats stats,
            int gridSize,
            int extinctionTurn
    ) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float yPosition = pageHeight - MARGIN;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Title
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Eco Simulator Report");
                contentStream.endText();
                yPosition -= 40;

                // Date
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                contentStream.showText("Generated: " + date);
                contentStream.endText();
                yPosition -= 30;

                // Simulation Summary
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Simulation Summary");
                contentStream.endText();
                yPosition -= 25;

                // Statistics
                String[] lines = {
                    "Total Turns: " + totalTurns,
                    "Grid Size: " + gridSize + " x " + gridSize,
                    "Final Predator Count: " + stats.getPredatorCount(),
                    "Final Prey Count: " + stats.getPreyCount(),
                    "Final Third Species Count: " + stats.getThirdSpeciesCount(),
                    "Mutated Creatures: " + stats.getMutatedCount(),
                    "Total Creatures: " + stats.getTotalCreatures(),
                    "Water Sources: " + stats.getWaterCount(),
                    "Food Sources: " + stats.getFoodCount(),
                    "Total Water Consumed: " + stats.getTotalWaterConsumed(),
                    "Total Food Consumed: " + stats.getTotalFoodConsumed(),
                    extinctionTurn > 0 ? "Extinction Turn: " + extinctionTurn : "No Extinction",
                    "Result: " + stats.getWinner()
                };

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                for (String line : lines) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText(line);
                    contentStream.endText();
                    yPosition -= LINE_HEIGHT;
                }

                yPosition -= 20;

                // Ecosystem Occupancy
                int totalCells = gridSize * gridSize;
                int occupiedCells = stats.getTotalCreatures();
                double occupancyPercent = (double) occupiedCells / totalCells * 100;
                
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText(String.format("Ecosystem Occupancy: %.1f%%", occupancyPercent));
                contentStream.endText();
                yPosition -= 30;

                // Population Chart
                Map<String, Integer> populationData = new LinkedHashMap<>();
                populationData.put("Predators", stats.getPredatorCount());
                populationData.put("Prey", stats.getPreyCount());
                if (stats.getThirdSpeciesCount() > 0) {
                    populationData.put("Third Species", stats.getThirdSpeciesCount());
                }

                BufferedImage populationChart = ChartGenerator.createPopulationChart(populationData);
                PDImageXObject populationImage = LosslessFactory.createFromImage(document, populationChart);
                
                float imageWidth = 300;
                float imageHeight = 225;
                float imageX = (pageWidth - imageWidth) / 2;
                
                contentStream.drawImage(populationImage, imageX, yPosition - imageHeight, imageWidth, imageHeight);
                yPosition -= (imageHeight + 30);

                // Occupancy Chart
                BufferedImage occupancyChart = ChartGenerator.createOccupancyChart(occupiedCells, totalCells);
                PDImageXObject occupancyImage = LosslessFactory.createFromImage(document, occupancyChart);
                
                contentStream.drawImage(occupancyImage, imageX, yPosition - imageHeight, imageWidth, imageHeight);
                yPosition -= (imageHeight + 30);

                // Resource Consumption Chart (if resources were consumed)
                if (stats.getTotalWaterConsumed() > 0 || stats.getTotalFoodConsumed() > 0) {
                    BufferedImage resourceChart = ChartGenerator.createResourceConsumptionChart(
                        stats.getTotalWaterConsumed(), stats.getTotalFoodConsumed());
                    PDImageXObject resourceImage = LosslessFactory.createFromImage(document, resourceChart);
                    
                    // Add new page if needed
                    if (yPosition < imageHeight + MARGIN) {
                        PDPage newPage = new PDPage(PDRectangle.A4);
                        document.addPage(newPage);
                        yPosition = newPage.getMediaBox().getHeight() - MARGIN;
                        
                        try (PDPageContentStream newContentStream = new PDPageContentStream(document, newPage)) {
                            newContentStream.drawImage(resourceImage, imageX, yPosition - imageHeight, imageWidth, imageHeight);
                        }
                    } else {
                        contentStream.drawImage(resourceImage, imageX, yPosition - imageHeight, imageWidth, imageHeight);
                    }
                }
            }

            document.save(outputPath);
        }
    }

    /**
     * Generate a simple report without charts (fallback for environments without AWT)
     * @param outputPath path where the PDF will be saved
     * @param totalTurns total number of turns executed
     * @param stats final simulation statistics
     * @param gridSize size of the simulation grid
     * @param extinctionTurn turn when extinction occurred (-1 if no extinction)
     * @throws IOException if PDF generation fails
     */
    public static void generateSimpleReport(
            String outputPath,
            int totalTurns,
            SimulationStats stats,
            int gridSize,
            int extinctionTurn
    ) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float pageHeight = page.getMediaBox().getHeight();
            float yPosition = pageHeight - MARGIN;

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Title
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Eco Simulator Report");
                contentStream.endText();
                yPosition -= 40;

                // Date
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                contentStream.showText("Generated: " + date);
                contentStream.endText();
                yPosition -= 30;

                // Statistics
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("Simulation Summary");
                contentStream.endText();
                yPosition -= 25;

                int totalCells = gridSize * gridSize;
                double occupancyPercent = (double) stats.getTotalCreatures() / totalCells * 100;

                String[] lines = {
                    "Total Turns: " + totalTurns,
                    "Grid Size: " + gridSize + " x " + gridSize,
                    "",
                    "Final Population:",
                    "  - Predators: " + stats.getPredatorCount(),
                    "  - Prey: " + stats.getPreyCount(),
                    "  - Third Species: " + stats.getThirdSpeciesCount(),
                    "  - Mutated: " + stats.getMutatedCount(),
                    "  - Total: " + stats.getTotalCreatures(),
                    "",
                    "Resources:",
                    "  - Water Sources: " + stats.getWaterCount(),
                    "  - Food Sources: " + stats.getFoodCount(),
                    "  - Water Consumed: " + stats.getTotalWaterConsumed(),
                    "  - Food Consumed: " + stats.getTotalFoodConsumed(),
                    "",
                    String.format("Ecosystem Occupancy: %.1f%%", occupancyPercent),
                    extinctionTurn > 0 ? "Extinction at Turn: " + extinctionTurn : "No Extinction Occurred",
                    "",
                    "Result: " + stats.getWinner()
                };

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                for (String line : lines) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText(line);
                    contentStream.endText();
                    yPosition -= LINE_HEIGHT;
                }
            }

            document.save(outputPath);
        }
    }

    /**
     * Get the default report filename
     * @return default filename with timestamp
     */
    public static String getDefaultFilename() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "report_" + timestamp + ".pdf";
    }
    
    /**
     * Generate a multi-simulation report with one page per simulation
     * @param outputPath path where the PDF will be saved
     * @param report the multi-simulation report containing all simulation results
     * @throws IOException if PDF generation fails
     * @throws IllegalStateException if report is empty
     */
    public static void generateMultiSimulationReport(String outputPath, MultiSimulationReport report) 
            throws IOException {
        if (report.isEmpty()) {
            throw new IllegalStateException("Cannot generate report with no simulations");
        }
        
        try (PDDocument document = new PDDocument()) {
            float pageWidth = PDRectangle.A4.getWidth();
            float pageHeight = PDRectangle.A4.getHeight();
            
            // Summary page
            addSummaryPage(document, report, pageWidth, pageHeight);
            
            // Individual simulation pages
            for (SimulationResult result : report.getResults()) {
                addSimulationPage(document, result, pageWidth, pageHeight);
            }
            
            document.save(outputPath);
        }
    }
    
    /**
     * Add a summary page to the document
     */
    private static void addSummaryPage(PDDocument document, MultiSimulationReport report,
                                      float pageWidth, float pageHeight) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        float yPosition = pageHeight - MARGIN;
        
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Title
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 24);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Multi-Simulation Report");
            contentStream.endText();
            yPosition -= 40;
            
            // Date
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            String date = report.getReportTimestamp()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            contentStream.showText("Generated: " + date);
            contentStream.endText();
            yPosition -= 30;
            
            // Summary
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Summary");
            contentStream.endText();
            yPosition -= 25;
            
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Total Simulations: " + report.getSimulationCount());
            contentStream.endText();
            yPosition -= LINE_HEIGHT + 10;
            
            // List all simulations
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Simulations Included:");
            contentStream.endText();
            yPosition -= LINE_HEIGHT + 5;
            
            int simNum = 1;
            for (SimulationResult result : report.getResults()) {
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN + 10, yPosition);
                contentStream.showText(simNum + ". " + result.getConfigDescription());
                contentStream.endText();
                yPosition -= LINE_HEIGHT;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN + 20, yPosition);
                contentStream.showText("Turns: " + result.getFinalStats().getTurn() + 
                                     " | Winner: " + result.getFinalStats().getWinner());
                contentStream.endText();
                yPosition -= LINE_HEIGHT + 5;
                
                simNum++;
            }
        }
    }
    
    /**
     * Add a page for a single simulation result
     */
    private static void addSimulationPage(PDDocument document, SimulationResult result,
                                         float pageWidth, float pageHeight) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        float yPosition = pageHeight - MARGIN;
        
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Title with simulation number
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 20);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("Simulation #" + result.getSimulationNumber());
            contentStream.endText();
            yPosition -= 30;
            
            // Configuration
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(result.getConfigDescription());
            contentStream.endText();
            yPosition -= 25;
            
            // Date
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            String date = result.getTimestamp()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            contentStream.showText("Completed: " + date);
            contentStream.endText();
            yPosition -= 25;
            
            // Statistics
            SimulationStats stats = result.getFinalStats();
            int totalCells = result.getGridSize() * result.getGridSize();
            double occupancyPercent = (double) stats.getTotalCreatures() / totalCells * 100;
            
            String[] lines = {
                "Total Turns: " + stats.getTurn(),
                "Grid Size: " + result.getGridSize() + " x " + result.getGridSize(),
                "",
                "Final Population:",
                "  - Predators: " + stats.getPredatorCount() + 
                    " (Male: " + stats.getPredatorMaleCount() + ", Female: " + stats.getPredatorFemaleCount() + ")",
                "  - Prey: " + stats.getPreyCount() + 
                    " (Male: " + stats.getPreyMaleCount() + ", Female: " + stats.getPreyFemaleCount() + ")",
                "  - Third Species: " + stats.getThirdSpeciesCount() + 
                    " (Male: " + stats.getThirdSpeciesMaleCount() + ", Female: " + stats.getThirdSpeciesFemaleCount() + ")",
                "  - Mutated: " + stats.getMutatedCount(),
                "  - Total: " + stats.getTotalCreatures(),
                "",
                "Resources:",
                "  - Water Sources: " + stats.getWaterCount(),
                "  - Food Sources: " + stats.getFoodCount(),
                "  - Water Consumed: " + stats.getTotalWaterConsumed(),
                "  - Food Consumed: " + stats.getTotalFoodConsumed(),
                "",
                String.format("Ecosystem Occupancy: %.1f%%", occupancyPercent),
                result.getExtinctionTurn() > 0 ? 
                    "Extinction at Turn: " + result.getExtinctionTurn() : "No Extinction Occurred",
                "",
                "Result: " + stats.getWinner()
            };
            
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
            for (String line : lines) {
                contentStream.beginText();
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText(line);
                contentStream.endText();
                yPosition -= LINE_HEIGHT;
            }
        }
    }
}
