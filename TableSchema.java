/**
 * @file:   TableSchema.java
 * @authors:    Kyle, Derek, Kellen, Jaron, Beckett
 * The TableSchema holds the information about the structure of the table.  It has a unique tableName and tableNumber,
 * along with a list of the AttributeSchemas for the attributes in the table and a list of the pageIndexes for the pages
 * holding the information in the table.
 */

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

    /**
     * Constructor for TableSchema.  Will take the name (unique) and a list of AttributeSchemas corresponding to the
     * attributes that will be in the table.  This will initialize an empty list of pageIndexes.
     * @param tableName String - name of the table (should be unique).
     * @param attributes ArrayList<AttributeSchema> - list of attributes in the table.
     */
    public TableSchema(String tableName, ArrayList<AttributeSchema> attributes) {
        this.tableName = tableName;
        this.pageIndexes = new ArrayList<>();
        this.attributes = attributes;
    }

    public TableSchema(TableSchema old){
        this.tableName = old.tableName;
        this.tableNumber = old.tableNumber;
        this.attributes = new ArrayList<>(old.attributes);
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
        if(attributes.get(i).isPrimaryKey){
            System.err.println("This column is a primary key, cannot be removed");
            return;
        }
        this.attributes.remove(i);
    }

    public void addAttribute(AttributeSchema a) {
        this.attributes.add(a);
        
    }

    /**
     * TableSchema.findAttribute(String attrName) takes in a string representing the attribute we want to find.
     * This method looks through the attributes in the TableSchema and will try to match the string to the name of an
     * attribute.  If no match is found this method returns -1.
     * @param attrName  Name of the attribute to find.
     * @return  int - index of the attribute; -1 if not found.
     */
    public int findAttribute(String attrName){
        attrName = attrName.substring(0, attrName.length() - 1);
        for (int i = 0; i < attributes.size(); i++){
            if (attrName.equals(attributes.get(i).attrName)){
                return i;
            }
        }
        return -1;
    }

    /**
     * TableSchema.findPrimaryKeyColNum() will search through the attributes associated with the instance of the
     * TableSchema, returning the index of the primary key when it is found.
     * @return  int - index of the primary key; -1 if not found.
     */
    public int findPrimaryKeyColNum(){
        for (int i = 0; i < attributes.size(); i++){
            if(attributes.get(i).isPrimaryKey){
                return i;
            }
        }
        return -1;
    }

    /**
     * TableSchema.getPrimaryKeyType() will find the primary key in the attributes if they exist.  It will then get the
     * type from the primary key AttributeSchema and return it.
     * @return String - type of the primary key; null if not found.
     */
    public String getPrimaryKeyType(){
        if (!this.attributes.isEmpty()) {
            AttributeSchema primaryKeySchema = attributes.get(findPrimaryKeyColNum());
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
        return this.pageIndexes;
    }



    public void updateIndexList(ArrayList<Integer> pageIndexes){
        this.pageIndexes = pageIndexes;
    }

    public void addToIndexList(Integer pageIndex){
        this.pageIndexes.add(pageIndex);
    }

    public void addToIndexList(Integer indexToInsertInto, Integer pageIndex){
        this.pageIndexes.add(indexToInsertInto, pageIndex);
    }

    public int getRecordSize(){
        // Get the size of a record
        return 0;
    }

    public String prettyPrint() {
        StringBuilder str = new StringBuilder();
        attributes.forEach(a -> str.append("|").append(a.prettyPrint()).append("|"));
        str.append(String.format("\n%" + str.length() + "s", " ").replace(" ", "-"));
        return str.toString();
    }

    @Override
    public String toString() {
        return "TableSchema{" +
                "\n\tName=" + this.tableName +
                "\n\tTable Number=" + this.tableNumber +
                "\n\tAttributes=" + this.attributes +
                "\n}";
    }
}

