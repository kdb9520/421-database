import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Record<E> {
    private ArrayList<E> values;

    public Record() {
        this.values = new ArrayList<>();
    }

    // Set the value of an attribute
    public void addAttribute(E value) {
        values.add(value);
    }

    // Get the value of an attribute
    public String getAttribute(String attributeName) {
        int index = tableSchema.findAttribute(attributeName);
        return values.get(index);
    }

    // Get the value of an attribute
    public void dropAttribute(int i) {
        this.values.remove(i);
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