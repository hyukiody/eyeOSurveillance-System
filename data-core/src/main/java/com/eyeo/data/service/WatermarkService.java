package com.eyeo.data.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Watermark injection service for FREE tier video streams.
 * 
 * Adds "FREE TIER - Upgrade to remove" overlay to video frames
 * before encryption. This implements the monetization strategy
 * by visually differentiating free vs. paid content.
 * 
 * Performance: Uses Java2D for in-memory processing (no FFmpeg dependency).
 * For production, consider GPU-accelerated watermarking.
 */
@Service
@Slf4j
public class WatermarkService {
    
    private static final String WATERMARK_TEXT = "FREE TIER - Upgrade to remove";
    private static final Color WATERMARK_COLOR = new Color(255, 165, 0, 180); // Semi-transparent orange
    private static final int FONT_SIZE = 24;
    private static final String FONT_FAMILY = "Arial";
    
    /**
     * Add watermark overlay to video frame bytes
     * 
     * @param videoFrameBytes Original frame bytes (JPEG/PNG)
     * @param position Watermark position (TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER)
     * @return Watermarked frame bytes
     */
    public byte[] addWatermark(byte[] videoFrameBytes, WatermarkPosition position) throws IOException {
        // Decode image bytes
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(videoFrameBytes));
        if (originalImage == null) {
            log.warn("Failed to decode image for watermarking, returning original");
            return videoFrameBytes;
        }
        
        // Create copy for watermarking
        BufferedImage watermarked = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D g2d = watermarked.createGraphics();
        
        // Draw original image
        g2d.drawImage(originalImage, 0, 0, null);
        
        // Configure text rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE));
        
        // Calculate text position
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(WATERMARK_TEXT);
        int textHeight = fm.getHeight();
        
        int x = calculateX(position, originalImage.getWidth(), textWidth);
        int y = calculateY(position, originalImage.getHeight(), textHeight);
        
        // Draw semi-transparent background rectangle
        g2d.setColor(new Color(0, 0, 0, 120));
        int padding = 10;
        g2d.fillRoundRect(x - padding, y - textHeight, 
                         textWidth + 2 * padding, textHeight + padding, 
                         10, 10);
        
        // Draw watermark text
        g2d.setColor(WATERMARK_COLOR);
        g2d.drawString(WATERMARK_TEXT, x, y);
        
        g2d.dispose();
        
        // Encode back to bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(watermarked, "JPEG", baos);
        byte[] result = baos.toByteArray();
        
        log.debug("Watermark added to frame ({} bytes -> {} bytes)", 
                 videoFrameBytes.length, result.length);
        
        return result;
    }
    
    /**
     * Add watermark to entire video chunk (frame-by-frame processing)
     * 
     * NOTE: This is a simplified implementation. For production, use FFmpeg
     * with command: ffmpeg -i input.mp4 -vf "drawtext=text='FREE TIER':..." output.mp4
     */
    public byte[] addWatermarkToChunk(byte[] videoChunkBytes) throws IOException {
        // For now, just log that watermarking would happen
        // Full implementation would extract frames, watermark each, reassemble
        log.debug("Watermark flag set for {} byte video chunk", videoChunkBytes.length);
        
        // TODO: Implement frame extraction and watermarking
        // This requires video codec knowledge (H.264, VP9, etc.)
        
        return videoChunkBytes; // Passthrough for now
    }
    
    /**
     * Check if watermark is required based on license tier
     */
    public boolean isWatermarkRequired(String licenseTier) {
        return "FREE".equalsIgnoreCase(licenseTier);
    }
    
    private int calculateX(WatermarkPosition position, int imageWidth, int textWidth) {
        return switch (position) {
            case TOP_LEFT, BOTTOM_LEFT -> 20;
            case TOP_RIGHT, BOTTOM_RIGHT -> imageWidth - textWidth - 20;
            case CENTER -> (imageWidth - textWidth) / 2;
        };
    }
    
    private int calculateY(WatermarkPosition position, int imageHeight, int textHeight) {
        return switch (position) {
            case TOP_LEFT, TOP_RIGHT -> textHeight + 20;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> imageHeight - 20;
            case CENTER -> (imageHeight + textHeight) / 2;
        };
    }
    
    /**
     * Watermark position enum
     */
    public enum WatermarkPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        CENTER
    }
}
