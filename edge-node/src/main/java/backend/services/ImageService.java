package backend.services;

import backend.models.ImageRecord;
import backend.repositories.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class ImageService {

    private final ImageRepository repository;

    public ImageService(ImageRepository repository) {
        this.repository = repository;
    }

    public byte[] processAndSave(MultipartFile file) throws IOException {
        // 1. Read Image
        BufferedImage originalInfo = ImageIO.read(file.getInputStream());
        if (originalInfo == null) {
            throw new IOException("Invalid image file");
        }
        
        BufferedImage invertedInfo = invertColors(originalInfo);

        // 3. Convert to Bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(invertedInfo, "png", baos);
        byte[] invertedBytes = baos.toByteArray();

        // 4. Save to Database
        ImageRecord record = new ImageRecord();
        record.setFileName(file.getOriginalFilename());
        record.setProcessedAt(LocalDateTime.now());
        record.setOriginalImage(file.getBytes());
        record.setInvertedImage(invertedBytes);
        
        repository.save(record);

        return invertedBytes;
    }

    private BufferedImage invertColors(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        // Use TYPE_INT_ARGB to support transparency
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgba = image.getRGB(x, y); // Fixed: Was missing in your upload
                
                int alpha = rgba & 0xFF000000; // Keep Alpha
                int colors = (rgba & 0x00FFFFFF) ^ 0xFFFFFF; // Invert Colors
                
                result.setRGB(x, y, alpha | colors);
            }
        }
        return result;
    }
}