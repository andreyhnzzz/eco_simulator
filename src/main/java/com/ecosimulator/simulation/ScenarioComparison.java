package com.ecosimulator.simulation;

import com.ecosimulator.model.Scenario;
import com.ecosimulator.model.SimulationConfig;
import com.ecosimulator.model.SimulationStats;

import java.util.*;

/**
 * Scenario Comparison Module for analyzing and comparing simulation results.
 * Provides algorithmic analysis of different ecosystem scenarios.
 */
public class ScenarioComparison {
    
    /**
     * Results from a single simulation run
     */
    public static class SimulationResult {
        private final Scenario scenario;
        private final boolean thirdSpeciesEnabled;
        private final boolean mutationsEnabled;
        private final int totalTurns;
        private final int finalPredators;
        private final int finalPrey;
        private final int finalThirdSpecies;
        private final int extinctionTurn; // -1 if no extinction
        private final boolean equilibriumMaintained;
        private final List<Integer> predatorHistory;
        private final List<Integer> preyHistory;
        
        public SimulationResult(Scenario scenario, boolean thirdSpeciesEnabled, boolean mutationsEnabled,
                               int totalTurns, int finalPredators, int finalPrey, int finalThirdSpecies,
                               int extinctionTurn, List<Integer> predatorHistory, List<Integer> preyHistory) {
            this.scenario = scenario;
            this.thirdSpeciesEnabled = thirdSpeciesEnabled;
            this.mutationsEnabled = mutationsEnabled;
            this.totalTurns = totalTurns;
            this.finalPredators = finalPredators;
            this.finalPrey = finalPrey;
            this.finalThirdSpecies = finalThirdSpecies;
            this.extinctionTurn = extinctionTurn;
            this.predatorHistory = new ArrayList<>(predatorHistory);
            this.preyHistory = new ArrayList<>(preyHistory);
            this.equilibriumMaintained = extinctionTurn < 0 && finalPredators > 0 && finalPrey > 0;
        }
        
        public Scenario getScenario() { return scenario; }
        public boolean isThirdSpeciesEnabled() { return thirdSpeciesEnabled; }
        public boolean isMutationsEnabled() { return mutationsEnabled; }
        public int getTotalTurns() { return totalTurns; }
        public int getFinalPredators() { return finalPredators; }
        public int getFinalPrey() { return finalPrey; }
        public int getFinalThirdSpecies() { return finalThirdSpecies; }
        public int getExtinctionTurn() { return extinctionTurn; }
        public boolean isEquilibriumMaintained() { return equilibriumMaintained; }
        public List<Integer> getPredatorHistory() { return Collections.unmodifiableList(predatorHistory); }
        public List<Integer> getPreyHistory() { return Collections.unmodifiableList(preyHistory); }
        
        public int getTotalOccupancy() {
            return finalPredators + finalPrey + finalThirdSpecies;
        }
        
        public String getConfigDescription() {
            StringBuilder sb = new StringBuilder(scenario.getDisplayName());
            if (thirdSpeciesEnabled) sb.append(" + Third Species");
            if (mutationsEnabled) sb.append(" + Mutations");
            return sb.toString();
        }
    }
    
    /**
     * Comparison analysis results
     */
    public static class ComparisonAnalysis {
        private final List<SimulationResult> results;
        private SimulationResult mostStable;
        private SimulationResult fastestExtinction;
        private SimulationResult highestOccupancy;
        private String analysisReport;
        
        public ComparisonAnalysis(List<SimulationResult> results) {
            this.results = new ArrayList<>(results);
            performAnalysis();
        }
        
