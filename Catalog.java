import java.util.ArrayList;

/**
 * Catalog
 * @author - Jaron Cummings
 * represents the catalog of the database
 */
public class Catalog {

    private ArrayList<TableSchema> tableSchemas;    // arrayList representing the table schemas
    public static Catalog catalog;                  // private instance of Catalog, accessible by static methods

    // constructor for when there is no catalog on hardware
    private Catalog(){

    }

    // constructor for when there is a catalog
    private Catalog(String dir){

    }

    /**
     * needs to pass in file directory
     * @return
     */
    public static ArrayList<TableSchema> readCatalog(){
        ArrayList<TableSchema> tableSchemas = new ArrayList<>(); //todo - read from hardware
        return tableSchemas;

    }

    /**
     * Writes to hardware
     *
     */
    public static void writeCatalog(){
        // todo - write from hardware

    }

    /**
     * creates a new schema in the catalog
     * @param tableSchema - the schema to add
     */
    public void updateCatalog(TableSchema tableSchema){
        this.tableSchemas.add(tableSchema);

    }

    /**
     * removes a schema from the catalog
     * @param tableSchema - the schema to remove
     *
     */
    public void removeSchema(TableSchema tableSchema){
        this.tableSchemas.remove(tableSchema);
    }

    /**
     * alters a schema in the catalog
     * @param tableSchema - schema to alter
     */
    public void alterSchema(TableSchema tableSchema){
        //this.schemas.remove(tableSchema);
        // todo modify schema passed in
        //this.schemas.add(tableSchema);


    }


    public static Catalog getCatalog(){
        return Catalog.catalog;
    }

}
