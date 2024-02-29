public class AndNode implements WhereNode {
    private WhereNode lCondition;
    private WhereNode rCondition;

    public AndNode(WhereNode left, WhereNode right) {
        this.lCondition = left;
        this.rCondition = right;
    }

    @Override
    public boolean evaluate() {
        // If any condition evaluates to true, return true
        return lCondition.evaluate() && rCondition.evaluate();
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