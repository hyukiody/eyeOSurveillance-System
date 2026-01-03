package backend.entities.gui;

import backend.nodes.ImageNode;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class MainFrame extends JFrame {

    private JLabel imageLabel;
    private ImageNode rootNode; 
    private SwingImageClient apiClient; // Client instance

    public MainFrame() {
        // Init API Client
        apiClient = new SwingImageClient();

        setTitle("Image Processor Client");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        imageLabel = new JLabel("No image loaded", SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(Color.DARK_GRAY);
        
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton loadBtn = new JButton("Load Image");
        JButton invertBtn = new JButton("Invert via API");
        
        buttonPanel.add(loadBtn);
        buttonPanel.add(invertBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Actions ---
        
        loadBtn.addActionListener(e -> carregarImagem());

        invertBtn.addActionListener(e -> {
            if (rootNode == null || rootNode.getData() == null) {
                JOptionPane.showMessageDialog(this, "Please load an image first.");
                return;
            }

            invertBtn.setEnabled(false);
            File fileToUpload = new File(rootNode.getData().getFsPath()); // Ensure ImageNode stores path
            
            // Call API
            apiClient.sendImage(fileToUpload, 
                // Success Callback
                (imageData) -> {
                    try {
                        BufferedImage result = ImageIO.read(new ByteArrayInputStream(imageData));
                        displayImage(result);
                        JOptionPane.showMessageDialog(this, "Success! Image processed by Server.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    invertBtn.setEnabled(true);
                },
                // Error Callback
                (errorMsg) -> {
                    JOptionPane.showMessageDialog(this, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
                    invertBtn.setEnabled(true);
                }
            );
        });
    }

    private void carregarImagem() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
            	String uniqueId = java.util.UUID.randomUUID().toString();
                // Assuming ImageNode and ImageData constructor fits this
                // You might need to adapt this based on your exact Node definition
            	backend.models.ImageData imageData = new backend.models.ImageData(
                        file.getAbsolutePath(), 
                        uniqueId // Passa o UUID gerado em vez de chamar método inexistente
                    );

                    rootNode = new ImageNode(imageData);
                BufferedImage img = ImageIO.read(file);
                rootNode.getData().setImageBuffer(img);
                displayImage(img);
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void displayImage(BufferedImage img) {
        if (img != null) {
            ImageIcon icon = new ImageIcon(img);
            imageLabel.setText("");
            imageLabel.setIcon(icon);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}