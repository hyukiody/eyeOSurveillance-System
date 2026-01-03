package com.teraapi.stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DetectionEventEntity - Blue Flow metadata storage
 */
@Repository
public interface DetectionEventRepository extends JpaRepository<DetectionEventEntity, Long> {
    
    /**
     * Find event by unique event ID
     */
    Optional<DetectionEventEntity> findByEventId(String eventId);
    
    /**
     * Find events by device/camera source
     */
    List<DetectionEventEntity> findBySource_DeviceId(String deviceId);
    
    /**
     * Find events by type (e.g., PERSON_DETECTED, WEAPON_DETECTED)
     */
    List<DetectionEventEntity> findByEventType(String eventType);
    
    /**
     * Find events by storage reference
     */
    List<DetectionEventEntity> findByStorageRef(String storageRef);
    
    /**
     * Find events in time range
     */
    List<DetectionEventEntity> findByTimestampBetween(Instant start, Instant end);
    
    /**
     * Find events by status (PENDING, PROCESSED, ARCHIVED)
     */
    List<DetectionEventEntity> findByStatus(String status);
    
    /**
     * Count events by device
     */
    long countBySource_DeviceId(String deviceId);
}
