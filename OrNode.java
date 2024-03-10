import java.util.ArrayList;

public class OrNode implements WhereNode {
    private WhereNode lCondition;
    private WhereNode rCondition;

    public OrNode(WhereNode left, WhereNode right) {
        this.lCondition = left;
        this.rCondition = right;
    }

    @Override
    public boolean evaluate(Pair<Object,Object> pair,TableSchema tSchema,ArrayList<Integer> indexes) {
        // If any condition evaluates to true, return true
        return lCondition.evaluate(pair,tSchema, indexes) || rCondition.evaluate(pair,tSchema, indexes);
    }

    @Override
    public Object get(Pair<Object,Object> pair, TableSchema tSchema, ArrayList<Integer> indexes) {
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