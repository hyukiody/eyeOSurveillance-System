package backend.video;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

/**
 * SecureVideoStreamClient - Phase 2: Red Flow Implementation
 * 
 * Responsible for streaming video from edge cameras to the secure-io-engine (data-core)
 * using HTTP chunked transfer encoding. This is the entry point for the Red Flow where
 * video content is sent for processing before blind storage.
 * 
 * Architecture: CaCTUs Zero-Trust Model
 * - Client-side video capture and streaming
 * - Chunked transfer encoding (64KB chunks)
 * - Server-side processing in data-core
 * - Blind storage (edge-node never sees processed content)
 * 
 * Security: 
 * - HTTPS only in production
 * - Device authentication via JWT token
 * - No local video storage (streaming only)
 */
@Component
public class SecureVideoStreamClient {
    
    private static final Logger logger = LoggerFactory.getLogger(SecureVideoStreamClient.class);
    
    private static final int CHUNK_SIZE = 64 * 1024; // 64KB chunks per CaCTUs spec
    private static final int CONNECTION_TIMEOUT_SECONDS = 30;
    private static final int REQUEST_TIMEOUT_MINUTES = 10; // For long video streams
    
    @Value("${eyeo.data-core.url:http://localhost:9090}")
    private String dataCoreUrl;
    
    @Value("${eyeo.device.id}")
    private String deviceId;
    
    @Value("${eyeo.device.token}")
    private String deviceToken;
    
    private final HttpClient httpClient;
    
    public SecureVideoStreamClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT_SECONDS))
            .build();
    }
    
    /**
     * Stream video to secure-io-engine for processing and blind storage
     * 
     * @param videoStream Input stream from camera/video source
     * @param metadata Video metadata (camera ID, timestamp, detection info)
     * @return Storage reference for the processed video
     * @throws IOException If streaming fails
     */
    public VideoStreamResponse streamVideo(InputStream videoStream, VideoMetadata metadata) throws IOException {
        String streamId = UUID.randomUUID().toString();
        logger.info("Starting video stream {} from device {} to data-core", streamId, deviceId);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(dataCoreUrl + "/api/v1/video/stream"))
                .timeout(Duration.ofMinutes(REQUEST_TIMEOUT_MINUTES))
                .header("Content-Type", "application/octet-stream")
                .header("Authorization", "Bearer " + deviceToken)
                .header("X-Device-ID", deviceId)
                .header("X-Stream-ID", streamId)
                .header("X-Camera-ID", metadata.getCameraId())
                .header("X-Timestamp", metadata.getTimestamp().toString())
                .header("X-Duration", String.valueOf(metadata.getDurationSeconds()))
                .header("Transfer-Encoding", "chunked")
                .POST(HttpRequest.BodyPublishers.ofInputStream(() -> videoStream))
                .build();
            
            HttpResponse<String> response = httpClient.send(
                request, 
                HttpResponse.BodyHandlers.ofString()
            );
            
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                logger.info("Video stream {} completed successfully. Response: {}", 
                    streamId, response.body());
                return parseResponse(response.body(), streamId);
            } else {
                logger.error("Video stream {} failed with status {}: {}", 
                    streamId, response.statusCode(), response.body());
                throw new IOException("Stream failed with HTTP " + response.statusCode());
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Video stream {} interrupted", streamId, e);
            throw new IOException("Stream interrupted", e);
        }
    }
    
    /**
     * Parse data-core response containing processed storage reference
     */
    private VideoStreamResponse parseResponse(String responseBody, String streamId) {
        // Simple JSON parsing (in production use Jackson/Gson)
        // Expected format: {"storageRef": "processed/2026/01/02/uuid.dat", "status": "PROCESSED"}
        VideoStreamResponse response = new VideoStreamResponse();
        response.setStreamId(streamId);
        
        if (responseBody.contains("storageRef")) {
            String storageRef = extractJsonValue(responseBody, "storageRef");
            response.setStorageReference(storageRef);
            response.setStatus("SUCCESS");
            logger.info("Received storage reference: {}", storageRef);
        } else {
            response.setStatus("FAILED");
            logger.warn("No storage reference in response for stream {}", streamId);
        }
        
        return response;
    }
    
    /**
     * Simple JSON value extraction (replace with proper JSON library)
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return null;
        
        startIndex += searchKey.length();
        int endIndex = json.indexOf("\"", startIndex);
        
        return json.substring(startIndex, endIndex);
    }
    
    /**
     * Video metadata container
     */
    public static class VideoMetadata {
        private String cameraId;
        private java.time.Instant timestamp;
        private int durationSeconds;
        private String eventType;
        private Double confidence;
        
        public VideoMetadata(String cameraId, java.time.Instant timestamp, int durationSeconds) {
            this.cameraId = cameraId;
            this.timestamp = timestamp;
            this.durationSeconds = durationSeconds;
        }
        
        // Getters and setters
        public String getCameraId() { return cameraId; }
        public void setCameraId(String cameraId) { this.cameraId = cameraId; }
        
        public java.time.Instant getTimestamp() { return timestamp; }
        public void setTimestamp(java.time.Instant timestamp) { this.timestamp = timestamp; }
        
        public int getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public Double getConfidence() { return confidence; }
        public void setConfidence(Double confidence) { this.confidence = confidence; }
    }
    
    /**
     * Response from data-core after video processing
     */
    public static class VideoStreamResponse {
        private String streamId;
        private String storageReference;
        private String status;
        private String errorMessage;
        
        // Getters and setters
        public String getStreamId() { return streamId; }
        public void setStreamId(String streamId) { this.streamId = streamId; }
        
        public String getStorageReference() { return storageReference; }
        public void setStorageReference(String storageReference) { 
            this.storageReference = storageReference; 
        }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
