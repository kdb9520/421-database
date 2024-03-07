public interface WhereNode {

    public boolean evaluate(Object o,TableSchema tSchema);
    public Object get();
    public WhereNode getLeft();
    public WhereNode getRight();


}


