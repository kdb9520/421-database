package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Attr;

public class DMLParser {

    public static void handleQuery(String query, String databaseLocation) {

        if (query.startsWith("insert into ")) {
            insert(query.substring(12));
        }

        else if(query.startsWith("update ")){
            update(query.substring(7));
        }

        else if(query.startsWith("delete ")){
            delete(query.substring(12));
        }

        else if (query.strip().equals("display schema;")) {
            displaySchema(databaseLocation);
        }

        else if (query.startsWith("display info ")) {
            displayInfo(query.substring(13));
        } else if (query.startsWith("select")) {
            select(query.substring(6));
        }
    }

    private static void update(String substring) {
        //
        String [] split = substring.split(" ");
        String tableName = split[0];
        String columnName = split[2]; // <name> set <columnName>
        String valueString = split[4];
        TableSchema tSchema = Catalog.getTableSchema(tableName);
        // Get the where clause
        // Construct the WHERE clause from split[6] to the end
        String whereClause = String.join(" ", Arrays.copyOfRange(split, 5, split.length));
        whereClause = whereClause.substring(0,whereClause.length()-1);
        WhereParser wp = new WhereParser();
        WhereNode whereTree = wp.parse(whereClause);
        ArrayList<String> variableNames = wp.getVariableNames();

        // Now we can do some error checking here with type of valueString matching the schema
        int colNum = tSchema.findAttribute(columnName);
        AttributeSchema aSchema = tSchema.findAttributeSchema(colNum);
        String colType = aSchema.getType();

        // Check if the types equal, to do this we need to brute force test for each type
        String valType = getType(valueString);

        if(valType.equals("null")){
            // Do nothing
        }
        else if(!valType.equals(colType)){
            // Constants are all marked as a varchar, just check and make sure our variable isn't a char. If it is a char its a valid comparison
            if((!(colType.startsWith("varchar") && valType.startsWith("char")) || colType.startsWith("char") && valType.startsWith("varchar"))){
                System.err.println("Types of column and constant do not match. Aborting update.");
                return;
            }
        }

        // We now know that the constants type matches the column type

        // For each page go and see if we need to update record
        int num_pages = tSchema.getIndexList().size();
        for (int i = 0; i < num_pages; i++) {
            Page page = BufferManager.getPage(tSchema.tableName, i);
            for(int j = 0; j < page.getRecords().size(); j++){
                Record r = page.getRecords().get(j);
                ArrayList<Object> variables = new ArrayList<>();
                // Get all variables
                for (String varName : variableNames){
                    // First figure out what index of the var is in the record
                    int index = tSchema.findAttribute(varName);
                    variables.add(r.getAttribute(index));
                }
                if(whereTree.evaluate(variables, wp.getVariableNames(), tSchema)){
                    page.updateValue(j,colNum, valueString, colType);
                    
                    System.out.println(r.prettyPrint(tableName));
                    // Then need to find out which pages to put the edited values in
                    // Obviously only if PK col is edited
                }
            }

        }


        return;
    }  

    // Gets type of a string
    private static String getType(String string){
        if (string.equals("true") || string.equals("false")){
           return("boolean");
        }
        else if(string.startsWith("'") || string.startsWith("\"")){
            return("varchar");
        }
        else if(string.equals(null)){
            return("null");
        }
        // Now need to check if its integer or double,if not its a varNode
    
        try {
            Integer number = Integer.parseInt(string);
            return("integer");
          } catch (NumberFormatException e) {
          }
    
          try {
            double number = Double.parseDouble(string);
            return("double");
          } catch (NumberFormatException e) {
          }
    
          return null;
    }

    private static void delete(String substring) {
        String [] split = substring.split(" ");
        String tableName = split[0];
        if(Catalog.getTableSchema(tableName) == null){
            System.err.println("Table does not exist");
        }

        TableSchema tableSchema = Catalog.getTableSchema(tableName);
        String whereClause = String.join(" ", Arrays.copyOfRange(split, 1, split.length));
        deleteRecord(tableSchema, whereClause);



    }

