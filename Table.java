import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    public String getName(){
        return this.name;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        // Write number of pages
        dataOutputStream.writeInt(this.numPages);

        // Write each page
        for (Page page : pages) {
            byte[] pageBytes = page.serialize();
            dataOutputStream.write(pageBytes);
        }

        dataOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static Page deserialize(byte[] data) throws IOException {
              return null;
    }

}
