package backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generic API response wrapper for all endpoints
 * Provides consistent response format across the API
 */
public class ApiResponse<T> {
    
    private static final AtomicLong SEQUENCE_COUNTER = new AtomicLong(0);
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private T data;
    
    @JsonProperty("timestamp")
    private long timestamp;
    
    @JsonProperty("sequence")
    private long sequence;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
        this.sequence = SEQUENCE_COUNTER.incrementAndGet();
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.sequence = SEQUENCE_COUNTER.incrementAndGet();
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
        this.sequence = SEQUENCE_COUNTER.incrementAndGet();
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }
}
