import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.AttributedCharacterIterator.Attribute;
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
        AttributeSchema primaryKeySchema = attributes.get(findPrimaryKeyColNum());
        return primaryKeySchema.attrType;
    }

    public AttributeSchema findAttributeSchema(int attr){
        return this.attributes.get(attr);
    }

    // Serialize in format [numAttributes,[attr1][attr2][attr....n]]
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        // Write number of pages


        // Write each page
        for (AttributeSchema attribute : attributes) {
            byte[] attribute_bytes = attribute.serialize();
            dataOutputStream.write(attribute_bytes);
        }

        dataOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

      // Deserialize a byte array into a TableSchema object
    public static TableSchema deserialize(byte[] data) {
        return null;
    }
}

