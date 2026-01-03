package com.eyeo.data.controller;

import com.eyeo.data.service.SecureStateIOService;
import com.eyeo.data.service.SecureStateIOService.EncryptionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stream Processing Controller
 * 
 * Endpoints:
 * - POST /stream/encrypt : Recebe stream de vídeo e processa
 * - GET /storage/{key} : Serve blob processado
 * - POST /storage/decrypt : Decodifica e retorna stream
 */
@RestController
@RequestMapping
public class StreamController {
    
    private static final Logger logger = LoggerFactory.getLogger(StreamController.class);
    
    @Autowired
    private SecureStateIOService secureStateIO;
    
    /**
     * FLUXO VERMELHO: Endpoint de processamento de stream
     * 
     * Recebe stream de vídeo via HTTP chunked e retorna storage key
     */
    @PostMapping("/stream/encrypt")
    public ResponseEntity<Map<String, Object>> encryptVideoStream(
            HttpServletRequest request,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
            @RequestHeader(value = "X-Camera-ID", required = false) String cameraId
    ) {
        logger.info("📥 Recebendo stream criptografado");
        logger.info("  - Session ID: {}", sessionId);
        logger.info("  - Camera ID: {}", cameraId);
        logger.info("  - Content-Type: {}", request.getContentType());
        
        try {
            // Gera storage key único
            String storageKey = generateStorageKey(sessionId, cameraId);
            
            // Criptografa stream
            InputStream inputStream = request.getInputStream();
            EncryptionResult result = secureStateIO.encryptStream(inputStream, storageKey);
            
            // Retorna resposta JSON
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("storageKey", result.storageKey);
            response.put("storage_key", result.storageKey); // Alias para compatibilidade
            response.put("encryptedSize", result.encryptedSize);
            response.put("originalSize", result.originalSize);
            response.put("compressionRatio", 
                        (double) result.encryptedSize / result.originalSize);
            
            logger.info("✓ Stream criptografado com sucesso: {}", storageKey);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao criptografar stream", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
    
    /**
     * Serve blob criptografado (download direto)
     */
    @GetMapping("/storage/{storageKey}")
    public ResponseEntity<StreamingResponseBody> getEncryptedBlob(
            @PathVariable String storageKey
    ) {
        logger.info("📤 Servindo blob criptografado: {}", storageKey);
        
        if (!secureStateIO.blobExists(storageKey)) {
            return ResponseEntity.notFound().build();
        }
        
        long blobSize = secureStateIO.getBlobSize(storageKey);
        
        StreamingResponseBody responseBody = outputStream -> {
            try {
                // Aqui retornamos o arquivo CRIPTOGRAFADO
                // O cliente será responsável por descriptografar
                String filePath = System.getProperty("storage.path", "/encrypted-storage");
                File file = new File(filePath, storageKey + ".enc");
                
                try (FileInputStream fis = new FileInputStream(file);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {
                    
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                }
                
            } catch (IOException e) {
                logger.error("Erro ao servir blob", e);
                throw e;
            }
        };
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + storageKey + ".enc\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(blobSize))
                .body(responseBody);
    }
    
    /**
     * Descriptografa e retorna stream de vídeo
     * (CUIDADO: Expõe dados descriptografados!)
     */
    @PostMapping("/storage/decrypt")
    public ResponseEntity<StreamingResponseBody> decryptAndStream(
            @RequestBody Map<String, String> request
    ) {
        String storageKey = request.get("storageKey");
        
        logger.info("🔓 Requisição de descriptografia: {}", storageKey);
        
        if (!secureStateIO.blobExists(storageKey)) {
            return ResponseEntity.notFound().build();
        }
        
        StreamingResponseBody responseBody = outputStream -> {
            try {
                secureStateIO.decryptStream(storageKey, outputStream);
                outputStream.flush();
            } catch (Exception e) {
                logger.error("Erro ao descriptografar stream", e);
                throw new IOException("Decryption failed", e);
            }
        };
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                .body(responseBody);
    }
    
    /**
     * Health check específico do storage
     */
    @GetMapping("/storage/health")
    public ResponseEntity<Map<String, Object>> storageHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("storage", "accessible");
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Gera chave única para storage
     */
    private String generateStorageKey(String sessionId, String cameraId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("%s_%s_%s_%s", 
                           cameraId != null ? cameraId : "cam",
                           sessionId != null ? sessionId.substring(0, 8) : uuid,
                           timestamp,
                           uuid);
    }
}
