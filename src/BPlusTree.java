package src;

import java.util.ArrayList;
import java.util.List;

class BPlusTree {
    private Node root;
    private int maxDegree;

    // Constructor
    public BPlusTree(int maxDegree) {
        this.maxDegree = maxDegree;
        this.root = new Node(true, maxDegree); // Initially, root is a leaf node
    }

    // Insert method
    public void insert(int key, Object value) {
        root.insert(key, value);
        if (root.isOverflow()) {
            Node newRoot = new Node(false, maxDegree); // New root will be an internal node
            newRoot.addChild(root);
            root.split(newRoot, 0);
            root = newRoot;
        }
    }

    // Search method
    public Object search(int key) {
        return root.search(key);
    }

    // Delete method
    public void delete(int key) {
        // Implement deletion logic here
    }

}