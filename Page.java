import java.util.ArrayList;

public class Page {
    int numRecords;
    ArrayList<Record> records;

    public Page() {
        this.numRecords = 0;
        this.records = new ArrayList<>();
    }

    public void dropAttribute(String a_name) {

    }
}
