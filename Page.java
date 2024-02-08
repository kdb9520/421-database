import java.util.ArrayList;

public class Page {
    int numRecords;
    ArrayList<Record> records;
    nextPage = Page;

    public Page() {
        this.numRecords = 0;
        this.records = new ArrayList<>();
    }

    public void dropAttribute(String a_name) {

    }

    public ArrayList<Record> splitPage{
        // Cut array in half, remove second half of values, return to Table class those values
        int cutoffIndex = record.size() / 2 - 1;
        ArrayList<Record> page2 = records.sublist(cutOffIndex,records.size()-1);
        records = records.sublist(0, cutoffIndex);
        return page2;
    }
}
