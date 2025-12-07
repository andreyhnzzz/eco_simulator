package com.ecosimulator.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Cryptographic utility class for password hashing and verification
 * Supports both PBKDF2 and BCrypt algorithms
 */
public class CryptoUtil {
    
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int PBKDF2_KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Generate a random salt
     * @return base64-encoded salt
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hash a password using PBKDF2 with the provided salt
     * @param password the password to hash
     * @param salt the salt (base64-encoded)
     * @return the password hash (base64-encoded)
     */
    public static String hashPasswordPBKDF2(String password, String salt) {
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), 
                saltBytes, 
                PBKDF2_ITERATIONS, 
                PBKDF2_KEY_LENGTH
            );
            
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();
            
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password with PBKDF2", e);
        }
    }
    
    /**
     * Verify a password against a PBKDF2 hash
     * @param password the password to verify
     * @param salt the salt (base64-encoded)
     * @param hash the expected hash (base64-encoded)
     * @return true if the password matches
     */
    public static boolean verifyPasswordPBKDF2(String password, String salt, String hash) {
        String computedHash = hashPasswordPBKDF2(password, salt);
        return computedHash.equals(hash);
    }
    
    /**
     * Hash a password using BCrypt
     * @param password the password to hash
     * @return BCrypt hash string (contains salt and hash)
     */
    public static String hashPasswordBCrypt(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }
    
    /**
     * Verify a password against a BCrypt hash
     * @param password the password to verify
     * @param bcryptHash the BCrypt hash string
     * @return true if the password matches
     */
    public static boolean verifyPasswordBCrypt(String password, String bcryptHash) {
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), bcryptHash);
        return result.verified;
    }
    
    /**
     * Hash a password using PBKDF2 (creates salt automatically)
     * @param password the password to hash
     * @return string in format "salt:hash" (both base64-encoded)
     */
    public static String hashPassword(String password) {
        String salt = generateSalt();
        String hash = hashPasswordPBKDF2(password, salt);
        return salt + ":" + hash;
    }
    
    /**
     * Verify a password against a combined salt:hash string
     * @param password the password to verify
     * @param saltAndHash string in format "salt:hash"
     * @return true if the password matches
     */
    public static boolean verifyPassword(String password, String saltAndHash) {
        String[] parts = saltAndHash.split(":", 2);
        if (parts.length != 2) {
            return false;
        }
        return verifyPasswordPBKDF2(password, parts[0], parts[1]);
    }
}
