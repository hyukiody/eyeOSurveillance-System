package com.eyeo.contracts.monetization;

import java.util.EnumSet;
import java.util.Set;

/**
 * Feature flags for tier-based access control.
 * 
 * Uses composition pattern to enable/disable specific features per tier.
 * Each feature can be checked at runtime to gate functionality.
 */
public enum FeatureFlags {
    
    // AI & Detection Features
    REAL_YOLO_DETECTION("Real-time YOLOv8 object detection", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE)),
    CUSTOM_AI_MODELS("Custom trained AI models", EnumSet.of(LicenseTier.ENTERPRISE)),
    FACIAL_RECOGNITION("Facial recognition capability", EnumSet.of(LicenseTier.ENTERPRISE)),
    BEHAVIOR_ANALYSIS("Advanced behavior pattern analysis", EnumSet.of(LicenseTier.ENTERPRISE)),
    
    // Video Features
    HD_VIDEO_QUALITY("1080p video streaming", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE)),
    FOUR_K_VIDEO("4K video streaming", EnumSet.of(LicenseTier.ENTERPRISE)),
    NO_WATERMARK("Watermark-free video", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE)),
    LIVE_STREAMING("Real-time live streaming", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE)),
    
    // Storage & Retention
    EXTENDED_RETENTION("30+ days video retention", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE)),
    UNLIMITED_STORAGE("Unlimited cloud storage", EnumSet.of(LicenseTier.ENTERPRISE)),
    REMOTE_PRESERVATION("Automatic remote backup", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE)),
    WRITE_ONLY_BACKUP("Write-only cloud backup (anti-theft)", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE)),
    
    // Security Features
    AES_256_ENCRYPTION("AES-256-GCM encryption", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE)),
    HSM_ENCRYPTION("Hardware Security Module encryption", EnumSet.of(LicenseTier.ENTERPRISE)),
    KEY_ROTATION("Automatic key rotation", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE)),
    CRYPTO_SHREDDING("GDPR-compliant crypto-shredding", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE)),
    
    // API & Integration
    WEBHOOK_NOTIFICATIONS("Webhook event notifications", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE)),
    REST_API_ACCESS("Full REST API access", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE)),
    MQTT_INTEGRATION("MQTT broker integration", EnumSet.of(LicenseTier.ENTERPRISE)),
    CUSTOM_INTEGRATIONS("Custom third-party integrations", EnumSet.of(LicenseTier.ENTERPRISE)),
    
    // Support & SLA
    EMAIL_SUPPORT("Email support", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE)),
    PRIORITY_SUPPORT("Priority support with SLA", EnumSet.of(LicenseTier.ENTERPRISE)),
    DEDICATED_ACCOUNT_MANAGER("Dedicated account manager", EnumSet.of(LicenseTier.ENTERPRISE)),
    
    // Advanced Features
    MULTI_SITE_MANAGEMENT("Multi-site management dashboard", EnumSet.of(LicenseTier.ENTERPRISE)),
    ROLE_BASED_ACCESS_CONTROL("RBAC with custom roles", EnumSet.of(LicenseTier.ENTERPRISE)),
    AUDIT_LOGGING("Comprehensive audit logs", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE)),
    CUSTOM_ALERTS("Custom alert rules and triggers", EnumSet.of(LicenseTier.PRO, LicenseTier.ENTERPRISE));
    
    private final String description;
    private final Set<LicenseTier> allowedTiers;
    
    FeatureFlags(String description, Set<LicenseTier> allowedTiers) {
        this.description = description;
        this.allowedTiers = allowedTiers;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this feature is enabled for the given tier
     */
    public boolean isEnabledFor(LicenseTier tier) {
        return allowedTiers.contains(tier);
    }
    
    /**
     * Get all features available for a specific tier
     */
    public static Set<FeatureFlags> getFeaturesForTier(LicenseTier tier) {
        EnumSet<FeatureFlags> enabledFeatures = EnumSet.noneOf(FeatureFlags.class);
        for (FeatureFlags feature : values()) {
            if (feature.isEnabledFor(tier)) {
                enabledFeatures.add(feature);
            }
        }
        return enabledFeatures;
    }
    
    /**
     * Check if tier has access to all required features
     */
    public static boolean hasAllFeatures(LicenseTier tier, FeatureFlags... requiredFeatures) {
        for (FeatureFlags feature : requiredFeatures) {
            if (!feature.isEnabledFor(tier)) {
                return false;
            }
        }
        return true;
    }
}
