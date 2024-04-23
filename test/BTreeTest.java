package test;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class BTreeTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
private final PrintStream originalOut = System.out;
private final PrintStream originalErr = System.err;

@Before
public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
}

@After
public void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
}

    @Test
    void insertValues() {
        try {
            // First make a table schema for this test table
            // Create mock table Schema
            // First make the AttributeSchemas for GPA and then Name
            String[] constraints = {"PRIMARYKEY"};
            AttributeSchema attr1 = new AttributeSchema("gpa", "integer", constraints);
            AttributeSchema attr2 = new AttributeSchema("name", "varchar(50)", null);
            ArrayList<AttributeSchema> aSchema = new ArrayList<>();
            aSchema.add(attr1);
            aSchema.add(attr2);
            TableSchema tSchema = new TableSchema("student", aSchema);
            Catalog.updateCatalog(tSchema);
            BPlusTree tree = new BPlusTree(10,"student");

            Object value1 = 1;
            Object value2 = 2;
            Object value3 = 3;

            ArrayList<String> tests = new ArrayList<>();
            ArrayList<String> expected = new ArrayList<>();

            
            tree.insert(1,0,0);
            
            tree.printTree();
            tree.insert(2,0,1);
            assertEquals("1", outContent.toString());
            tree.printTree();
            tree.insert(3,0,2);
            tree.printTree();


            assert(true);

        } catch (Exception e) {
            fail("Exception Caught!");
        }
    }
 
}
