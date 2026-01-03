package backend.processor;

import java.io.IOException;

import backend.models.ImageData;

public interface ImageProcessor {
    void loadImage(ImageData data) throws IOException;
    void invertColors(ImageData data);
    void saveImage(ImageData data, String outputPath) throws IOException;
}