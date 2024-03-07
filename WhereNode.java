import java.util.ArrayList;

public interface WhereNode {

    public boolean evaluate(Pair<Object,Object> pair,TableSchema tSchema, ArrayList<Integer> indexes);
    public Object get(Pair<Object,Object> pair, TableSchema tSchema, ArrayList<Integer> indexes);
    public WhereNode getLeft();
    public WhereNode getRight();


}


