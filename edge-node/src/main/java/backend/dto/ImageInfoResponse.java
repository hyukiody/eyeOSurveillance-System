package backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for image information endpoints
 */
public class ImageInfoResponse {
    
    @JsonProperty("image_id")
    private String imageId;
    
    @JsonProperty("filename")
    private String filename;
    
    @JsonProperty("format")
    private String format;
    
    @JsonProperty("width")
    private int width;
    
    @JsonProperty("height")
    private int height;
    
    @JsonProperty("size_bytes")
    private long sizeBytes;
    
    @JsonProperty("created_at")
    private String createdAt;

    public ImageInfoResponse() {
    }

    public ImageInfoResponse(String imageId, String filename, String format, int width, int height, long sizeBytes) {
        this.imageId = imageId;
        this.filename = filename;
        this.format = format;
        this.width = width;
        this.height = height;
        this.sizeBytes = sizeBytes;
        this.createdAt = new java.util.Date().toString();
    }

    // Getters and Setters
    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
