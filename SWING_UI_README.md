# Eco Simulator - Swing UI Implementation

This document describes the **Swing-based** implementation of the Ecosystem Simulator as specified in the project requirements.

## Overview

The Eco Simulator is a Java-based ecosystem simulation that models the interactions between predators, prey, and optionally a third species in a 10x10 grid environment. This implementation uses **Swing (javax.swing)** for the GUI and follows the exact requirements specified in the project documentation.

## Technology Stack

- **Language**: Java 17 (JDK 11+ compatible)
- **Build Tool**: Maven 3.6+
- **GUI Framework**: Swing (javax.swing)
- **Timer**: javax.swing.Timer for automatic turn scheduling
- **Dependencies**:
  - JFreeChart 1.5.6 (charts)
  - Apache PDFBox 3.0.6 (PDF generation)
  - BCrypt 0.10.2 (password hashing)
  - GSON 2.10.1 (JSON parsing)
  - Jakarta Mail 2.0.3 (email sending)

## Project Structure

```
src/main/java/com/ecosimulator/
├── model/
│   ├── User.java                    # User model with cedula, nombre, fechaNacimiento, genero
│   ├── Animal.java                  # Abstract base class for all animals
│   ├── Prey.java                    # Prey implementation
│   ├── Predator.java                # Predator implementation
│   ├── ThirdSpecies.java            # Third species (optional extension)
│   └── Cell.java                    # Grid cell representation
├── core/
│   ├── Ecosystem.java               # 10x10 matrix ecosystem management
│   ├── Simulator.java               # Core simulation engine
│   ├── Scheduler.java               # javax.swing.Timer-based turn scheduler
│   └── ScenarioConfig.java          # JSON scenario loader
├── persistence/
│   ├── UserRepository.java          # User persistence (users.txt)
│   └── EcosystemRepository.java     # Ecosystem state persistence
├── ui/
│   ├── LoginFrame.java              # Login screen
│   ├── RegistroFrame.java           # User registration screen
│   ├── MainFrame.java               # Main application window
│   ├── EcosystemPanel.java          # 10x10 grid visualization
│   ├── ControlsPanel.java           # Simulation controls
│   └── ReportPanel.java             # Statistics and reports
└── util/
    ├── CryptoUtil.java              # PBKDF2/BCrypt password hashing
    ├── PdfUtil.java                 # PDF report generation
    ├── MailSender.java              # Email sending
    └── ChartUtil.java               # Chart generation
```

## File Formats

### users.txt
Format: `cedula|nombre|fechaNacimiento(YYYY-MM-DD)|genero|email|salt|passwordHash`

Example:
```
123456789|Juan Pérez|1990-05-15|Masculino|juan@email.com|base64salt|base64hash
987654321|María García|1995-08-20|Femenino|maria@email.com|base64salt|base64hash
```

- **Never stores passwords in plain text**
- Each user has a unique salt
- Passwords are hashed using PBKDF2WithHmacSHA256 (65536 iterations, 256-bit key)

### ecosystem.txt
JSON format representing the initial ecosystem state:
```json
{
  "scenario": "balanced_scenario",
  "gridSize": 10,
  "timestamp": "2025-12-07T03:30:00",
  "cells": [
    {"type": "prey", "x": 0, "y": 1},
    {"type": "predator", "x": 1, "y": 2},
    ...
  ]
}
```

### turn_state.txt
Appendable blocks for each turn:
```
----------------------------------------
Turn: 5
2025-12-07T03:30:15
Counts: prey=25, predators=28, thirdSpecies=0, empty=47
Turn events:
  - Prey moved from (2,3) to (2,4)
  - Predator at (5,6) ate prey at (5,7)
  - Predator at (1,1) died from starvation
Grid snapshot:
  . . P P . . . . . .
  . D . . . . . . . .
  ...
```

## Scenarios

Three pre-configured scenarios in `data/scenarios/`:

1. **balanced_scenario.json**
   - 30 prey, 30 predators, 40 empty cells
   - Balanced ecosystem for observing natural dynamics

2. **dominant_predator_scenario.json**
   - 10 prey, 60 predators, 30 empty cells
   - Predator-heavy scenario, often leads to prey extinction

3. **dominant_prey_scenario.json**
   - 60 prey, 10 predators, 30 empty cells
   - Prey-heavy scenario, predators may struggle to survive

## Simulation Rules

The simulation follows these **exact** rules per turn:

### Phase 1: Movement
- **Prey**: Attempt to move to ONE adjacent EMPTY cell (N, S, E, W). Randomly choose from available empty cells. If none found, stay in place.
- **Predators**: If at least one adjacent prey cell exists, move to ONE of those cells (prioritize prey). Otherwise, move to a random adjacent EMPTY cell.
- **ThirdSpecies**: Hunt prey or weak predators opportunistically. If no targets, move to empty cell.

