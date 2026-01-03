package com.teraapi.stream.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiter {
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final int requestsPerMinute;

    public RateLimiter(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    public boolean allowRequest(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, k -> new TokenBucket(requestsPerMinute));
        return bucket.consumeToken();
    }

    public int getRemainingTokens(String clientId) {
        TokenBucket bucket = buckets.get(clientId);
        return bucket != null ? bucket.getAvailableTokens() : requestsPerMinute;
    }

    private static class TokenBucket {
        private final int capacity;
        private final AtomicInteger tokens;
        private long lastRefillTime;

        TokenBucket(int capacity) {
            this.capacity = capacity;
            this.tokens = new AtomicInteger(capacity);
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean consumeToken() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        synchronized int getAvailableTokens() {
            refill();
            return tokens.get();
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;
            int tokensToAdd = (int) (timePassed / 60000 * capacity);
            if (tokensToAdd > 0) {
                tokens.set(Math.min(capacity, tokens.get() + tokensToAdd));
                lastRefillTime = now;
            }
        }
    }
}
