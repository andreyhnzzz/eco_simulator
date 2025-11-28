package com.ecosimulator.ui;

import com.ecosimulator.auth.Session;
import com.ecosimulator.auth.User;
import com.ecosimulator.persistence.UserDAO;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Period;

/**
 * Login and Registration view for user authentication
 * Supports both login and new user registration with age validation
 */
public class LoginView extends VBox {
    private final UserDAO userDAO;
    private final Stage primaryStage;
    private final Runnable onLoginSuccess;

    // Login form fields
    private TextField loginIdField;
    private PasswordField loginPasswordField;
    private Label loginErrorLabel;

    // Registration form fields
    private TextField regIdField;
    private TextField regNameField;
    private PasswordField regPasswordField;
    private PasswordField regConfirmPasswordField;
    private TextField regEmailField;
    private DatePicker regBirthDatePicker;
    private Label regErrorLabel;

    // Tabs
    private TabPane tabPane;

    /**
     * Create a new LoginView
     * @param primaryStage the primary stage
     * @param onLoginSuccess callback to run when login is successful
     */
    public LoginView(Stage primaryStage, Runnable onLoginSuccess) {
        this.primaryStage = primaryStage;
        this.onLoginSuccess = onLoginSuccess;
        this.userDAO = new UserDAO();

        initializeUI();
        applyStyles();
    }

