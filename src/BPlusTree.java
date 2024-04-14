package src;

import java.util.ArrayList;
import java.util.List;

class BPlusTree {
    private Node root;
    private int maxDegree;
    private String tableName;

    // Constructor
    public BPlusTree(int maxDegree, String tableName) {
        this.maxDegree = maxDegree;
        this.root = new Node(true, maxDegree, tableName); // Initially, root is a leaf node
        this.tableName = tableName;
    }

    // Insert method
    public void insert(Object key, Integer index) {
        root.insert(key, index);
        if (root.isOverflow()) {
            Node newRoot = new Node(false, maxDegree, tableName); // New root will be an internal node
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