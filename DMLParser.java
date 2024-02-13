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

        else if (query.substring(0, 14).equals("display schema ")) {
            displaySchema(query.substring(14));
        }

        else if (query.substring(0, 12).equals("display info ")) {
            displaySchema(query.substring(12));
        }
    }

    public void insert(String query) {
        String name = "";
        String a_name = "";
        String constraint_1 = "";
        String constraint = "";

    }

    private boolean displaySchema(String tableName) {

        Table table = bufferManager.getTable(tableName);
        if (table != null) {

            String dbLocation = bufferManager.getDatabaseLocation();

            int pageSize = table.pages.getLast().getPageSize();

            int bufferSize = bufferManager.getSize();
           
            String schema = "_";

            System.out.println("Database Location: "+ dbLocation + "\nPage Size: " + pageSize + "\nBuffer Size: " + bufferSize + "Table Schema: " + schema);
            return true;
        }
        return false;
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
