import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    public AttributeSchema(String attrName, String attrType, boolean isNotNull, boolean isPK, boolean isUN) {
        this.attrName = attrName;
        this.attrType = attrType;
        this.isNotNull = isNotNull;
        this.isPrimaryKey = isPK;
        this.isUnique = isUN;
    }

    private void setConstraints(String[] constraints) {
        if(constraints == null){
            return;
        }
        for (String con : constraints) {
            con = con.toUpperCase();
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

    public boolean getIsNotNull () {
        return this.isNotNull;
    }

    public boolean getIsUnique () {
        return this.isUnique;
    }

    // Serialize in format: [AttrNameSize,AttrName,AttrTypeSize,AttrType,isNull,isPK,isUN,defaultValue]
    public byte[] serialize() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(bos);) {
            // Serialize the length and content of attrName
            byte[] attrNameBytes = attrName.getBytes("UTF-8");
            dataOutputStream.writeInt(attrNameBytes.length);
            dataOutputStream.write(attrNameBytes);

            // Serialize the length and content of attrType
            byte[] attrTypeBytes = attrType.getBytes();
            dataOutputStream.writeInt(attrTypeBytes.length);
            dataOutputStream.write(attrTypeBytes);

        
              // Serialize isNotNull, isPK, and isUN directly
              dataOutputStream.writeBoolean(isNotNull);
              dataOutputStream.writeBoolean(isPrimaryKey);
              dataOutputStream.writeBoolean(isUnique);

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

      // Deserialize a byte array into a record object
      public static AttributeSchema deserialize(ByteBuffer buffer) {
        // Read attribute details from the buffer
        int length = buffer.getInt();
        byte[] stringBytes = new byte[length];
        buffer.get(stringBytes);
        String attrName =  new String(stringBytes);
        String attrType = readString(buffer);
        boolean isNotNull = buffer.get() != 0;
        boolean isPK = buffer.get() != 0;
        boolean isUN = buffer.get() != 0;

        return new AttributeSchema(attrName, attrType, isNotNull, isPK, isUN);
    }

    private static String readString(ByteBuffer buffer) {
        int length = buffer.getInt();
        byte[] stringBytes = new byte[length];
        buffer.get(stringBytes);
        return new String(stringBytes);
    }

    public String prettyPrint() {
        return String.format("%10s", attrName);
    }

    @Override
    public String toString() {
        return "\n\t\tAttributeSchema{" +
                "\n\t\t\tAttribute Name='" + attrName + '\'' +
                "\n\t\t\tAttribute Type='" + attrType + '\'' +
                "\n\t\t\tNot Null=" + isNotNull +
                "\n\t\t\tPrimary Key=" + isPrimaryKey +
                "\n\t\t\tUnique=" + isUnique +
                "\n\t\t}";
    }
}



