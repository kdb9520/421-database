public class AttributeSchema<E> {
    String attrName;
    String attrType;
    boolean isNotNull;
    boolean isPrimaryKey;
    boolean isUnique;
    E defaultValue;
    
    public AttributeSchema(String attrName, String attrType, String[] constraints) {
        this.attrName = attrName;
        this.attrType = attrType;
        this.isNotNull = false;
        this.isPrimaryKey = false;
        this.isUnique = false;
        setConstraints(constraints);
        this.defaultValue = null;
    }

    public AttributeSchema(String attrName, String attrType, String[] constraints, E dVal) {
        this.attrName = attrName;
        this.attrType = attrType;
        this.isNotNull = false;
        this.isPrimaryKey = false;
        this.isUnique = false;
        setConstraints(constraints);
        this.defaultValue = dVal;
    }

    private void setConstraints(String[] constraints) {
        for (String con : constraints) {
            switch (con) {
                case "PRIMARYKEY":
                    this.isPrimaryKey = true;
                    this.isNotNull = true;
                    this.isUnique = true;
                    break;
                case "NOTNULL":
                    this.isNotNull = true;
                    break;
                case "UNIQUE":
                    this.isUnique = true;
                    break;
                default:
                    break;
            }
        }
    }

    public E getDefaultValue() {
        return this.defaultValue;
    }

    public String getType() {
        return this.attrType;
    }

}