        private void performAnalysis() {
            if (results.isEmpty()) {
                analysisReport = "No simulation results to analyze.";
                return;
            }
            
            StringBuilder report = new StringBuilder();
            report.append("========================================\n");
            report.append("  SCENARIO COMPARISON ANALYSIS\n");
            report.append("========================================\n\n");
            
            // Find most stable scenario (longest without extinction or maintained equilibrium)
            mostStable = null;
            int longestSurvival = 0;
            for (SimulationResult result : results) {
                int survivalScore = result.equilibriumMaintained ? 
                    result.totalTurns + 1000 : // Bonus for maintaining equilibrium
                    (result.extinctionTurn > 0 ? result.extinctionTurn : result.totalTurns);
                if (survivalScore > longestSurvival) {
                    longestSurvival = survivalScore;
                    mostStable = result;
                }
            }
            
            // Find fastest extinction
            fastestExtinction = null;
            int fastestTurn = Integer.MAX_VALUE;
            for (SimulationResult result : results) {
                if (result.extinctionTurn > 0 && result.extinctionTurn < fastestTurn) {
                    fastestTurn = result.extinctionTurn;
                    fastestExtinction = result;
                }
            }
            
            // Find highest occupancy
            highestOccupancy = null;
            int maxOccupancy = 0;
            for (SimulationResult result : results) {
                if (result.getTotalOccupancy() > maxOccupancy) {
                    maxOccupancy = result.getTotalOccupancy();
                    highestOccupancy = result;
                }
            }
            
            // Generate report sections
            report.append("--- EQUILIBRIUM ANALYSIS ---\n");
            int equilibriumCount = 0;
            for (SimulationResult result : results) {
                if (result.equilibriumMaintained) {
                    equilibriumCount++;
                    report.append(String.format("✓ %s: Equilibrium maintained (P:%d, R:%d)\n",
                        result.getConfigDescription(), result.finalPredators, result.finalPrey));
                } else {
                    report.append(String.format("✗ %s: Equilibrium NOT maintained\n",
                        result.getConfigDescription()));
                }
            }
            report.append(String.format("\nEquilibrium success rate: %d/%d scenarios\n\n", 
                equilibriumCount, results.size()));
            
            // Extinction analysis
            report.append("--- EXTINCTION ANALYSIS ---\n");
            for (SimulationResult result : results) {
                if (result.extinctionTurn > 0) {
                    String extinct = result.finalPredators == 0 && result.finalPrey == 0 ? 
                        "Both species" : (result.finalPredators == 0 ? "Predators" : "Prey");
                    report.append(String.format("%s: %s extinct at turn %d\n",
                        result.getConfigDescription(), extinct, result.extinctionTurn));
                } else {
                    report.append(String.format("%s: No extinction\n", result.getConfigDescription()));
                }
            }
            report.append("\n");
            
            // Stability ranking
            report.append("--- STABILITY RANKING ---\n");
            List<SimulationResult> sortedResults = new ArrayList<>(results);
            sortedResults.sort((a, b) -> {
                // Priority: equilibrium maintained > longer survival > higher population
                if (a.equilibriumMaintained != b.equilibriumMaintained) {
                    return a.equilibriumMaintained ? -1 : 1;
                }
                int survivalA = a.extinctionTurn > 0 ? a.extinctionTurn : a.totalTurns;
                int survivalB = b.extinctionTurn > 0 ? b.extinctionTurn : b.totalTurns;
                if (survivalA != survivalB) {
                    return survivalB - survivalA;
                }
                return b.getTotalOccupancy() - a.getTotalOccupancy();
            });
            
            for (int i = 0; i < sortedResults.size(); i++) {
                SimulationResult r = sortedResults.get(i);
                report.append(String.format("%d. %s - Score: %d (Equilibrium: %s)\n",
                    i + 1, r.getConfigDescription(),
                    calculateStabilityScore(r),
                    r.equilibriumMaintained ? "Yes" : "No"));
            }
            report.append("\n");
            
            // Population trends
            report.append("--- POPULATION TRENDS ---\n");
            for (SimulationResult result : results) {
                String trend = analyzePopulationTrend(result);
                report.append(String.format("%s: %s\n", result.getConfigDescription(), trend));
            }
            report.append("\n");
            
            // Key factors analysis
            report.append("--- KEY FACTORS ANALYSIS ---\n");
            report.append(analyzeKeyFactors());
            report.append("\n");
            
            // Conclusions
            report.append("--- CONCLUSIONS ---\n");
            if (mostStable != null) {
                report.append(String.format("• Most Stable: %s\n", mostStable.getConfigDescription()));
            }
            if (fastestExtinction != null) {
                report.append(String.format("• Fastest Extinction: %s (turn %d)\n", 
                    fastestExtinction.getConfigDescription(), fastestExtinction.extinctionTurn));
            }
            if (highestOccupancy != null) {
                report.append(String.format("• Highest Occupancy: %s (%d creatures)\n",
                    highestOccupancy.getConfigDescription(), highestOccupancy.getTotalOccupancy()));
            }
            
            // Initial population vs reproduction analysis
            report.append("\n--- INFLUENCE ANALYSIS ---\n");
            report.append(analyzeInfluenceFactors());
            
            report.append("\n========================================\n");
            
            this.analysisReport = report.toString();
        }
        
