package backend.processor;

import javax.imageio.ImageIO;

import backend.models.ImageData;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AwtImageProcessor implements ImageProcessor {

    @Override
    public void loadImage(ImageData data) throws IOException {
        File f = new File(data.getFsPath());
        if (!f.exists()) {
            throw new FileNotFoundException("File not found at: " + data.getFsPath());
        }
        BufferedImage img = ImageIO.read(f);
        if (img == null) {
            throw new IOException("File exists but is not a valid image: " + data.getFsPath());
        }
        data.setImageBuffer(img);
    }

    @Override
    public void invertColors(ImageData data) {
        BufferedImage src = data.getImageBuffer();
        if (src == null) {
            throw new IllegalStateException("Image buffer is empty. Call loadImage first.");
        }

        int width = src.getWidth();
        int height = src.getHeight();

        // Create destination image (TYPE_INT_ARGB supports transparency)
        BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgba = src.getRGB(x, y);
                Color col = new Color(rgba, true); // true = process alpha

                // Invert Logic: 255 - value
                int r = 255 - col.getRed();
                int g = 255 - col.getGreen();
                int b = 255 - col.getBlue();

                // Reconstruct pixel (keep original Alpha)
                Color newCol = new Color(r, g, b, col.getAlpha());
                dest.setRGB(x, y, newCol.getRGB());
            }
        }
        data.setImageBuffer(dest);
    }

    @Override
    public void saveImage(ImageData data, String outputPath) throws IOException {
        BufferedImage img = data.getImageBuffer();
        if (img == null) {
            System.err.println("No image to save.");
            return;
        }
        File outputFile = new File(outputPath);
        // Extract extension or default to png
        String ext = outputPath.endsWith(".jpg") ? "jpg" : "png";
        
        ImageIO.write(img, ext, outputFile);
        System.out.println("Saved image to: " + outputFile.getAbsolutePath());
    }
}