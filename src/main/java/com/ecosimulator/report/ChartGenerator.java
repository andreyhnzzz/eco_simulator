package com.ecosimulator.report;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Generates charts for simulation reports using JFreeChart
 */
public class ChartGenerator {

    // Color constants for species
    private static final Color PREDATOR_COLOR = new Color(0xD3, 0x2F, 0x2F);  // Red
    private static final Color PREY_COLOR = new Color(0x19, 0x76, 0xD2);      // Blue
    private static final Color THIRD_SPECIES_COLOR = new Color(0xFF, 0x98, 0x00); // Orange
    private static final Color OCCUPIED_COLOR = new Color(0x4C, 0xAF, 0x50);  // Green
    private static final Color EMPTY_COLOR = new Color(0x9E, 0x9E, 0x9E);     // Gray

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
            plot.setSectionPaint("Predators", PREDATOR_COLOR);
        }
        if (data.containsKey("Prey")) {
            plot.setSectionPaint("Prey", PREY_COLOR);
        }
        if (data.containsKey("Third Species")) {
            plot.setSectionPaint("Third Species", THIRD_SPECIES_COLOR);
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
        
        plot.setSectionPaint("Occupied", OCCUPIED_COLOR);
        plot.setSectionPaint("Empty", EMPTY_COLOR);

        return chart.createBufferedImage(400, 300);
    }

    /**
     * Create a pie chart showing resource consumption
     * @param waterConsumed total water consumed during simulation
     * @param foodConsumed total food consumed during simulation
     * @return BufferedImage containing the chart
     */
    public static BufferedImage createResourceConsumptionChart(int waterConsumed, int foodConsumed) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("Water Consumed", waterConsumed);
        dataset.setValue("Food Consumed", foodConsumed);

        JFreeChart chart = ChartFactory.createPieChart(
            "Resource Consumption",
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
        
        plot.setSectionPaint("Water Consumed", new Color(0x21, 0x96, 0xF3)); // Blue
        plot.setSectionPaint("Food Consumed", new Color(0x8B, 0xC3, 0x4A)); // Light green

        return chart.createBufferedImage(400, 300);
    }

    /**
     * Create a bar chart showing dominance index for each species
     * @param predatorDominance dominance index for predators (0.0 to 1.0)
     * @param preyDominance dominance index for prey (0.0 to 1.0)
     * @param thirdSpeciesDominance dominance index for third species (0.0 to 1.0)
     * @return BufferedImage containing the chart
     */
    public static BufferedImage createDominanceIndexChart(double predatorDominance, 
                                                          double preyDominance, 
                                                          double thirdSpeciesDominance) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(predatorDominance, "Dominance Index", "Predators");
        dataset.addValue(preyDominance, "Dominance Index", "Prey");
        if (thirdSpeciesDominance > 0) {
            dataset.addValue(thirdSpeciesDominance, "Dominance Index", "Scavenger");
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Dominance Index by Species",
            "Species",
            "Dominance Index",
            dataset
        );

        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        // Customize bar colors
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, PREDATOR_COLOR);
        
        // Set individual bar colors based on category
        renderer.setSeriesPaint(0, new Color(0x4C, 0xAF, 0x50)); // Default green for dominance
        
        return chart.createBufferedImage(400, 300);
    }
}
