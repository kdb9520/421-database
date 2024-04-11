package src;

import java.util.ArrayList;
import java.util.List;

 // Node class
class Node {
    private boolean isLeaf;


    private List<RecordPointer> recordPointers;
    private List<Integer> keys;
    private List<Integer> indices;
    private List<Node> children;
    private int maxDegree;

    public Node(boolean isLeaf, int maxDegree) {
        this.isLeaf = isLeaf;
        this.recordPointers = new ArrayList<>();
        this.keys = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.maxDegree = maxDegree;
        if (!isLeaf) {
            this.children = new ArrayList<>();
        }
    }

    public void insert(int key, Integer value) {
        if (isLeaf) {
            int index = 0;
            while (index < keys.size() && key > keys.get(index)) {
                index++;
            }

            recordPointers.add(new RecordPointer(key, index));

        } else {
            int index = 0;
            while (index < recordPointers.size() && key > recordPointers.get(index).getPageNumber()) {
                index++;
            }
            children.get(index).insert(key, value);
            if (children.get(index).isOverflow()) {
                children.get(index).split(this, index);
            }
        }
    }

    public RecordPointer search(int key) {
        if (isLeaf) {
            int index = -1;
            int c = 0;
            for(RecordPointer rp : this.recordPointers){
                if(rp.getPageNumber() == key){
                    index  = c;
                    break;
                }
                c ++;
            }
            if (index != -1) {
                return this.recordPointers.get(index);
            } else {
                return null;
            }
        } else {
            int index = 0;
            while (index < recordPointers.size() && key > recordPointers.get(index).getPageNumber()) {
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
        newNode.indices.addAll(indices.subList(maxDegree / 2, indices.size()));
        keys.subList(maxDegree / 2, keys.size()).clear();
        indices.subList(maxDegree / 2, indices.size()).clear();

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