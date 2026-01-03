package com.teraapi.stream.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {

    @Test
    public void testRateLimiterAllowsRequests() {
        RateLimiter limiter = new RateLimiter(10);
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.allowRequest("client1"));
        }
        assertFalse(limiter.allowRequest("client1"));
    }

    @Test
    public void testRateLimiterPerClient() {
        RateLimiter limiter = new RateLimiter(5);
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allowRequest("client1"));
            assertTrue(limiter.allowRequest("client2"));
        }
        assertFalse(limiter.allowRequest("client1"));
        assertFalse(limiter.allowRequest("client2"));
    }

    @Test
    public void testGetRemainingTokens() {
        RateLimiter limiter = new RateLimiter(10);
        limiter.allowRequest("client1");
        assertEquals(9, limiter.getRemainingTokens("client1"));
    }
}
