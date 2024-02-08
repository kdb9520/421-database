import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Record {
    private ArrayList<String> values;
    TableSchema tableSchema = new TableSchema();

    public Record() {
        this.values = new ArrayList<>();
    }

    // Set the value of an attribute
    public void setAttribute(String value) {
        values.add(value);
    }

    // Get the value of an attribute
    public String getAttribute(String attributeName) {
        int index = tableSchema.findAttribute(attributeName);
        return values.get(index);
    }

    // Get the value of an attribute
    public String deleteAttribute(String attributeName) {
        int index = tableSchema.findAttribute(attributeName);
        if (index != -1){
            return values.remove(index);
        }
        return null;
    }

    // Calculate the total size of the record
    public int calculateRecordSize() {
        int totalSize = 0;
        for (String value : attributes.values()) {
            // Assuming each character takes 2 bytes for simplicity
            totalSize += value.length() * 2;
        }
        return totalSize;
    }

    public byte[] serialize() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'serialize'");
    }

    public static Record deserialize(byte[] recordBytes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deserialize'");
    }
}