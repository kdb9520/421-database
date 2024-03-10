package src;

public class Pair<T, U> {
    private Object first;
    private Object second;

    public Pair(Object first, Object second) {
        this.first = first;
        this.second = second;
    }

    public Object getFirst() {
        return first;
    }

    public Object getSecond() {
        return second;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(U second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    public static void main(String[] args) {
        // Creating a Pair of objects
        Pair<String, Integer> pair = new Pair<>("Hello", 42);

        // Accessing values
        System.out.println("First: " + pair.getFirst());
        System.out.println("Second: " + pair.getSecond());

        // Modifying values
        pair.setFirst("World");
        pair.setSecond(99);

        // Printing the modified pair
        System.out.println("Modified Pair: " + pair);
    }
}
