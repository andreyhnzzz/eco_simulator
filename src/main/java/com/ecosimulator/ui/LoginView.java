package com.ecosimulator.ui;

import com.ecosimulator.auth.Session;
import com.ecosimulator.auth.User;
import com.ecosimulator.persistence.UserDAO;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.Period;

/**
 * Login and Registration view for user authentication
 * Features glassmorphism styling, smooth animations, and modern UI/UX
 */
public class LoginView extends StackPane {
    private final UserDAO userDAO;
    private final Stage primaryStage;
    private final Runnable onLoginSuccess;

    // Main content container
    private VBox mainContent;
    
    // Login form fields
    private TextField loginIdField;
    private PasswordField loginPasswordField;
    private Label loginErrorLabel;
    private Button loginButton;

    // Registration form fields
    private TextField regIdField;
    private TextField regNameField;
    private PasswordField regPasswordField;
    private PasswordField regConfirmPasswordField;
    private TextField regEmailField;
    private DatePicker regBirthDatePicker;
    private Label regErrorLabel;
    private Button registerButton;

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
        playEntranceAnimation();
    }

    private void initializeUI() {
        // Create main content container
        mainContent = new VBox(24);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(40));
        mainContent.setMaxWidth(450);
        mainContent.getStyleClass().addAll("glass-panel", "login-container");

        // Title with enhanced styling
        Label titleLabel = new Label("🌿 Eco Simulator 🌿");
        titleLabel.getStyleClass().add("title-label");

        Label subtitleLabel = new Label("Simulador Ecológico Interactivo");
        subtitleLabel.getStyleClass().add("subtitle-label");
        
        // Version/year label
        Label versionLabel = new Label("UTN 2025");
        versionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #78909C;");

        // Tab pane for login/register with enhanced styling
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setMaxWidth(400);
        tabPane.getStyleClass().add("login-tabs");

        Tab loginTab = new Tab("Iniciar Sesión", createLoginForm());
        Tab registerTab = new Tab("Registrarse", createRegisterForm());

        tabPane.getTabs().addAll(loginTab, registerTab);

        mainContent.getChildren().addAll(titleLabel, subtitleLabel, versionLabel, tabPane);
        
        // Center the main content
        setAlignment(Pos.CENTER);
        getChildren().add(mainContent);
    }

    private VBox createLoginForm() {
        VBox form = new VBox(18);
        form.setPadding(new Insets(30));
        form.setAlignment(Pos.CENTER);

        Label idLabel = new Label("Cédula (ID):");
        idLabel.getStyleClass().add("control-label");

        loginIdField = new TextField();
        loginIdField.setPromptText("Ingrese su cédula");
        loginIdField.setMaxWidth(300);
        loginIdField.getStyleClass().add("text-field-custom");
        
        // Add focus animation
        addInputFocusAnimation(loginIdField);

        Label passwordLabel = new Label("Contraseña:");
        passwordLabel.getStyleClass().add("control-label");

        loginPasswordField = new PasswordField();
        loginPasswordField.setPromptText("Ingrese su contraseña");
        loginPasswordField.setMaxWidth(300);
        loginPasswordField.getStyleClass().add("text-field-custom");
        
        // Add focus animation
        addInputFocusAnimation(loginPasswordField);

        loginErrorLabel = new Label();
        loginErrorLabel.getStyleClass().add("error-label");
        loginErrorLabel.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 12px; -fx-font-weight: 600;");
        loginErrorLabel.setVisible(false);
        loginErrorLabel.setWrapText(true);
        loginErrorLabel.setMaxWidth(300);

        loginButton = new Button("🔓 Ingresar");
        loginButton.getStyleClass().addAll("action-button", "start-button");
        loginButton.setMaxWidth(200);
        loginButton.setOnAction(e -> {
            AnimationUtils.playButtonClickAnimation(loginButton);
            handleLogin();
        });
        
        // Apply hover animation
        AnimationUtils.applyButtonHoverAnimation(loginButton);

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
        VBox form = new VBox(14);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);

        // ID
        Label idLabel = new Label("Cédula (ID):");
        idLabel.getStyleClass().add("control-label");
        regIdField = new TextField();
        regIdField.setPromptText("Ingrese su cédula");
        regIdField.setMaxWidth(300);
        regIdField.getStyleClass().add("text-field-custom");
        addInputFocusAnimation(regIdField);

        // Name
        Label nameLabel = new Label("Nombre completo:");
        nameLabel.getStyleClass().add("control-label");
        regNameField = new TextField();
        regNameField.setPromptText("Ingrese su nombre");
        regNameField.setMaxWidth(300);
        regNameField.getStyleClass().add("text-field-custom");
        addInputFocusAnimation(regNameField);

        // Email
        Label emailLabel = new Label("Correo electrónico:");
        emailLabel.getStyleClass().add("control-label");
        regEmailField = new TextField();
        regEmailField.setPromptText("correo@ejemplo.com");
        regEmailField.setMaxWidth(300);
        regEmailField.getStyleClass().add("text-field-custom");
        addInputFocusAnimation(regEmailField);

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
        regPasswordField.getStyleClass().add("text-field-custom");
        addInputFocusAnimation(regPasswordField);

        // Confirm password
        Label confirmLabel = new Label("Confirmar contraseña:");
        confirmLabel.getStyleClass().add("control-label");
        regConfirmPasswordField = new PasswordField();
        regConfirmPasswordField.setPromptText("Repita la contraseña");
        regConfirmPasswordField.setMaxWidth(300);
        regConfirmPasswordField.getStyleClass().add("text-field-custom");
        addInputFocusAnimation(regConfirmPasswordField);

        regErrorLabel = new Label();
        regErrorLabel.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 12px; -fx-font-weight: 600;");
        regErrorLabel.setWrapText(true);
        regErrorLabel.setMaxWidth(300);
        regErrorLabel.setVisible(false);

        registerButton = new Button("✨ Registrarse");
        registerButton.getStyleClass().addAll("action-button", "start-button");
        registerButton.setMaxWidth(200);
        registerButton.setOnAction(e -> {
            AnimationUtils.playButtonClickAnimation(registerButton);
            handleRegister();
        });
        
        // Apply hover animation
        AnimationUtils.applyButtonHoverAnimation(registerButton);

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
    
    /**
     * Add focus animation to input fields
     */
    private void addInputFocusAnimation(TextField field) {
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                ScaleTransition scale = new ScaleTransition(Duration.millis(150), field);
                scale.setToX(1.02);
                scale.setToY(1.02);
                scale.setInterpolator(AnimationUtils.EASE_OUT_CUBIC);
                scale.play();
            } else {
                ScaleTransition scale = new ScaleTransition(Duration.millis(150), field);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.setInterpolator(AnimationUtils.EASE_OUT_CUBIC);
                scale.play();
            }
        });
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
            playSuccessAnimation(() -> onLoginSuccess.run());
        } else {
            showLoginError("Cédula o contraseña incorrectos");
            AnimationUtils.playShakeAnimation(loginIdField);
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

        // Validate email format (RFC 5322 simplified pattern)
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!email.matches(emailRegex)) {
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
        loginErrorLabel.setText("⚠️ " + message);
        loginErrorLabel.setVisible(true);
        
        // Fade in the error label
        FadeTransition fade = new FadeTransition(Duration.millis(200), loginErrorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void showRegisterError(String message) {
        regErrorLabel.setText("⚠️ " + message);
        regErrorLabel.setVisible(true);
        
        // Fade in the error label
        FadeTransition fade = new FadeTransition(Duration.millis(200), regErrorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void showSuccessAndSwitchToLogin() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registro Exitoso");
        alert.setHeaderText("✨ ¡Bienvenido!");
        alert.setContentText("Su cuenta ha sido creada exitosamente. Por favor inicie sesión.");
        
        // Apply styles to dialog
        try {
            alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
            );
        } catch (Exception e) {
            // Ignore if styles cannot be loaded
        }
        
        alert.showAndWait();

        // Clear registration form with animation
        clearRegistrationForm();

        // Switch to login tab with animation
        tabPane.getSelectionModel().select(0);
    }
    
    /**
     * Clear registration form fields
     */
    private void clearRegistrationForm() {
        regIdField.clear();
        regNameField.clear();
        regEmailField.clear();
        regBirthDatePicker.setValue(null);
        regPasswordField.clear();
        regConfirmPasswordField.clear();
        regErrorLabel.setVisible(false);
    }
    
    /**
     * Play success animation before transitioning
     */
    private void playSuccessAnimation(Runnable onComplete) {
        // Scale up and fade out animation
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), mainContent);
        scale.setToX(0.95);
        scale.setToY(0.95);
        scale.setInterpolator(AnimationUtils.EASE_IN_OUT_CUBIC);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), mainContent);
        fade.setToValue(0);
        
        ParallelTransition exit = new ParallelTransition(scale, fade);
        exit.setOnFinished(e -> onComplete.run());
        exit.play();
    }

    private void applyStyles() {
        getStyleClass().add("login-view");
        setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2, #66a6ff);");
        
        // Apply shadow to main content panel
        DropShadow shadow = new DropShadow();
        shadow.setRadius(30);
        shadow.setOffsetY(15);
        shadow.setSpread(0.05);
        shadow.setColor(Color.rgb(0, 0, 0, 0.25));
        mainContent.setEffect(shadow);
    }
    
    /**
     * Play entrance animation for the login view
     */
    private void playEntranceAnimation() {
        mainContent.setOpacity(0);
        mainContent.setScaleX(0.9);
        mainContent.setScaleY(0.9);
        mainContent.setTranslateY(30);
        
        PauseTransition delay = new PauseTransition(Duration.millis(100));
        delay.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.millis(400), mainContent);
            fade.setToValue(1);
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(400), mainContent);
            scale.setToX(1);
            scale.setToY(1);
            scale.setInterpolator(AnimationUtils.EASE_OUT_CUBIC);
            
            TranslateTransition slide = new TranslateTransition(Duration.millis(400), mainContent);
            slide.setToY(0);
            slide.setInterpolator(AnimationUtils.EASE_OUT_CUBIC);
            
            new ParallelTransition(fade, scale, slide).play();
        });
        delay.play();
    }

    /**
     * Create a scene with the login view
     * @return the login scene
     */
    public Scene createScene() {
        Scene scene = new Scene(this, 500, 700);
        try {
            String cssPath = getClass().getResource("/css/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("Could not load CSS styles: " + e.getMessage());
        }
        return scene;
    }
}
