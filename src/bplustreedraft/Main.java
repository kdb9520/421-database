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
    bpt.insert(55, 10); 
    bpt.insert(65, 10); 
    bpt.insert(75, 10); 
    bpt.delete(5);
    bpt.print();
    if(1==1){
        return;
    }
    bpt.insert(10, 10); 
    bpt.insert(11, 10); 
    
    bpt.delete(35);
    bpt.print();
   
    

    
    bpt.insert(56, 10);
    bpt.insert(57, 10);
    bpt.insert(59, 10);
    bpt.insert(58, 10);
    bpt.insert(60, 10);

    bpt.insert(7, 10);
    bpt.insert(8, 10);
    bpt.insert(36, 10);
    bpt.insert(37, 10);
    bpt.insert(49, 10);
    bpt.insert(1, 10);
    bpt.insert(2, 10);
    bpt.insert(3, 10);
    




    bpt.print();

    if (bpt.search(15) != null) {
      System.out.println("Found");
    } else {
      System.out.println("Not Found");
    }
    ;
  }
}
