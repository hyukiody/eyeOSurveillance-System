package com.teraapi.stream.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Base64;

public class AdvancedEncryption {

    // AES-GCM Encryption
    public static class AESGCMEncryption {
        private static final int GCM_IV_LENGTH = 12;
        private static final int GCM_TAG_LENGTH = 128;

        public static String encrypt(String plaintext, SecretKey key) throws Exception {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        }

        public static String decrypt(String encryptedData, SecretKey key) throws Exception {
            byte[] combined = Base64.getDecoder().decode(encryptedData);
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plaintext = cipher.doFinal(combined, GCM_IV_LENGTH, combined.length - GCM_IV_LENGTH);
            return new String(plaintext);
        }
    }

    // RSA Encryption
    public static class RSAEncryption {
        public static KeyPair generateKeyPair() throws Exception {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            return kpg.generateKeyPair();
        }

        public static String encrypt(String plaintext, java.security.PublicKey publicKey) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
            return Base64.getEncoder().encodeToString(ciphertext);
        }

        public static String decrypt(String encryptedData, java.security.PrivateKey privateKey) throws Exception {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] plaintext = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(plaintext);
        }
    }

    // Generate AES-GCM Key
    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        return kg.generateKey();
    }
}
