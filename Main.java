//Imports

// Main class for the project
// Authors:

public class Main {
    public static void main (String[] args) {

        if (args.length != 3) {
            displayUsage();
        }
        else {
            // restart / create the database

            // loop waiting for quit command
            // looking for different commands we can pass in
        }

    }



    public static void displayUsage() {
        System.out.println("USAGE: java Main <db loc> <page size> <buffer size>");
        System.out.println("Hello world again!");
    }

}