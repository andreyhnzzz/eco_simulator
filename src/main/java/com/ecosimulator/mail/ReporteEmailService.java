package com.ecosimulator.mail;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Email service for sending report PDFs.
 * Uses Jakarta Mail (JavaMail).
 * Fails gracefully without blocking the application.
 */
public class ReporteEmailService {
    
    private final String smtpHost;
    private final int smtpPort;
    private final String username;
    private final String password;
    private final boolean useTLS;

    public ReporteEmailService(String smtpHost, int smtpPort, String username, String password, boolean useTLS) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
        this.useTLS = useTLS;
    }

    /**
     * Create a default service for Gmail SMTP.
     * @param username Gmail username
     * @param appPassword Gmail app password
     * @return configured email service
     */
    public static ReporteEmailService forGmail(String username, String appPassword) {
        return new ReporteEmailService("smtp.gmail.com", 587, username, appPassword, true);
    }

    /**
     * Send report PDF to the specified email.
     * If sending fails, saves the PDF locally and returns false.
     * 
     * @param toEmail recipient email
     * @param pdfPath path to the PDF file
     * @param nombreEscenario scenario name for email subject
     * @return true if sent successfully, false otherwise
     */
    public boolean enviarReporte(String toEmail, String pdfPath, String nombreEscenario) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", String.valueOf(smtpPort));
            props.put("mail.smtp.auth", "true");
            
            if (useTLS) {
                props.put("mail.smtp.starttls.enable", "true");
            }

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Reporte de Simulación - " + nombreEscenario);

            // Body text
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Adjunto encontrará el reporte de la simulación del escenario: " + nombreEscenario);

            // Attachment
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new File(pdfPath));

            // Combine parts
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            System.err.println("PDF saved locally at: " + pdfPath);
            return false;
        }
    }

    /**
     * Send report with fallback - if email fails, ensure PDF is saved locally.
     * 
     * @param toEmail recipient email
     * @param pdfPath path to the PDF file
     * @param nombreEscenario scenario name
     * @param fallbackDir directory to save PDF if email fails
     * @return path to the saved PDF (either original or fallback location)
     */
    public String enviarConFallback(String toEmail, String pdfPath, String nombreEscenario, String fallbackDir) {
        boolean sent = enviarReporte(toEmail, pdfPath, nombreEscenario);
        
        if (!sent) {
            // Ensure PDF is saved to fallback location
            try {
                Path original = Paths.get(pdfPath);
                Path fallback = Paths.get(fallbackDir, "reporte_" + nombreEscenario.replace(" ", "_") + ".pdf");
                Files.createDirectories(fallback.getParent());
                
                if (!Files.exists(fallback) && Files.exists(original)) {
                    Files.copy(original, fallback);
                    System.out.println("PDF guardado localmente en: " + fallback);
                    return fallback.toString();
                }
                return original.toString();
            } catch (IOException e) {
                System.err.println("Error saving fallback PDF: " + e.getMessage());
                return pdfPath;
            }
        }
        
        return pdfPath;
    }

    /**
     * Test the email configuration without sending an actual email.
     * @return true if configuration appears valid
     */
    public boolean testConfiguration() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", String.valueOf(smtpPort));
            props.put("mail.smtp.auth", "true");
            if (useTLS) {
                props.put("mail.smtp.starttls.enable", "true");
            }
            
            Session session = Session.getInstance(props);
            Transport transport = session.getTransport("smtp");
            transport.connect(smtpHost, username, password);
            transport.close();
            return true;
        } catch (Exception e) {
            System.err.println("Email configuration test failed: " + e.getMessage());
            return false;
        }
    }
}
