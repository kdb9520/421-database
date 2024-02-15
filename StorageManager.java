import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


// refactored to new file by Jaron Cummings
// written by Kyle
class StorageManager {
    private String databaseFileName; // Name of the database file

    public StorageManager(String databaseFileName) {
        this.databaseFileName = databaseFileName;
    }


    public Page readPageFromDisk(int pageNumber) {
        try (FileChannel fileChannel = FileChannel.open(Paths.get(databaseFileName), StandardOpenOption.READ)) {
            // Calculate the position in the file where the page starts
            long position = (long) pageNumber * Page.PAGE_SIZE;

            // Allocate a ByteBuffer to hold the page data
            ByteBuffer buffer = ByteBuffer.allocate(Page.PAGE_SIZE);

            // Read the page data from the file into the buffer
            fileChannel.read(buffer, position);

            // Convert the ByteBuffer to a byte array
            byte[] pageData = buffer.array();

            // Create and return a new Page object
            return new Page(pageNumber, pageData);
        } catch (IOException e) {
            // Handle the exception (e.g., log it or throw a runtime exception)
            e.printStackTrace();
            return null;
        }
    }

    public void writePageToDisk(int pageNumber, Page page) {
        // Write page to disk (mock implementation)
        // In a real scenario, this method would write the page data to disk
        // For simplicity, we just print the page data here
        System.out.println("Writing page " + pageNumber + " to disk: " + page);

        try (FileOutputStream fos = new FileOutputStream("database.db", true)) {
            // Calculate the offset where this page should be written
            long offset = pageNumber * Page.PAGE_SIZE;
            // Move the file pointer to the correct position
            fos.getChannel().position(offset);
            // Write the page data to the file
            fos.write(page.serialize());
        } catch (IOException e) {
            // Handle the exception (e.g., log it or throw a runtime exception)
            e.printStackTrace();
        }
    }

    public void writeTableToDisk(Table table) {
        // Write page to disk (mock implementation)
        // In a real scenario, this method would write the page data to disk
        // For simplicity, we just print the page data here
        System.out.println("Writing table " + table + " to disk");

        try (FileOutputStream fos = new FileOutputStream(table.getName(), true)) {

        } catch (IOException e) {
            // Handle the exception (e.g., log it or throw a runtime exception)
            e.printStackTrace();
        }
    }

}
