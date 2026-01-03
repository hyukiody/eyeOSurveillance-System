package com.teraapi.stream;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * DetectionEventEntity - Blue Flow metadata storage
 * Maps to: detection_events table in stream database
 * 
 * CRITICAL: This entity stores ONLY metadata, never actual video content.
 * The storage_ref field is a BLIND REFERENCE to encrypted video that the
 * server CANNOT decrypt per CaCTUs zero-trust architecture.
 */
@Entity
@Table(name = "detection_events")
public class DetectionEventEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_id", unique = true, nullable = false, length = 100)
    private String eventId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", foreignKey = @ForeignKey(name = "fk_events_source"))
    private EventSource source;
    
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @Column(name = "confidence", precision = 5, scale = 4)
    private Double confidence;
    
    /**
     * BLIND STORAGE REFERENCE
     * Points to processed video chunks that server uses separate restoration process
     * Example: "processed/2026/01/02/session-uuid"
     */
    @Column(name = "storage_ref", length = 500)
    private String storageRef;
    
    @Column(name = "storage_type", length = 50)
    private String storageType; // e.g., "AES-256-GCM-CHUNKED"
    
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    // Bounding box coordinates
    @Column(name = "bbox_x")
    private Integer bboxX;
    
    @Column(name = "bbox_y")
    private Integer bboxY;
    
    @Column(name = "bbox_width")
    private Integer bboxWidth;
    
    @Column(name = "bbox_height")
    private Integer bboxHeight;
    
    /**
     * Additional metadata as JSON
     * Example: {"zone": "entrance", "direction": "entering", "trackId": "123"}
     */
    @Column(name = "metadata", columnDefinition = "JSON")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> metadata;
    
    @Column(name = "status", length = 50)
    private String status = "PENDING"; // PENDING, PROCESSED, ARCHIVED
    
    @Column(name = "processed_at")
    private Instant processedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public EventSource getSource() { return source; }
    public void setSource(EventSource source) { this.source = source; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    
    public String getStorageRef() { return storageRef; }
    public void setStorageRef(String storageRef) { this.storageRef = storageRef; }
    
    public String getStorageType() { return storageType; }
    public void setStorageType(String storageType) { this.storageType = storageType; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    
    public Integer getBboxX() { return bboxX; }
    public void setBboxX(Integer bboxX) { this.bboxX = bboxX; }
    
    public Integer getBboxY() { return bboxY; }
    public void setBboxY(Integer bboxY) { this.bboxY = bboxY; }
    
    public Integer getBboxWidth() { return bboxWidth; }
    public void setBboxWidth(Integer bboxWidth) { this.bboxWidth = bboxWidth; }
    
    public Integer getBboxHeight() { return bboxHeight; }
    public void setBboxHeight(Integer bboxHeight) { this.bboxHeight = bboxHeight; }
    
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
    
    public Instant getCreatedAt() { return createdAt; }
}
