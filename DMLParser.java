import java.util.LinkedList;

import javax.management.Query;

public class DMLParser {

    public static void handleQuery(String query, String databaseLocation) {

        if (query.substring(0, 11).equals("insert into ")) {
            insert(query);
        }

        else if (query.substring(0, 14).equals("display schema ")) {
            displaySchema(query.substring(14), databaseLocation);
        }

        else if (query.substring(0, 12).equals("display info ")) {
            displayInfo(query.substring(12));
        }
    }

    public static void insert(String query) {
        String splitQuery[] = query.split(" ", 3);

        String tableName = splitQuery[0];

        String values = splitQuery[1];

        // splitQuery[3] - the tuple

    }

    public static void select(String query) {

    }

    private static boolean displaySchema(String tableName, String databaseLocation) {

        TableSchema tableSchema = Catalog.getCatalog().getTableSchema(tableName);
        if (tableSchema != null) {
           
            String schema = tableSchema.toString();

            System.out.println("Database Location: "+ databaseLocation + "\nPage Size: " + Main.pageSize + "\nBuffer Size: " + Main.bufferSize + "Table Schema: " + schema);
            return true;
        }
        else {
            System.out.println("Error: Table '" + tableName + "' not found");
        }
        return false;
    }

    private static boolean displayInfo(String tableName) {

        TableSchema tableSchema = Catalog.getCatalog().getTableSchema(tableName);
        
        if (tableSchema != null) {

            String schema = tableSchema.toString();

            int pageNumber = Catalog.getCatalog().getPageNumber(tableName);// table.numPages;

            LinkedList<Page> pages = table.pages;
            int numRecords = 0;
            for (Page page : pages) {
                numRecords = numRecords + page.numRecords;
            }

            System.out.println("Table: "+ tableName + "\nSchema: " + schema + "\nNumber of Pages: " + pageNumber + "\nNumber of Records: " + numRecords);
            return true;
        }
        else {
            System.out.println("Error: Table '" + tableName + "' not found");
        }

        return false;
    }

}
