import java.nio.Buffer;
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

            if (previousRecordFail) {
                break;
            }

            String valString = tuple.strip().split("[()]")[1];
            String[] attrs = valString.strip().split(" ");

            ArrayList<Object> values = new ArrayList<>();

            ArrayList<AttributeSchema> attributeSchemas = tableSchema.getAttributeSchema();

            if(attrs.length > attributeSchemas.size() || attrs.length < attributeSchemas.size()){
                // print error and go to command loop
                System.out.println("Error with inserting record: " + tuple);
                System.out.println("Expected " + attributeSchemas.size() + " values but got " + attrs.length + " values");

                System.out.println(
                        "If there were records inputted previous to this record, they have been successfuly inserted.");
                System.out.println("All records after the failed record were not inserted.");
                previousRecordFail = true;
                break;
            }

            // This does all the parsing and converting the attributes to correct type based off schema
            try {
                for (int i = 0; i < attributeSchemas.size(); i++) {
                    String type = attributeSchemas.get(i).getType();
                    String value = attrs[i];

                    if (value.equals("null")) {
                        
                        if (attributeSchemas.get(i).getIsNotNull()) {
                            throw new IllegalArgumentException("Invalid 'null' value for not null attribute");
                        }
                        values.add(null);

                    } else {
                        if (type.equals("integer")) {
                            // Check if the value consists only of numeric characters
                            if (value.matches("\\d+")) {
                                values.add(Integer.parseInt(value));
                            } else {
                                throw new IllegalArgumentException("Invalid value for integer type: " + value);
                            }
                        } else if (type.startsWith("varchar")) {
                            int numberOfChars = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));
                            // account for "" on either side of val
                            if (String.valueOf(value.charAt(0)).equals("'")
                                    && String.valueOf(value.charAt(value.length() - 1)).equals("'"))
                                values.add(value.substring(1, value.length() - 1));
                            else {
                                throw new IllegalArgumentException("Invalid value for varchar type: " + value);
                            }
                            // If it has char(size) characters or less, pad if needed and at it
                            if (value.length() <= numberOfChars + 2) { // Check if it's right length excluding the ''
                                String concatValue = value.substring(1, value.length() - 1);
                                String paddedString = String.format("%-" + numberOfChars + "s", concatValue);
                                values.add(paddedString);
                            } else {
                                throw new IllegalArgumentException("Invalid value for char type: " + value);
                            }
                        } else if (type.startsWith("char")) {
                            // account for '' on either side of val
                            // Get the number between ()
                            int numberOfChars = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));

                            // If not wrapped in a '' then we know its not a char
                            if (!value.startsWith("'") || !value.endsWith("'")) {
                                throw new IllegalArgumentException("Invalid value for char type: " + value);
                            }

                            // If it has char(size) characters or less, pad if needed and at it
                            if (value.length() <= numberOfChars + 2) { // Check if it's right length excluding the ''
                                String concatValue = value.substring(1, value.length() - 1);
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

            // We now have the record made corectly, we need to insert it into right place
            Record record = new Record(values);


            // If the table is empty, no pages exist. Create a new page
            if (tableSchema.getIndexList().size() == 0) {
                // Create new page (using bufferManager)
                Page newPage = BufferManager.createPage(tableName, 0);
                // add this entry to a new page
                newPage.addRecord(record);
                tableSchema.addToIndexList(0);
            // Else the table is not empty! We need to find where to insert this record now
            } else {
                // Get the primary key and its type so we can compare
                int numPages = tableSchema.getIndexList().size();

                // Get primary key col number so we can figure out where to insert this record
                int primaryKeyCol = tableSchema.findPrimaryKeyColNum();
                String primaryKeyType = tableSchema.getPrimaryKeyType();

                boolean primaryKeyOverwriting = false;
                for (int i = 0; i < numPages; i++) {
                    Page page = BufferManager.getPage(tableName, i);
                    for (Record r : page.getRecords()) {
                        if (r.getAttribute(primaryKeyCol).equals(record.getAttribute(primaryKeyCol))) {
                            primaryKeyOverwriting = true;
                        }
                    }
                }

                if (primaryKeyOverwriting == false) {
                    // Loop through pages and find which one to insert record into. Look ahead algorithim
                    Page next = null;
                    for (int i = 0; i < tableSchema.getIndexList().size(); i++) {
                        // See if we are going to be out of bounds
                        int sizetest = tableSchema.getIndexList().size();
                        if (i + 1 >= tableSchema.getIndexList().size()) {
                            break;
                        }
                        // Get the next page (must use BufferManager to get it)
                        next = BufferManager.getPage(tableName, i + 1);
                        Record firstRecordOfNextPage = next.getFirstRecord();

                        // If its less than the first value of next page (i+1) then it belongs to page i
                        // Type cast appropiately then compare records
                        if(Page.isLessThan(record, firstRecordOfNextPage, tableName)){
                            // Add record to current page
                            Page page = BufferManager.getPage(tableName, i);
                            Page splitPage = page.addRecord(record);
                            if (splitPage != null) { // If we split update stuff as needed
                                tableSchema.addToIndexList(numPages);
                                // Update all pages in the buffer pool list to have the correct page number
                                BufferManager.updatePageNumbersOnSplit(tableName, splitPage.getPageNumber());
                                BufferManager.addPageToBuffer(splitPage);
                            }
                        }
                        }
                    }

                    // Cycled through all pages -> Record belongs on the last page of the table

                    // Insert the record into the last page of the table
                    Page lastPage = BufferManager.getPage(tableName, numPages - 1).addRecord(record);

                if (lastPage != null) {
                        tableSchema.addToIndexList(numPages);
                        // Update all pages in the buffer pool list to have the correct page number
                        BufferManager.updatePageNumbersOnSplit(tableName, lastPage.getPageNumber());
                        BufferManager.addPageToBuffer(lastPage);
                    }
                
            }
            //else {
                    //System.out.println("Error: A record with that primary key already exists.");
                    //System.out.println("Tuple " + tuple + " not inserted!\n");
                //}
        }
    }

    public static void select(String query) {
        String[] splitQuery = query.strip().split(" ");
        if (splitQuery[0].equals("*")) {
            String tableName = splitQuery[2];
            // gets rid of semicolon after table name
            tableName = tableName.substring(0, tableName.length() - 1);
            TableSchema tableSchema = Catalog.getTableSchema(tableName);
            if (tableSchema != null) {
                // need to test formating of toStrings
                // todo: update the padding to be the highest varchar length or something like that
                System.out.println(tableSchema.prettyPrint());

                // Print all values in table
                // Loop through the table and print each page
                // For each page in table tableName
                int num_pages = tableSchema.getIndexList().size();
                for (int i = 0; i < num_pages; i++) {
                    Page page = BufferManager.getPage(tableName, i);
                    System.out.println(page.prettyPrint());
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

    // Returns true if either no attrs are isUnique or if the isUnique rule is held successfully
    // Returns false if there is a unique value about to be overwritten
    private static boolean checkUnique (String tableName, Record record, ArrayList<AttributeSchema> attrSchemas) {

        ArrayList<Integer> indeciesOfUnique = new ArrayList<>();
        for (int i = 0; i < attrSchemas.size(); i++) {
            if (attrSchemas.get(i).getIsUnique()) {
                indeciesOfUnique.add(i);
            }
        }

        if (indeciesOfUnique.size() >= 0) {
            
            TableSchema tableSchema = Catalog.getTableSchema(tableName);

            int numPages = tableSchema.getIndexList().size();

            for (int i = 0; i < numPages; i++) {
                Page page = BufferManager.getPage(tableName, i);
                for (Record r : page.getRecords()) {
                    for (int j = 0; j < indeciesOfUnique.size(); j++) {
                        if (r.getAttribute(indeciesOfUnique.get(i)).equals(record.getAttribute(indeciesOfUnique.get(i)))) {
                            return false;
                        }
                    }
                }
            }
        }
        else {
            return true;
        }
        return true;
    }
}