    /**
     * Deletes records from a table given a condition
     * @param tableSchema - the name of the table
     * @param whereClause - the condition
     */
    public static void deleteRecord(TableSchema tableSchema, String whereClause){


        // Print all values in table
        // Loop through the table and print each page
        // For each page in table tableName
        int num_pages = tableSchema.getIndexList().size();
        for (int i = 0; i < num_pages; i++) {
            Page page = BufferManager.getPage(tableSchema.tableName, i);
            ArrayList<Record> records = page.getRecords();
            for(Record r : records){
                if(true){   //todo - wait for where clause implementation
                    page.removeRecord(r);
                }
            }

        }

    }

    public static void insert(String query) {
        String[] splitQuery = query.split(" ", 2);
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
            System.err.println("Table: " + tableName + " does not exist");
            return;
        }

        String[] tuples = remaining.split(",");

        boolean previousRecordFail = false;
        for (String tuple : tuples) {

            if (previousRecordFail) {
                break;
            }

            String valString = tuple.strip().split("[()]")[1];
            ArrayList<String> attrs = parseStringValues(valString);

            ArrayList<Object> values = new ArrayList<>();

            ArrayList<AttributeSchema> attributeSchemas = tableSchema.getAttributeSchema();

            if (attrs.size() > attributeSchemas.size() || attrs.size() < attributeSchemas.size()) {
                // print error and go to command loop
                System.out.println("Error with inserting record: " + tuple);
                System.out
                        .println("Expected " + attributeSchemas.size() + " values but got " + attrs.size() + " values");

                System.out.println(
                        "If there were records inputted previous to this record, they have been successfuly inserted.");
                System.out.println("All records after the failed record were not inserted.");
                previousRecordFail = true;
                break;
            }

            // This does all the parsing and converting the attributes to correct type based
            // off schema
            try {
                for (int i = 0; i < attributeSchemas.size(); i++) {
                    String type = attributeSchemas.get(i).getType();
                    String value = attrs.get(i);

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
                            // account for "" on either side of val
                            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                                    (value.startsWith("'") && value.endsWith("'")))
                                values.add(value.substring(1, value.length() - 1));
                            else {
                                throw new IllegalArgumentException("Invalid value for varchar type: " + value);
                            }
                            // // If it has char(size) characters or less, pad if needed and at it
                            // if (value.length() <= numberOfChars + 2) { // Check if it's right length
                            // excluding the ''
                            // String concatValue = value.substring(1, value.length() - 1);
                            // String paddedString = String.format("%-" + numberOfChars + "s", concatValue);
                            // values.add(paddedString);
                            // } else {
                            // throw new IllegalArgumentException("Invalid value for char type: " + value);
                            // }
                        } else if (type.startsWith("char")) {
                            // account for '' on either side of val
                            // Get the number between ()
                            int numberOfChars = Integer
                                    .parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));

                            // If not wrapped in a '' then we know its not a char
                            if ((!value.startsWith("'") || !value.endsWith("'")) &&
                                    (!value.startsWith("\"") || !value.endsWith("\""))) {
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
                e.printStackTrace();
                System.out.println(e.getMessage());
                System.out.println(
                        "If there were records inputted previous to this record, they have been successfuly inserted.");
                System.out.println("All records after the failed record were not inserted.");
                previousRecordFail = true;
                break;
            }

            // We now have the record made corectly, we need to insert it into right place
            Record record = new Record(values);

            if (!checkUnique(tableName, record, attributeSchemas)) {
                System.out.println("\nError: A record with that unique value already exists.");
                System.out.println("Tuple " + tuple + " not inserted!\n");
                return;
            }

