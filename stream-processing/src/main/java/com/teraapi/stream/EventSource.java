package com.teraapi.stream;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * EventSource Entity - Represents camera/edge device generating events
 * Maps to: event_sources table in stream database
 */
@Entity
@Table(name = "event_sources")
public class EventSource {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "device_id", unique = true, nullable = false, length = 100)
    private String deviceId;
    
    @Column(name = "device_name", length = 255)
    private String deviceName;
    
    @Column(name = "location", length = 500)
    private String location;
    
    @Column(name = "status", length = 50)
    private String status = "ACTIVE";
    
    @Column(name = "last_heartbeat")
    private Instant lastHeartbeat;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Instant getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(Instant lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
