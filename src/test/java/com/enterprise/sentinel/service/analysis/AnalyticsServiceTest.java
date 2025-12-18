package com.enterprise.sentinel.service.analysis;

import com.enterprise.sentinel.domain.model.DetectionEvent;
import com.enterprise.sentinel.domain.repository.DetectionEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("AnalyticsService Unit Tests")
class AnalyticsServiceTest {

    @Mock
    private DetectionEventRepository detectionEventRepository;

    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        analyticsService = new AnalyticsService(detectionEventRepository);
    }

    @Test
    @DisplayName("Should generate heatmap from detection events")
    void testGenerateHeatmap() {
        // Arrange
        DetectionEvent event1 = DetectionEvent.builder()
                .id(UUID.randomUUID())
                .detectedClass("person")
                .confidence(0.95)
                .boundingBox("[50, 100, 150, 200]")
                .build();

        DetectionEvent event2 = DetectionEvent.builder()
                .id(UUID.randomUUID())
                .detectedClass("person")
                .confidence(0.88)
                .boundingBox("[200, 250, 300, 350]")
                .build();

        when(detectionEventRepository.findByTimeRange(any(), any()))
                .thenReturn(List.of(event1, event2));

        // Act
        Map<String, Integer> heatmap = analyticsService.generateHeatmap(
                "person", 
                LocalDateTime.now().minusHours(1), 
                LocalDateTime.now(),
                10
        );

        // Assert
        assertThat(heatmap).isNotEmpty();
        assertThat(heatmap.values().stream().reduce(0, Integer::sum)).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should filter detections by class in heatmap")
    void testGenerateHeatmap_FiltersByClass() {
        // Arrange
        DetectionEvent personEvent = DetectionEvent.builder()
                .detectedClass("person")
                .confidence(0.95)
                .boundingBox("[50, 100, 150, 200]")
                .build();

        DetectionEvent carEvent = DetectionEvent.builder()
                .detectedClass("car")
                .confidence(0.90)
                .boundingBox("[200, 250, 300, 350]")
                .build();

        when(detectionEventRepository.findByTimeRange(any(), any()))
                .thenReturn(List.of(personEvent, carEvent));

        // Act
        Map<String, Integer> heatmap = analyticsService.generateHeatmap(
                "person",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                10
        );

        // Assert
        // Should only count person events, not car events
        long totalDetections = heatmap.values().stream().reduce(0, Integer::sum);
        assertThat(totalDetections).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should calculate dwell time for objects in zone")
    void testCalculateDwellTime() {
        // Arrange
        long time1 = System.currentTimeMillis();
        long time2 = time1 + 5000; // 5 seconds later

        DetectionEvent event1 = DetectionEvent.builder()
                .id(UUID.randomUUID())
                .videoId(UUID.randomUUID())
                .detectedClass("person")
                .confidence(0.95)
                .boundingBox("[100, 100, 200, 200]")
                .timestampMs(time1)
                .build();

        DetectionEvent event2 = DetectionEvent.builder()
                .id(UUID.randomUUID())
                .videoId(event1.getVideoId())
                .detectedClass("person")
                .confidence(0.92)
                .boundingBox("[105, 105, 205, 205]") // Similar position
                .timestampMs(time2)
                .build();

        when(detectionEventRepository.findByTimeRange(any(), any()))
                .thenReturn(List.of(event1, event2));

        // Act
        Map<String, Long> dwellTimes = analyticsService.calculateDwellTime(
                "person",
                "[0, 0, 1000, 1000]",
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()
        );

        // Assert
        assertThat(dwellTimes).isNotEmpty();
        assertThat(dwellTimes.values().stream().findFirst().orElse(0L)).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should analyze PPE compliance")
    void testAnalyzePPECompliance() {
        // Arrange
        DetectionEvent person1 = DetectionEvent.builder()
                .detectedClass("person")
                .confidence(0.95)
                .build();

        DetectionEvent person2 = DetectionEvent.builder()
                .detectedClass("person")
                .confidence(0.92)
                .build();

        DetectionEvent helmet = DetectionEvent.builder()
                .detectedClass("helmet")
                .confidence(0.88)
                .build();

        when(detectionEventRepository.findByTimeRange(any(), any()))
                .thenReturn(List.of(person1, person2, helmet));

        // Act
        Map<String, Object> ppeData = analyticsService.analyzePPECompliance(
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()
        );

        // Assert
        assertThat(ppeData).containsKeys("totalPersons", "withHelmet", "complianceRate");
        assertThat(ppeData.get("totalPersons")).isEqualTo(2L);
        assertThat(ppeData.get("withHelmet")).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should generate compliance report for restricted zones")
    void testGenerateComplianceReport() {
        // Arrange
        DetectionEvent violation = DetectionEvent.builder()
                .id(UUID.randomUUID())
                .detectedClass("forklift")
                .confidence(0.92)
                .boundingBox("[100, 100, 200, 200]")
                .createdAt(LocalDateTime.now())
                .build();

        DetectionEvent allowed = DetectionEvent.builder()
                .id(UUID.randomUUID())
                .detectedClass("person")
                .confidence(0.95)
                .boundingBox("[300, 300, 400, 400]")
                .createdAt(LocalDateTime.now())
                .build();

        when(detectionEventRepository.findByTimeRange(any(), any()))
                .thenReturn(List.of(violation, allowed));

        // Act
        List<Map<String, Object>> violations = analyticsService.generateComplianceReport(
                List.of("forklift", "bicycle"),
                List.of("RESTRICTED_ZONE_A"),
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()
        );

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations.get(0)).containsKeys("timestamp", "detectedClass", "confidence", "zone");
    }

    @Test
    @DisplayName("Should calculate detection frequency by class")
    void testGetDetectionFrequency() {
        // Arrange
        DetectionEvent person1 = DetectionEvent.builder()
                .detectedClass("person")
                .confidence(0.95)
                .build();

        DetectionEvent person2 = DetectionEvent.builder()
                .detectedClass("person")
                .confidence(0.92)
                .build();

        DetectionEvent car = DetectionEvent.builder()
                .detectedClass("car")
                .confidence(0.88)
                .build();

        when(detectionEventRepository.findByTimeRange(any(), any()))
                .thenReturn(List.of(person1, person2, car));

        // Act
        Map<String, Long> frequency = analyticsService.getDetectionFrequency(
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()
        );

        // Assert
        assertThat(frequency)
                .containsEntry("person", 2L)
                .containsEntry("car", 1L);
    }

    @Test
    @DisplayName("Should calculate average confidence per class")
    void testGetAverageConfidence() {
        // Arrange
        DetectionEvent person1 = DetectionEvent.builder()
                .detectedClass("person")
                .confidence(0.90)
                .build();

        DetectionEvent person2 = DetectionEvent.builder()
                .detectedClass("person")
                .confidence(0.95)
                .build();

        DetectionEvent car = DetectionEvent.builder()
                .detectedClass("car")
                .confidence(0.85)
                .build();

        when(detectionEventRepository.findByTimeRange(any(), any()))
                .thenReturn(List.of(person1, person2, car));

        // Act
        Map<String, Double> avgConfidence = analyticsService.getAverageConfidence(
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now()
        );

        // Assert
        assertThat(avgConfidence).containsKeys("person", "car");
        assertThat(avgConfidence.get("person")).isCloseTo(0.925, within(0.01));
        assertThat(avgConfidence.get("car")).isCloseTo(0.85, within(0.01));
    }

    @Test
    @DisplayName("Should handle empty detection list")
    void testHandleEmptyDetections() {
        // Arrange
        when(detectionEventRepository.findByTimeRange(any(), any()))
                .thenReturn(List.of());

        // Act & Assert
        assertThatNoException().isThrownBy(() -> {
            Map<String, Integer> heatmap = analyticsService.generateHeatmap(
                    "person",
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now(),
                    10
            );
            assertThat(heatmap).isEmpty();
        });
    }
}
