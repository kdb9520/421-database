import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
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
        try (FileChannel fileChannel = FileChannel.open(Paths.get(Main.databaseLocation,tableName), StandardOpenOption.READ)) {
            // Calculate the position in the file where the page starts
            TableSchema tableSchema = Catalog.getTableSchema(tableName);
            ArrayList<Integer> indexList = tableSchema.getIndexList();
            // Set buffer to position of the right page
            long position = (indexList.get(pageNumber) * Main.pageSize) + 4; // Add 4 since num_pages is stored at beginning
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

    public static void writePageToDisk(Page page) {
        try {
            File file = new File(Main.databaseLocation + File.separator + page.getTableName());
            
            // Create the file if it doesn't exist
            if (!file.exists()) {
                // If file does not exist this means the table was dropped when page was still in page buffer
                // Just return and we good to go
                return;
                //System.out.println("Error: Table: " + page.getTableName() + " does not exist");
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            //FileOutputStream fos = new FileOutputStream(file); Depreceated cant use this
        
            // Calculate the offset where this page should be written
            TableSchema tableSchema = Catalog.getTableSchema(page.getTableName());
            ArrayList<Integer> indexList = tableSchema.getIndexList();
            // First we update the num_pages if it changed
            
            // Read the current num_pages to see if its up to date
            int written_num_pages = readNumberOfPages(file);
            
            if(page.getPageNumber() + 1 >  written_num_pages){
                ByteBuffer buffer = ByteBuffer.allocate(4);
                buffer.putInt(written_num_pages + 1);
                byte[] bytes = buffer.array();
                randomAccessFile.write(bytes);
            }
            //Integer pageIndex = ;
            long offset = (indexList.get(page.getPageNumber()) * Main.pageSize) + 4;
            // Move the file pointer to the correct position
            randomAccessFile.seek(offset);
            // Write the page data to the file
            randomAccessFile.write(page.serialize());
            randomAccessFile.close();
        } catch (IOException e) {
            // Handle the exception (e.g., log it or throw a runtime exception)
            e.printStackTrace();
        }

    }

    public static void updateTableNumPages(String tableName) {
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


    /**
     * Deletes records from a table given a condition
     * @param tableName - the name of the table
     * @param whereClause - the condition
     */
    public static void deleteRecord(String tableName, String whereClause){

    }

    public static void writeTableToDisk (String tableName) {

        // Create actual table file (they are stored as files)

        try {
            // Construct the file path
            String filePath = Main.databaseLocation + File.separator + tableName;
            File f = new File(filePath);
            f.createNewFile();

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                // Write the integer 0 to the file
                fos.write(ByteBuffer.allocate(4).putInt(0).array());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Table created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static int readNumberOfPages(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4];
            int bytesRead = fis.read(buffer);

            if (bytesRead == -1) {
                // End of file reached, handle it as appropriate for your application
                return -1; // You might want to choose an appropriate default value
            }

            // Convert the byte array to an integer
            int written_num_pages = ByteBuffer.wrap(buffer).getInt();

            return written_num_pages;

        } catch (IOException e) {
            // Handle the exception (e.g., log it or throw a runtime exception)
            e.printStackTrace();
            // Return a default value or handle the error as appropriate for your application
            return -1; // You might want to choose an appropriate default value
        }
    }

    public static void deleteTable(String tableName){
        Path filePath = Paths.get(Main.databaseLocation, tableName);
        File f = new File(Main.databaseLocation + File.separator + tableName);

        if(f.exists() && !f.isDirectory()) { 
            try{
            
                Files.delete(filePath);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void deleteRecord(String tableName, Object primaryKey) {

        TableSchema tableSchema = Catalog.getTableSchema(tableName);

        if (tableSchema == null) {
            System.err.println("Table: " + tableName + "does not exist");
            return;
        }

        ArrayList<AttributeSchema> attributeSchemas = tableSchema.getAttributeSchema();

        // Get the primary key and its type so we can compare
        int numPages = tableSchema.getIndexList().size();

        // Get primary key col number so we can figure out where to insert this record
        int primaryKeyCol = tableSchema.findPrimaryKeyColNum();

        for (int i = 0; i < numPages; i++) {
            Page page = BufferManager.getPage(tableName, i);
            for (Record r : page.getRecords()) {
                if (r.getAttribute(primaryKeyCol).equals(primaryKey)) {
                    // delete the current record 
                }
            }
        }
    }

}
