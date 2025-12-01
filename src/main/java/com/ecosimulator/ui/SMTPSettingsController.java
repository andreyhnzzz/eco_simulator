package com.ecosimulator.ui;

import com.ecosimulator.service.EmailService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.logging.Logger;

/**
 * SMTP Settings dialog controller for configuring email service.
 * Provides UI for entering SMTP credentials and testing the connection.
 */
public class SMTPSettingsController {
    private static final Logger LOGGER = Logger.getLogger(SMTPSettingsController.class.getName());

    private final EmailService emailService;
    private final Stage dialog;

    // Form fields
    private TextField hostField;
    private TextField portField;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField fromAddressField;
    private CheckBox startTlsCheckBox;
    private CheckBox sslCheckBox;
    private Label statusLabel;
    private Button testButton;
    private Button saveButton;

    /**
     * Create a new SMTP Settings controller.
     * 
     * @param owner Parent stage for the dialog
     * @param emailService Email service to configure
     */
    public SMTPSettingsController(Stage owner, EmailService emailService) {
        this.emailService = emailService;
        this.dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("SMTP Settings");
        dialog.setResizable(false);
        
        initializeUI();
        loadCurrentSettings();
    }

    /**
     * Show the settings dialog.
     */
    public void show() {
        dialog.showAndWait();
    }

