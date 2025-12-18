package com.enterprise.sentinel.service.analysis;

import com.enterprise.sentinel.domain.model.DetectionEvent;
import com.enterprise.sentinel.domain.repository.DetectionEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AnalyticsService computes statistics and insights from detection events.
 * 
 * Capabilities:
 * 1. Heatmaps: Spatial distribution of detections in zones
 * 2. Dwell Time: Time spent by objects in specific zones
 * 3. PPE Detection: Presence/absence of protective equipment
 * 4. Compliance Reports: Objects in restricted zones
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final DetectionEventRepository detectionEventRepository;

    /**
     * Generate heatmap data for a detection class in a time window.
     * Aggregates detection coordinates into spatial grid cells.
     * 
     * @param detectedClass The object class to analyze (e.g., "person")
     * @param startTime Start of time window
     * @param endTime End of time window
     * @param gridSize Number of cells per dimension (e.g., 10x10 grid)
     * @return Map of grid cell coordinates to detection count
     */
    public Map<String, Integer> generateHeatmap(String detectedClass, 
                                                 LocalDateTime startTime, 
                                                 LocalDateTime endTime, 
                                                 int gridSize) {
        List<DetectionEvent> events = detectionEventRepository
                .findByTimeRange(startTime, endTime);

        Map<String, Integer> heatmap = new HashMap<>();

        events.stream()
                .filter(e -> detectedClass.equalsIgnoreCase(e.getDetectedClass()))
                .filter(e -> e.getConfidence() > 0.6) // Only high-confidence detections
                .forEach(event -> {
                    String gridCell = extractGridCell(event, gridSize);
                    heatmap.merge(gridCell, 1, Integer::sum);
                });

        log.info("Generated heatmap for {}: {} cells with data", detectedClass, heatmap.size());
        return heatmap;
    }

    /**
     * Calculate dwell time for objects in a specific zone.
     * Dwell time = time from first detection to last detection in zone.
     * 
     * @param detectedClass The object class to track
     * @param zoneCoordinates Bounding box of the zone
     * @param startTime Start of analysis period
     * @param endTime End of analysis period
     * @return Map of object track IDs to dwell time in milliseconds
     */
    public Map<String, Long> calculateDwellTime(String detectedClass,
                                               String zoneCoordinates,
                                               LocalDateTime startTime,
                                               LocalDateTime endTime) {
        List<DetectionEvent> events = detectionEventRepository
                .findByTimeRange(startTime, endTime);

        Map<String, Long> dwellTimes = new HashMap<>();

        // Group detections by track (simplified: group by similar coordinates over time)
        Map<String, List<Long>> trackTimestamps = new HashMap<>();

        events.stream()
                .filter(e -> detectedClass.equalsIgnoreCase(e.getDetectedClass()))
                .filter(e -> isInZone(e, zoneCoordinates))
                .forEach(event -> {
                    String trackId = generateTrackId(event, detectedClass);
                    trackTimestamps.computeIfAbsent(trackId, k -> new ArrayList<>())
                            .add(event.getTimestampMs());
                });

        // Calculate dwell time per track
        trackTimestamps.forEach((trackId, timestamps) -> {
            if (!timestamps.isEmpty()) {
                long dwell = Collections.max(timestamps) - Collections.min(timestamps);
                dwellTimes.put(trackId, dwell);
            }
        });

        log.info("Calculated dwell times for {}: {} tracks analyzed", detectedClass, trackTimestamps.size());
        return dwellTimes;
    }

    /**
     * Detect PPE (Personal Protective Equipment) compliance.
     * Analyzes if persons detected are wearing helmets, vests, etc.
     * 
     * @param startTime Start of analysis period
     * @param endTime End of analysis period
     * @return PPE statistics: total persons, with helmet, with vest, compliance %
     */
    public Map<String, Object> analyzePPECompliance(LocalDateTime startTime, LocalDateTime endTime) {
        List<DetectionEvent> events = detectionEventRepository
                .findByTimeRange(startTime, endTime);

        long totalPersons = events.stream()
                .filter(e -> "person".equalsIgnoreCase(e.getDetectedClass()))
                .count();

        // Future: Use a separate PPE detection model or multi-class detection
        // For now, placeholder logic
        long withHelmet = events.stream()
                .filter(e -> "helmet".equalsIgnoreCase(e.getDetectedClass()))
                .count();

        long withVest = events.stream()
                .filter(e -> "safety_vest".equalsIgnoreCase(e.getDetectedClass()))
                .count();

        double complianceRate = totalPersons > 0 
                ? (double) withHelmet / totalPersons * 100 
                : 0;

        Map<String, Object> ppeData = new HashMap<>();
        ppeData.put("totalPersons", totalPersons);
        ppeData.put("withHelmet", withHelmet);
        ppeData.put("withVest", withVest);
        ppeData.put("complianceRate", complianceRate);

        log.info("PPE analysis: {} persons, {} with helmet, {:.1f}% compliance",
                totalPersons, withHelmet, complianceRate);

        return ppeData;
    }

    /**
     * Generate compliance report for restricted zones.
     * Lists all detections of restricted classes in restricted zones.
     * 
     * @param restrictedClasses Classes not allowed in zones (e.g., "forklift", "bicycle")
     * @param restrictedZones Zones where these classes are prohibited
     * @param startTime Start of period
     * @param endTime End of period
     * @return Violations: timestamp, class, zone, confidence
     */
    public List<Map<String, Object>> generateComplianceReport(
            List<String> restrictedClasses,
            List<String> restrictedZones,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        List<DetectionEvent> events = detectionEventRepository
                .findByTimeRange(startTime, endTime);

        return events.stream()
                .filter(e -> restrictedClasses.contains(e.getDetectedClass()))
                .filter(e -> isInRestrictedZone(e, restrictedZones))
                .map(e -> {
                    Map<String, Object> violation = new HashMap<>();
                    violation.put("timestamp", e.getCreatedAt());
                    violation.put("detectedClass", e.getDetectedClass());
                    violation.put("confidence", e.getConfidence());
                    violation.put("zone", identifyZone(e, restrictedZones));
                    return violation;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get detection frequency statistics by class.
     */
    public Map<String, Long> getDetectionFrequency(LocalDateTime startTime, LocalDateTime endTime) {
        List<DetectionEvent> events = detectionEventRepository
                .findByTimeRange(startTime, endTime);

        return events.stream()
                .collect(Collectors.groupingBy(
                        DetectionEvent::getDetectedClass,
                        Collectors.counting()
                ));
    }

    /**
     * Get average confidence per detection class.
     */
    public Map<String, Double> getAverageConfidence(LocalDateTime startTime, LocalDateTime endTime) {
        List<DetectionEvent> events = detectionEventRepository
                .findByTimeRange(startTime, endTime);

        return events.stream()
                .collect(Collectors.groupingBy(
                        DetectionEvent::getDetectedClass,
                        Collectors.averagingDouble(DetectionEvent::getConfidence)
                ));
    }

    // ====== HELPER METHODS ======

    private String extractGridCell(DetectionEvent event, int gridSize) {
        // Parse bounding box from boundingBox or inferenceData
        // Simplified: extract center coordinates and map to grid
        try {
            String bbox = event.getBoundingBox();
            // Parse format: "[x1, y1, x2, y2]"
            String[] coords = bbox.replaceAll("[\\[\\]]", "").split(",");
            
            if (coords.length >= 2) {
                int x = Integer.parseInt(coords[0].trim()) / 100;
                int y = Integer.parseInt(coords[1].trim()) / 100;
                
                int cellX = (x / gridSize) * gridSize;
                int cellY = (y / gridSize) * gridSize;
                
                return cellX + "_" + cellY;
            }
        } catch (Exception e) {
            log.debug("Failed to extract grid cell from bounding box: {}", event.getBoundingBox());
        }
        return "0_0";
    }

    private String generateTrackId(DetectionEvent event, String className) {
        // Simplified tracking: use video + approximate spatial position
        String videoId = event.getVideoId() != null ? event.getVideoId().toString() : "unknown";
        String position = event.getBoundingBox() != null 
                ? event.getBoundingBox().substring(0, Math.min(10, event.getBoundingBox().length())) 
                : "0";
        return videoId + "_" + position + "_" + className;
    }

    private boolean isInZone(DetectionEvent event, String zoneCoordinates) {
        // Simplified: check if detection bounding box overlaps with zone
        // In production: implement proper rectangle intersection
        return event.getBoundingBox() != null && zoneCoordinates != null;
    }

    private boolean isInRestrictedZone(DetectionEvent event, List<String> restrictedZones) {
        // Simplified: check if detection is in any restricted zone
        return event.getBoundingBox() != null && !restrictedZones.isEmpty();
    }

    private String identifyZone(DetectionEvent event, List<String> zones) {
        return !zones.isEmpty() ? zones.get(0) : "unknown";
    }
}
