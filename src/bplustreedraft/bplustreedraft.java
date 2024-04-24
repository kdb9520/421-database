package src.bplustreedraft;
import java.util.*;
public class bplustreedraft {

    // rules
    // each internal node has between ceiling(n/2) and n children
    // leaf node has between celing((n-1)/2) and n - 1 values.
    // if root not leaf, has at least two children
    // if root is leaf, can have between 0 and (n - 1) values

    int m;
    InternalNodeDraft root;
    leafNodeDraft firstLeaf;


    /** performs binary search
     * on the key value pairs
     * @param dps
     * @param numPairs
     * @param t
     * @return
     */
    private int binarySearch(DictionaryPairDraft[] dps, int numPairs, int t){
        Comparator<DictionaryPairDraft> c = new Comparator<DictionaryPairDraft>() {
            @Override
            public int compare(DictionaryPairDraft o1, DictionaryPairDraft o2) {
                Integer a = Integer.valueOf(o1.key);
                Integer b = Integer.valueOf(o2.key);
                return a.compareTo(b);
            }
        };
        return Arrays.binarySearch(dps, 0, numPairs, new DictionaryPairDraft(t, 0), c);
    }

    /**
     * finds a leaf node based on a given key
     * @param internalNodeDraft
     * @param key
     * @return
     */
    private leafNodeDraft findLeafNode(InternalNodeDraft internalNodeDraft, int key){
        Integer[] keys = this.root.keys;
        int i;
        // iterate through until the key being searched for is less than the current value in the array traversal
        for(i = 0; i < this.root.degree; i ++){
            if(key < keys[i]){
                break;                      // break;
            }
        }

        // set child to the one of this location
        nodeDraft child = this.root.childPointers[i];
        // if it is a leaf node, return this child
        if(child instanceof leafNodeDraft) return (leafNodeDraft) child;

        // otherwise, recurse
        else return findLeafNode((InternalNodeDraft) internalNodeDraft.childPointers[i], key);
    }

    /**
     * finds the index of a pointer
     * @param pointers
     * @param leafNodeDraft
     * @return
     */
    private int findIndexOfPointer(nodeDraft[] pointers, leafNodeDraft leafNodeDraft){
        int i;
        for (i = 0; i < pointers.length; i++) {
            if (pointers[i] == leafNodeDraft) {
                break;
            }
        }
        return i;
    }

    /**
     * finds the midpoint in the node
     * @return
     */
    private int getMidPoint(){
        return (int) Math.ceil((this.m + 1) / 2.0) - 1;
    }



    public static int linearNullSearch(DictionaryPairDraft[] dps) {
        for (int i = 0; i < dps.length; i++) {
            if (dps[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public static int linearNullSearch(nodeDraft[] pointers) {
        for (int i = 0; i < pointers.length; i++) {
            if (pointers[i] == null) {
                return i;
            }
        }
        return -1;
    }



















    public static void main(String [] args){

    }



}




