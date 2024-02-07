public class DDLParser {

    public DDLParser() {

    }

    public void createTable(String query) {
        String name = "";
        String a_name = "";
        String constraint_1 = "";
        String constraint = "";

        if(!query.contains("CREATE")){
            return;
        }

        if(!query.contains("Table")){
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
        String[] args = query.substring(startIndex, endIndex).split(",");
        for(int i = 0; i < args.length; i ++){
            String[] params = args[i].split(" ");
            Boolean typeValid = checkTypes(params[1]);
            Boolean constraintsValid = checkConstraint(params);
        }




    }

    private Boolean checkConstraint(String[] params) {
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

    public void dropTable() {

    }

    public void alterTable(){

    }
}
