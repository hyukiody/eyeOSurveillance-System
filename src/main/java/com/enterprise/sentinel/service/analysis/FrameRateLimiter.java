package com.enterprise.sentinel.service.analysis;

import org.springframework.stereotype.Service;

/**
 * Performance optimization: Limits AI inference to a sustainable frame rate
 * to prevent UI freezes and excessive resource consumption.
 * Default: 2 FPS cap ensures fluid UI even on modest hardware.
 */
@Service
public class FrameRateLimiter {

    private final long targetFrameIntervalMs; // Milliseconds between allowed frames
    private volatile long lastProcessedTimeMs = 0;

    public FrameRateLimiter() {
        // Default: 2 FPS = 500ms between frames
        this(2);
    }

    public FrameRateLimiter(int targetFps) {
        if (targetFps <= 0) {
            throw new IllegalArgumentException("Target FPS must be positive");
        }
        this.targetFrameIntervalMs = 1000 / targetFps;
    }

    /**
     * Check if enough time has elapsed to process the next frame.
     * Thread-safe for concurrent access from async processors.
     *
     * @return true if frame should be processed, false if should be dropped
     */
    public boolean shouldProcessFrame() {
        long now = System.currentTimeMillis();
        long timeSinceLastFrame = now - lastProcessedTimeMs;

        if (timeSinceLastFrame >= targetFrameIntervalMs) {
            lastProcessedTimeMs = now;
            return true;
        }
        return false;
    }

    /**
     * Get the configured target frame interval in milliseconds.
     */
    public long getTargetFrameIntervalMs() {
        return targetFrameIntervalMs;
    }

    /**
     * Reset the limiter (useful for testing or stream transitions).
     */
    public void reset() {
        lastProcessedTimeMs = 0;
    }
}
