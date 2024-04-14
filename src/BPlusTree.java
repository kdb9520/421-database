package src;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

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


    // Constructor for deserialzing an already existing BPlussTree
    public BPlusTree(String tableName, int maxDegree, Node root) {
        this.tableName = tableName;
        this.maxDegree = maxDegree;
        this.root = root;
    }

    // Insert method
    public void insert(Object key, Integer index, int pageNumber, int indexNumber) {
        root.insert(key, index, pageNumber, indexNumber);
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

        // Store nodes in following format:
    // boolean isLeaf, int numRecordPointers, recordPoint1,recordPointer2,....recordPointern
    // int numKeys, key1...n (size depends on table PK), int numChildren, child1...n (node) (call node.serialize on those) 
    public byte[] serialize(String tableName) {

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(bos);) {
        
            System.out.println(maxDegree);
            dataOutputStream.writeInt(maxDegree);
            byte[] nodeBytes = root.serialize(tableName);
            dataOutputStream.write(nodeBytes);
            dataOutputStream.close();
            return bos.toByteArray();
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
    }

    public static BPlusTree deserialize(byte[] data, String tableName) throws IOException {
        
        ByteBuffer buffer = ByteBuffer.wrap(data);


        // Read number of records
        int maxDegree = buffer.getInt();
        System.out.println(maxDegree);
        Node root = Node.deserialize(buffer, tableName, maxDegree);

        // Create and return the src.Page object
        BPlusTree tree = new BPlusTree(tableName, maxDegree, root);
        return tree;
    }

    public void printTree() {
        if (root == null) {
            System.out.println("Tree is empty.");
        } else {
            printTree(root, 0);
        }
    }

    private void printTree(Node node, int level) {
        if (node != null) {
            for (int i = 0; i < level; i++) {
                System.out.print("\t");
            }

            if (node.isLeaf()) {
                for (int i = 0; i < node.getNumKeys(); i++) {
                    System.out.print(node.getKeys().get(i) + " ");
                }
                System.out.println();
            } else {
                for (int i = 0; i < node.getNumKeys(); i++) {
                    printTree(node.getChildren().get(i), level + 1);
                    System.out.print(node.getKeys().get(i) + " ");
                    if (i == node.getNumKeys() - 1) {
                        printTree(node.getChildren().get(i + 1), level + 1);
                    }
                }
                System.out.println();
            }
        }
    }

    public String getTableName(){
        return this.tableName;
    }

}