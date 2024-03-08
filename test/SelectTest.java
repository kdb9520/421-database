package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import src.Main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SelectTest {
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor)); // Redirect System.out to capture output
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut); // Restore System.out
        System.setIn(System.in); // Restore System.in
    }

    @Test
    void testSingleTable() {
        try {
            tearDown();
            System.out.println("Testing SELECT * FROM single table.");
            setUp();

            String[] args = {"C:\\Users\\kelle\\RIT\\421\\db", "10000", "10000"};

            String input = """
                    CREATE TABLE test1 (foo INTEGER PRIMARYKEY);
                    INSERT INTO test1 VALUES (1), (2), (3);
                    SELECT * FROM test1;
                    DROP TABLE test1;
                    QUIT;
                    """;
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = """
                    > Processed Query: create table test1 (foo integer primarykey);
                    Table created successfully.
                    > Processed Query: insert into test1 values (1), (2), (3);
                    > Processed Query: select * from test1;
                    |       foo|
                    ------------
                    |         1|
                    |         2|
                    |         3|

                    > Processed Query: drop table test1;
                    Schema removed from src.Catalog
                    > Processed Query: quit;
                    Shutting down database...
                    Shutdown complete.
                    """;
            expected = expected.replaceAll("\r", "");
            String output = outputStreamCaptor.toString().replaceAll("\r", "");

            assertEquals(expected, output);

            tearDown();
            System.out.println("Captured Output:");
            System.out.println(output);
        } catch (Exception e) {
            fail("Exception Caught!");
        }
    }

    @Test
    void testTwoTableCartesian() {
        try {
            tearDown();
            System.out.println("Testing SELECT * FROM two tables.");
            setUp();

            String[] args = {"C:\\Users\\kelle\\RIT\\421\\db", "10000", "10000"};

            String input = """
                    CREATE TABLE test1 (foo INTEGER PRIMARYKEY);
                    CREATE TABLE test2 (foo INTEGER PRIMARYKEY);
                    INSERT INTO test1 VALUES (1), (2), (3);
                    INSERT INTO test2 VALUES (4), (5), (6);
                    SELECT * FROM test1, test2;
                    DROP TABLE test1;
                    DROP TABLE test2;
                    QUIT;
                    """;
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = """
                    > Processed Query: create table test1 (foo integer primarykey);
                    Table created successfully.
                    > Processed Query: create table test2 (foo integer primarykey);
                    Table created successfully.
                    > Processed Query: insert into test1 values (1), (2), (3);
                    > Processed Query: insert into test2 values (4), (5), (6);
                    > Processed Query: select * from test1, test2;
                    Table created successfully.
                    | test1.foo|| test2.foo||    row_id|
                    ------------------------------------
                    |         1||         4||         0|
                    |         1||         5||         1|
                    |         1||         6||         2|
                    |         2||         4||         3|
                    |         2||         5||         4|
                    |         2||         6||         5|
                    |         3||         4||         6|
                    |         3||         5||         7|
                    |         3||         6||         8|

                    Schema removed from src.Catalog
                    > Processed Query: drop table test1;
                    Schema removed from src.Catalog
                    > Processed Query: drop table test2;
                    Schema removed from src.Catalog
                    > Processed Query: quit;
                    Shutting down database...
                    Shutdown complete.
                    """;
            expected = expected.replaceAll("\r", "");
            String output = outputStreamCaptor.toString().replaceAll("\r", "");

            assertEquals(expected, output);

            tearDown();
            System.out.println("Captured Output:");
            System.out.println(output);
        } catch (Exception e) {
            fail("Exception Caught!");
        }
    }

}
