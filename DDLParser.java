import java.util.ArrayList;
import java.util.Arrays;

// todo make static
public class DDLParser {

    public DDLParser() {

    }

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

        for(int i = 0; i < query.length(); i ++){
            if(query.charAt(i) == '('){
                startIndex = i;
            }
            if(query.charAt(i) == (')')){
                endIndex = i;
            }
        }
        String tableName = query.substring(12, startIndex);

        //todo look into format string "create table %s (%s)"
        String[] args = query.substring(startIndex, endIndex).split(",");  // each "attribute and its type/constraint"
        Boolean typeValid = true;
        Boolean constraintsValid = true;
        ArrayList <Attribute> attributes = new ArrayList<>();
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
                Attribute a = new Attribute(attribute, type, constraints);
                attributes.add(a);

            }
            else{
                return;
            }

        }
        TableSchema tableSchema = new TableSchema();
        for(Attribute anAttribute : attributes){
            tableSchema.addAttribute(anAttribute);
        }

        Catalog.updateCatalog(tableSchema);

    }

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

    private Boolean checkTypes(String param) {
        //todo - look into types allowed
        return true;
    }

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
        //TODO - talk to catalog
    }

    public void alterTable(){

    }
}
