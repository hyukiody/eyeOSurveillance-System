package backend.entities;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageInverter {

    /**
     * Static utility method to invert a BufferedImage.
     */
    public static BufferedImage invertImage(BufferedImage inputImage) {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        // Create a new image for the result
        BufferedImage invertedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Loop through every pixel		
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                
                // Get current color
                int rgba = inputImage.getRGB(x, y);
                Color col = new Color(rgba, true);
                
                // Invert colors (255 - original)
                int r = 255 - col.getRed();
                int g = 255 - col.getGreen();
                int b = 255 - col.getBlue();
                
                // Set new color (keep alpha/transparency the same)
                Color newCol = new Color(r, g, b, col.getAlpha());
                invertedImage.setRGB(x, y, newCol.getRGB());
            }
        }
        return invertedImage;
    }
}