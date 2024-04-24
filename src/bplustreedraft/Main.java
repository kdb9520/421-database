package src.bplustreedraft;

import src.BPlusTree;

public class Main {
    public static void main(String[] args) {
        bplustreedraft bpt = null;
    bpt = new bplustreedraft(5);
    bpt.insert(5, 33);
    bpt.insert(15, 21);
    bpt.insert(25, 31);
    bpt.insert(35, 41);
    bpt.insert(45, 10);
    bpt.insert(10, 10); 
    bpt.insert(12, 10); 
    bpt.delete(35);
    bpt.delete(25);

    bpt.print();

    if(1==1){
        return;
    }


    if (bpt.search(15) != null) {
      System.out.println("Found");
    } else {
      System.out.println("Not Found");
    }
    ;
  }
}
