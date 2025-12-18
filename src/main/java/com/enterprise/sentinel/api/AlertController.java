package com.enterprise.sentinel.api;

import com.enterprise.sentinel.domain.model.SecurityAlert;
import com.enterprise.sentinel.domain.repository.SecurityAlertRepository;
import com.enterprise.sentinel.service.analysis.AlertEngine;
import com.enterprise.sentinel.service.analysis.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API for security alerts and analytics.
 * Provides endpoints for:
 * - Real-time alert monitoring
 * - Alert acknowledgment
 * - Analytics queries (heatmaps, dwell time, compliance)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertEngine alertEngine;
    private final AnalyticsService analyticsService;
    private final SecurityAlertRepository securityAlertRepository;

    // ====== ALERT ENDPOINTS ======

    /**
     * Get all unacknowledged alerts (paginated).
     * Query params:
     * - page: 0-indexed page number (default: 0)
     * - size: page size (default: 20, max: 100)
     */
    @GetMapping("/unacknowledged")
    public ResponseEntity<Page<SecurityAlert>> getUnacknowledgedAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<SecurityAlert> alerts = securityAlertRepository.findByAcknowledgedFalse(pageable);
        
        log.info("Retrieved {} unacknowledged alerts, page {}", alerts.getNumberOfElements(), page);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get all alerts for a specific geofence zone.
     */
    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<SecurityAlert>> getAlertsByZone(@PathVariable UUID zoneId) {
        List<SecurityAlert> alerts = securityAlertRepository
                .findByGeofenceZoneIdOrderByCreatedAtDesc(zoneId);
        
        log.info("Retrieved {} alerts for zone {}", alerts.size(), zoneId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get alerts by severity level.
     * Severity: CRITICAL, HIGH, MEDIUM, LOW
     */
    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<SecurityAlert>> getAlertsBySeverity(@PathVariable String severity) {
        List<SecurityAlert> alerts = securityAlertRepository
                .findBySeverityOrderByCreatedAtDesc(severity);
        
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get alerts in a specific time range.
     */
    @GetMapping("/timerange")
    public ResponseEntity<List<SecurityAlert>> getAlertsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        List<SecurityAlert> alerts = securityAlertRepository.findByTimeRange(start, end);
        log.info("Retrieved {} alerts between {} and {}", alerts.size(), start, end);
        
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get count of unacknowledged alerts for a zone.
     * Used for real-time indicators in UI.
     */
    @GetMapping("/zone/{zoneCode}/unacknowledged-count")
    public ResponseEntity<Map<String, Long>> getUnacknowledgedCountByZone(@PathVariable String zoneCode) {
        long count = alertEngine.getUnacknowledgedAlertCount(zoneCode);
        return ResponseEntity.ok(Map.of("unacknowledgedCount", count));
    }

    /**
     * Get critical alerts requiring immediate attention.
     */
    @GetMapping("/critical")
    public ResponseEntity<List<SecurityAlert>> getCriticalAlerts() {
        List<SecurityAlert> critical = alertEngine.getCriticalUnacknowledgedAlerts();
        return ResponseEntity.ok(critical);
    }

    /**
     * Acknowledge an alert.
     * POST body: { "username": "admin", "notes": "Incident handled" }
     */
    @PostMapping("/{alertId}/acknowledge")
    public ResponseEntity<?> acknowledgeAlert(
            @PathVariable String alertId,
            @RequestParam String username) {
        
        try {
            alertEngine.acknowledgeAlert(alertId, username);
            log.info("Alert {} acknowledged by {}", alertId, username);
            return ResponseEntity.ok(Map.of("status", "acknowledged"));
        } catch (Exception e) {
            log.error("Failed to acknowledge alert {}: {}", alertId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Alert not found"));
        }
    }

    /**
     * Delete an alert.
     */
    @DeleteMapping("/{alertId}")
    public ResponseEntity<?> deleteAlert(@PathVariable UUID alertId) {
        try {
            securityAlertRepository.deleteById(alertId);
            log.info("Alert {} deleted", alertId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Alert not found"));
        }
    }

    // ====== ANALYTICS ENDPOINTS ======

    /**
     * Generate heatmap for a detection class.
     * Query params:
     * - class: object class (e.g., "person", "car")
     * - start: start time (ISO format)
     * - end: end time (ISO format)
     * - gridSize: grid cells per dimension (default: 10)
     */
    @GetMapping("/analytics/heatmap")
    public ResponseEntity<Map<String, Integer>> getHeatmap(
            @RequestParam String objectClass,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "10") int gridSize) {
        
        Map<String, Integer> heatmap = analyticsService.generateHeatmap(
                objectClass, start, end, gridSize);
        
        log.info("Generated heatmap for {} with {} cells", objectClass, heatmap.size());
        return ResponseEntity.ok(heatmap);
    }

    /**
     * Calculate dwell time for objects in a zone.
     */
    @GetMapping("/analytics/dwell-time")
    public ResponseEntity<Map<String, Long>> getDwellTime(
            @RequestParam String objectClass,
            @RequestParam String zoneCoordinates,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        Map<String, Long> dwellTimes = analyticsService.calculateDwellTime(
                objectClass, zoneCoordinates, start, end);
        
        return ResponseEntity.ok(dwellTimes);
    }

    /**
     * Analyze PPE compliance.
     */
    @GetMapping("/analytics/ppe-compliance")
    public ResponseEntity<Map<String, Object>> getPPECompliance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        Map<String, Object> ppeData = analyticsService.analyzePPECompliance(start, end);
        return ResponseEntity.ok(ppeData);
    }

    /**
     * Generate compliance report for restricted zones.
     */
    @PostMapping("/analytics/compliance-report")
    public ResponseEntity<List<Map<String, Object>>> getComplianceReport(
            @RequestBody ComplianceReportRequest request) {
        
        List<Map<String, Object>> violations = analyticsService.generateComplianceReport(
                request.getRestrictedClasses(),
                request.getRestrictedZones(),
                request.getStartTime(),
                request.getEndTime()
        );
        
        log.info("Generated compliance report: {} violations found", violations.size());
        return ResponseEntity.ok(violations);
    }

    /**
     * Get detection frequency by class.
     */
    @GetMapping("/analytics/frequency")
    public ResponseEntity<Map<String, Long>> getDetectionFrequency(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        Map<String, Long> frequency = analyticsService.getDetectionFrequency(start, end);
        return ResponseEntity.ok(frequency);
    }

    /**
     * Get average confidence per detection class.
     */
    @GetMapping("/analytics/confidence")
    public ResponseEntity<Map<String, Double>> getAverageConfidence(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        Map<String, Double> confidence = analyticsService.getAverageConfidence(start, end);
        return ResponseEntity.ok(confidence);
    }

    /**
     * Request body for compliance report.
     */
    public record ComplianceReportRequest(
            List<String> restrictedClasses,
            List<String> restrictedZones,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {}
}
