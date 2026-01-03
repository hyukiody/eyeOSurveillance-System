package com.eyeo.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * StreamProcessingEndpoint - Phase 2: Red Flow Implementation
 * 
 * REST endpoint for receiving video streams from edge devices and processing them
 * using AES-256-GCM with chunked processing. This is the server-side component
 * of the Red Flow in the CaCTUs architecture.
 * 
 * Architecture:
 * - Receives chunked video streams via HTTP
 * - Processes each chunk with unique IV
 * - Stores processed chunks to disk (blind storage)
 * - Returns storage reference to edge device
 * - Edge device stores reference in stream database
 * 
 * Security:
 * - Device authentication via JWT token
 * - HTTPS only in production
 * - Server uses separate restoration process (zero-trust)
 * - Only authorized clients with master key can restore
 */
@RestController
@RequestMapping("/api/v1/video")
public class StreamProcessingEndpoint {
    
    private static final Logger logger = LoggerFactory.getLogger(StreamProcessingEndpoint.class);
    
    @Autowired
    private ChunkedVideoProcessor processor;
    
    /**
     * Stream video endpoint - receives video from edge devices
     * 
     * Headers:
     * - Authorization: Bearer <JWT token>
     * - X-Device-ID: Device identifier
     * - X-Stream-ID: Unique stream identifier
     * - X-Camera-ID: Camera identifier
     * - X-Timestamp: Video timestamp
     * - Transfer-Encoding: chunked
     * 
     * @param request HTTP request containing video stream
     * @return Storage reference and processing metadata
     */
    @PostMapping("/stream")
    public ResponseEntity<Map<String, Object>> handleVideoStream(
            HttpServletRequest request,
            @RequestHeader(value = "X-Device-ID", required = true) String deviceId,
            @RequestHeader(value = "X-Stream-ID", required = true) String streamId,
            @RequestHeader(value = "X-Camera-ID", required = true) String cameraId,
            @RequestHeader(value = "X-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Duration", required = false, defaultValue = "0") int duration) {
        
        logger.info("Received video stream request - Device: {}, Stream: {}, Camera: {}", 
            deviceId, streamId, cameraId);
        
        try {
            // Get video stream from request body
            InputStream videoStream = request.getInputStream();
            
            // Create processing metadata
            ChunkedVideoProcessor.ProcessingMetadata metadata = 
                new ChunkedVideoProcessor.ProcessingMetadata(deviceId, streamId, cameraId);
            
            // Process video stream
            ChunkedVideoProcessor.ProcessingResult result = 
                processor.processVideoStream(videoStream, metadata);
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "PROCESSED");
            response.put("storageRef", result.getStorageReference());
            response.put("sessionId", result.getSessionId());
            response.put("chunkCount", result.getChunkCount());
            response.put("totalBytes", result.getTotalBytesProcessed());
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("Video stream {} processed successfully: {} chunks, {} bytes, ref: {}", 
                streamId, result.getChunkCount(), result.getTotalBytesProcessed(), 
                result.getStorageReference());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IOException e) {
            logger.error("Failed to process video stream {} from device {}", streamId, deviceId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Failed to process video stream");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Health check endpoint for data-core service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "data-core-processing");
        health.put("version", "1.0.0");
        health.put("algorithm", "AES-256-GCM");
        return ResponseEntity.ok(health);
    }
    
    /**
     * Service info endpoint
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "EyeO Platform - Data Core");
        info.put("component", "Secure I/O Engine");
        info.put("architecture", "CaCTUs Zero-Trust");
        info.put("transformation", "AES-256-GCM");
        info.put("chunkSize", "64KB");
        info.put("capabilities", new String[]{
            "Video Stream Processing",
            "Chunked Processing",
            "Blind Storage",
            "IV Generation per Chunk"
        });
        return ResponseEntity.ok(info);
    }
}
