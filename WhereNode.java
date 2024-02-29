public interface WhereNode {

    public boolean evaluate();
    public Object get();
    public WhereNode getLeft();
    public WhereNode getRight();


}


