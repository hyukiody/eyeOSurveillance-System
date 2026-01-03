package backend.controllers;

import backend.dto.ApiResponse;
import backend.dto.ImageInvertResponse;
import backend.services.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for Image Inversion Operations
 * Provides multiple endpoints similar to helloworld-rs pattern:
 * - /api/images/invert - Binary image data (PNG)
 * - /api/images/invert/json - JSON response with base64 encoded image
 * - /api/images/invert/xml - XML response format
 * - /api/images/health - Health check endpoint
 */
@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Original endpoint: Returns inverted image as PNG binary data
     * Similar to helloworld-rs /rest/xml - returns binary data
     */
    @PostMapping("/invert")
    public ResponseEntity<byte[]> invertImage(@RequestParam("file") MultipartFile file) {
        try {
            long startTime = System.currentTimeMillis();
            byte[] processedImage = imageService.processAndSave(file);
            long processingTime = System.currentTimeMillis() - startTime;
            
            return ResponseEntity.ok()
                    .header("X-Processing-Time-Ms", String.valueOf(processingTime))
                    .contentType(MediaType.IMAGE_PNG)
                    .body(processedImage);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * JSON endpoint: Returns inverted image as JSON with base64 encoded data
     * Similar to helloworld-rs /rest/json
     */
    @PostMapping(value = "/invert/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ImageInvertResponse>> invertImageAsJson(
            @RequestParam("file") MultipartFile file) {
        try {
            long startTime = System.currentTimeMillis();
            byte[] processedImage = imageService.processAndSave(file);
            long processingTime = System.currentTimeMillis() - startTime;
            
            ImageInvertResponse response = new ImageInvertResponse(
                    "success",
                    "Image inverted successfully",
                    processedImage,
                    "PNG",
                    processingTime
            );
            
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Image processed successfully", response)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Error processing image: " + e.getMessage()));
        }
    }

    /**
     * XML endpoint: Returns inverted image as XML response
     * Similar to helloworld-rs /rest/xml
     */
    @PostMapping(value = "/invert/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<ApiResponse<ImageInvertResponse>> invertImageAsXml(
            @RequestParam("file") MultipartFile file) {
        try {
            long startTime = System.currentTimeMillis();
            byte[] processedImage = imageService.processAndSave(file);
            long processingTime = System.currentTimeMillis() - startTime;
            
            ImageInvertResponse response = new ImageInvertResponse(
                    "success",
                    "Image inverted successfully",
                    processedImage,
                    "PNG",
                    processingTime
            );
            
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Image processed successfully", response)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Error processing image: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     * Returns API status in JSON format
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Image Inverter API is running", "OK")
        );
    }

    /**
     * API information endpoint
     * Provides API version and available endpoints
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> info() {
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        info.put("service", "Image Inverter API");
        info.put("version", "1.0.0");
        info.put("endpoints", new String[]{
                "POST /api/images/invert - Returns PNG binary",
                "POST /api/images/invert/json - Returns JSON response",
                "POST /api/images/invert/xml - Returns XML response",
                "GET /api/images/health - Health check",
                "GET /api/images/info - API information"
        });
        
        return ResponseEntity.ok(
                new ApiResponse<>(true, "API Information", info)
        );
    }
}