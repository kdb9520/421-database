package src;

import java.util.ArrayList;
import java.util.List;

 // Node class
class Node {
    private boolean isLeaf;
    private List<Integer> keys;
    private List<Object> values;
    private List<Node> children;
    private int maxDegree;

    public Node(boolean isLeaf, int maxDegree) {
        this.isLeaf = isLeaf;
        this.keys = new ArrayList<>();
        this.values = new ArrayList<>();
        this.maxDegree = maxDegree;
        if (!isLeaf) {
            this.children = new ArrayList<>();
        }
    }

    public void insert(int key, Object value) {
        if (isLeaf) {
            int index = 0;
            while (index < keys.size() && key > keys.get(index)) {
                index++;
            }
            keys.add(index, key);
            values.add(index, value);
        } else {
            int index = 0;
            while (index < keys.size() && key > keys.get(index)) {
                index++;
            }
            children.get(index).insert(key, value);
            if (children.get(index).isOverflow()) {
                children.get(index).split(this, index);
            }
        }
    }

    public Object search(int key) {
        if (isLeaf) {
            int index = keys.indexOf(key);
            if (index != -1) {
                return values.get(index);
            } else {
                return null;
            }
        } else {
            int index = 0;
            while (index < keys.size() && key > keys.get(index)) {
                index++;
            }
            return children.get(index).search(key);
        }
    }

    public boolean isOverflow() {
        return keys.size() > maxDegree - 1;
    }

    public void split(Node parent, int index) {
        Node newNode = new Node(isLeaf, maxDegree);
        newNode.keys.addAll(keys.subList(maxDegree / 2, keys.size()));
        newNode.values.addAll(values.subList(maxDegree / 2, values.size()));
        keys.subList(maxDegree / 2, keys.size()).clear();
        values.subList(maxDegree / 2, values.size()).clear();

        if (!isLeaf) {
            newNode.children.addAll(children.subList(maxDegree / 2, children.size()));
            children.subList(maxDegree / 2, children.size()).clear();
        }

        parent.keys.add(index, newNode.keys.get(0));
        parent.children.add(index + 1, newNode);
    }

    public void addChild(Node child) {
        children.add(child);
    }
}