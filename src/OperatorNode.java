package src;

import java.util.ArrayList;

public class OperatorNode implements WhereNode{
    String operator;
    private WhereNode left;
    private WhereNode right;
    String type;

    public OperatorNode(WhereNode left, WhereNode right, String type, String operator){
        this.left = left;
        this.right = right;
        this.operator = operator;
        this.type = type;

    }

    // When creating a tree we dont have left and right immediately
    public OperatorNode(String operator){
        this.operator = operator;

    }

    // Takes an operation = > >= < <= 
    @Override
    public boolean evaluate(ArrayList<Object> variables, ArrayList<String> variable_names, TableSchema tSchema) {
        // First get the left and right value
        // In a proper tree these must be a var or constant
        Object leftVal = left.get(variables,variable_names,tSchema);
        Object rightVal = right.get(variables,variable_names,tSchema);
        
        // Get type and make sure type of both sides match
        String leftType = left.getType(tSchema);
        String rightType = right.getType(tSchema);

        if(leftType == null && rightType == null){
            System.err.println("Can not compare null to null, Returning false for WhereNode.");
                return false;
        }

        else if(leftType == null && rightType != null){
            // This case is legal but lets make both types match the non null type for type casting purposes
            leftType = rightType;
        }

        else if(leftType != null && rightType == null){
            // This case is legal but lets make both types match the non null type for type casting purposes
            rightType = leftType;
        }

        else if(!leftType.equals(rightType)){
            // Constants are all marked as a varchar, just check and make sure our variable isn't a char. If it is a char its a valid comparison
            if((!(leftType.startsWith("varchar") && rightType.startsWith("char")) || leftType.startsWith("char") && rightType.startsWith("varchar"))){
                System.err.println("Types of var and constant or var and var do not match. Returning false for WhereNode");
                return false;
            }
        }

        


        
        switch(operator){
            case "=":
                return nodeEquals(leftVal,rightVal,leftType);
            case "!=":
                return !nodeEquals(leftVal,rightVal,leftType);
            case ">":
                return nodeGreater(leftVal,rightVal,leftType);
            case ">=":
                return (nodeGreater(leftVal,rightVal,leftType) || nodeEquals(leftVal,rightVal,leftType));
            case "<":
                return !nodeGreater(leftVal,rightVal,leftType);
            case "<=":
                return (!nodeGreater(leftVal,rightVal,leftType) || nodeEquals(leftVal,rightVal,leftType));
            default:
                return false;
            
        }
    }

    private boolean nodeEquals(Object leftVal, Object rightVal, String type) {
        if (type.equals("integer")) {
            if ((Integer) leftVal == (Integer) rightVal) {
                return true;
            }
            else{
                return false;
            }
        } else if (type.startsWith("varchar") || type.startsWith("char")) {
            String leftString = (String) leftVal;
            String rightString = (String)  rightVal;
            // If left is greater than right
            if (leftString.equals(rightString)) {
                return true;
            }
            else{
                return false;
            }
        } else if (type.equals("double")) {
            if (((Double) leftVal).equals((Double) rightVal)) {
                return true;
            } else{
                return false;
            }
        }
         else if (type.equals("boolean")) {
            
            Boolean leftBoolean = (Boolean) leftVal;
            Boolean rightBoolean = (Boolean) rightVal;
            
            if (leftBoolean.equals(rightBoolean)) {
                return true;
            }
            else{
                return false;
            }
        }
        return false;
        
    }

    private boolean nodeGreater(Object leftVal, Object rightVal, String type) {
        // Type cast appropiately then compare records
        if (type.equals("integer")) {
            if ((Integer) leftVal > (Integer) rightVal) {
                return true;
            }
            else{
                return false;
            }
        } else if (type.startsWith("varchar")) {
            String leftString = (String) leftVal;
            String rightString = (String)  rightVal;
            // If left is greater than right
            if (leftString.compareTo(rightString) > 0) {
                return true;
            }
            else{
                return false;
            }
        } else if (type.equals("double")) {
            if ((Double) leftVal > (Double) rightVal) {
                return true;
        } else{
            return false;
        }
        }
         else if (type.equals("boolean")) {
            
            Boolean leftBoolean = (Boolean) leftVal;
            Boolean rightBoolean = (Boolean) rightVal;
            
            if (leftBoolean.compareTo(rightBoolean) > 0) {
                return true;
            }
            else{
                return false;
            }
        }
        return false;

    }

    @Override
    public Object get(ArrayList<Object> variables, ArrayList<String> variable_names, TableSchema tSchema) {
        return null;
    }

    @Override
    public WhereNode getLeft() {
        return this.left;
    }

    @Override
    public WhereNode getRight() {
        return this.right;
    }

    @Override
    public void setLeft(WhereNode left) {
        this.left = left;
    }

    @Override
    public void setRight(WhereNode right) {
        this.right = right;
    }

    @Override
    public String toString(){
        return (operator + "(" + left.toString() + ", " + right.toString() + ")");
        
    }

    @Override
    public String getType(TableSchema tSchema) {
        return null;
    }

    
}