        private int calculateStabilityScore(SimulationResult result) {
            int score = 0;
            if (result.equilibriumMaintained) {
                score += 1000;
            }
            score += result.totalTurns;
            if (result.extinctionTurn < 0) {
                score += 500;
            }
            score += result.getTotalOccupancy() * 2;
            return score;
        }
        
        private String analyzePopulationTrend(SimulationResult result) {
            List<Integer> predHist = result.getPredatorHistory();
            List<Integer> preyHist = result.getPreyHistory();
            
            if (predHist.isEmpty() || preyHist.isEmpty()) {
                return "Insufficient data";
            }
            
            // Calculate average change
            double predTrend = calculateTrend(predHist);
            double preyTrend = calculateTrend(preyHist);
            
            StringBuilder sb = new StringBuilder();
            sb.append("Predators: ");
            if (predTrend > 0.5) sb.append("Growing ↑");
            else if (predTrend < -0.5) sb.append("Declining ↓");
            else sb.append("Stable ↔");
            
            sb.append(" | Prey: ");
            if (preyTrend > 0.5) sb.append("Growing ↑");
            else if (preyTrend < -0.5) sb.append("Declining ↓");
            else sb.append("Stable ↔");
            
            return sb.toString();
        }
        
        private double calculateTrend(List<Integer> history) {
            if (history.size() < 2) return 0;
            
            // Compare first third with last third
            int thirdSize = Math.max(1, history.size() / 3);
            double firstThirdAvg = 0;
            double lastThirdAvg = 0;
            
            for (int i = 0; i < thirdSize; i++) {
                firstThirdAvg += history.get(i);
            }
            firstThirdAvg /= thirdSize;
            
            for (int i = history.size() - thirdSize; i < history.size(); i++) {
                lastThirdAvg += history.get(i);
            }
            lastThirdAvg /= thirdSize;
            
            return lastThirdAvg - firstThirdAvg;
        }
        
