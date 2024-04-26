package src;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class BxTree<Key extends Comparable<? super Key>, Value> {
    /**
     * Pointer to the root node. It may be a leaf or an inner node, but it is never
     * null.
     */
    private Node root;
    /**
     * the maximum number of keys in the leaf node, M must be > 0
     */
    private final int M;
    /**
     * the maximum number of keys in inner node, the number of pointer is N+1, N
     * must be > 2
     */
    private final int N;

    private String name;

    /**
     * Create a new empty tree.
     */
    public BxTree(int n) {
        this(n, n);
    }

    public BxTree(int m, int n) {
        M = m;
        N = n;
        root = new LNode();
    }

    /**
     * Create a new empty tree.
     */
    public BxTree(int n, Node root) {
        this.M = n;
        this.N = n;
        this.root = root;
    }

    public void insert(Key key, Value value) {
        System.out.println("insert key=" + key);
        Split result = root.insert(key, value);
        if (result != null) {
            // The old root was split into two parts.
            // We have to create a new root pointing to them
            INode _root = new INode();
            _root.num = 1;
            _root.keys[0] = result.key;
            _root.children[0] = result.left;
            _root.children[1] = result.right;
            root = _root;
        }
    }

    /**
     * Delete a key from the BxTree.
     *
     * @param key The key to delete.
     * @return The value associated with the deleted key, or null if the key is not found.
     */
    public Value delete(Key key) {
        Node node = root;
        while (node instanceof BxTree.INode) {
            INode inner = (INode) node;
            int idx = inner.getLoc(key);
            node = inner.children[idx];
        }

        LNode leaf = (LNode) node;
        int idx = leaf.getLoc(key);
        if (idx < leaf.num && leaf.keys[idx].equals(key)) {
            Value deletedValue = leaf.values[idx];
            leaf.num--;
            System.arraycopy(leaf.keys, idx + 1, leaf.keys, idx, leaf.num - idx);
            System.arraycopy(leaf.values, idx + 1, leaf.values, idx, leaf.num - idx);
            return deletedValue;
        } else {
            return null;
        }
    }

    /**
     * Looks for the given key. If it is not found, it returns null.
     * If it is found, it returns the associated value.
     */
    public RecordPointer find(Key key) {
        Node node = root;
        while (node instanceof BxTree.INode) { // need to traverse down to the leaf
            INode inner = (INode) node;
            int idx = inner.getLoc(key);
            node = inner.children[idx];
        }

        // We are @ leaf after while loop
        LNode leaf = (LNode) node;
        int idx = leaf.getLoc(key);
        if (idx < leaf.num && leaf.keys[idx].equals(key)) {
            return (RecordPointer) leaf.values[idx];
        } else {
            return null;
        }
    }

    /**
     * Looks for the given key. If it is not found, it returns null.
     * If it is found, it returns the associated value.
     */
    public void update(Key key, RecordPointer rp) {
        Node node = root;
        while (node instanceof BxTree.INode) { // need to traverse down to the leaf
            INode inner = (INode) node;
            int idx = inner.getLoc(key);
            node = inner.children[idx];
        }

        // We are @ leaf after while loop
        LNode leaf = (LNode) node;
        int idx = leaf.getLoc(key);
        if (idx < leaf.num && leaf.keys[idx].equals(key)) {
            leaf.values[idx] = (Value) rp;
        } else {
            System.out.println("Test Error: Record Not Found");
        }
    }

    public void dump() {
        root.dump();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void printTree() {
        root.print("");
    }

    public void print() {
        System.out.println("Root:");
        root.print("");
    }

    abstract class Node {
        protected int num; // number of keys
        protected Key[] keys;

        abstract public int getLoc(Key key);

        // returns null if no split, otherwise returns split info
        abstract public Split insert(Key key, Value value);

        abstract public void dump();

        abstract public void print(String indent);

        abstract public byte[] serialize(String tableName);

        abstract public Node deserialize(ByteBuffer buffer, String tableName, int N);
    }

    class LNode extends Node {
        // In some sense, the following casts are almost always illegal
        // (if Value was replaced with a real type other than Object,
        // the cast would fail); but they make our code simpler
        // by allowing us to pretend we have arrays of certain types.
        // They work because type erasure will erase the type variables.
        // It will break if we return it and other people try to use it.
        final Value[] values = (Value[]) new Object[M];

        {
            keys = (Key[]) new Comparable[M];
        }

        // Deserialize a byte array into a record object
        public Node deserialize(ByteBuffer buffer, String tableName, int N) {
            String type = Catalog.getTableSchema(tableName).getPrimaryKeyType();
            ArrayList<Object> newKeys = new ArrayList<>();
            ArrayList<Value> newRecordPointers = new ArrayList<>();


            int keySize = buffer.getInt();
            System.out.println("Number of Keys: " + keySize);
            for (int i = 0; i < keySize; i++) {
                if (type.equals("integer")) {
                    Integer attr = buffer.getInt();
                    newKeys.add(attr);
                } else if (type.startsWith("varchar")) {
                    // Get length of the varchar
                    int length = buffer.getInt();
                    // Read the varchar in
                    byte[] stringBytes = new byte[length];
                    buffer.get(stringBytes);
                    // Make it a string
                    String attr = new String(stringBytes);
                    newKeys.add(attr);
                } else if (type.startsWith("char")) {
                    // Get the size of char
                    int numberOfChars = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));
                    byte[] stringBytes = new byte[numberOfChars];
                    buffer.get(stringBytes);
                    // Get the string
                    String attr = new String(stringBytes);
                    newKeys.add(attr);
                } else if (type.equals("double")) {
                    Double attr = buffer.getDouble();
                    newKeys.add(attr);
                } else if (type.equals("boolean")) {
                    boolean attr = buffer.get() != 0;
                    newKeys.add(attr);
                }
            }

            int newNumRecordPointers = buffer.getInt();
            System.out.println("Num of record pointers: " + newNumRecordPointers);
            for (int i = 0; i < keySize; i++) {
                int newPageNum = buffer.getInt();
                int newIndexNum = buffer.getInt();
                RecordPointer newRP = new RecordPointer(newPageNum, newIndexNum);
                newRecordPointers.add((Value) newRP);
            }

            for (int i = 0; i < newNumRecordPointers; i++) {
                if (i < keySize) {
                    keys[i] = keys[i];
                    values[i] = values[i];
                }
            }

            return this;

        }

        // Store nodes in following format:
        // boolean isLeaf, int numRecordPointers, recordPoint1,recordPointer2,....recordPointern
        // int numKeys, key1...n (size depends on table PK), int numChildren, child1...n (node) (call node.serialize on those)
        public byte[] serialize(String tableName) {

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 DataOutputStream dataOutputStream = new DataOutputStream(bos);) {

                // Write number of keys
                dataOutputStream.writeInt(num);

                // Get type of the primary key
                String type = Catalog.getTableSchema(tableName).getPrimaryKeyType();
                // Now write the bytes depending on what the type is
                for (int i = 0; i < num; i++) {
                    if (type.startsWith("integer")) {
                        dataOutputStream.writeInt((Integer) keys[i]);
                    } else if (type.startsWith("varchar")) {
                        // Convert object to string, write how many bytes it is and write the string
                        String value = (String) keys[i];
                        dataOutputStream.writeInt(value.length());
                        dataOutputStream.write(value.getBytes("UTF-8"));
                    } else if (type.startsWith("char")) {
                        String value = (String) keys[i];
                        dataOutputStream.write(value.getBytes("UTF-8"));
                    } else if (type.equals("double")) {
                        dataOutputStream.writeDouble((Double) keys[i]);
                    } else if (type.equals("boolean")) {
                        dataOutputStream.writeBoolean((Boolean) keys[i]);
                    }
                }

                // Write number of values (Record Pointers)
                dataOutputStream.writeInt(values.length);

                //Now let's do it for values
                for (int i = 0; i < values.length; i++) {
                    if (values[i] != null) {
                        RecordPointer rec = (RecordPointer) values[i];

                        dataOutputStream.write(rec.serialize());
                    }
                }

                return bos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Returns the position where 'key' should be inserted in a leaf node
         * that has the given keys.
         */
        public int getLoc(Key key) {
            // Simple linear search. Faster for small values of N or M, binary search would
            // be faster for larger M / N
            for (int i = 0; i < num; i++) {
                if (keys[i].compareTo(key) >= 0) {
                    return i;
                }
            }
            return num;
        }

        public void print(String indent) {
            System.out.println(indent + "Leaf Node:");
            for (int i = 0; i < num; i++) {
                System.out.println(indent + "  " + keys[i] + " : " + values[i]);
            }
        }

        public Split insert(Key key, Value value) {
            // Simple linear search
            int i = getLoc(key);
            if (this.num == M) { // The node was full. We must split it
                int mid = (M + 1) / 2;
                int sNum = this.num - mid;
                LNode sibling = new LNode();
                sibling.num = sNum;
                System.arraycopy(this.keys, mid, sibling.keys, 0, sNum);
                System.arraycopy(this.values, mid, sibling.values, 0, sNum);
                this.num = mid;
                if (i < mid) {
                    // Inserted element goes to left sibling
                    this.insertNonfull(key, value, i);
                } else {
                    // Inserted element goes to right sibling
                    sibling.insertNonfull(key, value, i - mid);
                }
                // Notify the parent about the split
                Split result = new Split(sibling.keys[0], // make the right's key >= result.key
                        this,
                        sibling);
                return result;
            } else {
                // The node was not full
                this.insertNonfull(key, value, i);
                return null;
            }
        }

        private void insertNonfull(Key key, Value value, int idx) {
            // if (idx < M && keys[idx].equals(key)) {
            if (idx < num && keys[idx].equals(key)) {
                // We are inserting a duplicate value, simply overwrite the old one
                values[idx] = value;
            } else {
                // The key we are inserting is unique
                System.arraycopy(keys, idx, keys, idx + 1, num - idx);
                System.arraycopy(values, idx, values, idx + 1, num - idx);

                keys[idx] = key;
                values[idx] = value;
                num++;
            }
        }

        public void dump() {
            System.out.println("lNode h==0");
            for (int i = 0; i < num; i++) {
                System.out.println(keys[i]);
            }
        }
    }

    class INode extends Node {
        final Node[] children = new BxTree.Node[N + 1];

        {
            keys = (Key[]) new Comparable[N];
        }

        // Deserialize a byte array into a record object
        public Node deserialize(ByteBuffer buffer, String tableName, int N) {
            String type = Catalog.getTableSchema(tableName).getPrimaryKeyType();
            ArrayList<Object> newKeys = new ArrayList<>();
            ArrayList<Node> newChildren = new ArrayList<>();

            int keySize = buffer.getInt();
            System.out.println("Key size: " + keySize);
            for (int i = 0; i < keySize; i++) {
                if (type.equals("integer")) {
                    Integer attr = buffer.getInt();
                    newKeys.add(attr);
                } else if (type.startsWith("varchar")) {
                    // Get length of the varchar
                    int length = buffer.getInt();
                    // Read the varchar in
                    byte[] stringBytes = new byte[length];
                    buffer.get(stringBytes);
                    // Make it a string
                    String attr = new String(stringBytes);
                    newKeys.add(attr);
                } else if (type.startsWith("char")) {
                    // Get the size of char
                    int numberOfChars = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));
                    byte[] stringBytes = new byte[numberOfChars];
                    buffer.get(stringBytes);
                    // Get the string
                    String attr = new String(stringBytes);
                    newKeys.add(attr);
                } else if (type.equals("double")) {
                    Double attr = buffer.getDouble();
                    newKeys.add(attr);
                } else if (type.equals("boolean")) {
                    boolean attr = buffer.get() != 0;
                    newKeys.add(attr);
                }
            }

            int childrenSize = buffer.getInt();
            System.out.println("Number of children in node: " + childrenSize);
            for (int i = 0; i < childrenSize; i++) {
                Node childNode = deserialize(buffer, tableName, N); // Assuming deserialize method returns a Node
                newChildren.add(childNode);
            }

            return null; // Here return a new node putting this into constructor

        }

        public byte[] serialize(String tableName) {

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 DataOutputStream dataOutputStream = new DataOutputStream(bos);) {

                // Write number of keys
                dataOutputStream.writeInt(num);

                // Get type of the primary key
                String type = Catalog.getTableSchema(tableName).getPrimaryKeyType();
                // Now write the bytes depending on what the type is
                for (int i = 0; i < num; i++) {
                    if (type.startsWith("integer")) {
                        dataOutputStream.writeInt((Integer) keys[i]);
                    } else if (type.startsWith("varchar")) {
                        // Convert object to string, write how many bytes it is and write the string
                        String value = (String) keys[i];
                        dataOutputStream.writeInt(value.length());
                        dataOutputStream.write(value.getBytes("UTF-8"));
                    } else if (type.startsWith("char")) {
                        String value = (String) keys[i];
                        dataOutputStream.write(value.getBytes("UTF-8"));
                    } else if (type.equals("double")) {
                        dataOutputStream.writeDouble((Double) keys[i]);
                    } else if (type.equals("boolean")) {
                        dataOutputStream.writeBoolean((Boolean) keys[i]);
                    }
                }

                // Write number of children (Nodes)
                dataOutputStream.writeInt(children.length);

                //Now lets do it for values
                for (Node n : children) {
                    byte[] nodeBytes = n.serialize(tableName);
                    //dataOutputStream.writeInt(recordBytes.length); // Size of each record, is this needed
                    dataOutputStream.write(nodeBytes);
                }

                return bos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }


        /**
         * Returns the position where 'key' should be inserted in an inner node
         * that has the given keys.
         */
        public int getLoc(Key key) {
            // Simple linear search. Faster for small values of N or M
            for (int i = 0; i < num; i++) {
                if (keys[i].compareTo(key) > 0) {
                    return i;
                }
            }
            return num;
            // Binary search is faster when N or M is big,
        }

        public void print(String indent) {
            System.out.println(indent + "Inner Node:");
            for (int i = 0; i < num; i++) {
                children[i].print(indent + "  ");
                System.out.println(indent + "  " + keys[i]);
            }
            children[num].print(indent + "  ");
        }

        public Split insert(Key key, Value value) {
            /*
             * Early split if node is full.
             * This is not the canonical algorithm for B+ trees,
             * but it is simpler and it does break the definition
             * which might result in immature split, which might not be desired in database
             * because additional split lead to tree's height increase by 1, thus the number
             * of disk read
             * so first search to the leaf, and split from bottom up is the correct
             * approach.
             */

            if (this.num == N) { // Split
                int mid = (N + 1) / 2;
                int sNum = this.num - mid;
                INode sibling = new INode();
                sibling.num = sNum;
                System.arraycopy(this.keys, mid, sibling.keys, 0, sNum);
                System.arraycopy(this.children, mid, sibling.children, 0, sNum + 1);

                this.num = mid - 1;// this is important, so the middle one elevate to next depth(height), inner
                // node's key don't repeat itself

                // Set up the return variable
                Split result = new Split(this.keys[mid - 1],
                        this,
                        sibling);

                // Now insert in the appropriate sibling
                if (key.compareTo(result.key) < 0) {
                    this.insertNonfull(key, value);
                } else {
                    sibling.insertNonfull(key, value);
                }
                return result;

            } else {// No split
                this.insertNonfull(key, value);
                return null;
            }
        }

        private void insertNonfull(Key key, Value value) {
            // Simple linear search
            int idx = getLoc(key);
            Split result = children[idx].insert(key, value);

            if (result != null) {
                if (idx == num) {
                    // Insertion at the rightmost key
                    keys[idx] = result.key;
                    children[idx] = result.left;
                    children[idx + 1] = result.right;
                    num++;
                } else {
                    // Insertion not at the rightmost key
                    // shift i>idx to the right
                    System.arraycopy(keys, idx, keys, idx + 1, num - idx);
                    System.arraycopy(children, idx, children, idx + 1, num - idx + 1);

                    children[idx] = result.left;
                    children[idx + 1] = result.right;
                    keys[idx] = result.key;
                    num++;
                }
            } // else the current node is not affected
        }

        /**
         * This one only dump integer key
         */
        public void dump() {
            System.out.println("iNode h==?");
            for (int i = 0; i < num; i++) {
                children[i].dump();
                System.out.print('>');
                System.out.println(keys[i]);
            }
            children[num].dump();
        }
    }

    class Split {
        public final Key key;
        public final Node left;
        public final Node right;

        public Split(Key k, Node l, Node r) {
            key = k;
            left = l;
            right = r;
        }
    }


    // Store nodes in following format:
    // boolean isLeaf, int numRecordPointers, recordPoint1,recordPointer2,....recordPointer
    // int numKeys, key1...n (size depends on table PK), int numChildren, child1...n (node) (call node.serialize on those) 
    public byte[] serialize(String tableName) {

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(bos);) {

            dataOutputStream.writeInt(N);
            byte[] nodeBytes = root.serialize(tableName);
            dataOutputStream.write(nodeBytes);
            dataOutputStream.close();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BxTree deserialize(byte[] data, String tableName) throws IOException {

        ByteBuffer buffer = ByteBuffer.wrap(data);


        // Read number of records
        int N = buffer.getInt();
        BxTree tree = new BxTree(N);
        tree.setName(tableName);

        tree.root = tree.root.deserialize(buffer, tableName, N);

        return tree;
    }

}