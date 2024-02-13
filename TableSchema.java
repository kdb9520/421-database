import java.util.ArrayList;

public class TableSchema {

    String tableName;
    int tableNumber;
    Table table;
    ArrayList<AttributeSchema> attributes;

    public TableSchema(String tableName, ArrayList<AttributeSchema> attributes) {
        this.tableName = tableName;
        this.table = new Table(tableName);
        this.attributes = attributes;
    }

    public void dropAttribute(String attrName) {
        // remove an attribute and its data from the table
        int i = findAttribute(attrName);
        this.table.dropAttribute(i);
        this.attributes.remove(i);
    }

    public void addAttribute(AttributeSchema a) {
        this.attributes.add(a);

        if (a.getDefaultValue() != null) {
            this.table.addAttribute(a.getDefaultValue());
        }
        
    }

    public int findAttribute(String attrName){
        for (int i = 0; i < attributes.size(); i++){
            if (attrName.equals(attributes.get(i).attrName)){
                return i;
            }
        }
        return -1;
    }
}
