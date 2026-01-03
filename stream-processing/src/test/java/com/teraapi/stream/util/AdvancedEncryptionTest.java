package com.teraapi.stream.util;

import org.junit.jupiter.api.Test;
import javax.crypto.SecretKey;
import static org.junit.jupiter.api.Assertions.*;

public class AdvancedEncryptionTest {

    @Test
    public void testAESGCMEncryptDecrypt() throws Exception {
        SecretKey key = AdvancedEncryption.generateAESKey();
        String plaintext = "Sensitive Data 123";

        String encrypted = AdvancedEncryption.AESGCMEncryption.encrypt(plaintext, key);
        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);

        String decrypted = AdvancedEncryption.AESGCMEncryption.decrypt(encrypted, key);
        assertEquals(plaintext, decrypted);
    }

    @Test
    public void testRSAEncryptDecrypt() throws Exception {
        var keyPair = AdvancedEncryption.RSAEncryption.generateKeyPair();
        String plaintext = "Secret Message";

        String encrypted = AdvancedEncryption.RSAEncryption.encrypt(plaintext, keyPair.getPublic());
        assertNotNull(encrypted);

        String decrypted = AdvancedEncryption.RSAEncryption.decrypt(encrypted, keyPair.getPrivate());
        assertEquals(plaintext, decrypted);
    }
}
