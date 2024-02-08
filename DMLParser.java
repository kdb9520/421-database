public class DMLParser {

    public DMLParser() {

    }

    public void insert(String query) {
        String name = "";
        String a_name = "";
        String constraint_1 = "";
        String constraint = "";

    }

    private void displaySchema() {
        
    }

    private Boolean checkTypes(String param) {

        //todo - look into types allowed
        return true;
    }

    public void dropTable(String query) {
        if(!query.contains("drop table")){
            return;
        }

        String [] args = query.split(" ");
        if(args.length > 3){
            return;
        }

        String name = args[2];
    }

    public void alterTable(){

    }
}
