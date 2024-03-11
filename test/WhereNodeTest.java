package test;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import src.AndNode;
import src.AttributeSchema;
import src.ConstNode;
import src.OperatorNode;
import src.OrNode;
import src.TableSchema;
import src.VarNode;
import src.WhereNode;
import src.WhereParser;

public class WhereNodeTest {

    @Test
    void test() {
        String whereString = "where student.id = 2 and class.studentid = 2 ";
        WhereParser wp = new WhereParser();
        wp.parse(whereString);
    }

    // Tests where student.gpa >= 2 and student.name = 'ryan'
    @Test
    void testTrueTree1() {
        WhereNode riggedTree = createTestTree();
        ArrayList<String> variable_names = new ArrayList<>();
        variable_names.add("student.gpa");
        variable_names.add("student.name");

        // Initialize Variables student.gpa and student.name
        ArrayList<Object> variables = new ArrayList<>();
        // First lets make the gpa object
        Integer gpa = 2;
        variables.add(gpa);

        String name = "ryan";
        variables.add(name);

        // Create mock table Schema
        // First make the AttributeSchemas for GPA and then Name
        AttributeSchema attr1 = new AttributeSchema("student.gpa", "integer", null);
        AttributeSchema attr2 = new AttributeSchema("student.name", "varchar(50)", null);
        ArrayList<AttributeSchema> aSchema = new ArrayList<>();
        aSchema.add(attr1);
        aSchema.add(attr2);
        TableSchema tSchema = new TableSchema("student", aSchema);
        assertTrue(riggedTree.evaluate(variables, variable_names, tSchema));
    }

    // Tests where student.gpa >= 2 and student.name = 'ryan' but Gpa here is < 2
    @Test
    void testTrueTree2() {
        WhereNode riggedTree = createTestTree();
        ArrayList<String> variable_names = new ArrayList<>();
        variable_names.add("student.gpa");
        variable_names.add("student.name");

        // Initialize Variables student.gpa and student.name
        ArrayList<Object> variables = new ArrayList<>();
        // First lets make the gpa object
        Integer gpa = 1;
        variables.add(gpa);

        String name = "ryan";
        variables.add(name);

        // Create mock table Schema
        // First make the AttributeSchemas for GPA and then Name
        AttributeSchema attr1 = new AttributeSchema("student.gpa", "integer", null);
        AttributeSchema attr2 = new AttributeSchema("student.name", "varchar(50)", null);
        ArrayList<AttributeSchema> aSchema = new ArrayList<>();
        aSchema.add(attr1);
        aSchema.add(attr2);
        TableSchema tSchema = new TableSchema("student", aSchema);
        assertFalse(riggedTree.evaluate(variables, variable_names, tSchema));
    }

    // Tests where student.gpa >= 2 or student.name = 'ryan' but Gpa here is < 2, name is ryan
    @Test
    void testTrueOrTree() {
        WhereNode riggedTree = createTestOrTree();
        ArrayList<String> variable_names = new ArrayList<>();
        variable_names.add("student.gpa");
        variable_names.add("student.name");

        // Initialize Variables student.gpa and student.name
        ArrayList<Object> variables = new ArrayList<>();
        // First lets make the gpa object
        Integer gpa = 1;
        variables.add(gpa);

        String name = "ryan";
        variables.add(name);

        // Create mock table Schema
        // First make the AttributeSchemas for GPA and then Name
        AttributeSchema attr1 = new AttributeSchema("student.gpa", "integer", null);
        AttributeSchema attr2 = new AttributeSchema("student.name", "varchar(50)", null);
        ArrayList<AttributeSchema> aSchema = new ArrayList<>();
        aSchema.add(attr1);
        aSchema.add(attr2);
        TableSchema tSchema = new TableSchema("student", aSchema);
        assertTrue(riggedTree.evaluate(variables, variable_names, tSchema));
    }

