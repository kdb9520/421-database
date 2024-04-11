package src;

public class RecordPointer {
    int pageNumber;
    int indexNumber;



    public RecordPointer(int p, int i){
        this.pageNumber = p;
        this.indexNumber = i;

    }


    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getIndexNumber() {
        return indexNumber;
    }

    public void setIndexNumber(int indexNumber) {
        this.indexNumber = indexNumber;
    }
}
