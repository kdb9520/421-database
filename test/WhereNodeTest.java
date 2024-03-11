package test;
import org.junit.jupiter.api.Test;

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
        String whereString = "where gpa>= 3 or name = 'ryan' ";
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
        TableSchema tSchema = new TableSchema("student", null);
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
        TableSchema tSchema = new TableSchema("student", null);
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
        TableSchema tSchema = new TableSchema("student", null);
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
        TableSchema tSchema = new TableSchema("student", null);
        assertFalse(riggedTree.evaluate(variables, variable_names, tSchema));
    }

     // Tests where student.gpa >= 2 or student.name = 'ryan' or student.year <= 2
     @Test
     void testTrueTwoLayerOrTree() {

        // First create a two layer OR tree
        
        
        VarNode varNode1 = new VarNode("student.gpa");
        ConstNode const1 = new ConstNode(2);
        OperatorNode opNode = new OperatorNode(varNode1, const1, "integer",">=");
        // Right will be = then student.name 'ryan'
        
        VarNode varNode2 = new VarNode("student.name");
        ConstNode const2 = new ConstNode("ryan");
        OperatorNode opNode2 = new OperatorNode(varNode2, const2,"varchar", "=");

        VarNode varNode3 = new VarNode("student.year");
        ConstNode const3 = new ConstNode(2);
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
         TableSchema tSchema = new TableSchema("student", null);
         assertTrue(riggedTree.evaluate(variables, variable_names, tSchema));
     }

     // Tests where student.gpa >= 2 or student.name = 'ryan' or student.year <= 2; here all conditions false
     @Test
     void testFalseTwoLayerOrTree() {

        // First create a two layer OR tree
        
        
        VarNode varNode1 = new VarNode("student.gpa");
        ConstNode const1 = new ConstNode(2);
        OperatorNode opNode = new OperatorNode(varNode1, const1, "integer",">=");
        // Right will be = then student.name 'ryan'
        
        VarNode varNode2 = new VarNode("student.name");
        ConstNode const2 = new ConstNode("ryan");
        OperatorNode opNode2 = new OperatorNode(varNode2, const2,"varchar", "=");

        VarNode varNode3 = new VarNode("student.year");
        ConstNode const3 = new ConstNode(2);
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
         TableSchema tSchema = new TableSchema("student", null);
         assertFalse(riggedTree.evaluate(variables, variable_names, tSchema));
     }

     // Tests if where x is not null works [ not implemented yet ]
     @Test
     void testVarNotNull() {
        assertFalse(true);
     }


    private static WhereNode createTestTree() {
        
        // Left will be >= then student.gpa 2
        
        VarNode varNode1 = new VarNode("student.gpa");
        ConstNode const1 = new ConstNode(2);
        OperatorNode opNode = new OperatorNode(varNode1, const1, "integer",">=");
        // Right will be = then student.name 'ryan'
        
        VarNode varNode2 = new VarNode("student.name");
        ConstNode const2 = new ConstNode("ryan");
        OperatorNode opNode2 = new OperatorNode(varNode2, const2,"varchar", "=");

        // First node AND
        AndNode firstNode = new AndNode(opNode, opNode2);
        return firstNode;
    }

    private static WhereNode createTestOrTree() {
        
        // Left will be >= then student.gpa 2
        
        VarNode varNode1 = new VarNode("student.gpa");
        ConstNode const1 = new ConstNode(2);
        OperatorNode opNode = new OperatorNode(varNode1, const1, "integer",">=");
        // Right will be = then student.name 'ryan'
        
        VarNode varNode2 = new VarNode("student.name");
        ConstNode const2 = new ConstNode("ryan");
        OperatorNode opNode2 = new OperatorNode(varNode2, const2,"varchar", "=");

        // First node AND
        OrNode firstNode = new OrNode(opNode, opNode2);
        return firstNode;
    }
}
