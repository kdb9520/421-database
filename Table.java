import java.util.LinkedList;

public class Table {

    LinkedList pages;
    
    public Table() {
        this.pages = new LinkedList<>();
    }

    public boolean insert(Record record){
        return false;
    }

    public boolean writeToHardware(){
        return false;
    }

    private Byte[] convertToBinary() {
        return null;
    }

}
