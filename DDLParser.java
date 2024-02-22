import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.ServerError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ServiceConfigurationError;

// todo make static

/**
 * @author Jaron Cummings
 */
public class DDLParser {
    private ArrayList<TableSchema> tableSchemas; // arrayList of TableSchemas


    /**
     * creates a table by creating a new schema and inserting into the catalog
     * @param query - the query entered by the user
     */
    public static void createTable(String query) {

        if(!query.contains("create table")){
            return;
        }


        int startIndex = -1;
        int endIndex = query.length() - 2;

        // determine where the ( ) are based off the writeup formatting
        for(int i = 0; i < query.length(); i ++){
            if(query.charAt(i) == '('){
                startIndex = i;
                break;
            }


        }

        if(startIndex == -1){
            System.err.println("Invalid syntax");
            return;
        }

        if (!query.contains(");")) {
            System.err.println("Invalid syntax");
            return;

        }

        query = query.replace("\n", "");
        // get table name
        String tableName = query.substring(12, startIndex).trim();

        // Check if table with that name already exists
        if (Catalog.getTableSchema(tableName) != null) {
            System.err.println("Error table: '" + tableName + "' already exists.");
            return;
        }

        //todo look into format string "create table %s (%s)"
        String[] args = query.substring(startIndex  + 1, endIndex).split(",");  // each "attribute and its type/constraint"
        Boolean typeValid = true;
        Boolean constraintsValid = true;
        ArrayList <AttributeSchema> attributes = new ArrayList<>();
        ArrayList <String> attributeNames = new ArrayList<>();

        Boolean primaryKeyExists = false;
        // for each "column" in the create table query, perform validation
        // and create new attribute objects
        for (String arg : args) {
            arg = arg.trim();
            typeValid = true;
            constraintsValid = true;


            String[] attribute_data = arg.split(" ");
            String attribute = attribute_data[0].trim();
            if(attribute_data.length < 2){
                System.err.println("Invalid query!\n");
                return;
            }
            if(attributeNames.contains(attribute)) {
                System.err.println("Invalid query!");
                System.err.println("Table has attributes with matching names.\n");
                return;
            }
            attributeNames.add(attribute);

            String type = attribute_data[1].trim();
            String[] constraints = Arrays.copyOfRange(attribute_data, 2, attribute_data.length);
            if(type.contains("varchar") || type.contains("char")){
                String value = type.substring(type.indexOf('(') + 1, type.indexOf(')'));
                type = type.substring(0, type.indexOf('('));
            }
            typeValid = checkTypes(type);
            for(String p : constraints){
                if(p.equals("primarykey")){
                    primaryKeyExists = true;
                }
            }
            constraintsValid = checkConstraint(constraints);
            if (typeValid && constraintsValid) {
                AttributeSchema a = new AttributeSchema(attribute, attribute_data[1], constraints);
                attributes.add(a);

            } else {
                if(!typeValid){
                    System.err.println("Invalid type");
                }

                if(!constraintsValid){
                    System.err.println("Invalid constraint");
                }
                return;
            }

        }

        if(!primaryKeyExists){
            System.err.println("No primary key in this table, not being created");
            return;
        }


        //create new table schema
        TableSchema tableSchema = new TableSchema(tableName, attributes);

        // update the catalog
        Catalog.updateCatalog(tableSchema);

        // write the file to the disk
        StorageManager.writeTableToDisk(tableName);
    }

