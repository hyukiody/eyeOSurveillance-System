package com.eyeo.contracts.monetization;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Usage quotas and rate limits per license tier.
 * 
 * Provides centralized quota management for enforcing tier-based limits
 * across all microservices (data-core, stream-processing, middleware).
 */
public class UsageQuotas {
    
    private final LicenseTier tier;
    private final Map<String, Integer> currentUsage;
    
    public UsageQuotas(LicenseTier tier) {
        this.tier = tier;
        this.currentUsage = new HashMap<>();
    }
    
    // ===== API Rate Limiting =====
    
    /**
     * Get API rate limit for this tier (requests per second)
     */
    public int getApiRateLimit() {
        return tier.getApiRateLimit();
    }
    
    /**
     * Get burst capacity (5x rate limit for short bursts)
     */
    public int getBurstCapacity() {
        return tier.getApiRateLimit() * 5;
    }
    
    /**
     * Get refill rate for token bucket (requests per second)
     */
    public Duration getRefillDuration() {
        return Duration.ofSeconds(1);
    }
    
    // ===== Storage Quotas =====
    
    /**
     * Get storage quota in bytes
     */
    public long getStorageQuotaBytes() {
        if (tier.isUnlimited("storage")) {
            return Long.MAX_VALUE;
        }
        return (long) tier.getStorageQuotaGb() * 1024 * 1024 * 1024;
    }
    
    /**
     * Get storage quota in GB for display
     */
    public int getStorageQuotaGb() {
        return tier.getStorageQuotaGb();
    }
    
    /**
     * Check if storage upload is within quota
     */
    public boolean canUpload(long sizeBytes) {
        if (tier.isUnlimited("storage")) return true;
        
        long currentStorageBytes = currentUsage.getOrDefault("storage_bytes", 0);
        return (currentStorageBytes + sizeBytes) <= getStorageQuotaBytes();
    }
    
    /**
     * Record storage usage
     */
    public void recordStorageUsage(long bytes) {
        int currentBytes = currentUsage.getOrDefault("storage_bytes", 0);
        currentUsage.put("storage_bytes", currentBytes + (int) bytes);
    }
    
    // ===== Camera Limits =====
    
    /**
     * Get maximum number of cameras allowed
     */
    public int getMaxCameras() {
        return tier.getMaxCameras();
    }
    
    /**
     * Check if another camera can be added
     */
    public boolean canAddCamera() {
        if (tier.isUnlimited("cameras")) return true;
        
        int currentCameras = currentUsage.getOrDefault("cameras", 0);
        return currentCameras < tier.getMaxCameras();
    }
    
    /**
     * Record a new camera
     */
    public void recordNewCamera() {
        int count = currentUsage.getOrDefault("cameras", 0);
        currentUsage.put("cameras", count + 1);
    }
    
    // ===== Retention Policy =====
    
    /**
     * Get retention period in days
     */
    public int getRetentionDays() {
        return tier.getRetentionDays();
    }
    
    /**
     * Get retention period in milliseconds
     */
    public long getRetentionMillis() {
        if (tier.isUnlimited("retention")) {
            return Long.MAX_VALUE;
        }
        return (long) tier.getRetentionDays() * 24 * 60 * 60 * 1000;
    }
    
    /**
     * Check if a timestamp is within retention period
     */
    public boolean isWithinRetention(long timestampMillis) {
        if (tier.isUnlimited("retention")) return true;
        
        long ageMillis = System.currentTimeMillis() - timestampMillis;
        return ageMillis <= getRetentionMillis();
    }
    
    // ===== Detection Limits =====
    
    /**
     * Get max detections per hour (to prevent abuse)
     */
    public int getMaxDetectionsPerHour() {
        return switch (tier) {
            case FREE -> 100;
            case PRO -> 10000;
            case ENTERPRISE -> Integer.MAX_VALUE;
        };
    }
    
    /**
     * Get max concurrent video streams
     */
    public int getMaxConcurrentStreams() {
        return switch (tier) {
            case FREE -> 1;
            case PRO -> 5;
            case ENTERPRISE -> Integer.MAX_VALUE;
        };
    }
    
    // ===== Video Quality =====
    
    /**
     * Get maximum video resolution
     */
    public String getMaxVideoQuality() {
        return tier.getVideoQuality();
    }
    
    /**
     * Get maximum bitrate in kbps
     */
    public int getMaxBitrateKbps() {
        return switch (tier.getVideoQuality()) {
            case "480p" -> 1500;
            case "1080p" -> 5000;
            case "4K" -> 25000;
            default -> 1000;
        };
    }
    
    // ===== Feature Checks =====
    
    /**
     * Check if tier requires watermark
     */
    public boolean requiresWatermark() {
        return tier.hasWatermark();
    }
    
    /**
     * Check if real AI detection is enabled
     */
    public boolean hasRealAiDetection() {
        return tier.hasRealAiDetection();
    }
    
    /**
     * Get encryption level
     */
    public String getEncryptionLevel() {
        return tier.getEncryptionLevel();
    }
    
    // ===== Usage Tracking =====
    
    /**
     * Get current usage for a resource
     */
    public int getCurrentUsage(String resource) {
        return currentUsage.getOrDefault(resource, 0);
    }
    
    /**
     * Get usage percentage for display (0-100)
     */
    public int getUsagePercentage(String resource) {
        int current = currentUsage.getOrDefault(resource, 0);
        int max = switch (resource) {
            case "cameras" -> tier.getMaxCameras();
            case "storage_gb" -> tier.getStorageQuotaGb();
            default -> 100;
        };
        
        if (max == -1) return 0; // Unlimited
        if (max == 0) return 100;
        
        return Math.min(100, (current * 100) / max);
    }
    
    /**
     * Reset usage counters (for testing/periodic reset)
     */
    public void resetUsage() {
        currentUsage.clear();
    }
    
    /**
     * Get tier information
     */
    public LicenseTier getTier() {
        return tier;
    }
}
