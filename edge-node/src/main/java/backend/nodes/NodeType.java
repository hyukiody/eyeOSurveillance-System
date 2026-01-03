package backend.nodes; 

import java.io.IOException;

public class NodeType {

	private String id;
	private String originalImagePath;
	private String invertedImagePath;
	private String name;
	private Object data; // Generic data holder

	private NodeType leftNode;
	private NodeType rightNode;

	public NodeType(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public boolean isBranch() {
		return (leftNode != null) || (rightNode != null);
	}

	public void setData(Object doc) {
		if (doc != null) {
			this.data = doc;
		} else {
			System.out.println("Error: Data cannot be null");
		}
	}

	public void setLeftNode(NodeType node) {
		this.leftNode = node;
	}

	public void setRightNode(NodeType node) {
		this.rightNode = node;
	}

	public String getId() {
		return id;
	}
}