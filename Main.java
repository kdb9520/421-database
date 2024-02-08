import java.util.Scanner;

//Imports

// Main class for the project
// Authors:

public class Main {
    public static void main (String[] args) {

        if (args.length != 3) {
            displayUsage();
        }
        else {
            String dbLoc = args[1];
            int pageSize = Integer.valueOf(args[2]);
            int bufferSize = Integer.valueOf(args[3]);

            // restart / create the database
            int started = DatabaseStart.initiateDatabase(dbLoc, pageSize, bufferSize);

            DiskManager diskManager = new DiskManager(dbLoc);
            BufferManager bufferManager = new BufferManager(bufferSize, diskManager)

            if (started == 1) {
                Scanner scanner = new Scanner(System.in);
                //String query = "";
                StringBuilder commandBuilder = new StringBuilder();

                while (true) {
                    
                    String line = readCommand(scanner);
                    commandBuilder.append(line).append("\n");

                    // SCAN UNTIL SEMICOLON
                    // CLEAN OUT ALL WHITE SPACE AND REPLACE WITH SPACES

                    if (line.contains(";")) {
                        String command = commandBuilder.toString().trim().replaceAll("\\s+", " ");
                        handleQuery(command);
                        if (line.endsWith(";")) {
                            // Clear the command builder if the semicolon is at the end of the line
                            commandBuilder.setLength(0);
                        } else {
                            // If the semicolon is not at the end, reset the builder to the part after the semicolon
                            int index = line.lastIndexOf(";");
                            commandBuilder.setLength(0);
                            commandBuilder.append(line.substring(index + 1).trim()).append("\n");
                        }
                    }
                }
            }
            else {
                displayUsage();
            }
        }
    }

    // Helper method to read a command from the user
    private static String readCommand(Scanner scanner) {
        System.out.print("> ");
        return scanner.nextLine().trim();
    }

    private static void handleQuery (String query, BufferManager bufferManager) {
        
        if(query.substring(0, 3).equals("quit")) {
            shutdown(bufferManager);
        }
        else if (query.substring(0, 3).equals("help")) {
            helpCommand();
        }
        else if (query.substring(0, 6).equals("create ") || 
            query.substring(0, 4).equals("drop ") || 
            query.substring(0, 5).equals("alter ")) {
                
            // give buffer manager too
            DDLParser.query(query);
        }
        else if(query.substring(0, 11).equals("insert into ") ||
                query.substring(0, 12).equals("display info ") ||
                query.substring(0, 6).equals("select ")) {

                // give buffer manager too
                DMLParser.query(query);
        }
        else {
            System.out.println("Command not valid!\n\nEnter 'help;' to list all commands.");
        }
    }

    public static void displayUsage() {
        System.out.println("USAGE: java Main <db loc> <page size> <buffer size> \n\nEnter 'help;' to list all commands.");
    }

    public static void helpCommand() {
        System.out.println("COMMANDS: ________");
    }

    private static void shutdown(BufferManager bufferManager) {
        System.out.println("Shutting down database...");
        bufferManager.purgeBuffer();
        System.out.println("Shutdown complete.");
        System.exit(0);
    }
}