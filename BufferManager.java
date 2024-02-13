
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class BufferManager {
    private Map<Integer, Page> bufferPool; // Map page number to page data
    private int bufferSize; // Size of buffer pool
    private DiskManager diskManager; // A hypothetical class for disk operations

    public BufferManager(int bufferSize, DiskManager diskManager) {
        this.bufferSize = bufferSize;
        this.bufferPool = new HashMap<>();
        this.diskManager = diskManager;
    }

    public Page getPage(int pageNumber) {
        if (bufferPool.containsKey(pageNumber)) {
            // Page is already in buffer pool
            return bufferPool.get(pageNumber);
        } else {
            // Page is not in buffer pool, so read it from disk
            Page page = diskManager.readPageFromDisk(pageNumber);
            if (bufferPool.size() >= bufferSize) {
                // Buffer pool is full, evict a page using some policy (e.g., LRU)
                evictPage();
            }
            // Add the new page to buffer pool
            bufferPool.put(pageNumber, page);
            return page;
        }
    }

    public void writePage(Page page) {
        int pageNumber = page.getPageNumber();
        if (bufferPool.containsKey(pageNumber)) {
            // Page is already in the buffer pool, update its data
            bufferPool.put(pageNumber, page);
        } else {
            // Page is not in the buffer pool, add it
            if (bufferPool.size() >= bufferSize) {
                // Buffer pool is full, evict a page using some policy (e.g., LRU)
                evictPage();
            }
            bufferPool.put(pageNumber, page);
        }
    }

    private void evictPage() {
        // Implementation of page eviction policy (e.g., LRU)
        // For simplicity, this example just removes the first page in the buffer pool
        int firstPageNumber = bufferPool.keySet().iterator().next();
        Page removedPage = bufferPool.get(firstPageNumber);
        diskManager.writePageToDisk(firstPageNumber, removedPage);
        bufferPool.remove(firstPageNumber);
    }

    public void purgeBuffer() {
        // Iterate through all entries in the buffer pool
        for (Map.Entry<Integer, Page> entry : bufferPool.entrySet()) {
            int pageNumber = entry.getKey();
            Page page = entry.getValue();
            // Write the page to disk
            diskManager.writePageToDisk(pageNumber, page);
        }
        // Clear the buffer pool
        bufferPool.clear();
    }
}

class DiskManager {
    private String databaseFileName; // Name of the database file

    public DiskManager(String databaseFileName) {
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