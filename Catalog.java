import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Catalog
 * @author - Jaron Cummings
 * represents the catalog of the database
 */
public class Catalog {

    private static ArrayList<TableSchema> tableSchemas;    // arrayList representing the table schemas
    public static Catalog catalog;                  // private instance of Catalog, accessible by static methods

    static{
        tableSchemas = new ArrayList<>();
    }

    /**
     * Check db_loc/Schema folder and read in each file, converting it to TableSchema objects
     * @return
     */
    public static void readCatalog(String db_loc) {
        
        try{
            // Assuming db_loc is a directory path
            File schemaDirectory = new File(db_loc + "/Schema");

            if (schemaDirectory.exists() && schemaDirectory.isDirectory()) {
                File[] schemaFiles = schemaDirectory.listFiles();

                if (schemaFiles != null) {
                    for (File schemaFile : schemaFiles) {
                        // Read each schema file and create TableSchema objects
                        TableSchema tableSchema = TableSchema.deserialize(Files.readAllBytes(schemaFile.toPath()));
                        if (tableSchema != null) {
                            tableSchemas.add(tableSchema);
                        }
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        

        
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
     * @param name - the schema name to remove
     *
     */
    public Boolean removeSchema(String name){
        for(TableSchema t : this.tableSchemas){
            if (t.tableName.equals(name)){
                this.tableSchemas.remove(t);
                return true;
            }
        }
        return false;
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

    /**
     * Gets a table schema from the catalog
     * @param name - name of the table
     * @return - the schema or null if not found
     */
    public static TableSchema getTableSchema(String name){
        for(TableSchema t : tableSchemas){
            if (t.tableName.equals(name)){
                return t;
            }
        }
        return null;
    }


    public static Catalog getCatalog(){
        return Catalog.catalog;
    }

}
