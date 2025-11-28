package com.ecosimulator.auth;

import java.time.LocalDate;
import java.time.Period;

/**
 * User domain model for authentication
 * Stores user data including ID, name, password (encrypted), email, and birth date
 */
public class User {
    private int id;
    private String name;
    private String encryptedPassword;
    private String email;
    private LocalDate birthDate;

    /**
     * Create a new user with all required fields
     */
    public User(int id, String name, String encryptedPassword, String email, LocalDate birthDate) {
        this.id = id;
        this.name = name;
        this.encryptedPassword = encryptedPassword;
        this.email = email;
        this.birthDate = birthDate;
    }

    /**
     * Calculate user's age based on birth date
     * @return age in years
     */
    public int getAge() {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Check if user is at least 18 years old
     * @return true if user is 18 or older
     */
    public boolean isAdult() {
        return getAge() >= 18;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, name=%s, email=%s, age=%d]", id, name, email, getAge());
    }
}
