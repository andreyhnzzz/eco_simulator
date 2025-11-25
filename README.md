# Eco Simulator
UTN Work from 2025 III Quarter

## Description
A decoupled ecosystem simulation engine that:
- Represents an ecosystem in a 10×10 matrix
- Applies movement, feeding, reproduction, and death rules by turns
- Is observable (for GUI and persistence)
- Supports three scenarios + two extensions
- Fails gracefully if a module (e.g., email) fails

## Project Structure

The project follows a modular architecture with the following modules:

### 1. Core Module (`com.ecosimulator.core`)
Pure domain model with no I/O, GUI, or persistence dependencies:
- `Especie` - Abstract base class for species
- `Presas` - Prey species (reproduces every 2 turns)
- `Depredadores` - Predator species (dies after 3 turns without eating)
- `Carroneros` - Scavenger species (feeds on corpses)
- `Mutacion` - Enum for mutations (VELOCIDAD, FERTILIDAD, RESISTENCIA_HAMBRE)
- `Celda` - Cell in the ecosystem matrix
- `Ecosistema` - 10×10 matrix of cells

### 2. Simulation Module (`com.ecosimulator.simulation`)
- `Escenario` - DTO for scenario configuration
- `MotorDeSimulacion` - Simulation engine that orchestrates rules
- `SimulationObserver` - Observer interface for simulation events
- `EstadoTurno` - Turn state record

### 3. Persistence Module (`com.ecosimulator.persistence`)
- `EstadoTurnoDAO` - Persists turn states to `estado_turnos.txt`
- `EcosistemaDAO` - Persists ecosystem configuration to `ecosistema.txt`

### 4. Auth Module (`com.ecosimulator.auth`)
- `Usuario` - User entity
- `UsuarioDAO` - User persistence with SHA-256 + salt hashing

### 5. Reporting Module (`com.ecosimulator.reporting`)
- `AnalisisEscenarios` - Analyzes simulation results
- `ReportePDFGenerator` - Generates PDF reports with charts

### 6. Mail Module (`com.ecosimulator.mail`)
- `ReporteEmailService` - Sends reports via email with fallback

### 7. UI Module (`com.ecosimulator.ui`)
JavaFX-based user interface:
- `MainApplication` - Application entry point
- `MainController` - Main controller orchestrating the flow
- `LoginView` - Login screen
- `RegistroView` - Registration screen
- `ScenarioSelectionView` - Scenario selection
- `SimulacionView` - Real-time simulation view
- `ReporteView` - Report display

## Predefined Scenarios
- **EQUILIBRADO**: 30 prey, 30 predators, 0 scavengers
- **DEPREDADORES_DOMINANTES**: 15 prey, 45 predators, 0 scavengers
- **PRESAS_DOMINANTES**: 45 prey, 15 predators, 0 scavengers

## Simulation Rules (per turn)
1. Mark corpses
2. Movement (prey → empty, predator → prey or empty, scavenger → corpse)
3. Feeding (predator eats prey)
4. Deaths (predator starves after 3 turns)
5. Reproduction
6. Notify observers

## Mutations
- **VELOCIDAD**: Move 2 cells per turn
- **FERTILIDAD**: Reproduce more frequently
- **RESISTENCIA_HAMBRE**: Predators survive 5 turns without eating

## Building and Running

```bash
# Compile
mvn clean compile

# Run tests
mvn test

# Run application (requires JavaFX)
mvn javafx:run
```

## Requirements
- Java 17+
- Maven 3.6+
- JavaFX 17+
