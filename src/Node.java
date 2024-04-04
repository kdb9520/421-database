import java.util.ArrayList;
import java.util.List;

abstract class Node {
    List<Integer> keys;

    public Node() {
        this.keys = new ArrayList<>();
    }

    public abstract boolean isLeaf();

    public abstract void insert(int key, Object value);

    public abstract void delete(int key);

    public abstract Object search(int key);
}
