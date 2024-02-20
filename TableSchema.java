import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class TableSchema {

    String tableName;
    int tableNumber;
    Table table;
    ArrayList<AttributeSchema> attributes;

    public TableSchema(String tableName, ArrayList<AttributeSchema> attributes) {
        this.tableName = tableName;
        this.table = new Table(tableName);
        this.attributes = attributes;
    }

    public TableSchema(ArrayList<AttributeSchema> attributeList) {
        this.attributes = attributeList;
    }

    public void dropAttribute(String attrName) {
        // remove an attribute and its data from the table
        int i = findAttribute(attrName);
        this.table.dropAttribute(i);
        this.attributes.remove(i);
    }

    public void addAttribute(AttributeSchema a) {
        this.attributes.add(a);

        if (a.getDefaultValue() != null) {
            this.table.addAttribute(a.getDefaultValue());
        }
        
    }

    public int findAttribute(String attrName){
        for (int i = 0; i < attributes.size(); i++){
            if (attrName.equals(attributes.get(i).attrName)){
                return i;
            }
        }
        return -1;
    }

    public int findPrimaryKeyColNum(){
        for (int i = 0; i < attributes.size(); i++){
            if(attributes.get(i).isPrimaryKey){
                return i;
            }
        }
        return -1;
    }

    public String getPrimaryKeyType(){
        if (this.attributes.size() > 0) {
            AttributeSchema primaryKeySchema = attributes.get(findPrimaryKeyColNum());
            //todo - removed regex because it seemed unnecessary, unless it was for varchar or char?
            return primaryKeySchema.attrType.strip().split("(")[0];
        } 
        return null;
    }

    public String getTableName(){
        return this.tableName;
    }

    public AttributeSchema findAttributeSchema(int attr){
        return this.attributes.get(attr);
    }

    public ArrayList<AttributeSchema> getAttributeSchema() {
        return attributes;
    }

    // Serialize in format [numAttributes,[attr1][attr2][attr....n]]
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        // Write the attribute number
        dataOutputStream.write(attributes.size());

        // Write each attribute
        for (AttributeSchema attribute : attributes) {
            byte[] attribute_bytes = attribute.serialize();
            dataOutputStream.write(attribute_bytes);
        }

        dataOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

      // Deserialize a byte array into a TableSchema object
    public static TableSchema deserialize(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        // Read the number of attributes
        int numAttributes = buffer.getInt();

        // Read each attribute
        ArrayList<AttributeSchema> attributeList = new ArrayList<>();
        for (int i = 0; i < numAttributes; i++) {
            AttributeSchema attribute = AttributeSchema.deserialize(buffer);
            attributeList.add(attribute);
        }

        return new TableSchema(attributeList);
    }

    @Override
    public String toString() {
        return "TableSchema{" +
                "attributes=" + attributes +
                '}';
    }
}

