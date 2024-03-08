package test;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import src.Main;

import static org.junit.jupiter.api.Assertions.fail;

public class MainTest {

    @Test
    void testMain() {
        try{
            System.out.println("main");
            String[] args = null;
            final InputStream original = System.in;
            final FileInputStream fips = new FileInputStream(new File("[path_to_file]"));
            System.setIn(fips);
            Main.main(args);
            System.setIn(original);
        } catch(Exception e){
            fail("Exception detected in src.Main");
        }

    }
}
