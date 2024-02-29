public class VarNode implements WhereNode {

    private String varName;

    public VarNode(String varName){
        this.varName = varName;
    }

    @Override
    public boolean evaluate() {
        return true;
    }

    @Override
    public Object get() {
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
