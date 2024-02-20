import java.util.Scanner;

//Imports

// Main class for the project
// Authors:

public class Main {

    public static int pageSize;
    public static int bufferSize;

    public static void main (String[] args) {

        if (args.length != 3) {
            displayUsage();
        }
        else {
            String dbLoc = args[0];
            pageSize = Integer.valueOf(args[1]);
            bufferSize = Integer.valueOf(args[2]);

            // restart / create the database
            // Jaron Handling start up of dbint started = DatabaseStart.initiateDatabase(dbLoc, pageSize, bufferSize);

            boolean started = DatabaseStart.initiateDatabase(dbLoc, pageSize, bufferSize);

            Catalog.readCatalog(dbLoc);

            if (started) {
                Scanner scanner = new Scanner(System.in);
                StringBuilder commandBuilder = new StringBuilder();

                while (true) {
                    String line = readCommand(scanner);
                    commandBuilder.append(line).append("\n");

                    // SCAN UNTIL SEMICOLON
                    // CLEAN OUT ALL WHITE SPACE AND REPLACE WITH SPACES

                    if (line.contains(";")) {
                        String command = commandBuilder.toString().trim().replaceAll("\\s+", " ");
                        handleQuery(command, dbLoc);
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

    private static void handleQuery (String query, String dbloc) {
        
        query = query.toLowerCase();

        if(query.substring(0, 3).equals("quit")) {
            shutdown();
        }
        else if (query.substring(0, 3).equals("help")) {
            helpCommand();
        }
        else if (query.substring(0, 6).equals("create ")) {
            DDLParser.createTable(query);
        }
        else if (query.substring(0, 4).equals("drop ") ) {
            DDLParser.dropTable(query);
        }
        else if (query.substring(0, 5).equals("alter ")) {
            DDLParser.alterTable(query);
        }
        else if (query.startsWith("insert into ") ||
            query.startsWith("display schema ") ||
            query.startsWith("display info ") ||
            query.startsWith("select ")) {

                // give buffer manager too
                DMLParser.handleQuery(query, dbloc);
        }
        else {
            System.out.println("Command not valid!\n\nEnter 'help;' to list all commands.");
        }
    }

    public static void displayUsage() {
        System.out.println("USAGE: java Main <db loc> <page size> <buffer size> \n\nEnter 'help;' to list all commands.");
    }

    public static void helpCommand() {
        System.out.println("COMMANDS: \n");
        System.out.println("-----Utility Commands-----");
        System.out.println("help;   -Displays all commands\nquit;   -Shutdown the database and application");
        System.out.println("\n-----Schema Commands-----");
        System.out.println("create table <name> ___MORE__;   -Creates new table in database");
        System.out.println("drop table <name>;   -Drops the table in the database");
        System.out.println("alter table <name> ____MORE___;  -Alters table with specified attributes");
        System.out.println("\n-----Query Commands-----");
        System.out.println("insert into <name> values <tuples>;");
        System.out.println("display schema;");
        System.out.println("display info <name>;");
        System.out.println("select * from <name>;");
    }

    private static void shutdown() {
        System.out.println("Shutting down database...");
        BufferManager.purgeBuffer();
        System.out.println("Shutdown complete.");
        System.exit(0);
    }
}