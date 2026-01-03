package com.teraapi.stream;

import java.util.function.Predicate;

/**
 * Lightweight Event Pattern for CEP-style stream matching
 * Persistent query stored in memory, events flow past it
 */
public class EventPattern<T> {
    private final String patternId;
    private final Predicate<T> matcher;
    private final String description;
    private final long createdAt;

    public EventPattern(String patternId, Predicate<T> matcher, String description) {
        this.patternId = patternId;
        this.matcher = matcher;
        this.description = description;
        this.createdAt = System.currentTimeMillis();
    }

    public String getPatternId() {
        return patternId;
    }

    public boolean matches(T event) {
        return matcher.test(event);
    }

    public String getDescription() {
        return description;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
