package com.ecosimulator.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class for sending emails using JavaMail
 * Configuration is read from config/app.properties
 */
public class MailSender {
    
    private Properties mailProperties;
    private String smtpHost;
    private int smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private String fromAddress;
    private boolean useStartTls;
    private boolean useSsl;
    
    public MailSender() {
        loadConfiguration();
    }
    
    /**
     * Load email configuration from config/app.properties
     */
    private void loadConfiguration() {
        Properties appProps = new Properties();
        try (FileInputStream fis = new FileInputStream("config/app.properties")) {
            appProps.load(fis);
            
            this.smtpHost = appProps.getProperty("mail.smtp.host", "localhost");
            this.smtpPort = Integer.parseInt(appProps.getProperty("mail.smtp.port", "587"));
            this.smtpUsername = appProps.getProperty("mail.smtp.username", "");
            this.smtpPassword = appProps.getProperty("mail.smtp.password", "");
            this.fromAddress = appProps.getProperty("mail.smtp.from", "noreply@ecosimulator.com");
            this.useStartTls = Boolean.parseBoolean(appProps.getProperty("mail.smtp.starttls", "false"));
            this.useSsl = Boolean.parseBoolean(appProps.getProperty("mail.smtp.ssl", "false"));
            
            // Set up mail properties
            this.mailProperties = new Properties();
            mailProperties.put("mail.smtp.host", smtpHost);
            mailProperties.put("mail.smtp.port", String.valueOf(smtpPort));
            mailProperties.put("mail.smtp.auth", !smtpUsername.isEmpty());
            mailProperties.put("mail.smtp.starttls.enable", useStartTls);
            mailProperties.put("mail.smtp.ssl.enable", useSsl);
            
        } catch (IOException e) {
            System.err.println("Could not load mail configuration: " + e.getMessage());
            // Use defaults
            this.smtpHost = "localhost";
            this.smtpPort = 1025;
            this.fromAddress = "noreply@ecosimulator.com";
            this.mailProperties = new Properties();
            mailProperties.put("mail.smtp.host", smtpHost);
            mailProperties.put("mail.smtp.port", String.valueOf(smtpPort));
        }
    }
    
    /**
     * Send an email with attachment
     * @param toAddress recipient email address
     * @param subject email subject
     * @param body email body (plain text)
     * @param attachmentPath path to attachment file (optional)
     * @return true if email sent successfully
     */
    public boolean sendEmail(String toAddress, String subject, String body, String attachmentPath) {
        try {
            // Create session
            Session session;
            if (!smtpUsername.isEmpty()) {
                session = Session.getInstance(mailProperties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUsername, smtpPassword);
                    }
                });
            } else {
                session = Session.getInstance(mailProperties);
            }
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            message.setSubject(subject);
            
            // Create multipart message
            Multipart multipart = new MimeMultipart();
            
            // Add text body
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);
            multipart.addBodyPart(textPart);
            
            // Add attachment if provided
            if (attachmentPath != null && !attachmentPath.isEmpty()) {
                File attachment = new File(attachmentPath);
                if (attachment.exists()) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.attachFile(attachment);
                    multipart.addBodyPart(attachmentPart);
                }
            }
            
            message.setContent(multipart);
            
            // Send message
            Transport.send(message);
            
            System.out.println("Email sent successfully to: " + toAddress);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Send a simple email without attachment
     * @param toAddress recipient email address
     * @param subject email subject
     * @param body email body
     * @return true if successful
     */
    public boolean sendEmail(String toAddress, String subject, String body) {
        return sendEmail(toAddress, subject, body, null);
    }
    
    /**
     * Test the email configuration
     * @return true if connection successful
     */
    public boolean testConnection() {
        try {
            Session session;
            if (!smtpUsername.isEmpty()) {
                session = Session.getInstance(mailProperties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUsername, smtpPassword);
                    }
                });
            } else {
                session = Session.getInstance(mailProperties);
            }
            
            Transport transport = session.getTransport("smtp");
            transport.connect(smtpHost, smtpPort, smtpUsername, smtpPassword);
            transport.close();
            
            System.out.println("SMTP connection test successful");
            return true;
            
        } catch (Exception e) {
            System.err.println("SMTP connection test failed: " + e.getMessage());
            return false;
        }
    }
}
