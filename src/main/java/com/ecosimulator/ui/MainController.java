package com.ecosimulator.ui;

import com.ecosimulator.auth.Usuario;
import com.ecosimulator.auth.UsuarioDAO;
import com.ecosimulator.core.Ecosistema;
import com.ecosimulator.mail.ReporteEmailService;
import com.ecosimulator.persistence.EcosistemaDAO;
import com.ecosimulator.persistence.EstadoTurnoDAO;
import com.ecosimulator.reporting.AnalisisEscenarios;
import com.ecosimulator.reporting.ReportePDFGenerator;
import com.ecosimulator.simulation.Escenario;
import com.ecosimulator.simulation.MotorDeSimulacion;

import javafx.stage.Stage;

/**
 * Main controller that orchestrates the application flow.
 * Flow: login -> scenario selection -> simulation -> report
 */
public class MainController {
    
    private final Stage primaryStage;
    private final UsuarioDAO usuarioDAO;
    private final EcosistemaDAO ecosistemaDAO;
    private final EstadoTurnoDAO estadoTurnoDAO;
    
    private Usuario usuarioActual;
    private MotorDeSimulacion motor;
    private Escenario escenarioActual;

    public MainController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.usuarioDAO = new UsuarioDAO();
        this.ecosistemaDAO = new EcosistemaDAO();
        this.estadoTurnoDAO = new EstadoTurnoDAO();
        
        primaryStage.setTitle("Eco Simulator");
    }

    /**
     * Show the login view.
     */
    public void showLoginView() {
        LoginView loginView = new LoginView(this);
        primaryStage.setScene(loginView.getScene());
        primaryStage.setWidth(400);
        primaryStage.setHeight(350);
        primaryStage.show();
    }

    /**
     * Show the registration view.
     */
    public void showRegistroView() {
        RegistroView registroView = new RegistroView(this);
        primaryStage.setScene(registroView.getScene());
        primaryStage.setWidth(400);
        primaryStage.setHeight(500);
    }

    /**
     * Show the scenario selection view.
     */
    public void showScenarioSelection() {
        ScenarioSelectionView selectionView = new ScenarioSelectionView(this);
        primaryStage.setScene(selectionView.getScene());
        primaryStage.setWidth(500);
        primaryStage.setHeight(400);
    }

    /**
     * Show the simulation view.
     */
    public void showSimulacionView() {
        SimulacionView simulacionView = new SimulacionView(this);
        primaryStage.setScene(simulacionView.getScene());
        primaryStage.setWidth(800);
        primaryStage.setHeight(700);
    }

    /**
     * Show the report view.
     */
    public void showReporteView() {
        ReporteView reporteView = new ReporteView(this);
        primaryStage.setScene(reporteView.getScene());
        primaryStage.setWidth(700);
        primaryStage.setHeight(600);
    }

    /**
     * Handle login attempt.
     * @param cedula user ID
     * @param password password
     * @return true if successful
     */
    public boolean login(String cedula, String password) {
        usuarioActual = usuarioDAO.autenticar(cedula, password);
        return usuarioActual != null;
    }

    /**
     * Handle user registration.
     * @return the created user, or null if failed
     */
    public Usuario registrar(String cedula, String nombre, java.time.LocalDate fechaNacimiento,
                            String genero, String password, String correo) {
        return usuarioDAO.registrar(cedula, nombre, fechaNacimiento, genero, password, correo);
    }

    /**
     * Start simulation with selected scenario.
     * @param escenario the selected scenario
     * @param maxTurnos maximum number of turns
     */
    public void startSimulation(Escenario escenario, int maxTurnos) {
        this.escenarioActual = escenario;
        
        Ecosistema ecosistema = new Ecosistema();
        motor = new MotorDeSimulacion(ecosistema, escenario, maxTurnos);
        
        // Add persistence observer
        motor.addObserver(estadoTurnoDAO);
        
        // Initialize ecosystem
        motor.inicializarEcosistema();
        
        // Save initial configuration
        ecosistemaDAO.guardarConfiguracionInicial(ecosistema, escenario);
    }

    /**
     * Run the simulation step by step for UI updates.
     */
    public void runSimulationStep() {
        if (motor != null && motor.isSimulacionActiva()) {
            motor.ejecutarTurno();
        }
    }

    /**
     * Generate the final report.
     * @return path to generated PDF
     */
    public String generateReport() {
        if (motor == null || escenarioActual == null) {
            return null;
        }
        
        AnalisisEscenarios analisis = new AnalisisEscenarios(
                motor.getHistorial(),
                motor.getTurnoExtincion(),
                motor.getEspecieExtinta()
        );
        
        String pdfPath = "reporte_" + escenarioActual.getNombre().replace(" ", "_") + ".pdf";
        ReportePDFGenerator generator = new ReportePDFGenerator(analisis, escenarioActual.getNombre());
        
        if (generator.generarReporte(pdfPath)) {
            return pdfPath;
        }
        return null;
    }

    /**
     * Send report via email.
     * @param pdfPath path to the PDF
     * @return true if sent successfully
     */
    public boolean sendReportByEmail(String pdfPath) {
        if (usuarioActual == null || pdfPath == null) {
            return false;
        }
        
        // Note: In a real application, these would be configured externally
        // For now, this will fail gracefully and save locally
        ReporteEmailService emailService = new ReporteEmailService(
                "smtp.example.com", 587, "user@example.com", "password", true
        );
        
        return emailService.enviarReporte(
                usuarioActual.getCorreo(),
                pdfPath,
                escenarioActual != null ? escenarioActual.getNombre() : "Simulación"
        );
    }

    /**
     * Cleanup resources.
     */
    public void cleanup() {
        if (motor != null) {
            motor.detener();
        }
    }

    // Getters
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public MotorDeSimulacion getMotor() {
        return motor;
    }

    public Escenario getEscenarioActual() {
        return escenarioActual;
    }

    public EstadoTurnoDAO getEstadoTurnoDAO() {
        return estadoTurnoDAO;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
