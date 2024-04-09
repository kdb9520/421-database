package src;

import java.util.ArrayList;
import java.util.List;

class BPlusTree {
    private Node root;
    private int maxPageSize;
    private int maxKeySize;
    private int maxPointerSize;
    private int n;

    // Constructor
    public BPlusTree(int maxPageSize, int maxKeySize, int maxPointerSize) {
        this.maxPageSize = maxPageSize;
        this.maxKeySize = maxKeySize;
        this.maxPointerSize = maxPointerSize;
        this.n = (int) (Math.floor(maxPageSize / (maxKeySize + maxPointerSize)) - 1);
        this.root = new LeafNode();
    }

    // Search method
    public Node search(int key) {
        Node C = root;




        //Traverse the tree until we reach a leaf node
        while(!(C instanceof LeafNode)){
            int i = 0;
            while (i < C.keys.size()){
                if(key < C.keys.get(i)){
                    break;
                }
                i +=1;
            }
            // need child node functionality
            //C = node.children[i];


            i = 0;

            // Now 'node' points to a leaf node, search for the key within this node
            while(i < C.keys.size()){
                if (key == C.keys.get(i)){
                    // return C.values[i];

                }
                else if (key < C.keys.size()) {
                    break;
                }

                i += 1;
            }


        }

        return null;
    }

    // Insert method
    public void insert(int key, Object value) {
        // Implement insertion logic here
    }

    // Delete method
    public void delete(int key) {
        // Implement deletion logic here
    }
    
}