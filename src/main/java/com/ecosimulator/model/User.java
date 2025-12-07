package com.ecosimulator.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * User model for authentication and registration
 * Stores user data with encrypted password and salt
 */
public class User {
    private String cedula;  // ID number (can be numeric or alphanumeric)
    private String nombre;  // Full name
    private LocalDate fechaNacimiento;  // Birth date
    private String genero;  // Gender
    private String email;
    private String salt;  // Password salt
    private String passwordHash;  // Hashed password
    
    public User() {
    }
    
    public User(String cedula, String nombre, LocalDate fechaNacimiento, 
                String genero, String email, String salt, String passwordHash) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.email = email;
        this.salt = salt;
        this.passwordHash = passwordHash;
    }
    
    /**
     * Calculate user's age
     * @return age in years
     */
    public int getAge() {
        if (fechaNacimiento == null) {
            return 0;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }
    
    /**
     * Check if user is at least 18 years old
     * @return true if user is adult
     */
    public boolean isAdult() {
        return getAge() >= 18;
    }
    
    // Getters and setters
    public String getCedula() {
        return cedula;
    }
    
    public void setCedula(String cedula) {
        this.cedula = cedula;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    
    public String getGenero() {
        return genero;
    }
    
    public void setGenero(String genero) {
        this.genero = genero;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getSalt() {
        return salt;
    }
    
    public void setSalt(String salt) {
        this.salt = salt;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    @Override
    public String toString() {
        return String.format("User[cedula=%s, nombre=%s, email=%s, age=%d]", 
                           cedula, nombre, email, getAge());
    }
}
