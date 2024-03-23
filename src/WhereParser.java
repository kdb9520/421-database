package src;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class WhereParser {
     private List<String> tokens;
    private ArrayList<String> variableNames;
    private static final Map<String, Integer> precedence = Map.of(
        "and", 2,
        "or", 1,
        ">", 3,
        ">=", 3,
        "<", 3,
        "<=", 3,
        "=", 3,
        "!=", 3
    );


    public WhereParser(){
        this.variableNames = new ArrayList<>();
    }

    public WhereNode parse(String expression) {
        tokens = tokenize(expression);
        WhereNode result = buildTree();
        System.out.println(result);
        return result;
    }

    private List<String> tokenize(String expression) {
        // This is a simple tokenizer; you might want to enhance it based on your needs
        String[] parts = expression.split("\\s+|(?<=[<>!=]=)|(?=[<>!=]=)");

        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty() && !part.equals("where")) {
                tokens.add(part);
            }
        }
        return tokens;
    }

   private WhereNode buildTree() {
        // Initialize a stack for nodes
        Stack<WhereNode> outputStack = new Stack<>();
        Stack<String> operatorStack = new Stack<>();

        for (String token : tokens) {
            if (isOperator(token)) {
                while (!operatorStack.isEmpty() && isOperator(operatorStack.peek())) {
                    String op = operatorStack.pop();
                    WhereNode right = outputStack.pop();
                    WhereNode left = outputStack.pop();
                    outputStack.push(new OperatorNode(left, right,null, op));
                }
                operatorStack.push(token);
            } else if (token.equals("and") || token.equals("or")) {
                while (!operatorStack.isEmpty() && precedence.get(token) <= precedence.get(operatorStack.peek())) {
                    String op = operatorStack.pop();
                    WhereNode right = outputStack.pop();
                    WhereNode left = outputStack.pop();
                    if (op.equals("and")) {
                        outputStack.push(new AndNode(left, right));
                    } else if (op.equals("or")) {
                        outputStack.push(new OrNode(left, right));
                    } else if(isOperator(op)){
                        WhereNode node = createNode(op);
                        node.setLeft(left);
                        node.setRight(right);
                        outputStack.push(node);
                    }
                    else {
                        throw new IllegalArgumentException("Invalid operator: " + op);
                    }
                }
                operatorStack.push(token); // This line
            } else {
                outputStack.push(createNode(token));
            }
        }

        while (!operatorStack.isEmpty()) {
            String op = operatorStack.pop();
            WhereNode right = outputStack.pop();
            WhereNode left = outputStack.pop();
            if (op.equals("and")) {
                outputStack.push(new AndNode(left, right));
            } else if (op.equals("or")) {
                outputStack.push(new OrNode(left, right));
            } else {
                outputStack.push(new OperatorNode(left, right, null, op));
            }
        }

        if (outputStack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression");
        }

        return outputStack.pop();
    }
    // Takes a token, figures out its type and creates a node of that type
    private WhereNode createNode(String token) {
    // If token is And or an OR easy to make
    if(token.equals("and")){
        return new AndNode(null, null);
    }
    else if (token.equals("or")){
        return new OrNode(null, null);
    }
    else if (isOperator(token)){
        return new OperatorNode(token);
    }
    else if (token.equals("true") || token.equals("false")){
        boolean o = Boolean.valueOf(token);
        return new ConstNode(o, "boolean");
    }
    else if(token.startsWith("'") || token.startsWith("\"")){
        // Lets parse the '' or "" and ; out
        String string = parseString(token);
        return new ConstNode(string, "varchar");
    }
    else if(token.equals(null)){
        return new ConstNode(null,null);
    }
    // Now need to check if its integer or double,if not its a varNode

    try {
        Integer number = Integer.parseInt(token);
        return new ConstNode(number, "integer");
      } catch (NumberFormatException e) {
      }

      try {
        double number = Double.parseDouble(token);
        return new ConstNode(number, "double");
      } catch (NumberFormatException e) {
      }

      // Its a variable node!
      variableNames.add(token);
      return new VarNode(token);

      
}

    boolean isOperator(String token) {
        return token.equals(">") || token.equals(">=") || token.equals("<") || token.equals("<=") || token.equals("=") || token.equals("!=") ;
    }

    String peekNextToken(ArrayList<String> tokens, String currentToken) {
        int currentIndex = tokens.indexOf(currentToken);
        if (currentIndex < tokens.size() - 1) {
            return tokens.get(currentIndex + 1);
        } else {
            // Handle potential incomplete expression (optional)
            throw new RuntimeException("Incomplete expression encountered!"); // Or handle differently
        }
    }

    public ArrayList<String> getVariableNames(){
        return this.variableNames;
    }

    public static String parseString(String str) {
        if (str.length() < 2) {
          return ""; // Handle empty or single character strings
        }
    
        char startQuote = str.charAt(0);
        char endQuote = startQuote == '\'' ? '\'' : '"'; // Determine closing quote
    
        int startIndex = str.indexOf(startQuote) + 1;
        if (startIndex < 1 || startIndex >= str.length()) {
          return ""; // Handle missing opening quote
        }
    
        int endIndex = str.indexOf(endQuote, startIndex);
        if (endIndex == -1) {
          return ""; // Handle missing closing quote
        }
    
        return str.substring(startIndex, endIndex);
      }
}