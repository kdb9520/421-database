import java.util.HashMap;
import java.util.Map;

public class Record {
    private Map<String, String> attributes;

    public Record() {
        this.attributes = new HashMap<>();
    }

    // Set the value of an attribute
    public void setAttribute(String attributeName, String value) {
        attributes.put(attributeName, value);
    }

    // Get the value of an attribute
    public String getAttribute(String attributeName) {
        return attributes.get(attributeName);
    }

    // Get the value of an attribute
    public String deleteAttribute(String attributeName) {
        return attributes.remove(attributeName);
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

    public static void main(String[] args) {
        // Example usage
        Record record = new Record();

        // Add attributes to the record
        record.setAttribute("Name", "John");
        record.setAttribute("LastName", "Doe");
        record.setAttribute("Age", "25");
        record.setAttribute("Address", "123 Main St");

        // Set a new value for the "LastName" attribute
        record.setAttribute("LastName", "Smith");

        // Display the values of all attributes
        for (Map.Entry<String, String> entry : record.attributes.entrySet()) {
            System.out.println("Attribute: " + entry.getKey() + ", Value: " + entry.getValue());
        }

        // Display the total size of the record
        System.out.println("Total Record Size: " + record.calculateRecordSize() + " bytes");
    }
}