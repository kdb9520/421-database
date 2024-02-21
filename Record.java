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

    public byte[] serialize(String tablename) {
        ArrayList<AttributeSchema> attributes = Catalog.getTableSchema(tablename).getAttributeSchema();

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(bos);) {

                    // For each attribute we need to know its type before writing to hardware
                    for(int i = 0; i < attributes.size(); i++){
                        String type = attributes.get(i).getType();
                        // Now write the bytes depending on what the type is
                    }
                    // Serialize the length and content of attrName
                    byte[] attrNameBytes = attrName.getBytes("UTF-8");
                    dataOutputStream.writeInt(attrNameBytes.length);
                    dataOutputStream.write(attrNameBytes);

                    // Serialize the length and content of attrType
                    byte[] attrTypeBytes = attrType.getBytes();
                    dataOutputStream.writeInt(attrTypeBytes.length);
                    dataOutputStream.write(attrTypeBytes);

                
                    // Serialize isNull, isPK, and isUN directly
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
    public static Record deserialize(ByteBuffer buffer) {
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

            if (type.equals("integer")) {
                int n = (int) value;
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