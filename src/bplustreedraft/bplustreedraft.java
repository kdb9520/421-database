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



    // Find the leaf node
  private leafNodeDraft findLeafNode(int key) {

    Integer[] keys = this.root.keys;
    int i;

    for (i = 0; i < this.root.degree - 1; i++) {
      if (key < keys[i]) {
        break;
      }
    }

    nodeDraft child = this.root.childPointers[i];
    if (child instanceof leafNodeDraft) {
      return (leafNodeDraft) child;
    } else {
      return findLeafNode((InternalNodeDraft) child, key);
    }
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

     // Balance the tree
  private void handleDeficiency(InternalNodeDraft in) {

    InternalNodeDraft sibling;
    InternalNodeDraft parent = in.parent;

    if (this.root == in) {
      for (int i = 0; i < in.childPointers.length; i++) {
        if (in.childPointers[i] != null) {
          if (in.childPointers[i] instanceof InternalNodeDraft) {
            this.root = (InternalNodeDraft) in.childPointers[i];
            this.root.parent = null;
          } else if (in.childPointers[i] instanceof leafNodeDraft) {
            this.root = null;
          }
        }
      }
    }

    else if (in.leftSibling != null && in.leftSibling.borrowable()) {
      sibling = in.leftSibling;
    } else if (in.rightSibling != null && in.rightSibling.borrowable()) {
      sibling = in.rightSibling;

      int borrowedKey = sibling.keys[0];
      nodeDraft pointer = sibling.childPointers[0];

      in.keys[in.degree - 1] = parent.keys[0];
      in.childPointers[in.degree] = pointer;

      parent.keys[0] = borrowedKey;

      sibling.removePointer(0);
      Arrays.sort(sibling.keys);
      sibling.removePointer(0);
      shiftDown(in.childPointers, 1);
    } else if (in.leftSibling != null && in.leftSibling.mergable()) {

    } else if (in.rightSibling != null && in.rightSibling.mergable()) {
      sibling = in.rightSibling;
      sibling.keys[sibling.degree - 1] = parent.keys[parent.degree - 2];
      Arrays.sort(sibling.keys, 0, sibling.degree);
      parent.keys[parent.degree - 2] = null;

      for (int i = 0; i < in.childPointers.length; i++) {
        if (in.childPointers[i] != null) {
          sibling.prependChildPointer(in.childPointers[i]);
          in.childPointers[i].parent = sibling;
          in.removePointer(i);
        }
      }

      parent.removePointer(in);

      sibling.leftSibling = in.leftSibling;
    }

    if (parent != null && parent.degreeToLow()) {
      handleDeficiency(parent);
    }
  }

  private boolean isEmpty() {
    return firstLeaf == null;
  }




  private void shiftDown(nodeDraft[] pointers, int amount) {
    nodeDraft[] newPointers = new nodeDraft[this.m + 1];
    for (int i = amount; i < pointers.length; i++) {
      newPointers[i - amount] = pointers[i];
    }
    pointers = newPointers;
  }

  private void sortDictionary(DictionaryPairDraft[] dictionary) {
    Arrays.sort(dictionary, new Comparator<DictionaryPairDraft>() {
      @Override
      public int compare(DictionaryPairDraft o1, DictionaryPairDraft o2) {
        if (o1 == null && o2 == null) {
          return 0;
        }
        if (o1 == null) {
          return 1;
        }
        if (o2 == null) {
          return -1;
        }
        return o1.compareTo(o2);
      }
    });
  }


  private nodeDraft[] splitChildPointers(InternalNodeDraft in, int split) {

    nodeDraft[] pointers = in.childPointers;
    nodeDraft[] halfPointers = new nodeDraft[this.m + 1];

    for (int i = split + 1; i < pointers.length; i++) {
      halfPointers[i - split - 1] = pointers[i];
      in.removePointer(i);
    }

    return halfPointers;
  }

  private DictionaryPairDraft[] splitDictionary(leafNodeDraft ln, int split) {

    DictionaryPairDraft[] dictionary = ln.dictionary;

    DictionaryPairDraft[] halfDict = new DictionaryPairDraft[this.m];

    for (int i = split; i < dictionary.length; i++) {
      halfDict[i - split] = dictionary[i];
      ln.delete(i);
    }
    return halfDict;
}

private void splitInternalNode(InternalNodeDraft in) {

    InternalNodeDraft parent = in.parent;

    int midpoint = getMidPoint();
    int newParentKey = in.keys[midpoint];
    Integer[] halfKeys = splitKeys(in.keys, midpoint);
    nodeDraft[] halfPointers = splitChildPointers(in, midpoint);

    in.degree = linearNullSearch(in.childPointers);

    InternalNodeDraft sibling = new InternalNodeDraft(this.m, halfKeys, halfPointers);
    for (nodeDraft pointer : halfPointers) {
      if (pointer != null) {
        pointer.parent = sibling;
      }
    }

    sibling.rightSibling = in.rightSibling;
    if (sibling.rightSibling != null) {
      sibling.rightSibling.leftSibling = sibling;
    }
    in.rightSibling = sibling;
    sibling.leftSibling = in;

    if (parent == null) {

      Integer[] keys = new Integer[this.m];
      keys[0] = newParentKey;
      InternalNodeDraft newRoot = new InternalNodeDraft(this.m, keys);
      newRoot.appendChildPointer(in);
      newRoot.appendChildPointer(sibling);
      this.root = newRoot;

      in.parent = newRoot;
      sibling.parent = newRoot;

    } else {

      parent.keys[parent.degree - 1] = newParentKey;
      Arrays.sort(parent.keys, 0, parent.degree);

      int pointerIndex = parent.findIndexOfPointer(in) + 1;
      parent.insertChildPointer(sibling, pointerIndex);
      sibling.parent = parent;
    }
  }

  private Integer[] splitKeys(Integer[] keys, int split) {

    Integer[] halfKeys = new Integer[this.m];

    keys[split] = null;

    for (int i = split + 1; i < keys.length; i++) {
      halfKeys[i - split - 1] = keys[i];
      keys[i] = null;
    }

    return halfKeys;
  }

  public void insert(int key, double value) {
    if (isEmpty()) {

      leafNodeDraft ln = new leafNodeDraft(this.m, new DictionaryPairDraft(key, value));

      this.firstLeaf = ln;

    } else {
        leafNodeDraft ln = (this.root == null) ? this.firstLeaf : findLeafNode(key);

      if (!ln.insert(new DictionaryPairDraft(key, value))) {

        ln.dictionary[ln.numPairs] = new DictionaryPairDraft(key, value);
        ln.numPairs++;
        sortDictionary(ln.dictionary);

        int midpoint = getMidPoint();
        DictionaryPairDraft[] halfDict = splitDictionary(ln, midpoint);

        if (ln.parent == null) {

          Integer[] parent_keys = new Integer[this.m];
          parent_keys[0] = halfDict[0].key;
          InternalNodeDraft parent = new InternalNodeDraft(this.m, parent_keys);
          ln.parent = parent;
          parent.appendChildPointer(ln);

        } else {
          int newParentKey = halfDict[0].key;
          ln.parent.keys[ln.parent.degree - 1] = newParentKey;
          Arrays.sort(ln.parent.keys, 0, ln.parent.degree);
        }

        leafNodeDraft newLeafNode = new leafNodeDraft(this.m, halfDict, ln.parent);

        int pointerIndex = ln.parent.findIndexOfPointer(ln) + 1;
        ln.parent.insertChildPointer(newLeafNode, pointerIndex);

        newLeafNode.rightSibling = ln.rightSibling;
        if (newLeafNode.rightSibling != null) {
          newLeafNode.rightSibling.leftSibling = newLeafNode;
        }
        ln.rightSibling = newLeafNode;
        newLeafNode.leftSibling = ln;

        if (this.root == null) {

          this.root = ln.parent;

        } else {
          InternalNodeDraft in = ln.parent;
          while (in != null) {
            if (in.overfilled()) {
              splitInternalNode(in);
            } else {
              break;
            }
            in = in.parent;
          }
        }
      }
    }
  }

  public Double search(int key) {

    if (isEmpty()) {
      return null;
    }

    leafNodeDraft ln = (this.root == null) ? this.firstLeaf : findLeafNode(key);

    DictionaryPairDraft[] dps = ln.dictionary;
    int index = binarySearch(dps, ln.numPairs, key);

    if (index < 0) {
      return null;
    } else {
      return dps[index].value;
    }
  }

  public ArrayList<Double> search(int lowerBound, int upperBound) {

    ArrayList<Double> values = new ArrayList<Double>();

    leafNodeDraft currNode = this.firstLeaf;
    while (currNode != null) {

      DictionaryPairDraft dps[] = currNode.dictionary;
      for (DictionaryPairDraft dp : dps) {

        if (dp == null) {
          break;
        }

        if (lowerBound <= dp.key && dp.key <= upperBound) {
          values.add(dp.value);
        }
      }
      currNode = currNode.rightSibling;

    }

    return values;
  }

  public bplustreedraft(int m) {
    this.m = m;
    this.root = null;
  }
  public void print() {
    if (isEmpty()) {
        System.out.println("Empty B+ Tree");
    } else {
        System.out.println("B+ Tree:");
        System.out.println("Root: ");
        System.out.println(root);
    }
}




}




