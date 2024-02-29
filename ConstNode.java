public class ConstNode implements WhereNode {

    private Object value;

    public ConstNode(Object value){
        this.value = value;
    }

    @Override
    public boolean evaluate() {
        return true;
    }

    @Override
    public Object get() {
        return this.value;
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
