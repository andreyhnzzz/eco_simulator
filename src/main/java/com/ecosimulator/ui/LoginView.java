package com.ecosimulator.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Login view for user authentication.
 */
public class LoginView {
    
    private final MainController controller;
    private final Scene scene;
    
    private TextField cedulaField;
    private PasswordField passwordField;
    private Label messageLabel;

    public LoginView(MainController controller) {
        this.controller = controller;
        this.scene = createScene();
    }

    private Scene createScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label("Eco Simulator");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Iniciar Sesión");
        subtitleLabel.setStyle("-fx-font-size: 16px;");

        // Form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setAlignment(Pos.CENTER);

        Label cedulaLabel = new Label("Cédula:");
        cedulaField = new TextField();
        cedulaField.setPromptText("Ingrese su cédula");

        Label passwordLabel = new Label("Contraseña:");
        passwordField = new PasswordField();
        passwordField.setPromptText("Ingrese su contraseña");

        form.add(cedulaLabel, 0, 0);
        form.add(cedulaField, 1, 0);
        form.add(passwordLabel, 0, 1);
        form.add(passwordField, 1, 1);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button loginButton = new Button("Ingresar");
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(e -> handleLogin());

        Button registerButton = new Button("Registrarse");
        registerButton.setOnAction(e -> controller.showRegistroView());

        buttonBox.getChildren().addAll(loginButton, registerButton);

        // Message
        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(titleLabel, subtitleLabel, form, buttonBox, messageLabel);

        return new Scene(root);
    }

    private void handleLogin() {
        String cedula = cedulaField.getText().trim();
        String password = passwordField.getText();

        if (cedula.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Por favor complete todos los campos");
            return;
        }

        if (controller.login(cedula, password)) {
            messageLabel.setText("");
            controller.showScenarioSelection();
        } else {
            messageLabel.setText("Credenciales inválidas");
            passwordField.clear();
        }
    }

    public Scene getScene() {
        return scene;
    }
}
