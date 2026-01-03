package com.teraapi.identity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Security Audit Log Entity
 * 
 * Tracks security-sensitive events for compliance and threat detection.
 * Implements 90-day retention policy as per SECURITY.md requirements.
 * 
 * @author eyeO Platform Security Team
 * @version 1.0
 */
@Entity
@Table(name = "security_audit_logs", indexes = {
    @Index(name = "idx_event_type", columnList = "eventType"),
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_severity", columnList = "severity"),
    @Index(name = "idx_ip_address", columnList = "ipAddress")
})
public class SecurityAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Type of security event
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EventType eventType;
    
    /**
     * Severity level of the event
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;
    
    /**
     * Username associated with the event (null for anonymous attempts)
     */
    @Column(length = 100)
    private String username;
    
    /**
     * IP address of the request source
     */
    @Column(nullable = false, length = 45) // IPv6 max length
    private String ipAddress;
    
    /**
     * User agent string
     */
    @Column(length = 500)
    private String userAgent;
    
    /**
     * Event timestamp
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * Success or failure indicator
     */
    @Column(nullable = false)
    private Boolean success;
    
    /**
     * Detailed event message
     */
    @Column(length = 1000)
    private String message;
    
    /**
     * Additional context as JSON
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    /**
     * Session ID for correlation
     */
    @Column(length = 100)
    private String sessionId;
    
    /**
     * Resource accessed (e.g., "/api/auth/login")
     */
    @Column(length = 200)
    private String resource;
    
    /**
     * HTTP method (GET, POST, etc.)
     */
    @Column(length = 10)
    private String httpMethod;
    
    /**
     * Response status code
     */
    private Integer statusCode;
    
    // ==================== Enums ====================
    
    public enum EventType {
        // Authentication Events
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        LOGOUT,
        TOKEN_GENERATED,
        TOKEN_EXPIRED,
        TOKEN_REVOKED,
        
        // Authorization Events
        ACCESS_GRANTED,
        ACCESS_DENIED,
        PERMISSION_ESCALATION_ATTEMPT,
        
        // Account Management
        USER_REGISTERED,
        USER_DELETED,
        PASSWORD_CHANGED,
        PASSWORD_RESET_REQUESTED,
        PASSWORD_RESET_COMPLETED,
        
        // License & Quota Events
        QUOTA_EXCEEDED,
        LICENSE_EXPIRED,
        TRIAL_STARTED,
        TRIAL_EXPIRED,
        SUBSCRIPTION_UPGRADED,
        SUBSCRIPTION_DOWNGRADED,
        
        // Security Events
        BRUTE_FORCE_DETECTED,
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED,
        SUSPICIOUS_ACTIVITY,
        RATE_LIMIT_EXCEEDED,
        INVALID_TOKEN,
        
        // Data Protection Events
        STREAM_PROCESSED,
        STREAM_ACCESSED,
        DATA_DELETED,
        KEY_ROTATED,
        
        // System Events
        CONFIGURATION_CHANGED,
        SERVICE_STARTED,
        SERVICE_STOPPED,
        HEALTH_CHECK_FAILED
    }
    
    public enum Severity {
        INFO,      // Normal operations
        WARN,      // Warning conditions
        ERROR,     // Error conditions
        CRITICAL   // Critical security events requiring immediate attention
    }
    
    // ==================== Constructors ====================
    
    public SecurityAuditLog() {
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Builder pattern for creating audit logs
     */
    public static class Builder {
        private SecurityAuditLog log = new SecurityAuditLog();
        
        public Builder eventType(EventType eventType) {
            log.eventType = eventType;
            return this;
        }
        
        public Builder severity(Severity severity) {
            log.severity = severity;
            return this;
        }
        
        public Builder username(String username) {
            log.username = username;
            return this;
        }
        
        public Builder ipAddress(String ipAddress) {
            log.ipAddress = ipAddress;
            return this;
        }
        
        public Builder userAgent(String userAgent) {
            log.userAgent = userAgent;
            return this;
        }
        
        public Builder success(Boolean success) {
            log.success = success;
            return this;
        }
        
        public Builder message(String message) {
            log.message = message;
            return this;
        }
        
        public Builder metadata(String metadata) {
            log.metadata = metadata;
            return this;
        }
        
        public Builder sessionId(String sessionId) {
            log.sessionId = sessionId;
            return this;
        }
        
        public Builder resource(String resource) {
            log.resource = resource;
            return this;
        }
        
        public Builder httpMethod(String httpMethod) {
            log.httpMethod = httpMethod;
            return this;
        }
        
        public Builder statusCode(Integer statusCode) {
            log.statusCode = statusCode;
            return this;
        }
        
        public SecurityAuditLog build() {
            // Validate required fields
            if (log.eventType == null) {
                throw new IllegalStateException("EventType is required");
            }
            if (log.severity == null) {
                throw new IllegalStateException("Severity is required");
            }
            if (log.ipAddress == null) {
                throw new IllegalStateException("IP Address is required");
            }
            if (log.success == null) {
                throw new IllegalStateException("Success status is required");
            }
            return log;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // ==================== Getters & Setters ====================
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public EventType getEventType() {
        return eventType;
    }
    
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getResource() {
        return resource;
    }
    
    public void setResource(String resource) {
        this.resource = resource;
    }
    
    public String getHttpMethod() {
        return httpMethod;
    }
    
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    
    public Integer getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
}
