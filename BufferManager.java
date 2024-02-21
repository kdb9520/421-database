import java.io.File;
import java.util.ArrayList;

public class BufferManager {
    //private Map<Integer, Page> bufferPool; // Map page number to page data
    private static ArrayList<Page> bufferPool;
    private static final int bufferSize = 50; // Size of buffer pool

    static{
        bufferPool = new ArrayList<>();
    }

    public static Page getPage(String tableName, int pageNumber) {

        for(Page p : bufferPool){
            if(p.getTableName().equals(tableName) && p.getPageNumber() == pageNumber){
                return p;
            }
        }
            // Page is not in buffer pool, so read it from disk
            Page page = StorageManager.readPageFromDisk(tableName, pageNumber);
            if (bufferPool.size() + 1 >= bufferSize) {
                // Buffer pool is full, evict a page using some policy (e.g., LRU)
                evictPage();
            }
            // Add the new page to buffer pool
            bufferPool.add(page);
            return page;
        }
    

    public static void writePage(Page page) {

        
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
            if (bufferPool.size() + 1 >= bufferSize) {
                // Buffer pool is full, evict a page using some policy (e.g., LRU)
                evictPage();
            }
            bufferPool.add(page);
        }

    private static void evictPage() {
        // Implementation of page eviction policy (e.g., LRU)
        // For simplicity, this example just removes the first page in the buffer pool
        Page removedPage = bufferPool.get(0);
        StorageManager.writePageToDisk(removedPage);
        bufferPool.remove(0);
    }

    public static void purgeBuffer() {
        // Iterate through all entries in the buffer pool
        for (Page p : bufferPool) {
            StorageManager.writePageToDisk(p);
        }
        // Clear the buffer pool
        bufferPool.clear();
    }

    public static Page createPage(String tableName, int pageNumber){
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

    public static void deleteTable(String tableName, int pageNumber){
        for(Page p : bufferPool){
            if(p.getTableName().equals(tableName) && p.getPageNumber() == pageNumber){
                bufferPool.remove(p);
            }

            return;
        }


        File file = new File(Main.databaseLocation + tableName);
        if(!file.exists()) {
            System.err.println("File does not exist");
            return;
        }

        file.delete();
        System.out.println("File deleted successfully");
    }
    public static void addPageToBuffer(Page page){
        // First create a page
        // Add it to buffer
        if (bufferPool.size() + 1 >= bufferSize) {
            // Buffer pool is full, evict a page using some policy (e.g., LRU)
            evictPage();
        }
        // Add the new page to buffer pool
        bufferPool.add(page);
    }
}
