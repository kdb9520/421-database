public class VarNode implements WhereNode {

    private String varName;

    public VarNode(String varName){
        this.varName = varName;
    }

    @Override
    public boolean evaluate(Pair<Object,Object> pair,TableSchema tSchema) {
        return true;
    }

    @Override
    public Object get(Pair<Object,Object> pair) {
        return this.varName;
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