            // If the table is empty, no pages exist. Create a new page
            if (tableSchema.getIndexList().size() == 0) {

                if (!checkUnique(tableName, record, attributeSchemas)) {
                    System.out.println("\nError: A record with that unique value already exists.");
                    System.out.println("Tuple " + tuple + " not inserted!\n");
                    return;
                }
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

                for (int i = 0; i < numPages; i++) {
                    Page page = BufferManager.getPage(tableName, i);
                    for (Record r : page.getRecords()) {
                        if (r.getAttribute(primaryKeyCol).equals(record.getAttribute(primaryKeyCol))) {
                            System.out.println("Error: A record with that primary key already exists.");
                            System.out.println("Tuple " + tuple + " not inserted!\n");
                            return;
                        }
                    }
                }

                boolean wasInserted = false;
                // Loop through pages and find which one to insert record into. Look ahead
                // algorithim
                Page next = null;
                for (int i = 0; i < tableSchema.getIndexList().size(); i++) {
                    // See if we are going to be out of bounds
                    if (i + 1 >= tableSchema.getIndexList().size()) {
                        break;
                    }
                    // Get the next page (must use src.BufferManager to get it)
                    next = BufferManager.getPage(tableName, i + 1);


                    Record firstRecordOfNextPage = next.getFirstRecord();

                    // If its less than the first value of next page (i+1) then it belongs to page i
                    // Type cast appropiately then compare records
                    if (Page.isLessThan(record, firstRecordOfNextPage, tableName)) {
                        // Add record to current page
                        Page page = BufferManager.getPage(tableName, i);
                        
                        Page splitPage = page.addRecord(record);
                        wasInserted = true;
                        if (splitPage != null) { // If we split update stuff as needed
                            tableSchema.addToIndexList(i+1,numPages);
                            // Update all pages in the buffer pool list to have the correct page number
                            BufferManager.updatePageNumbersOnSplit(tableName, splitPage.getPageNumber());
                            BufferManager.addPageToBuffer(splitPage);
                            // Break out of for loop; go to next row to insert
                            break;
                        }
                        break;
                    }
                }

                // Cycled through all pages -> src.Record belongs on the last page of the table

                if (!wasInserted) {
                    if (!checkUnique(tableName, record, attributeSchemas)) {
                        System.out.println("\nError: A record with that unique value already exists.");
                        System.out.println("Tuple " + tuple + " not inserted!\n");
                        return;
                    }
                    
                    // Insert the record into the last page of the table
                    Page lastPage = BufferManager.getPage(tableName, numPages - 1).addRecord(record);

                    if (lastPage != null) {
                        tableSchema.addToIndexList(numPages);
                        // Update all pages in the buffer pool list to have the correct page number
                        BufferManager.updatePageNumbersOnSplit(tableName, lastPage.getPageNumber());
                        BufferManager.addPageToBuffer(lastPage);
                    }
                }
            }
        }
    }

    public static ArrayList<String> parseStringValues(String input) {
        ArrayList<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;
        char quoteType = '\0'; // Tracks the current type of quote

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // Check for quote characters only if we're not currently escaping characters
            if ((c == '"' || c == '\'') && (i == 0 || input.charAt(i - 1) != '\\')) {
                if (inQuotes) {
                    // If we're inside quotes and see the same quote type, we're closing it
                    if (c == quoteType) {
                        inQuotes = false; // We're closing the quote
                    }
                } else {
                    inQuotes = true; // Opening quotes
                    quoteType = c; // Remember the quote type
                }
                currentValue.append(c); // Append quote to current value
            } else if (c == ' ' && !inQuotes) {
                // If we encounter a space outside of quotes, add the current value to the list
                // (if not empty)
                if (currentValue.length() > 0) {
                    values.add(currentValue.toString());
                    currentValue.setLength(0); // Reset the StringBuilder for the next value
                }
            } else {
                // Append the current character to the current value
                currentValue.append(c);
            }
        }

        // Don't forget to add the last value if it exists
        if (currentValue.length() > 0) {
            values.add(currentValue.toString());
        }

        return values;
    }

    public static void select(String query) {
        String[] splitQuery = query.strip().split(" ");
        String[] tableNames = QueryHandler.getTableNamesFromSelect(query);
        ArrayList<TableSchema> tableSchemas = new ArrayList<>();
        for (String t : tableNames) {
            TableSchema tbs = Catalog.getTableSchema(t);
            if (tbs == null) {
                System.err.println("Table: " + t + " does not exist!");
                return;
            }
            tableSchemas.add(tbs);
        }

        TableSchema tableSchema = ((tableSchemas.size() > 1) ? tableCartesian(tableSchemas) : tableSchemas.get(0));

        if (tableSchema == null) {
            System.err.println("Error when calculating Cartesian of tables.");
            return;
        }

        if (splitQuery[0].equals("*")) {

            // need to test formating of toStrings
            // todo: update the padding to be the highest varchar length or something like
            // that
            System.out.println(tableSchema.prettyPrint());

            // Print all values in table
            // Loop through the table and print each page
            // For each page in table tableName
            int num_pages = tableSchema.getIndexList().size();
            for (int i = 0; i < num_pages; i++) {
                Page page = BufferManager.getPage(tableSchema.getTableName(), i);
                System.out.println(page.prettyPrint());
            }

            
        } else {
            
            // Split the input query into parts
            String[] parts = query.split("\\s+");
            
            // Initialize lists for attributes, tables, and the where clause
            ArrayList<String> attributes = new ArrayList<>();
            ArrayList<String> tables = new ArrayList<>();
            String whereClause = null;
            
            // Flags to track whether "from" and "where" keywords are found
            boolean fromFound = false;
            boolean whereFound = false;
            
            // Loop through the parts of the query
            for (int i = 0; i < parts.length; i++) {
                
                String part = parts[i].toLowerCase();

                if (part.length() == 0) {
                    continue;
                }

                // take semicolon or comma off of the end of the part
                part = (part.endsWith(";") || part.endsWith(",")) ? part.substring(0, part.length() - 1) : part;
                
                // Check for "from" keyword
                if (part.equals("from")) {
                    fromFound = true;
                    continue;
                }
                
                // Check for "where" keyword
                if (part.equals("where")) {
                    whereFound = true;
                    // The remaining part of the query after "where" is the where clause
                    whereClause = String.join(" ", List.of(parts).subList(i + 1, parts.length));
                    break; // Exit loop since we found "where"
                }
                
                // Add attributes and tables based on whether "from" has been found
                if (!fromFound) {
                    if (!part.equals("select") && !part.equals(",")) {
                        attributes.add(part);
                    }
                } else if (!whereFound) {
                    if (!part.equals(",") && !part.equals("and")) {
                        tables.add(part);
                    }
                }
            }
        
            // Check for errors
            if (!fromFound) {
                System.out.println("Error: 'from' keyword not found.");
                return;
            }
            
            if (tables.size() == 0) {
                System.out.println("Error: No tables found.");
                return;
            }

            SelectOutput selectOutput = buildAttributeTable(attributes, tableSchema, whereClause);
            if (selectOutput != null) {
                printSelectTable(selectOutput);
            }
        }

        if (Catalog.getTableSchema("tempcartesian") != null) {
            Catalog.removeSchema("tempcartesian");
            StorageManager.deleteTable("tempcartesian");
        }
    }

    /**
     * tableCartesian calculates and returns a cartesian product of all the values in given tables.
     * Writes a temporary table 'tempcartesian' to the hardware for later use in SELECT and WHERE.
     * @param tableSchemas a list of tableSchemas to take the cartesian of.
     * @return temporary src.TableSchema with cartesian records
     */
    private static TableSchema tableCartesian(ArrayList<TableSchema> tableSchemas) {
        ArrayList<AttributeSchema> as = new ArrayList<>();
        ArrayList<Record> oldCartesian = new ArrayList<>();
        ArrayList<Record> newCartesian;
        TableSchema temp;

        // loop through our TableSchemas
        for (int i = 0; i < tableSchemas.size(); i++) {

            // add attributes as tablename.attrname
            for (AttributeSchema a : tableSchemas.get(i).attributes) {
                String newName = tableSchemas.get(i).tableName + "." + a.attrName;
                as.add(new AttributeSchema(newName, a.attrType, a.isNotNull, a.isPrimaryKey, a.isUnique));
            }

            ArrayList<Record> toAdd = new ArrayList<>();
            TableSchema current = tableSchemas.get(i);

            // get the records in the tables so we can cartesian
            int numPages = current.getIndexList().size();
            if (numPages != 0) {

                int j = 0;
                // add all old records to new array
                do {
                    Page page = BufferManager.getPage(current.tableName, j);
                    ArrayList<Record> t = page.getRecords();
                    toAdd.addAll(t);
                    j += 1;
                } while (j < numPages);
            }

            newCartesian = new ArrayList<>();

            // calculate the cartesian for the previous table and current table
            ArrayList<Object> tempRec = new ArrayList<>();
            if (!oldCartesian.isEmpty()) {
                for (Record o : oldCartesian) {
                    for (Record t : toAdd) {
                        tempRec.addAll(o.getValues());
                        tempRec.addAll(t.getValues());
                        newCartesian.add(new Record(tempRec));
                        tempRec = new ArrayList<>();
                    }
                }
            } else {
                newCartesian.addAll(toAdd);
            }

            oldCartesian = newCartesian;
        }

        // get rid of primary keys and uniques
        for (AttributeSchema a : as) {
            a.isPrimaryKey = false;
            a.isUnique = false;
        }

        // make our own primary key to id rows and sort
        as.add(new AttributeSchema("row_id", "integer", false, true, false));
        int rowId = 0;

        newCartesian = new ArrayList<>();
        for (Record r : oldCartesian) {
            ArrayList<Object> vals = r.getValues();
            vals.add(rowId++);
            newCartesian.add(new Record(vals));
        }

        // make the temp table and insert values into it
        if (!newCartesian.isEmpty()) {
            temp = new TableSchema("tempcartesian", as);
            Catalog.updateCatalog(temp);
            StorageManager.writeTableToDisk(temp.tableName);
            // TODO: DELETE THE TEMP TABLE AFTER USE
            String insertQuery = QueryHandler.buildInsertQuery(newCartesian, temp);
            insert(insertQuery);

            return temp;
        }

        return null;
    }

    private static SelectOutput buildAttributeTable (ArrayList<String> attributes, TableSchema tableSchema, String whereClause) {

        ArrayList<Record> recordOutput = new ArrayList<>();

        // Only display the specified attributes from table
        if (whereClause == null) {

            // for each record in the table
            // for each attribute index in attributes
            // add to tuple builder

            ArrayList<AttributeSchema> attributeSchemas = new ArrayList<>();
            ArrayList<Integer> indecies = new ArrayList<>();
            for (String attr : attributes) {
                if (tableSchema.getAttributeNames().contains(attr)) {
                    int attrIndex = tableSchema.findAttribute(attr);
                    indecies.add(attrIndex);
                    attributeSchemas.add(tableSchema.findAttributeSchema(attrIndex));
                }
                else {
                    System.err.println("Error: Attribute '" + attr + "' is not present in any table!");
                    return null;
                }
            }

            TableSchema selectTableSchema = new TableSchema("selectSchema", attributeSchemas);
            for (int i = 0; i < tableSchema.getIndexList().size(); i++) {
                Page page = BufferManager.getPage(tableSchema.getTableName(), i);
                for (Record record : page.records) {
                    ArrayList<Object> recordBuilder = new ArrayList<>();
                    for (int j = 0; j < record.getValues().size(); j++) {
                        if (indecies.contains(j)) {
                            recordBuilder.add(record.getAttribute(j));
                        }
                    }
                    recordOutput.add(new Record(recordBuilder));
                }
            }

            return new SelectOutput(recordOutput, attributeSchemas);
        }
        else {
            // Handle where clause
            // First lets build our WhereTree from the whereStatement
            WhereParser wp = new WhereParser();
            WhereNode whereTree = wp.parse(whereClause);
             // for each record in the table
            // for each attribute index in attributes
            // add to tuple builder

            ArrayList<AttributeSchema> attributeSchemas = new ArrayList<>();
            ArrayList<Integer> indecies = new ArrayList<>();
            for (String attr : attributes) {
                if (tableSchema.getAttributeNames().contains(attr)) {
                    int attrIndex = tableSchema.findAttribute(attr);
                    indecies.add(attrIndex);
                    attributeSchemas.add(tableSchema.findAttributeSchema(attrIndex));
                }
                else {
                    System.err.println("Error: Attribute '" + attr + "' is not present in any table!");
                    return null;
                }
            }

            TableSchema selectTableSchema = new TableSchema("selectSchema", attributeSchemas);
            for (int i = 0; i < tableSchema.getIndexList().size(); i++) {
                Page page = BufferManager.getPage(tableSchema.getTableName(), i);
                for (Record record : page.records) {
                    ArrayList<Object> recordBuilder = new ArrayList<>();
                    // If WhereTree evaluates true then add it
                    // First get the name of the variables we need to solve WhereTree
                    // Then get the values for those variables
                    ArrayList<String> variableNames = wp.getVariableNames();
                    ArrayList<Object> variables = new ArrayList<>();

                    // Get all variables
                    for (String varName : variableNames){
                        // First figure out what index of the var is in the record
                        int index = tableSchema.findAttribute(varName);
                        variables.add(record.getAttribute(index));
                    }

                    if(whereTree.evaluate(variables, wp.getVariableNames(), selectTableSchema)){
                        for (int j = 0; j < record.getValues().size(); j++) {
                            if (indecies.contains(j)) {
                                recordBuilder.add(record.getAttribute(j));
                            }
                        }
                        recordOutput.add(new Record(recordBuilder));
                    } 
                }
            }

            return new SelectOutput(recordOutput, attributeSchemas);
        }
    }

    // CURRENTLY NON-FUNCTIONAL
    private static void printSelectTable (SelectOutput selectOutput){
        
        String tableName = null;// selectOutput.getTableName();
        ArrayList<Record> records = selectOutput.getRecords();
        ArrayList<AttributeSchema> attributeSchemas = selectOutput.getAttributeSchemas();

        if (records == null) {
            return;
        }

        System.out.println("Select Result: \n");
        for (Record record : records) {

            record.prettyPrint(null);

            System.out.print(attributeSchemas.get(2) + ": ");
            String type = null; //(String) attributeSchemas.get(3);
            for (int i = 4; i < attributeSchemas.size(); i++) {
                Object value = null;//attrList.get(i);
                String str = "";
                if (value == null) {
                    str = "null";
                } else if (type.equals("integer")) {
                    Integer n = (Integer) value;
                    str = n.toString();
                } else if (type.startsWith("varchar")) {
                    str = "\"" + value.toString().strip() + "\"";
                } else if (type.startsWith("char")) {
                    str = "\"" + value.toString().strip() + "\"";
                } else if (type.equals("double")) {
                    Double d = (Double) value;
                    str = d.toString();
                } else if (type.equals("boolean")) {
                    Boolean b = (Boolean) value;
                    str = b.toString();
                }
                System.out.print(str + " | ");
            }
            System.out.println("\n");
        }
    }

    private static void displaySchema(String databaseLocation) {

        System.out.println("Database Location: " + databaseLocation + "\nsrc.Page Size: " + Main.pageSize
                + "\nBuffer Size: " + Main.bufferSize + "\nTable Schema: ");

        Catalog.getTableSchemas().forEach((System.out::println));
    }

    private static boolean displayInfo(String tableName) {

        tableName = tableName.strip().split(";")[0];

        TableSchema tableSchema = Catalog.getTableSchema(tableName);

        if (tableSchema != null) {

            String schema = tableSchema.toString();

            // int pageNumber = src.Catalog.getCatalog().getPageNumber(tableName);//
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

    // Returns true if either no attrs are isUnique or if the isUnique rule is held
    // successfully
    // Returns false if there is a unique value about to be overwritten
    private static boolean checkUnique(String tableName, Record record, ArrayList<AttributeSchema> attrSchemas) {

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
                        if (r.getAttribute(indeciesOfUnique.get(j)) != null && r.getAttribute(indeciesOfUnique.get(j))
                                .equals(record.getAttribute(indeciesOfUnique.get(j)))) {
                            return false;
                        }
                    }
                }
            }
        } else {
            return true;
        }
        return true;
    }
}
