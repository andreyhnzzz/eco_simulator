package com.ecosimulator;

import com.ecosimulator.auth.Session;
import com.ecosimulator.auth.User;
import com.ecosimulator.persistence.UserDAO;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User authentication and persistence
 */
class UserAuthTest {

    private static Path tempDir;
    private UserDAO userDAO;

    @BeforeAll
    static void setupClass() throws IOException {
        tempDir = Files.createTempDirectory("eco_test_");
    }

    @AfterAll
    static void cleanupClass() throws IOException {
        // Clean up temp directory (delete files before directories using reverse order)
        Files.walk(tempDir)
            .sorted(java.util.Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(f -> f.delete());
    }

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO(tempDir);
        // Clean up any existing file
        try {
            Files.deleteIfExists(userDAO.getFilePath());
        } catch (IOException e) {
            // Ignore
        }
    }

    @AfterEach
    void tearDown() {
        Session.logout();
    }

    @Test
    void testUserCreation() {
        LocalDate birthDate = LocalDate.of(2000, 1, 15);
        User user = new User(123456789, "Juan Perez", "encrypted123", "juan@test.com", birthDate);

        assertEquals(123456789, user.getId());
        assertEquals("Juan Perez", user.getName());
        assertEquals("encrypted123", user.getEncryptedPassword());
        assertEquals("juan@test.com", user.getEmail());
        assertEquals(birthDate, user.getBirthDate());
    }

    @Test
    void testUserAgeCalculation() {
        LocalDate birthDate = LocalDate.now().minusYears(25);
        User user = new User(1, "Test User", "pass", "test@test.com", birthDate);

        assertEquals(25, user.getAge());
        assertTrue(user.isAdult());
    }

    @Test
    void testUserMinorAge() {
        LocalDate birthDate = LocalDate.now().minusYears(16);
        User user = new User(1, "Minor User", "pass", "minor@test.com", birthDate);

        assertEquals(16, user.getAge());
        assertFalse(user.isAdult());
    }

    @Test
    void testPasswordEncryption() {
        String password = "testPassword123";
        String encrypted1 = UserDAO.encrypt(password);
        String encrypted2 = UserDAO.encrypt(password);

        // Same password should produce same hash
        assertEquals(encrypted1, encrypted2);

        // Different passwords should produce different hashes
        String differentPassword = "differentPassword";
        String encrypted3 = UserDAO.encrypt(differentPassword);
        assertNotEquals(encrypted1, encrypted3);

        // Hash should be 64 characters (SHA-256 hex)
        assertEquals(64, encrypted1.length());
    }

    @Test
    void testUserRegistration() {
        LocalDate birthDate = LocalDate.now().minusYears(25);
        
        boolean success = userDAO.register(
            123456789, 
            "Juan Perez", 
            "password123", 
            "juan@test.com", 
            birthDate
        );

        assertTrue(success);
        assertTrue(userDAO.fileExists());

        // Should find the user
        User found = userDAO.findByID(123456789);
        assertNotNull(found);
        assertEquals("Juan Perez", found.getName());
        assertEquals("juan@test.com", found.getEmail());
    }

    @Test
    void testDuplicateRegistrationFails() {
        LocalDate birthDate = LocalDate.now().minusYears(25);

        // First registration should succeed
        boolean first = userDAO.register(111111111, "User 1", "pass1", "user1@test.com", birthDate);
        assertTrue(first);

        // Duplicate registration should fail
        boolean second = userDAO.register(111111111, "User 2", "pass2", "user2@test.com", birthDate);
        assertFalse(second);

        // Only one user should exist
        assertEquals(1, userDAO.loadAllUsers().size());
    }

    @Test
    void testMinorRegistrationFails() {
        LocalDate birthDate = LocalDate.now().minusYears(17); // 17 years old

        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.register(222222222, "Minor User", "pass", "minor@test.com", birthDate);
        });
    }

    @Test
    void testSuccessfulAuthentication() {
        LocalDate birthDate = LocalDate.now().minusYears(25);
        userDAO.register(333333333, "Auth User", "myPassword", "auth@test.com", birthDate);

        User authenticated = userDAO.authenticate(333333333, "myPassword");

        assertNotNull(authenticated);
        assertEquals("Auth User", authenticated.getName());
    }

    @Test
    void testFailedAuthenticationWrongPassword() {
        LocalDate birthDate = LocalDate.now().minusYears(25);
        userDAO.register(444444444, "User 4", "correctPassword", "user4@test.com", birthDate);

        User authenticated = userDAO.authenticate(444444444, "wrongPassword");

        assertNull(authenticated);
    }

    @Test
    void testFailedAuthenticationNonExistentUser() {
        User authenticated = userDAO.authenticate(999999999, "anyPassword");

        assertNull(authenticated);
    }

    @Test
    void testFindByEmail() {
        LocalDate birthDate = LocalDate.now().minusYears(25);
        userDAO.register(555555555, "Email User", "pass", "findme@test.com", birthDate);

        User found = userDAO.findByEmail("findme@test.com");

        assertNotNull(found);
        assertEquals(555555555, found.getId());
    }

    @Test
    void testSessionManagement() {
        assertFalse(Session.isLoggedIn());
        assertNull(Session.getUser());

        LocalDate birthDate = LocalDate.now().minusYears(25);
        User user = new User(666666666, "Session User", "pass", "session@test.com", birthDate);

        Session.setUser(user);

        assertTrue(Session.isLoggedIn());
        assertEquals("Session User", Session.getUser().getName());

        Session.logout();

        assertFalse(Session.isLoggedIn());
        assertNull(Session.getUser());
    }

    @Test
    void testMultipleUsersStorage() {
        LocalDate birthDate = LocalDate.now().minusYears(30);

        userDAO.register(111111111, "User One", "pass1", "one@test.com", birthDate);
        userDAO.register(222222222, "User Two", "pass2", "two@test.com", birthDate);
        userDAO.register(333333333, "User Three", "pass3", "three@test.com", birthDate);

        assertEquals(3, userDAO.loadAllUsers().size());

        assertNotNull(userDAO.findByID(111111111));
        assertNotNull(userDAO.findByID(222222222));
        assertNotNull(userDAO.findByID(333333333));
    }

    @Test
    void testPasswordNeverStoredInPlaintext() {
        LocalDate birthDate = LocalDate.now().minusYears(25);
        String plainPassword = "mySecretPassword";
        
        userDAO.register(777777777, "Secure User", plainPassword, "secure@test.com", birthDate);

        User found = userDAO.findByID(777777777);
        
        // Password should be encrypted, not plain
        assertNotEquals(plainPassword, found.getEncryptedPassword());
        assertEquals(UserDAO.encrypt(plainPassword), found.getEncryptedPassword());
    }
}
