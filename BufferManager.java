
import java.util.HashMap;
import java.util.Map;

public class BufferManager {
    private Map<Integer, byte[]> bufferPool; // Map page number to page data
    private int bufferSize; // Size of buffer pool
    private DiskManager diskManager; // A hypothetical class for disk operations

    public BufferManager(int bufferSize, DiskManager diskManager) {
        this.bufferSize = bufferSize;
        this.bufferPool = new HashMap<>();
        this.diskManager = diskManager;
    }

    public byte[] getPage(int pageNumber) {
        if (bufferPool.containsKey(pageNumber)) {
            // Page is already in buffer pool
            return bufferPool.get(pageNumber);
        } else {
            // Page is not in buffer pool, so read it from disk
            byte[] pageData = diskManager.readPageFromDisk(pageNumber);
            if (bufferPool.size() >= bufferSize) {
                // Buffer pool is full, evict a page using some policy (e.g., LRU)
                evictPage();
            }
            // Add the new page to buffer pool
            bufferPool.put(pageNumber, pageData);
            return pageData;
        }
    }

    public void writePage(int pageNumber, byte[] pageData) {
        // Write page to buffer pool
        bufferPool.put(pageNumber, pageData);
        // Write page to disk
        diskManager.writePageToDisk(pageNumber, pageData);
    }

    private void evictPage() {
        // Implementation of page eviction policy (e.g., LRU)
        // For simplicity, this example just removes the first page in the buffer pool
        int firstPageNumber = bufferPool.keySet().iterator().next();
        bufferPool.remove(firstPageNumber);
    }
}

class DiskManager {
    public byte[] readPageFromDisk(int pageNumber) {
        // Read page from disk (mock implementation)
        // In a real scenario, this method would read the page data from disk
        // For simplicity, we just return some mock data here
        return ("Page " + pageNumber).getBytes();
    }

    public void writePageToDisk(int pageNumber, byte[] pageData) {
        // Write page to disk (mock implementation)
        // In a real scenario, this method would write the page data to disk
        // For simplicity, we just print the page data here
        System.out.println("Writing page " + pageNumber + " to disk: " + new String(pageData));
    }
}