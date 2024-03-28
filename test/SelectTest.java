package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import src.Main;

import static org.junit.jupiter.api.Assertions.*;

public class SelectTest {
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
    void testSingleTable() {
        try {
            tearDown();
            System.out.println("Testing SELECT * FROM single table.");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE test1 (foo INTEGER PRIMARYKEY);\nINSERT INTO test1 VALUES (1), (2), (3);\nSELECT * FROM test1;\nDROP TABLE test1;\nQUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table test1 (foo integer primarykey);\nTable created successfully.\n> Processed Query: insert into test1 values (1), (2), (3);\n> Processed Query: select * from test1;\n|       foo|\n------------\n|         1|\n|         2|\n|         3|\n\n> Processed Query: drop table test1;\nSchema removed from src.Catalog\n> Processed Query: quit;\nShutting down database...\nShutdown complete.\n";
            expected = expected.replaceAll("\r", "");
            String output = outputStreamCaptor.toString().replaceAll("\r", "");

            assertEquals(expected, output);

            tearDown();
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

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE test1 (foo INTEGER PRIMARYKEY);\nCREATE TABLE test2 (foo INTEGER PRIMARYKEY);\nINSERT INTO test1 VALUES (1), (2), (3);\nINSERT INTO test2 VALUES (4), (5), (6);\nSELECT * FROM test1, test2;\nDROP TABLE test1;\nDROP TABLE test2;\nQUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table test1 (foo integer primarykey);\nTable created successfully.\n> Processed Query: create table test2 (foo integer primarykey);\nTable created successfully.\n> Processed Query: insert into test1 values (1), (2), (3);\n> Processed Query: insert into test2 values (4), (5), (6);\n> Processed Query: select * from test1, test2;\nTable created successfully.\n| test1.foo|| test2.foo|\n------------------------\n|         1||         4|\n|         1||         5|\n|         1||         6|\n|         2||         4|\n|         2||         5|\n|         2||         6|\n|         3||         4|\n|         3||         5|\n|         3||         6|\n\nSchema removed from src.Catalog\n> Processed Query: drop table test1;\nSchema removed from src.Catalog\n> Processed Query: drop table test2;\nSchema removed from src.Catalog\n> Processed Query: quit;\nShutting down database...\nShutdown complete.\n";
            expected = expected.replaceAll("\r", "");
            String output = outputStreamCaptor.toString().replaceAll("\r", "");

            assertEquals(expected, output);

            tearDown();
        } catch (Exception e) {
            fail("Exception Caught!");
        }
    }


    @Test
    void ThreeTableCartesian() {
        try {
            tearDown();
            System.out.println("Testing SELECT * FROM three tables with different number of columns.");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE test1 (foo INTEGER PRIMARYKEY);\nCREATE TABLE test2 (foo INTEGER PRIMARYKEY, bar INTEGER);\nCREATE TABLE test3 (foo INTEGER PRIMARYKEY, bar INTEGER, baz INTEGER);\nINSERT INTO test1 VALUES (1), (2);\nINSERT INTO test2 VALUES (3 4), (5 6);\nINSERT INTO test3 VALUES (7 8 9), (10 11 12);\nSELECT * FROM test1, test2, test3;\nDROP TABLE test1;\nDROP TABLE test2;\nDROP TABLE test3;\nQUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table test1 (foo integer primarykey);\nTable created successfully.\n> Processed Query: create table test2 (foo integer primarykey, bar integer);\nTable created successfully.\n> Processed Query: create table test3 (foo integer primarykey, bar integer, baz integer);\nTable created successfully.\n> Processed Query: insert into test1 values (1), (2);\n> Processed Query: insert into test2 values (3 4), (5 6);\n> Processed Query: insert into test3 values (7 8 9), (10 11 12);\n> Processed Query: select * from test1, test2, test3;\nTable created successfully.\n| test1.foo|| test2.foo|| test2.bar|| test3.foo|| test3.bar|| test3.baz|\n------------------------------------------------------------------------\n|         1||         3||         4||         7||         8||         9|\n|         1||         3||         4||        10||        11||        12|\n|         1||         5||         6||         7||         8||         9|\n|         1||         5||         6||        10||        11||        12|\n|         2||         3||         4||         7||         8||         9|\n|         2||         3||         4||        10||        11||        12|\n|         2||         5||         6||         7||         8||         9|\n|         2||         5||         6||        10||        11||        12|\n\nSchema removed from src.Catalog\n> Processed Query: drop table test1;\nSchema removed from src.Catalog\n> Processed Query: drop table test2;\nSchema removed from src.Catalog\n> Processed Query: drop table test3;\nSchema removed from src.Catalog\n> Processed Query: quit;\nShutting down database...\nShutdown complete.\n";
            expected = expected.replaceAll("\r", "");
            String output = outputStreamCaptor.toString().replaceAll("\r", "");

            assertEquals(expected, output);

            tearDown();
        } catch (Exception e) {
            fail("Exception Caught!");
        }
    }

