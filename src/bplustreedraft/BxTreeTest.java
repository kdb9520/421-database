package src.bplustreedraft;

import static org.junit.Assert.*;

import org.junit.Test;

public class BxTreeTest {
    
    @Test
    public void testEmptyTree() {
        BxTree<Integer, String> tree = new BxTree<>(3);
        assertNull(tree.find(5));
    }

    @Test
    public void testInsertAndFindSingleValue() {
        BxTree<Integer, String> tree = new BxTree<>(3);
        tree.insert(5, "apple");
        assertEquals("apple", tree.find(5));
    }

    @Test
    public void testInsertAndFindMultipleValues() {
        BxTree<Integer, String> tree = new BxTree<>(3);
        tree.insert(5, "apple");
        tree.insert(5, "banana");
        tree.insert(5, "orange");
        assertEquals("apple", tree.find(5));
    }

    @Test
    public void testInsertAndFindAcrossMultipleNodes() {
        BxTree<Integer, String> tree = new BxTree<>(3);
        tree.insert(5, "apple");
        tree.insert(10, "banana");
        tree.insert(15, "orange");
        assertEquals("apple", tree.find(5));
        assertEquals("banana", tree.find(10));
        assertEquals("orange", tree.find(15));
    }

    @Test
    public void testInsertAndFindWithDifferentKeys() {
        BxTree<Integer, String> tree = new BxTree<>(3);
        tree.insert(5, "apple");
        tree.insert(10, "banana");
        assertEquals("apple", tree.find(5));
        assertEquals("banana", tree.find(10));
        assertNull(tree.find(15));
    }

    @Test
    public void testInsertAndFindWithLargeNumberOfValues() {
        BxTree<Integer, String> tree = new BxTree<>(3);
        for (int i = 0; i < 1000; i++) {
            tree.insert(i, "value_" + i);
        }
        for (int i = 0; i < 1000; i++) {
            assertEquals("value_" + i, tree.find(i));
        }
    }

    @Test
    public void testInsertAndFindWithDuplicateKeys() {
        BxTree<Integer, String> tree = new BxTree<>(3);
        tree.insert(5, "apple");
        tree.insert(5, "banana");
        assertEquals("banana", tree.find(5));
    }

    @Test
    public void testInsertAndFindWithMaximumNodeCapacity() {
        BxTree<Integer, String> tree = new BxTree<>(3);
        // Insert 3 key-value pairs to fill up a leaf node
        tree.insert(1, "apple");
        tree.insert(2, "banana");
        tree.insert(3, "orange");
        // Insert one more pair to cause a leaf split
        tree.insert(4, "grape");
        assertEquals("apple", tree.find(1));
        assertEquals("banana", tree.find(2));
        assertEquals("orange", tree.find(3));
        assertEquals("grape", tree.find(4));
    }

    @Test
    public void testInsertAndFindWithDifferentDataTypes() {
        BxTree<String, Integer> tree = new BxTree<>(3);
        tree.insert("apple", 5);
        tree.insert("banana", 10);
        assertEquals(Integer.valueOf(5), tree.find("apple"));
        assertEquals(Integer.valueOf(10), tree.find("banana"));
    }

    @Test
    public void testInsertAndFindWithNullValues() {
        BxTree<Integer, String> tree = new BxTree<>(3);
        tree.insert(1, null);
        assertNull(tree.find(1));
    }
}
