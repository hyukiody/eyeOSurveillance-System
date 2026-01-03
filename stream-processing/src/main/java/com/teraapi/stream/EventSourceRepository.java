package com.teraapi.stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for EventSource - Camera/device registration
 */
@Repository
public interface EventSourceRepository extends JpaRepository<EventSource, Long> {
    
    /**
     * Find event source by device ID
     */
    Optional<EventSource> findByDeviceId(String deviceId);
    
    /**
     * Check if device is registered
     */
    boolean existsByDeviceId(String deviceId);
}
