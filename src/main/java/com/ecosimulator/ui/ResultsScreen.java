package com.ecosimulator.ui;

import com.ecosimulator.auth.Session;
import com.ecosimulator.model.SimulationStats;
import com.ecosimulator.report.PDFReportGenerator;
import com.ecosimulator.service.EmailService;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Premium animated Results Screen for the Ecosystem Simulation
 * Features glassmorphism, animated charts, staggered animations, and modern UI/UX
 */
public class ResultsScreen extends StackPane {
    
    private static final Logger LOGGER = Logger.getLogger(ResultsScreen.class.getName());
    
    private final SimulationStats stats;
    private final int gridSize;
    private final int extinctionTurn;
    private final EmailService emailService;
    
    // UI Components
    private VBox mainContainer;
    private PieChart populationChart;
    private HBox statsCardsContainer;
    private VBox insightsContainer;
    private HBox actionButtonsContainer;
    
    // Particle effect container
    private Pane particleContainer;
    private List<Circle> particles = new ArrayList<>();
    private Timeline particleTimeline;
    
    // Animation timelines
    private Timeline chartBreathingAnimation;
    
    private static final int STAGGER_DELAY_MS = 100;
    
    /**
     * Create a new Results Screen
     * @param stats the simulation statistics
     * @param gridSize the grid size
     * @param extinctionTurn the turn when extinction occurred (-1 if none)
     */
    public ResultsScreen(SimulationStats stats, int gridSize, int extinctionTurn) {
        this(stats, gridSize, extinctionTurn, null);
    }
    
    /**
     * Create a new Results Screen with email service
     * @param stats the simulation statistics
     * @param gridSize the grid size
     * @param extinctionTurn the turn when extinction occurred (-1 if none)
     * @param emailService the email service for sharing results
     */
    public ResultsScreen(SimulationStats stats, int gridSize, int extinctionTurn, EmailService emailService) {
        this.stats = stats;
        this.gridSize = gridSize;
        this.extinctionTurn = extinctionTurn;
        this.emailService = emailService;
        
        initializeUI();
        applyStyles();
    }
    
    private void initializeUI() {
        // Create particle background
        particleContainer = new Pane();
        particleContainer.setMouseTransparent(true);
        particleContainer.getStyleClass().add("particle-background");
        
        // Create main scrollable container
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("results-scroll");
        
        mainContainer = new VBox(30);
        mainContainer.setPadding(new Insets(40, 50, 40, 50));
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setMaxWidth(900);
        mainContainer.getStyleClass().add("results-container");
        
        // Title section
        VBox titleSection = createTitleSection();
        
        // Outcome badge
        HBox badgeSection = createBadgeSection();
        
        // Stats cards
        statsCardsContainer = createStatsCards();
        
        // Population pie chart
        VBox chartSection = createChartSection();
        
        // Insights section
        insightsContainer = createInsightsSection();
        
        // Action buttons
        actionButtonsContainer = createActionButtons();
        
        mainContainer.getChildren().addAll(
            titleSection,
            badgeSection,
            statsCardsContainer,
            chartSection,
            insightsContainer,
            actionButtonsContainer
        );
        
        scrollPane.setContent(mainContainer);
        
        getChildren().addAll(particleContainer, scrollPane);
    }
    
    private VBox createTitleSection() {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("🏆 Simulation Complete");
        titleLabel.getStyleClass().add("results-title");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: 800;");
        
        Label subtitleLabel = new Label("Final Results & Analysis");
        subtitleLabel.getStyleClass().add("subtitle-label");
        subtitleLabel.setStyle("-fx-font-size: 16px;");
        
        section.getChildren().addAll(titleLabel, subtitleLabel);
        return section;
    }
    
