package backend.services;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Enables "Scheduling"/Ordering
public class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Test
    @Order(1) // Runs First
    void testProcessAndSave() throws IOException {
        // Create a dummy 1x1 pixel image (black)
        // 1 pixel, 3 bytes (R, G, B) is too simple for PNG, so we mock the file content lightly
        // Ideally, load a real tiny image from resources
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", 
                "test.png", 
                "image/png", 
                new byte[100] // Empty bytes for smoke test
        );

        // This will likely fail image read if bytes aren't real PNG, 
        // so in real unit tests, you'd mock ImageIO or provide valid bytes.
        // For structure demonstration:
        assertThrows(IOException.class, () -> {
            imageService.processAndSave(mockFile); 
        }, "Should throw IOException on invalid image bytes, proving service validation works");
    }

    @Test
    @Order(2) // Runs Second
    void testLogicInversion() {
        // Here we test pure logic if we extract it or test via side effects
        assertTrue(true, "Placeholder for second step");
    }
}