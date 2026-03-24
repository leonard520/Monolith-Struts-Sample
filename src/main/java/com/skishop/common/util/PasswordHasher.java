package com.skishop.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public final class PasswordHasher {
    private static final int HASH_ITERATIONS = 1000;

    private PasswordHasher() {
    }

    public static String generateSalt() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String hash(String passwordRaw, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
            byte[] input = concat(saltBytes, passwordRaw.getBytes(StandardCharsets.UTF_8));
            for (int i = 0; i < HASH_ITERATIONS; i++) {
                input = digest.digest(input);
                digest.reset();
                input = concat(saltBytes, input);
            }
            return toHex(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static boolean matches(String passwordRaw, String passwordHash, String salt) {
        if (passwordRaw == null || passwordHash == null) {
            return false;
        }
        if (salt == null || salt.isEmpty()) {
            return secureEquals(passwordRaw, passwordHash);
        }
        String hashed = hash(passwordRaw, salt);
        return secureEquals(hashed, passwordHash);
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] combined = new byte[first.length + second.length];
        System.arraycopy(first, 0, combined, 0, first.length);
        System.arraycopy(second, 0, combined, first.length, second.length);
        return combined;
    }

    private static String toHex(byte[] data) {
        StringBuilder builder = new StringBuilder();
        for (byte value : data) {
            String hex = Integer.toHexString(value & 0xff);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    private static boolean secureEquals(String left, String right) {
        if (left == null || right == null || left.length() != right.length()) {
            return false;
        }
        int result = 0;
        char[] leftChars = left.toCharArray();
        char[] rightChars = right.toCharArray();
        int index = 0;
        for (char leftChar : leftChars) {
            result |= leftChar ^ rightChars[index++];
        }
        return result == 0;
    }
}
