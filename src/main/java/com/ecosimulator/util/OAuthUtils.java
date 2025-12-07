package com.ecosimulator.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.GmailScopes;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for Google OAuth2 authentication with Gmail.
 * Handles credential management, token storage, and OAuth flow.
 */
public class OAuthUtils {
    
    private static final Logger LOGGER = Logger.getLogger(OAuthUtils.class.getName());
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_SEND);
    private static final String APPLICATION_NAME = "Eco Simulator";
    
    private static String credentialsFilePath = "credentials.json";
    
    /**
     * Set the path to the credentials.json file
     * @param path Path to the credentials file
     */
    public static void setCredentialsFilePath(String path) {
        credentialsFilePath = path;
    }
    
    /**
     * Get the current credentials file path
     * @return Path to credentials file
     */
    public static String getCredentialsFilePath() {
        return credentialsFilePath;
    }
    
    /**
     * Retrieve Gmail credentials using OAuth2.
     * If credentials don't exist, initiates OAuth flow.
     * If tokens exist, reuses them.
     * 
     * @return Credential object for Gmail API access
     * @throws IOException If credentials file cannot be read
     * @throws GeneralSecurityException If security setup fails
     */
    public static Credential getGmailCredential() throws IOException, GeneralSecurityException {
        // Check if credentials file exists
        File credentialsFile = new File(credentialsFilePath);
        if (!credentialsFile.exists()) {
            LOGGER.severe("Credentials file not found at: " + credentialsFilePath);
            throw new IOException("Credentials file not found: " + credentialsFilePath + 
                                ". Please download it from Google Cloud Console.");
        }
        
        LOGGER.info("Loading credentials from: " + credentialsFilePath);
        
        // Load client secrets
        GoogleClientSecrets clientSecrets;
        try (FileReader reader = new FileReader(credentialsFile)) {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load credentials file", e);
            throw new IOException("Invalid credentials file format: " + e.getMessage(), e);
        }
        
        // Build flow and trigger user authorization request
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        // Ensure tokens directory exists
        Path tokensPath = Paths.get(TOKENS_DIRECTORY);
        if (!Files.exists(tokensPath)) {
            Files.createDirectories(tokensPath);
            LOGGER.info("Created tokens directory: " + TOKENS_DIRECTORY);
        }
        
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, 
                JSON_FACTORY, 
                clientSecrets, 
                SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY)))
                .setAccessType("offline")
                .build();
        
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .build();
        
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        
        LOGGER.info("Gmail OAuth credentials obtained successfully");
        return credential;
    }
    
    /**
     * Test if OAuth credentials are configured and valid.
     * 
     * @return true if credentials can be obtained
     */
    public static boolean testOAuthConnection() {
        try {
            Credential credential = getGmailCredential();
            boolean hasToken = credential.getAccessToken() != null;
            LOGGER.info("OAuth connection test: " + (hasToken ? "SUCCESS" : "FAILED"));
            return hasToken;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "OAuth connection test failed", e);
            return false;
        }
    }
    
    /**
     * Check if credentials file exists.
     * 
     * @return true if credentials file exists
     */
    public static boolean credentialsFileExists() {
        return new File(credentialsFilePath).exists();
    }
    
    /**
     * Clear stored OAuth tokens.
     * This will force re-authentication on next use.
     */
    public static void clearTokens() {
        try {
            Path tokensPath = Paths.get(TOKENS_DIRECTORY);
            if (Files.exists(tokensPath)) {
                // Delete files in reverse order (children before parents)
                Files.walk(tokensPath)
                    .sorted((a, b) -> b.compareTo(a)) // Reverse order for proper deletion
                    .map(Path::toFile)
                    .forEach(File::delete);
                LOGGER.info("OAuth tokens cleared");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to clear tokens", e);
        }
    }
    
    /**
     * Get the tokens directory path.
     * 
     * @return Path to tokens directory
     */
    public static String getTokensDirectory() {
        return TOKENS_DIRECTORY;
    }
}
