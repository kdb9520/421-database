import java.util.ArrayList;
import java.util.LinkedList;

import javax.management.Query;

public class DMLParser {

    public static void main(String[] args) {
        handleQuery("select * from <name>;", null);
    }

    public static void handleQuery(String query, String databaseLocation) {

        if (query.substring(0, 12).equals("insert into ")) {
            insert(query.substring(12));
        }

        else if (query.substring(0, 14).equals("display schema ")) {
            displaySchema(query.substring(14), databaseLocation);
        }

        else if (query.substring(0, 12).equals("display info ")) {
            displayInfo(query.substring(12));
        } else if (query.substring(0, 6).equals("select")) {
            select(query.substring(6));
        }
    }

    public static void insert(String query) {
        String splitQuery[] = query.split(" ", 3);
        String tableName = splitQuery[0];

        TableSchema tableSchema = Catalog.getTableSchema(tableName);

        String[] tuples = splitQuery[2].strip().split("[,;]");

        for (String tuple : tuples) {
            String valString = tuple.strip().split("[()]")[1];
            ArrayList<Object> values = new ArrayList<>();
            String[] attrs = valString.strip().split(" ");
            for (int i = 0; i < attrs.length; i++) {
                values.add(attrs[i]);
            }

            tableSchema.table.insert(new Record(values));

        }

    }

    public static void select(String query) {
        System.out.println("spliting");
        String[] splitQuery = query.strip().split(" ");
        System.out.println(splitQuery[0]);
        if (splitQuery[0].equals("*")) {
            String tableName = splitQuery[2];
            // gets rid of semicolon after table name
            tableName = tableName.substring(0, tableName.length() - 1);
            TableSchema tableSchema = Catalog.getTableSchema(tableName);
            if (tableSchema != null) {
                // need to test formating of toStrings
                System.out.println(tableSchema.toString());

                // Print all values in table
                // Loop through the table and print each page
                // For each page in table tableName
                int num_pages = StorageManager.readNumberOfPages(tableName);     
                for(int i = 0; i < num_pages; i++){
                    Page page = BufferManager.getPage(tableName, i);
                    System.out.println(page.toString());
                }
                
                System.out.println(tableSchema.table.toString());
            } else {
                System.err.println("Table: " + tableName + "does not exist");
            }
        }

    }

    private static boolean displaySchema(String tableName, String databaseLocation) {

        TableSchema tableSchema = Catalog.getTableSchema(tableName);
        if (tableSchema != null) {

            String schema = tableSchema.toString();

            System.out.println("Database Location: " + databaseLocation + "\nPage Size: " + Main.pageSize
                    + "\nBuffer Size: " + Main.bufferSize + "Table Schema: " + schema);
            return true;
        } else {
            System.out.println("Error: Table '" + tableName + "' not found");
        }
        return false;
    }

    private static boolean displayInfo(String tableName) {

        TableSchema tableSchema = Catalog.getTableSchema(tableName);

        if (tableSchema != null) {

            String schema = tableSchema.toString();

            // int pageNumber = Catalog.getCatalog().getPageNumber(tableName);//
            // table.numPages;
            int pageNumber = tableSchema.table.getNumberOfPages();
            LinkedList<Page> pages = tableSchema.table.getPages();
            int numRecords = 0;
            for (Page page : pages) {
                numRecords = numRecords + page.numRecords;
            }

            System.out.println("Table: " + tableName + "\nSchema: " + schema + "\nNumber of Pages: " + pageNumber
                    + "\nNumber of Records: " + numRecords);
            return true;
        } else {
            System.out.println("Error: Table '" + tableName + "' not found");
        }

        return false;
    }

}
