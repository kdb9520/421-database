package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class WhereParser {
     private List<String> tokens;
    private int currentTokenIndex;
    private ArrayList<String> variableNames;

    public WhereParser(){
        this.variableNames = new ArrayList<>();
    }

    public WhereNode parse(String expression) {
        tokens = tokenize(expression);
        currentTokenIndex = 0;
        WhereNode result = buildTree();
        System.out.println(result);
        return null;
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
        Stack<WhereNode> stack = new Stack<>();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if(token.equals("and")){
                // Make and node and push it
                AndNode andNode = new AndNode(null, null);
                int test = stack.size();
                System.out.println(test);
                stack.push(andNode);
                int test2 = stack.size();
                System.out.println(test2);

                
            }
            else if(token.equals("or")){
                // Make and node and push it
                OrNode orNode = new OrNode(null, null);
                stack.push(orNode);

            }
            else if (isOperator(token)) {
                // Stops stuff like where and and
                if (i >= (tokens.size()-1) || isOperator(tokens.get(i+1))) {
                   // This is invalid where statement
                   System.err.println("Invalid where statement");
                   return null;
                }

                
                // Determine if its AND/OR or an Operator and make right node
                // Create an operator node (now with potential right operand)
                OperatorNode operatorNode = new OperatorNode(token);

                // Get the right operand 
                WhereNode right = createNode(tokens.get(i+1));
                i++; // Increment i since we peaked ahead
                operatorNode.setRight(right);
    
                // Pop left operand from the stack (if available)
                if (stack.size() >= 1) {
                    operatorNode.setLeft(stack.pop());
                } else {
                    // Handle potential incomplete expression (optional)
                    throw new RuntimeException("Incomplete expression encountered!"); // Or handle differently
                }
    
                // Push the operator node (now with potentially both operands) back onto the stack
                stack.push(operatorNode);
            } else {
                // Create a value node and push it onto the stack
                stack.push(createNode(token));
            }
        }
        // If > 1 left in stack it means we left with ex: opNode andNode opNode
        if(stack.size() > 1){
            WhereNode left = stack.pop();
            WhereNode middle = stack.pop();
            WhereNode right = stack.pop();
            middle.setLeft(left);
            middle.setRight(right);
            return middle;
        }
        
        // The root of the tree is the only remaining node on the stack, ex goa > 3
        return stack.pop();
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