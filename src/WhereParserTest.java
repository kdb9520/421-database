package src;

import java.util.ArrayList;

public class WhereParserTest {
    public static void main(String[] args) {
        String expression = "where student.gpa >= 2 and student.name = 'ryan'";

        WhereParser whereParser = new WhereParser();
        //WhereNode expressionTree = whereParser.parse(expression);
        WhereNode riggedTree = createTestTree();
        // Print the parsed expression tree
        System.out.println(riggedTree);

        // Now lets test evaluating it
        System.out.println(test1(riggedTree));
    }

    // Take a tree in and test its evaluate
    private static boolean test1(WhereNode riggedTree) {
        
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
        return riggedTree.evaluate(variables, variable_names, tSchema);
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
}
