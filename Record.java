import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;


public class Record {
    private ArrayList<Object> values;

    public Record() {
        this.values = new ArrayList<>();
    }

    public Record(ArrayList<Object> newValues){
        this.values = newValues;
    }


    public void modifyAttribute(Object newValue, int index){
        this.values.set(index, newValue);
    }
    // Set the value of an attribute
    public void setAttribute(Object value) {
        values.add(value);
    }

    // Get the value of an attribute
    public Object getAttribute(int index) {
        return values.get(index);
    }

    // Get the value of an attribute
    public Object deleteAttribute(int index) {
        return values.remove(index);
    }
    // }

    // Calculate the size of the record in bytes
    public int calculateRecordSize() {
        int size = 0;

        // Add overhead for attribute names list and attribute values list
        size += calculateListSize(values);

        // Add sizes of attribute names and their corresponding values
        for (Object attribute: values) {
            size += calculateObjectSize(attribute);
        }

        return size;
    }

    public ArrayList<Object> getValues(){
        return this.values;
    }

    // Helper method to calculate the size of a list
    private int calculateListSize(ArrayList<Object> list) {
        int size = 4; // For the list size (integer is 4 bytes)
        for (Object obj : list) {
            size += calculateObjectSize(obj);
        }
        return size;
    }
    // Helper method to calculate the size of an object (if it's serializable)
    private int calculateObjectSize(Object obj) {
        if (obj == null) {
            return 0;
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.size();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public byte[] serialize(String tableName) {
        ArrayList<AttributeSchema> attributes = Catalog.getTableSchema(tableName).getAttributeSchema();

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(bos);) {

                    // For each attribute we need to know its type before writing to hardware
                    for(int i = 0; i < attributes.size(); i++){
                        String type = attributes.get(i).getType();
                        // Now write the bytes depending on what the type is
                        if(type.equals("integer")){
                            dataOutputStream.writeInt((Integer) values.get(i));
                        }
                        else if(type.startsWith("varchar")){
                            // Convert object to string, write how many bytes it is and write the string
                            String value = (String) values.get(i);
                            dataOutputStream.writeInt(value.length());
                            dataOutputStream.write(value.getBytes("UTF-8"));
                        }
                        else if(type.startsWith("char")){
                            String value = (String) values.get(i);
                            dataOutputStream.write(value.getBytes("UTF-8"));
                        }
                        else if(type.equals("double")){
                            dataOutputStream.writeDouble((Double) values.get(i));
                        }
                        else if(type.equals("boolean")){
                            dataOutputStream.writeBoolean((boolean) values.get(i));
                        }
                    }
                    return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

      // Deserialize a byte array into a record object
    public static Record deserialize(ByteBuffer buffer, String tableName) {
        ArrayList<AttributeSchema> attributes = Catalog.getTableSchema(tableName).getAttributeSchema();
        ArrayList<Object> values = new ArrayList<>();

        for(int i = 0; i < attributes.size(); i++){
            String type = attributes.get(i).getType();
                        // Now write the bytes depending on what the type is
                        if(type.equals("integer")){
                            Integer attr = buffer.getInt();
                            values.add(attr);
                        }
                        else if(type.startsWith("varchar")){
                            // Get length of the varchar
                            int length = buffer.getInt();
                            // Read the varchar in
                            byte[] stringBytes = new byte[length];
                            buffer.get(stringBytes);
                            // Make it a string
                            String attr =  new String(stringBytes);
                            values.add(attr);
                        }
                        else if(type.startsWith("char")){
                            // Get the size of char
                            int numberOfChars = Integer.parseInt(type.substring(type.indexOf("(")+1, type.indexOf(")")));
                            byte[] stringBytes = new byte[numberOfChars];
                            buffer.get(stringBytes);
                            // Get the string
                            String attr =  new String(stringBytes);
                            values.add(attr);
                        }
                        else if(type.equals("double")){
                            Double attr = buffer.getDouble();
                            values.add(attr);
                        }
                        else if(type.equals("boolean")){
                            boolean attr = buffer.get() != 0;
                            values.add(attr);
                        }
        }
 
         return new Record(values);
        
    }

    // Alternate toString method if objects need to be specified
    public String toString(String tableName) {
        
        TableSchema tableSchema = Catalog.getTableSchema(tableName);

        ArrayList<AttributeSchema> attributeSchemas = tableSchema.getAttributeSchema();
        
        String output = "( ";

        for ( int i = 0; i < attributeSchemas.size(); i++) {
            String type = attributeSchemas.get(i).getType();
            Object value = values.get(i);

            if (type.equals("integer")) {
                Integer n = (Integer) value;
                output = output + n;
            }
            else if (type.startsWith("varchar")) {
                String s = (String) value;
                output = output + s;
            }
            else if (type.startsWith("char")) {
                String c = (String) value;
                output = output + c;
            }
            else if (type.equals("double")) {
                double d = (double) value;
                output = output + d;
            }
            else if (type.equals("boolean")) {
                boolean b = (boolean) value;
                output = output + b;
            }
            output = output + " ";
        }
        output = output + ")";
        return output;
    }
}