public class OrNode implements WhereNode {
    private WhereNode lCondition;
    private WhereNode rCondition;

    public OrNode(WhereNode left, WhereNode right) {
        this.lCondition = left;
        this.rCondition = right;
    }

    @Override
    public boolean evaluate(Object o,TableSchema tSchema) {
        // If any condition evaluates to true, return true
        return lCondition.evaluate(o,tSchema) || rCondition.evaluate(o,tSchema);
    }

    @Override
    public Object get() {
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