package src;

import java.util.ArrayList;

public class VarNode implements WhereNode {

    private String varName;

    public VarNode(String varName){
        this.varName = varName;
    }

    @Override
    public boolean evaluate(ArrayList<Object> variables, ArrayList<String> variable_names, TableSchema tSchema) {
        return true;
    }

    @Override
    public Object get(ArrayList<Object> variables, ArrayList<String> variable_names, TableSchema tSchema) {
        
        // If both are not null then we need to check indexes to see which one is right
        // Get the index and see if its first or second value in indexes
        int this_var_index = -1;
        for(int i=0; i < variable_names.size(); i++){
            if(variable_names.get(i) == this.varName){
                this_var_index = i;
                break;
            }
        }
        return variables.get(this_var_index);
        
    }

    @Override
    public WhereNode getLeft() {
        return null;
    }

    @Override
    public WhereNode getRight() {
        return null;
    }

    @Override
    public String toString(){
        return (varName);
        
    }

    @Override
    public void setLeft(WhereNode left) {
        return;
    }

    @Override
    public void setRight(WhereNode right) {
        return;
    }


    
}
