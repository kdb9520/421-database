package src;

import java.util.ArrayList;
import java.util.List;

public class InternalNode extends Node {
    List<Node> pointers;

    public InternalNode() {
        super();
        this.pointers = new ArrayList<>();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public void insert(int key, Object value) {
        // Implement insertion logic for internal node
    }

    @Override
    public void delete(int key) {
        // Implement deletion logic for internal node
    }

    @Override
    public Object search(int key) {
        // Implement search logic for internal node
        return null;
    }
}
