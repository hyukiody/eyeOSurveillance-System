package com.teraapi.stream;

/**
 * Blue Flow event - metadata from Edge Node detection
 */
public class DetectionEvent {
    private final String cameraId;
    private final String objectType;
    private final double confidence;
    private final long timestamp;
    private final String zone;

    public DetectionEvent(String cameraId, String objectType, double confidence, long timestamp, String zone) {
        this.cameraId = cameraId;
        this.objectType = objectType;
        this.confidence = confidence;
        this.timestamp = timestamp;
        this.zone = zone;
    }

    public String getCameraId() {
        return cameraId;
    }

    public String getObjectType() {
        return objectType;
    }

    public double getConfidence() {
        return confidence;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getZone() {
        return zone;
    }

    @Override
    public String toString() {
        return String.format("DetectionEvent{camera=%s, type=%s, conf=%.2f, zone=%s, ts=%d}",
                cameraId, objectType, confidence, zone, timestamp);
    }
}
