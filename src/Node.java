package src;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

 // Node class
class Node {
    private boolean isLeaf;


    private List<RecordPointer> recordPointers;
    private List<Object> keys;
    private List<Integer> indices;
    private List<Node> children;
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

    public void insert(int key, Integer value) {
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
}