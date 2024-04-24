package src.bplustreedraft;

import src.BPlusTree;

public class Main {
    public static void main(String[] args) {
        bplustreedraft bpt = null;
    
        bpt = new bplustreedraft(5);
        for(int i = 0; i < 30; i++){
            bpt.insert(i, 1);
        }
        
        bpt.print();

  }
}
