package com.teraapi.identity.service;

import com.teraapi.identity.entity.SecurityAuditLog;
import com.teraapi.identity.entity.SecurityAuditLog.EventType;
import com.teraapi.identity.entity.SecurityAuditLog.Severity;
import com.teraapi.identity.repository.SecurityAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Security Audit Logging Service
 * 
 * Provides centralized audit logging for all security-sensitive events.
 * Implements automatic 90-day retention policy with scheduled cleanup.
 * 
 * @author eyeO Platform Security Team
 * @version 1.0
 */
@Service
public class AuditLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);
    private static final int RETENTION_DAYS = 90;
    
    @Autowired
    private SecurityAuditLogRepository auditLogRepository;
    
    /**
     * Log a security event
     */
    @Transactional
    public void log(SecurityAuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Never fail application due to audit logging errors
            logger.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Log successful login
     */
    public void logLoginSuccess(String username, String ipAddress, String userAgent, String sessionId) {
        log(SecurityAuditLog.builder()
            .eventType(EventType.LOGIN_SUCCESS)
            .severity(Severity.INFO)
            .username(username)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .sessionId(sessionId)
            .success(true)
            .message("User logged in successfully")
            .resource("/api/auth/login")
            .httpMethod("POST")
            .statusCode(200)
            .build());
    }
    
    /**
     * Log failed login attempt
     */
    public void logLoginFailed(String username, String ipAddress, String userAgent, String reason) {
        log(SecurityAuditLog.builder()
            .eventType(EventType.LOGIN_FAILED)
            .severity(Severity.WARN)
            .username(username)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .success(false)
            .message("Login failed: " + reason)
            .resource("/api/auth/login")
            .httpMethod("POST")
            .statusCode(401)
            .build());
    }
    
    /**
     * Log quota exceeded event
     */
    public void logQuotaExceeded(String username, String quotaType, String ipAddress) {
        log(SecurityAuditLog.builder()
            .eventType(EventType.QUOTA_EXCEEDED)
            .severity(Severity.WARN)
            .username(username)
            .ipAddress(ipAddress)
            .success(false)
            .message("Quota exceeded: " + quotaType)
            .metadata("{\"quotaType\":\"" + quotaType + "\"}")
            .statusCode(429)
            .build());
    }
    
    /**
     * Log access denied event
     */
    public void logAccessDenied(String username, String resource, String ipAddress, String reason) {
        log(SecurityAuditLog.builder()
            .eventType(EventType.ACCESS_DENIED)
            .severity(Severity.WARN)
            .username(username)
            .ipAddress(ipAddress)
            .resource(resource)
            .success(false)
            .message("Access denied: " + reason)
            .statusCode(403)
            .build());
    }
    
    /**
     * Log brute force detection
     */
    public void logBruteForceDetected(String username, String ipAddress, int attemptCount) {
        log(SecurityAuditLog.builder()
            .eventType(EventType.BRUTE_FORCE_DETECTED)
            .severity(Severity.CRITICAL)
            .username(username)
            .ipAddress(ipAddress)
            .success(false)
            .message("Brute force attack detected: " + attemptCount + " failed attempts")
            .metadata("{\"attemptCount\":" + attemptCount + "}")
            .build());
    }
    
    /**
     * Log account lockout
     */
    public void logAccountLocked(String username, String ipAddress) {
        log(SecurityAuditLog.builder()
            .eventType(EventType.ACCOUNT_LOCKED)
            .severity(Severity.CRITICAL)
            .username(username)
            .ipAddress(ipAddress)
            .success(true)
            .message("Account locked due to excessive failed login attempts")
            .statusCode(403)
            .build());
    }
    
    /**
     * Check for brute force attack pattern
     * Returns true if suspicious activity detected
     */
    public boolean detectBruteForce(String username, String ipAddress) {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        
        // Check failed attempts by username
        List<SecurityAuditLog> userAttempts = auditLogRepository
            .findRecentFailedLogins(username, fiveMinutesAgo);
        
        // Check failed attempts by IP
        long ipAttempts = auditLogRepository
            .countFailedLoginsByIp(ipAddress, fiveMinutesAgo);
        
        // Threshold: 5 failed attempts in 5 minutes (configurable)
        if (userAttempts.size() >= 5 || ipAttempts >= 5) {
            logBruteForceDetected(username, ipAddress, 
                Math.max(userAttempts.size(), (int) ipAttempts));
            return true;
        }
        
        return false;
    }
    
    /**
     * Get recent critical events (for dashboard/alerts)
     */
    public List<SecurityAuditLog> getCriticalEvents(int hours) {
        return auditLogRepository.findBySeverityOrderByTimestampDesc(Severity.CRITICAL);
    }
    
    /**
     * Scheduled task to cleanup old logs (runs daily at 2 AM)
     * Implements 90-day retention policy
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldLogs() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
            int deletedCount = auditLogRepository.deleteOldLogs(cutoffDate);
            
            logger.info("Audit log cleanup completed: {} records deleted (older than {})", 
                deletedCount, cutoffDate);
            
            // Log the cleanup activity itself
            log(SecurityAuditLog.builder()
                .eventType(EventType.CONFIGURATION_CHANGED)
                .severity(Severity.INFO)
                .ipAddress("SYSTEM")
                .success(true)
                .message("Audit log retention cleanup: " + deletedCount + " records deleted")
                .metadata("{\"deletedCount\":" + deletedCount + ",\"retentionDays\":" + RETENTION_DAYS + "}")
                .build());
                
        } catch (Exception e) {
            logger.error("Failed to cleanup old audit logs: {}", e.getMessage(), e);
        }
    }
}
