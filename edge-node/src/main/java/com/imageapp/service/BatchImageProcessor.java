package com.imageapp.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BatchImageProcessor {

    public static class BatchRequest {
        public List<ImageData> images;
        public String operation; // "invert", "grayscale", "blur"

        public BatchRequest() {
            this.images = new ArrayList<>();
        }
    }

    public static class ImageData {
        public byte[] imageBytes;
        public String format;
        public String id;
    }

    public static class BatchResponse {
        public List<ProcessedImage> results;
        public long totalProcessingTime;

        public BatchResponse() {
            this.results = new ArrayList<>();
        }
    }

    public static class ProcessedImage {
        public String id;
        public byte[] imageBytes;
        public String status;
        public long processingTime;
    }

    public static BatchResponse processBatch(BatchRequest request, ImageInversionService service) throws IOException {
        BatchResponse response = new BatchResponse();
        long startTime = System.currentTimeMillis();

        for (ImageData imageData : request.images) {
            long itemStart = System.currentTimeMillis();
            ProcessedImage result = new ProcessedImage();
            result.id = imageData.id;

            try {
                BufferedImage img = service.readImage(imageData.imageBytes);
                img = service.processImage(img, request.operation);
                result.imageBytes = service.writeImage(img, imageData.format);
                result.status = "SUCCESS";
            } catch (Exception e) {
                result.status = "ERROR: " + e.getMessage();
                result.imageBytes = new byte[0];
            }

            result.processingTime = System.currentTimeMillis() - itemStart;
            response.results.add(result);
        }

        response.totalProcessingTime = System.currentTimeMillis() - startTime;
        return response;
    }
}
