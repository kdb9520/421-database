import java.util.ArrayList;

public class OperatorNode implements WhereNode{
    String operator;
    private WhereNode left;
    private WhereNode right;
    String type;

    public OperatorNode(WhereNode left, WhereNode right, String type){
        this.left = left;
        this.right = right;
        this.type = type;
    }

    // Takes an operation = > >= < <= 
    @Override
    public boolean evaluate(Pair<Object,Object> pair,TableSchema tSchema, ArrayList<Integer> indexes) {
        // First get the left and right value
        // In a proper tree these must be a var or constant
        Object leftVal = left.get(pair,tSchema,indexes);
        Object rightVal = left.get(pair,tSchema,indexes);
        


        
        switch(operator){
            case "=":
                return nodeEquals(leftVal,rightVal);
            case "!=":
                return !nodeEquals(leftVal,rightVal);
            case ">":
                return nodeGreater(leftVal,rightVal);
            case ">=":
                return (nodeGreater(leftVal,rightVal) || nodeEquals(leftVal,rightVal));
            case "<":
                return !nodeGreater(leftVal,rightVal);
            case "<=":
                return (!nodeGreater(leftVal,rightVal) || nodeEquals(leftVal,rightVal));
            default:
                return false;
            
        }
    }

    private boolean nodeEquals(Object leftVal, Object rightVal) {
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
            if ((Double) leftVal == (Double) rightVal) {
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

    private boolean nodeGreater(Object leftVal, Object rightVal) {
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
    public Object get(Pair<Object,Object> pair, TableSchema tSchema, ArrayList<Integer> indexes) {
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
    
}