    // Tests where student.gpa >= 2 or student.name = 'ryan' but both are false
    @Test
    void testFalseOrTree() {
        WhereNode riggedTree = createTestOrTree();
        ArrayList<String> variable_names = new ArrayList<>();
        variable_names.add("student.gpa");
        variable_names.add("student.name");

        // Initialize Variables student.gpa and student.name
        ArrayList<Object> variables = new ArrayList<>();
        // First lets make the gpa object
        Integer gpa = 1;
        variables.add(gpa);

        String name = "ryan2";
        variables.add(name);

        // Create mock table Schema
        // First make the AttributeSchemas for GPA and then Name
        AttributeSchema attr1 = new AttributeSchema("student.gpa", "integer", null);
        AttributeSchema attr2 = new AttributeSchema("student.name", "varchar(50)", null);
        ArrayList<AttributeSchema> aSchema = new ArrayList<>();
        aSchema.add(attr1);
        aSchema.add(attr2);
        TableSchema tSchema = new TableSchema("student", aSchema);
        assertFalse(riggedTree.evaluate(variables, variable_names, tSchema));
    }

     // Tests where student.gpa >= 2 or student.name = 'ryan' or student.year <= 2
     @Test
     void testTrueTwoLayerOrTree() {

        // First create a two layer OR tree
        
        
        VarNode varNode1 = new VarNode("student.gpa");
        ConstNode const1 = new ConstNode(2,"integer");
        OperatorNode opNode = new OperatorNode(varNode1, const1, "integer",">=");
        // Right will be = then student.name 'ryan'
        
        VarNode varNode2 = new VarNode("student.name");
        ConstNode const2 = new ConstNode("ryan","varchar");
        OperatorNode opNode2 = new OperatorNode(varNode2, const2,"varchar", "=");

        VarNode varNode3 = new VarNode("student.year");
        ConstNode const3 = new ConstNode(2,"integer");
        OperatorNode opNode3 = new OperatorNode(varNode3, const3,"integer", "<=");
        OrNode secondOr = new OrNode(opNode, opNode2);

        // First node AND
        OrNode firstNode = new OrNode(secondOr, opNode3);

        // Test now
         WhereNode riggedTree = firstNode;
         System.out.println(riggedTree);
         ArrayList<String> variable_names = new ArrayList<>();
         variable_names.add("student.gpa");
         variable_names.add("student.name");
         variable_names.add("student.year");

 
         // Initialize Variables student.gpa and student.name
         ArrayList<Object> variables = new ArrayList<>();
         // First lets make the gpa object
         Integer gpa = 1;
         variables.add(gpa);
 
         String name = "ryan2";
         variables.add(name);

         Integer year = 1;
         variables.add(year);
 
         // Create mock table Schema
         // First make the AttributeSchemas for GPA and then Name
         AttributeSchema attr1 = new AttributeSchema("student.gpa", "integer", null);
         AttributeSchema attr2 = new AttributeSchema("student.name", "varchar(50)", null);
         AttributeSchema attr3 = new AttributeSchema("student.year", "integer", null);

         ArrayList<AttributeSchema> aSchema = new ArrayList<>();
         aSchema.add(attr1);
         aSchema.add(attr2);
         aSchema.add(attr3);
         TableSchema tSchema = new TableSchema("student", aSchema);
         assertTrue(riggedTree.evaluate(variables, variable_names, tSchema));
     }

