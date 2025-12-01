package com.ecosimulator;

import com.ecosimulator.service.EmailService;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for EmailService using GreenMail in-memory SMTP server.
 */
class EmailServiceTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());

    private EmailService emailService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        emailService = new EmailService();
        // Configure for GreenMail test server
        emailService.configureSmtp(
            "localhost",
            greenMail.getSmtp().getPort(),
            "test@localhost",
            "",
            "test@localhost",
            false,
            false
        );
        tempDir = Files.createTempDirectory("email_test_");
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up temp directory
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                .sorted(java.util.Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
        // Clean up fallback directory
        Path fallbackDir = Path.of(EmailService.getFallbackDirectory());
        if (Files.exists(fallbackDir)) {
            Files.walk(fallbackDir)
                .sorted(java.util.Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    @Test
    @DisplayName("Should send email successfully to GreenMail")
    void testSuccessfulEmailSend() throws Exception {
        // Act
        boolean result = emailService.sendEmail("recipient@test.com", "Test Subject", "Test body content");

        // Assert
        assertTrue(result, "Email should be sent successfully");
        assertEquals(1, greenMail.getReceivedMessages().length, "GreenMail should receive one message");
        
        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
        assertEquals("Test Subject", receivedMessage.getSubject());
        assertTrue(GreenMailUtil.getBody(receivedMessage).contains("Test body content"));
    }

    @Test
    @DisplayName("Should send email with PDF attachment")
    void testSendReportWithAttachment() throws Exception {
        // Arrange - create a test PDF file
        Path pdfPath = tempDir.resolve("test_report.pdf");
        Files.writeString(pdfPath, "%PDF-1.4 Test PDF content");
        File pdfFile = pdfPath.toFile();

        // Act
        boolean result = emailService.sendReport("recipient@test.com", pdfFile, 
            "Simulation Report", "Please find attached the report.");

        // Assert
        assertTrue(result, "Email with attachment should be sent successfully");
        assertEquals(1, greenMail.getReceivedMessages().length);
        
        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];
        assertEquals("Simulation Report", receivedMessage.getSubject());
        
        // Verify attachment
        Object content = receivedMessage.getContent();
        assertTrue(content instanceof MimeMultipart, "Message should have multipart content");
        MimeMultipart multipart = (MimeMultipart) content;
        assertTrue(multipart.getCount() >= 2, "Should have at least text and attachment parts");
    }

    @Test
    @DisplayName("Should return false when not configured")
    void testSendWhenNotConfigured() {
        // Arrange - create unconfigured service
        EmailService unconfiguredService = new EmailService();

        // Act
        boolean result = unconfiguredService.sendEmail("test@test.com", "Subject", "Body");

        // Assert
        assertFalse(result, "Should return false when not configured");
        assertEquals(0, greenMail.getReceivedMessages().length, "No messages should be received");
    }

    @Test
    @DisplayName("Should save PDF to fallback on failure")
    void testFallbackOnFailure() throws IOException {
        // Arrange - create a test PDF file
        Path pdfPath = tempDir.resolve("fallback_test.pdf");
        Files.writeString(pdfPath, "%PDF-1.4 Fallback test content");
        File pdfFile = pdfPath.toFile();
        
        // Create unconfigured service to simulate failure
        EmailService failingService = new EmailService();
        
        // Act
        boolean result = failingService.sendReport("recipient@test.com", pdfFile, "Subject", "Body");
        
        // Assert
        assertFalse(result, "Should return false on failure");
        
        // Check fallback directory exists
        Path fallbackDir = Path.of(EmailService.getFallbackDirectory());
        assertTrue(Files.exists(fallbackDir), "Fallback directory should be created");
        
        // Check that a file was saved
        long fileCount = Files.list(fallbackDir).count();
        assertTrue(fileCount > 0, "Fallback PDF should be saved");
    }

    @Test
    @DisplayName("Should correctly verify recipient in received message")
    void testCorrectRecipient() throws Exception {
        // Act
        emailService.sendEmail("specific.recipient@example.com", "Test", "Content");

        // Assert
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertTrue(GreenMailUtil.getAddressList(messages[0].getAllRecipients())
            .contains("specific.recipient@example.com"));
    }

    @Test
    @DisplayName("Should handle null recipient gracefully")
    void testNullRecipient() {
        // Act
        boolean result = emailService.sendReport(null, null, "Subject", "Body");

        // Assert
        assertFalse(result, "Should return false for null recipient");
    }

    @Test
    @DisplayName("Should handle empty recipient gracefully")
    void testEmptyRecipient() {
        // Act
        boolean result = emailService.sendReport("", null, "Subject", "Body");

        // Assert
        assertFalse(result, "Should return false for empty recipient");
    }

    @Test
    @DisplayName("Should test connection successfully to GreenMail")
    void testConnectionSuccess() {
        // Act
        boolean result = emailService.testConnection();

        // Assert
        assertTrue(result, "Connection test should succeed with GreenMail");
    }

    @Test
    @DisplayName("Should report correct configuration status")
    void testConfigurationStatus() {
        // Assert
        assertTrue(emailService.isConfigured(), "Service should be configured");
        String status = emailService.getConfigurationStatus();
        assertTrue(status.contains("localhost"), "Status should contain host");
    }

    @Test
    @DisplayName("Should handle missing PDF file gracefully")
    void testMissingPdfFile() {
        // Arrange
        File nonExistentPdf = new File("/nonexistent/path/report.pdf");

        // Act - should not throw exception
        boolean result = emailService.sendReport("recipient@test.com", nonExistentPdf, 
            "Subject", "Body");

        // Assert - email without attachment should still work
        assertTrue(result, "Email should be sent even without attachment");
    }
}
