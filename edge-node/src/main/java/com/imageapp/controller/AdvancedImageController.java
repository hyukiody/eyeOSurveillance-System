package com.imageapp.controller;

import com.imageapp.service.BatchImageProcessor;
import com.imageapp.service.ImageFiltersService;
import com.imageapp.service.ImageInversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class AdvancedImageController {

    private final ImageInversionService inversionService;

    public AdvancedImageController(ImageInversionService inversionService) {
        this.inversionService = inversionService;
    }

    @PostMapping("/batch-process")
    public ResponseEntity<?> batchProcess(@RequestBody BatchImageProcessor.BatchRequest request) {
        try {
            BatchImageProcessor.BatchResponse response = BatchImageProcessor.processBatch(request, null);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/filter/grayscale")
    public ResponseEntity<?> applyGrayscale(@RequestParam("file") MultipartFile file) {
        try {
            BufferedImage img = inversionService.readImage(file.getBytes());
            BufferedImage filtered = ImageFiltersService.convertToGrayscale(img);
            byte[] result = inversionService.writeImage(filtered, "png");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "filter", "grayscale",
                "size", result.length
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/filter/blur")
    public ResponseEntity<?> applyBlur(@RequestParam("file") MultipartFile file,
                                       @RequestParam(defaultValue = "5") int radius) {
        try {
            BufferedImage img = inversionService.readImage(file.getBytes());
            BufferedImage filtered = ImageFiltersService.applyBlur(img, radius);
            byte[] result = inversionService.writeImage(filtered, "png");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "filter", "blur",
                "radius", radius
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/filter/brightness")
    public ResponseEntity<?> adjustBrightness(@RequestParam("file") MultipartFile file,
                                              @RequestParam(defaultValue = "1.0") float factor) {
        try {
            BufferedImage img = inversionService.readImage(file.getBytes());
            BufferedImage filtered = ImageFiltersService.adjustBrightness(img, factor);
            byte[] result = inversionService.writeImage(filtered, "png");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "filter", "brightness",
                "factor", factor
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/filter/contrast")
    public ResponseEntity<?> adjustContrast(@RequestParam("file") MultipartFile file,
                                            @RequestParam(defaultValue = "1.0") float factor) {
        try {
            BufferedImage img = inversionService.readImage(file.getBytes());
            BufferedImage filtered = ImageFiltersService.adjustContrast(img, factor);
            byte[] result = inversionService.writeImage(filtered, "png");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "filter", "contrast",
                "factor", factor
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/filter/sharpen")
    public ResponseEntity<?> applySharpen(@RequestParam("file") MultipartFile file) {
        try {
            BufferedImage img = inversionService.readImage(file.getBytes());
            BufferedImage filtered = ImageFiltersService.applySharpen(img);
            byte[] result = inversionService.writeImage(filtered, "png");
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "filter", "sharpen"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/filters")
    public ResponseEntity<?> getAvailableFilters() {
        return ResponseEntity.ok(Map.of(
            "available_filters", List.of(
                "invert", "grayscale", "blur", "brightness", "contrast", "sharpen"
            ),
            "batch_supported", true
        ));
    }
}
