package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WhereParser {
     private List<String> tokens;
    private int currentTokenIndex;

    public WhereNode parse(String expression) {
        tokens = tokenize(expression);
        currentTokenIndex = 0;
        WhereNode result = parseExpression();
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

   private WhereNode parseExpression() {
        WhereNode left = parseTerm();
        while (currentTokenIndex < tokens.size() && (tokens.get(currentTokenIndex).equals("and") || tokens.get(currentTokenIndex).equals("or"))) {
            String operator = tokens.get(currentTokenIndex);
            currentTokenIndex++;
            WhereNode right = parseTerm();
            if (operator.equals("and")) {
                left = new AndNode(left, right);
            } else {
                left = new OrNode(left, right);
            }
        }
        return left;
    }

    private WhereNode parseTerm() {
        WhereNode factor = parseFactor();
        while (currentTokenIndex < tokens.size() && (tokens.get(currentTokenIndex).equals("and") || tokens.get(currentTokenIndex).equals("or"))) {
            String operator = tokens.get(currentTokenIndex);
            currentTokenIndex++;
            WhereNode right = parseFactor();
            factor = new OperatorNode(factor, right,"test", operator);
        }
        return factor;
    }

    private WhereNode parseFactor() {
        String token = tokens.get(currentTokenIndex);
        currentTokenIndex++;
        
        boolean isInt = false;
        Integer intValue = null;
        // See if its integer or not
        try {
            intValue = Integer.parseInt(token);
            isInt = true;
        } catch (NumberFormatException e) {
        }
        
        if (token.startsWith("'") || token.startsWith("\"")) {
            return new ConstNode(token);
        } else if (token.equals("true")) {
            boolean t = true;
            return new ConstNode(t);
        }
        else if(token.equals("false")){
            boolean f = false;
            return new ConstNode(f);
        } 
        else if(isInt){
            return new ConstNode(intValue);
        }
        else {
            return new VarNode(token);
        }
    }

    
}
