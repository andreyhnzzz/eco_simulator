# Consecutive Simulations Feature Guide

## Overview

The Eco Simulator now supports running multiple consecutive simulations and generating a comprehensive multi-page PDF report with all results. This feature allows you to compare different scenarios and track ecosystem evolution across multiple runs.

## Features

### 1. Automatic Scenario Cycling
- Start a simulation and it automatically cycles through scenarios
- Scenarios rotate: Equilibrado → Depredadores Dominantes → Presas Dominantes → Equilibrado...
- Each simulation maintains your chosen extensions (Third Species, Mutations)

### 2. Simulation Tracking
- Each simulation is numbered (Simulation #1, #2, #3, etc.)
- Status bar shows current simulation number and total completed
- Results from each simulation are stored for the final report

### 3. Multi-Page PDF Reports
- **Summary Page**: Overview of all simulations with quick statistics
- **Individual Pages**: One page per simulation with detailed results
  - Simulation number and configuration
  - Population statistics (predators, prey, third species)
  - Gender distribution (male/female counts)
  - Resource usage (water, food consumption)
  - Ecosystem occupancy percentage
  - Extinction information (if applicable)
  - Final result/winner

## How to Use

### Starting Consecutive Simulations

1. **Configure Initial Settings**:
   - Select scenario (Equilibrado, Depredadores Dominantes, or Presas Dominantes)
   - Check/uncheck Third Species extension
   - Check/uncheck Mutations extension
   - Adjust simulation speed if desired

2. **Start First Simulation**:
   - Click the **"▶ Iniciar"** button
   - This begins Simulation #1 with your selected settings

3. **Monitor Progress**:
   - Status bar shows: "🔄 Simulación #1 en progreso..."
   - Watch the ecosystem evolve in real-time
   - Grid displays creatures, resources, and interactions

### Continuing to Next Simulation

4. **When Simulation Completes**:
   - Status bar updates: "✅ Simulación #1 terminada: [Result] | Total: 1 simulaciones"
   - A notification appears with options
   - Two new buttons become active:
     - **"➡️ Siguiente"**: Continue to next simulation
     - **"⏹ Finalizar & PDF"**: Stop and generate report

5. **Continue to Next Simulation**:
   - Click **"➡️ Siguiente"** button
   - Scenario automatically advances to the next one
   - Simulation counter increments (Simulation #2, #3, etc.)
   - Grid resets and new simulation begins immediately

6. **Repeat as Desired**:
   - Each completed simulation is added to the report
   - You can run as many consecutive simulations as you want
   - Status bar always shows current simulation number and total count

### Generating Final Report

7. **Stop and Generate Report**:
   - Click **"⏹ Finalizar & PDF"** when you want to stop
   - System generates a comprehensive PDF report
   - Report includes:
     - Summary page with all simulation configurations
     - One page per completed simulation with full details
     - Statistics and final results for each run

8. **Report Location**:
   - Saved to: `reports/multi_simulation_report_[timestamp].pdf`
   - If logged in with email configured, report is automatically sent via email
   - Notification shows the file path and simulation count

### Resetting

9. **Start Fresh**:
   - Click **"🔄 Reiniciar"** to clear all data
   - Resets simulation counter to 0
   - Clears multi-simulation report
   - Returns to initial state ready for new sequence

## UI Controls Reference

### Main Buttons
- **▶ Iniciar**: Start first simulation in consecutive mode
- **⏸ Pausar**: Pause current simulation
- **🔄 Reiniciar**: Reset everything and start fresh
- **📊 Comparar**: Run scenario comparison (separate feature)
- **⚙ Email**: Configure email settings

### Consecutive Simulation Buttons
- **➡️ Siguiente**: Start next simulation (active after completion)
- **⏹ Finalizar & PDF**: Stop sequence and generate multi-page report

### Status Indicators
- **Turn Counter**: Shows current turn number
- **Status Bar**: Shows current state:
  - "🔄 Simulación #X en progreso..."
  - "✅ Simulación #X terminada: [Result] | Total: X simulaciones"
  - "📄 Generando reporte multi-simulación..."
  - "✅ Reporte generado: X simulaciones"

## Example Workflow

### Running 3 Consecutive Simulations

1. **Setup**:
   - Select "Equilibrado" scenario
   - Enable "Tercer Especie"
   - Leave "Mutaciones" unchecked
   - Set speed to 1000ms

2. **Simulation #1** (Equilibrado):
   - Click "▶ Iniciar"
   - Wait for completion
   - Status: "✅ Simulación #1 terminada"

3. **Simulation #2** (Depredadores Dominantes):
   - Click "➡️ Siguiente"
   - Scenario automatically changes
   - Wait for completion
   - Status: "✅ Simulación #2 terminada | Total: 2 simulaciones"

4. **Simulation #3** (Presas Dominantes):
   - Click "➡️ Siguiente"
   - Scenario automatically changes
   - Wait for completion
   - Status: "✅ Simulación #3 terminada | Total: 3 simulaciones"

5. **Generate Report**:
   - Click "⏹ Finalizar & PDF"
   - PDF generated with 4 pages total:
     - Page 1: Summary of all 3 simulations
     - Page 2: Simulation #1 details
     - Page 3: Simulation #2 details
     - Page 4: Simulation #3 details

## PDF Report Structure

### Summary Page
```
Multi-Simulation Report
Generated: 2025-12-07 10:30:00

Summary
Total Simulations: 3

Simulations Included:
1. Equilibrado + Tercer Especie
   Turns: 150 | Winner: Prey Dominant
2. Depredadores Dominantes + Tercer Especie
   Turns: 75 | Winner: Extinction Occurred
3. Presas Dominantes + Tercer Especie
   Turns: 200 | Winner: Balanced Ecosystem
```

### Individual Simulation Pages
```
Simulation #1
Equilibrado + Tercer Especie
Completed: 2025-12-07 10:25:00

Total Turns: 150
Grid Size: 25 x 25

Final Population:
  - Predators: 45 (Male: 22, Female: 23)
  - Prey: 89 (Male: 44, Female: 45)
  - Third Species: 12 (Male: 6, Female: 6)
  - Mutated: 0
  - Total: 146

Resources:
  - Water Sources: 10
  - Food Sources: 15
  - Water Consumed: 450
  - Food Consumed: 890

Ecosystem Occupancy: 23.4%
No Extinction Occurred

Result: Prey Dominant - Balanced Ecosystem
```

## Tips and Best Practices

### 1. Planning Your Simulation Sequence
- **Comparison Studies**: Run all 3 scenarios consecutively to compare outcomes
- **Extension Testing**: Run same scenario with/without extensions
- **Stability Analysis**: Run same configuration multiple times to test variance

### 2. Managing Simulation Count
- **Recommended**: 3-6 simulations per report (one page per scenario)
- **Maximum**: No hard limit, but PDFs become large with many simulations
- **Minimum**: At least 2 simulations for meaningful comparison

### 3. Speed Settings
- **Fast Analysis**: 500ms for quick iterations
- **Detailed Observation**: 1500-2000ms to watch ecosystem dynamics
- **Speed can be adjusted** while simulation is paused

### 4. Email Integration
- Configure email settings before starting long simulation sequences
- Report automatically sent to your registered email when generated
- Local copy always saved even if email fails

### 5. Data Persistence
- Simulation results stored in memory until report generation
- Click "⏹ Finalizar & PDF" to save before closing application
- Clicking "🔄 Reiniciar" clears all stored simulation data

## Technical Notes

### Memory Usage
- Each simulation stores ~2-5 KB of data (stats, configuration)
- Running 100 simulations uses approximately 200-500 KB
- No memory leaks; data cleared after report generation

### Report Generation Time
- Summary page: ~50ms
- Each simulation page: ~100ms
- Total for 5 simulations: ~500ms
- Generation happens in background thread (non-blocking)

### File Naming
- Pattern: `multi_simulation_report_YYYYMMDD_HHMMSS.pdf`
- Example: `multi_simulation_report_20251207_103045.pdf`
- Timestamp prevents file conflicts

## Troubleshooting

### Issue: "Siguiente" Button Not Activating
**Cause**: Simulation not fully complete  
**Solution**: Wait for status to show "✅ Simulación #X terminada"

### Issue: Report Shows Fewer Simulations Than Expected
**Cause**: Only completed simulations are included  
**Solution**: Ensure each simulation runs to completion before clicking "Siguiente"

### Issue: Can't Start New Sequence After Report
**Cause**: Need to reset state  
**Solution**: Click "🔄 Reiniciar" to clear and start fresh

### Issue: Report Not Generated
**Check**:
1. At least one simulation completed?
2. `reports/` directory permissions?
3. Check console for error messages

**Solution**: Ensure simulations complete successfully before generating report

## Keyboard Shortcuts (Future Enhancement)

*Note: These are planned for future versions*
- `Space`: Pause/Resume
- `N`: Next simulation
- `R`: Reset
- `G`: Generate report

## Related Features

- **Single Simulation Mode**: Original behavior (without "Siguiente" button)
- **Scenario Comparison**: Separate feature that runs all scenarios automatically
- **Email Reports**: Integrates with consecutive simulation reports

## Feedback and Support

For issues or suggestions regarding the consecutive simulation feature:
1. Check the console logs for detailed error messages
2. Verify all simulations completed successfully
3. Ensure sufficient disk space for PDF generation
4. Review this guide for proper usage patterns

---

**Version**: 1.0.0  
**Last Updated**: December 2025  
**Compatible With**: Eco Simulator v1.0.0+
