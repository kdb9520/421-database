public class OrNode implements WhereNode {
    private WhereNode lCondition;
    private WhereNode rCondition;

    public OrNode(WhereNode left, WhereNode right) {
        this.lCondition = left;
        this.rCondition = right;
    }

    @Override
    public boolean evaluate() {
        // If any condition evaluates to true, return true
        return lCondition.evaluate() || rCondition.evaluate();
    }
}