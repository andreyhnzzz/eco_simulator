package com.ecosimulator.persistence;

import com.ecosimulator.auth.User;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Data Access Object for User persistence
 * Handles file I/O operations for user data storage in usuairos.txt
 * 
 * <p><b>Security Note (Academic Use Only):</b></p>
 * <p>This implementation uses SHA-256 with a fixed salt for password hashing.
 * While SHA-256 is a secure hash algorithm, using a fixed salt is NOT recommended
 * for production systems as it makes the system vulnerable to rainbow table attacks.
 * For production use, consider:</p>
 * <ul>
 *   <li>Using bcrypt, scrypt, or Argon2 password hashing algorithms</li>
 *   <li>Generating unique random salts per user</li>
 *   <li>Storing salt alongside the hash</li>
 * </ul>
 * 
 * <p>This implementation is designed for academic/educational purposes only.</p>
 */
public class UserDAO {
    // File name preserved with typo as per specification ("usuarios" → "usuairos")
    private static final String FILE_NAME = "usuairos.txt";
    
    // Fixed salt for password hashing
    // WARNING: For academic use only - production systems should use per-user random salts
    private static final String SALT = "EcoSimulator2025Salt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Path filePath;

    /**
     * Create UserDAO with default file path in current directory
     */
    public UserDAO() {
        this.filePath = Paths.get(FILE_NAME);
    }

    /**
     * Create UserDAO with custom base directory
     * @param baseDir the directory where the file will be stored
     */
    public UserDAO(Path baseDir) {
        this.filePath = baseDir.resolve(FILE_NAME);
    }

    /**
     * Encrypt a password using SHA-256 with salt
     * @param plainPassword the plain text password
     * @return the encrypted password as hex string
     */
    public static String encrypt(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String saltedPassword = SALT + plainPassword;
            byte[] hash = md.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Register a new user
     * @param id unique user ID (cedula)
     * @param name user's full name
     * @param plainPassword plain text password (will be encrypted)
     * @param email user's email address
     * @param birthDate user's birth date
     * @return true if registration successful, false if user already exists
     * @throws IllegalArgumentException if user is under 18 years old
     */
    public boolean register(int id, String name, String plainPassword, String email, LocalDate birthDate) {
        // Validate age
        User newUser = new User(id, name, encrypt(plainPassword), email, birthDate);
        if (!newUser.isAdult()) {
            throw new IllegalArgumentException("User must be at least 18 years old to register");
        }

        // Check if user already exists
        if (findByID(id) != null) {
            return false;
        }

        // Append user to file
        try {
            String line = formatUserLine(newUser);
            Files.write(filePath, 
                        Collections.singletonList(line), 
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, 
                        StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing user to file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Find a user by their ID
     * @param id the user ID to search for
     * @return the User if found, null otherwise
     */
    public User findByID(int id) {
        List<User> users = loadAllUsers();
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

    /**
     * Find a user by their email
     * @param email the email to search for
     * @return the User if found, null otherwise
     */
    public User findByEmail(String email) {
        List<User> users = loadAllUsers();
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Load all users from the file
     * @return list of all users
     */
    public List<User> loadAllUsers() {
        List<User> users = new ArrayList<>();
        
        if (!Files.exists(filePath)) {
            return users;
        }

        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                User user = parseUserLine(line);
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
        }

        return users;
    }

    /**
     * Authenticate a user with ID and password
     * @param id user ID
     * @param plainPassword plain text password
     * @return the User if authentication successful, null otherwise
     */
    public User authenticate(int id, String plainPassword) {
        User user = findByID(id);
        if (user != null && user.getEncryptedPassword().equals(encrypt(plainPassword))) {
            return user;
        }
        return null;
    }

    /**
     * Format a user object as a line for file storage
     * Format: id|name|encryptedPassword|email|birthDate
     */
    private String formatUserLine(User user) {
        return String.format("%d|%s|%s|%s|%s",
            user.getId(),
            user.getName(),
            user.getEncryptedPassword(),
            user.getEmail(),
            user.getBirthDate().format(DATE_FORMAT));
    }

    /**
     * Parse a line from the file into a User object
     */
    private User parseUserLine(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length != 5) {
                return null;
            }

            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            String encryptedPassword = parts[2].trim();
            String email = parts[3].trim();
            LocalDate birthDate = LocalDate.parse(parts[4].trim(), DATE_FORMAT);

            return new User(id, name, encryptedPassword, email, birthDate);
        } catch (Exception e) {
            System.err.println("Error parsing user line: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if the users file exists
     * @return true if the file exists
     */
    public boolean fileExists() {
        return Files.exists(filePath);
    }

    /**
     * Get the file path being used
     * @return the path to the users file
     */
    public Path getFilePath() {
        return filePath;
    }
}