### Phase 2: Feeding
- When a predator moves to a cell with prey, the prey disappears (is eaten)
- Predator's `lastTurnEated` is set to `currentTurn`

### Phase 3: Hunger
- If a predator goes 3 consecutive turns without eating (`currentTurn - lastTurnEated >= 3`), it dies
- Its cell becomes EMPTY

### Phase 4: Reproduction
- **Prey**: Reproduces if it has survived 2 consecutive turns AND has enough energy, in an adjacent EMPTY cell
- **Predator**: Reproduces if it has eaten at least once in the last 3 turns AND has enough energy, in an adjacent EMPTY cell
- **ThirdSpecies**: Reproduces with sufficient energy in an adjacent EMPTY cell

### Phase 5: End of Turn
- Append turn block to `turn_state.txt` with timestamp, counts, events, and grid snapshot
- Update UI with current state
- Check for simulation end conditions (max turns reached or extinction)

## Building and Running

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build
```bash
# Clean and compile
mvn clean compile

# Build executable JAR (includes all dependencies)
mvn clean package

# Run tests
mvn test
```

### Running the Application

#### Option 1: Run from IDE
Run the main class: `com.ecosimulator.ui.LoginFrame`

#### Option 2: Run from command line (after build)
```bash
# Using the shaded JAR (all dependencies included)
java -jar target/eco-simulator-1.0.0.jar

# Or specify the main class
java -cp target/eco-simulator-1.0.0.jar com.ecosimulator.ui.LoginFrame
```

#### Option 3: Run directly with Maven
```bash
mvn exec:java -Dexec.mainClass="com.ecosimulator.ui.LoginFrame"
```

## User Workflow

1. **Registration**
   - Click "Register" on login screen
   - Fill in all required fields:
     - Cédula (ID) - unique identifier
     - Nombre completo (Full name)
     - Fecha de Nacimiento (Birth date) - using dropdowns
     - Género (Gender) - radio buttons
     - Email - validated with regex
     - Contraseña (Password)
   - Must be 18 years or older
   - System generates unique salt and hashes password
   - Saves to `users.txt`

2. **Login**
   - Enter Cédula and Password
   - System verifies using stored salt and hash
   - Opens main application window on success

3. **Run Simulation**
   - Select scenario from dropdown (Balanced, Dominant Predator, Dominant Prey)
   - Optional: Enable extensions (Third Species, Mutations)
   - Set max turns (1-1000)
   - Set interval (100-2000 ms between turns)
   - Click "Start Simulation"
   - Watch the ecosystem evolve in real-time
   - Click "Stop Simulation" to end early

4. **View Results**
   - Statistics panel shows:
     - Current turn number
     - Population counts (prey, predators, third species)
     - Extinction turn (if occurred)
     - Final statistics summary
   - Files generated:
     - `ecosystem.txt` - Initial state
     - `turn_state.txt` - Complete simulation log

## Email Configuration

Edit `config/app.properties` to configure email settings:

```properties
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.username=your-email@gmail.com
mail.smtp.password=your-app-password
mail.smtp.from=your-email@gmail.com
mail.smtp.starttls=true
mail.smtp.ssl=false
```

**Note**: For Gmail, you need to create an App Password:
1. Enable 2-Factor Authentication
2. Visit https://myaccount.google.com/apppasswords
3. Generate an App Password for "Mail"
4. Use the 16-character password in the config

## Security Features

- **Password Hashing**: PBKDF2WithHmacSHA256 with 65536 iterations
- **Unique Salts**: Each user has a unique random salt
- **No Plain Text Storage**: Passwords are never stored in plain text
- **Age Validation**: Registration requires users to be 18+ years old
- **Email Validation**: Basic regex validation for email format

## Simulation Extensions

The UI supports two optional extensions:

1. **Third Species** - Adds an opportunistic hunter that can prey on both prey and weak predators
2. **Mutations** - Enables random mutations that give creatures efficiency bonuses

*Note: These extensions are UI placeholders. Full implementation in the simulation engine can be added.*

## Troubleshooting

### Build Issues
```bash
# Clear Maven cache and rebuild
mvn clean install -U
```

### Module Issues
The project uses Java modules (`module-info.java`). Ensure all required modules are listed.

### Runtime Issues
- Ensure Java 17+ is installed: `java -version`
- Check that all dependencies are in the classpath (use shaded JAR)
- Verify `data/scenarios/` directory exists with JSON files

## Differences from JavaFX Version

This project includes **both** Swing and JavaFX implementations:

- **Swing UI** (this implementation): Uses `javax.swing.Timer`, follows exact requirements
- **JavaFX UI** (existing): More modern, uses `javafx.animation.Timeline`

Both UIs work with the same core simulation engine and persistence layer.

To run the JavaFX version:
```bash
mvn javafx:run
```

## License

This project is part of UTN coursework for 2025 III Quarter.

## Authors

- Development Team: UTN Students 2025
- Repository: andreyhnzzz/eco_simulator