    @Test
    void testCartesianSelectColumns() {
        try {
            tearDown();
            System.out.println("Testing SELECT columns FROM two tables.");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE test1 (foo INTEGER PRIMARYKEY, bar INTEGER);\nCREATE TABLE test2 (foo INTEGER PRIMARYKEY);\nINSERT INTO test1 VALUES (1 1), (2 2), (3 3);\nINSERT INTO test2 VALUES (4), (5), (6);\nSELECT test1.foo, test2.foo FROM test1, test2;\nDROP TABLE test1;\nDROP TABLE test2;\nQUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table test1 (foo integer primarykey, bar integer);\nTable created successfully.\n> Processed Query: create table test2 (foo integer primarykey);\nTable created successfully.\n> Processed Query: insert into test1 values (1 1), (2 2), (3 3);\n> Processed Query: insert into test2 values (4), (5), (6);\n> Processed Query: select test1.foo, test2.foo from test1, test2;\nTable created successfully.\n| test1.foo|| test2.foo|\n------------------------\n|         1||         4|\n|         1||         5|\n|         1||         6|\n|         2||         4|\n|         2||         5|\n|         2||         6|\n|         3||         4|\n|         3||         5|\n|         3||         6|\n\nSchema removed from src.Catalog\n> Processed Query: drop table test1;\nSchema removed from src.Catalog\n> Processed Query: drop table test2;\nSchema removed from src.Catalog\n> Processed Query: quit;\nShutting down database...\nShutdown complete.\n";
            expected = expected.replaceAll("\r", "");
            String output = outputStreamCaptor.toString().replaceAll("\r", "");

            assertEquals(expected, output);

            tearDown();
        } catch (Exception e) {
            fail("Exception Caught!");
        }
    }

    @Test
    void testCartesianDifferentTypes() {
        try {
            tearDown();
            System.out.println("Testing SELECT attrs FROM two tables with different attr types.");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE test1 (foo VARCHAR(10) PRIMARYKEY, baz DOUBLE);\nCREATE TABLE test2 (foo INTEGER PRIMARYKEY);\nINSERT INTO test1 VALUES ('Hello' 1.0), ('Bye' 2.1), ('Kellen' 3.0);\nINSERT INTO test2 VALUES (4), (5), (6);\nSELECT test1.foo, test2.foo FROM test1, test2;\nDROP TABLE test1;\nDROP TABLE test2;\nQUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table test1 (foo varchar(10) primarykey, baz double);\nTable created successfully.\n> Processed Query: create table test2 (foo integer primarykey);\nTable created successfully.\n> Processed Query: insert into test1 values ('Hello' 1.0), ('Bye' 2.1), ('Kellen' 3.0);\n> Processed Query: insert into test2 values (4), (5), (6);\n> Processed Query: select test1.foo, test2.foo from test1, test2;\nTable created successfully.\n| test1.foo|| test2.foo|\n------------------------\n|     \"Bye\"||         4|\n|     \"Bye\"||         5|\n|     \"Bye\"||         6|\n|   \"Hello\"||         4|\n|   \"Hello\"||         5|\n|   \"Hello\"||         6|\n|  \"Kellen\"||         4|\n|  \"Kellen\"||         5|\n|  \"Kellen\"||         6|\n\nSchema removed from src.Catalog\n> Processed Query: drop table test1;\nSchema removed from src.Catalog\n> Processed Query: drop table test2;\nSchema removed from src.Catalog\n> Processed Query: quit;\nShutting down database...\nShutdown complete.\n";
            expected = expected.replaceAll("\r", "");
            String output = outputStreamCaptor.toString().replaceAll("\r", "");

            assertEquals(expected, output);

            tearDown();
        } catch (Exception e) {
            fail("Exception Caught!");
        }
    }

