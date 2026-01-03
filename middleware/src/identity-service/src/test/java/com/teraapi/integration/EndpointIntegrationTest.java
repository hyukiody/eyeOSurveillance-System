package com.teraapi.integration;

import com.teraapi.encryption.AESGCMEncryption;
import com.teraapi.ratelimit.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class EndpointIntegrationTest {
    private RateLimiter rateLimiter;
    private AESGCMEncryption encryption;
    private static final String TEST_KEY = "0123456789abcdef0123456789abcdef";

    @BeforeEach
    void setup() {
        rateLimiter = new RateLimiter(60, 60000);
        encryption = new AESGCMEncryption();
    }

    @Test
    void testRateLimiterConcurrent() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(100);
        AtomicInteger allowed = new AtomicInteger(0);
        AtomicInteger rejected = new AtomicInteger(0);

        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                if (rateLimiter.allowRequest("concurrent-client")) {
                    allowed.incrementAndGet();
                } else {
                    rejected.incrementAndGet();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        assertEquals(60, allowed.get());
        assertEquals(40, rejected.get());
    }

    @Test
    void testEncryptionRoundTrip() {
        String plaintext = "Test sensitive data for encryption";
        String aesKey = TEST_KEY;

        String ciphertext = AESGCMEncryption.encrypt(plaintext, aesKey);
        assertNotNull(ciphertext);
        assertNotEquals(plaintext, ciphertext);

        String decrypted = AESGCMEncryption.decrypt(ciphertext, aesKey);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void testRSAEncryption() {
        String plaintext = "Secret message";

        String publicKeyPem = RSAEncryption.getPublicKeyPEM();
        String ciphertext = RSAEncryption.encrypt(plaintext, publicKeyPem);
        assertNotNull(ciphertext);

        String privateKeyPem = RSAEncryption.getPrivateKeyPEM();
        String decrypted = RSAEncryption.decrypt(ciphertext, privateKeyPem);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void testRateLimiterRefill() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(2, 100);
        String clientId = "refill-test";

        assertTrue(limiter.allowRequest(clientId));
        assertTrue(limiter.allowRequest(clientId));
        assertFalse(limiter.allowRequest(clientId));

        Thread.sleep(150);

        assertTrue(limiter.allowRequest(clientId));
    }
}
