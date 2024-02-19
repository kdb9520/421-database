import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class StorageManager {
    public static final String db_loc = "";
    public static Catalog catalog;                  // private instance of Catalog, accessible by static methods

    private StorageManager() {
       
    }


    public static Page readPageFromDisk(String tableName, int pageNumber) {
        try (FileChannel fileChannel = FileChannel.open(Paths.get(tableName), StandardOpenOption.READ)) {
            // Calculate the position in the file where the page starts
            long position = (long) pageNumber * Page.PAGE_SIZE;
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

    public static void writePageToDisk(Page page) {
        System.out.println("Writing page " + page.getPageNumber() + " to disk: " + page);

        try {
            File file = new File(db_loc + page.getTableName());
            
            // Create the file if it doesn't exist
            if (!file.exists()) {
                file.createNewFile();
            }

        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            // Calculate the offset where this page should be written
            long offset = page.getPageNumber() * Page.PAGE_SIZE;
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
        try (FileChannel fileChannel = FileChannel.open(Paths.get(db_loc, tableName), StandardOpenOption.READ)) {

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
        File file = new File(db_loc + tableName);
        if(!file.exists()) {
            System.err.println("File does not exist");
            return;
        }

        file.delete();
        System.out.println("File deleted successfully");
    }
}
