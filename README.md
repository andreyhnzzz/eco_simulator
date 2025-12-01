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

### Email Reports
- Automatic PDF report generation at simulation end
- Email sending with PDF attachment
- SMTP configuration UI with test connection
- Fallback to local file storage if email fails

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
│   └── EmailService.java     # Email notifications (SMTP)
├── report/
│   ├── PDFReportGenerator.java  # PDF report generation
│   └── ChartGenerator.java      # Chart generation for reports
└── ui/
    ├── SimulationView.java      # JavaFX main view
    ├── LoginView.java           # Login/registration view
    └── SMTPSettingsController.java  # SMTP settings dialog
```

## Simulation Rules

1. **Predators** hunt prey and gain energy when eating
2. **Prey** eat vegetation (always available) and reproduce quickly
3. **Third Species** is opportunistic and can hunt both prey and predators
4. Creatures lose energy each turn and die when energy reaches 0
5. Creatures with enough energy can reproduce
6. Mutated creatures have a 50% bonus to efficiency

## Email Configuration

The application supports sending simulation reports via email. When a simulation completes, a PDF report is generated and can be automatically sent to the logged-in user's email address.

### Quick Setup

Click the "⚙ Configurar Email" button in the simulation view to open the SMTP settings dialog. Use the preset buttons for quick configuration.

### Configuration Options

#### 1. Gmail with App Password (Recommended for production)

Gmail requires an App Password for SMTP access:

1. Enable 2-Factor Authentication on your Google account
2. Go to https://myaccount.google.com/apppasswords
3. Create a new App Password for "Mail"
4. Use these settings:
   - **Host**: `smtp.gmail.com`
   - **Port**: `587`
   - **Username**: Your Gmail address
   - **Password**: The 16-character App Password (no spaces)
   - **STARTTLS**: Enabled
   - **SSL**: Disabled

#### 2. MailHog (Recommended for local development/testing)

MailHog is a local email testing tool that catches all outgoing emails.

Start MailHog with Docker:
```bash
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

Configuration:
- **Host**: `localhost`
- **Port**: `1025`
- **Username**: (leave empty)
- **Password**: (leave empty)
- **From Address**: `test@localhost`
- **STARTTLS**: Disabled
- **SSL**: Disabled

View captured emails at: http://localhost:8025

#### 3. Mailtrap (Alternative for testing)

Mailtrap is a cloud-based email testing service:
- **Host**: `smtp.mailtrap.io`
- **Port**: `2525`
- **Username**: Your Mailtrap inbox username
- **Password**: Your Mailtrap inbox password
- **STARTTLS**: Enabled

#### 4. Environment Variables

You can also configure SMTP via environment variables:
```bash
export SMTP_HOST=smtp.gmail.com
export SMTP_PORT=587
export SMTP_USERNAME=your.email@gmail.com
export SMTP_PASSWORD=your-app-password
export SMTP_FROM_ADDRESS=your.email@gmail.com
export SMTP_STARTTLS=true
export SMTP_SSL=false
```

Environment variables take precedence over the configuration file.

### Configuration File

Copy `config/smtp.example.properties` to `config/smtp.properties` and edit:
```properties
smtp.host=smtp.gmail.com
smtp.port=587
smtp.username=your.email@gmail.com
smtp.password=your-app-password
smtp.from=your.email@gmail.com
smtp.starttls=true
smtp.ssl=false
```

**Note**: The `config/smtp.properties` file is excluded from version control for security.

### Fallback Behavior

If email sending fails (network issues, invalid credentials, etc.):
- The PDF report is saved to `./outgoing_reports/`
- The filename includes the recipient email and timestamp
- The application continues to run normally (no crash)
- A notification is shown to the user

### Testing SMTP Connection

Use the "🔌 Test Connection" button in the SMTP settings dialog to verify your configuration before saving.

## Testing

The project includes automated tests using JUnit 5 and GreenMail (in-memory SMTP server):

```bash
# Run all tests
mvn test

# Run email service tests only
mvn test -Dtest=EmailServiceTest
```

### Test Coverage

- EmailService: Connection, sending, attachments, fallback behavior
- PDF Generation: Report creation, content validation
- Simulation Engine: Turn execution, creature behavior
- User Authentication: Login, registration, session management

## Technologies Used

- **JavaFX 21** - UI Framework
- **Jakarta Mail (Angus Mail 2.0.3)** - Email sending
- **Apache PDFBox 3.0.2** - PDF generation
- **JFreeChart 1.5.4** - Chart generation
- **JUnit 5** - Unit testing
- **GreenMail 2.1.0** - Email testing (in-memory SMTP)

## Security Notes

- Never commit real credentials to version control
- Use App Passwords instead of regular passwords for Gmail
- The `config/smtp.properties` file is in `.gitignore`
- Passwords are not saved to disk by the settings dialog
- Use environment variables for CI/CD deployments

## License

This project is part of UTN coursework for 2025 III Quarter.
