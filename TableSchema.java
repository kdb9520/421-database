import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class TableSchema {

    String tableName;
    int tableNumber;
    ArrayList<AttributeSchema> attributes;
    ArrayList<Integer> pageIndexes;

    public TableSchema(String tableName, ArrayList<AttributeSchema> attributes) {
        this.tableName = tableName;
        this.pageIndexes = new ArrayList<>();
        this.attributes = attributes;
    }

    public TableSchema(TableSchema old){
        this.tableName = old.tableName;
        this.tableNumber = old.tableNumber;
        this.attributes = old.attributes;
        this.pageIndexes = old.pageIndexes;


    }
    public TableSchema(ArrayList<AttributeSchema> attributeList, ArrayList<Integer> pageIndexes, String tableName) {
        this.attributes = attributeList;
        this.pageIndexes = pageIndexes;
        this.tableName = tableName;
    }

    public void dropAttribute(String attrName) {
        // remove an attribute and its data from the table
        int i = findAttribute(attrName);
        this.attributes.remove(i);
    }

    public void addAttribute(AttributeSchema a) {
        this.attributes.add(a);
        
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
            return primaryKeySchema.attrType.strip().split("[(]")[0];
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
        dataOutputStream.writeInt(attributes.size());

        // Write each attribute
        for (AttributeSchema attribute : attributes) {
            byte[] attribute_bytes = attribute.serialize();
            dataOutputStream.write(attribute_bytes);
        }
        // Write the number of pages
            if(!pageIndexes.isEmpty()){
                dataOutputStream.writeInt(pageIndexes.size());

            // Write each attribute
            for (Integer pageIndex : pageIndexes) {
                dataOutputStream.writeInt(pageIndex);
            }
        }
        else{
            dataOutputStream.writeInt(0);
        }
        

        dataOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

      // Deserialize a byte array into a TableSchema object
    public static TableSchema deserialize(byte[] data, String tableName) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        // Read the number of attributes
        int numAttributes = buffer.getInt();

        // Read each attribute
        ArrayList<AttributeSchema> attributeList = new ArrayList<>();
        ArrayList<Integer> pageIndexList = new ArrayList<>();

        for (int i = 0; i < numAttributes; i++) {
            AttributeSchema attribute = AttributeSchema.deserialize(buffer);
            attributeList.add(attribute);
        }

          // Read the number of attributes
          int numPages = buffer.getInt();
          for (int i = 0; i < numPages; i++) {
            Integer pageIndex = buffer.getInt();
            pageIndexList.add(pageIndex);
        }
        return new TableSchema(attributeList,pageIndexList,tableName);
    }

    public ArrayList<Integer> getIndexList(){
        if(this.pageIndexes.isEmpty()){
            return null;
        }
        return this.pageIndexes;
    }

    public void updateIndexList(ArrayList<Integer> pageIndexes){
        this.pageIndexes = pageIndexes;
    }

    @Override
    public String toString() {
        return "TableSchema{" +
                "attributes=" + attributes +
                '}';
    }
}

