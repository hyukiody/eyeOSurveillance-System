package utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ImageProcessor {

    /**
     * Inverts the colors of the image.
     * Replaces your logical mix of Python/Java.
     */
    public static BufferedImage invert(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = new BufferedImage(width, height, source.getType());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgba = source.getRGB(x, y);
                Color col = new Color(rgba, true);
                
                // Invert RGB logic (255 - value)
                int r = 255 - col.getRed();
                int g = 255 - col.getGreen();
                int b = 255 - col.getBlue();
                
                // Reconstruct pixel preserving Alpha
                Color newCol = new Color(r, g, b, col.getAlpha());
                result.setRGB(x, y, newCol.getRGB());
            }
        }
        return result;
    }

    /**
     * Mirrors the image horizontally.
     */
    public static BufferedImage mirror(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = new BufferedImage(width, height, source.getType());
        
        Graphics2D g = result.createGraphics();
        // Draw image from right to left (dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2)
        g.drawImage(source, width, 0, 0, height, 0, 0, width, height, null);
        g.dispose();
        return result;
    }

    /**
     * "Thumble" - Flips the image vertically.
     */
    public static BufferedImage thumble(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = new BufferedImage(width, height, source.getType());
        
        Graphics2D g = result.createGraphics();
        // Draw image from bottom to top
        g.drawImage(source, 0, height, width, 0, 0, 0, width, height, null);
        g.dispose();
        return result;
    }
}