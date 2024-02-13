import java.util.LinkedList;

import javax.management.Query;

public class DMLParser {

    BufferManager bufferManager;

    public DMLParser(BufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }

    public void handleQuery(String query) {

        if (query.substring(0, 11).equals("insert into ")) {
            insert(query);
        }

        if (query.substring(0, 14).equals("display schema ")) {
            displaySchema(query.substring(14));
        }
    }

    public void insert(String query) {
        String name = "";
        String a_name = "";
        String constraint_1 = "";
        String constraint = "";

    }

    private void displaySchema(String query) {

    }

    private boolean displayInfo(String tableName) {

        Table table = bufferManager.getTable(tableName);
        if (table != null) {
            // get schema
            String schema = "";

            int pageNumber = table.numPages;

            LinkedList<Page> pages = table.pages;
            int numRecords = 0;
            for (Page page : pages) {
                numRecords = numRecords + page.numRecords;
            }

            System.out.println("Table: "+ tableName + "\nSchema: " + schema + "\nNumber of Pages: " + pageNumber + "\nNumber of Records: " + numRecords);
            return true;
        }

        return false;
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

}
