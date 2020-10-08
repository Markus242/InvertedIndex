import java.util.Iterator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        InvertedIndex myIndex = new InvertedIndex();
        myIndex.indexCollection("collection_html"); //не все тексты из папки txt есть в виде htm, индексы вхождений немного скачут
        //myIndex.indexCollection("collection"); //txt коллекция
        //myIndex.urlCrawler("http://shakespeare.mit.edu/index.html",2);
        //System.out.println(myIndex.executeComplexQuery("Calpurnia"));
        //System.out.println(myIndex.executeComplexQuery("Brutus OR Calpurnia"));
        //System.out.println(myIndex.executeComplexQuery("Brutus AND Calpurnia"));
        //System.out.println(myIndex.executeComplexQuery("Brutus AND Spiderman"));
        System.out.println(myIndex.executeComplexQuery("( Brutus OR Calpurnia ) AND ( Caesar OR Romeo OR Hamlet )"));
        System.out.println(myIndex.executeComplexQuery("Caesar OR ( Brutus AND Calpurnia )"));
        System.out.println(myIndex.executeComplexQuery("caesar or brutus and calpurnia"));
        System.out.println(myIndex.executeComplexQuery("Brutus and Calpurnia or Caesar and Romeo and Hamlet"));
        System.out.println(myIndex.executeComplexQuery("( Brutus and Calpurnia ) or ( Caesar and Romeo and Hamlet )"));



        System.out.println(myIndex.countTokens);
        System.out.println(myIndex.index.size());
    }
}
