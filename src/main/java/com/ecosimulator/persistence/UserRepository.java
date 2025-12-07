package com.ecosimulator.persistence;

import com.ecosimulator.model.User;
import com.ecosimulator.util.CryptoUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Repository for user persistence to users.txt
 * Format: cedula|nombre|fechaNacimiento(YYYY-MM-DD)|genero|email|salt|passwordHash
 */
public class UserRepository {
    private static final String FILE_NAME = "users.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private final Path filePath;
    
    public UserRepository() {
        this.filePath = Paths.get(FILE_NAME);
    }
    
    public UserRepository(String basePath) {
        this.filePath = Paths.get(basePath, FILE_NAME);
    }
    
    /**
     * Register a new user
     * @param user the user to register
     * @param plainPassword the plain text password
     * @return true if successful
     * @throws IllegalArgumentException if user is under 18
     */
    public boolean register(User user, String plainPassword) {
        // Validate age
        if (!user.isAdult()) {
            throw new IllegalArgumentException("User must be at least 18 years old");
        }
        
        // Check if user already exists
        if (findByCedula(user.getCedula()) != null) {
            return false;
        }
        
        // Generate salt and hash password
        String salt = CryptoUtil.generateSalt();
        String hash = CryptoUtil.hashPasswordPBKDF2(plainPassword, salt);
        user.setSalt(salt);
        user.setPasswordHash(hash);
        
        // Write to file
        try {
            String line = formatUserLine(user);
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
     * Find user by cedula
     * @param cedula the user ID
     * @return the user, or null if not found
     */
    public User findByCedula(String cedula) {
        List<User> users = loadAllUsers();
        for (User user : users) {
            if (user.getCedula().equals(cedula)) {
                return user;
            }
        }
        return null;
    }
    
    /**
     * Find user by email
     * @param email the email address
     * @return the user, or null if not found
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
     * Authenticate a user
     * @param cedula the user ID
     * @param plainPassword the password
     * @return the user if authenticated, null otherwise
     */
    public User authenticate(String cedula, String plainPassword) {
        User user = findByCedula(cedula);
        if (user != null) {
            if (CryptoUtil.verifyPasswordPBKDF2(plainPassword, user.getSalt(), user.getPasswordHash())) {
                return user;
            }
        }
        return null;
    }
    
    /**
     * Load all users from file
     * @return list of users
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
     * Format user as line for file
     * Format: cedula|nombre|fechaNacimiento|genero|email|salt|passwordHash
     */
    private String formatUserLine(User user) {
        return String.format("%s|%s|%s|%s|%s|%s|%s",
            user.getCedula(),
            user.getNombre(),
            user.getFechaNacimiento().format(DATE_FORMAT),
            user.getGenero(),
            user.getEmail(),
            user.getSalt(),
            user.getPasswordHash());
    }
    
    /**
     * Parse a line into a User object
     */
    private User parseUserLine(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length != 7) {
                return null;
            }
            
            String cedula = parts[0].trim();
            String nombre = parts[1].trim();
            LocalDate fechaNacimiento = LocalDate.parse(parts[2].trim(), DATE_FORMAT);
            String genero = parts[3].trim();
            String email = parts[4].trim();
            String salt = parts[5].trim();
            String passwordHash = parts[6].trim();
            
            return new User(cedula, nombre, fechaNacimiento, genero, email, salt, passwordHash);
        } catch (Exception e) {
            System.err.println("Error parsing user line: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if file exists
     * @return true if file exists
     */
    public boolean fileExists() {
        return Files.exists(filePath);
    }
}
