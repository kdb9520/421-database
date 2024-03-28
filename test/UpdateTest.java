package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import src.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class UpdateTest {

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
    void updateGPA() {
        try {
            tearDown();
            System.out.println("Testing update students set gpa = 3.5 where id = 1;");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE students (id INTEGER PRIMARYKEY, gpa double);\nINSERT INTO students VALUES (1 1.5), (2 1.5), (3 1.5);\nSELECT * FROM students;\nupdate students set gpa = 3.5 where id = 1;\nselect * from students;\nDROP TABLE students;\nQUIT;\n";
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
                    "> Processed Query: update students set gpa = 3.5 where id = 1;\n" +
                    "=(id, 1)\n" +
                    "> Processed Query: select * from students;\n" +
                    "|        id||       gpa|\n" +
                    "------------------------\n" +
                    "|         1||       3.5|\n" +
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
    void updateVarchar() {
        try {
            tearDown();
            System.out.println("Testing update students set school = \"Z\" where id = 1");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE students (id INTEGER PRIMARYKEY, school varchar(40));\nINSERT INTO students VALUES (1 \"A\"), (2 \"B\"), (3 \"C\");\nSELECT * FROM students;\nupdate students set school = \"Z\" where id = 1;\nselect * from students;\nDROP TABLE students;\nQUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table students (id integer primarykey, school varchar(40));\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: insert into students values (1 \"A\"), (2 \"B\"), (3 \"C\");\n" +
                    "> Processed Query: select * from students;\n" +
                    "|        id||    school|\n" +
                    "------------------------\n" +
                    "|         1||       \"A\"|\n" +
                    "|         2||       \"B\"|\n" +
                    "|         3||       \"C\"|\n" +
                    "\n" +
                    "> Processed Query: update students set school = \"Z\" where id = 1;\n" +
                    "=(id, 1)\n" +
                    "> Processed Query: select * from students;\n" +
                    "|        id||    school|\n" +
                    "------------------------\n" +
                    "|         1||       \"Z\"|\n" +
                    "|         2||       \"B\"|\n" +
                    "|         3||       \"C\"|\n" +
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
    void updatePrimaryKey() {
        try {
            tearDown();
            System.out.println("update students set id = 9 where id = 1;");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE students (id INTEGER PRIMARYKEY, school varchar(40));\nINSERT INTO students VALUES (1 \"A\"), (2 \"B\"), (3 \"C\");\nSELECT * FROM students;\nupdate students set id = 9 where id = 1;\nselect * from students;\nDROP TABLE students;\nQUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table students (id integer primarykey, school varchar(40));\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: insert into students values (1 \"A\"), (2 \"B\"), (3 \"C\");\n" +
                    "> Processed Query: select * from students;\n" +
                    "|        id||    school|\n" +
                    "------------------------\n" +
                    "|         1||       \"A\"|\n" +
                    "|         2||       \"B\"|\n" +
                    "|         3||       \"C\"|\n" +
                    "\n" +
                    "> Processed Query: update students set id = 9 where id = 1;\n" +
                    "=(id, 1)\n" +
                    "|         9||       \"A\"|\n" +
                    "> Processed Query: select * from students;\n" +
                    "|        id||    school|\n" +
                    "------------------------\n" +
                    "|         2||       \"B\"|\n" +
                    "|         3||       \"C\"|\n" +
                    "|         9||       \"A\"|\n" +
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
    void updateVarcharSameCol() {
        try {
            tearDown();
            System.out.println("update students set id = 9 where id = 1");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE students (id INTEGER PRIMARYKEY, school varchar(40));\nINSERT INTO students VALUES (1 \"A\"), (2 \"B\"), (3 \"C\");\nSELECT * FROM students;\nupdate students set school = \"Z\" where school = \"A\";\nselect * from students;\nDROP TABLE students;\nQUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table students (id integer primarykey, school varchar(40));\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: insert into students values (1 \"A\"), (2 \"B\"), (3 \"C\");\n" +
                    "> Processed Query: select * from students;\n" +
                    "|        id||    school|\n" +
                    "------------------------\n" +
                    "|         1||       \"A\"|\n" +
                    "|         2||       \"B\"|\n" +
                    "|         3||       \"C\"|\n" +
                    "\n" +
                    "> Processed Query: update students set school = \"Z\" where school = \"A\";\n" +
                    "=(school, A)\n" +
                    "> Processed Query: select * from students;\n" +
                    "|        id||    school|\n" +
                    "------------------------\n" +
                    "|         1||       \"Z\"|\n" +
                    "|         2||       \"B\"|\n" +
                    "|         3||       \"C\"|\n" +
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


    // Add more test methods to cover other scenarios
}
