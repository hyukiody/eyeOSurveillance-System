package com.teraapi.stream.util;

import org.junit.jupiter.api.Test;
import javax.crypto.SecretKey;
import static org.junit.jupiter.api.Assertions.*;

public class DataProtectionTest {

    @Test
    public void testAESGCMEncodeecode() throws Exception {
        SecretKey key = DataProtection.generateAESKey();
        String plaintext = "Sensitive Data 123";

        String encoded = DataProtection.AESGCMProtection.encode(plaintext, key);
        assertNotNull(encoded);
        assertNotEquals(plaintext, encoded);

        String decoded = DataProtection.AESGCMProtection.decode(encoded, key);
        assertEquals(plaintext, decoded);
    }

    @Test
    public void testRSAEncodeDecode() throws Exception {
        var keyPair = DataProtection.RSAProtection.generateKeyPair();
        String plaintext = "Important Message";

        String encoded = DataProtection.RSAProtection.encode(plaintext, keyPair.getPublic());
        assertNotNull(encoded);

        String decoded = DataProtection.RSAProtection.decode(encoded, keyPair.getPrivate());
        assertEquals(plaintext, decoded);
    }
}
