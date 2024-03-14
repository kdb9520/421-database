package src;

import java.io.File;
import java.util.ArrayList;

public class BufferManager {
    //private Map<Integer, src.Page> bufferPool; // Map page number to page data
    private static ArrayList<Page> bufferPool;

    static{
        bufferPool = new ArrayList<>();
    }

    public static Page getPage(String tableName, int pageNumber) {

        for(Page p : bufferPool){
            if(p.getTableName().equals(tableName) && p.getPageNumber() == pageNumber){
                return p;
            }
        }
            // src.Page is not in buffer pool, so read it from disk
            Page page = StorageManager.readPageFromDisk(tableName, pageNumber);
            if (bufferPool.size() + 1 >= Main.bufferSize) {
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
            // src.Page is not in the buffer pool, add it
            if (bufferPool.size() + 1 >= Main.bufferSize) {
                // Buffer pool is full, evict a page using some policy (e.g., LRU)
                evictPage();
            }
            bufferPool.add(page);
        }

    private static void evictPage() {
        // Implementation of page eviction policy (e.g., LRU)
        // For simplicity, this example just removes the first page in the buffer pool
        // Evict the first nonlocked page
        for(int i = 0; i<bufferPool.size();i++){
            if(!bufferPool.get(i).isLocked()){
                Page removedPage = bufferPool.get(i);
                if(removedPage.wasEdited){
                    if(removedPage.getRecords() == null || removedPage.getRecords().size() == 0){
                        StorageManager.deletePage(removedPage);
                        updatePageNumbersOnRemoval(removedPage.getTableName(), i+1);
                    }
                    else{
                        StorageManager.writePageToDisk(removedPage);
                    }
                    
                }
                bufferPool.remove(i);
                return;
            }
        }
        
    }

    public static void purgeBuffer() {
        while(bufferPool.size() > 0){
            evictPage();
        }
    }

    public static Page createPage(String tableName, int pageNumber){
        // First create a page
        Page page = new Page(tableName, pageNumber, null);
        // Add it to buffer
        if (bufferPool.size() >= Main.bufferSize) {
            // Buffer pool is full, evict a page using some policy (e.g., LRU)
            evictPage();
        }
        // Add the new page to buffer pool
        bufferPool.add(page);
        return page;
    }

    public static void deleteTable(String tableName){
        for(Page p : bufferPool){
            if(p.getTableName().equals(tableName)){
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
        if (bufferPool.size() + 1 > Main.bufferSize) {
            // Buffer pool is full, evict a page using some policy (e.g., LRU)
            evictPage();
        }
        // Add the new page to buffer pool
        bufferPool.add(page);
    }
    // updates the page numbers of all pages in the buffer pool if a page split occurred
    public static void updatePageNumbersOnSplit (String tableName, int newPageNumber) {
        for (Page page : bufferPool) {
            int currentPageNumber = page.getPageNumber();
            if (page.getTableName().equals(tableName) && currentPageNumber >= newPageNumber) {
                page.setPageNumber(currentPageNumber + 1);
            }
        }
    }

    public static void updatePageNumbersOnRemoval (String tableName, int newPageNumber) {
        for (Page page : bufferPool) {
            int currentPageNumber = page.getPageNumber();
            if (page.getTableName().equals(tableName) && currentPageNumber >= newPageNumber) {
                page.setPageNumber(currentPageNumber - 1);
                System.out.println(page.getPageNumber());
            }
        }
    }
}
