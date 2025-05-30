package bt.nhdcl.assetmicroservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
public class Category {
    
    @Id
    private String id;
    private String name;
    private int depreciatedValue;
    private boolean deleted;

    // Default constructor
    public Category() {}

    // Parameterized constructor
    public Category(String name, int depreciatedValue, boolean deleted) {
        this.name = name;
        this.depreciatedValue = depreciatedValue;
        this.deleted =deleted;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDepreciatedValue() {
        return depreciatedValue;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDepreciatedValue(int depreciatedValue) {
        this.depreciatedValue = depreciatedValue;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}