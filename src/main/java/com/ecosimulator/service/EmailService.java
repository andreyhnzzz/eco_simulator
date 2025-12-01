package com.ecosimulator.service;

import com.ecosimulator.auth.User;
import com.ecosimulator.model.SimulationStats;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Production-ready email service for sending simulation reports.
 * Supports SMTP configuration with STARTTLS (port 587) and SSL (port 465).
 * Includes single retry on transient failures and graceful fallback to disk.
 * 
 * Configuration options:
 * - For Gmail: Use App Password (not regular password), smtp.gmail.com:587, STARTTLS
 * - For MailHog (local testing): localhost:1025, no auth required
 * - For other providers: Configure host, port, and credentials accordingly
 */
public class EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static final String OUTGOING_REPORTS_DIR = "outgoing_reports";
    private static final String CONFIG_FILE = "config/smtp.properties";
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int IO_TIMEOUT = 10000;
    private static final int RETRY_DELAY_MS = 2000;

    private String smtpHost;
    private int smtpPort;
    private String username;
    private String password;
    private String fromAddress;
    private boolean useStartTls;
    private boolean useSsl;
    private boolean configured;

    public EmailService() {
        this.configured = false;
        this.useStartTls = true;
        this.useSsl = false;
        loadConfiguration();
    }

    /**
     * Configure SMTP settings for email sending.
     * This method provides a simple programmatic way to configure SMTP.
     * 
     * @param host SMTP server host
     * @param port SMTP server port (typically 587 for STARTTLS, 465 for SSL)
     * @param username SMTP username for authentication
     * @param password SMTP password for authentication
     */
    public void configureSmtp(String host, int port, String username, String password) {
        configureSmtp(host, port, username, password, username, true, false);
    }

    /**
     * Configure SMTP settings with all options.
     * 
     * @param host SMTP server host
     * @param port SMTP server port
     * @param username SMTP username
     * @param password SMTP password
     * @param fromAddress Email address to send from
     * @param useStartTls Whether to use STARTTLS (recommended for port 587)
     * @param useSsl Whether to use SSL/TLS (for port 465)
     */
    public void configureSmtp(String host, int port, String username, String password, 
                               String fromAddress, boolean useStartTls, boolean useSsl) {
        this.smtpHost = host;
        this.smtpPort = port;
        this.username = username;
        this.password = password;
        this.fromAddress = fromAddress != null ? fromAddress : username;
        this.useStartTls = useStartTls;
        this.useSsl = useSsl;
        this.configured = host != null && !host.isEmpty();
        
        LOGGER.info("SMTP configured: " + host + ":" + port + 
                   " (STARTTLS=" + useStartTls + ", SSL=" + useSsl + ")");
    }

    /**
     * Load SMTP configuration from environment variables or config file.
     * Environment variables take precedence over config file.
     */
    private void loadConfiguration() {
        // Try environment variables first
        String envHost = System.getenv("SMTP_HOST");
        if (envHost != null && !envHost.isEmpty()) {
            this.smtpHost = envHost;
            this.smtpPort = parseIntOrDefault(System.getenv("SMTP_PORT"), 587);
            this.username = System.getenv("SMTP_USERNAME");
            this.password = System.getenv("SMTP_PASSWORD");
            this.fromAddress = System.getenv("SMTP_FROM_ADDRESS");
            if (this.fromAddress == null) this.fromAddress = this.username;
            this.useStartTls = parseBooleanOrDefault(System.getenv("SMTP_STARTTLS"), true);
            this.useSsl = parseBooleanOrDefault(System.getenv("SMTP_SSL"), false);
            this.configured = true;
            LOGGER.info("SMTP configuration loaded from environment variables");
            return;
        }

        // Try config file
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (InputStream is = new FileInputStream(configFile)) {
                Properties props = new Properties();
                props.load(is);
                this.smtpHost = props.getProperty("smtp.host");
                this.smtpPort = parseIntOrDefault(props.getProperty("smtp.port"), 587);
                this.username = props.getProperty("smtp.username");
                this.password = props.getProperty("smtp.password");
                this.fromAddress = props.getProperty("smtp.from", this.username);
                this.useStartTls = parseBooleanOrDefault(props.getProperty("smtp.starttls"), true);
                this.useSsl = parseBooleanOrDefault(props.getProperty("smtp.ssl"), false);
                this.configured = smtpHost != null && !smtpHost.isEmpty();
                if (this.configured) {
                    LOGGER.info("SMTP configuration loaded from " + CONFIG_FILE);
                }
            } catch (IOException e) {
                LOGGER.warning("Failed to load SMTP configuration: " + e.getMessage());
            }
        }
    }

    /**
     * Save current SMTP configuration to config file.
     * Does not save password to file for security.
     * 
     * @return true if saved successfully
     */
    public boolean saveConfiguration() {
        try {
            Path configDir = Paths.get("config");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Properties props = new Properties();
            props.setProperty("smtp.host", smtpHost != null ? smtpHost : "");
            props.setProperty("smtp.port", String.valueOf(smtpPort));
            props.setProperty("smtp.username", username != null ? username : "");
            // Note: Password is intentionally not saved for security
            props.setProperty("smtp.from", fromAddress != null ? fromAddress : "");
            props.setProperty("smtp.starttls", String.valueOf(useStartTls));
            props.setProperty("smtp.ssl", String.valueOf(useSsl));

            try (OutputStream os = new FileOutputStream(CONFIG_FILE)) {
                props.store(os, "SMTP Configuration - Password must be configured separately");
            }
            LOGGER.info("SMTP configuration saved to " + CONFIG_FILE);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save SMTP configuration", e);
            return false;
        }
    }

    /**
     * Test the SMTP connection by attempting to connect to the server.
     * 
     * @return true if connection successful
     */
    public boolean testConnection() {
        if (!configured) {
            LOGGER.warning("Cannot test connection: SMTP not configured");
            return false;
        }

        Properties props = buildSmtpProperties();
        Session session = createSession(props);

        try {
            Transport transport = session.getTransport("smtp");
            transport.connect(smtpHost, smtpPort, username, password);
            transport.close();
            LOGGER.info("SMTP connection test successful");
            return true;
        } catch (MessagingException e) {
            LOGGER.log(Level.WARNING, "SMTP connection test failed: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send a report email to the specified recipient with a PDF attachment.
     * Includes single retry on transient failures and fallback to disk on permanent failure.
     * 
     * @param recipientEmail Recipient's email address
     * @param pdf PDF file to attach
     * @param subject Email subject
     * @param body Email body text
     * @return true if email sent successfully, false if failed (PDF saved to fallback location)
     */
    public boolean sendReport(String recipientEmail, File pdf, String subject, String body) {
        if (!configured) {
            LOGGER.warning("Email service not configured. Saving PDF to fallback location.");
            savePdfToFallback(recipientEmail, pdf);
            return false;
        }

        if (recipientEmail == null || recipientEmail.isEmpty()) {
            LOGGER.warning("Invalid recipient email address.");
            return false;
        }

        // First attempt
        boolean success = attemptSend(recipientEmail, pdf, subject, body);
        
        if (!success) {
            // Retry once after delay
            LOGGER.info("First send attempt failed, retrying after " + RETRY_DELAY_MS + "ms...");
            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            success = attemptSend(recipientEmail, pdf, subject, body);
        }

        if (!success) {
            // Save to fallback location
            savePdfToFallback(recipientEmail, pdf);
        }

        return success;
    }

    /**
     * Send simulation report via email with PDF attachment.
     * 
     * @param user the recipient user
     * @param pdfFile the PDF report file to attach
     * @return true if email was sent successfully
     */
    public boolean sendReport(User user, File pdfFile) {
        if (user == null || user.getEmail() == null) {
            LOGGER.warning("Invalid user or email address.");
            return false;
        }

        String subject = "Eco Simulator - Simulation Report";
        String body = "Hello " + user.getName() + ",\n\n" +
                      "Please find attached your Eco Simulator report.\n\n" +
                      "Best regards,\nEco Simulator";

        return sendReport(user.getEmail(), pdfFile, subject, body);
    }

    /**
     * Send simulation report via email
     */
    public boolean sendSimulationReport(String toEmail, String subject, SimulationStats stats) {
        if (!configured) {
            LOGGER.warning("Email service not configured. Please configure SMTP settings.");
            return false;
        }

        String body = formatReportBody(stats);
        return sendEmail(toEmail, subject, body);
    }

    /**
     * Send a generic email without attachment.
     */
    public boolean sendEmail(String toEmail, String subject, String body) {
        if (!configured) {
            LOGGER.warning("Email service not configured.");
            return false;
        }

        return attemptSend(toEmail, null, subject, body);
    }

    /**
     * Attempt to send an email with optional attachment.
     */
    private boolean attemptSend(String toEmail, File attachment, String subject, String body) {
        try {
            Properties props = buildSmtpProperties();
            Session session = createSession(props);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress != null ? fromAddress : username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setSentDate(new java.util.Date());

            if (attachment != null && attachment.exists()) {
                Multipart multipart = new MimeMultipart();

                // Text part
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(body, "UTF-8");
                multipart.addBodyPart(textPart);

                // Attachment part
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(attachment);
                
                // Set content type based on file
                String contentType = Files.probeContentType(attachment.toPath());
                if (contentType == null) {
                    contentType = "application/pdf";
                }
                attachmentPart.setHeader("Content-Type", contentType);
                
                multipart.addBodyPart(attachmentPart);
                message.setContent(multipart);
            } else {
                message.setText(body);
            }

            Transport.send(message);
            
            LOGGER.info("Email sent successfully to: " + toEmail);
            return true;

        } catch (MessagingException | IOException e) {
            LOGGER.log(Level.WARNING, "Failed to send email to " + toEmail + ": " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Build SMTP properties based on configuration.
     */
    private Properties buildSmtpProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.connectiontimeout", String.valueOf(CONNECTION_TIMEOUT));
        props.put("mail.smtp.timeout", String.valueOf(IO_TIMEOUT));
        
        // Authentication - only if credentials provided
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            props.put("mail.smtp.auth", "true");
        } else {
            props.put("mail.smtp.auth", "false");
        }

        // SSL/TLS configuration
        if (useSsl) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", String.valueOf(smtpPort));
        } else if (useStartTls) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            props.put("mail.smtp.ssl.trust", smtpHost);
        }

        return props;
    }

    /**
     * Create mail session with authentication if credentials provided.
     */
    private Session createSession(Properties props) {
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        }
        return Session.getInstance(props);
    }

    /**
     * Save PDF to fallback location when email fails.
     * Pattern: recipient_sanitized_originalname_timestamp.pdf
     */
    private void savePdfToFallback(String recipientEmail, File pdf) {
        try {
            Path fallbackDir = Paths.get(OUTGOING_REPORTS_DIR);
            if (!Files.exists(fallbackDir)) {
                Files.createDirectories(fallbackDir);
            }

            String sanitizedRecipient = recipientEmail != null 
                ? recipientEmail.replaceAll("[^a-zA-Z0-9]", "_") 
                : "unknown";
            String originalName = pdf != null ? pdf.getName() : "report.pdf";
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fallbackName = sanitizedRecipient + "_" + 
                                  originalName.replace(".pdf", "") + "_" + 
                                  timestamp + ".pdf";

            Path fallbackPath = fallbackDir.resolve(fallbackName);

            if (pdf != null && pdf.exists()) {
                Files.copy(pdf.toPath(), fallbackPath);
                LOGGER.info("PDF saved to fallback location: " + fallbackPath.toAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save PDF to fallback location", e);
        }
    }

    /**
     * Format simulation statistics into an email body.
     */
    private String formatReportBody(SimulationStats stats) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Eco Simulator Report ===\n\n");
        sb.append(String.format("Turn: %d\n", stats.getTurn()));
        sb.append(String.format("Predators: %d\n", stats.getPredatorCount()));
        sb.append(String.format("Prey: %d\n", stats.getPreyCount()));
        sb.append(String.format("Third Species: %d\n", stats.getThirdSpeciesCount()));
        sb.append(String.format("Mutated Creatures: %d\n", stats.getMutatedCount()));
        sb.append(String.format("Total Creatures: %d\n\n", stats.getTotalCreatures()));
        sb.append(String.format("Status: %s\n", stats.getWinner()));
        sb.append("\n--- End of Report ---\n");
        return sb.toString();
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean parseBooleanOrDefault(String value, boolean defaultValue) {
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }

    // Getters for UI configuration

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public String getUsername() {
        return username;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public boolean isUseStartTls() {
        return useStartTls;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public boolean isConfigured() {
        return configured;
    }

    public String getConfigurationStatus() {
        if (!configured) {
            return "Email service not configured";
        }
        return "Using SMTP: " + smtpHost + ":" + smtpPort + 
               (useStartTls ? " (STARTTLS)" : "") + 
               (useSsl ? " (SSL)" : "");
    }

    /**
     * Get the fallback directory path where unsent reports are saved.
     */
    public static String getFallbackDirectory() {
        return OUTGOING_REPORTS_DIR;
    }
}
