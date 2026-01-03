package worker;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

import utils.ImageProcessor;

public class ImageProcessingWorker extends SwingWorker<BufferedImage, Void> {

    private final BufferedImage sourceImage;
    private final Consumer<BufferedImage> onFinish; // Callback para atualizar UI

    public ImageProcessingWorker(BufferedImage source, Consumer<BufferedImage> onFinish) {
        this.sourceImage = source;
        this.onFinish = onFinish;
    }

    @Override
    protected BufferedImage doInBackground() throws Exception {
        // Processamento pesado acontece aqui
        return utils.ImageProcessor.invert(sourceImage);
    }

    @Override
    protected void done() {
        try {
            BufferedImage result = get();
            if (onFinish != null) {
                onFinish.accept(result); // Devolve o resultado para a GUI
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}