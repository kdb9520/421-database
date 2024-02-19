import java.util.ArrayList;
import java.util.LinkedList;

import javax.management.Query;

public class DMLParser {

    public static void main(String[] args) {
        handleQuery("insert into foo values (1 \"foo bar\" true 2.1),(3 \"baz\" true 4.14),(2 \"bar\" false 5.2),(5 \"true\" true null);", null);
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
        }
    }

    public static void insert(String query) {
        String splitQuery[] = query.split(" ", 3);
        String tableName = splitQuery[0];

        TableSchema tableSchema = Catalog.getCatalog().getTableSchema(tableName);

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

            //int pageNumber = Catalog.getCatalog().getPageNumber(tableName);// table.numPages;
            int pageNumber = tableSchema.table.getNumberOfPages();
            LinkedList<Page> pages = tableSchema.table.getPages();
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
