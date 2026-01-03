package backend.models;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

/**
 * POJO representing the specific data spec.
 */
public class ImageData {

	private BufferedImage imageBuffer; // The image data in memory
	private double axisX;
	private double axisY;
	private double rotationAngle;
	private String fsPath; // File System Path
	private String referenceId;
	private LocalDateTime originDate;
	private String metaDataCode;

	public ImageData(String fsPath, String referenceId) {
		this.fsPath = fsPath;
		this.referenceId = referenceId;
		this.originDate = LocalDateTime.now();
		this.metaDataCode = "";
		this.axisX = 0.0;
		this.axisY = 0.0;
	}

	// Getters and Setters
	public BufferedImage getImageBuffer() {
		return imageBuffer;
	}

	public void setImageBuffer(BufferedImage imageBuffer) {
		this.imageBuffer = imageBuffer;
	}

	public String getFsPath() {
		return fsPath;
	}

	public void setFsPath(String fsPath) {
		this.fsPath = fsPath;
	}

	public String getMetaDataCode() {
		return metaDataCode;
	}

	public void setMetaDataCode(String metaDataCode) {
		this.metaDataCode = metaDataCode;
	}

	public double getAxisX() {
		return axisX;
	}

	public void setAxisX(double axisX) {
		this.axisX = axisX;
	}

	public double getAxisY() {
		return axisY;
	}

	public void setAxisY(double axisY) {
		this.axisY = axisY;
	}

	public double getRotationAngle() {
		return rotationAngle;
	}

	public void setRotationAngle(double rotationAngle) {
		this.rotationAngle = rotationAngle;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public LocalDateTime getOriginDate() {
		return originDate;
	}

	public void setOriginDate(LocalDateTime originDate) {
		this.originDate = originDate;
	}

	// Add other getters/setters as needed for axisX, axisY, etc.
}