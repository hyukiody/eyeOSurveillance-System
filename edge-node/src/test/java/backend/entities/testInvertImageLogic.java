package backend.entities;

import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class ImageInverterTest {

    @Test
    void testInvertImageLogic() {
        // 1. Setup: Create a 1x1 pixel White Image
        BufferedImage original = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        int whiteRgba = new Color(255, 255, 255, 255).getRGB();
        original.setRGB(0, 0, whiteRgba);

        // 2. Execute: Invert
        BufferedImage result = ImageInverter.invertImage(original);

        // 3. Verify: Expect Black (Inverse of White) but Alpha preserved
        int resultRgba = result.getRGB(0, 0);
        Color resultColor = new Color(resultRgba, true);

        assertEquals(0, resultColor.getRed(), "Red should be 0");
        assertEquals(0, resultColor.getGreen(), "Green should be 0");
        assertEquals(0, resultColor.getBlue(), "Blue should be 0");
        assertEquals(255, resultColor.getAlpha(), "Alpha should remain 255");
    }
}