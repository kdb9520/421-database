package src;

import java.util.ArrayList;
import java.util.List;

class LeafNode extends Node {
    List<Bucket> buckets;

    public LeafNode() {
        super();
        this.buckets = new ArrayList<>();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void insert(int key, Object value) {
        // Implement insertion logic for leaf node
    }

    @Override
    public void delete(int key) {
        // Implement deletion logic for leaf node
    }

    @Override
    public Object search(int key) {
        // Implement search logic for leaf node
        return null;
    }
}
