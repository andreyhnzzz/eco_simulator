package com.ecosimulator.report;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Generates charts for simulation reports using JFreeChart
 */
public class ChartGenerator {

    /**
     * Create a pie chart showing population distribution
     * @param data map of species names to their counts
     * @return BufferedImage containing the chart
     */
    public static BufferedImage createPopulationChart(Map<String, Integer> data) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        data.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart(
            "Final Population Distribution",
            dataset,
            true,  // legend
            true,  // tooltips
            false  // urls
        );

        // Customize chart appearance
        chart.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setShadowPaint(null);
        
        // Set custom colors for species
        if (data.containsKey("Predators")) {
            plot.setSectionPaint("Predators", new Color(0xD3, 0x2F, 0x2F)); // Red
        }
        if (data.containsKey("Prey")) {
            plot.setSectionPaint("Prey", new Color(0x19, 0x76, 0xD2)); // Blue
        }
        if (data.containsKey("Third Species")) {
            plot.setSectionPaint("Third Species", new Color(0xFF, 0x98, 0x00)); // Orange
        }

        return chart.createBufferedImage(400, 300);
    }

    /**
     * Create a pie chart showing ecosystem occupancy
     * @param occupiedCells number of occupied cells
     * @param totalCells total number of cells in the grid
     * @return BufferedImage containing the chart
     */
    public static BufferedImage createOccupancyChart(int occupiedCells, int totalCells) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("Occupied", occupiedCells);
        dataset.setValue("Empty", totalCells - occupiedCells);

        JFreeChart chart = ChartFactory.createPieChart(
            "Ecosystem Occupancy",
            dataset,
            true,
            true,
            false
        );

        chart.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setShadowPaint(null);
        
        plot.setSectionPaint("Occupied", new Color(0x4C, 0xAF, 0x50)); // Green
        plot.setSectionPaint("Empty", new Color(0x9E, 0x9E, 0x9E)); // Gray

        return chart.createBufferedImage(400, 300);
    }
}
