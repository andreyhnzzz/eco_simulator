# Eco Simulator 🌿

UTN Work from 2025 III Quarter - Ecological Simulation with JavaFX

## Description

Eco Simulator is an ecological simulation application built with JavaFX that simulates the interaction between predators, prey, and optionally a third species in a grid-based environment.

## Features

### Three Scenarios
- **Equilibrado (Balanced)**: Equal distribution of predators and prey (10% predators, 20% prey)
- **Depredadores Dominantes (Predator Dominant)**: More predators than prey (20% predators, 10% prey)
- **Presas Dominantes (Prey Dominant)**: More prey than predators (5% predators, 30% prey)

### Extensions
Each scenario can be run with optional extensions:
- **Tercer Especie (Third Species)**: Adds a third species that is an opportunistic hunter
- **Mutaciones (Mutations)**: Enables random mutations that give creatures a 50% efficiency bonus

### Automatic Turn Execution
- Turns are executed automatically using a timer
- Adjustable speed from 100ms to 2000ms between turns
- No manual "next turn" button needed

### Modern UI Design
- Material Design inspired styling with JFoenix concepts
- Smooth animations and visual effects
- Glow effect for mutated creatures
- Color-coded species visualization
- Real-time statistics display

## Requirements

- Java 17 or higher
- Maven 3.6+

## Building and Running

```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Run the application
mvn javafx:run
```

## Project Structure

```
src/main/java/com/ecosimulator/
├── EcoSimulatorApp.java      # Main application entry point
├── model/
│   ├── CellType.java         # Cell types (EMPTY, PREDATOR, PREY, THIRD_SPECIES)
│   ├── Creature.java         # Creature with position, energy, mutations
│   ├── Scenario.java         # Three scenario types
│   ├── SimulationConfig.java # Configuration builder
│   └── SimulationStats.java  # Statistics tracking
├── simulation/
│   ├── SimulationEngine.java # Core simulation logic
│   └── SimulationRunner.java # Timer-based automatic execution
├── service/
│   └── EmailService.java     # Email notifications (SMTP/Google OAuth)
└── ui/
    └── SimulationView.java   # JavaFX main view
```

## Simulation Rules

1. **Predators** hunt prey and gain energy when eating
2. **Prey** eat vegetation (always available) and reproduce quickly
3. **Third Species** is opportunistic and can hunt both prey and predators
4. Creatures lose energy each turn and die when energy reaches 0
5. Creatures with enough energy can reproduce
6. Mutated creatures have a 50% bonus to efficiency

## Email Configuration (Optional)

The application supports email notifications for simulation reports. To enable:

### SMTP Configuration
```java
emailService.configureSmtp("smtp.gmail.com", 587, "user@gmail.com", "password");
```

### Google OAuth2
1. Create a project in Google Cloud Console
2. Enable Gmail API
3. Create OAuth 2.0 credentials
4. Configure the application with client ID and secret

## Technologies Used

- **JavaFX 21** - UI Framework
- **JUnit 5** - Testing

## License

This project is part of UTN coursework for 2025 III Quarter.
