//Imports

// Starts or creates the user specified database
// Authors:

public static class DatabaseStart {
    
    public static int initiateDatabase (String databaseLocation, int pageSize, int bufferSize) {

        boolean success
        if (/* DB file exists */) {
            success = startDatabase(databaseLocation, int pageSize, int bufferSize);
        }
        else {
            success = createDatabase(databaseLocation, int pageSize, int bufferSize);
        }

        return success;
    }

    public static int startDatabase (String databaseLocation, int pageSize, int bufferSize) {

        // Restart the database; ignoring the provided page size, using the new buffer size

        // Look for all .table files in the folder 'Tables'

        // 

        if (/* Success */)
            return 1;
        else
            return 0;
    }

    public static int createDatabase (String databaseLocation, int pageSize, int bufferSize) {
        
        // Create a new database at that location using the provided page and buffer size

        // Create tables folder

        if (/* Success */)
            return 1;
        else
            return 0;
    }
}