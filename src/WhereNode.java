package src;

import java.util.ArrayList;

public interface WhereNode {

    // Takes in a tuple of up to two Objects
    // These are the varNodes
    public boolean evaluate(ArrayList<Object> variables, ArrayList<String> variable_names, TableSchema tSchema);
    public Object get(ArrayList<Object> variables, ArrayList<String> variable_names, TableSchema tSchema);
    public WhereNode getLeft();
    public WhereNode getRight();
    public void setLeft(WhereNode left);
    public void setRight(WhereNode right);


}