package src.bplustreedraft;

import src.BPlusTree;

public class InternalNodeDraft extends nodeDraft{
    int maxDegree;
    int minDegree;
    int degree;
    InternalNodeDraft leftSibling;
    InternalNodeDraft rightSibling;
    Integer[] keys;
    nodeDraft[] childPointers;


    public InternalNodeDraft(int m, Integer[] keys) {
        this.maxDegree = m;
        this.minDegree = (int) Math.ceil(m / 2.0);
        this.degree = 0;
        this.keys = keys;
        this.childPointers = new nodeDraft[this.maxDegree + 1];
    }

    public InternalNodeDraft(int m, Integer[] keys, nodeDraft[] pointers) {
        this.maxDegree = m;
        this.minDegree = (int) Math.ceil(m / 2.0);
        this.degree = bplustreedraft.linearNullSearch(pointers);   // don't know what this is for
        this.keys = keys;
        this.childPointers = pointers;
    }

    /**(
     * This adds a new child pointer to the node, to the end of the array
     * @param pointer
     */
    public void appendChildPointer(nodeDraft pointer) {
        this.childPointers[degree] = pointer;
        this.degree++;
    }

    /**
     * This finds the index of a pointer
     * @param pointer
     * @return
     */
    public int findIndexOfPointer(nodeDraft pointer) {
        for (int i = 0; i < childPointers.length; i++) {
            if (childPointers[i] == pointer) {
                return i;
            }
        }
        return -1;
    }

    /**
     * adds child node pointer to beginning of list, shifts everything up
     * @param pointer
     */
    public void prependChildPointer(nodeDraft pointer) {
        for (int i = degree - 1; i >= 0; i--) {
            childPointers[i + 1] = childPointers[i];
        }
        this.childPointers[0] = pointer;
        this.degree++;
    }

    /**
     * This inserts a pointer at a given index in the array
     */
    public void insertChildPointer(nodeDraft pointer, int index) {
        for (int i = degree - 1; i >= index; i--) {
            childPointers[i + 1] = childPointers[i];
        }
        this.childPointers[index] = pointer;
        this.degree++;
    }

    /**
     * Determines if the degree of hte node is too low
     * @return
     */
    public boolean degreeToLow(){
        return this.degree < this.minDegree;
    }

    /**
     * Is this node borrowable?
     * @return
     */
    public boolean isLendable(){
        return this.degree > this.minDegree;
    }

    /**
     * is this node allowed to bew merged?
     * @return
     */
    public boolean isMergeable(){
        return this.degree == this.minDegree;
    }

    /**
     * is this node overfilled?
     * @return
     */
    public boolean overfilled(){
        return this.degree == maxDegree + 1;
    }

    /**
     * Removes a key from the list, sets to null because the size of array does not change.
     * Constant amount of pointers
     * @param index
     */
    public void removeKey(int index){
        this.keys[index] = null;
    }

    /**
     * Removes pointer from the node based off certain index.
     * @param index
     */
    public void removePointer(int index) {
        this.childPointers[index] = null;
        this.degree--;
    }


    /**
     * removes a specific pointer from the list
     * @param pointer
     */
    public void removePointer(nodeDraft pointer) {
        for (int i = 0; i < childPointers.length; i++) {
            if (childPointers[i] == pointer) {
                this.childPointers[i] = null;
            }
        }
        this.degree--;
    }

    @Override
    public String toString() {
        String str = "Internal node:\t";
        if(keys != null){
            for(Integer i : keys){
                str += i;
                str += "\t";
            }
    
        }
        
        if(childPointers != null){
            for(nodeDraft child : childPointers ){
                if(child instanceof InternalNodeDraft){
                    InternalNodeDraft internalNode = (InternalNodeDraft) child;
                    str += internalNode.toString();
                }
    
                if(child instanceof leafNodeDraft){
                    leafNodeDraft internalNode = (leafNodeDraft) child;
                    str += internalNode.toString();
                }
            }
        }
        
    return str;
}

}
