import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


// Starts or creates the user specified database
// Creates directory at location if it does not exist. Stores the pageSize info in page_size file
public class DatabaseStart {
    
    public static boolean initiateDatabase (String databaseLocation, int pageSize, int bufferSize) {
        // Restart the database; ignoring the provided page size, using the new buffer size
        // Create a new database at that location using the provided page and buffer size

        Path path = Paths.get(databaseLocation);
        boolean exists = Files.exists(path) && Files.isDirectory(path);


        boolean success = false;
        if (exists) {
            success = startDatabase(databaseLocation, pageSize, bufferSize);
        
        }
        else {
            success = createDatabase(databaseLocation, pageSize, bufferSize);
        }

        return success;
    }

    private static boolean startDatabase (String databaseLocation, int pageSize, int bufferSize) {

        boolean success = false;
        try {
            Path folder = Paths.get(databaseLocation);
            Path file = folder.resolve("page_size");

            if (Files.exists(folder) && Files.isDirectory(folder) && Files.exists(file) && Files.isRegularFile(file)) {
                folder = Paths.get(databaseLocation);
                Path filePath = folder.resolve("page_size");
                
                // Overwrite the file content with the new number
                String newContent = String.valueOf(pageSize);
                Files.write(filePath, newContent.getBytes());

                success = true;
            }
            else {
                success = createDatabase(databaseLocation, pageSize, bufferSize);
            }
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
        return success;
    }

    private static boolean createDatabase (String databaseLocation, int pageSize, int bufferSize) {
        
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
            success = true;
            
        } catch (IOException e) {
            System.err.println("An error occurred in creating the database: " + e.getMessage());
            success = false;
        }
        return success;
    }
}