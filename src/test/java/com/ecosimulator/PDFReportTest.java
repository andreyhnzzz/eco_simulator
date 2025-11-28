package com.ecosimulator;

import com.ecosimulator.model.SimulationStats;
import com.ecosimulator.report.PDFReportGenerator;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PDF Report Generation
 */
class PDFReportTest {

    private static Path tempDir;

    @BeforeAll
    static void setupClass() throws IOException {
        tempDir = Files.createTempDirectory("pdf_test_");
    }

    @AfterAll
    static void cleanupClass() throws IOException {
        // Clean up temp directory
        Files.walk(tempDir)
            .map(Path::toFile)
            .forEach(File::delete);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void testSimpleReportGeneration() throws IOException {
        SimulationStats stats = new SimulationStats();
        stats.setPredatorCount(10);
        stats.setPreyCount(20);
        stats.setThirdSpeciesCount(5);
        stats.setMutatedCount(3);
        stats.setTurn(100);

        String outputPath = tempDir.resolve("test_simple_report.pdf").toString();

        PDFReportGenerator.generateSimpleReport(
            outputPath,
            100,      // totalTurns
            stats,
            25,       // gridSize
            -1        // extinctionTurn (no extinction)
        );

        File pdfFile = new File(outputPath);
        assertTrue(pdfFile.exists(), "PDF file should be created");
        assertTrue(pdfFile.length() > 0, "PDF file should not be empty");
    }

    @Test
    void testReportWithExtinction() throws IOException {
        SimulationStats stats = new SimulationStats();
        stats.setPredatorCount(0);
        stats.setPreyCount(25);
        stats.setThirdSpeciesCount(0);
        stats.setMutatedCount(2);
        stats.setTurn(50);

        String outputPath = tempDir.resolve("test_extinction_report.pdf").toString();

        PDFReportGenerator.generateSimpleReport(
            outputPath,
            50,       // totalTurns
            stats,
            20,       // gridSize
            45        // extinctionTurn
        );

        File pdfFile = new File(outputPath);
        assertTrue(pdfFile.exists(), "PDF file should be created");
        assertTrue(pdfFile.length() > 0, "PDF file should not be empty");
    }

    @Test
    void testDefaultFilename() {
        String filename = PDFReportGenerator.getDefaultFilename();
        
        assertTrue(filename.startsWith("report_"));
        assertTrue(filename.endsWith(".pdf"));
    }

    @Test
    void testReportWithZeroCreatures() throws IOException {
        SimulationStats stats = new SimulationStats();
        stats.setPredatorCount(0);
        stats.setPreyCount(0);
        stats.setThirdSpeciesCount(0);
        stats.setMutatedCount(0);
        stats.setTurn(10);

        String outputPath = tempDir.resolve("test_empty_report.pdf").toString();

        PDFReportGenerator.generateSimpleReport(
            outputPath,
            10,
            stats,
            15,
            5
        );

        File pdfFile = new File(outputPath);
        assertTrue(pdfFile.exists(), "PDF file should be created even with zero creatures");
    }
}
