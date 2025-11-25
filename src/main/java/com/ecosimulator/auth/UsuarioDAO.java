package com.ecosimulator.auth;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * DAO for user persistence.
 * Uses SHA-256 + fixed salt for password hashing.
 * Note: file name "usuairos.txt" is intentionally misspelled as per requirements.
 */
public class UsuarioDAO {
    
    // Intentional typo as per requirements
    private static final String DEFAULT_FILE = "usuairos.txt";
    private static final String SALT = "UTN2025";
    private static final String DELIMITER = "|";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private final Path filePath;

    public UsuarioDAO() {
        this(DEFAULT_FILE);
    }

    public UsuarioDAO(String fileName) {
        this.filePath = Paths.get(fileName);
        ensureFileExists();
    }

    private void ensureFileExists() {
        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            System.err.println("Error creating users file: " + e.getMessage());
        }
    }

    /**
     * Hash a password using SHA-256 + salt.
     * @param password the plain text password
     * @return the hashed password as hex string
     */
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = SALT + password;
            byte[] hash = digest.digest(saltedPassword.getBytes());
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
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Register a new user.
     * @param cedula user ID
     * @param nombre user name
     * @param fechaNacimiento birth date
     * @param genero gender
     * @param password plain text password (will be hashed)
     * @param correo email
     * @return the created user, or null if user already exists
     */
    public Usuario registrar(String cedula, String nombre, LocalDate fechaNacimiento,
                            String genero, String password, String correo) {
        // Check if user already exists
        if (buscarPorCedula(cedula) != null) {
            return null;
        }
        
        String hash = hashPassword(password);
        Usuario usuario = new Usuario(cedula, nombre, fechaNacimiento, genero, hash, correo);
        
        try {
            String line = serializeUsuario(usuario);
            Files.writeString(filePath, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return usuario;
        } catch (IOException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return null;
        }
    }

    /**
     * Authenticate a user.
     * @param cedula user ID
     * @param password plain text password
     * @return the authenticated user, or null if authentication fails
     */
    public Usuario autenticar(String cedula, String password) {
        Usuario usuario = buscarPorCedula(cedula);
        if (usuario == null) {
            return null;
        }
        
        String hash = hashPassword(password);
        if (hash.equals(usuario.getContrasenaHash())) {
            return usuario;
        }
        
        return null;
    }

    /**
     * Find a user by cedula.
     * @param cedula user ID
     * @return the user, or null if not found
     */
    public Usuario buscarPorCedula(String cedula) {
        try {
            if (!Files.exists(filePath)) {
                return null;
            }
            
            for (String line : Files.readAllLines(filePath)) {
                if (line.trim().isEmpty()) continue;
                
                Usuario usuario = deserializeUsuario(line);
                if (usuario != null && usuario.getCedula().equals(cedula)) {
                    return usuario;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Get all users.
     * @return list of all users
     */
    public List<Usuario> listarTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        
        try {
            if (!Files.exists(filePath)) {
                return usuarios;
            }
            
            for (String line : Files.readAllLines(filePath)) {
                if (line.trim().isEmpty()) continue;
                
                Usuario usuario = deserializeUsuario(line);
                if (usuario != null) {
                    usuarios.add(usuario);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
        }
        
        return usuarios;
    }

    private String serializeUsuario(Usuario usuario) {
        return String.join(DELIMITER,
                usuario.getCedula(),
                usuario.getNombre(),
                usuario.getFechaNacimiento().format(DATE_FORMAT),
                usuario.getGenero(),
                usuario.getContrasenaHash(),
                usuario.getCorreo()
        );
    }

    private Usuario deserializeUsuario(String line) {
        try {
            String[] parts = line.split("\\" + DELIMITER);
            if (parts.length != 6) {
                return null;
            }
            
            return new Usuario(
                    parts[0], // cedula
                    parts[1], // nombre
                    LocalDate.parse(parts[2], DATE_FORMAT), // fechaNacimiento
                    parts[3], // genero
                    parts[4], // contrasenaHash
                    parts[5]  // correo
            );
        } catch (Exception e) {
            System.err.println("Error parsing user line: " + e.getMessage());
            return null;
        }
    }

    public Path getFilePath() {
        return filePath;
    }
}
