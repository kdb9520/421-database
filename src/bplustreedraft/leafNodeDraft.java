package src.bplustreedraft;
import java.util.*;
public class leafNodeDraft extends nodeDraft{
    int maxNumPairs;
    int minNumPairs;
    int numPairs;
    leafNodeDraft leftSibling;
    leafNodeDraft rightSibling;
    public DictionaryPairDraft[] dictionary;

    /**
     * deletes a leaf node
     * @param index
     */
    public void delete(int index){
        this.dictionary[index] = null;
        numPairs--;
    }


    /**
     * inserts a key value pair into the tree
     * @param dp
     * @return
     */
    public boolean insert(DictionaryPairDraft dp){
        if (this.checkIfFull()) {
            return false;
        } else {
            this.dictionary[numPairs] = dp;
            numPairs++;
            Arrays.sort(this.dictionary, 0, numPairs);

            return true;
        }
    }

    /**
     * determines if the number of pairs is lower than what is allowed
     * @return
     */
    public boolean isTooLow(){
        return numPairs < minNumPairs;
    }

    /**
     * checks if node is full
     * @return
     */
    public boolean checkIfFull(){
        return numPairs == maxNumPairs;
    }

    /**
     * checks if a value can be borrowed
     * @return
     */
    public boolean borrowable(){
        return numPairs > minNumPairs;
    }

    /**
     * checks if you can borrow
     * @return
     */
    public boolean mergeable(){
        return numPairs == minNumPairs;
    }

    /**
     * creates new leaf node
     * @param m
     * @param dp
     */
    public leafNodeDraft(int m, DictionaryPairDraft dp) {
        this.maxNumPairs = m - 1;
        this.minNumPairs = (int) (Math.ceil(m / 2) - 1);
        this.dictionary = new DictionaryPairDraft[m];
        this.numPairs = 0;
        this.insert(dp);
    }

    /**
     * creates new leaf node for given parent
     * @param m
     * @param dps
     * @param parent
     */
    public leafNodeDraft(int m, DictionaryPairDraft[] dps, InternalNodeDraft parent) {
        this.maxNumPairs = m - 1;
        this.minNumPairs = (int) (Math.ceil(m / 2) - 1);
        this.dictionary = dps;
        this.numPairs = bplustreedraft.linearNullSearch(dps);
        this.parent = parent;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Leaf node: [ ");
        for (int i = 0; i < numPairs; i++) {
            str.append("(").append(dictionary[i].key).append(",").append(dictionary[i].value).append(")");
            if (i < numPairs - 1) {
                str.append(", ");
            }
        }
        str.append(" ]");
        return str.toString();
    }

}