        private String analyzeKeyFactors() {
            StringBuilder sb = new StringBuilder();
            
            // Compare scenarios with and without third species
            int withThirdStable = 0;
            int withoutThirdStable = 0;
            int withThirdCount = 0;
            int withoutThirdCount = 0;
            
            for (SimulationResult r : results) {
                if (r.isThirdSpeciesEnabled()) {
                    withThirdCount++;
                    if (r.equilibriumMaintained) withThirdStable++;
                } else {
                    withoutThirdCount++;
                    if (r.equilibriumMaintained) withoutThirdStable++;
                }
            }
            
            if (withThirdCount > 0 && withoutThirdCount > 0) {
                double withThirdRate = (double) withThirdStable / withThirdCount;
                double withoutThirdRate = (double) withoutThirdStable / withoutThirdCount;
                
                if (withThirdRate > withoutThirdRate + 0.2) {
                    sb.append("• Third species INCREASES ecosystem stability\n");
                } else if (withThirdRate < withoutThirdRate - 0.2) {
                    sb.append("• Third species DECREASES ecosystem stability\n");
                } else {
                    sb.append("• Third species has NEUTRAL effect on stability\n");
                }
            }
            
            // Compare scenarios with and without mutations
            int withMutStable = 0;
            int withoutMutStable = 0;
            int withMutCount = 0;
            int withoutMutCount = 0;
            
            for (SimulationResult r : results) {
                if (r.isMutationsEnabled()) {
                    withMutCount++;
                    if (r.equilibriumMaintained) withMutStable++;
                } else {
                    withoutMutCount++;
                    if (r.equilibriumMaintained) withoutMutStable++;
                }
            }
            
            if (withMutCount > 0 && withoutMutCount > 0) {
                double withMutRate = (double) withMutStable / withMutCount;
                double withoutMutRate = (double) withoutMutStable / withoutMutCount;
                
                if (withMutRate > withoutMutRate + 0.2) {
                    sb.append("• Mutations INCREASE ecosystem stability\n");
                } else if (withMutRate < withoutMutRate - 0.2) {
                    sb.append("• Mutations DECREASE ecosystem stability\n");
                } else {
                    sb.append("• Mutations have NEUTRAL effect on stability\n");
                }
            }
            
            return sb.toString();
        }
        
        private String analyzeInfluenceFactors() {
            StringBuilder sb = new StringBuilder();
            
            // Analyze if initial population or reproduction rules had more influence
            Map<Scenario, List<SimulationResult>> byScenario = new HashMap<>();
            for (SimulationResult r : results) {
                byScenario.computeIfAbsent(r.getScenario(), k -> new ArrayList<>()).add(r);
            }
            
            // Check variance within same scenario (reproduction effect)
            double withinScenarioVariance = 0;
            int withinCount = 0;
            for (List<SimulationResult> scenarioResults : byScenario.values()) {
                if (scenarioResults.size() > 1) {
                    for (int i = 0; i < scenarioResults.size() - 1; i++) {
                        for (int j = i + 1; j < scenarioResults.size(); j++) {
                            int survivalDiff = Math.abs(
                                getSurvivalScore(scenarioResults.get(i)) - 
                                getSurvivalScore(scenarioResults.get(j)));
                            withinScenarioVariance += survivalDiff;
                            withinCount++;
                        }
                    }
                }
            }
            if (withinCount > 0) withinScenarioVariance /= withinCount;
            
            // Check variance between scenarios (initial population effect)
            double betweenScenarioVariance = 0;
            int betweenCount = 0;
            List<Scenario> scenarios = new ArrayList<>(byScenario.keySet());
            for (int i = 0; i < scenarios.size() - 1; i++) {
                for (int j = i + 1; j < scenarios.size(); j++) {
                    double avgSurvival1 = byScenario.get(scenarios.get(i)).stream()
                        .mapToInt(this::getSurvivalScore).average().orElse(0);
                    double avgSurvival2 = byScenario.get(scenarios.get(j)).stream()
                        .mapToInt(this::getSurvivalScore).average().orElse(0);
                    betweenScenarioVariance += Math.abs(avgSurvival1 - avgSurvival2);
                    betweenCount++;
                }
            }
            if (betweenCount > 0) betweenScenarioVariance /= betweenCount;
            
            if (betweenScenarioVariance > withinScenarioVariance * 1.5) {
                sb.append("• Initial population (scenario) has MORE influence on outcomes\n");
            } else if (withinScenarioVariance > betweenScenarioVariance * 1.5) {
                sb.append("• Reproduction/mutation rules have MORE influence on outcomes\n");
            } else {
                sb.append("• Both initial population and reproduction rules have SIMILAR influence\n");
            }
            
            return sb.toString();
        }
        
        private int getSurvivalScore(SimulationResult r) {
            return r.extinctionTurn > 0 ? r.extinctionTurn : r.totalTurns + 100;
        }
        
