import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Page {
    int pageNumber;
    String tableName;
    ArrayList<Record> records;


    public Page() { 
        this.records = new ArrayList<>();
    }



    public Page(String tablename, int pageNumber, ArrayList<Record> records) {
        this.tableName = tablename;
        this.pageNumber = pageNumber;
        this.records = records;

    }

    public void dropAttribute(int i) {
        for (Record record : this.records) {
            record.deleteAttribute(i);
        }
    }

    public void addAttribute(Object value) {
        for (Record record : this.records) {
            record.setAttribute(value);
        }
    }

    public Page addRecord(Record r) {
        if (this.records.size() + 1 > Main.pageSize) {
            Page newPage = splitPage();
            records.add(r);
            return newPage;
        }
        records.add(r);
        return null;
    }

    public Object getFirstRecord(int primaryKey) {
        return this.records.get(0).getAttribute(primaryKey);
    }

    public int getPageSize() {
        return this.records.size();
    }

    public Page splitPage() {

        // Get from buffer manager a new blank page
        Page newPage = new Page();

        // Cut array in half, remove second half of values, add second half of erecords
        // to new page
        int cutoffIndex = records.size() / 2 - 1;
        ArrayList<Record> page2 = new ArrayList<>(records.subList(cutoffIndex, records.size()));
        // Remove the second half of records from the current page
        records.subList(cutoffIndex, records.size()).clear();

        // Add the second half of records to the new page
        newPage.records.addAll(page2);
        return newPage;
        // Ask buffer manager to create empty page then throw values into it

    }

    // Write page to hardware, saving the changes
    public void save() {
        return;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setPageNumber(int pageNumber){
        this.pageNumber = pageNumber;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        // Write number of records
        dataOutputStream.writeInt(records.size());

        // Write each record
        for (Record record : records) {
            byte[] recordBytes = record.serialize(tableName);
            //dataOutputStream.writeInt(recordBytes.length); // Size of each record, is this needed
            dataOutputStream.write(recordBytes);
        }

        dataOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static Page deserialize(byte[] data, String tableName, int pageNumber) throws IOException {
        
        ByteBuffer buffer = ByteBuffer.wrap(data);


        // Read number of records
        int numRecords = buffer.getInt();

        // Read each record
        List<Record> records = new ArrayList<>();
        for (int i = 0; i < numRecords; i++) {;
            records.add(Record.deserialize(buffer,tableName));
        }

        // Create and return the Page object
        Page page = new Page(tableName, pageNumber, new ArrayList<Record>(records));
        return page;
    }

    public ArrayList<Record> getRecords() {
        return records;
    }

    @Override
    public String toString() {
        String pageString = "";
        for (Record record : records) {
            pageString += record.toString(this.tableName);
        }
        return pageString;
    }

}
