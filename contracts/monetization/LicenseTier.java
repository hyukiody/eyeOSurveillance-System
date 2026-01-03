package com.eyeo.contracts.monetization;

/**
 * License tier enumeration for eyeO platform monetization strategy.
 * 
 * Defines three commercial tiers with distinct feature sets and quotas:
 * - FREE: Limited trial tier with simulated AI and watermarked video
 * - PRO: Production tier with real YOLOv8 detection and premium storage
 * - ENTERPRISE: Custom tier with unlimited resources and dedicated support
 */
public enum LicenseTier {
    
    FREE(
        "Free Trial",
        0,                  // Price in USD cents
        1,                  // Max cameras
        5,                  // Storage quota in GB
        10,                 // API rate limit (requests/second)
        7,                  // Retention days
        "480p",             // Video quality
        true,               // Has watermark
        false,              // Real AI detection
        "AES-128"           // Encryption level
    ),
    
    PRO(
        "Professional",
        2900,               // $29/month
        5,                  // Max cameras
        100,                // Storage quota in GB
        100,                // API rate limit (requests/second)
        30,                 // Retention days
        "1080p",            // Video quality
        false,              // Has watermark
        true,               // Real AI detection (YOLOv8)
        "AES-256"           // Encryption level
    ),
    
    ENTERPRISE(
        "Enterprise",
        -1,                 // Custom pricing
        -1,                 // Unlimited cameras
        -1,                 // Unlimited storage
        1000,               // API rate limit (requests/second)
        -1,                 // Custom retention (unlimited)
        "4K",               // Video quality
        false,              // Has watermark
        true,               // Real AI detection + custom models
        "AES-256-HSM"       // Hardware Security Module
    );
    
    private final String displayName;
    private final int priceUsdCents;
    private final int maxCameras;
    private final int storageQuotaGb;
    private final int apiRateLimit;
    private final int retentionDays;
    private final String videoQuality;
    private final boolean hasWatermark;
    private final boolean realAiDetection;
    private final String encryptionLevel;
    
    LicenseTier(String displayName, int priceUsdCents, int maxCameras, 
                int storageQuotaGb, int apiRateLimit, int retentionDays,
                String videoQuality, boolean hasWatermark, 
                boolean realAiDetection, String encryptionLevel) {
        this.displayName = displayName;
        this.priceUsdCents = priceUsdCents;
        this.maxCameras = maxCameras;
        this.storageQuotaGb = storageQuotaGb;
        this.apiRateLimit = apiRateLimit;
        this.retentionDays = retentionDays;
        this.videoQuality = videoQuality;
        this.hasWatermark = hasWatermark;
        this.realAiDetection = realAiDetection;
        this.encryptionLevel = encryptionLevel;
    }
    
    public String getDisplayName() { return displayName; }
    public int getPriceUsdCents() { return priceUsdCents; }
    public int getMaxCameras() { return maxCameras; }
    public int getStorageQuotaGb() { return storageQuotaGb; }
    public int getApiRateLimit() { return apiRateLimit; }
    public int getRetentionDays() { return retentionDays; }
    public String getVideoQuality() { return videoQuality; }
    public boolean hasWatermark() { return hasWatermark; }
    public boolean hasRealAiDetection() { return realAiDetection; }
    public String getEncryptionLevel() { return encryptionLevel; }
    
    /**
     * Check if this tier allows unlimited resources (-1 quota)
     */
    public boolean isUnlimited(String resource) {
        return switch (resource.toLowerCase()) {
            case "cameras" -> maxCameras == -1;
            case "storage" -> storageQuotaGb == -1;
            case "retention" -> retentionDays == -1;
            default -> false;
        };
    }
    
    /**
     * Validate if usage is within tier limits
     */
    public boolean isWithinLimit(String resource, int currentUsage) {
        if (isUnlimited(resource)) return true;
        
        return switch (resource.toLowerCase()) {
            case "cameras" -> currentUsage < maxCameras;
            case "storage" -> currentUsage < storageQuotaGb;
            default -> false;
        };
    }
}
