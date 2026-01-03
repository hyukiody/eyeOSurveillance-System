package com.imageapp.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Service for image inversion and processing operations
 */
@Service
public class ImageInversionService implements ImageProcessingService {

    @Override
    public BufferedImage processImage(BufferedImage image, String operation) throws IOException {
        return switch (operation.toLowerCase()) {
            case "invert" -> invertImage(image);
            case "grayscale" -> ImageFiltersService.convertToGrayscale(image);
            case "blur" -> ImageFiltersService.applyBlur(image, 3);
            default -> throw new IllegalArgumentException("Unknown operation: " + operation);
        };
    }

    @Override
    public BufferedImage invertImage(BufferedImage image) throws IOException {
        BufferedImage inverted = new BufferedImage(
            image.getWidth(), 
            image.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                
                // Extract RGB components
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // Invert colors
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;
                
                // Reconstruct RGB
                int invertedRgb = (r << 16) | (g << 8) | b;
                inverted.setRGB(x, y, invertedRgb);
            }
        }
        
        return inverted;
    }

    /**
     * Read image from byte array
     */
    public BufferedImage readImage(byte[] imageBytes) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                throw new IOException("Failed to decode image");
            }
            return image;
        }
    }

    /**
     * Write image to byte array
     */
    public byte[] writeImage(BufferedImage image, String format) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format, baos);
            return baos.toByteArray();
        }
    }
}
