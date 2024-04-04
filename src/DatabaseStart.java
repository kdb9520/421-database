package src;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


// Starts or creates the user specified database
// Creates directory at location if it does not exist. Stores the pageSize info in page_size file
public class DatabaseStart {
    
    public static boolean initiateDatabase (String databaseLocation, int pageSize, int bufferSize, boolean useIndex) {
        // Restart the database; ignoring the provided page size, using the new buffer size
        // Create a new database at that location using the provided page and buffer size

        Path path = Paths.get(databaseLocation);
        boolean exists = Files.exists(path) && Files.isDirectory(path);


        boolean success = false;
        if (exists) {
            success = startDatabase(databaseLocation, pageSize, bufferSize, useIndex);
        
        }
        else {
            success = createDatabase(databaseLocation, pageSize, bufferSize, useIndex);
        }

        return success;
    }

    private static boolean startDatabase (String databaseLocation, int pageSize, int bufferSize, boolean useIndex) {

        boolean success = false;
        try {
            Path folder = Paths.get(databaseLocation);
            Path file = folder.resolve("page_size");
            Path indexFile = folder.resolve("index");

            if (Files.exists(folder) && Files.isDirectory(folder) && Files.exists(file) && Files.isRegularFile(file)
                    && Files.exists(indexFile) && Files.isRegularFile(indexFile)) {
                folder = Paths.get(databaseLocation);
                Path filePath = folder.resolve("page_size");
                Path indexFilePath = folder.resolve("index");
                
                 // Read the content of the file as a string
                String content = Files.readString(filePath);
                String indexContent = Files.readString(indexFilePath);
                // Parse the integer value from the content
                int ps = Integer.parseInt(content);
                StorageManager.BPlusExists = Boolean.parseBoolean(indexContent);
                // Overwrite the command arg page size user provided with the size stored in files
                Main.pageSize = ps;
                success = true;
            }
            else {
                success = createDatabase(databaseLocation, pageSize, bufferSize, useIndex);
            }
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
        return success;
    }

    private static boolean createDatabase (String databaseLocation, int pageSize, int bufferSize, boolean useIndex) {
        
        boolean success = false;
        try {
            //createFolderAndFile(databaseLocation, pageSize);

            // Create the folder
            Path folder = Paths.get(databaseLocation);
            Files.createDirectories(folder);

            // Create the file within the folder
            Path filePath = folder.resolve("page_size");
            String content = String.valueOf(pageSize);
            Files.write(filePath, content.getBytes());

            // Create the index file
            Path indexPath = folder.resolve("index");
            String indexVal = String.valueOf(useIndex);
            Files.write(indexPath, indexVal.getBytes());
            StorageManager.BPlusExists = useIndex;

            success = true;
            
        } catch (IOException e) {
            System.err.println("An error occurred in creating the database: " + e.getMessage());
            success = false;
        }
        return success;
    }
}
