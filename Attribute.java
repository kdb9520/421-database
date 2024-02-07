public class Attribute {
    String a_name;
    String a_type;
    boolean isNotNull;
    boolean isPrimaryKey;
    boolean isUnique;
    String defaultValue;

    public Attribute(String a_name, String a_type) {
        this.a_name = a_name;
        this.a_type = a_type;
        this.isNotNull = false;
        this.isPrimaryKey = false;
        this.isUnique = false;
        this.defaultValue = null;
    }

    public void setNotNull(boolean val) {
        this.isNotNull = val;
    }

    public void setPrimaryKey(boolean val) {
        this.isPrimaryKey = val;
    }

    public void setUnique(boolean val) {
        this.isUnique = val;
    }

    public void setDefaultValue(String dVal) {
        this.defaultValue = dVal;
    }

    public String getName() {
        return this.a_name;
    }

    public String getType() {
        return this.a_type;
    }

}
