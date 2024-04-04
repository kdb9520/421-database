package src;

import java.util.ArrayList;
import java.util.List;

public class Bucket {
    List<Integer> pageNumbers;
    List<Integer> indices;
    int nextPagePointer;

    public Bucket() {
        this.pageNumbers = new ArrayList<>();
        this.indices = new ArrayList<>();
        this.nextPagePointer = -1; // Initially no next page
    }
}