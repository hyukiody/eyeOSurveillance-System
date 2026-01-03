package com.imageapp.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class ImageFiltersService {

    public static BufferedImage convertToGrayscale(BufferedImage image) {
        BufferedImage grayscale = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayscale.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return grayscale;
    }

    public static BufferedImage applyBlur(BufferedImage image, int radius) {
        int size = radius * 2 + 1;
        float[] kernel = new float[size * size];
        float value = 1.0f / (size * size);
        for (int i = 0; i < kernel.length; i++) {
            kernel[i] = value;
        }

        BufferedImage blurred = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        ConvolveOp op = new ConvolveOp(new Kernel(size, size, kernel), ConvolveOp.EDGE_NO_OP, null);
        op.filter(image, blurred);
        return blurred;
    }

    public static BufferedImage adjustBrightness(BufferedImage image, float factor) {
        BufferedImage adjusted = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                r = Math.min(255, (int) (r * factor));
                g = Math.min(255, (int) (g * factor));
                b = Math.min(255, (int) (b * factor));

                int newRgb = (r << 16) | (g << 8) | b;
                adjusted.setRGB(x, y, newRgb);
            }
        }
        return adjusted;
    }

    public static BufferedImage adjustContrast(BufferedImage image, float factor) {
        BufferedImage adjusted = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                r = Math.min(255, Math.max(0, (int) ((r - 128) * factor + 128)));
                g = Math.min(255, Math.max(0, (int) ((g - 128) * factor + 128)));
                b = Math.min(255, Math.max(0, (int) ((b - 128) * factor + 128)));

                int newRgb = (r << 16) | (g << 8) | b;
                adjusted.setRGB(x, y, newRgb);
            }
        }
        return adjusted;
    }

    public static BufferedImage applySharpen(BufferedImage image) {
        float[] kernelData = {
            0, -1, 0,
            -1, 5, -1,
            0, -1, 0
        };

        BufferedImage sharpened = new BufferedImage(
            image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        ConvolveOp op = new ConvolveOp(new Kernel(3, 3, kernelData));
        op.filter(image, sharpened);
        return sharpened;
    }
}
