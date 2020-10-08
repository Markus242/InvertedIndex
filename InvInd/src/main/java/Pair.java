import java.util.LinkedList;

public class Pair {
    int termFrequency;
    LinkedList<Integer> list = new LinkedList<>();
    Pair(int docID){
        termFrequency = 1;
        list.add(docID);
    }

    public void addDocument(int docID){
        termFrequency++;
        if (list.getLast() != docID) list.add(docID);
    }
}
