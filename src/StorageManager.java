package src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Arrays;

public class StorageManager {
    public static Catalog catalog;                  // private instance of src.Catalog, accessible by static methods
    private static ArrayList<BxTree> indexes; // All the indexes BPlusTrees

    static {
        indexes = new ArrayList<>();
    }

    private StorageManager() {

    }


    public static Page readPageFromDisk(String tableName, int pageNumber) {
        try (FileChannel fileChannel = FileChannel.open(Paths.get(Main.databaseLocation, tableName), StandardOpenOption.READ)) {
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

            // Create and return a new src.Page object
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

            if (page.getRecords() == null || page.getRecords().size() == 0) {
                deletePage(page);
                return;
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            //FileOutputStream fos = new FileOutputStream(file); Depreceated cant use this

            // Calculate the offset where this page should be written
            TableSchema tableSchema = Catalog.getTableSchema(page.getTableName());
            ArrayList<Integer> indexList = tableSchema.getIndexList();
            // First we update the num_pages if it changed

            // Read the current num_pages to see if its up to date
            int written_num_pages = readNumberOfPages(file);

            if (page.getPageNumber() + 1 > written_num_pages) {
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
            byte[] serializedPageData = page.serialize();
            int paddingSize = Main.pageSize - serializedPageData.length;
            byte[] paddingBytes = new byte[paddingSize];
            Arrays.fill(paddingBytes,   /* Pass the byte array directly */
                    (byte) 0);

            byte[] dataToWrite = new byte[serializedPageData.length + paddingSize];
            System.arraycopy(serializedPageData, 0, dataToWrite, 0, serializedPageData.length);
            System.arraycopy(paddingBytes, 0, dataToWrite, serializedPageData.length, paddingSize);
            randomAccessFile.write(dataToWrite);
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


    public static void writeTableToDisk(String tableName) {

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

    /**
     * Gets a single tree from the index list
     *
     * @param tableName - table name
     * @return - a b+ tree if found, null if not (and error printed)
     */
    public static BxTree getTree(String tableName) {
        for (BxTree bPlusTree : indexes) {
            if (bPlusTree.getName().equals(tableName)) {
                return bPlusTree;
            }
        }
        if (Main.useIndex) System.err.println("B+ tree for specified index not found");
        return null;
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

    public static void deleteTable(String tableName) {
        Path filePath = Paths.get(Main.databaseLocation, tableName);
        File f = new File(Main.databaseLocation + File.separator + tableName);

        Path indexPath = Paths.get(Main.databaseLocation + "/Indexes", tableName);
        File indexf = new File(indexPath.toString());

        if (f.exists() && !f.isDirectory()) {
            try {

                Files.delete(filePath);
                BufferManager.deleteTable(tableName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (BxTree bPlusTree : indexes) {
            if (bPlusTree.getName().equals(tableName)) {
                indexes.remove(bPlusTree);
                break;
            }
        }

        if (indexf.exists() && !indexf.isDirectory()) {
            try {

                Files.delete(indexPath);
//                BufferManager.deleteTable(tableName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void deletePage(Page p) {
        String tableName = p.getTableName();
        int pageNum = p.getPageNumber();

        TableSchema tableSchema = Catalog.getTableSchema(tableName);
        ArrayList<Integer> indexList = tableSchema.getIndexList();
        int numPages = indexList.size();

        if (tableSchema == null) {
            System.err.println("Table: " + tableName + "does not exist");
            return;
        }

        // Move to the page and delete it's bytes
        long offset = (indexList.get(p.getPageNumber()) * Main.pageSize) + 4;

        File file = new File(Main.databaseLocation + File.separator + p.getTableName());

        // Create the file if it doesn't exist
        if (!file.exists()) {
            // If file does not exist this means the table was dropped when page was still in page buffer
            // Just return and we good to go
            return;
            //System.out.println("Error: Table: " + page.getTableName() + " does not exist");
        }

        try {
            int written_num_pages = readNumberOfPages(file);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");


            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.putInt(written_num_pages - 1);
            byte[] bytes = buffer.array();
            randomAccessFile.write(bytes);

            // Reset the file pointer to the beginning of the file
            randomAccessFile.seek(0);

            byte[] entireFileContent = new byte[(int) randomAccessFile.length()];
            randomAccessFile.readFully(entireFileContent);


            randomAccessFile.close();

            int startIndex = (int) offset;  // Assuming offset is a long value
            int endIndex = startIndex + Main.pageSize;

            int modifiedContentLength = entireFileContent.length - Main.pageSize;
            byte[] modifiedContent = new byte[modifiedContentLength];

            // Copy the part before the deletion point
            System.arraycopy(entireFileContent, 0, modifiedContent, 0, startIndex);

            // Copy the part after the deletion point
            System.arraycopy(entireFileContent, endIndex, modifiedContent, startIndex, modifiedContentLength - startIndex);

            File newFile = new File(file.getParent(), file.getName() + ".new");
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            fileOutputStream.write(modifiedContent);
            fileOutputStream.close();

            if (file.delete() && newFile.renameTo(file)) {
                System.out.println("File modified successfully.");
            } else {
                System.out.println("Error replacing original file.");
                // Handle the error (e.g., delete the new file)
            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // For all pages beyond the removed page lets move them down a spot
        // Maybe call function in buffermanager
        for (int i = pageNum + 1; i < numPages; i++) {
            // Update the index list value so it rewrites to hardware correctly
            indexList.set(i, indexList.get(i) - 1);

            // Do we need any other action? Maybe not
        }
        // Remove the reference from indexList
        indexList.remove(pageNum);
    }

    /**
     * Check db_loc/Indexes folder and read in each file, converting it to BPlusTree objects
     *
     * @return
     */
    public static void readIndexes(String db_loc) {

        try {
            // Assuming db_loc is a directory path
            File schemaDirectory = new File(db_loc + "/Indexes");

            if (schemaDirectory.exists() && schemaDirectory.isDirectory()) {
                File[] schemaFiles = schemaDirectory.listFiles();

                if (schemaFiles != null) {
                    for (File schemaFile : schemaFiles) {
                        // Read each schema file and create src.TableSchema objects
                        BxTree tree = BxTree.deserialize(Files.readAllBytes(schemaFile.toPath()), schemaFile.getName());
                        if (tree != null) {
                            indexes.add(tree);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Writes to hardware
     */
    public static void writeIndexes(String db_loc) {
        // Ensure the db_loc/Schema directory exists or create it if it doesn't
        String schemaDirectoryPath = db_loc + "/Indexes";
        createDirectoryIfNotExists(schemaDirectoryPath);

        for (BxTree index : indexes) {
            // Generate a unique filename for each src.TableSchema (you may need to adjust this)
            String filename = schemaDirectoryPath + "/" + index.getName();

            // Write the src.TableSchema to the file
            // Write the src.TableSchema to the file
            try (FileOutputStream fileOutputStream = new FileOutputStream(filename)) {
                // Serialize the src.TableSchema to obtain a byte array
                byte[] serializedData = index.serialize(index.getName());

                index.printTree();

                // Write the byte array to the file
                fileOutputStream.write(serializedData);
                System.out.println("src.Index written to: " + filename);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception according to your requirements
            }
        }
    }

    private static void createDirectoryIfNotExists(String directoryPath) {
        try {
            Path path = Path.of(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void addIndex(BxTree index) {
        indexes.add(index);
    }

}
