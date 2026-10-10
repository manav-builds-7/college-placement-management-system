package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hashes passwords with SHA-256 and a random salt, and verifies them at login.
 * Stored format: "<base64 salt>:<base64 hash>".
 *
 * Teaching note: real systems use bcrypt or Argon2, which are deliberately slow.
 * SHA-256 is used here because it is in the standard Java library and easy to explain.
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_BYTES = 16;
    private static final String SEPARATOR = ":";

    private PasswordUtil() {
    }

    /**
     * Turns a plain password into a salted hash string for storage.
     * Hashing the same password twice gives different results, because the salt differs.
     */
    public static String hashPassword(String plainPassword) {
        byte[] salt = generateSalt();
        byte[] hash = digest(salt, plainPassword);
        return Base64.getEncoder().encodeToString(salt)
             + SEPARATOR
             + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Checks a typed password against a stored value from hashPassword().
     * Returns false for any malformed stored value instead of crashing.
     */
    public static boolean verifyPassword(String plainPassword, String storedValue) {
        if (plainPassword == null || storedValue == null) {
            return false;
        }
        String[] parts = storedValue.split(SEPARATOR);
        if (parts.length != 2) {
            return false;
        }
        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            byte[] actualHash = digest(salt, plainPassword);
            // MessageDigest.isEqual compares in constant time, which avoids timing leaks
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException e) {
            return false;   // stored value is not valid Base64
        }
    }

    /** Creates a random 16-byte salt. SecureRandom is suitable for security use. */
    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /** Computes SHA-256 over the salt followed by the password bytes. */
    private static byte[] digest(byte[] salt, String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            return md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is required on every Java platform, so this should never happen.
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }
}