    private void initializeUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Title
        Label titleLabel = new Label("📧 SMTP Email Configuration");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Info text
        Label infoLabel = new Label(
            "Configure SMTP settings to enable email sending.\n" +
            "For Gmail: Use smtp.gmail.com:587 with STARTTLS and an App Password.\n" +
            "For local testing: Use MailHog at localhost:1025 (no auth)."
        );
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        // Form grid
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER_LEFT);

        int row = 0;

        // Host
        formGrid.add(new Label("SMTP Host:"), 0, row);
        hostField = new TextField();
        hostField.setPromptText("e.g., smtp.gmail.com or localhost");
        hostField.setPrefWidth(250);
        formGrid.add(hostField, 1, row++);

        // Port
        formGrid.add(new Label("Port:"), 0, row);
        portField = new TextField();
        portField.setPromptText("587 (STARTTLS) or 465 (SSL) or 1025 (MailHog)");
        portField.setPrefWidth(250);
        formGrid.add(portField, 1, row++);

        // Username
        formGrid.add(new Label("Username:"), 0, row);
        usernameField = new TextField();
        usernameField.setPromptText("your.email@example.com");
        usernameField.setPrefWidth(250);
        formGrid.add(usernameField, 1, row++);

        // Password
        formGrid.add(new Label("Password:"), 0, row);
        passwordField = new PasswordField();
        passwordField.setPromptText("App Password or SMTP password");
        passwordField.setPrefWidth(250);
        formGrid.add(passwordField, 1, row++);

        // From Address
        formGrid.add(new Label("From Address:"), 0, row);
        fromAddressField = new TextField();
        fromAddressField.setPromptText("(Optional) sender@example.com");
        fromAddressField.setPrefWidth(250);
        formGrid.add(fromAddressField, 1, row++);

        // TLS/SSL options
        HBox securityBox = new HBox(20);
        securityBox.setAlignment(Pos.CENTER_LEFT);
        
        startTlsCheckBox = new CheckBox("Use STARTTLS (port 587)");
        startTlsCheckBox.setSelected(true);
        startTlsCheckBox.setOnAction(e -> {
            if (startTlsCheckBox.isSelected()) {
                sslCheckBox.setSelected(false);
            }
        });

        sslCheckBox = new CheckBox("Use SSL (port 465)");
        sslCheckBox.setOnAction(e -> {
            if (sslCheckBox.isSelected()) {
                startTlsCheckBox.setSelected(false);
            }
        });

        securityBox.getChildren().addAll(startTlsCheckBox, sslCheckBox);
        formGrid.add(new Label("Security:"), 0, row);
        formGrid.add(securityBox, 1, row++);

        // Status label
        statusLabel = new Label("");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(400);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        testButton = new Button("🔌 Test Connection");
        testButton.setOnAction(e -> testConnection());
        testButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        saveButton = new Button("💾 Save & Close");
        saveButton.setOnAction(e -> saveAndClose());
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(testButton, saveButton, cancelButton);

        // Presets section
        HBox presetBox = new HBox(10);
        presetBox.setAlignment(Pos.CENTER_LEFT);
        
        Label presetLabel = new Label("Quick Setup:");
        presetLabel.setStyle("-fx-font-weight: bold;");
        
        Button gmailPreset = new Button("Gmail");
        gmailPreset.setOnAction(e -> applyGmailPreset());
        
        Button mailhogPreset = new Button("MailHog (Local)");
        mailhogPreset.setOnAction(e -> applyMailHogPreset());
        
        presetBox.getChildren().addAll(presetLabel, gmailPreset, mailhogPreset);

        // Add all to root
        root.getChildren().addAll(
            titleLabel,
            infoLabel,
            new Separator(),
            presetBox,
            new Separator(),
            formGrid,
            statusLabel,
            new Separator(),
            buttonBox
        );

        Scene scene = new Scene(root, 480, 450);
        dialog.setScene(scene);
    }

    private void loadCurrentSettings() {
        if (emailService.isConfigured()) {
            hostField.setText(emailService.getSmtpHost() != null ? emailService.getSmtpHost() : "");
            portField.setText(String.valueOf(emailService.getSmtpPort()));
            usernameField.setText(emailService.getUsername() != null ? emailService.getUsername() : "");
            fromAddressField.setText(emailService.getFromAddress() != null ? emailService.getFromAddress() : "");
            startTlsCheckBox.setSelected(emailService.isUseStartTls());
            sslCheckBox.setSelected(emailService.isUseSsl());
            statusLabel.setText("✅ Current configuration loaded");
            statusLabel.setStyle("-fx-text-fill: green;");
        }
    }

    private void applyGmailPreset() {
        hostField.setText("smtp.gmail.com");
        portField.setText("587");
        startTlsCheckBox.setSelected(true);
        sslCheckBox.setSelected(false);
        statusLabel.setText("Gmail preset applied. Enter your email and App Password.\n" +
                           "Note: You need to enable 2FA and create an App Password at\n" +
                           "https://myaccount.google.com/apppasswords");
        statusLabel.setStyle("-fx-text-fill: #1976D2;");
    }

    private void applyMailHogPreset() {
        hostField.setText("localhost");
        portField.setText("1025");
        usernameField.setText("");
        passwordField.setText("");
        fromAddressField.setText("test@localhost");
        startTlsCheckBox.setSelected(false);
        sslCheckBox.setSelected(false);
        statusLabel.setText("MailHog preset applied. Start MailHog with:\n" +
                           "docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog\n" +
                           "View emails at http://localhost:8025");
        statusLabel.setStyle("-fx-text-fill: #1976D2;");
    }

    private void testConnection() {
        testButton.setDisable(true);
        statusLabel.setText("⏳ Testing connection...");
        statusLabel.setStyle("-fx-text-fill: #666;");

        // Apply current form values
        applyFormValues();

        // Test in background thread
        new Thread(() -> {
            boolean success = emailService.testConnection();
            
            Platform.runLater(() -> {
                testButton.setDisable(false);
                if (success) {
                    statusLabel.setText("✅ Connection successful! SMTP server is reachable.");
                    statusLabel.setStyle("-fx-text-fill: green;");
                } else {
                    statusLabel.setText("❌ Connection failed. Check your settings and credentials.\n" +
                                       "Make sure the SMTP server is accessible.");
                    statusLabel.setStyle("-fx-text-fill: red;");
                }
            });
        }).start();
    }

    private void applyFormValues() {
        String host = hostField.getText().trim();
        int port = 587;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            // Use default
        }
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String fromAddress = fromAddressField.getText().trim();
        if (fromAddress.isEmpty()) {
            fromAddress = username;
        }
        boolean useStartTls = startTlsCheckBox.isSelected();
        boolean useSsl = sslCheckBox.isSelected();

        emailService.configureSmtp(host, port, username, password, fromAddress, useStartTls, useSsl);
    }

    private void saveAndClose() {
        applyFormValues();
        
        // Save configuration to file
        boolean saved = emailService.saveConfiguration();
        
        if (saved) {
            statusLabel.setText("✅ Configuration saved!");
            statusLabel.setStyle("-fx-text-fill: green;");
            
            // Show confirmation and close
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Settings Saved");
            alert.setHeaderText("SMTP Configuration Saved");
            alert.setContentText("Your SMTP settings have been saved.\n\n" +
                               "Note: Password is not saved to disk for security.\n" +
                               "You'll need to re-enter it after restarting the app,\n" +
                               "or set the SMTP_PASSWORD environment variable.");
            alert.showAndWait();
            
            dialog.close();
        } else {
            statusLabel.setText("⚠️ Could not save configuration file.");
            statusLabel.setStyle("-fx-text-fill: orange;");
        }
    }

    /**
     * Create and show the SMTP settings dialog.
     * 
     * @param owner Parent stage
     * @param emailService Email service to configure
     */
    public static void showDialog(Stage owner, EmailService emailService) {
        SMTPSettingsController controller = new SMTPSettingsController(owner, emailService);
        controller.show();
    }
}
