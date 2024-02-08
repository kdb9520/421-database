import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Page {
    public static final int PAGE_SIZE = 8;
    int numRecords;
    int pageNumber;
    ArrayList<Record> records;
    Page nextPage;

    public Page() {
        this.numRecords = 0;
        this.records = new ArrayList<>();
    }


    public Page(int pageNumber, byte[] pageData) {
        //TODO Auto-generated constructor stub
        
    }

    public Page(int pageNumber, ArrayList<Record> records) {
        //TODO Auto-generated constructor stub
        
    }

    public void dropAttribute(String a_name) {

    }

    public boolean addRecord(Record r){
        records.add(r);
        return true;
    }

    public int getPageSize(){
        return numRecords;
    }

    public Page splitPage(){

        // Get from buffer manager a new blank page
        Page newPage = new Page();

        // Cut array in half, remove second half of values, add second half of erecords to new page
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
    public void save(){
        return;
    }


    public int getPageNumber() {
        return this.pageNumber;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        // Write page number
        dataOutputStream.writeInt(pageNumber);

        // Write number of records
        dataOutputStream.writeInt(records.size());

        // Write each record
        for (Record record : records) {
            byte[] recordBytes = record.serialize();
            dataOutputStream.writeInt(recordBytes.length);
            dataOutputStream.write(recordBytes);
        }

        dataOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static Page deserialize(byte[] data) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        // Read page number
        int pageNumber = dataInputStream.readInt();

        // Read number of records
        int numRecords = dataInputStream.readInt();

        // Read each record
        List<Record> records = new ArrayList<>();
        for (int i = 0; i < numRecords; i++) {
            int recordLength = dataInputStream.readInt();
            byte[] recordBytes = new byte[recordLength];
            dataInputStream.readFully(recordBytes);
            records.add(Record.deserialize(recordBytes));
        }

        dataInputStream.close();

        // Create and return the Page object
        Page page = new Page(pageNumber,new ArrayList<Record>(records) );
        return page;
    }
}
