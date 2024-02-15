
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
    private StorageManager storageManager; // A hypothetical class for disk operations

    public BufferManager(int bufferSize, StorageManager storageManager) {
        this.bufferSize = bufferSize;
        this.bufferPool = new ArrayList<Page>();
        this.storageManager = storageManager;
    }

    public Page getPage(String tableName, int pageNumber) {

        for(Page p : bufferPool){
            if(p.getTableName() == tableName & p.getPageNumber() == pageNumber){
                return p;
            }
        }
            // Page is not in buffer pool, so read it from disk
            Page page = storageManager.readPageFromDisk(tableName, pageNumber);
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
        storageManager.writePageToDisk(removedPage);
        bufferPool.remove(0);
    }

    public void purgeBuffer() {
        // Iterate through all entries in the buffer pool
        for (Page p : bufferPool) {
            storageManager.writePageToDisk(p);
        }
        // Clear the buffer pool
        bufferPool.clear();
    }

    public Page createPage(String tableName, int pageNumber){
        // First create a page
        Page page = new Page(tableName, pageNumber, null);
        // Add it to buffer
        if (bufferPool.size() >= bufferSize) {
            // Buffer pool is full, evict a page using some policy (e.g., LRU)
            evictPage();
        }
        // Add the new page to buffer pool
        bufferPool.add(page);
        return page;
    }
}
