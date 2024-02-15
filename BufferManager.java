
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class BufferManager {
    //private Map<Integer, Page> bufferPool; // Map page number to page data
    private ArrayList<Page> bufferPool;
    private int bufferSize; // Size of buffer pool
    private DiskManager diskManager; // A hypothetical class for disk operations

    public BufferManager(int bufferSize, DiskManager diskManager) {
        this.bufferSize = bufferSize;
        this.bufferPool = new ArrayList<Page>();
        this.diskManager = diskManager;
    }

    public Page getPage(String tableName, int pageNumber) {

        for(Page p : bufferPool){
            if(p.getTableName() == tableName & p.getPageNumber() == pageNumber){
                return p;
            }
        }
            // Page is not in buffer pool, so read it from disk
            Page page = diskManager.readPageFromDisk(tableName, pageNumber);
            if (bufferPool.size() >= bufferSize) {
                // Buffer pool is full, evict a page using some policy (e.g., LRU)
                evictPage();
            }
            // Add the new page to buffer pool
            bufferPool.add(page);
            return page;
        }
    

    public void writePage(Page page) {

        
        for(Page p : bufferPool){
            if(p.getTableName() == page.getTableName() && p.getPageNumber() == page.getPageNumber()){
                // Replace that page with this updated page
                int index = bufferPool.indexOf(p);
                if (index != -1) {
                    bufferPool.set(index, page);
                }
                return;
            }
        }
            // Page is not in the buffer pool, add it
            if (bufferPool.size() >= bufferSize) {
                // Buffer pool is full, evict a page using some policy (e.g., LRU)
                evictPage();
            }
            bufferPool.add(page);
        }

    private void evictPage() {
        // Implementation of page eviction policy (e.g., LRU)
        // For simplicity, this example just removes the first page in the buffer pool
        Page removedPage = bufferPool.get(0);
        diskManager.writePageToDisk(removedPage);
        bufferPool.remove(0);
    }

    public void purgeBuffer() {
        // Iterate through all entries in the buffer pool
        for (Page p : bufferPool) {
            diskManager.writePageToDisk(p);
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


    public Page readPageFromDisk(String tableName, int pageNumber) {
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

    public void writePageToDisk(Page page) {
        System.out.println("Writing page " + page.getPageNumber() + " to disk: " + page);

        try (FileOutputStream fos = new FileOutputStream(page.getTableName(), true)) {
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
    }
}