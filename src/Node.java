package src;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

 // Node class
class Node {
    private boolean isLeaf;


    private ArrayList<RecordPointer> recordPointers;
    private ArrayList<Object> keys;
    private ArrayList<Integer> indices;
    private ArrayList<Node> children;
    private int maxDegree;
    private boolean wasEdited;

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

    public Node(boolean isLeaf, ArrayList<RecordPointer> recordPointers, ArrayList<Object> keys, ArrayList<Node> children, int maxDegree, String tableName){
        this.isLeaf = isLeaf;
        this.recordPointers = recordPointers;
        this.keys = keys;
        this.children = children;
        this.maxDegree = maxDegree;
        this.tableName = tableName;
    }

    public void insert(Object key, Integer value, int pageNum, int recordIndex) {
        if (isLeaf) {
            int index = 0;
            while (index < keys.size() && key > keys.get(index)) {
                index++;
            }

            recordPointers.add(new RecordPointer(key, index));
            this.setEdited();

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

        newNode.setEdited();
        this.setEdited();
        parent.setEdited();
    }

    public void addChild(Node child) {
        children.add(child);
        wasEdited = true;
    }

    public void setEdited(){
        this.wasEdited = true;
    }

    public boolean wasEdited(){
        return this.wasEdited;
    }

    // Store nodes in following format:
    // boolean isLeaf, int numRecordPointers, recordPoint1,recordPointer2,....recordPointern
    // int numKeys, key1...n (size depends on table PK), int numChildren, child1...n (node) (call node.serialize on those) 
    public byte[] serialize(String tableName) {

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(bos);) {
        
            dataOutputStream.writeBoolean(isLeaf);
            dataOutputStream.writeInt(recordPointers.size());
            for(RecordPointer r : recordPointers){
                byte[] recordBytes = r.serialize(tableName);
                //dataOutputStream.writeInt(recordBytes.length); // Size of each record, is this needed
                dataOutputStream.write(recordBytes);
            }
            dataOutputStream.writeInt(keys.size());
            String type = Catalog.getTableSchema(tableName).getPrimaryKeyType();
                    // Now write the bytes depending on what the type is
                for(int i = 0; i < keys.size(); i++){
                    if (type.equals("integer")) {
                        dataOutputStream.writeInt((Integer) keys.get(i));
                    } else if (type.startsWith("varchar")) {
                        // Convert object to string, write how many bytes it is and write the string
                        String value = (String) keys.get(i);
                        dataOutputStream.writeInt(value.length());
                        dataOutputStream.write(value.getBytes("UTF-8"));
                    } else if (type.startsWith("char")) {
                        String value = (String) keys.get(i);
                        dataOutputStream.write(value.getBytes("UTF-8"));
                    } else if (type.equals("double")) {
                        dataOutputStream.writeDouble((Double) keys.get(i));
                    } else if (type.equals("boolean")) {
                        dataOutputStream.writeBoolean((boolean) keys.get(i));
                    }
                }
                        

            dataOutputStream.write(children.size());
            for(Node n : children){
                byte[] nodeBytes = n.serialize(tableName);
                dataOutputStream.write(nodeBytes);
            }
            
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

        // Deserialize a byte array into a record object
    public static Node deserialize(ByteBuffer buffer, String tableName, int maxDegree;) {
        String type = Catalog.getTableSchema(tableName).getPrimaryKeyType();
        boolean newIsLeafNode;
        int newNumRecordPointers;
        ArrayList<RecordPointer> newRecordPointers;
        ArrayList<Object> newKeys;
        ArrayList<Object> children;

        newIsLeafNode = buffer.get() != 0;
        newNumRecordPointers = buffer.getInt();

        for (int i = 0; i < newNumRecordPointers; i++) {
            int newPageNum = buffer.getInt();
            int newIndexNum = buffer.getInt();
            RecordPointer newRP = new RecordPointer(newPageNum, newIndexNum);
            newRecordPointers.add(newRP);
        }

        int keySize = buffer.getInt();

        for(int i = 0; i < keySize; i++){
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

        while(buffer.hasRemaining()){
            Node childNode = deserialize(buffer, tableName); // Assuming deserialize method returns a Node
            children.add(childNode);
        }

        return new Node(newIsLeafNode, newRecordPointers, newKeys, children, maxDegree, tableName);

    }
}