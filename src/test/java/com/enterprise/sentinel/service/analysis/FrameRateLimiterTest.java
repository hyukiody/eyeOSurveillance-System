package com.enterprise.sentinel.service.analysis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FrameRateLimiterTest {

    private FrameRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new FrameRateLimiter(2); // 2 FPS = 500ms between frames
    }

    @Test
    void shouldAllowFirstFrame() {
        assertTrue(limiter.shouldProcessFrame(), "First frame should always be processed");
    }

    @Test
    void shouldDropFrameWithinInterval() {
        // First frame
        assertTrue(limiter.shouldProcessFrame());
        
        // Second frame immediately after (should be dropped)
        assertFalse(limiter.shouldProcessFrame(), 
            "Frame within 500ms interval should be dropped");
    }

    @Test
    void shouldAllowFrameAfterInterval() throws InterruptedException {
        // First frame
        assertTrue(limiter.shouldProcessFrame());
        
        // Wait for interval to elapse
        Thread.sleep(510);
        
        // Second frame (should be allowed)
        assertTrue(limiter.shouldProcessFrame(), 
            "Frame after interval should be processed");
    }

    @Test
    void shouldMaintain2FpsTarget() throws InterruptedException {
        limiter = new FrameRateLimiter(2); // 500ms target
        
        assertTrue(limiter.shouldProcessFrame()); // Frame 1
        assertFalse(limiter.shouldProcessFrame()); // Frame 2 (dropped)
        assertFalse(limiter.shouldProcessFrame()); // Frame 3 (dropped)
        
        Thread.sleep(510);
        
        assertTrue(limiter.shouldProcessFrame()); // Frame 4 (allowed at ~500ms)
    }

    @Test
    void shouldResetState() {
        assertTrue(limiter.shouldProcessFrame());
        assertFalse(limiter.shouldProcessFrame());
        
        limiter.reset();
        
        assertTrue(limiter.shouldProcessFrame(), "After reset, next frame should be processed");
    }

    @Test
    void shouldRejectInvalidFps() {
        try {
            new FrameRateLimiter(0);
            assertFalse(true, "Should reject FPS <= 0");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    void shouldExposeTargetInterval() {
        assertEquals(500, limiter.getTargetFrameIntervalMs(), 
            "2 FPS should equal 500ms interval");
    }

    private void assertEquals(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message);
        }
    }
}
