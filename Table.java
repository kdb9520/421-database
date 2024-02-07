import java.util.ArrayList;
import java.util.LinkedList;

public class Table {

    String name;
    int numPages;
    LinkedList<Page> pages;
    TableSchema schema;
    
    public Table(String name) {
        this.name = name;
        this.numPages = 0;
        this.pages = new LinkedList<>();
        this.schema = new TableSchema();
    }

    /**
     * inserts a record into the table
     * @param record the record to insert
     */
    public void insert(Record record) {

        // if there are no pages
        if (this.numPages == 0) {
            // make a new file for the table

            // add this entry to a new page

            // insert the page into the table file

        }
        
    }

    public void dropTable() {
        // erase the table from hardware
    }

    /**
     * Given an attribute's name, drops that attribute and all its data from the table
     * @param a_name name of the attribute to drop from the table
     */
    public void dropAttribute(String a_name) {
        // remove an attribute and its data from the table
        this.schema.dropAttribute(a_name);

        // remove its data from the table
        for (Page page : this.pages) {
            page.dropAttribute(a_name);
        }

    }

    /**
     * Given an Attribute, add it to the table
     * @param a an Attribute
     */
    public void addAttribute(Attribute a) {
        // add an attribute to the table
        this.schema.addAttribute(a);

        // add the column to the pages
        
    }

    // public void writeToHardware() {
    // }

    // private void createPage() {
    // }

    // private Byte[] convertToBinary() {
    //     return null;
    // }

}
