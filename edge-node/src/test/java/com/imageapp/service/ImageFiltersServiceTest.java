package com.imageapp.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class ImageFiltersServiceTest {

    private BufferedImage createTestImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                img.setRGB(x, y, Color.RED.getRGB());
            }
        }
        return img;
    }

    @Test
    public void testGrayscaleConversion() {
        BufferedImage img = createTestImage(10, 10);
        BufferedImage gray = ImageFiltersService.convertToGrayscale(img);
        
        assertNotNull(gray);
        assertEquals(10, gray.getWidth());
        assertEquals(10, gray.getHeight());
    }

    @Test
    public void testBlurApplication() {
        BufferedImage img = createTestImage(20, 20);
        BufferedImage blurred = ImageFiltersService.applyBlur(img, 3);
        
        assertNotNull(blurred);
        assertEquals(img.getWidth(), blurred.getWidth());
    }

    @Test
    public void testBrightnessAdjustment() {
        BufferedImage img = createTestImage(10, 10);
        BufferedImage bright = ImageFiltersService.adjustBrightness(img, 1.5f);
        
        assertNotNull(bright);
        assertEquals(img.getWidth(), bright.getWidth());
    }

    @Test
    public void testContrastAdjustment() {
        BufferedImage img = createTestImage(10, 10);
        BufferedImage contrast = ImageFiltersService.adjustContrast(img, 1.2f);
        
        assertNotNull(contrast);
        assertEquals(img.getWidth(), contrast.getWidth());
    }

    @Test
    public void testSharpenFilter() {
        BufferedImage img = createTestImage(10, 10);
        BufferedImage sharpened = ImageFiltersService.applySharpen(img);
        
        assertNotNull(sharpened);
    }
}
