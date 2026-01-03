package com.eyeo.edge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * FLUXO AZUL (Blue Flow): Intelligence & Metadata Service
 * 
 * Responsabilidades:
 * - Processar eventos de detecção de IA (YOLO)
 * - Enviar metadados ao Middleware para indexação
 * - Sanitizar dados sensíveis
 */
@Service
public class EventIngestionService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventIngestionService.class);
    
    @Value("${middleware.url}")
    private String middlewareUrl;
    
    @Value("${ai.confidence.threshold:0.5}")
    private double confidenceThreshold;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public EventIngestionService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Envia evento de detecção ao Middleware
     * 
     * @param cameraId ID da câmera
     * @param detectedClass Classe detectada (person, car, etc)
     * @param confidence Nível de confiança (0.0 - 1.0)
     * @param storageRefKey Chave do blob de vídeo no storage
     * @return true se enviado com sucesso
     */
    public boolean sendDetectionEvent(
            String cameraId,
            String detectedClass,
            double confidence,
            String storageRefKey
    ) {
        return sendDetectionEvent(cameraId, detectedClass, confidence, storageRefKey, null);
    }
    
    /**
     * Envia evento de detecção com metadados adicionais
     */
    public boolean sendDetectionEvent(
            String cameraId,
            String detectedClass,
            double confidence,
            String storageRefKey,
            Map<String, Object> additionalMetadata
    ) {
        // Validação de confiança mínima
        if (confidence < confidenceThreshold) {
            logger.debug("🔵 Evento ignorado - Confiança {} abaixo do threshold {}", 
                        confidence, confidenceThreshold);
            return false;
        }
        
        logger.info("🔵 FLUXO AZUL: Engatilhando evento - Class: {}, Confidence: {:.2f}", 
                   detectedClass, confidence);
        
        try {
            // Monta payload JSON
            Map<String, Object> payload = new HashMap<>();
            payload.put("camera_id", cameraId);
            payload.put("timestamp", Instant.now().toString());
            payload.put("detected_class", detectedClass);
            payload.put("confidence", confidence);
            payload.put("storage_ref_key", storageRefKey);
            
            // Adiciona metadados se fornecidos
            if (additionalMetadata != null && !additionalMetadata.isEmpty()) {
                payload.put("metadata", sanitizeMetadata(additionalMetadata));
            }
            
            // Envia ao Middleware
            String url = middlewareUrl + "/api/ingest";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("✓ Evento enviado ao Middleware com sucesso");
                return true;
            } else {
                logger.warn("⚠️ Middleware retornou status: {}", response.getStatusCode());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("❌ Erro ao enviar evento ao Middleware", e);
            return false;
        }
    }
    
    /**
     * Sanitiza metadados removendo informações sensíveis demais
     */
    private Map<String, Object> sanitizeMetadata(Map<String, Object> metadata) {
        Map<String, Object> sanitized = new HashMap<>(metadata);
        
        // Remove coordenadas GPS exatas (se existirem)
        if (sanitized.containsKey("gps_lat")) {
            double lat = (double) sanitized.get("gps_lat");
            sanitized.put("gps_lat_region", truncateCoordinate(lat, 2)); // 2 decimais ~1km
            sanitized.remove("gps_lat");
        }
        
        if (sanitized.containsKey("gps_lon")) {
            double lon = (double) sanitized.get("gps_lon");
            sanitized.put("gps_lon_region", truncateCoordinate(lon, 2));
            sanitized.remove("gps_lon");
        }
        
        // Remove endereços IP se presentes
        sanitized.remove("client_ip");
        sanitized.remove("device_ip");
        
        return sanitized;
    }
    
    /**
     * Trunca coordenada para reduzir precisão
     */
    private double truncateCoordinate(double coord, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(coord * factor) / factor;
    }
    
    /**
     * Envia batch de eventos (otimização)
     */
    public int sendEventBatch(DetectionEvent[] events) {
        int successCount = 0;
        
        for (DetectionEvent event : events) {
            boolean sent = sendDetectionEvent(
                event.cameraId,
                event.detectedClass,
                event.confidence,
                event.storageRefKey,
                event.metadata
            );
            
            if (sent) successCount++;
        }
        
        logger.info("📊 Batch enviado: {}/{} eventos com sucesso", successCount, events.length);
        return successCount;
    }
    
    /**
     * Inner class para representar evento de detecção
     */
    public static class DetectionEvent {
        public String cameraId;
        public String detectedClass;
        public double confidence;
        public String storageRefKey;
        public Map<String, Object> metadata;
        
        public DetectionEvent(String cameraId, String detectedClass, double confidence, 
                            String storageRefKey) {
            this.cameraId = cameraId;
            this.detectedClass = detectedClass;
            this.confidence = confidence;
            this.storageRefKey = storageRefKey;
        }
    }
}
