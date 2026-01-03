package com.imageapp.service;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Common interface for image processing operations
 */
public interface ImageProcessingService {
    
    /**
     * Process an image with the specified operation
     * 
     * @param image Input image
     * @param operation Operation type ("invert", "grayscale", "blur", etc.)
     * @return Processed image
     * @throws IOException if processing fails
     */
    BufferedImage processImage(BufferedImage image, String operation) throws IOException;
    
    /**
     * Invert image colors
     */
    BufferedImage invertImage(BufferedImage image) throws IOException;
}
