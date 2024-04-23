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
    //private ArrayList<Integer> indices;
    private ArrayList<Node> children;
    private int maxDegree;
    private int minKeys;
    private boolean wasEdited;
    private String tableName;
    private String type;
    private Node parent;
    private boolean isRoot;
    
    public Node(boolean isLeaf, boolean isRoot, int maxDegree, String tableName) {
        this.isLeaf = isLeaf;
        this.recordPointers = new ArrayList<>();
        this.keys = new ArrayList<>();
        //this.indices = new ArrayList<>();
        this.maxDegree = maxDegree;
        this.minKeys = maxDegree / 2;
        this.children = new ArrayList<>();
        
        this.tableName = tableName;
    }

    public Node(boolean isLeaf, boolean isRoot, ArrayList<RecordPointer> recordPointers, ArrayList<Object> keys, ArrayList<Node> children, int maxDegree, String tableName){
        this.isLeaf = isLeaf;
        this.recordPointers = recordPointers;
        this.keys = keys;
        this.children = children;
        this.maxDegree = maxDegree;
        this.minKeys = maxDegree / 2;
        this.tableName = tableName;
        // Set parent for each child node
        for (Node child : children) {
            child.setParent(this);
        }
    }

     public Node(boolean b, int maxDegree, String tableName, String type) {
        this.isLeaf = b;
        this.maxDegree = maxDegree;
        this.minKeys = maxDegree / 2;
        this.tableName = tableName;
        this.type = type;
        this.recordPointers = new ArrayList<>();
        this.keys = new ArrayList<>();
        // this.indices = new ArrayList<>();
        this.children = new ArrayList<>();
        // Set parent for each child node
        for (Node child : children) {
            child.setParent(this);
        }
     }

     public Node getParent() {
        return parent;
    }
    
    public void setParent(Node parent) {
        this.parent = parent;
    }

     public void insert(Object key, int pageNum, int recordIndex) {
        if (isLeaf) {
            int index = 0;
            while (index < keys.size() && compareObjects(key, keys.get(index)) > 0) {
                index++;
            }
            //recordPointers.add(index, new RecordPointer(pageNum, recordIndex));
            keys.add(index, key);
            if (isOverflow()) {
                handleOverflow();
            }
        } else {
            int index = 0;
            while (index < keys.size() && compareObjects(key, keys.get(index)) > 0) {
                index++;
            }
            children.get(index).insert(key, pageNum, recordIndex);

            // Explicit Parent Code 1
            // Node child = children.get(index);
            // child.insert(key, pageNum, recordIndex);

            if (children.get(index).isOverflow()) {
                children.get(index).handleOverflow();
            }

            // Explicit Parent Code 2
            // Update parent reference for the child node
            // child.setParent(this);
        }
    }

    // Given a page number and index find the record pointer and update it to new values
    // TODO: I am not sure if this should just take the primary key val of that record and do it this way
    // Or if it should take a page and index to find it. Will leave this stubbed for now
    // key should be swapped to object too
    // Returns: Success if record exists and pointer updated
    // Fail if record not found
    public boolean updateRecordPointer(int oldPageIndex, int oldIndex, int newPageIndex, int newIndex, int key){
        RecordPointer record = search(key);

        if(record != null){
            record.setPageNumber(newPageIndex);
            record.setIndexNumber(newIndex);
            return true;
        }
        return false;
    }

     /**
      * Searches a node for a value
      * @param key - the key to search for
      * @return - a RecordPointer if found, null if no node contains the search key
      */
    // TODO: Swap key to Object
    public RecordPointer search(Object key) {

        // base case - node is a leaf
        if (isLeaf) {
            int index = -1;
            for(int i = 0; i < this.keys.size(); i ++){
                // find a key in the node that equals search key
                if(this.keys.get(i).equals(key)){
                    index = i;
                }
            }
            // return corresponding Record Pointer
            if (index != -1) {
                return this.recordPointers.get(index);
            } else {
                return null;
            }
        } else {
                int index = 0;

                // iterate through the node
                while (index < children.size()) {
                    Object k = this.keys.get(index);
                    if (type.equals("integer")) {
                        if ((Integer) key < (Integer) k) {
                            break;
                        }
                    } else if (type.equals("varchar") || type.equals("char")) {
                        String s = (String) k;
                        String s1 = (String) key;
                        if (s.compareTo(s1) < 0) {
                            break;
                        }
                    } else if (type.equals("double")) {
                        if ((Double) key < (Double) k) {
                            break;
                        }
                    } else if (type.equals("boolean")) {
                        if (Boolean.compare((Boolean) key, (Boolean) k) < 0) {
                            break;
                        }

                    }


                    index++;        // internal nodes can have 5 children, but only 4 keys. Pretty sure this line is right
                                    // in order for the last possible child node to be reached. If index hits the size of the key list
                                    // then there should still be a child node at this location
                }
                return children.get(index).search(key);
            }

    }

    public boolean isOverflow() {
        return keys.size() > maxDegree - 1;
    }

    public void split(Node parent, int index) {
        Node newNode = new Node(isLeaf, false, maxDegree, tableName);
        newNode.keys.addAll(keys.subList(maxDegree / 2, keys.size()));  // copy elements over to the new node
        // newNode.indices.addAll(indices.subList(maxDegree / 2, indices.size()));
        keys.subList(maxDegree / 2, keys.size()).clear(); // clear the current node of the elements copied into new node
        // indices.subList(maxDegree / 2, indices.size()).clear();

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

    public boolean isLeaf() {
        return isLeaf;
    }

    public ArrayList<Object> getKeys() {
        return keys;
    }

    public int getNumKeys() {
        return keys.size();
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void setChildren(List<Node> newChildren) {
        this.children = new ArrayList<>(newChildren);
        for (Node child : children) {
            child.setParent(this);
        }
    }

    // returns postive if myObject is greater than otherObject
    public int compareObjects(Object myObject, Object otherObject) {

        String type = Catalog.getTableSchema(tableName).getPrimaryKeyType();

        if(type.equals("integer")){
            return (Integer) myObject - (Integer) otherObject;
        }
        else if(type.equals("varchar") || type.equals("char")){
            String myString = (String) myObject;
            String otherString = (String) otherObject;
            return myString.compareTo(otherString);
        }
        else if (type.equals("double")){
            return Double.compare((Double) myObject, (Double) otherObject);
        }
        else if(type.equals("boolean")){
            return Boolean.compare((Boolean) myObject, (Boolean) otherObject);
        }
        return 0;
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
                        

            dataOutputStream.writeInt(children.size());
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
    public static Node deserialize(ByteBuffer buffer, String tableName, int maxDegree) {
        String type = Catalog.getTableSchema(tableName).getPrimaryKeyType();
        boolean newIsLeafNode;
        int newNumRecordPointers;
        ArrayList<RecordPointer> newRecordPointers = new ArrayList<>();
        ArrayList<Object> newKeys = new ArrayList<>();
        ArrayList<Node> children = new ArrayList<>();

        newIsLeafNode = buffer.get() != 0;
        newNumRecordPointers = buffer.getInt();
        System.out.println("Num of record pointers: " + newNumRecordPointers);
        for (int i = 0; i < newNumRecordPointers; i++) {
            int newPageNum = buffer.getInt();
            int newIndexNum = buffer.getInt();
            RecordPointer newRP = new RecordPointer(newPageNum, newIndexNum);
            newRecordPointers.add(newRP);
        }

        int keySize = buffer.getInt();
        System.out.println("Key size: " + keySize);
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

        int childrenSize = buffer.getInt();
        System.out.println("Number of children in node: " + childrenSize);
        for(int i = 0; i < childrenSize; i++){
            Node childNode = deserialize(buffer, tableName, maxDegree); // Assuming deserialize method returns a Node
            children.add(childNode);
        }

        return new Node(newIsLeafNode, false, newRecordPointers, newKeys, children, maxDegree, tableName);

    }

    public int findKeyIndex(Object key) {
        for (int i = 0; i < keys.size(); i++) {
            if (compareObjects(key, keys.get(i)) == 0) {
                return i; // Return the index of the key if found
            }
        }
        return -1; // Return -1 if key is not found
    }
    
    public int findChildIndex(Object key) {
        if (isLeaf) {
            return -1; // Leaf nodes do not have children
        }
        for (int i = 0; i < keys.size(); i++) {
            if (compareObjects(key, keys.get(i)) < 0) {
                return i; // Return the index of the child node where the key should be inserted
            }
        }
        return keys.size(); // If key is greater than all keys, return index of the last child
    }

    public boolean delete(Object key) {
        if (isLeaf) {
            int index = findKeyIndex(key);
            if (index != -1) {
                keys.remove(index);
                recordPointers.remove(index);
    
                // Check for underflow and handle it if necessary
                if (keys.size() < minKeys) {
                    // Handle underflow by merging or redistributing keys
                    // Note: You need to implement this part to ensure the B+ tree properties are maintained
                    handleUnderflow();
                }
                return true; // Key found and deleted successfully
            }
            return false; // Key not found in the leaf node
        } else {
            int index = findChildIndex(key);
            if (index != -1) {
                boolean deleted = children.get(index).delete(key);
                if (deleted) {
                    // Check for underflow and handle it if necessary
                    if (children.get(index).getNumKeys() < minKeys) {
                        // Handle underflow by merging or redistributing keys
                        // Note: You need to implement this part to ensure the B+ tree properties are maintained
                        handleUnderflow();
                    }
                    return true; // Key found and deleted successfully
                }
            }
            return false; // Key not found in the internal node's children
        }
    }

    private void handleUnderflow() {
        if (isLeaf) {
            // Handle underflow for leaf node
            if (this.isRoot) {
                // If the root is a leaf node and becomes empty, simply clear the keys
                keys.clear();
                recordPointers.clear();
            } else {
                // If the leaf node is not the root, find its siblings
                Node leftSibling = findLeftSibling();
                Node rightSibling = findRightSibling();
                
                // Attempt redistribution with siblings
                if (leftSibling != null && leftSibling.getNumKeys() > minKeys) {
                    redistributeFromLeft(leftSibling);
                } else if (rightSibling != null && rightSibling.getNumKeys() > minKeys) {
                    redistributeFromRight(rightSibling);
                } else {
                    // If redistribution is not possible, merge with siblings
                    if (leftSibling != null) {
                        mergeWithLeftSibling(leftSibling);
                    } else if (rightSibling != null) {
                        mergeWithRightSibling(rightSibling);
                    }
                }
            }
        } else {
            // Handle underflow for internal node
            // This part needs to be implemented based on your B+ tree structure and properties
            // You need to consider redistributing keys or merging with siblings to maintain B+ tree properties
            // This might involve redistributing keys from parent or merging with a sibling
            // Handle underflow for internal node
            if (this.isRoot) {
                // If the root is an internal node and becomes empty, set its first child as the new root
                if (children.size() == 1) {
                    children.get(0).setRoot(true);
                    children.get(0).setParent(null);
                    this.isRoot = false;
                }
            } else {
                // If the internal node is not the root, find its siblings
                Node leftSibling = findLeftSibling();
                Node rightSibling = findRightSibling();
                
                // Attempt redistribution with siblings
                if (leftSibling != null && leftSibling.getNumKeys() > minKeys) {
                    redistributeFromLeft(leftSibling);
                } else if (rightSibling != null && rightSibling.getNumKeys() > minKeys) {
                    redistributeFromRight(rightSibling);
                } else {
                    // If redistribution is not possible, merge with siblings
                    if (leftSibling != null) {
                        mergeWithLeftSibling(leftSibling);
                    } else if (rightSibling != null) {
                        mergeWithRightSibling(rightSibling);
                    }
                }
            }
        }
    }

    public void setRoot(Boolean status) {
        this.isRoot = status;
    }

    private void redistributeFromLeft(Node leftSibling) {
        // Get the last key from the left sibling and its last child (if any)
        Object lastKeyFromLeftSibling = leftSibling.getKeys().get(leftSibling.getKeys().size() - 1);
        Node lastChildFromLeftSibling = leftSibling.getChildren().isEmpty() ? null : leftSibling.getChildren().get(leftSibling.getChildren().size() - 1);
    
        // Move the last key from the left sibling to the parent
        int parentIndex = getParent().getChildren().indexOf(this);
        getParent().getKeys().set(parentIndex - 1, lastKeyFromLeftSibling);
    
        // If the left sibling has a child, move it to the current node's children list
        if (lastChildFromLeftSibling != null) {
            children.add(0, lastChildFromLeftSibling);
            lastChildFromLeftSibling.setParent(this);
            leftSibling.getChildren().remove(leftSibling.getChildren().size() - 1);
        }
    
        // Move the first key from the left sibling to the current node
        Object firstKeyFromLeftSibling = leftSibling.getKeys().remove(leftSibling.getKeys().size() - 1);
        keys.add(0, firstKeyFromLeftSibling);
    }
    
    private void redistributeFromRight(Node rightSibling) {
        // Get the first key from the right sibling and its first child (if any)
        Object firstKeyFromRightSibling = rightSibling.getKeys().get(0);
        Node firstChildFromRightSibling = rightSibling.getChildren().isEmpty() ? null : rightSibling.getChildren().get(0);
    
        // Move the first key from the right sibling to the parent
        int parentIndex = getParent().getChildren().indexOf(this);
        getParent().getKeys().set(parentIndex, firstKeyFromRightSibling);
    
        // If the right sibling has a child, move it to the current node's children list
        if (firstChildFromRightSibling != null) {
            children.add(firstChildFromRightSibling);
            firstChildFromRightSibling.setParent(this);
            rightSibling.getChildren().remove(0);
        }
    
        // Move the last key from the right sibling to the current node
        Object lastKeyFromRightSibling = rightSibling.getKeys().remove(0);
        keys.add(lastKeyFromRightSibling);
    }

    private void mergeWithRightSibling(Node rightSibling) {
        // Move keys and children from the right sibling to the current node
        keys.add(rightSibling.getKeys().remove(0));
        keys.addAll(rightSibling.getKeys());
    
        children.addAll(rightSibling.getChildren());
    
        // Update parent keys and children
        Node parent = getParent();
        int parentIndex = parent.getChildren().indexOf(this);
        parent.getKeys().remove(parentIndex);
        parent.getChildren().remove(rightSibling);
    
        // Update child-parent relationship
        for (Node child : rightSibling.getChildren()) {
            child.setParent(this);
        }
    }
    
    private void mergeWithLeftSibling(Node leftSibling) {
        // Move keys and children from the left sibling to the current node
        keys.addAll(0, leftSibling.getKeys());
        keys.add(0, leftSibling.getKeys().remove(leftSibling.getKeys().size() - 1));
    
        children.addAll(0, leftSibling.getChildren());
    
        // Update parent keys and children
        Node parent = getParent();
        int parentIndex = parent.getChildren().indexOf(leftSibling);
        parent.getKeys().remove(parentIndex);
        parent.getChildren().remove(leftSibling);
    
        // Update child-parent relationship
        for (Node child : leftSibling.getChildren()) {
            child.setParent(this);
        }
    }

    public Node findLeftSibling() {
        Node parent = getParent();
        if (parent == null) {
            return null; // No parent, hence no left sibling
        }
        int index = parent.getChildren().indexOf(this);
        if (index > 0) {
            return parent.getChildren().get(index - 1); // Return the left sibling
        } else {
            return null; // No left sibling if the current node is the first child of the parent
        }
    }
    
    public Node findRightSibling() {
        Node parent = getParent();
        if (parent == null) {
            return null; // No parent, hence no right sibling
        }
        int index = parent.getChildren().indexOf(this);
        if (index < parent.getChildren().size() - 1) {
            return parent.getChildren().get(index + 1); // Return the right sibling
        } else {
            return null; // No right sibling if the current node is the last child of the parent
        }
    }

    private void handleOverflow() {
        if (isLeaf) {
            // Handle overflow for leaf nodes (if necessary)
            return; // Leaf nodes don't overflow in this implementation
        }
    
        // Split internal node
        
        int midIndex = keys.size() / 2; // Find the index to split the keys and children
    
        // Move keys and children to the new child node
        ArrayList<Object> newKeys = new ArrayList<>(keys.subList(midIndex + 1, keys.size()));
        ArrayList<Node> newChildren = new ArrayList<>(children.subList(midIndex + 1, children.size()));
        Node newChild = new Node(isLeaf, false, null, newKeys, newChildren, maxDegree, tableName); // Create a new internal node
        // Remove moved keys and children from the current node
        keys.subList(midIndex, keys.size()).clear();
        children.subList(midIndex + 1, children.size()).clear();
    
        // Get the median key to move up to the parent
        Object medianKey = keys.remove(midIndex);
    
        // Add the median key to the parent node
        Node parent = getParent();
        if (parent == null) {
            // Create a new root node if the current node is the root
            parent = new Node(false, true, maxDegree, tableName);
            parent.addChild(this);
            setParent(parent);
        }
    
        // Insert the median key and new child into the parent node
        parent.keys.add(keys.indexOf(medianKey), medianKey);
        parent.children.add(keys.indexOf(medianKey) + 1, newChild);
    
        // Update child-parent relationship for the new child node
        for (Node child : newChildren) {
            child.setParent(newChild);
        }
        newChild.setChildren(newChildren);
    
        // If the parent node is overflowed, handle overflow recursively
        if (parent.isOverflow()) {
            parent.handleOverflow();
        }
    }

}