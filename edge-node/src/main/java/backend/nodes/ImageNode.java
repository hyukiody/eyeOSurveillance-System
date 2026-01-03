package backend.nodes;

import backend.processor.ImageProcessor;

import backend.models.ImageData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImageNode {

    private final String nodeId;
    private ImageData data;
    
    // Hierarchy
    private ImageNode parent;
    private List<ImageNode> children;

    public ImageNode(ImageData data) {
        this.nodeId = UUID.randomUUID().toString();
        this.data = data;
        this.children = new ArrayList<>();
    }

    // --- Tree Operations ---

    public void addChild(ImageNode node) {
        node.setParent(this);
        this.children.add(node);
    }

    public void removeChild(ImageNode node) {
        this.children.remove(node);
    }

    // Corrected: Returns a List, not a Listener
    public List<ImageNode> getChildren() {
        return new ArrayList<>(children);
    }

    public ImageNode getParent() {
        return parent;
    }

    public void setParent(ImageNode parent) {
        this.parent = parent;
    }

    // --- Data Access ---

    public String getNodeId() {
        return nodeId;
    }

    public ImageData getData() {
        return data;
    }

    public void setData(ImageData data) {
        this.data = data;
    }

    // --- Processing Hook ---
    
    public void process(ImageProcessor processor) {
        try {
            // 1. Load image if not loaded
            if (data.getImageBuffer() == null) {
                processor.loadImage(data);
            }
            // 2. Apply inversion
            processor.invertColors(data);
            
            System.out.println("Node " + nodeId + " processed successfully.");
            
        } catch (Exception e) {
            System.err.println("Error processing node " + nodeId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}