    private void initializeUI() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));
        setSpacing(20);

        // Title
        Label titleLabel = new Label("🌿 Eco Simulator 🌿");
        titleLabel.getStyleClass().add("title-label");

        Label subtitleLabel = new Label("Simulador Ecológico - UTN 2025");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        // Tab pane for login/register
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setMaxWidth(400);

        Tab loginTab = new Tab("Iniciar Sesión", createLoginForm());
        Tab registerTab = new Tab("Registrarse", createRegisterForm());

        tabPane.getTabs().addAll(loginTab, registerTab);

        getChildren().addAll(titleLabel, subtitleLabel, tabPane);
    }

    private VBox createLoginForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(30));
        form.setAlignment(Pos.CENTER);

        Label idLabel = new Label("Cédula (ID):");
        idLabel.getStyleClass().add("control-label");

        loginIdField = new TextField();
        loginIdField.setPromptText("Ingrese su cédula");
        loginIdField.setMaxWidth(300);
        loginIdField.getStyleClass().add("text-field-custom");

        Label passwordLabel = new Label("Contraseña:");
        passwordLabel.getStyleClass().add("control-label");

        loginPasswordField = new PasswordField();
        loginPasswordField.setPromptText("Ingrese su contraseña");
        loginPasswordField.setMaxWidth(300);
        loginPasswordField.getStyleClass().add("text-field-custom");

        loginErrorLabel = new Label();
        loginErrorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");
        loginErrorLabel.setVisible(false);

        Button loginButton = new Button("Ingresar");
        loginButton.getStyleClass().addAll("action-button", "start-button");
        loginButton.setOnAction(e -> handleLogin());

        // Allow Enter key to submit
        loginPasswordField.setOnAction(e -> handleLogin());

        form.getChildren().addAll(
            idLabel, loginIdField,
            passwordLabel, loginPasswordField,
            loginErrorLabel,
            loginButton
        );

        return form;
    }

    private VBox createRegisterForm() {
        VBox form = new VBox(12);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);

        // ID
        Label idLabel = new Label("Cédula (ID):");
        idLabel.getStyleClass().add("control-label");
        regIdField = new TextField();
        regIdField.setPromptText("Ingrese su cédula");
        regIdField.setMaxWidth(300);

        // Name
        Label nameLabel = new Label("Nombre completo:");
        nameLabel.getStyleClass().add("control-label");
        regNameField = new TextField();
        regNameField.setPromptText("Ingrese su nombre");
        regNameField.setMaxWidth(300);

        // Email
        Label emailLabel = new Label("Correo electrónico:");
        emailLabel.getStyleClass().add("control-label");
        regEmailField = new TextField();
        regEmailField.setPromptText("correo@ejemplo.com");
        regEmailField.setMaxWidth(300);

        // Birth date
        Label birthLabel = new Label("Fecha de nacimiento:");
        birthLabel.getStyleClass().add("control-label");
        regBirthDatePicker = new DatePicker();
        regBirthDatePicker.setPromptText("Seleccione fecha");
        regBirthDatePicker.setMaxWidth(300);
        regBirthDatePicker.setEditable(false);

        // Password
        Label passwordLabel = new Label("Contraseña:");
        passwordLabel.getStyleClass().add("control-label");
        regPasswordField = new PasswordField();
        regPasswordField.setPromptText("Mínimo 6 caracteres");
        regPasswordField.setMaxWidth(300);

        // Confirm password
        Label confirmLabel = new Label("Confirmar contraseña:");
        confirmLabel.getStyleClass().add("control-label");
        regConfirmPasswordField = new PasswordField();
        regConfirmPasswordField.setPromptText("Repita la contraseña");
        regConfirmPasswordField.setMaxWidth(300);

        regErrorLabel = new Label();
        regErrorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");
        regErrorLabel.setWrapText(true);
        regErrorLabel.setMaxWidth(300);
        regErrorLabel.setVisible(false);

        Button registerButton = new Button("Registrarse");
        registerButton.getStyleClass().addAll("action-button", "start-button");
        registerButton.setOnAction(e -> handleRegister());

        form.getChildren().addAll(
            idLabel, regIdField,
            nameLabel, regNameField,
            emailLabel, regEmailField,
            birthLabel, regBirthDatePicker,
            passwordLabel, regPasswordField,
            confirmLabel, regConfirmPasswordField,
            regErrorLabel,
            registerButton
        );

        return form;
    }

    private void handleLogin() {
        loginErrorLabel.setVisible(false);

        String idText = loginIdField.getText().trim();
        String password = loginPasswordField.getText();

        // Validate input
        if (idText.isEmpty() || password.isEmpty()) {
            showLoginError("Por favor complete todos los campos");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showLoginError("La cédula debe ser un número válido");
            return;
        }

        // Authenticate
        User user = userDAO.authenticate(id, password);
        if (user != null) {
            Session.setUser(user);
            onLoginSuccess.run();
        } else {
            showLoginError("Cédula o contraseña incorrectos");
        }
    }

    private void handleRegister() {
        regErrorLabel.setVisible(false);

        String idText = regIdField.getText().trim();
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        LocalDate birthDate = regBirthDatePicker.getValue();
        String password = regPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();

        // Validate all fields
        if (idText.isEmpty() || name.isEmpty() || email.isEmpty() || 
            birthDate == null || password.isEmpty() || confirmPassword.isEmpty()) {
            showRegisterError("Por favor complete todos los campos");
            return;
        }

        // Validate ID format
        int id;
        try {
            id = Integer.parseInt(idText);
            if (id <= 0) {
                showRegisterError("La cédula debe ser un número positivo");
                return;
            }
        } catch (NumberFormatException e) {
            showRegisterError("La cédula debe ser un número válido");
            return;
        }

        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showRegisterError("Ingrese un correo electrónico válido");
            return;
        }

        // Validate age (must be 18+)
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18) {
            showRegisterError("Debe ser mayor de 18 años para registrarse");
            return;
        }

        // Validate password
        if (password.length() < 6) {
            showRegisterError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            showRegisterError("Las contraseñas no coinciden");
            return;
        }

        // Attempt registration
        try {
            boolean success = userDAO.register(id, name, password, email, birthDate);
            if (success) {
                showSuccessAndSwitchToLogin();
            } else {
                showRegisterError("Ya existe un usuario con esta cédula");
            }
        } catch (IllegalArgumentException e) {
            showRegisterError(e.getMessage());
        }
    }

    private void showLoginError(String message) {
        loginErrorLabel.setText(message);
        loginErrorLabel.setVisible(true);
    }

    private void showRegisterError(String message) {
        regErrorLabel.setText(message);
        regErrorLabel.setVisible(true);
    }

    private void showSuccessAndSwitchToLogin() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registro Exitoso");
        alert.setHeaderText("¡Bienvenido!");
        alert.setContentText("Su cuenta ha sido creada exitosamente. Por favor inicie sesión.");
        alert.showAndWait();

        // Clear registration form
        regIdField.clear();
        regNameField.clear();
        regEmailField.clear();
        regBirthDatePicker.setValue(null);
        regPasswordField.clear();
        regConfirmPasswordField.clear();

        // Switch to login tab
        tabPane.getSelectionModel().select(0);
    }

    private void applyStyles() {
        getStyleClass().add("main-view");
        setStyle("-fx-background-color: linear-gradient(to bottom right, #E8F5E9, #C8E6C9);");
    }

    /**
     * Create a scene with the login view
     * @return the login scene
     */
    public Scene createScene() {
        Scene scene = new Scene(this, 500, 650);
        try {
            String cssPath = getClass().getResource("/css/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Could not load CSS styles: " + e.getMessage());
        }
        return scene;
    }
}
