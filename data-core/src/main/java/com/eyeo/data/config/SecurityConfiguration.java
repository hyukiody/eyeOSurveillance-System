package com.eyeo.data.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized Security Configuration
 * 
 * Provides standardized security constants and environment-driven key management
 * across all eyeO platform services. Implements Zero-Trust principles with
 * externalized configuration for production deployments.
 * 
 * @author eyeO Platform Security Team
 * @version 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "eyeo.security")
public class SecurityConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);
    
    // ==================== Stream Protection Configuration ====================
    
    /**
     * Data transformation algorithm (default: AES/GCM/NoPadding)
     * GCM mode provides authenticated encryption with associated data (AEAD)
     */
    public static final String TRANSFORM_ALGORITHM = "AES/GCM/NoPadding";
    
    /**
     * Symmetric key size in bits (256-bit for military-grade protection)
     */
    public static final int KEY_SIZE_BITS = 256;
    
    /**
     * Initialization Vector (IV) size in bytes (96 bits recommended for GCM)
     */
    public static final int IV_SIZE_BYTES = 12;
    
    /**
     * Authentication tag size in bits (128-bit tag for GCM integrity)
     */
    public static final int AUTH_TAG_SIZE_BITS = 128;
    
    /**
     * Stream processing chunk size (64KB for optimal memory/performance balance)
     */
    public static final int CHUNK_SIZE_BYTES = 64 * 1024;
    
    // ==================== Key Derivation Configuration ====================
    
    /**
     * Key derivation function algorithm
     */
    public static final String KDF_ALGORITHM = "PBKDF2WithHmacSHA256";
    
    /**
     * PBKDF2 iteration count (100,000 iterations as per OWASP 2024 recommendations)
     */
    public static final int PBKDF2_ITERATIONS = 100_000;
    
    /**
     * Salt size for key derivation (256 bits)
     */
    public static final int SALT_SIZE_BYTES = 32;
    
    // ==================== JWT Configuration ====================
    
    /**
     * JWT signing algorithm (HS512 for symmetric key signatures)
     */
    public static final String JWT_ALGORITHM = "HS512";
    
    /**
     * JWT token expiration time in milliseconds (24 hours)
     */
    public static final long JWT_EXPIRATION_MS = 24 * 60 * 60 * 1000;
    
    /**
     * JWT refresh token expiration (7 days)
     */
    public static final long JWT_REFRESH_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000;
    
    // ==================== Session & Rate Limiting ====================
    
    /**
     * Session timeout in minutes (30 minutes idle timeout)
     */
    public static final int SESSION_TIMEOUT_MINUTES = 30;
    
    /**
     * API rate limit (requests per second per user)
     */
    public static final int RATE_LIMIT_PER_SECOND = 10;
    
    /**
     * Maximum failed login attempts before account lockout
     */
    public static final int MAX_FAILED_ATTEMPTS = 5;
    
    /**
     * Account lockout duration in minutes
     */
    public static final int LOCKOUT_DURATION_MINUTES = 15;
    
    // ==================== Environment-Driven Properties ====================
    
    @Value("${EYEO_MASTER_KEY:}")
    private String masterKey;
    
    @Value("${JWT_SECRET_KEY:}")
    private String jwtSecret;
    
    @Value("${STORAGE_PATH:/protected-storage}")
    private String storagePath;
    
    @Value("${EYEO_ENV:development}")
    private String environment;
    
    @Value("${ENABLE_KMS:false}")
    private boolean enableKMS;
    
    @Value("${KMS_KEY_ID:}")
    private String kmsKeyId;
    
    // ==================== Validation & Initialization ====================
    
    @PostConstruct
    public void validateConfiguration() {
        logger.info("=================================================");
        logger.info("  eyeO Security Configuration Validation");
        logger.info("=================================================");
        logger.info("Environment: {}", environment);
        logger.info("Transform Algorithm: {}", TRANSFORM_ALGORITHM);
        logger.info("Key Size: {} bits", KEY_SIZE_BITS);
        logger.info("PBKDF2 Iterations: {}", PBKDF2_ITERATIONS);
        logger.info("JWT Algorithm: {}", JWT_ALGORITHM);
        logger.info("Rate Limit: {} req/sec", RATE_LIMIT_PER_SECOND);
        
        // Production environment validation
        if ("production".equalsIgnoreCase(environment)) {
            validateProductionSecurity();
        } else {
            logger.warn("⚠️  DEVELOPMENT MODE - Using default configurations");
        }
        
        logger.info("=================================================\n");
    }
    
    private void validateProductionSecurity() {
        boolean isValid = true;
        
        // Validate master key is set
        if (masterKey == null || masterKey.isEmpty()) {
            logger.error("❌ CRITICAL: EYEO_MASTER_KEY environment variable not set");
            isValid = false;
        }
        
        // Validate JWT secret is set and strong
        if (jwtSecret == null || jwtSecret.length() < 32) {
            logger.error("❌ CRITICAL: JWT_SECRET_KEY must be at least 32 characters");
            isValid = false;
        }
        
        // Validate KMS configuration if enabled
        if (enableKMS && (kmsKeyId == null || kmsKeyId.isEmpty())) {
            logger.error("❌ CRITICAL: KMS enabled but KMS_KEY_ID not configured");
            isValid = false;
        }
        
        if (!isValid) {
            throw new IllegalStateException(
                "Production security validation failed. " +
                "Review logs and configure required environment variables."
            );
        }
        
        logger.info("✓ Production security validation passed");
    }
    
    // ==================== Getters ====================
    
    public String getMasterKey() {
        return masterKey;
    }
    
    public String getJwtSecret() {
        return jwtSecret;
    }
    
    public String getStoragePath() {
        return storagePath;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public boolean isKMSEnabled() {
        return enableKMS;
    }
    
    public String getKmsKeyId() {
        return kmsKeyId;
    }
    
    /**
     * Check if running in production mode
     */
    public boolean isProduction() {
        return "production".equalsIgnoreCase(environment);
    }
    
    /**
     * Check if running in development mode
     */
    public boolean isDevelopment() {
        return "development".equalsIgnoreCase(environment);
    }
}
