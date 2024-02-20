import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Vector;

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
                        TableSchema tableSchema = TableSchema.deserialize(Files.readAllBytes(schemaFile.toPath()),schemaFile.getName());
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
    public static void writeCatalog(String db_loc) {
        // Ensure the db_loc/Schema directory exists or create it if it doesn't
        String schemaDirectoryPath = db_loc + "/Schema";
        createDirectoryIfNotExists(schemaDirectoryPath);

        for (TableSchema tableSchema : tableSchemas) {
            // Generate a unique filename for each TableSchema (you may need to adjust this)
            String filename = schemaDirectoryPath + "/" + tableSchema.getTableName();

            // Write the TableSchema to the file
           // Write the TableSchema to the file
        try (FileOutputStream fileOutputStream = new FileOutputStream(filename)) {
            // Serialize the TableSchema to obtain a byte array
            byte[] serializedData = tableSchema.serialize();

            // Write the byte array to the file
            fileOutputStream.write(serializedData);
            System.out.println("TableSchema written to: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception according to your requirements
        }
        }
    }

    private static void createDirectoryIfNotExists(String directoryPath) {
        try {
            Path path = Path.of(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * creates a new schema in the catalog
     * @param tableSchema - the schema to add
     */
    public static void updateCatalog(TableSchema tableSchema){
        tableSchemas.add(tableSchema);

    }

    /**
     * removes a schema from the catalog
     * @param name - the schema name to remove
     *
     */
    public static void removeSchema(String name){
        for(TableSchema t : tableSchemas){
            if (t.tableName.equals(name)){
                tableSchemas.remove(t);
                System.out.println("Schema removed from Catalog");

            }

        }
        System.err.println("Error removing table schema from catalog");
    }

    /**
     * alters a schema in the catalog
     * @param tableSchema - schema to alter
     */
    public static void alterSchema(TableSchema tableSchema){
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
