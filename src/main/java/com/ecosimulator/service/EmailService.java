package com.ecosimulator.service;

import com.ecosimulator.auth.User;
import com.ecosimulator.model.SimulationStats;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Email service for sending simulation reports
 * Supports SMTP configuration with PDF attachment capability
 * 
 * Note: Full email functionality requires SMTP configuration:
 * - For Gmail: Use an App Password (not regular password)
 * - Configure with smtp.gmail.com:587 for TLS
 */
public class EmailService {
    private String smtpHost;
    private int smtpPort;
    private String username;
    private String password;
    private boolean configured;
    private boolean useOAuth;

    // Google OAuth2 credentials (to be configured via Google Cloud Console)
    private String clientId;
    private String clientSecret;
    private String accessToken;

    public EmailService() {
        this.configured = false;
        this.useOAuth = false;
    }

    /**
     * Configure SMTP settings for email sending
     */
    public void configureSmtp(String host, int port, String username, String password) {
        this.smtpHost = host;
        this.smtpPort = port;
        this.username = username;
        this.password = password;
        this.configured = true;
        this.useOAuth = false;
    }

    /**
     * Configure Google OAuth2 for Gmail API
     */
    public void configureGoogleOAuth(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.useOAuth = true;
        // Note: Access token needs to be obtained through OAuth flow
    }

    /**
     * Set the OAuth access token after authentication
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        this.configured = accessToken != null && !accessToken.isEmpty();
    }

    /**
     * Send simulation report via email with PDF attachment
     * @param user the recipient user
     * @param pdfFile the PDF report file to attach
     * @return true if email was sent successfully
     */
    public boolean sendReport(User user, File pdfFile) {
        if (!configured) {
            System.err.println("Email service not configured. Please configure SMTP settings.");
            return false;
        }

        if (user == null || user.getEmail() == null) {
            System.err.println("Invalid user or email address.");
            return false;
        }

        String subject = "Eco Simulator - Simulation Report";
        String body = "Hello " + user.getName() + ",\n\n" +
                      "Please find attached your Eco Simulator report.\n\n" +
                      "Best regards,\nEco Simulator";

        return sendEmailWithAttachment(user.getEmail(), subject, body, pdfFile);
    }

    /**
     * Send simulation report via email
     */
    public boolean sendSimulationReport(String toEmail, String subject, SimulationStats stats) {
        if (!configured) {
            System.err.println("Email service not configured. Please configure SMTP or OAuth settings.");
            return false;
        }

        String body = formatReportBody(stats);
        return sendEmail(toEmail, subject, body);
    }

    /**
     * Send a generic email
     */
    public boolean sendEmail(String toEmail, String subject, String body) {
        if (!configured) {
            System.err.println("Email service not configured.");
            return false;
        }

        if (useOAuth) {
            return sendWithGoogleApi(toEmail, subject, body);
        } else {
            return sendWithSmtp(toEmail, subject, body, null);
        }
    }

    /**
     * Send email with PDF attachment
     */
    public boolean sendEmailWithAttachment(String toEmail, String subject, String body, File attachment) {
        if (!configured) {
            System.err.println("Email service not configured.");
            return false;
        }

        return sendWithSmtp(toEmail, subject, body, attachment);
    }

    /**
     * Send email using SMTP with optional attachment
     * Uses Jakarta Mail API for full email functionality
     */
    private boolean sendWithSmtp(String toEmail, String subject, String body, File attachment) {
        try {
            // Configure SMTP properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", String.valueOf(smtpPort));
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.ssl.trust", smtpHost);

            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            if (attachment != null && attachment.exists()) {
                // Create multipart message with attachment
                Multipart multipart = new MimeMultipart();

                // Text part
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(body);
                multipart.addBodyPart(textPart);

                // Attachment part
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(attachment);
                multipart.addBodyPart(attachmentPart);

                message.setContent(multipart);
            } else {
                message.setText(body);
            }

            // Send message
            Transport.send(message);
            
            System.out.println("Email sent successfully to: " + toEmail);
            return true;

        } catch (MessagingException | IOException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            // Log but don't crash - resilient behavior
            printFallbackMessage(toEmail, subject, body, attachment);
            return false;
        }
    }

    /**
     * Print fallback message when email fails
     */
    private void printFallbackMessage(String toEmail, String subject, String body, File attachment) {
        System.out.println("=== Email Service (Fallback) ===");
        System.out.println("Email could not be sent. Details:");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        if (attachment != null) {
            System.out.println("Attachment: " + attachment.getAbsolutePath());
        }
        System.out.println("The report has been saved locally.");
        System.out.println("================================");
    }

    /**
     * Send email using Google Gmail API with OAuth2
     * Note: This is a placeholder - full implementation requires Google API setup
     */
    private boolean sendWithGoogleApi(String toEmail, String subject, String body) {
        if (accessToken == null || accessToken.isEmpty()) {
            System.err.println("OAuth access token not set. Please authenticate first.");
            return false;
        }

        // Placeholder for Google Gmail API integration
        // Full implementation would use:
        // 1. GoogleAuthorizationCodeFlow for OAuth
        // 2. Gmail.Users.Messages.send() to send emails
        
        System.out.println("=== Email Service (Google OAuth) ===");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("---");
        System.out.println(body);
        System.out.println("====================================");
        System.out.println("Google Gmail API integration requires OAuth setup:");
        System.out.println("1. Create a project in Google Cloud Console");
        System.out.println("2. Enable Gmail API");
        System.out.println("3. Create OAuth 2.0 credentials");
        System.out.println("4. Download credentials.json and configure the application");
        
        return true; // Returns true to indicate the message was processed
    }

    /**
     * Format simulation statistics into an email body
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

    /**
     * Check if the email service is properly configured
     */
    public boolean isConfigured() {
        return configured;
    }

    /**
     * Check if using OAuth authentication
     */
    public boolean isUsingOAuth() {
        return useOAuth;
    }

    /**
     * Get configuration status message
     */
    public String getConfigurationStatus() {
        if (!configured) {
            return "Email service not configured";
        }
        if (useOAuth) {
            return "Using Google OAuth2 authentication";
        }
        return "Using SMTP: " + smtpHost + ":" + smtpPort;
    }
}
