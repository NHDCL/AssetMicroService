package bt.nhdcl.assetmicroservice.entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


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
    private int academyID;
    private int assetCategoryID;
    private List<Attribute> attributes;
    
    public void setAssetIDAuto(int nextAssetID) {
        if (this.assetID == 0) { // If assetID is not provided
            this.assetID = nextAssetID; // Set the new assetID
        }
    }
    // Getters and Setters
    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }

    public int getAssetID() { return assetID; }
    public void setAssetID(int assetID) { this.assetID = assetID; }

    // Other getters and setters...
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }

    public String getAcquireDate() { return acquireDate; }
    public void setAcquireDate(String acquireDate) { this.acquireDate = acquireDate; }

    public String getLifespan() { return lifespan; }
    public void setLifespan(String lifespan) { this.lifespan = lifespan; }

    public String getAssetArea() { return assetArea; }
    public void setAssetArea(String assetArea) { this.assetArea = assetArea; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }

    public int getAcademyID() { return academyID; }
    public void setAcademyID(int academyID) { this.academyID = academyID; }

    public int getAssetCategoryID() { return assetCategoryID; }
    public void setAssetCategoryID(int assetCategoryID) { this.assetCategoryID = assetCategoryID; }

    public List<Attribute> getAttributes() { return attributes; }
    public void setAttributes(List<Attribute> attributes) { this.attributes = attributes; }
}
