package bt.nhdcl.assetmicroservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.List;

@Document(collection = "assets")
public class Asset {

    @Id
    private String assetCode; // Given by the user
    private int assetID; // Automatically generated
    private String title;
    private int cost;
    private String acquireDate;
    private String lifespan;
    private String assetArea;
    private String description;
    private String status;
    private String createdBy;
    private String deletedBy;
    private String academyID;
    private String assetCategoryName;
    private List<Attribute> attributes;

    // No-argument constructor
    public Asset() {
    }

    // Parameterized constructor
    public Asset(String assetCode, int assetID, String title, int cost, String acquireDate, String lifespan,
            String assetArea, String description, String status, String createdBy, String deletedBy,
            String academyID, String assetCategoryName, List<Attribute> attributes) {
        System.out.println("Asset constructor called for: " + assetCode); // Debugging statement
        this.assetCode = assetCode;
        this.assetID = assetID;
        this.title = title;
        this.cost = cost;
        this.acquireDate = acquireDate;
        this.lifespan = lifespan;
        this.assetArea = assetArea;
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
        this.deletedBy = deletedBy;
        this.academyID = academyID;
        this.assetCategoryName = assetCategoryName;
        if (attributes == null) {
            this.attributes = new ArrayList<>();
        } else {
            this.attributes = attributes;
        }

    }

    private boolean isExcludedCategory(String category) {
        return category != null && (category.equalsIgnoreCase("Building") ||
                category.equalsIgnoreCase("Landscaping") ||
                category.equalsIgnoreCase("Infrastructure") ||
                category.equalsIgnoreCase("Facilities"));
    }

    private void generateQRCodeIfNeeded() {
        if (!isExcludedCategory(this.assetCategoryName)) {
            addQRCodeToAttributes();
        }
    }

    private void addQRCodeToAttributes() {
        String qrData = "Asset Code: " + assetCode +
                ", Title: " + title +
                ", Category: " + assetCategoryName;
        String qrBase64 = generateQRCode(qrData);

        if (qrBase64 != null) {
            Attribute qrAttribute = new Attribute("QR Code", qrBase64);
            this.attributes.add(qrAttribute);
        }
    }

    private String generateQRCode(String text) {
        try {
            int width = 200;
            int height = 200;
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (WriterException | java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // This method will be used to ensure QR code generation after loading from
    // MongoDB
    public void ensureQRCodeGeneration() {
        if (!isExcludedCategory(this.assetCategoryName)) {
            addQRCodeToAttributes();
        }
    }

    public void addFileAttribute(String fileUrl) {
        if (fileUrl != null && !fileUrl.isEmpty()) {
            this.attributes.add(new Attribute("file", fileUrl));
        }
    }

    public void setAssetIDAuto(int nextAssetID) {
        if (this.assetID == 0) { // If assetID is not provided
            this.assetID = nextAssetID; // Set the new assetID
        }
    }

    // Getters and Setters
    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public int getAssetID() {
        return assetID;
    }

    public void setAssetID(int assetID) {
        this.assetID = assetID;
    }

    // Other getters and setters...
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public String getAcquireDate() {
        return acquireDate;
    }

    public void setAcquireDate(String acquireDate) {
        this.acquireDate = acquireDate;
    }

    public String getLifespan() {
        return lifespan;
    }

    public void setLifespan(String lifespan) {
        this.lifespan = lifespan;
    }

    public String getAssetArea() {
        return assetArea;
    }

    public void setAssetArea(String assetArea) {
        this.assetArea = assetArea;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public String getAcademyID() {
        return academyID;
    }

    public void setAcademyID(String academyID) {
        this.academyID = academyID;
    }

    public String getAssetCategoryName() {
        return assetCategoryName;
    }

    public void setAssetCategoryName(String assetCategoryName) {
        this.assetCategoryName = assetCategoryName;

    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
        // Generate QR code if necessary
        generateQRCodeIfNeeded();
    }
}
