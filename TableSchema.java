import java.util.ArrayList;

import org.w3c.dom.Attr;

public class TableSchema {

    Attribute primaryKey;
    ArrayList<Attribute> attributes;

    public TableSchema() {
        this.primaryKey = null;
        this.attributes = null;
    }

    public void dropAttribute(String a_name) {
        // remove an attribute and its data from the table
        for (Attribute a : attributes) {
            if (a.getName().equals(a_name)) {
                attributes.remove(a);
                break;
            }
        }
    }

    public void addAttribute(Attribute a) {
        if (a.isPrimaryKey) {
            if (this.primaryKey == null) {
                this.primaryKey = a;
            } else {
                // break with an error
            }
        }

        this.attributes.add(a);
    }

    public int findAttribute(String attrName){
        for (int i = 0; i < attributes.size(); i++){
            if (attrName.equals(attributes.get(i).a_name)){
                return i;
            }
        }
        return -1;
    }
}
