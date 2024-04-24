package src;
import java.util.ArrayList;
import java.util.List;

class BPlusTree<T extends Comparable<T>> {
    private Node<T> root;
    private final int degree;

    public BPlusTree(int degree) {
        this.degree = degree;
        this.root = new LeafNode<>();
    }

    public void insert(T key) {
        root.insert(key);
        if (root.isOverflow()) {
            Node<T> newRoot = new InternalNode<>();
            newRoot.addChild(root);
            root.split(newRoot, 0);
            root = newRoot;
        }
    }

    public void printTree() {
        root.print();
    }

    private interface Node<T extends Comparable<T>> {
        void insert(T key);
        boolean isOverflow();
        void split(Node<T> parent, int index);
        void addChild(Node<T> child);
        void print();
    }

    private class InternalNode<T extends Comparable<T>> implements Node<T> {
        private final List<T> keys;
        private final List<Node<T>> children;

        public InternalNode() {
            this.keys = new ArrayList<>();
            this.children = new ArrayList<>();
        }

        @Override
        public void insert(T key) {
            int index = 0;
            while (index < keys.size() && key.compareTo(keys.get(index)) >= 0) {
                index++;
            }
            children.get(index).insert(key);
            if (children.get(index).isOverflow()) {
                children.get(index).split(this, index);
            }
        }

        @Override
        public boolean isOverflow() {
            return keys.size() >= degree;
        }

        @Override
        public void split(Node<T> parent, int index) {
            InternalNode<T> newNode = new InternalNode<>();
        
            // Determine the midpoint index
            int midIndex = (keys.size() - 1) / 2;
        
            // Move keys and children to the new child node
            newNode.keys.addAll(keys.subList(midIndex + 1, keys.size()));
            newNode.children.addAll(children.subList(midIndex + 1, children.size()));
        
            // Clear the keys and children from the current node
            keys.subList(midIndex, keys.size()).clear();
            children.subList(midIndex + 1, children.size()).clear();
        
            // Get the median key to move up to the parent
            T medianKey = keys.remove(midIndex);
        
            // Add the median key to the parent node
            parent.addChild(newNode);
            ((InternalNode<T>) parent).keys.add(index, medianKey);
        }

        @Override
        public void addChild(Node<T> child) {
            children.add(child);
        }

        @Override
        public void print() {
            for (int i = 0; i < children.size(); i++) {
                children.get(i).print();
                if (i < keys.size()) {
                    System.out.print(keys.get(i) + " ");
                }
            }
            System.out.println();
        }
    }

    private class LeafNode<T extends Comparable<T>> implements Node<T> {
        private final List<T> keys;

        public LeafNode() {
            this.keys = new ArrayList<>();
        }

        @Override
        public void insert(T key) {
            keys.add(key);
            keys.sort(Comparable::compareTo);
        }

        @Override
        public boolean isOverflow() {
            return keys.size() >= degree;
        }

        @Override
        public void split(Node<T> parent, int index) {
            LeafNode<T> newNode = new LeafNode<>();
            newNode.keys.addAll(keys.subList(degree / 2, keys.size()));
            keys.subList(degree / 2, keys.size()).clear();

            parent.addChild(newNode);
            T medianKey = newNode.keys.get(0);
            ((InternalNode<T>) parent).keys.add(index, medianKey);
        }

        @Override
        public void addChild(Node<T> child) {
            throw new UnsupportedOperationException("Leaf nodes cannot have children.");
        }

        @Override
        public void print() {
            for (T key : keys) {
                System.out.print(key + " ");
            }
        }
    }
}