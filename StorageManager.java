import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class StorageManager {
    public static Catalog catalog;                  // private instance of Catalog, accessible by static methods

    private StorageManager() {
       
    }


    public static Page readPageFromDisk(String tableName, int pageNumber) {
        try (FileChannel fileChannel = FileChannel.open(Paths.get(tableName), StandardOpenOption.READ)) {
            // Calculate the position in the file where the page starts
            TableSchema tableSchema = Catalog.getTableSchema(tableName);
            ArrayList<Integer> indexList = tableSchema.getIndexList();
            long position = indexList.get(pageNumber) * Page.PAGE_SIZE;
            // Allocate a ByteBuffer to hold the page data
            ByteBuffer buffer = ByteBuffer.allocate(Page.PAGE_SIZE);

            // Read the page data from the file into the buffer
            fileChannel.read(buffer, position);

            // Convert the ByteBuffer to a byte array
            byte[] pageData = buffer.array();

            // Create and return a new Page object
            return new Page(tableName, pageNumber, pageData);
        } catch (IOException e) {
            // Handle the exception (e.g., log it or throw a runtime exception)
            e.printStackTrace();
            return null;
        }
    }

    public static void insert(int numPages, String tableName, Record record, TableSchema tableSchema, ArrayList<Integer> pageIndexList) {
        if (numPages == 0) {
            Page newPage = BufferManager.createPage(tableName, 0);
            // add this entry to a new page
            newPage.addRecord(record);
        } else {
            // Get the primary key and its type so we can compare

            // Get primary key col number
            int primaryKeyCol = tableSchema.findPrimaryKeyColNum();
            String primaryKeyType = tableSchema.getPrimaryKeyType();
            // Loop through pages and find which one to insert record into.
            Page next = null;
            for (int i = 0; i < numPages; i++) {
                // See if we are out of bounds
                if (i + 1 >= numPages) {
                    break;
                }
                next = BufferManager.getPage(tableName, i + 1);

                // If its less than the first value of next page (i+1) then it belongs to page i
                // Type cast appropiately then compare records
                if (primaryKeyType.equals("Integer")) {
                    if ((Integer) record.getAttribute(primaryKeyCol) < (Integer) next.getFirstRecord(i)) {
                        // Add the record to the page. Check if it split page or not
                        Page result = BufferManager.getPage(tableName, i).addRecord(record);

                        // If we split then add the new page to our page list.
                        if (result != null) {
                            // Add new page to our page
                            pageIndexList.add(i + 1, numPages);
                            BufferManager.addPageToBuffer(result);
                            // Go in and update page number of all pages current in here
                            // Update our pageIndexList, and update pageNumber every Page after this page

                            return;
                        }
                        return;
                    }
                } else if (primaryKeyType.equals("String")) {

                    if (record.getAttribute(primaryKeyCol).toString()
                            .compareTo(next.getFirstRecord(i).toString()) <= 0) {
                        // Add the record to the page. Check if it split page or not
                        Page result = BufferManager.getPage(tableName, i).addRecord(record);
                        // If we split then add the new page to our page list.
                        if (result != null) {
                            pageIndexList.add(i + 1, numPages);
                            BufferManager.addPageToBuffer(result);
                            return;
                        }
                        return;
                    }
                } else if (primaryKeyType.equals("Char")) {
                    if ((char) record.getAttribute(primaryKeyCol) < (char) next.getFirstRecord(i)) {
                        // Add the record to the page. Check if it split page or not
                        Page result = BufferManager.getPage(tableName, i).addRecord(record);
                        // If we split then add the new page to our page list.
                        if (result != null) {
                            pageIndexList.add(i + 1, numPages);
                            BufferManager.addPageToBuffer(result);
                            return;
                        }
                        return;
                    }
                }

            }
            // If we make it here then the record belongs in a new page at the end of the
            // list.
            // Create page
            Page newPage = BufferManager.createPage(tableName, numPages);
            // Add record to page
            newPage.addRecord(record);
            pageIndexList.add(numPages);
        }
    }

    public static void writePageToDisk(Page page) {
        System.out.println("Writing page " + page.getPageNumber() + " to disk: " + page);

        try {
            File file = new File(Main.databaseLocation + page.getTableName());
            
            // Create the file if it doesn't exist
            if (!file.exists()) {
                file.createNewFile();
            }

        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            // Calculate the offset where this page should be written
            TableSchema tableSchema = Catalog.getTableSchema(page.getTableName());
            ArrayList<Integer> indexList = tableSchema.getIndexList();
            //Integer pageIndex = ;
            long offset = indexList.get(page.getPageNumber()) * Page.PAGE_SIZE;
            // Move the file pointer to the correct position
            fos.getChannel().position(offset);
            // Write the page data to the file
            fos.write(page.serialize());
        } catch (IOException e) {
            // Handle the exception (e.g., log it or throw a runtime exception)
            e.printStackTrace();
        }
    } catch (IOException e) {
        // Handle the exception (e.g., log it or throw a runtime exception)
        e.printStackTrace();
    }
    }

 
        

    public static int readNumberOfPages(String tableName) {
        System.out.println("Getting number of pages from table " + tableName);
        final int INTEGER_SIZE = 4; // Assuming an integer is 4 bytes
        try (FileChannel fileChannel = FileChannel.open(Paths.get(Main.databaseLocation, tableName), StandardOpenOption.READ)) {

            // Allocate a ByteBuffer to hold the integer data
            ByteBuffer buffer = ByteBuffer.allocate(INTEGER_SIZE);

            // Read an integer from the buffer
            fileChannel.read(buffer, 0);
            
            // Reset the position to read from the beginning of the buffer
            buffer.rewind();

            // Return the integer value
            return buffer.getInt();

        } catch (IOException e) {
            // Handle the exception (e.g., log it or throw a runtime exception)
            e.printStackTrace();
            // Return a default value or handle the error as appropriate for your application
            return -1; // You might want to choose an appropriate default value
        }
    }

    public static void deleteTable(String tableName){
        File file = new File(Main.databaseLocation + tableName);
        if(!file.exists()) {
            System.err.println("File does not exist");
            return;
        }

        file.delete();
        System.out.println("File deleted successfully");
    }

    public static void select(TableSchema tableSchema, String tableName) {
        if (tableSchema != null) {
            // need to test formating of toStrings
            System.out.println(tableSchema.toString());

            // Print all values in table
            // Loop through the table and print each page
            // For each page in table tableName
            int num_pages = tableSchema.getIndexList().size();
            for(int i = 0; i < num_pages; i++){
                Page page = BufferManager.getPage(tableName, i);
                System.out.println(page.toString());
            }

            System.out.println(tableSchema.table.toString());
        } else {
            System.err.println("Table: " + tableName + "does not exist");
        }
    }
}
