import java.util.HashMap;
import java.util.Map;

public class Record {
    private Map<Attribute, String> attributes;

    public Record() {
        this.attributes = new HashMap<>();
    }

    // Set the value of an attribute
    public void setAttribute(Attribute attributeName, String value) {
        attributes.put(attributeName, value);
    }

    // Get the value of an attribute
    public String getAttribute(Attribute attributeName) {
        return attributes.get(attributeName);
    }

    // Get the value of an attribute
    public String deleteAttribute(String attributeName) {
        for (Attribute a : attributes.keySet()) {
            if (attributeName.equals(a.a_name)){
               return attributes.remove(a);
            }
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
}