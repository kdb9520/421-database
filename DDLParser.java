import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

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

        //todo look into format string "create table %s (%s)"
        String[] args = query.substring(startIndex  + 1, endIndex).split(",");  // each "attribute and its type/constraint"
        Boolean typeValid = true;
        Boolean constraintsValid = true;
        ArrayList <AttributeSchema> attributes = new ArrayList<>();

        // for each "column" in the create table query, perform validation
        // and create new attribute objects
        for (String arg : args) {
            arg = arg.trim();
            typeValid = true;
            constraintsValid = true;


            String[] attribute_data = arg.split(" ");
            String attribute = attribute_data[0].trim();
            String type = attribute_data[1].trim();
            String[] constraints = Arrays.copyOfRange(attribute_data, 2, attribute_data.length);
            if(type.contains("varchar") || type.contains("char")){
                String value = type.substring(type.indexOf('(') + 1, type.indexOf(')'));
                type = type.substring(0, type.indexOf('('));
            }
            typeValid = checkTypes(type);
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


        StorageManager.deleteTable(name);
        Catalog.removeSchema(name);

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
            tableSchema.dropAttribute(parsed[4]);

        }

        // add operation

        if(operation.equals("add")){
            TableSchema tableSchemaOld = new TableSchema(tableSchema); // make a deep copy
            Catalog.updateCatalog(tableSchemaOld);
            String temp = "temp";
            tableSchema.tableName = temp;

            // if invalid args, return
            if(parsed.length != 6 && parsed.length != 8){
                System.err.println("Invalid syntax");
                return;
            }

            String attributeName = parsed[4];
            String attributeType = parsed[5];


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
            if(parsed.length == 8){
                value = parsed[7];
            }

            // make a new attribute schema
            AttributeSchema a = new AttributeSchema(attributeName, parsed[5], null);
            tableSchema.addAttribute(a);


            // get the old records
            ArrayList<Record> recordsOld = new ArrayList<>();   // old records


            // To copy the records into a new ArrayList of records, does this look right?
            // don't know if I need this, this is intended to be for the old array
            int numPages = tableSchema.getIndexList().size();

            // these are based off insert from the DML
            ArrayList<Integer> pageIndexList = tableSchema.getIndexList();

            // add all old records from new array
            for(int i = 0; i < numPages; i ++){
                Page page = BufferManager.getPage(name,i);
                ArrayList<Record> t = page.getRecords();

                recordsOld.addAll(t);

            }

            // set default attribute, if any
            // insert
            for (Record record: recordsOld){
                record.setAttribute(value);
                StorageManager.insert(temp, record, tableSchema);

            }

            // remove old table from catalog and StorageManager
            Catalog.removeSchema(name);
            StorageManager.deleteTable(name);

            //rename to the new table
            numPages = tableSchema.getIndexList().size();

            // these are based off insert from the DML
            pageIndexList = tableSchema.getIndexList();

            // rename
            for(int i = 0; i < numPages; i ++){
                Page page = BufferManager.getPage(name,i);
                page.tableName = name;

            }



            // todo delete old table/schema
            StorageManager.deleteTable(tableSchemaOld.tableName);


        }


    }
}
