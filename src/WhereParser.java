package src;

import java.util.ArrayList;
import java.util.List;

public class WhereParser {
     private List<String> tokens;
    private int currentTokenIndex;

    public WhereNode parse(String expression) {
        tokens = tokenize(expression);
        currentTokenIndex = 0;
        return parseExpression();
    }

    private List<String> tokenize(String expression) {
        // This is a simple tokenizer; you might want to enhance it based on your needs
        String[] parts = expression.split("\\s+|(?<=[<>!=])|(?=[<>!=])");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty()) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    private WhereNode parseExpression() {
        WhereNode left = parseTerm();

        while (match("and") || match("or")) {
            String operator = tokens.get(currentTokenIndex - 1);
            WhereNode right = parseTerm();
            left = (operator.equals("and")) ? new AndNode(left, right) : new OrNode(left, right);
        }

        return left;
    }

    private WhereNode parseTerm() {
        WhereNode left = parseFactor();

        while (match("<", ">", "<=", ">=", "=", "!=")) {
            String operator = tokens.get(currentTokenIndex - 1);
            WhereNode right = parseFactor();
            left = new OperatorNode(left, right, operator);
        }

        return left;
    }

    private WhereNode parseFactor() {
        if (match("(")) {
            WhereNode expression = parseExpression();
            consume(")");
            return expression;
        } else if (matchIdentifier()) {
            return new VarNode(tokens.get(currentTokenIndex - 1));
        } else if (matchConstant()) {
            return new ConstNode(tokens.get(currentTokenIndex - 1));
        } else {
            // Handle unexpected token or syntax error
            throw new RuntimeException("Unexpected token: " + tokens.get(currentTokenIndex));
        }
    }

    private boolean match(String... expectedTokens) {
        for (String expectedToken : expectedTokens) {
            if (check(expectedToken)) {
                consume(expectedToken);
                return true;
            }
        }
        return false;
    }

    private boolean matchIdentifier() {
        return checkType(TokenType.IDENTIFIER);
    }

    private boolean matchConstant() {
        return checkType(TokenType.CONSTANT);
    }

    private boolean check(String expectedToken) {
        return currentTokenIndex < tokens.size() && tokens.get(currentTokenIndex).equals(expectedToken);
    }

    private boolean checkType(TokenType type) {
        return currentTokenIndex < tokens.size() && getTokenType(tokens.get(currentTokenIndex)) == type;
    }

    private void consume(String expectedToken) {
        if (check(expectedToken)) {
            currentTokenIndex++;
        } else {
            // Handle unexpected token or syntax error
            throw new RuntimeException("Expected token: " + expectedToken + ", but found: " + tokens.get(currentTokenIndex));
        }
    }

    private void consume(TokenType type) {
        if (checkType(type)) {
            currentTokenIndex++;
        } else {
            // Handle unexpected token or syntax error
            throw new RuntimeException("Expected token of type " + type + ", but found: " + tokens.get(currentTokenIndex));
        }
    }

    private TokenType getTokenType(String token) {
        // A simple method to determine the type of a token (identifier or constant)
        if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            return TokenType.IDENTIFIER;
        } else {
            return TokenType.CONSTANT;
        }
    }

    private enum TokenType {
        IDENTIFIER,
        CONSTANT
    }
}
