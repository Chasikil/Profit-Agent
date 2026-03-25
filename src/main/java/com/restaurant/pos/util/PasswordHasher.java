package com.restaurant.pos.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for password hashing using BCrypt.
 */
public class PasswordHasher {

    private static final int ROUNDS = 10;

    /**
     * Hash a password using BCrypt.
     *
     * @param plainPassword plain text password
     * @return hashed password
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return null;
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(ROUNDS));
    }

    /**
     * Verify a password against a hash.
     *
     * @param plainPassword plain text password
     * @param hash stored hash
     * @return true if password matches hash
     */
    public static boolean verify(String plainPassword, String hash) {
        if (plainPassword == null || hash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hash);
        } catch (Exception e) {
            return false;
        }
    }
}
