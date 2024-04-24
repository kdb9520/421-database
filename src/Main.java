package src;

import java.util.Scanner;

//Imports

// src.Main class for the project
// Authors: Group 15

public class Main {
    public static void main(String[] args) {
        BPlusTree<Integer> tree = new BPlusTree<>(3); // Creating a B+ tree with degree 3

        // Inserting some keys
        tree.insert(10);
        tree.insert(5);
        tree.insert(15);
        tree.insert(3);
        tree.insert(7);
        tree.insert(12);
        tree.insert(20);
        tree.insert(1);
        tree.insert(2);
        tree.insert(25);

        // Printing the tree structure
        tree.printTree();
    }
}