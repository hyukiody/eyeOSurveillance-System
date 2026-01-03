package com.teraapi.stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * BlueFlowIngestionEndpoint - Phase 2/3: Blue Flow Implementation
 * 
 * Receives AI detection events from middleware (image-inverter) and stores
 * metadata in stream database. This implements the "Blue Flow" of the CaCTUs
 * architecture where ONLY metadata is processed, never the actual video content.
 * 
 * Architecture:
 * - Edge Node captures video → sends to data-core (Red Flow)
 * - Edge Node runs YOLOv8 detection → sends metadata here (Blue Flow)
 * - Metadata includes storage_ref to encrypted video (blind reference)
 * - Server processes metadata but CANNOT decrypt video
 * 
 * Database Integration:
 * - Stores to detection_events table
 * - References event_sources (camera/device)
 * - Links to encrypted storage via storage_ref field
 * - Enables pattern matching without video access
 */
@RestController
@RequestMapping("/api/v1/events")
public class BlueFlowIngestionEndpoint {
    
    private static final Logger logger = LoggerFactory.getLogger(BlueFlowIngestionEndpoint.class);
    
    @Autowired
    private DetectionEventRepository eventRepository;
    
    @Autowired
    private EventSourceRepository sourceRepository;
    
    /**
     * Ingest detection event with storage reference
     * 
     * Body: {
     *   "eventId": "uuid",
     *   "deviceId": "camera-001",
     *   "eventType": "PERSON_DETECTED",
     *   "confidence": 0.95,
     *   "storageRef": "encrypted/2026/01/02/session-uuid",
     *   "timestamp": "2026-01-02T10:30:00Z",
     *   "duration": 30,
     *   "bbox": {"x": 100, "y": 200, "width": 150, "height": 300},
     *   "metadata": {"zone": "entrance", "direction": "entering"}
     * }
     */
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, Object>> ingestDetectionEvent(
            @RequestBody DetectionEventRequest request,
            @RequestHeader(value = "X-Device-ID", required = true) String deviceId,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        
        logger.info("Ingesting Blue Flow event - Device: {}, Type: {}, Confidence: {}", 
            deviceId, request.getEventType(), request.getConfidence());
        
        try {
            // Validate device exists
            EventSource source = sourceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not registered: " + deviceId));
            
            // Update device heartbeat
            source.setLastHeartbeat(Instant.now());
            sourceRepository.save(source);
            
            // Create detection event entity
            DetectionEventEntity event = new DetectionEventEntity();
            event.setEventId(request.getEventId());
            event.setSource(source);
            event.setEventType(request.getEventType());
            event.setConfidence(request.getConfidence());
            event.setStorageRef(request.getStorageRef()); // BLIND REFERENCE
            event.setStorageType("AES-256-GCM-CHUNKED");
            event.setTimestamp(request.getTimestamp());
            event.setDurationSeconds(request.getDuration());
            
            // Bounding box (if present)
            if (request.getBbox() != null) {
                event.setBboxX(request.getBbox().getX());
                event.setBboxY(request.getBbox().getY());
                event.setBboxWidth(request.getBbox().getWidth());
                event.setBboxHeight(request.getBbox().getHeight());
            }
            
            // Additional metadata as JSON
            event.setMetadata(request.getMetadata());
            event.setStatus("PENDING");
            
            // Save to database
            DetectionEventEntity saved = eventRepository.save(event);
            
            logger.info("Blue Flow event {} stored successfully with storage ref: {}", 
                saved.getEventId(), saved.getStorageRef());
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "INGESTED");
            response.put("eventId", saved.getEventId());
            response.put("databaseId", saved.getId());
            response.put("storageRef", saved.getStorageRef());
            response.put("timestamp", saved.getCreatedAt());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid device: {}", deviceId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (Exception e) {
            logger.error("Failed to ingest event from device {}", deviceId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Register or update event source (camera/device)
     */
    @PostMapping("/sources/register")
    public ResponseEntity<Map<String, Object>> registerEventSource(
            @RequestBody EventSourceRequest request,
            @RequestHeader(value = "Authorization", required = true) String authHeader) {
        
        logger.info("Registering event source: {}", request.getDeviceId());
        
        try {
            EventSource source = sourceRepository.findByDeviceId(request.getDeviceId())
                .orElse(new EventSource());
            
            source.setDeviceId(request.getDeviceId());
            source.setDeviceName(request.getDeviceName());
            source.setLocation(request.getLocation());
            source.setStatus("ACTIVE");
            source.setLastHeartbeat(Instant.now());
            
            EventSource saved = sourceRepository.save(source);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "REGISTERED");
            response.put("deviceId", saved.getDeviceId());
            response.put("databaseId", saved.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to register source {}", request.getDeviceId(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Request DTOs
     */
    public static class DetectionEventRequest {
        private String eventId;
        private String eventType;
        private Double confidence;
        private String storageRef;
        private Instant timestamp;
        private Integer duration;
        private BoundingBox bbox;
        private Map<String, Object> metadata;
        
        // Getters and setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
        
        public String getStorageRef() { return storageRef; }
        public void setStorageRef(String storageRef) { this.storageRef = storageRef; }
        
        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
        
        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }
        
        public BoundingBox getBbox() { return bbox; }
        public void setBbox(BoundingBox bbox) { this.bbox = bbox; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
    
    public static class BoundingBox {
        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
        
        public Integer getX() { return x; }
        public void setX(Integer x) { this.x = x; }
        
        public Integer getY() { return y; }
        public void setY(Integer y) { this.y = y; }
        
        public Integer getWidth() { return width; }
        public void setWidth(Integer width) { this.width = width; }
        
        public Integer getHeight() { return height; }
        public void setHeight(Integer height) { this.height = height; }
    }
    
    public static class EventSourceRequest {
        private String deviceId;
        private String deviceName;
        private String location;
        
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
}
