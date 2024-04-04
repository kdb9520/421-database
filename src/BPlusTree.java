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
    public void search(int key) {
        // Implement search logic here
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