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

    public Table(String name) {
        this.name = name;
        this.numPages = 0;
        this.pages = new LinkedList<>();
    }

    /**
     * inserts a record into the table
     * 
     * @param record the record to insert
     */
    public void insert(Record record) {

        // if there are no pages
        if (this.numPages == 0) {
            Page newPage = BufferManager.createPage(this.name, 0);
            // add this entry to a new page
            Page result = newPage.addRecord(record);
            // Add new page to the Table
            this.pages.add(result);
        } else {
            // Get the primary key and its type so we can compare
            TableSchema tableSchema = Catalog.getTableSchema(this.name);
            // Get primary key col number
            int primaryKeyCol = tableSchema.findPrimaryKeyColNum();
            String primaryKeyType = tableSchema.getPrimaryKeyType();
            // Loop through pages and find which one to insert record into.
            Page next = null;
            for (int i = 0; i < pages.size(); i++) {
                // See if we are out of bounds
                if (i + 1 >= pages.size()) {
                    break;
                }

                next = pages.get(i + 1);

                // If its less than the first value of next page (i+1) then it belongs to page i
                // Type cast appropiately then compare records
                if (primaryKeyType == "Integer") {
                    if ((Integer) record.getAttribute(primaryKeyCol) < (Integer) next.getFirstRecord(i)) {
                        // Add the record to the page. Check if it split page or not
                        Page result = pages.get(i).addRecord(record);
                        // If we split then add the new page to our page list.
                        if (result != null) {
                            pages.add(result);
                            return;
                        }
                        return;
                    }
                }

                else if (primaryKeyType == "String") {

                    if (record.getAttribute(primaryKeyCol).toString()
                            .compareTo(next.getFirstRecord(i).toString()) <= 0) {
                        // Add the record to the page. Check if it split page or not
                        Page result = pages.get(i).addRecord(record);
                        // If we split then add the new page to our page list.
                        if (result != null) {
                            pages.add(result);
                            return;
                        }
                        return;
                    }
                }

                else if (primaryKeyType == "Char") {
                    if ((char) record.getAttribute(primaryKeyCol) < (char) next.getFirstRecord(i)) {
                        // Add the record to the page. Check if it split page or not
                        Page result = pages.get(i).addRecord(record);
                        // If we split then add the new page to our page list.
                        if (result != null) {
                            pages.add(result);
                            return;
                        }
                        return;
                    }
                }

            }
            // If we make it here then the record belongs in a new page at the end of the
            // list.
            // Create page
            Page newPage = BufferManager.createPage(this.name, 0);
            // Add record to page
            newPage.addRecord(record);
            // Put page in out table list
            pages.add(newPage);

        }

    }

    public int getNumberOfPages() {
        return this.numPages;
    }

    public LinkedList<Page> getPages() {
        return this.pages;
    }

    public void dropTable() {
        // erase the table from hardware
    }

    /**
     * Given an attribute's name, drops that attribute and all its data from the
     * table
     * 
     * @param i index of the attribute to drop from the table
     */
    public void dropAttribute(int i) {

        // remove its data from the table
        for (Page page : this.pages) {
            page.dropAttribute(i);
        }

    }

    /**
     * Given an Attribute, add it to the table
     * 
     * @param value value to add to the records in the table
     */
    public void addAttribute(Object value) {
        // add an attribute to the table
        for (Page page : this.pages) {
            page.addAttribute(value);
        }
    }

    // public void writeToHardware() {
    // }

    // private void createPage() {
    // }

    // private Byte[] convertToBinary() {
    // return null;
    // }

    public String getName() {
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
    // Deserialize in format [num_pages,page1,page2,etc]
    public static Table deserialize(byte[] data, TableSchema schema) throws IOException {
        return null;
    }

    // need to test this page and record.
    @Override
    public String toString() {
        String tableString = "";
        for (Page page : pages) {
            tableString += page.toString();
        }
        return tableString;
    }

}