    private HBox createBadgeSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);
        
        String outcome = stats.getWinner();
        Label badge = new Label(getOutcomeEmoji() + " " + outcome);
        badge.getStyleClass().addAll("results-badge", getBadgeStyleClass());
        badge.setStyle("-fx-padding: 12px 28px; -fx-font-size: 16px;");
        
        section.getChildren().add(badge);
        return section;
    }
    
    private String getOutcomeEmoji() {
        if (stats.getPredatorCount() == 0 && stats.getPreyCount() == 0) {
            return "💀";
        } else if (stats.getPredatorCount() == 0) {
            return "🐰";
        } else if (stats.getPreyCount() == 0) {
            return "🐺";
        } else {
            return "⚖️";
        }
    }
    
    private String getBadgeStyleClass() {
        if (stats.getPredatorCount() > 0 && stats.getPreyCount() > 0) {
            return "results-badge-success";
        } else if (stats.getPredatorCount() == 0 && stats.getPreyCount() == 0) {
            return "results-badge-danger";
        } else {
            return "results-badge-warning";
        }
    }
    
    private HBox createStatsCards() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);
        
        int totalCells = gridSize * gridSize;
        
        // Create stat cards
        VBox turnCard = createStatCard("🔄", "Total Turns", String.valueOf(stats.getTurn()), "#00E5FF");
        VBox predatorCard = createStatCard("🐺", "Predators", 
            stats.getPredatorCount() + " (" + getPercentage(stats.getPredatorCount(), totalCells) + "%)", 
            ThemeManager.getSpeciesColor("predator"));
        VBox preyCard = createStatCard("🐰", "Prey", 
            stats.getPreyCount() + " (" + getPercentage(stats.getPreyCount(), totalCells) + "%)", 
            ThemeManager.getSpeciesColor("prey"));
        VBox thirdCard = createStatCard("🦎", "Third Species", 
            stats.getThirdSpeciesCount() + " (" + getPercentage(stats.getThirdSpeciesCount(), totalCells) + "%)", 
            ThemeManager.getSpeciesColor("third"));
        VBox mutatedCard = createStatCard("🧬", "Mutated", 
            String.valueOf(stats.getMutatedCount()), 
            ThemeManager.getSpeciesColor("mutated"));
        
        container.getChildren().addAll(turnCard, predatorCard, preyCard, thirdCard, mutatedCard);
        return container;
    }
    
    private VBox createStatCard(String emoji, String label, String value, String accentColor) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(140);
        card.setPrefHeight(120);
        card.getStyleClass().add("results-stat-card");
        
        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 28px;");
        
        Label labelText = new Label(label);
        labelText.getStyleClass().add("results-stat-label");
        
        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: " + accentColor + ";");
        
        card.getChildren().addAll(emojiLabel, labelText, valueText);
        return card;
    }
    
    private int getPercentage(int count, int total) {
        if (total == 0) return 0;
        return Math.round((float) count / total * 100);
    }
    
    private VBox createChartSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        section.getStyleClass().add("results-card");
        section.setPadding(new Insets(25));
        
        Label chartTitle = new Label("📊 Population Distribution");
        chartTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 700;");
        
        populationChart = new PieChart();
        populationChart.setTitle("");
        populationChart.setLegendVisible(true);
        populationChart.setPrefSize(400, 300);
        populationChart.setLabelsVisible(true);
        
        // Add data with proper names
        if (stats.getPredatorCount() > 0) {
            populationChart.getData().add(new PieChart.Data("Predators 🐺", stats.getPredatorCount()));
        }
        if (stats.getPreyCount() > 0) {
            populationChart.getData().add(new PieChart.Data("Prey 🐰", stats.getPreyCount()));
        }
        if (stats.getThirdSpeciesCount() > 0) {
            populationChart.getData().add(new PieChart.Data("Third Species 🦎", stats.getThirdSpeciesCount()));
        }
        
        // If all zero, show "No survivors"
        if (populationChart.getData().isEmpty()) {
            populationChart.getData().add(new PieChart.Data("No Survivors 💀", 1));
        }
        
        section.getChildren().addAll(chartTitle, populationChart);
        return section;
    }
    
    private VBox createInsightsSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER_LEFT);
        section.getStyleClass().add("results-card");
        section.setPadding(new Insets(25));
        
        Label insightTitle = new Label("💡 Simulation Insights");
        insightTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 700;");
        
        VBox insightsBox = new VBox(12);
        insightsBox.setAlignment(Pos.CENTER_LEFT);
        
        // Generate insights based on results
        List<String> insights = generateInsights();
        for (String insight : insights) {
            Label insightLabel = new Label("• " + insight);
            insightLabel.getStyleClass().add("results-insight");
            insightLabel.setWrapText(true);
            insightLabel.setMaxWidth(700);
            insightsBox.getChildren().add(insightLabel);
        }
        
        section.getChildren().addAll(insightTitle, insightsBox);
        return section;
    }
    
    private List<String> generateInsights() {
        List<String> insights = new ArrayList<>();
        
        // Extinction analysis
        if (extinctionTurn > 0) {
            insights.add("Extinction occurred at turn " + extinctionTurn + ".");
        }
        
        // Population balance
        if (stats.getPredatorCount() > 0 && stats.getPreyCount() > 0) {
            double ratio = (double) stats.getPredatorCount() / stats.getPreyCount();
            if (ratio > 2) {
                insights.add("Predator-to-prey ratio is high. Prey may struggle to survive longer.");
            } else if (ratio < 0.5) {
                insights.add("Prey outnumber predators significantly. The ecosystem is prey-dominated.");
            } else {
                insights.add("A relatively balanced predator-prey ratio indicates ecosystem stability.");
            }
        }
        
        // Third species impact
        if (stats.getThirdSpeciesCount() > 0) {
            int totalPop = stats.getPredatorCount() + stats.getPreyCount() + stats.getThirdSpeciesCount();
            double thirdRatio = (double) stats.getThirdSpeciesCount() / totalPop * 100;
            if (thirdRatio > 30) {
                insights.add("The third species has a strong presence (" + Math.round(thirdRatio) + "% of population).");
            } else {
                insights.add("The third species maintains a moderate presence in the ecosystem.");
            }
        }
        
        // Mutation impact
        if (stats.getMutatedCount() > 0) {
            insights.add(stats.getMutatedCount() + " creatures have mutations, potentially giving them survival advantages.");
        }
        
        // Duration analysis
        if (stats.getTurn() >= 100) {
            insights.add("The simulation ran for " + stats.getTurn() + " turns, showing ecosystem resilience.");
        } else if (stats.getTurn() < 30) {
            insights.add("The simulation ended quickly (" + stats.getTurn() + " turns), indicating rapid extinction events.");
        }
        
        // Final outcome
        String outcome = stats.getWinner();
        if (outcome.contains("Predators win")) {
            insights.add("Predators successfully eliminated all prey, demonstrating hunting efficiency.");
        } else if (outcome.contains("Prey win")) {
            insights.add("Prey successfully outlasted predators, possibly through high reproduction rates.");
        } else if (outcome.contains("No survivors")) {
            insights.add("Complete ecosystem collapse occurred. Both species went extinct.");
        }
        
        return insights;
    }
    
    private HBox createActionButtons() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20, 0, 0, 0));
        
        // Share Results button
        Button shareButton = new Button("📤 Share Results");
        shareButton.getStyleClass().addAll("action-button", "share-button");
        shareButton.setOnAction(e -> {
            AnimationUtils.playButtonClickAnimation(shareButton);
            shareResults();
        });
        AnimationUtils.applyButtonHoverAnimation(shareButton);
        
        // Export PDF button
        Button exportButton = new Button("📄 Export PDF");
        exportButton.getStyleClass().addAll("action-button", "reset-button");
        exportButton.setOnAction(e -> {
            AnimationUtils.playButtonClickAnimation(exportButton);
            exportToPDF();
        });
        AnimationUtils.applyButtonHoverAnimation(exportButton);
        
        // Close button
        Button closeButton = new Button("✖ Close");
        closeButton.getStyleClass().addAll("action-button", "settings-button");
        closeButton.setOnAction(e -> {
            AnimationUtils.playButtonClickAnimation(closeButton);
            closeScreen();
        });
        AnimationUtils.applyButtonHoverAnimation(closeButton);
        
        container.getChildren().addAll(shareButton, exportButton, closeButton);
        return container;
    }
    
    private void applyStyles() {
        getStyleClass().add("results-container");
    }
    
    /**
     * Play all entrance animations for the results screen
     */
    public void playEntranceAnimations() {
        // Start particle animation
        startParticleAnimation();
        
        // Animate children with staggered delay
        List<Node> animatableNodes = new ArrayList<>();
        animatableNodes.addAll(mainContainer.getChildren());
        
        for (int i = 0; i < animatableNodes.size(); i++) {
            Node node = animatableNodes.get(i);
            node.setOpacity(0);
            node.setTranslateY(30);
            
            int delay = i * STAGGER_DELAY_MS;
            PauseTransition pause = new PauseTransition(Duration.millis(delay));
            pause.setOnFinished(e -> playNodeEntranceAnimation(node));
            pause.play();
        }
        
        // Play chart sweep animation after a delay
        PauseTransition chartDelay = new PauseTransition(Duration.millis(animatableNodes.size() * STAGGER_DELAY_MS));
        chartDelay.setOnFinished(e -> {
            playChartSweepAnimation();
            startChartBreathingAnimation();
        });
        chartDelay.play();
        
        // Check for balance achievement and play confetti
        if (stats.getPredatorCount() > 0 && stats.getPreyCount() > 0) {
            PauseTransition confettiDelay = new PauseTransition(Duration.millis(800));
            confettiDelay.setOnFinished(e -> playBalanceConfetti());
            confettiDelay.play();
        }
    }
    
    private void playNodeEntranceAnimation(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(300), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        TranslateTransition translate = new TranslateTransition(Duration.millis(300), node);
        translate.setFromY(30);
        translate.setToY(0);
        translate.setInterpolator(AnimationUtils.EASE_OUT_CUBIC);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), node);
        scale.setFromX(0.95);
        scale.setFromY(0.95);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(AnimationUtils.EASE_OUT_CUBIC);
        
        new ParallelTransition(fade, translate, scale).play();
    }
    
    private void playChartSweepAnimation() {
        if (populationChart != null) {
            // Rotate the chart for sweep effect
            populationChart.setRotate(-90);
            populationChart.setOpacity(0);
            
            RotateTransition rotate = new RotateTransition(Duration.millis(800), populationChart);
            rotate.setFromAngle(-90);
            rotate.setToAngle(0);
            rotate.setInterpolator(AnimationUtils.EASE_OUT_CUBIC);
            
            FadeTransition fade = new FadeTransition(Duration.millis(600), populationChart);
            fade.setFromValue(0);
            fade.setToValue(1);
            
            new ParallelTransition(rotate, fade).play();
        }
    }
    
    private void startChartBreathingAnimation() {
        if (populationChart != null) {
            Glow glow = new Glow(0);
            populationChart.setEffect(glow);
            
            chartBreathingAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0)),
                new KeyFrame(Duration.millis(1500), new KeyValue(glow.levelProperty(), 0.15)),
                new KeyFrame(Duration.millis(3000), new KeyValue(glow.levelProperty(), 0))
            );
            chartBreathingAnimation.setCycleCount(Animation.INDEFINITE);
            chartBreathingAnimation.play();
        }
    }
    
    private void startParticleAnimation() {
        Random random = new Random();
        
        // Pre-defined particle colors for better performance
        Color[] particleColors = {
            Color.rgb(0, 229, 255, 0.3),
            Color.rgb(0, 229, 255, 0.4),
            Color.rgb(0, 229, 255, 0.5),
            Color.rgb(187, 134, 252, 0.3),
            Color.rgb(187, 134, 252, 0.4)
        };
        
        // Create initial particles (reduced count for performance)
        for (int i = 0; i < 20; i++) {
            Circle particle = new Circle(2 + random.nextDouble() * 3);
            particle.setFill(particleColors[random.nextInt(particleColors.length)]);
            particle.setCenterX(random.nextDouble() * 900);
            particle.setCenterY(random.nextDouble() * 700);
            particles.add(particle);
            particleContainer.getChildren().add(particle);
        }
        
        // Animate particles with longer interval for better performance
        particleTimeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            for (Circle particle : particles) {
                particle.setCenterY(particle.getCenterY() - 0.8 - random.nextDouble() * 0.5);
                particle.setCenterX(particle.getCenterX() + (random.nextDouble() - 0.5) * 0.8);
                
                // Reset particle when it goes off screen
                if (particle.getCenterY() < -10) {
                    particle.setCenterY(700 + random.nextDouble() * 50);
                    particle.setCenterX(random.nextDouble() * 900);
                }
            }
        }));
        particleTimeline.setCycleCount(Animation.INDEFINITE);
        particleTimeline.play();
    }
    
    private void playBalanceConfetti() {
        Random random = new Random();
        // Pre-computed colors for better performance
        Color[] confettiColors = {
            Color.web("#69F0AE"),
            Color.web("#00E5FF"),
            Color.web("#BB86FC"),
            Color.web("#FFD740"),
            Color.web("#FF4081")
        };
        
        // Reduced confetti count for better performance
        for (int i = 0; i < 35; i++) {
            Circle confetti = new Circle(3 + random.nextDouble() * 4);
            confetti.setFill(confettiColors[random.nextInt(confettiColors.length)]);
            confetti.setCenterX(400 + (random.nextDouble() - 0.5) * 100);
            confetti.setCenterY(100);
            confetti.setOpacity(0.8);
            
            particleContainer.getChildren().add(confetti);
            
            // Animate confetti falling
            TranslateTransition fall = new TranslateTransition(Duration.millis(1500 + random.nextDouble() * 1000), confetti);
            fall.setFromY(0);
            fall.setToY(500 + random.nextDouble() * 200);
            fall.setFromX(0);
            fall.setToX((random.nextDouble() - 0.5) * 400);
            
            FadeTransition fade = new FadeTransition(Duration.millis(2000), confetti);
            fade.setFromValue(0.8);
            fade.setToValue(0);
            fade.setDelay(Duration.millis(500));
            
            RotateTransition rotate = new RotateTransition(Duration.millis(2000), confetti);
            rotate.setByAngle(360 * (random.nextBoolean() ? 1 : -1));
            
            ParallelTransition animation = new ParallelTransition(fall, fade, rotate);
            animation.setDelay(Duration.millis(random.nextDouble() * 300));
            animation.setOnFinished(e -> particleContainer.getChildren().remove(confetti));
            animation.play();
        }
    }
    
    private void shareResults() {
        if (emailService == null || !Session.isLoggedIn()) {
            showNotification("Cannot Share", "Please log in and configure email settings to share results.", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            // Generate PDF first
            String reportFilename = PDFReportGenerator.getDefaultFilename();
            Path reportsDir = Paths.get("reports");
            
            if (!Files.exists(reportsDir)) {
                Files.createDirectories(reportsDir);
            }
            
            String reportPath = reportsDir.resolve(reportFilename).toString();
            PDFReportGenerator.generateSimpleReport(reportPath, stats.getTurn(), stats, gridSize, extinctionTurn);
            
            File reportFile = new File(reportPath);
            
            // Send email
            var currentUser = Session.getUser();
            if (currentUser != null && currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                String subject = "Eco Simulator - Simulation Results";
                String body = buildEmailBody();
                
                boolean success = emailService.sendReport(currentUser.getEmail(), reportFile, subject, body);
                
                if (success) {
                    showNotification("Success! 📧", "Results sent to " + currentUser.getEmail(), Alert.AlertType.INFORMATION);
                } else {
                    showNotification("Email Failed", "Could not send email. Check SMTP settings.", Alert.AlertType.ERROR);
                }
            } else {
                showNotification("No Email", "Please configure your email in user settings.", Alert.AlertType.WARNING);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to share results", e);
            showNotification("Error", "Could not generate or send report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private String buildEmailBody() {
        StringBuilder sb = new StringBuilder();
        sb.append("Hello!\n\n");
        sb.append("Here are your Eco Simulator results:\n\n");
        sb.append("📊 FINAL STATISTICS\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("Total Turns: ").append(stats.getTurn()).append("\n");
        sb.append("Predators: ").append(stats.getPredatorCount()).append("\n");
        sb.append("Prey: ").append(stats.getPreyCount()).append("\n");
        sb.append("Third Species: ").append(stats.getThirdSpeciesCount()).append("\n");
        sb.append("Mutated: ").append(stats.getMutatedCount()).append("\n\n");
        sb.append("Result: ").append(stats.getWinner()).append("\n\n");
        sb.append("See the attached PDF for detailed analysis.\n\n");
        sb.append("Best regards,\nEco Simulator 🌿");
        return sb.toString();
    }
    
    private void exportToPDF() {
        try {
            String reportFilename = PDFReportGenerator.getDefaultFilename();
            Path reportsDir = Paths.get("reports");
            
            if (!Files.exists(reportsDir)) {
                Files.createDirectories(reportsDir);
            }
            
            String reportPath = reportsDir.resolve(reportFilename).toString();
            PDFReportGenerator.generateSimpleReport(reportPath, stats.getTurn(), stats, gridSize, extinctionTurn);
            
            showNotification("PDF Exported! 📄", "Report saved to: " + reportPath, Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to export PDF", e);
            showNotification("Export Failed", "Could not generate PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void closeScreen() {
        // Stop animations
        if (chartBreathingAnimation != null) {
            chartBreathingAnimation.stop();
        }
        if (particleTimeline != null) {
            particleTimeline.stop();
        }
        
        // Play exit animation
        FadeTransition fade = new FadeTransition(Duration.millis(200), this);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            if (getScene() != null && getScene().getWindow() instanceof Stage) {
                ((Stage) getScene().getWindow()).close();
            }
        });
        fade.play();
    }
    
    private void showNotification(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Apply current theme to dialog
        if (alert.getDialogPane().getScene() != null) {
            ThemeManager.applyCurrentTheme(alert.getDialogPane().getScene());
        }
        
        alert.showAndWait();
    }
    
    /**
     * Show the results screen as a modal dialog
     * @param owner the owner stage
     * @param stats the simulation statistics
     * @param gridSize the grid size
     * @param extinctionTurn the extinction turn (-1 if none)
     * @param emailService optional email service
     */
    public static void showResultsDialog(Stage owner, SimulationStats stats, int gridSize, 
                                          int extinctionTurn, EmailService emailService) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Simulation Results");
        
        ResultsScreen resultsScreen = new ResultsScreen(stats, gridSize, extinctionTurn, emailService);
        
        Scene scene = new Scene(resultsScreen, 900, 700);
        ThemeManager.applyCurrentTheme(scene);
        
        dialog.setScene(scene);
        dialog.centerOnScreen();
        
        // Play entrance animations after showing
        dialog.setOnShown(e -> resultsScreen.playEntranceAnimations());
        
        dialog.showAndWait();
    }
    
    /**
     * Show the results screen as a modal dialog (simpler version)
     * @param owner the owner stage
     * @param stats the simulation statistics
     * @param gridSize the grid size
     * @param extinctionTurn the extinction turn (-1 if none)
     */
    public static void showResultsDialog(Stage owner, SimulationStats stats, int gridSize, int extinctionTurn) {
        showResultsDialog(owner, stats, gridSize, extinctionTurn, null);
    }
}
