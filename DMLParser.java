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
        String tableName = query.substring(0, query.indexOf('('));
        String remaining = query.substring(query.indexOf('('));
        TableSchema tableSchema = Catalog.getTableSchema(tableName);

        String[] tuples = remaining.split(",");

        for (String tuple : tuples) {
            String valString = tuple.strip().split("[()]")[1];
            ArrayList<Object> values = new ArrayList<>();
            String[] attrs = valString.strip().split(" ");
            for (int i = 0; i < attrs.length; i++) {
                values.add(attrs[i]);
            }
            
            Record record = new Record(values);
            int numPages = StorageManager.readNumberOfPages(tableName);  
             // if there are no pages
            if (numPages == 0) {
                Page newPage = BufferManager.createPage(tableName, 0);
                // add this entry to a new page
                newPage.addRecord(record);
            } else {
                // Get the primary key and its type so we can compare
                
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
                    next = BufferManager.getPage(tableName,i + 1);

                    // If its less than the first value of next page (i+1) then it belongs to page i
                    // Type cast appropiately then compare records
                    if (primaryKeyType == "Integer") {
                        if ((Integer) record.getAttribute(primaryKeyCol) < (Integer) next.getFirstRecord(i)) {
                            // Add the record to the page. Check if it split page or not
                            Page result = BufferManager.getPage(tableName,i).addRecord(record);
                            // If we split then add the new page to our page list.
                            if (result != null) {
                                BufferManager.addPageToBuffer(result);
                                return;
                            }
                            return;
                        }
                    }

                    else if (primaryKeyType == "String") {

                        if (record.getAttribute(primaryKeyCol).toString()
                                .compareTo(next.getFirstRecord(i).toString()) <= 0) {
                            // Add the record to the page. Check if it split page or not
                            Page result = BufferManager.getPage(tableName,i).addRecord(record);
                            // If we split then add the new page to our page list.
                            if (result != null) {
                                BufferManager.addPageToBuffer(result);
                                return;
                            }
                            return;
                        }
                    }

                    else if (primaryKeyType == "Char") {
                        if ((char) record.getAttribute(primaryKeyCol) < (char) next.getFirstRecord(i)) {
                            // Add the record to the page. Check if it split page or not
                            Page result = BufferManager.getPage(tableName,i).addRecord(record);
                            // If we split then add the new page to our page list.
                            if (result != null) {
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

        }

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
