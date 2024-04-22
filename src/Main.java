package src;

import java.util.Scanner;

//Imports

// src.Main class for the project
// Authors: Group 15

public class Main {

    public static int pageSize;
    public static int bufferSize;
    public static String databaseLocation;
    public static boolean useIndex;

    // Grab command args in form [DB_Location] [Page_Size] [Buffer_Size]
    // [Page_Size] = integer saying max size of a src.Page in bytes
    // [Buffer_Size] = integer setting how many Pages a src.Page Buffer may hold at once
    public static void main (String[] args) {

        if (args.length != 4) {
            displayUsage();
        }
        else {
            databaseLocation = args[0];
            pageSize = Integer.valueOf(args[1]);
            bufferSize = Integer.valueOf(args[2]);
            useIndex = args[3].equals("true");

            // restart / create the database
            // Jaron Handling start up of dbint started = src.DatabaseStart.initiateDatabase(dbLoc, pageSize, bufferSize);
            // This will override pageSize command arg if DB exists already
            boolean started = DatabaseStart.initiateDatabase(databaseLocation, pageSize, bufferSize, useIndex);

            Catalog.readCatalog(databaseLocation);
            
            if(useIndex){
                StorageManager.readIndexes(databaseLocation);
            }
            

            if (started) {
                Scanner scanner = new Scanner(System.in);
                StringBuilder commandBuilder = new StringBuilder();

                boolean on = true;
                    while (on) {
                        try{
                            System.out.print("> ");
                            String line = readCommand(scanner);
                            commandBuilder.append(line).append("\n");
        
                            // SCAN UNTIL SEMICOLON
                            // CLEAN OUT ALL WHITE SPACE AND REPLACE WITH SPACES
        
                            if (line.contains(";")) {
                                String command = commandBuilder.toString().trim().replaceAll("\\s+", " ");
                                on = handleQuery(command, databaseLocation);
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
                        } catch(Exception e){
                            System.out.println("Error with command. Debugging statement:");
                            commandBuilder.setLength(0);
                            e.printStackTrace();
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

        return scanner.nextLine().trim();
    }

    private static boolean handleQuery (String query, String dbloc) {
        
        query = query.replaceAll("\\s+", " ");
        if (query.substring(query.length() - 2).equals(" ;")) {
            query = query.substring(0, query.length() - 2) + ";";
        }

        char[] chars = query.toCharArray();
        boolean insideQuotes = false;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\'' || chars[i] == '"') {
                insideQuotes = !insideQuotes;
            } else if (!insideQuotes) {
            chars[i] = Character.toLowerCase(chars[i]);
            }
        }

        query = new String(chars);
        System.out.println("Processed Query: " + query);

        if(query.startsWith("quit")) {
            shutdown();
            return false;
        }
        else if (query.startsWith("help")) {
            helpCommand();
        }
        else if (query.startsWith("display index ")) {
            String[] splitstring5 = query.split("\\s+");
            displayIndexes(splitstring5[1]);
        }
        else if (query.startsWith("create ")) {
            DDLParser.createTable(query);
        }
        else if (query.startsWith("drop ") ) {
            DDLParser.dropTable(query);
        }
        else if (query.startsWith("alter ")) {
            DDLParser.alterTable(query);
        }


        else if (query.startsWith("delete ") || query.startsWith("insert into ") ||
            query.strip().equals("display schema;") ||
            query.startsWith("display info ") ||
            query.startsWith("select ") ||
            query.startsWith("update ")){

                // give buffer manager too
                DMLParser.handleQuery(query, dbloc, useIndex);
        }
        else {
            System.out.println("Command not valid!\n\nEnter 'help;' to list all commands.");
        }
        return true;
    }

    private static void displayIndexes(String tableName) {
        BPlusTree indexes = StorageManager.getTree(tableName);
        indexes.printTree();
    }

    public static void displayUsage() {
        System.out.println("USAGE: java src.Main <db loc> <page size> <buffer size> <indexing> \n\nEnter 'help;' to list all commands.");
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
        Catalog.writeCatalog(databaseLocation);
        StorageManager.writeIndexes(databaseLocation);
        System.out.println("Shutdown complete.");
//        System.exit(0);
    }
}