    @Test
    void testAttrsOutOfOrder() {

        try {
            tearDown();
            System.out.println("Testing SELECT out of order attrs.");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE foo(baz INTEGER PRIMARYKEY, bar DOUBLE NOTNULL, bazzle CHAR(10) UNIQUE NOTNULL);\n" +
                    "INSERT INTO foo VALUES (1 1 'test');\n" +
                    "INSERT INTO foo VALUES (2 1 'testi');\n" +
                    "INSERT INTO foo VALUES (3 1 'testin');\n" +
                    "SELECT bazzle, baz FROM foo;\n" +
                    "DROP TABLE foo;\n" +
                    "QUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table foo(baz integer primarykey, bar double notnull, bazzle char(10) unique notnull);\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: insert into foo values (1 1 'test');\n" +
                    "> Processed Query: insert into foo values (2 1 'testi');\n" +
                    "> Processed Query: insert into foo values (3 1 'testin');\n" +
                    "> Processed Query: select bazzle, baz from foo;\n" +
                    "|    bazzle||       baz|\n" +
                    "------------------------\n" +
                    "|    \"test\"||         1|\n" +
                    "|   \"testi\"||         2|\n" +
                    "|  \"testin\"||         3|\n\n" +
                    "> Processed Query: drop table foo;\n" +
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
    void testSelectAttrsAndStar() {
        try {
            tearDown();
            System.out.println("Testing SELECT *, a1, ..., an FROM table.");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE test1 (foo VARCHAR(10) PRIMARYKEY, baz DOUBLE);\nINSERT INTO test1 VALUES ('Hello' 1.0), ('Bye' 2.1), ('Kellen' 3.0);\nSELECT *, baz FROM test1;\nDROP TABLE test1;\nQUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table test1 (foo varchar(10) primarykey, baz double);\nTable created successfully.\n> Processed Query: insert into test1 values ('Hello' 1.0), ('Bye' 2.1), ('Kellen' 3.0);\n> Processed Query: select *, baz from test1;\nIllegal attribute arguments in Select statement.\n> Processed Query: drop table test1;\nSchema removed from src.Catalog\n> Processed Query: quit;\nShutting down database...\nShutdown complete.\n";
            expected = expected.replaceAll("\r", "");
            String output = outputStreamCaptor.toString().replaceAll("\r", "");

            assertEquals(expected, output);

            tearDown();
        } catch (Exception e) {
            fail("Exception Caught!");
        }
    }

    @Test
    void testOrderby() {
        try {
            tearDown();
            System.out.println("Testing SELECT * FROM table orderby x.");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE test1 (foo VARCHAR(10) PRIMARYKEY, baz DOUBLE);\n" +
                    "INSERT INTO test1 VALUES ('A' 5.0), ('Z' 2.1), ('G' 3.0);\n" +
                    "SELECT * FROM test1 ORDERBY baz;\n" +
                    "DROP TABLE test1;\n" +
                    "QUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table test1 (foo varchar(10) primarykey, baz double);\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: insert into test1 values ('A' 5.0), ('Z' 2.1), ('G' 3.0);\n" +
                    "> Processed Query: select * from test1 orderby baz;\n" +
                    "|       foo||       baz|\n" +
                    "------------------------\n" +
                    "|       \"Z\"||       2.1|\n" +
                    "|       \"G\"||       3.0|\n" +
                    "|       \"A\"||       5.0|\n\n" +
                    "> Processed Query: drop table test1;\n" +
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
    void testWhere() {
        try {
            tearDown();
            System.out.println("Testing SELECT * FROM table where x = y.");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE test1 (foo VARCHAR(10) PRIMARYKEY, baz DOUBLE);\n" +
                    "INSERT INTO test1 VALUES ('A' 5.0), ('Z' 2.1), ('G' 3.0);\n" +
                    "SELECT * FROM test1 WHERE baz = 5.0;\n" +
                    "DROP TABLE test1;\n" +
                    "QUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table test1 (foo varchar(10) primarykey, baz double);\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: insert into test1 values ('A' 5.0), ('Z' 2.1), ('G' 3.0);\n" +
                    "> Processed Query: select * from test1 where baz = 5.0;\n" +
                    "|       foo||       baz|\n" +
                    "------------------------\n" +
                    "|       \"A\"||       5.0|\n\n" +
                    "> Processed Query: drop table test1;\n" +
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
    void testWhereCartesian() {
        try {
            tearDown();
            System.out.println("Testing SELECT * FROM table1, table2 where table1.x = table2.y");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE test1 (foo VARCHAR(10) PRIMARYKEY, baz DOUBLE);\n" +
                    "CREATE TABLE test2 (bar VARCHAR(10) PRIMARYKEY, bazzle DOUBLE);\n" +
                    "INSERT INTO test1 VALUES ('A' 5.0), ('Z' 2.1), ('G' 3.0);\n" +
                    "INSERT INTO test2 VALUES ('A' 7.0), ('R' 3.1), ('G' 3.2);\n" +
                    "SELECT test1.foo, test2.bar FROM test1, test2 WHERE test1.foo = test2.bar;\n" +
                    "DROP TABLE test1;\n" +
                    "DROP TABLE test2;\n" +
                    "QUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table test1 (foo varchar(10) primarykey, baz double);\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: create table test2 (bar varchar(10) primarykey, bazzle double);\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: insert into test1 values ('A' 5.0), ('Z' 2.1), ('G' 3.0);\n" +
                    "> Processed Query: insert into test2 values ('A' 7.0), ('R' 3.1), ('G' 3.2);\n" +
                    "> Processed Query: select test1.foo, test2.bar from test1, test2 where test1.foo = test2.bar;\n" +
                    "Table created successfully.\n" +
                    "| test1.foo|| test2.bar|\n" +
                    "------------------------\n" +
                    "|       \"A\"||       \"A\"|\n" +
                    "|       \"G\"||       \"G\"|\n\n" +
                    "Schema removed from src.Catalog\n" +
                    "> Processed Query: drop table test1;\n" +
                    "Schema removed from src.Catalog\n" +
                    "> Processed Query: drop table test2;\n" +
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
    void testWhereOrderbyCartesian() {
        try {
            tearDown();
            System.out.println("Testing SELECT * FROM table1, table2 where table1.x = table2.y orderby table2.y");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE test1 (foo VARCHAR(10) PRIMARYKEY, baz DOUBLE);\n" +
                    "CREATE TABLE test2 (bar VARCHAR(10) PRIMARYKEY, bazzle DOUBLE);\n" +
                    "INSERT INTO test1 VALUES ('A' 1.0), ('Z' 2.1), ('G' 3.0);\n" +
                    "INSERT INTO test2 VALUES ('A' 7.0), ('R' 3.1), ('G' 3.2);\n" +
                    "SELECT * FROM test1, test2 WHERE test1.foo = test2.bar ORDERBY test2.bazzle;\n" +
                    "DROP TABLE test1;\n" +
                    "DROP TABLE test2;\n" +
                    "QUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table test1 (foo varchar(10) primarykey, baz double);\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: create table test2 (bar varchar(10) primarykey, bazzle double);\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: insert into test1 values ('A' 1.0), ('Z' 2.1), ('G' 3.0);\n" +
                    "> Processed Query: insert into test2 values ('A' 7.0), ('R' 3.1), ('G' 3.2);\n" +
                    "> Processed Query: select * from test1, test2 where test1.foo = test2.bar orderby test2.bazzle;\n" +
                    "Table created successfully.\n" +
                    "| test1.foo|| test1.baz|| test2.bar||test2.bazzle|\n" +
                    "--------------------------------------------------\n" +
                    "|       \"G\"||       3.0||       \"G\"||       3.2|\n" +
                    "|       \"A\"||       1.0||       \"A\"||       7.0|\n\n" +
                    "Schema removed from src.Catalog\n" +
                    "> Processed Query: drop table test1;\n" +
                    "Schema removed from src.Catalog\n" +
                    "> Processed Query: drop table test2;\n" +
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
    void testWhereColumnsNotInSelect() {
        try {
            tearDown();
            System.out.println("Testing SELECT with columns not in the WHERE");
            setUp();

            String[] args = { "db", "10000", "10000" };

            String input = "CREATE TABLE test1 (foo VARCHAR(10) PRIMARYKEY, baz DOUBLE);\n" +
                    "CREATE TABLE test2 (bar VARCHAR(10) PRIMARYKEY, bazzle DOUBLE);\n" +
                    "INSERT INTO test1 VALUES ('A' 1.0), ('Z' 3.1), ('G' 3.0);\n" +
                    "INSERT INTO test2 VALUES ('A' 7.0), ('R' 3.1), ('G' 3.2);\n" +
                    "SELECT test1.foo, test2.bar FROM test1, test2 WHERE test1.baz = test2.bazzle;\n" +
                    "DROP TABLE test1;\n" +
                    "DROP TABLE test2;\n" +
                    "QUIT;\n";
            ByteArrayInputStream inputIn = new ByteArrayInputStream(input.getBytes());
            System.setIn(inputIn);

            Main.main(args);

            String expected = "> Processed Query: create table test1 (foo varchar(10) primarykey, baz double);\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: create table test2 (bar varchar(10) primarykey, bazzle double);\n" +
                    "Table created successfully.\n" +
                    "> Processed Query: insert into test1 values ('A' 1.0), ('Z' 3.1), ('G' 3.0);\n" +
                    "> Processed Query: insert into test2 values ('A' 7.0), ('R' 3.1), ('G' 3.2);\n" +
                    "> Processed Query: select test1.foo, test2.bar from test1, test2 where test1.baz = test2.bazzle;\n" +
                    "Table created successfully.\n" +
                    "| test1.foo|| test2.bar|\n" +
                    "------------------------\n" +
                    "|       \"Z\"||       \"R\"|\n\n" +
                    "Schema removed from src.Catalog\n" +
                    "> Processed Query: drop table test1;\n" +
                    "Schema removed from src.Catalog\n" +
                    "> Processed Query: drop table test2;\n" +
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
