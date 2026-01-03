package com.teraapi.stream;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * High-performance data protection service for stream processing
 */
public class DataProtectionService {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;

    /**
     * Generate a new AES key
     */
    public static String generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(KEY_SIZE, new SecureRandom());
        SecretKey key = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Encode data using AES
     */
    public static String encode(String data, String encodedKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encodedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encodedData);
    }

    /**
     * Decode data using AES
     */
    public static String decode(String encodedData, String encodedKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decodedDataBytes = Base64.getDecoder().decode(encodedData);
        byte[] decryptedData = cipher.doFinal(decodedDataBytes);
        return new String(decryptedData);
    }

    /**
     * Hash data using SHA-256 (one-way)
     */
    public static String hash(String data) throws Exception {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
}
