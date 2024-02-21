import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
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
            // Set buffer to position of the right page
            long position = indexList.get(pageNumber) * Main.pageSize;
            // Allocate a ByteBuffer to hold the page data
            ByteBuffer buffer = ByteBuffer.allocate(Main.pageSize);

            // Read the page data from the file into the buffer
            fileChannel.read(buffer, position);

            // Convert the ByteBuffer to a byte array
            byte[] pageData = buffer.array();

            // Create and return a new Page object
            Page page = Page.deserialize(pageData, tableName, pageNumber);
            if (page != null) {
                return page;
            }
            throw new IllegalArgumentException("Error reading from table: " + tableName);
        } catch (IOException e) {
            // Handle the exception (e.g., log it or throw a runtime exception)
            e.printStackTrace();
            return null;
        }
    }

    public static void insert(String tableName, Record record, TableSchema tableSchema) {


        
    }

    public static void writePageToDisk(Page page) {
        System.out.println("Writing page " + page.getPageNumber() + " to disk: " + page);

        try {
            File file = new File(Main.databaseLocation + page.getTableName());
            
            // Create the file if it doesn't exist
            if (!file.exists()) {
                System.out.println("Error: Table: " + page.getTableName() + " does not exist");
            }
            FileOutputStream fos = new FileOutputStream(file, true);
       
            // Calculate the offset where this page should be written
            TableSchema tableSchema = Catalog.getTableSchema(page.getTableName());
            ArrayList<Integer> indexList = tableSchema.getIndexList();
            //Integer pageIndex = ;
            long offset = indexList.get(page.getPageNumber()) * Main.pageSize;
            // Move the file pointer to the correct position
            fos.getChannel().position(offset);
            // Write the page data to the file
            fos.write(page.serialize());
            fos.close();
        } catch (IOException e) {
            // Handle the exception (e.g., log it or throw a runtime exception)
            e.printStackTrace();
        }

    }

    public static void writeTableToDisk (String tableName) {

        // Create actual table file (they are stored as files)

        try {
            Path folder = Paths.get(Main.databaseLocation);
    
            // Create the file within the folder
            Path filePath = folder.resolve(tableName);
    
            // Write content to the file
            byte[] bytes = ByteBuffer.allocate(4).putInt(0).array();
        
            Files.write(filePath, bytes);
        } catch (Exception e) {
            System.out.println("Error writing table: " + tableName + " to hardware.");
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

        } else {
            System.err.println("Table: " + tableName + "does not exist");
        }
    }
}
