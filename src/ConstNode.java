package src;

import java.util.ArrayList;

public class ConstNode implements WhereNode {

    private Object value;

    public ConstNode(Object value){
        this.value = value;
    }

    @Override
    public boolean evaluate(ArrayList<Object> variables, ArrayList<String> variable_names, TableSchema tSchema) {
        return true;
    }

    @Override
    public Object get(ArrayList<Object> variables, ArrayList<String> variable_names, TableSchema tSchema) {
        return this.value;
    }

    @Override
    public WhereNode getLeft() {
        return null;
    }

    @Override
    public WhereNode getRight() {
        return null;
    }

    @Override
    public String toString(){
        return this.value.toString();
        
    }
    
}
