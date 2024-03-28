package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.Main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class DeleteTest {
    private final PrintStream standardOut = System.out;
    private final PrintStream standardErr = System.err;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor)); // Redirect System.out to capture output
        System.setErr(new PrintStream(outputStreamCaptor)); // Redirect System.err
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut); // Restore System.out
        System.setErr(standardErr); // Restore System.err
        System.setIn(System.in); // Restore System.in
    }
    @Test
    void deletePrimaryKey() {
        try {
            tearDown();
            System.out.println("Testing delete from students where id = 1;");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE students (id INTEGER PRIMARYKEY, gpa double);\nINSERT INTO students VALUES (1 1.5), (2 1.5), (3 1.5);\nSELECT * FROM students;\ndelete from students where id = 1;\nselect * from students;\nDROP TABLE students;\nQUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table students (id integer primarykey, gpa double);\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: insert into students values (1 1.5), (2 1.5), (3 1.5);\n" +
                    "> Processed Query: select * from students;\n" +
                    "|        id||       gpa|\n" +
                    "------------------------\n" +
                    "|         1||       1.5|\n" +
                    "|         2||       1.5|\n" +
                    "|         3||       1.5|\n" +
                    "\n" +
                    "> Processed Query: delete from students where id = 1;\n" +
                    "> Processed Query: select * from students;\n" +
                    "|        id||       gpa|\n" +
                    "------------------------\n" +
                    "|         2||       1.5|\n" +
                    "|         3||       1.5|\n" +
                    "\n" +
                    "> Processed Query: drop table students;\n" +
                    "Schema removed from src.Catalog\n" +
                    "> Processed Query: quit;\n" +
                    "Shutting down database...\n" +
                    "Shutdown complete.\n";
            expected = expected.replaceAll("\r", "");
            String output = outputStreamCaptor.toString().replaceAll("\r", "");

            assertEquals(expected, output);

            tearDown();
        } catch (Exception e) {
            fail("Exception Caught!");
        }
    }
    @Test
    void deleteOnTwoAttrs() {
        try {
            tearDown();
            System.out.println("Testing delete from students where id = 1 and gpa = 1.5;");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE students (id INTEGER PRIMARYKEY, gpa double);\nINSERT INTO students VALUES (1 1.5), (2 1.5), (3 1.5);\nSELECT * FROM students;\ndelete from students where id = 1 and gpa = 1.5;\nselect * from students;\nDROP TABLE students;\nQUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table students (id integer primarykey, gpa double);\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: insert into students values (1 1.5), (2 1.5), (3 1.5);\n" +
                    "> Processed Query: select * from students;\n" +
                    "|        id||       gpa|\n" +
                    "------------------------\n" +
                    "|         1||       1.5|\n" +
                    "|         2||       1.5|\n" +
                    "|         3||       1.5|\n" +
                    "\n" +
                    "> Processed Query: delete from students where id = 1 and gpa = 1.5;\n" +
                    "> Processed Query: select * from students;\n" +
                    "|        id||       gpa|\n" +
                    "------------------------\n" +
                    "|         2||       1.5|\n" +
                    "|         3||       1.5|\n" +
                    "\n" +
                    "> Processed Query: drop table students;\n" +
                    "Schema removed from src.Catalog\n" +
                    "> Processed Query: quit;\n" +
                    "Shutting down database...\n" +
                    "Shutdown complete.\n";
            expected = expected.replaceAll("\r", "");
            String output = outputStreamCaptor.toString().replaceAll("\r", "");

            assertEquals(expected, output);

            tearDown();
        } catch (Exception e) {
            fail("Exception Caught!");
        }
    }
    @Test
    void deleteOnNotEquals() {
        try {
            tearDown();
            System.out.println("Testing delete from students where id != 1;");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE students (id INTEGER PRIMARYKEY, gpa double);\nINSERT INTO students VALUES (1 1.5), (2 1.5), (3 1.5);\nSELECT * FROM students;\ndelete from students where id != 1;\nselect * from students;\nDROP TABLE students;\nQUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table students (id integer primarykey, gpa double);\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: insert into students values (1 1.5), (2 1.5), (3 1.5);\n" +
                    "> Processed Query: select * from students;\n" +
                    "|        id||       gpa|\n" +
                    "------------------------\n" +
                    "|         1||       1.5|\n" +
                    "|         2||       1.5|\n" +
                    "|         3||       1.5|\n" +
                    "\n" +
                    "> Processed Query: delete from students where id != 1;\n" +
                    "> Processed Query: select * from students;\n" +
                    "|        id||       gpa|\n" +
                    "------------------------\n" +
                    "|         1||       1.5|\n" +
                    "\n" +
                    "> Processed Query: drop table students;\n" +
                    "Schema removed from src.Catalog\n" +
                    "> Processed Query: quit;\n" +
                    "Shutting down database...\n" +
                    "Shutdown complete.\n";
            expected = expected.replaceAll("\r", "");
            String output = outputStreamCaptor.toString().replaceAll("\r", "");

            assertEquals(expected, output);

            tearDown();
        } catch (Exception e) {
            fail("Exception Caught!");
        }
    }
}
