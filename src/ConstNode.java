package src;

import java.util.ArrayList;

public class ConstNode implements WhereNode {

    private Object value;

    public ConstNode(Object value){
        this.value = value;
    }

    @Override
    public boolean evaluate(Pair<Object,Object> pair,TableSchema tSchema,ArrayList<Integer> indexes) {
        return true;
    }

    @Override
    public Object get(Pair<Object,Object> pair, TableSchema tSchema, ArrayList<Integer> indexes) {
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
    
}
