package top.chenray.qlogin.security;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

public class PasswordHasher {
    private static final int SALT_LENGTH = 16;
    private static final String ALGORITHM = "SHA-256";

    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt.getBytes());
            return HexFormat.of().formatHex(md.digest(password.getBytes()));
        } catch (Exception e) { throw new RuntimeException("SHA-256 不可用", e); }
    }

    public static boolean verify(String password, String salt, String hashedPassword) {
        return hash(password, salt).equals(hashedPassword.toLowerCase());
    }
}
