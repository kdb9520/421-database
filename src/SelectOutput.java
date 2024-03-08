package src;

import java.util.ArrayList;

public class SelectOutput {

    ArrayList<AttributeSchema> attributes;
    ArrayList<Record> records;

    public SelectOutput(ArrayList<Record> records, ArrayList<AttributeSchema> attributes) {
        this.records = records;
        this.attributes = attributes;
    }

    public ArrayList<AttributeSchema> getAttributeSchemas() {
        return attributes;
    }

    public ArrayList<Record> getRecords() {
        return records;
    }

}
