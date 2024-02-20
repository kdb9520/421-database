import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import javax.management.Query;

public class DMLParser {

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
        String[] splitQuery = query.split(" ", 3);
        String tableName = splitQuery[0];
        String remaining = query.substring(query.indexOf('('));
        TableSchema tableSchema = Catalog.getTableSchema(tableName);
        ArrayList<Integer> pageIndexList = tableSchema.getIndexList();
        int numPages = tableSchema.getIndexList().size(); 
        String[] tuples = remaining.split(",");

        for (String tuple : tuples) {
            String valString = tuple.strip().split("[()]")[1];
            String[] attrs = valString.strip().split(" ");
            ArrayList<Object> values = new ArrayList<>(Arrays.asList(attrs));
            
            Record record = new Record(values);


            StorageManager.insert(numPages, tableName, record, tableSchema, pageIndexList);
             // if there are no pages


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
            StorageManager.select(tableSchema, tableName);

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
