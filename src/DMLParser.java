package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DMLParser {

    public static void handleQuery(String query, String databaseLocation) {

        if (query.startsWith("insert into ")) {
            insert(query.substring(12));
        }

        else if (query.startsWith("update ")) {
            update(query.substring(7));
        }

        else if (query.startsWith("delete ")) {
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
        String[] split = substring.split("[\\s;]+|(?<=[<>!=]=)|(?=[<>!=]=)");
        String tableName = split[0];
        String columnName = split[2]; // <name> set <columnName>
        String valueString = split[4];

        TableSchema tSchema = Catalog.getTableSchema(tableName);
        // Get the where clause
        // Construct the WHERE clause from split[6] to the end
        String whereClause = String.join(" ", Arrays.copyOfRange(split, 5, split.length));
        WhereParser wp = new WhereParser();
        WhereNode whereTree = wp.parse(whereClause);
        ArrayList<String> variableNames = wp.getVariableNames();

        // Now we can do some error checking here with type of valueString matching the
        // schema
        int colNum = tSchema != null ? tSchema.findAttribute(columnName) : -1;

        if (colNum == -1) {
            System.err.println("Attribute " + columnName + " not found!");
            return;
        }

        AttributeSchema aSchema = tSchema.findAttributeSchema(colNum);
        boolean isPk = aSchema.isPrimaryKey;
        String colType = aSchema.getType();

        // Check if the types equal, to do this we need to brute force test for each
        // type
        String valType = getType(valueString);
        if (valType == null) {
            System.err.println("Type of column is null!");
            return;
        }

        // Get the value. if no wrapping ' or " return error
        if (valueString.length() >= 2 && (valueString.startsWith("'") && valueString.endsWith("'"))
                || (valueString.startsWith("\"") && valueString.endsWith("\""))) {
            // If the string starts and ends with a matching single or double quote
            valueString = valueString.substring(1, valueString.length() - 1);
        } else {
//            System.err.println("Wrapping quotes expected, please check statement and try again");
        }

        if (!valType.equals(colType) && !valType.equals("null")) {
            // Constants are all marked as a varchar, just check and make sure our variable
            // isn't a char. If it is a char it's a valid comparison
            if (!((colType.startsWith("varchar") && valType.startsWith("char"))
                    || (colType.startsWith("char") && valType.startsWith("varchar"))
                    || (colType.startsWith("varchar") && valType.startsWith("varchar")))) {
                System.err.println("Types of column and constant do not match. Aborting update.");
                return;
            }
        }

        // We now know that the constants type matches the column type

        // For each page go and see if we need to update record
        int num_pages = tSchema.getIndexList().size();
        for (int i = 0; i < num_pages; i++) {
            Page page = BufferManager.getPage(tSchema.tableName, i);
            page.toggleLock();
            for (int j = 0; j < page.getRecords().size(); j++) {
                Record r = page.getRecords().get(j);
                ArrayList<Object> variables = new ArrayList<>();
                // Get all variables
                for (String varName : variableNames) {
                    // First figure out what index of the var is in the record
                    int index = tSchema.findAttribute(varName);
                    variables.add(r.getAttribute(index));
                }
                if (whereTree.evaluate(variables, wp.getVariableNames(), tSchema)) {
                    // If not PK
                    if (!isPk) {
                        page.updateValue(j, colNum, valueString, colType);
                    } else {
                        // First remove the value from the page
                        page.removeRecord(j);

                        Record r_clone = new Record(r.cloneValues());
                        // Update its value
                        r_clone.setCol(colNum, valueString, valType);
                        System.out.println(r_clone.prettyPrint(tableName));

                        // Insert it back into the table
                        Boolean success = insert(r_clone, tableName);

                        // If insert failed then revert the record and re-insert to old spot
                        if (!success) {
                            // Update its value
                            success = insert(r, tableName);
                            System.out.println(r.prettyPrint(tableName));
                        }
                    }
                }
            }
            page.toggleLock();
        }
    }

    // Gets type of string
    private static String getType(String string) {
        if (string.equals("true") || string.equals("false")) {
            return ("boolean");
        } else if (string.startsWith("'") || string.startsWith("\"")) {
            return ("varchar");
        } else if (string.equals(null)) {
            return ("null");
        }
        // Now need to check if its integer or double, if not it's a varNode

        try {
            int number = Integer.parseInt(string);
            return ("integer");
        } catch (NumberFormatException e) {
        }

        try {
            double number = Double.parseDouble(string);
            return ("double");
        } catch (NumberFormatException e) {
        }

        return null;
    }

    private static void delete(String substring) {
        String[] split = substring.split("[\\s;]+|(?<=[<>!=]=)|(?=[<>!=]=)");
        String tableName = split[0];
        if (Catalog.getTableSchema(tableName) == null) {
            System.err.println("Table does not exist");
        }

        TableSchema tableSchema = Catalog.getTableSchema(tableName);
        String whereClause = String.join(" ", Arrays.copyOfRange(split, 1, split.length));
        if (tableSchema != null) {
            deleteRecord(tableSchema, whereClause);
        }
    }

    /**
     * Deletes records from a table given a condition
     * 
     * @param tableSchema - the name of the table
     * @param whereClause - the condition
     */
    public static void deleteRecord(TableSchema tableSchema, String whereClause) {
        WhereParser wp = new WhereParser();
        WhereNode whereTree = wp.parse(whereClause);
        ArrayList<String> variableNames = wp.getVariableNames();

        // Print all values in table
        // Loop through the table and print each page
        // For each page in table tableName
        int num_pages = tableSchema.getIndexList().size();
        for (int i = 0; i < num_pages; i++) {
            Page page = BufferManager.getPage(tableSchema.tableName, i);
            page.toggleLock();
            ArrayList<Record> records = page.getRecords();
            for (int j = 0; j < records.size(); j++) {
                Record r = records.get(j);
                ArrayList<Object> variables = new ArrayList<>();
                for (String varName : variableNames) {
                    // First figure out what index of the var is in the record
                    int index = tableSchema.findAttribute(varName);
                    variables.add(r.getAttribute(index));
                }
                if (whereTree.evaluate(variables, variableNames, tableSchema)) { // todo - wait for where clause
                                                                                 // implementation
                    page.removeRecord(j);
                    j--; // Since we removed record we
                }
            }
            page.toggleLock();

        }

    }

    public static void insert(String query) {
        String[] splitQuery = query.split(" ", 2);
        String tableName = splitQuery[0];
        String remaining;
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

        for (String tuple : tuples) {

            String valString = tuple.strip().split("[()]")[1];
            ArrayList<String> attrs = parseStringValues(valString);

            ArrayList<Object> values = new ArrayList<>();

            ArrayList<AttributeSchema> attributeSchemas = tableSchema.getAttributeSchema();

            if (attrs.size() > attributeSchemas.size() || attrs.size() < attributeSchemas.size()) {
                // print error and go to command loop
                System.err.println("Error with inserting record: " + tuple);
                System.err
                        .println("Expected " + attributeSchemas.size() + " values but got " + attrs.size() + " values");

                System.err.println(
                        "If there were records inputted previous to this record, they have been successfully inserted.");
                System.err.println("All records after the failed record were not inserted.");
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

                            // If not wrapped in a '' then we know it's not a char
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
                System.err.println("Error with inserting record: " + tuple);
                // e.printStackTrace();
                System.err.println(e.getMessage());
                System.err.println(
                        "If there were records inputted previous to this record, they have been successfully inserted.");
                System.err.println("All records after the failed record were not inserted.");
                break;
            }

            // We now have the record made correctly, we need to insert it into right place
            Record record = new Record(values);

            if (!checkUnique(tableName, record, attributeSchemas)) {
                System.err.println("\nError: A record with that unique value already exists.");
                System.err.println("Tuple " + tuple + " not inserted!\n");
                return;
            }

            // If the table is empty, no pages exist. Create a new page
            if (tableSchema.getIndexList().isEmpty()) {

                if (!checkUnique(tableName, record, attributeSchemas)) {
                    System.err.println("\nError: A record with that unique value already exists.");
                    System.err.println("Tuple " + tuple + " not inserted!\n");
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

                // Get primary key col number so that we can figure out where to insert this
                // record
                int primaryKeyCol = tableSchema.findPrimaryKeyColNum();

                for (int i = 0; i < numPages; i++) {
                    Page page = BufferManager.getPage(tableName, i);
                    for (Record r : page.getRecords()) {
                        if (r.getAttribute(primaryKeyCol).equals(record.getAttribute(primaryKeyCol))) {
                            System.err.println("Error: A record with that primary key already exists.");
                            System.err.println("Tuple " + tuple + " not inserted!\n");
                            return;
                        }
                    }
                }

                boolean wasInserted = false;
                // Loop through pages and find which one to insert record into. Look ahead
                // algorithm
                Page next = null;
                for (int i = 0; i < tableSchema.getIndexList().size(); i++) {
                    // See if we are going to be out of bounds
                    if (i + 1 >= tableSchema.getIndexList().size()) {
                        break;
                    }
                    // Get the next page (must use src.BufferManager to get it)
                    next = BufferManager.getPage(tableName, i + 1);

                    Record firstRecordOfNextPage = next.getFirstRecord();

                    // If it's less than the first value of next page (i+1) then it belongs to page
                    // 'i'
                    // Type cast appropriately then compare records
                    if (Page.isLessThan(record, firstRecordOfNextPage, tableName)) {
                        // Add record to current page
                        Page page = BufferManager.getPage(tableName, i);

                        Page splitPage = page.addRecord(record);
                        wasInserted = true;
                        if (splitPage != null) { // If we split update stuff as needed
                            tableSchema.addToIndexList(i + 1, numPages);
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
                        System.err.println("\nError: A record with that unique value already exists.");
                        System.err.println("Tuple " + tuple + " not inserted!\n");
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
                if (((currentValue.length()) > 0)) {
                    values.add(currentValue.toString());
                    currentValue.setLength(0); // Reset the StringBuilder for the next value
                }
            } else {
                // Append the current character to the current value
                currentValue.append(c);
            }
        }

        // Don't forget to add the last value if it exists
        if (((currentValue.length()) > 0)) {
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

            // need to test formatting of toStrings
            // todo: update the padding to be the highest varchar length or something like
            // that
            ArrayList<String> attrs = new ArrayList<>();

            for (AttributeSchema a : tableSchema.getAttributeSchema()) {
                if (!a.attrName.equals("row_id")) {
                    attrs.add(a.attrName);
                }
            }

            String whereClause = null;
            if (query.contains("where")) {
                whereClause = query.split("where")[1].split(";")[0];
                if (query.contains("orderby")) {
                    whereClause = whereClause.split("orderby")[0];
                }
            }

            SelectOutput selectOutput = buildAttributeTable(attrs, tableSchema, whereClause);
            //todo - should this be here
            if (query.contains("orderby")) {
                String orderByClause = query.split("orderby")[1];
                String attr = orderByClause.split(";")[0].strip();
                selectOutput.orderBy(attr, "asc");
            }
            if (selectOutput != null) {
                printSelectTable(selectOutput);
            }

        } else {

            // Split the input query into parts
            String[] parts = query.split("\\s+");

            for (String p : parts) {
                if (p.contains("*")) {
                    System.err.println("Illegal attribute arguments in Select statement.");
                    return;
                }
            }

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

                if (part.isEmpty()) {
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
                    whereClause = whereClause.split("orderby")[0].split(";")[0].strip();
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
                System.err.println("Error: 'from' keyword not found.");
                return;
            }

            if (tables.isEmpty()) {
                System.err.println("Error: No tables found.");
                return;
            }

            SelectOutput selectOutput = buildAttributeTable(attributes, tableSchema, whereClause);
            if (query.contains("orderby")) {
                String orderByClause = query.split("orderby")[1];
                String attr = orderByClause.split(";")[0].strip();
                selectOutput.orderBy(attr, "asc");
            }
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
     * tableCartesian calculates and returns a cartesian product of all the values
     * in given tables.
     * Writes a temporary table 'tempcartesian' to the hardware for later use in
     * SELECT and WHERE.
     * 
     * @param tableSchemas a list of tableSchemas to take the cartesian of.
     * @return temporary src.TableSchema with cartesian records
     */
    private static TableSchema tableCartesian(ArrayList<TableSchema> tableSchemas) {
        ArrayList<AttributeSchema> as = new ArrayList<>();
        ArrayList<Record> oldCartesian = new ArrayList<>();
        ArrayList<Record> newCartesian;
        TableSchema temp;

        // loop through our TableSchemas
        for (TableSchema tableSchema : tableSchemas) {

            // add attributes as tableName.attrName
            for (AttributeSchema a : tableSchema.attributes) {
                String newName = tableSchema.tableName + "." + a.attrName;
                as.add(new AttributeSchema(newName, a.attrType, a.isNotNull, a.isPrimaryKey, a.isUnique));
            }

            ArrayList<Record> toAdd = new ArrayList<>();

            // get the records in the tables so we can cartesian
            int numPages = tableSchema.getIndexList().size();
            if (numPages != 0) {

                int j = 0;
                // add all old records to new array
                do {
                    Page page = BufferManager.getPage(tableSchema.tableName, j);
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
            ArrayList<Object> values = r.getValues();
            values.add(rowId++);
            newCartesian.add(new Record(values));
        }

        // make the temp table and insert values into it
        if (!newCartesian.isEmpty()) {
            temp = new TableSchema("tempcartesian", as);
            Catalog.updateCatalog(temp);
            StorageManager.writeTableToDisk(temp.tableName);

            String insertQuery = QueryHandler.buildInsertQuery(newCartesian, temp);
            insert(insertQuery);

            return temp;
        }

        return null;
    }

    private static SelectOutput buildAttributeTable(ArrayList<String> attributes, TableSchema tableSchema,
            String whereClause) {

        ArrayList<Record> recordOutput = new ArrayList<>();

        // Only display the specified attributes from table
        if (whereClause == null) {

            // for each record in the table
            // for each attribute index in attributes
            // add to tuple builder

            ArrayList<AttributeSchema> attributeSchemas = new ArrayList<>();
            ArrayList<Integer> indices = new ArrayList<>();
            for (String attr : attributes) {
                if (tableSchema.getAttributeNames().contains(attr)) {
                    int attrIndex = tableSchema.findAttribute(attr);
                    indices.add(attrIndex);
                    attributeSchemas.add(tableSchema.findAttributeSchema(attrIndex));
                } else {
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
                        if (j >= indices.size()) break;

                        recordBuilder.add(record.getAttribute(indices.get(j)));
                    }
                    recordOutput.add(new Record(recordBuilder));
                }
            }

            return new SelectOutput(recordOutput, attributeSchemas);
        } else {
            // Handle where clause
            // First lets build our WhereTree from the whereStatement
            WhereParser wp = new WhereParser();
            WhereNode whereTree = wp.parse(whereClause);
            // for each record in the table
            // for each attribute index in attributes
            // add to tuple builder

            ArrayList<AttributeSchema> attributeSchemas = new ArrayList<>();
            ArrayList<Integer> indices = new ArrayList<>();
            for (String attr : attributes) {
                if (tableSchema.getAttributeNames().contains(attr)) {
                    int attrIndex = tableSchema.findAttribute(attr);
                    indices.add(attrIndex);
                    attributeSchemas.add(tableSchema.findAttributeSchema(attrIndex));
                } else {
                    System.err.println("Error: Attribute '" + attr + "' is not present in any table!");
                    return null;
                }
            }

            TableSchema selectTableSchema = new TableSchema("selectSchema", tableSchema.attributes);
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
                    for (String varName : variableNames) {
                        // First figure out what index of the var is in the record
                        int index = tableSchema.findAttribute(varName);
                        variables.add(record.getAttribute(index));
                    }

                    if (whereTree.evaluate(variables, wp.getVariableNames(), selectTableSchema)) {
                        for (int j = 0; j < record.getValues().size(); j++) {
                            if (indices.contains(j)) {
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

    private static void printSelectTable(SelectOutput selectOutput) {

        // String tableName = selectOutput.getTableName();
        ArrayList<Record> records = selectOutput.getRecords();
        ArrayList<AttributeSchema> attributeSchemas = selectOutput.getAttributeSchemas();

        if (records == null) {
            return;
        }

        TableSchema temp = new TableSchema("temp", attributeSchemas);
        System.out.println(temp.prettyPrint());
        for (Record record : records) {
            System.out.println(record.prettyPrint(attributeSchemas));
        }
        System.out.println();

    }

    private static void displaySchema(String databaseLocation) {

        System.out.println("Database Location: " + databaseLocation + "\nsrc.Page Size: " + Main.pageSize
                + "\nBuffer Size: " + Main.bufferSize + "\nTable Schema: ");

        Catalog.getTableSchemas().forEach((System.out::println));
    }

    private static void displayInfo(String tableName) {

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
        } else {
            System.out.println("Error: Table '" + tableName + "' not found");
        }
    }

    // Returns true if either no attrs are isUnique or if the isUnique rule is held
    // successfully
    // Returns false if there is a unique value about to be overwritten
    private static boolean checkUnique(String tableName, Record record, ArrayList<AttributeSchema> attrSchemas) {

        ArrayList<Integer> indicesOfUnique = new ArrayList<>();
        for (int i = 0; i < attrSchemas.size(); i++) {
            if (attrSchemas.get(i).getIsUnique()) {
                indicesOfUnique.add(i);
            }
        }

        if (!indicesOfUnique.isEmpty()) {

            TableSchema tableSchema = Catalog.getTableSchema(tableName);
            if (tableSchema == null) {
                System.err.println("TableSchema " + tableName + " is null!");
                return false;
            }

            int numPages = tableSchema.getIndexList().size();

            for (int i = 0; i < numPages; i++) {
                Page page = BufferManager.getPage(tableName, i);
                for (Record r : page.getRecords()) {
                    for (int j = 0; j < indicesOfUnique.size(); j++) {
                        if (r.getAttribute(indicesOfUnique.get(j)) != null && r.getAttribute(indicesOfUnique.get(j))
                                .equals(record.getAttribute(indicesOfUnique.get(j)))) {
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

    // Insert a record into a table (used for update)
    public static Boolean insert(Record record, String tableName) {

        TableSchema tableSchema = Catalog.getTableSchema(tableName);
        if (tableSchema == null) {
            System.err.println("Table: " + tableName + " does not exist");
            return false;
        }

        ArrayList<AttributeSchema> attributeSchemas = tableSchema.getAttributeSchema();

        // We now have the record made correctly, we need to insert it into right place

        if (!checkUnique(tableName, record, attributeSchemas)) {
            System.out.println("\nError: A record with that unique value already exists. Cancelling update");
            return false;
        }

        // If the table is empty, no pages exist. Create a new page
        if (tableSchema.getIndexList().isEmpty()) {
            // Create new page (using bufferManager)
            Page newPage = BufferManager.createPage(tableName, 0);

            // add this entry to a new page
            newPage.addRecord(record);
            tableSchema.addToIndexList(0);
            // Else the table is not empty! We need to find where to insert this record now
        } else {
            // Get the primary key and its type so we can compare
            int numPages = tableSchema.getIndexList().size();

            // Get primary key col number, so we can figure out where to insert this record
            int primaryKeyCol = tableSchema.findPrimaryKeyColNum();

            for (int i = 0; i < numPages; i++) {
                Page page = BufferManager.getPage(tableName, i);
                for (Record r : page.getRecords()) {
                    if (r.getAttribute(primaryKeyCol).equals(record.getAttribute(primaryKeyCol))) {
                        System.out.println("Error: A record with that primary key already exists.");
                        return false;
                    }
                }
            }

            boolean wasInserted = false;
            // Loop through pages and find which one to insert record into. Look ahead
            // algorithm
            Page next = null;
            for (int i = 0; i < tableSchema.getIndexList().size(); i++) {
                // See if we are going to be out of bounds
                if (i + 1 >= tableSchema.getIndexList().size()) {
                    break;
                }
                // Get the next page (must use src.BufferManager to get it)
                next = BufferManager.getPage(tableName, i + 1);

                Record firstRecordOfNextPage = next.getFirstRecord();

                // If it's less than the first value of next page (i+1) then it belongs to page
                // 'i'
                // Type cast appropriately then compare records
                if (Page.isLessThan(record, firstRecordOfNextPage, tableName)) {
                    // Add record to current page
                    Page page = BufferManager.getPage(tableName, i);

                    Page splitPage = page.addRecord(record);
                    wasInserted = true;
                    if (splitPage != null) { // If we split update stuff as needed
                        tableSchema.addToIndexList(i + 1, numPages);
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
                    return false;
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
        return true;
    }
}
