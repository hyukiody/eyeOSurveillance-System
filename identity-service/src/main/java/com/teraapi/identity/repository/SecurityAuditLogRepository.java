package com.teraapi.identity.repository;

import com.teraapi.identity.entity.SecurityAuditLog;
import com.teraapi.identity.entity.SecurityAuditLog.EventType;
import com.teraapi.identity.entity.SecurityAuditLog.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for SecurityAuditLog entity
 * 
 * Provides data access methods with automatic 90-day retention cleanup
 */
@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {
    
    /**
     * Find logs by username within time range
     */
    List<SecurityAuditLog> findByUsernameAndTimestampBetween(
        String username, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    /**
     * Find logs by event type
     */
    List<SecurityAuditLog> findByEventTypeOrderByTimestampDesc(EventType eventType);
    
    /**
     * Find failed login attempts for a username
     */
    @Query("SELECT l FROM SecurityAuditLog l WHERE l.username = :username " +
           "AND l.eventType = 'LOGIN_FAILED' AND l.timestamp > :since")
    List<SecurityAuditLog> findRecentFailedLogins(
        @Param("username") String username, 
        @Param("since") LocalDateTime since
    );
    
    /**
     * Find critical security events
     */
    List<SecurityAuditLog> findBySeverityOrderByTimestampDesc(Severity severity);
    
    /**
     * Find logs by IP address (for suspicious activity detection)
     */
    List<SecurityAuditLog> findByIpAddressAndTimestampAfter(
        String ipAddress, 
        LocalDateTime since
    );
    
    /**
     * Delete logs older than specified date (90-day retention policy)
     */
    @Modifying
    @Query("DELETE FROM SecurityAuditLog l WHERE l.timestamp < :cutoffDate")
    int deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count failed login attempts for IP address in time window
     */
    @Query("SELECT COUNT(l) FROM SecurityAuditLog l WHERE l.ipAddress = :ipAddress " +
           "AND l.eventType = 'LOGIN_FAILED' AND l.timestamp > :since")
    long countFailedLoginsByIp(
        @Param("ipAddress") String ipAddress, 
        @Param("since") LocalDateTime since
    );
}
