package backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Base64;

/**
 * Response DTO for image inversion operations
 * Returns processed image data in multiple formats
 * Similar to helloworld-rs HelloWorldResponse
 */
public class ImageInvertResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("image_base64")
    private String imageBase64;
    
    @JsonProperty("format")
    private String format;
    
    @JsonProperty("size_bytes")
    private long sizeBytes;
    
    @JsonProperty("processing_time_ms")
    private long processingTimeMs;

    public ImageInvertResponse() {
    }

    public ImageInvertResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public ImageInvertResponse(String status, String message, byte[] imageData, String format, long processingTimeMs) {
        this.status = status;
        this.message = message;
        this.imageBase64 = Base64.getEncoder().encodeToString(imageData);
        this.format = format;
        this.sizeBytes = imageData.length;
        this.processingTimeMs = processingTimeMs;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}