     // Tests where student.gpa >= 2 or student.name = 'ryan' or student.year <= 2; here all conditions false
     @Test
     void testFalseTwoLayerOrTree() {

        // First create a two layer OR tree
        
        
        VarNode varNode1 = new VarNode("student.gpa");
        ConstNode const1 = new ConstNode(2,"integer");
        OperatorNode opNode = new OperatorNode(varNode1, const1, "integer",">=");
        // Right will be = then student.name 'ryan'
        
        VarNode varNode2 = new VarNode("student.name");
        ConstNode const2 = new ConstNode("ryan","varchar");
        OperatorNode opNode2 = new OperatorNode(varNode2, const2,"varchar", "=");

        VarNode varNode3 = new VarNode("student.year");
        ConstNode const3 = new ConstNode(2,"integer");
        OperatorNode opNode3 = new OperatorNode(varNode3, const3,"integer", "<=");
        OrNode secondOr = new OrNode(opNode, opNode2);

        // First node AND
        OrNode firstNode = new OrNode(secondOr, opNode3);

        // Test now
         WhereNode riggedTree = firstNode;
         System.out.println(riggedTree);
         ArrayList<String> variable_names = new ArrayList<>();
         variable_names.add("student.gpa");
         variable_names.add("student.name");
         variable_names.add("student.year");

 
         // Initialize Variables student.gpa and student.name
         ArrayList<Object> variables = new ArrayList<>();
         // First lets make the gpa object
         Integer gpa = 1;
         variables.add(gpa);
 
         String name = "ryan2";
         variables.add(name);

         Integer year = 3;
         variables.add(year);
 
         // Create mock table Schema
         // First make the AttributeSchemas for GPA and then Name
         AttributeSchema attr1 = new AttributeSchema("student.gpa", "integer", null);
         AttributeSchema attr2 = new AttributeSchema("student.name", "varchar(50)", null);
         AttributeSchema attr3 = new AttributeSchema("student.year", "integer", null);

         ArrayList<AttributeSchema> aSchema = new ArrayList<>();
         aSchema.add(attr1);
         aSchema.add(attr2);
         aSchema.add(attr3);
         TableSchema tSchema = new TableSchema("student", aSchema);
         assertFalse(riggedTree.evaluate(variables, variable_names, tSchema));
     }

     // Tests if where x is not null works [ not implemented yet ]
     @Test
     void testVarNotNull() {
        assertFalse(true);
     }

     // Tests WhereParser functionality
     // Given a string it should return a correct tree
     @Test
     void tesWhereParser() {
        WhereParser wp = new WhereParser();

        ArrayList<String> tests = new ArrayList<>();
        ArrayList<String> expected = new ArrayList<>();

        String test1 = "where gpa > 3";
        tests.add(test1);

        String test2 = "where gpa > 3 and name = 'bob'";
        tests.add(test2);

        String test3 = "where gpa > 3 or name = 'bob'";
        tests.add(test3);

        String test4 = "where gpa != null";
        tests.add(test4);

        String result1 = ">(gpa, 3)";
        expected.add(result1);

        String result2 = "AND(=(name, bob), >(gpa, 3))";
        expected.add(result2);

        String result3 = "OR(=(name, bob), >(gpa, 3))";
        expected.add(result3);

        String result4 = "!=(gpa, null)";
        expected.add(result4);

        // Do each test
        for(int i = 0; i < tests.size(); i++){
            String result = wp.parse(tests.get(i)).toString();
            assertEquals(result,expected.get(i));
        }
        


     }


    private static WhereNode createTestTree() {
        
        // Left will be >= then student.gpa 2
        
        VarNode varNode1 = new VarNode("student.gpa");
        ConstNode const1 = new ConstNode(2,"integer");
        OperatorNode opNode = new OperatorNode(varNode1, const1, "integer",">=");
        // Right will be = then student.name 'ryan'
        
        VarNode varNode2 = new VarNode("student.name");
        ConstNode const2 = new ConstNode("ryan","varchar");
        OperatorNode opNode2 = new OperatorNode(varNode2, const2,"varchar", "=");

        // First node AND
        AndNode firstNode = new AndNode(opNode, opNode2);
        System.out.println(firstNode);
        return firstNode;
    }

    private static WhereNode createTestOrTree() {
        
        // Left will be >= then student.gpa 2
        
        VarNode varNode1 = new VarNode("student.gpa");
        ConstNode const1 = new ConstNode(2,"integer");
        OperatorNode opNode = new OperatorNode(varNode1, const1, "integer",">=");
        // Right will be = then student.name 'ryan'
        
        VarNode varNode2 = new VarNode("student.name");
        ConstNode const2 = new ConstNode("ryan","varchar");
        OperatorNode opNode2 = new OperatorNode(varNode2, const2,"varchar", "=");

        // First node AND
        OrNode firstNode = new OrNode(opNode, opNode2);
        return firstNode;
    }
}
