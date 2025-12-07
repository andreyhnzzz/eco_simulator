package com.ecosimulator.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.JFreeChart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Utility class for generating PDF reports
 */
public class PdfUtil {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Generate a simulation report PDF
     * @param outputPath path where PDF will be saved
     * @param scenarioName name of the scenario
     * @param totalTurns total turns executed
     * @param initialCounts initial population counts
     * @param finalCounts final population counts
     * @param extinctionTurn turn when extinction occurred (-1 if no extinction)
     * @param populationChart optional population chart
     * @return true if successful
     */
    public static boolean generateSimulationReport(String outputPath, 
                                                   String scenarioName,
                                                   int totalTurns,
                                                   Map<String, Integer> initialCounts,
                                                   Map<String, Integer> finalCounts,
                                                   int extinctionTurn,
                                                   JFreeChart populationChart) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Title
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Ecosystem Simulation Report");
                contentStream.endText();
                
                // Metadata
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("Generated: " + LocalDateTime.now().format(DATE_FORMAT));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Scenario: " + scenarioName);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Total Turns: " + totalTurns);
                contentStream.endText();
                
                // Initial Population
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.newLineAtOffset(50, 650);
                contentStream.showText("Initial Population:");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 630);
                contentStream.showText("Prey: " + initialCounts.getOrDefault("prey", 0));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Predators: " + initialCounts.getOrDefault("predator", 0));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Third Species: " + initialCounts.getOrDefault("third", 0));
                contentStream.endText();
                
                // Final Population
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.newLineAtOffset(50, 560);
                contentStream.showText("Final Population:");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 540);
                contentStream.showText("Prey: " + finalCounts.getOrDefault("prey", 0));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Predators: " + finalCounts.getOrDefault("predator", 0));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Third Species: " + finalCounts.getOrDefault("third", 0));
                contentStream.endText();
                
                // Extinction info
                if (extinctionTurn > 0) {
                    contentStream.beginText();
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                    contentStream.setNonStrokingColor(1f, 0f, 0f);  // Red color
                    contentStream.newLineAtOffset(50, 480);
                    contentStream.showText("Extinction occurred at turn: " + extinctionTurn);
                    contentStream.setNonStrokingColor(0f, 0f, 0f);  // Back to black
                    contentStream.endText();
                }
                
                // Add chart if provided
                if (populationChart != null) {
                    try {
                        BufferedImage chartImage = populationChart.createBufferedImage(500, 300);
                        
                        // Save chart to temporary file
                        File tempFile = File.createTempFile("chart", ".png");
                        ImageIO.write(chartImage, "png", tempFile);
                        
                        // Add image to PDF
                        PDImageXObject pdImage = PDImageXObject.createFromFile(tempFile.getAbsolutePath(), document);
                        contentStream.drawImage(pdImage, 50, 120, 500, 300);
                        
                        // Delete temporary file
                        tempFile.delete();
                        
                    } catch (Exception e) {
                        System.err.println("Error adding chart to PDF: " + e.getMessage());
                    }
                }
            }
            
            // Save document
            document.save(outputPath);
            System.out.println("PDF report generated: " + outputPath);
            return true;
            
        } catch (IOException e) {
            System.err.println("Error generating PDF report: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generate a simple simulation report without chart
     * @param outputPath path where PDF will be saved
     * @param scenarioName name of the scenario
     * @param totalTurns total turns executed
     * @param initialCounts initial population counts
     * @param finalCounts final population counts
     * @param extinctionTurn turn when extinction occurred
     * @return true if successful
     */
    public static boolean generateSimulationReport(String outputPath, 
                                                   String scenarioName,
                                                   int totalTurns,
                                                   Map<String, Integer> initialCounts,
                                                   Map<String, Integer> finalCounts,
                                                   int extinctionTurn) {
        return generateSimulationReport(outputPath, scenarioName, totalTurns, 
                                       initialCounts, finalCounts, extinctionTurn, null);
    }
}
