package com.ecosimulator.service;

import com.ecosimulator.model.SimulationStats;
import java.util.Properties;

/**
 * Email service for sending simulation reports
 * Supports SMTP configuration and Google OAuth2 authentication
 * 
 * Note: Full email functionality requires additional configuration:
 * - For SMTP: Configure your email server settings
 * - For Google OAuth: Set up Google Cloud project and credentials
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
            return sendWithSmtp(toEmail, subject, body);
        }
    }

    /**
     * Send email using SMTP
     * Note: This is a placeholder - full implementation requires Jakarta Mail API
     */
    private boolean sendWithSmtp(String toEmail, String subject, String body) {
        // Placeholder for SMTP email sending
        // Full implementation would use jakarta.mail API:
        // 1. Create Properties with SMTP settings
        // 2. Create Session with Authenticator
        // 3. Create MimeMessage
        // 4. Use Transport.send()
        
        System.out.println("=== Email Service (SMTP) ===");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Host: " + smtpHost + ":" + smtpPort);
        System.out.println("---");
        System.out.println(body);
        System.out.println("===========================");
        System.out.println("Note: SMTP sending requires Jakarta Mail API configuration.");
        System.out.println("Add 'requires jakarta.mail;' to module-info.java when ready.");
        
        return true; // Returns true to indicate the message was processed
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
