package com.exam.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {
    private static final int SALT_LENGTH = 16;
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int ITERATIONS = 10000;

    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.reset();
            digest.update(Base64.getDecoder().decode(salt));
            
            byte[] hashedBytes = password.getBytes();
            for (int i = 0; i < ITERATIONS; i++) {
                hashedBytes = digest.digest(hashedBytes);
                digest.reset();
            }
            
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public static boolean verifyPassword(String password, String salt, String storedHash) {
        if (password == null || salt == null || storedHash == null) {
            return false;
        }
        String newHash = hashPassword(password, salt);
        return MessageDigest.isEqual(
            Base64.getDecoder().decode(newHash),
            Base64.getDecoder().decode(storedHash)
        );
    }

    // For testing purposes
    public static void main(String[] args) {
        String salt = generateSalt();
        String password = "securePassword123";
        String hashedPassword = hashPassword(password, salt);
        
        System.out.println("Salt: " + salt);
        System.out.println("Hashed Password: " + hashedPassword);
        System.out.println("Verification: " + 
            verifyPassword(password, salt, hashedPassword));
    }
}