public class Attribute {
    String a_name;
    String a_type;
    boolean isNotNull;
    boolean isPrimaryKey;
    boolean isUnique;
    String defaultValue;

    public Attribute(String a_name, String a_type, String[] constraints) {
        this.a_name = a_name;
        this.a_type = a_type;
        this.isNotNull = false;
        this.isPrimaryKey = false;
        this.isUnique = false;
        setConstraints(constraints);
        this.defaultValue = null;
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