        // Getters
        public List<SimulationResult> getResults() { return Collections.unmodifiableList(results); }
        public SimulationResult getMostStable() { return mostStable; }
        public SimulationResult getFastestExtinction() { return fastestExtinction; }
        public SimulationResult getHighestOccupancy() { return highestOccupancy; }
        public String getAnalysisReport() { return analysisReport; }
    }
    
    /**
     * Run simulations for all scenario combinations and generate comparison analysis.
     * This method runs quick simulations (reduced turns) for comparison.
     * @param gridSize size of the simulation grid
     * @param turnsPerSimulation turns to run each simulation
     * @return ComparisonAnalysis with all results
     */
    public static ComparisonAnalysis runComparisonSimulations(int gridSize, int turnsPerSimulation) {
        List<SimulationResult> results = new ArrayList<>();
        
        // Run all 12 combinations: 3 scenarios × 4 extension modes
        for (Scenario scenario : Scenario.values()) {
            // Mode 1: No extensions
            results.add(runSingleSimulation(scenario, false, false, gridSize, turnsPerSimulation));
            
            // Mode 2: Third species only
            results.add(runSingleSimulation(scenario, true, false, gridSize, turnsPerSimulation));
            
            // Mode 3: Mutations only
            results.add(runSingleSimulation(scenario, false, true, gridSize, turnsPerSimulation));
            
            // Mode 4: Both extensions
            results.add(runSingleSimulation(scenario, true, true, gridSize, turnsPerSimulation));
        }
        
        return new ComparisonAnalysis(results);
    }
    
    /**
     * Run a single simulation and collect results
     */
    private static SimulationResult runSingleSimulation(Scenario scenario, boolean thirdSpecies,
                                                         boolean mutations, int gridSize, int maxTurns) {
        SimulationConfig config = new SimulationConfig()
            .withScenario(scenario)
            .withThirdSpecies(thirdSpecies)
            .withMutations(mutations)
            .withGridSize(gridSize)
            .withMaxTurns(maxTurns);
        
        SimulationEngine engine = new SimulationEngine(config);
        
        List<Integer> predatorHistory = new ArrayList<>();
        List<Integer> preyHistory = new ArrayList<>();
        int extinctionTurn = -1;
        
        // Record initial state
        predatorHistory.add(engine.getStats().getPredatorCount());
        preyHistory.add(engine.getStats().getPreyCount());
        
        // Run simulation
        engine.start();
        for (int turn = 1; turn <= maxTurns; turn++) {
            engine.executeTurn();
            
            SimulationStats stats = engine.getStats();
            predatorHistory.add(stats.getPredatorCount());
            preyHistory.add(stats.getPreyCount());
            
            // Check for extinction
            if (extinctionTurn < 0 && stats.isExtinct()) {
                extinctionTurn = turn;
                break;
            }
        }
        engine.stop();
        
        SimulationStats finalStats = engine.getStats();
        return new SimulationResult(
            scenario,
            thirdSpecies,
            mutations,
            finalStats.getTurn(),
            finalStats.getPredatorCount(),
            finalStats.getPreyCount(),
            finalStats.getThirdSpeciesCount(),
            extinctionTurn,
            predatorHistory,
            preyHistory
        );
    }
    
    /**
     * Generate a summary comparison table as a string
     */
    public static String generateSummaryTable(ComparisonAnalysis analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-35s | %5s | %5s | %5s | %7s | %s%n",
            "Configuration", "Pred", "Prey", "Third", "Extinct", "Equilibrium"));
        sb.append("─".repeat(80)).append("\n");
        
        for (SimulationResult r : analysis.getResults()) {
            sb.append(String.format("%-35s | %5d | %5d | %5d | %7s | %s%n",
                r.getConfigDescription(),
                r.getFinalPredators(),
                r.getFinalPrey(),
                r.getFinalThirdSpecies(),
                r.getExtinctionTurn() > 0 ? "T" + r.getExtinctionTurn() : "No",
                r.isEquilibriumMaintained() ? "✓" : "✗"));
        }
        
        return sb.toString();
    }
}
