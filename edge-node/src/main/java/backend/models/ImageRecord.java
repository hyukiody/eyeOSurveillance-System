package backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "image_history")
public class ImageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private LocalDateTime processedAt;

    @Lob
    @Column(length = 10000000) // Aumenta tamanho para imagens grandes
    private byte[] originalImage;

    @Lob
    @Column(length = 10000000)
    private byte[] invertedImage;

    // Getters e Setters padrão (omissos para brevidade)
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public void setOriginalImage(byte[] originalImage) { this.originalImage = originalImage; }
    public void setInvertedImage(byte[] invertedImage) { this.invertedImage = invertedImage; }
}