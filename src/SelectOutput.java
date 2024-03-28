package src;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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


    public void orderBy(String attr, String order){
        int pos = -1;
        for(int i = 0; i < this.attributes.size(); i ++){
            if(this.attributes.get(i).attrName.equals(attr))
                pos = i;
        }
        if(pos == -1){
            System.err.println("Invalid attribute to order on");
            return;
        }
        String type = this.attributes.get(pos).getType();
        type = type.split("\\(")[0];
        Record.setType(type);
        Record.setOrderToCompare(order);
        Record.setColToCompare(pos);
        Collections.sort(records);


    }




}
