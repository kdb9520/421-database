package src;

import java.util.ArrayList;

public class VarNode implements WhereNode {

    private String varName;

    public VarNode(String varName){
        this.varName = varName;
    }

    @Override
    public boolean evaluate(Pair<Object,Object> pair,TableSchema tSchema, ArrayList<Integer> indexes) {
        return true;
    }

    @Override
    public Object get(Pair<Object,Object> pair, TableSchema tSchema, ArrayList<Integer> indexes) {
        // If one side of pair is null one isn't null just use that side
        Object first = pair.getFirst();
        Object second = pair.getSecond();
        if(first != null && second == null){
            return first;
        }

        if(second != null && first == null){
            return first;
        }

        // If both are not null then we need to check indexes to see which one is right
        // Get the index and see if its first or second value in indexes
        Integer index = tSchema.findAttribute(varName);
        // If the index is the first one in indexes then pair.Left is this var
        if(indexes.get(0) == index){
            return first;
        }
        // Otherwise this var is the Right val
        return second;
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
