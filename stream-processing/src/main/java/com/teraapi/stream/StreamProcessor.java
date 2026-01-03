package com.teraapi.stream;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * High-performance stream data processor
 */
public class StreamProcessor {

    private static final String TRANSFORM_MODE = "AES";
    private static final int KEY_SIZE = 256;

    /**
     * Generate a new processing key
     */
    public static String generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(TRANSFORM_MODE);
        keyGen.init(KEY_SIZE, new SecureRandom());
        SecretKey key = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Transform data for secure storage
     */
    public static String transform(String data, String encodedKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, TRANSFORM_MODE);

        Cipher cipher = Cipher.getInstance(TRANSFORM_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] transformedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(transformedData);
    }

    /**
     * Decrypt data using AES
     */
    public static String restore(String protectedData, String encodedKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, TRANSFORM_MODE);

        Cipher cipher = Cipher.getInstance(TRANSFORM_MODE);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decodedData = Base64.getDecoder().decode(protectedData);
        byte[] restoredData = cipher.doFinal(decodedData);
        return new String(restoredData);
    }

    /**
     * Generate data fingerprint (one-way)
     */
    public static String fingerprint(String data) throws Exception {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] signature = digest.digest(data.getBytes());
        return Base64.getEncoder().encodeToString(signature);
    }
}
