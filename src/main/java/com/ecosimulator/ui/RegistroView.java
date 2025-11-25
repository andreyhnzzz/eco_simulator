package com.ecosimulator.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;

/**
 * Registration view for new users.
 */
public class RegistroView {
    
    private final MainController controller;
    private final Scene scene;
    
    private TextField cedulaField;
    private TextField nombreField;
    private DatePicker fechaNacimientoPicker;
    private ComboBox<String> generoCombo;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField correoField;
    private Label messageLabel;

    public RegistroView(MainController controller) {
        this.controller = controller;
        this.scene = createScene();
    }

    private Scene createScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label("Registro de Usuario");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setAlignment(Pos.CENTER);

        int row = 0;

        // Cedula
        form.add(new Label("Cédula:"), 0, row);
        cedulaField = new TextField();
        cedulaField.setPromptText("Número de cédula");
        form.add(cedulaField, 1, row++);

        // Nombre
        form.add(new Label("Nombre:"), 0, row);
        nombreField = new TextField();
        nombreField.setPromptText("Nombre completo");
        form.add(nombreField, 1, row++);

        // Fecha Nacimiento
        form.add(new Label("Fecha Nacimiento:"), 0, row);
        fechaNacimientoPicker = new DatePicker();
        fechaNacimientoPicker.setValue(LocalDate.now().minusYears(18));
        form.add(fechaNacimientoPicker, 1, row++);

        // Genero
        form.add(new Label("Género:"), 0, row);
        generoCombo = new ComboBox<>();
        generoCombo.getItems().addAll("Masculino", "Femenino", "Otro");
        generoCombo.setValue("Masculino");
        form.add(generoCombo, 1, row++);

        // Correo
        form.add(new Label("Correo:"), 0, row);
        correoField = new TextField();
        correoField.setPromptText("correo@ejemplo.com");
        form.add(correoField, 1, row++);

        // Password
        form.add(new Label("Contraseña:"), 0, row);
        passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña");
        form.add(passwordField, 1, row++);

        // Confirm Password
        form.add(new Label("Confirmar:"), 0, row);
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmar contraseña");
        form.add(confirmPasswordField, 1, row++);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button registerButton = new Button("Registrar");
        registerButton.setDefaultButton(true);
        registerButton.setOnAction(e -> handleRegister());

        Button backButton = new Button("Volver");
        backButton.setOnAction(e -> controller.showLoginView());

        buttonBox.getChildren().addAll(registerButton, backButton);

        // Message
        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(titleLabel, form, buttonBox, messageLabel);

        return new Scene(root);
    }

    private void handleRegister() {
        String cedula = cedulaField.getText().trim();
        String nombre = nombreField.getText().trim();
        LocalDate fechaNacimiento = fechaNacimientoPicker.getValue();
        String genero = generoCombo.getValue();
        String correo = correoField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (cedula.isEmpty() || nombre.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Por favor complete todos los campos");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Las contraseñas no coinciden");
            return;
        }

        if (password.length() < 4) {
            messageLabel.setText("La contraseña debe tener al menos 4 caracteres");
            return;
        }

        if (!correo.contains("@")) {
            messageLabel.setText("Correo inválido");
            return;
        }

        var usuario = controller.registrar(cedula, nombre, fechaNacimiento, genero, password, correo);
        
        if (usuario != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registro exitoso");
            alert.setHeaderText(null);
            alert.setContentText("Usuario registrado correctamente. Ahora puede iniciar sesión.");
            alert.showAndWait();
            controller.showLoginView();
        } else {
            messageLabel.setText("Error al registrar. La cédula ya existe.");
        }
    }

    public Scene getScene() {
        return scene;
    }
}
