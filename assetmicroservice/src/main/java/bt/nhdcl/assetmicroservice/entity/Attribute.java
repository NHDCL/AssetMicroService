package bt.nhdcl.assetmicroservice.entity;

public class Attribute {
    private String field;
    private Object value;

    // Constructor
    public Attribute(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    // Getters and Setters
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}

