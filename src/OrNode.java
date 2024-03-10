package src;

import java.util.ArrayList;

public class OrNode implements WhereNode {
    private WhereNode lCondition;
    private WhereNode rCondition;

    public OrNode(WhereNode left, WhereNode right) {
        this.lCondition = left;
        this.rCondition = right;
    }

    @Override
    public boolean evaluate(ArrayList<Object> variables, ArrayList<String> variable_names, TableSchema tSchema) {
        // If any condition evaluates to true, return true
        return lCondition.evaluate(variables,variable_names,tSchema) || rCondition.evaluate(variables,variable_names,tSchema);
    }

    @Override
    public Object get(ArrayList<Object> variables, ArrayList<String> variable_names, TableSchema tSchema) {
        return null;
    }

    @Override
    public WhereNode getLeft() {
        return this.lCondition;
    }

    @Override
    public WhereNode getRight() {
        return this.rCondition;
    }
    
}