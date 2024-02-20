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

        String[] tuples = remaining.split(",");

        for (String tuple : tuples) {
            String valString = tuple.strip().split("[()]")[1];
            String[] attrs = valString.strip().split(" ");
            ArrayList<Object> values = new ArrayList<>(Arrays.asList(attrs));
            
            Record record = new Record(values);


            if (tableSchema.getIndexList() == null) {
                Page newPage = BufferManager.createPage(tableName, 0);
                // add this entry to a new page
                newPage.addRecord(record);
            } else {
                // Get the primary key and its type so we can compare
                int numPages = tableSchema.getIndexList().size();
                // Get primary key col number
                int primaryKeyCol = tableSchema.findPrimaryKeyColNum();
                String primaryKeyType = tableSchema.getPrimaryKeyType();
                // Loop through pages and find which one to insert record into.
                Page next = null;
                for (int i = 0; i < numPages; i++) {
                    // See if we are out of bounds
                    if (i + 1 >= numPages) {
                        break;
                    }
                    next = BufferManager.getPage(tableName, i + 1);
    
                    // If its less than the first value of next page (i+1) then it belongs to page i
                    // Type cast appropiately then compare records
                    if (primaryKeyType.equals("Integer")) {
                        if ((Integer) record.getAttribute(primaryKeyCol) < (Integer) next.getFirstRecord(i)) {
                            // Add the record to the page. Check if it split page or not
                            Page result = BufferManager.getPage(tableName, i).addRecord(record);
    
                            // If we split then add the new page to our page list.
                            if (result != null) {
                                // Add new page to our page
                                tableSchema.getIndexList().add(i + 1, numPages);
                                BufferManager.addPageToBuffer(result);
                                // Go in and update page number of all pages current in here
                                // Update our pageIndexList, and update pageNumber every Page after this page
    
                                return;
                            }
                            return;
                        }
                    } else if (primaryKeyType.equals("String")) {
    
                        if (record.getAttribute(primaryKeyCol).toString()
                                .compareTo(next.getFirstRecord(i).toString()) <= 0) {
                            // Add the record to the page. Check if it split page or not
                            Page result = BufferManager.getPage(tableName, i).addRecord(record);
                            // If we split then add the new page to our page list.
                            if (result != null) {
                                tableSchema.getIndexList().add(i + 1, numPages);
                                BufferManager.addPageToBuffer(result);
                                return;
                            }
                            return;
                        }
                    } else if (primaryKeyType.equals("Char")) {
                        if ((char) record.getAttribute(primaryKeyCol) < (char) next.getFirstRecord(i)) {
                            // Add the record to the page. Check if it split page or not
                            Page result = BufferManager.getPage(tableName, i).addRecord(record);
                            // If we split then add the new page to our page list.
                            if (result != null) {
                                tableSchema.getIndexList().add(i + 1, numPages);
                                BufferManager.addPageToBuffer(result);
                                return;
                            }
                            return;
                        }
                    }
    
                }
                // If we make it here then the record belongs in a new page at the end of the
                // list.
                // Create page
                Page newPage = BufferManager.createPage(tableName, numPages);
                // Add record to page
                newPage.addRecord(record);
                tableSchema.getIndexList().add(numPages);
            }
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
            int numOfPages = tableSchema.getIndexList().size();
            int numOfRecords = 0;

            for(int i = 0; i < numOfPages; i++){
                Page page = BufferManager.getPage(tableName, i);
                numOfRecords += page.getRecords().size();
            }


            System.out.println("Table: " + tableName + "\nSchema: " + schema + "\nNumber of Pages: " + numOfPages
                    + "\nNumber of Records: " + numOfRecords);
            return true;
        } else {
            System.out.println("Error: Table '" + tableName + "' not found");
        }

        return false;
    }

}
