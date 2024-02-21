import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import javax.management.Query;

public class DMLParser {

    public static void handleQuery(String query, String databaseLocation) {

        if (query.startsWith("insert into ")) {
            insert(query.substring(12));
        }

        else if (query.startsWith("display schema ")) {
            displaySchema(query.substring(15), databaseLocation);
        }

        else if (query.startsWith("display info ")) {
            displayInfo(query.substring(13));
        } else if (query.startsWith("select")) {
            select(query.substring(6));
        }
    }

    public static void insert(String query) {
        String[] splitQuery = query.split(" ", 3);
        String tableName = splitQuery[0];
        String remaining = "";
        if (query.contains("(")) {
            remaining = query.substring(query.indexOf('('));
        } else {
            System.err.println("Invalid insert no values provided");
            return;
        }
        TableSchema tableSchema = Catalog.getTableSchema(tableName);
        if (tableSchema == null) {
            System.err.println("Table: " + tableName + "does not exist");
            return;
        }

        String[] tuples = remaining.split(",");

        boolean previousRecordFail = false;
        for (String tuple : tuples) {

            if (previousRecordFail == true) {
                break;
            }

            String valString = tuple.strip().split("[()]")[1];
            String[] attrs = valString.strip().split(" ");

            ArrayList<Object> values = new ArrayList<>();

            ArrayList<AttributeSchema> attributeSchemas = tableSchema.getAttributeSchema();

            try {
                for (int i = 0; i < attributeSchemas.size(); i++) {
                    String type = attributeSchemas.get(i).getType();
                    String value = attrs[i];

                    if (type.equals("integer")) {
                        // Check if the value consists only of numeric characters
                        if (value.matches("\\d+")) {
                            values.add(Integer.parseInt(value));
                        } else {
                            throw new IllegalArgumentException("Invalid value for integer type: " + value);
                        }
                    } else if (type.startsWith("varchar")) {
                        // account for "" on either side of val
                        if (String.valueOf(value.charAt(0)).equals("\"")
                                && String.valueOf(value.charAt(value.length() - 1)).equals("\""))
                            values.add(value.substring(1, value.length() - 1));
                        else {
                            throw new IllegalArgumentException("Invalid value for varchar type: " + value);
                        }
                    } else if (type.startsWith("char")) {
                        // account for '' on either side of val
                        // Get the number between ()
                        int numberOfChars = Integer.parseInt(type.substring(type.indexOf("(")+1, type.indexOf(")")));
                        // If it has char(size) characters or less, pad if needed and at it
                        if (value.length() <= numberOfChars+2) { // Check if it's right length excluding the ''
                            String concatValue = value.substring(1, value.length()-1);
                            String paddedString = String.format("%-" + numberOfChars + "s", concatValue);
                            values.add(paddedString);
                        } else {
                            throw new IllegalArgumentException("Invalid value for char type: " + value);
                        }
                    } else if (type.equals("double")) {
                        // Check if the value is a valid double (numeric characters with optional
                        // decimal point)
                        if (value.matches("-?\\d+(\\.\\d+)?")) {
                            values.add(Double.parseDouble(value));
                        } else {
                            throw new IllegalArgumentException("Invalid value for double type: " + value);
                        }
                    } else if (type.equals("boolean")) {
                        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                            values.add(Boolean.parseBoolean(value));
                        } else {
                            throw new IllegalArgumentException("Invalid value for boolean type: " + value);
                        }
                    }
                }
            } catch (Exception e) {
                // print error and go to command loop
                System.out.println("Error with inserting record: " + tuple);
                System.out.println(e.getMessage());
                System.out.println(
                        "If there were records inputted previous to this record, they have been successfuly inserted.");
                System.out.println("All records after the failed record were not inserted.");
                previousRecordFail = true;
                break;
            }

            Record record = new Record(values);

            if (tableSchema.getIndexList().size() == 0) {
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
                    if (primaryKeyType.equals("integer")) {
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
                    } else if (primaryKeyType.equals("string")) {

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
                    } else if (primaryKeyType.equals("char")) {
                        String recordString = (String) record.getAttribute(primaryKeyCol);
                        String nextRecordString = (String)  next.getFirstRecord(i);
                        if (recordString.compareTo(nextRecordString) <= 0) {
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
            if (tableSchema != null) {
                // need to test formating of toStrings
                System.out.println(tableSchema.toString());

                // Print all values in table
                // Loop through the table and print each page
                // For each page in table tableName
                int num_pages = tableSchema.getIndexList().size();
                for (int i = 0; i < num_pages; i++) {
                    Page page = BufferManager.getPage(tableName, i);
                    System.out.println(page.toString());
                }

            } else {
                System.err.println("Table: " + tableName + "does not exist");
            }

        }

    }

    private static boolean displaySchema(String tableName, String databaseLocation) {

        tableName = tableName.strip().split(";")[0];

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

        tableName = tableName.strip().split(";")[0];

        TableSchema tableSchema = Catalog.getTableSchema(tableName);

        if (tableSchema != null) {

            String schema = tableSchema.toString();

            // int pageNumber = Catalog.getCatalog().getPageNumber(tableName);//
            // table.numPages;
            int numOfPages = tableSchema.getIndexList().size();
            int numOfRecords = 0;

            for (int i = 0; i < numOfPages; i++) {
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
