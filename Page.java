import java.util.ArrayList;

public class Page {
    int numRecords;
    ArrayList<Record> records;
    Page nextPage;

    public Page() {
        this.numRecords = 0;
        this.records = new ArrayList<>();
    }


    public Page(int pageNumber, byte[] pageData) {
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


    public byte[] getPageData() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPageData'");
    }
}
