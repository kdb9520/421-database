import java.util.ArrayList;
import java.util.LinkedList;

public class Table {

    String name;
    int numPages;
    LinkedList pages;
    Attribute primaryKey;
    ArrayList<Attribute> attributes;
    
    public Table(String name) {
        this.name = name;
        this.numPages = 0;
        this.pages = new LinkedList<>();
    }

    public boolean insert(Record record) {

        // if there are no pages
        if (this.numPages == 0) {
            // make a new file for the table

            // add this entry to a new page

            // insert the page into the table file

        }
        
        return false;
    }

    public void dropTable() {
        // erase the table from hardware
    }

    /**
     * Given an attribute's name, drops that attribute and all its data from the table
     * @param a_name name of the attribute to drop from the table
     * @return void
     */
    public void dropAttribute(String a_name) {
        // remove an attribute and its data from the table
        for (Attribute a : attributes) {
            if (a.getName().equals(a_name)) {
                attributes.remove(a);
                break;
            }
        }

        // remove its data from the table

    }

    public void addAttribute(Attribute a) {
        // add an attribute to the table
        this.attributes.add(a);

        // add the column to the pages
        
    }

    public void alter() {
    }

    public void writeToHardware() {
    }

    private void createPage() {
    }

    private Byte[] convertToBinary() {
        return null;
    }

}
