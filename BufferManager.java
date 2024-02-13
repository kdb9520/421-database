
import java.util.HashMap;
import java.util.Map;

public class BufferManager {
    private Map<Integer, Page> bufferPool; // Map page number to page data
    private int bufferSize; // Size of buffer pool
    private StorageManager storageManager; // A hypothetical class for disk operations

    public BufferManager(int bufferSize, StorageManager storageManager) {
        this.bufferSize = bufferSize;
        this.bufferPool = new HashMap<>();
        this.storageManager = storageManager;
    }

    public Page getPage(int pageNumber) {
        if (bufferPool.containsKey(pageNumber)) {
            // Page is already in buffer pool
            return bufferPool.get(pageNumber);
        } else {
            // Page is not in buffer pool, so read it from disk
            Page page = storageManager.readPageFromDisk(pageNumber);
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
        storageManager.writePageToDisk(firstPageNumber, removedPage);
        bufferPool.remove(firstPageNumber);
    }

    public void purgeBuffer() {
        // Iterate through all entries in the buffer pool
        for (Map.Entry<Integer, Page> entry : bufferPool.entrySet()) {
            int pageNumber = entry.getKey();
            Page page = entry.getValue();
            // Write the page to disk
            storageManager.writePageToDisk(pageNumber, page);
        }
        // Clear the buffer pool
        bufferPool.clear();
    }
}

