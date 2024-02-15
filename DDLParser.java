import java.util.ArrayList;
import java.util.Arrays;

// todo make static

/**
 * @author Jaron Cummings
 */
public class DDLParser {
    private ArrayList<TableSchema> tableSchemas; // arrayList of TableSchemas
    public DDLParser() {

        // reads the catalog in from hardware
        this.tableSchemas = Catalog.readCatalog();

    }

    /**
     * creates a table by creating a new schema and inserting into the catalog
     * @param query - the query entered by the user
     */
    public void createTable(String query) {

        // todo - worry about casing of letters
        String name = "";
        String a_name = "";
        String constraint_1 = "";
        String constraint = "";

        if(!query.contains("CREATE TABLE")){
            return;
        }


        int startIndex = 0;
        int endIndex = 0;

        // determine where the ( ) are based off the writeup formatting
        for(int i = 0; i < query.length(); i ++){
            if(query.charAt(i) == '('){
                startIndex = i;
            }
            if(query.charAt(i) == (')')){
                endIndex = i;
            }
        }

        // get table name
        String tableName = query.substring(12, startIndex);

        //todo look into format string "create table %s (%s)"
        String[] args = query.substring(startIndex, endIndex).split(",");  // each "attribute and its type/constraint"
        Boolean typeValid = true;
        Boolean constraintsValid = true;
        ArrayList <AttributeSchema> attributes = new ArrayList<>();

        // for each "column" in the create table query, perform validation
        // and create new attribute objects
        for(int i = 0; i < args.length; i ++){
            typeValid = true;
            constraintsValid = true;
            String[] attribute_data = args[i].split(" ");
            String attribute = args[0];
            String type = attribute_data[1];
            String [] constraints = Arrays.copyOfRange(attribute_data, 2, attribute_data.length);
            typeValid = checkTypes(attribute_data[1]);
            constraintsValid = checkConstraint(constraints);
            if(typeValid && constraintsValid){
                AttributeSchema a = new AttributeSchema(attribute, type, constraints);
                attributes.add(a);

            }
            else{
                return;
            }

        }

        // create new table schema
        TableSchema tableSchema = new TableSchema(tableName, attributes);


        // update the catalog
        Catalog.getCatalog().updateCatalog(tableSchema);

    }

    /**
     * Checks if a constraint is valid
     * @param params - the list of constraints supplied by the user
     * @return - true if valid false if not
     */
    private Boolean checkConstraint(String[] params) {
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
    private Boolean checkTypes(String param) {
        //todo - look into types allowed
        return true;
    }

    /**
     * Drops a table
     * @param query - query given by user
     */
    public void dropTable(String query) {
        if(!query.contains("DROP TABLE")){
            return;
        }

        String [] args = query.split(" ");
        if(args.length > 3){
            return;
        }

        String name = args[2];


        //TODO  - talk to storage manager
        for(TableSchema t : this.tableSchemas){
            if (t.tableName.equals(name)){
                Catalog.getCatalog().removeSchema(t);
                break;
            }
        }
    }

    /**
     * Alter table modifies a table schema
     * @param query - query given by user
     */
    public void alterTable(String query){

        // create a table called temp based off new schema
        // copy the data over
        // drop the old table
        // add the new table
        // todo - worry about casing of letters
        String name = "";
        String operation = "";
        String constraint_1 = "";
        String constraint = "";

        // check if valid operation
        if(!query.contains("ALTER TABLE")){
            return;
        }

        String [] parsed = query.split(" ");
        name = parsed[2];   // name of table
        operation = parsed[3]; // operation, drop or add

        // check if operation is valid
        if(!operation.equals("DROP") && !operation.equals("ADD")){
            System.err.println("Invalid operation, must be Drop or Add");
            return;
        }
        Boolean found = false;
        TableSchema tableSchema = null;

        // check to make sure the Catalog contains the table to alter
        for(TableSchema t : this.tableSchemas){
            if (t.tableName.equals(name)){
                found = true;
                tableSchema = t;
                break;
            }
        }

        // return if not found
        if(!found){
            System.err.println("Invalid table name");
            return;

        }


        if(operation.equals("DROP")){
            tableSchema.dropAttribute(parsed[4]);
        }


        if(operation.equals("ADD")){
            if(parsed.length != 6 || parsed.length != 8){
                System.err.println("Invalid number of args");
            }

            String attributeName = parsed[4];
            String attributeType = parsed[5];
            AttributeSchema a = new AttributeSchema(attributeName, attributeType, null);

            tableSchema.addAttribute(a);
            Catalog.getCatalog().alterSchema(tableSchema);

            // todo - deal with other args for alter table pending new constructor
        }


    }


}
