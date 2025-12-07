package com.ecosimulator.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.util.List;
import java.util.Map;

/**
 * Utility class for creating charts using JFreeChart
 */
public class ChartUtil {
    
    /**
     * Create a line chart showing population over time
     * @param preyData list of prey counts per turn
     * @param predatorData list of predator counts per turn
     * @param thirdData list of third species counts per turn
     * @return JFreeChart object
     */
    public static JFreeChart createPopulationChart(List<Integer> preyData, 
                                                   List<Integer> predatorData, 
                                                   List<Integer> thirdData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Add data for each turn
        for (int turn = 0; turn < preyData.size(); turn++) {
            dataset.addValue(preyData.get(turn), "Prey", String.valueOf(turn + 1));
        }
        
        for (int turn = 0; turn < predatorData.size(); turn++) {
            dataset.addValue(predatorData.get(turn), "Predators", String.valueOf(turn + 1));
        }
        
        if (thirdData != null && !thirdData.isEmpty()) {
            for (int turn = 0; turn < thirdData.size(); turn++) {
                dataset.addValue(thirdData.get(turn), "Third Species", String.valueOf(turn + 1));
            }
        }
        
        return ChartFactory.createLineChart(
            "Population Over Time",
            "Turn",
            "Population",
            dataset,
            PlotOrientation.VERTICAL,
            true,  // legend
            true,  // tooltips
            false  // urls
        );
    }
    
    /**
     * Create a pie chart showing final population distribution
     * @param counts map of species to counts
     * @return JFreeChart object
     */
    public static JFreeChart createDistributionPieChart(Map<String, Integer> counts) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        int prey = counts.getOrDefault("prey", 0);
        int predators = counts.getOrDefault("predator", 0);
        int third = counts.getOrDefault("third", 0);
        
        if (prey > 0) {
            dataset.setValue("Prey", prey);
        }
        if (predators > 0) {
            dataset.setValue("Predators", predators);
        }
        if (third > 0) {
            dataset.setValue("Third Species", third);
        }
        
        return ChartFactory.createPieChart(
            "Final Population Distribution",
            dataset,
            true,  // legend
            true,  // tooltips
            false  // urls
        );
    }
    
    /**
     * Create a bar chart comparing initial and final populations
     * @param initialCounts initial population counts
     * @param finalCounts final population counts
     * @return JFreeChart object
     */
    public static JFreeChart createComparisonBarChart(Map<String, Integer> initialCounts, 
                                                     Map<String, Integer> finalCounts) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        dataset.addValue(initialCounts.getOrDefault("prey", 0), "Initial", "Prey");
        dataset.addValue(finalCounts.getOrDefault("prey", 0), "Final", "Prey");
        
        dataset.addValue(initialCounts.getOrDefault("predator", 0), "Initial", "Predators");
        dataset.addValue(finalCounts.getOrDefault("predator", 0), "Final", "Predators");
        
        if (initialCounts.getOrDefault("third", 0) > 0 || finalCounts.getOrDefault("third", 0) > 0) {
            dataset.addValue(initialCounts.getOrDefault("third", 0), "Initial", "Third Species");
            dataset.addValue(finalCounts.getOrDefault("third", 0), "Final", "Third Species");
        }
        
        return ChartFactory.createBarChart(
            "Population Comparison",
            "Species",
            "Count",
            dataset,
            PlotOrientation.VERTICAL,
            true,  // legend
            true,  // tooltips
            false  // urls
        );
    }
}