    /**
     * Checks if a constraint is valid
     * @param params - the list of constraints supplied by the user
     * @return - true if valid false if not
     */
    private static Boolean checkConstraint(String[] params) {
        if(params.length < 1){
            return true;
        }
        for(String p : params){
            if(!p.equals("notnull") && !p.equals("primarykey") && !p.equals("unique")){
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if type is valid
     * @param param - the type given by the user
     * @return - true if valid false if not
     */
    private static Boolean checkTypes(String param) {
        if(param.equals("integer") || param.equals("double") || param.equals("boolean")
        || param.equals("boolean") || param.equals("char") || param.equals("varchar")){
            return true;
        }

        else{
            return false;
        }
    }

    /**
     * Drops a table
     * @param query - query given by user
     */
    public static void dropTable(String query) {
        if(!query.contains("drop table")){
            return;
        }

        String [] args = query.split(" ");
        if(args.length > 3){
            System.err.println("Invalid syntax");
            return;
        }

        String name = args[2];
        name = name.substring(0,name.length()-1); // Remove the ; from end

        StorageManager.deleteTable(name);
        Catalog.removeSchema(name);
        // get rid of all pages in the buffermanager with that name are removed
        BufferManager.deleteTable(name);
    }

    /**
     * Alter table modifies a table schema
     * @param query - query given by user
     */
    public static void alterTable(String query){

        // create a table called temp based off new schema
        // copy the data over
        // drop the old table
        // add the new table
        // todo - worry about casing of letters
        String name = "";
        String operation = "";


        // check if valid operation
        if(!query.contains("alter table")){
            return;
        }

        String [] parsed = query.split(" ");
        name = parsed[2];   // name of table
        operation = parsed[3]; // operation, drop or add

        // check if operation is valid
        if(!operation.equals("drop") && !operation.equals("add")){
            System.err.println("Invalid operation, must be Drop or Add");
            return;
        }
        Boolean found = false;
        TableSchema tableSchema = Catalog.getTableSchema(name);
        // return if not found
        if(tableSchema == null){
            System.err.println("Invalid table name");
            return;

        }




        if(operation.equals("drop")){
            // create a copy
            if(parsed.length != 5){
                System.err.println("Invalid syntax");
                return;
            }

            if(!tableSchema.dropAttribute(parsed[4])){
                return;
            }
            int position = tableSchema.findAttribute(parsed[4]);


            int numPages = tableSchema.getIndexList().size();
            ArrayList<Record> recordsOld = new ArrayList<>();
            // these are based off insert from the DML

            int i = 0;
            // add all old records to new array
            do {
                Page page = BufferManager.getPage(name,i);
                page.removeValue(position);
                i += 1;
            }while (i < numPages);


        }

        // add operation

        //todo - right now this only has been tested with a default value
        if(operation.equals("add")){
            TableSchema tableSchemaOld = new TableSchema(tableSchema); // make a deep copy
            Catalog.updateCatalog(tableSchemaOld);                     // adds the copy to the catalog
            String temp = "temp";
            tableSchema.tableName = temp;                               // temp name for other copy
            StorageManager.writeTableToDisk(temp);

            // if invalid args, return
            if(parsed.length != 6 && parsed.length != 8){
                System.err.println("Invalid syntax");
                return;
            }

            String attributeName = parsed[4];   // name of attribute
            String attributeType = parsed[5];   // name of type


            // check for nested ()
            if(attributeType.contains("varchar") || attributeType.contains("char")){
                String value = attributeType.substring(attributeType.indexOf('(') + 1, attributeType.indexOf(')'));
                attributeType = attributeType.substring(0, attributeType.indexOf('('));
            }

            // if invalid types
            if(!checkTypes(attributeType)){
                System.err.println("Invalid type");
                return;
            }
            String value = "";
            // if there is a default value
            if(parsed.length == 8){
                value = parsed[7];
                value = value.substring(0, value.length() - 1);
            }
            else{
                value = "null";
            }

            // make a new attribute schema
            AttributeSchema a = new AttributeSchema(attributeName, parsed[5], null);
            tableSchema.addAttribute(a);


            // get the old records
            ArrayList<Record> recordsOld = new ArrayList<>();   // old records


            // get the number of pages
            int numPages = tableSchemaOld.getIndexList().size();

            // these are based off insert from the DML
            ArrayList<Integer> pageIndexList = tableSchema.getIndexList();

            int i = 0;

            // add all old records to new array
            do {
                Page page = BufferManager.getPage(name,i);
                ArrayList<Record> t = page.getRecords();
                recordsOld.addAll(t);
                i += 1;
            }while (i < numPages);



            // set default attribute, if any
            // insert
            for (Record record: recordsOld){


                ArrayList<Object> attrs = record.getValues();
                // build a new query for insert operation
                StringBuilder tempQuery = new StringBuilder(temp + " values(");
                int c = 0;
                for(Object attr : attrs){
                    tempQuery.append(attr.toString());
                    if(c != attrs.size() - 1){
                        tempQuery.append(" ");
                    }
                    c += 1;

                }
                tempQuery.append(" " + value);
                tempQuery.append(");");

                // insert the new record
                DMLParser.insert(String.valueOf(tempQuery));
                //StorageManager.insert(temp, record, tableSchema);

            }

            // remove old table from catalog and StorageManager
            Catalog.removeSchema(name);
            BufferManager.deleteTable(name);

            //rename to the new table
            numPages = tableSchema.getIndexList().size();

            // these are based off insert from the DML
            pageIndexList = tableSchema.getIndexList();


            //rename all the pages
            i = 0;
            do {
                Page page = BufferManager.getPage(temp,i);
                page.tableName = name;
                i ++;

            }while (i < numPages);

            // rename schema in Catalog
            Catalog.renameSchema(name);



        }


    }

}
