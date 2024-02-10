import java.util.ArrayList;

public class Catalog {
    private static ArrayList<TableSchema> schemas;



    public Catalog(ArrayList<TableSchema> tableSchemas){
        this.schemas = tableSchemas;
    }

    public static void updateCatalog(TableSchema tableSchema){
        //this.schemas.add(tableSchema);
    }

    public static void removeSchema(TableSchema tableSchema){
        //this.schemas.remove(tableSchema);
    }

    public static void alterSchema(TableSchema tableSchema){
        //this.schemas.remove(tableSchema);
        // todo modify schema passed in
        //this.schemas.add(tableSchema);

    }

}
