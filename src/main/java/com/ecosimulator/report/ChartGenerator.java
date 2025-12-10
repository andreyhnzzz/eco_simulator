package com.ecosimulator.report;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
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
     * Dominance index represents the proportion of total population for each species (0.0 to 1.0)
     * @param predatorDominance dominance index for predators (0.0-1.0)
     * @param preyDominance dominance index for prey (0.0-1.0)
     * @param thirdSpeciesDominance dominance index for third species (0.0-1.0), use 0 if not present
     * @param includeThirdSpecies whether to include third species in the chart
     * @return BufferedImage containing the chart
     */
    public static BufferedImage createDominanceIndexChart(double predatorDominance, double preyDominance,
                                                          double thirdSpeciesDominance, boolean includeThirdSpecies) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Convert to percentage for better visualization
        // Use separate series for each species to enable individual coloring
        dataset.addValue(predatorDominance * 100, "Predators", "Dominance");
        dataset.addValue(preyDominance * 100, "Prey", "Dominance");
        if (includeThirdSpecies) {
            dataset.addValue(thirdSpeciesDominance * 100, "Third Species", "Dominance");
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Species Dominance Index",
            "",
            "Dominance (%)",
            dataset,
            PlotOrientation.VERTICAL,
            true,   // legend
            true,   // tooltips
            false   // urls
        );

        // Customize chart appearance
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setOutlinePaint(null);
        
        // Set range axis to always show 0-100%
        plot.getRangeAxis().setRange(0.0, 100.0);
        
        // Customize bar renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.2);
        
        // Set colors for each series (species)
        renderer.setSeriesPaint(0, PREDATOR_COLOR);   // Predators
        renderer.setSeriesPaint(1, PREY_COLOR);       // Prey
        if (includeThirdSpecies) {
            renderer.setSeriesPaint(2, THIRD_SPECIES_COLOR);  // Third Species
        }

        return chart.createBufferedImage(400, 300);
    }
    
    /**
     * Create a pie chart showing dominance distribution among species
     * An alternative visualization for dominance index
     * @param predatorDominance dominance index for predators (0.0-1.0)
     * @param preyDominance dominance index for prey (0.0-1.0)
     * @param thirdSpeciesDominance dominance index for third species (0.0-1.0)
     * @param includeThirdSpecies whether to include third species
     * @return BufferedImage containing the chart
     */
    public static BufferedImage createDominancePieChart(double predatorDominance, double preyDominance,
                                                        double thirdSpeciesDominance, boolean includeThirdSpecies) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        
        // Only add species with non-zero dominance
        if (predatorDominance > 0) {
            dataset.setValue(String.format("Predators (%.1f%%)", predatorDominance * 100), predatorDominance);
        }
        if (preyDominance > 0) {
            dataset.setValue(String.format("Prey (%.1f%%)", preyDominance * 100), preyDominance);
        }
        if (includeThirdSpecies && thirdSpeciesDominance > 0) {
            dataset.setValue(String.format("Third Species (%.1f%%)", thirdSpeciesDominance * 100), thirdSpeciesDominance);
        }
        
        // If all species are extinct
        if (dataset.getItemCount() == 0) {
            dataset.setValue("No Survivors", 1.0);
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Species Dominance Index",
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
        
        // Set colors dynamically based on keys
        for (Object key : dataset.getKeys()) {
            String keyStr = key.toString();
            if (keyStr.startsWith("Predators")) {
                plot.setSectionPaint((String) key, PREDATOR_COLOR);
            } else if (keyStr.startsWith("Prey")) {
                plot.setSectionPaint((String) key, PREY_COLOR);
            } else if (keyStr.startsWith("Third Species")) {
                plot.setSectionPaint((String) key, THIRD_SPECIES_COLOR);
            } else {
                plot.setSectionPaint((String) key, EMPTY_COLOR);
            }
        }

        return chart.createBufferedImage(400, 300);
    }
}
