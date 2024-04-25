package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import src.*;

public class BxTreeDeleteTest {

    private BxTree<Integer, String> tree;

    @BeforeEach
    public void setUp() {
        tree = new BxTree<>(3, 3); // Creating a BxTree with M=N=3
        tree.insert(1, "One");
        tree.insert(2, "Two");
        tree.insert(3, "Three");
        tree.insert(4, "Four");
        tree.insert(5, "Five");
        tree.insert(6, "Six");
    }

    @Test
    public void testDeleteLeafKey() {
        assertEquals("Three", tree.delete(3)); // Delete key 3
        assertNull(tree.find(3)); // Key 3 should not be found after deletion
        assertEquals("Four", tree.find(4)); // Key 4 should still be in the tree
        assertEquals("Five", tree.find(5)); // Key 5 should still be in the tree
    }

    @Test
    public void testDeleteInnerNodeKey() {
        assertEquals("Two", tree.delete(2)); // Delete key 2
        assertNull(tree.find(2)); // Key 2 should not be found after deletion
        assertEquals("One", tree.find(1)); // Key 1 should still be in the tree
        assertEquals("Three", tree.find(3)); // Key 3 should still be in the tree
    }

    @Test
    public void testDeleteNonExistentKey() {
        assertNull(tree.delete(7)); // Trying to delete a key that doesn't exist
    }

    @Test
    public void testDeleteFromEmptyTree() {
        BxTree<Integer, String> emptyTree = new BxTree<>(3, 3);
        assertNull(emptyTree.delete(1)); // Trying to delete from an empty tree
    }
}

