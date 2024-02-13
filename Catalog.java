import java.util.ArrayList;

public class Catalog {



    public static ArrayList<TableSchema> readCatalog(){
        ArrayList<TableSchema> tableSchemas = new ArrayList<>(); //todo - read from hardware
        return tableSchemas;

    }

    public static void writeCatalog(ArrayList<TableSchema> tableSchemas){
        // todo - write from hardware

    }

    public static ArrayList<TableSchema> updateCatalog(ArrayList<TableSchema> tableSchemas, TableSchema tableSchema){
        tableSchemas.add(tableSchema);
        return tableSchemas;
    }

    public static ArrayList<TableSchema> removeSchema(ArrayList<TableSchema> tableSchemas, TableSchema tableSchema){
        tableSchemas.remove(tableSchema);
        return tableSchemas;
    }

    public static ArrayList<TableSchema> alterSchema(ArrayList<TableSchema> tableSchemas, TableSchema tableSchema){
        //this.schemas.remove(tableSchema);
        // todo modify schema passed in
        //this.schemas.add(tableSchema);
        return tableSchemas;

    }

}
