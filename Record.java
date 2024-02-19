import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.lang.*;


public class Record {
    private ArrayList<Object> values;

    public Record() {
        this.values = new ArrayList<>();
    }

    public Record(ArrayList<Object> newValues){
        this.values = newValues;
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
  
    private String getAttributeType(int attributeIndex) {
        // Implement this based on your Catalog class
        // Example: return Catalog.getAttributeType(attributeName);
        return "String"; // Replace with actual implementation
    }



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

    public byte[] serialize() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(values);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

      // Deserialize a byte array into a record object
    public static Record deserialize(byte[] data) {
        Record record = new Record();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            record.values = (ArrayList<Object>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return record;
    }

    // Alternate toString method if objects need to be specified
    public String toString(String tableName) {
        
        TableSchema tableSchema = Catalog.getTableSchema(tableName);

        ArrayList<AttributeSchema> attributeSchemas = tableSchema.getAttributeSchema();
        
        String output = "( ";

        for ( int i = 0; i < attributeSchemas.size(); i++) {
            String type = attributeSchemas.get(i).getType();
            Object value = values.get(i);

            if (type.equals("Integer")) {
                Integer n = (Integer) value;
                output = output + n.toString();
            }
            else if (type.equals("String")) {
                String s = (String) value;
                output = output + s;
            }
            else if (type.equals("Char")) {
                char c = (char) value;
                output = output + c;
            }
            output = output + " ";
        }
        output = output + ")";
        return output;
    }

    @Override
    public String toString() {
        String str = "(";
        for (int i = 0; i < this.values.size(); i++) {
            str += this.values.get(i);
            if (i < this.values.size() - 1) {
                str += " ";
            }
        }
        return str += ")";
    }
}