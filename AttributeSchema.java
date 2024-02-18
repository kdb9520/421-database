import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class AttributeSchema {
    String attrName;
    String attrType;
    boolean isNotNull;
    boolean isPrimaryKey;
    boolean isUnique;
    Object defaultValue;
    
    public AttributeSchema(String attrName, String attrType, String[] constraints) {
        this.attrName = attrName;
        this.attrType = attrType;
        this.isNotNull = false;
        this.isPrimaryKey = false;
        this.isUnique = false;
        setConstraints(constraints);
        this.defaultValue = null;
    }

    public AttributeSchema(String attrName, String attrType, String[] constraints, Object dVal) {
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

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public String getType() {
        return this.attrType;
    }

    // Serialize in format: [AttrName,AttrType,isNull,isPK,isUN,defaultValue]
    public byte[] serialize() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(attrName.getBytes());
            oos.writeObject(attrType.getBytes());
            byte isNull = (byte)(isNotNull?1:0);
            oos.writeObject(isNull);
            byte isPK = (byte)(isPrimaryKey?1:0);
            oos.writeObject(isPK);
            byte isUN = (byte)(isUnique?1:0);
            oos.writeObject(isUN);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

  // Deserialize a byte array into a record object
  public static AttributeSchema deserialize(byte[] data) {
   
    return null;
}

