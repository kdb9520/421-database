import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

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

    public AttributeSchema(String attrName, String attrType, boolean isNull, boolean isPK, boolean isUN) {
        this.attrName = attrName;
        this.attrType = attrType;
        this.isNotNull = !isNull;
        this.isPrimaryKey = isPK;
        this.isUnique = isUN;
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

    // Serialize in format: [AttrNameSize,AttrName,AttrTypeSize,AttrType,isNull,isPK,isUN,defaultValue]
    public byte[] serialize() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            // Serialize the length and content of attrName
            byte[] attrNameBytes = attrName.getBytes();
            oos.writeInt(attrNameBytes.length);
            oos.write(attrNameBytes);

            // Serialize the length and content of attrType
            byte[] attrTypeBytes = attrType.getBytes();
            oos.writeInt(attrTypeBytes.length);
            oos.write(attrTypeBytes);

        
              // Serialize isNull, isPK, and isUN directly
              oos.writeBoolean(isNotNull);
              oos.writeBoolean(isPrimaryKey);
              oos.writeBoolean(isUnique);

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

      // Deserialize a byte array into a record object
      public static AttributeSchema deserialize(ByteBuffer buffer) {
        // Read attribute details from the buffer
        String attrName = readString(buffer);
        String attrType = readString(buffer);
        boolean isNull = buffer.get() != 0;
        boolean isPK = buffer.get() != 0;
        boolean isUN = buffer.get() != 0;

        return new AttributeSchema(attrName, attrType, isNull, isPK, isUN);
    }

    private static String readString(ByteBuffer buffer) {
        int length = buffer.getInt();
        byte[] stringBytes = new byte[length];
        buffer.get(stringBytes);
        return new String(stringBytes);
    }

    @Override
    public String toString() {
        return "AttributeSchema{" +
                "attrName='" + attrName + '\'' +
                ", attrType='" + attrType + '\'' +
                ", isNull=" + isNotNull +
                ", isPK=" + isPrimaryKey +
                ", isUN=" + isUnique +
                '}';
    }
}



