package src.bplustreedraft;

public class DictionaryPairDraft implements Comparable<DictionaryPairDraft>{
    int key;
    double value;

    public DictionaryPairDraft(int key, double value){
        this.key = key;
        this.value = value;
    }

    @Override
    public int compareTo(DictionaryPairDraft o) {
        if (key == o.key) {
            return 0;
        } else if (key > o.key) {
            return 1;
        } else {
            return -1;
        }
    }
}
