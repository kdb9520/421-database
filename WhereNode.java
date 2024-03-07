public interface WhereNode {

    public boolean evaluate(Pair<Object,Object> pair,TableSchema tSchema);
    public Object get(Pair<Object,Object> pair);
    public WhereNode getLeft();
    public WhereNode getRight();


}


