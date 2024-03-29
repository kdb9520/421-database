package src;

import java.util.ArrayList;

public class AndNode implements WhereNode {
    private WhereNode lCondition;
    private WhereNode rCondition;

    public AndNode(WhereNode left, WhereNode right) {
        this.lCondition = left;
        this.rCondition = right;
    }

    @Override
    public boolean evaluate(ArrayList<Object> variables, ArrayList<String> variable_names, TableSchema tSchema) {
        // If any condition evaluates to true, return true
        return lCondition.evaluate(variables,variable_names,tSchema) && rCondition.evaluate(variables,variable_names,tSchema);
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

    @Override
    public void setLeft(WhereNode left) {
        this.lCondition = left;
    }

    @Override
    public void setRight(WhereNode right) {
        this.rCondition = right;
    }

    @Override
    public String toString(){
        return ("AND(" + lCondition.toString() + ", " + rCondition.toString() + ")");
        
    }

    @Override
    public String getType(TableSchema tSchema) {
        return null;
    }
}