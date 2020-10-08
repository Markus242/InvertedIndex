import java.io.*;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class InvertedIndex implements Serializable {
    int countTokens;
    List<String> documents = new ArrayList<String>();
    Map<String, Pair> index = new HashMap<String, Pair>();
    List<String> indexedUrls = new ArrayList<>();

    public static void main(String[] args) { //схоронить индекс в бинарник, чтобы не составлять лишний раз
        ObjectInputStream objectIn = null;
        InvertedIndex myIndex = null;
        try {
            objectIn = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
                    "Index.bin")));
            myIndex = (InvertedIndex) objectIn.readObject();
        } catch (Exception e) {
            myIndex = new InvertedIndex();
            myIndex.indexCollection("collection");
            myIndex.saveIndex();
        }
        System.out.println();
    }

    /*public List<Integer> executeQuery(String query) { //не может правильно раскрыть скобочные выражения
        List<Integer> res;
        if (query.toLowerCase().split("\\s+and\\s+").length > 1) {
            String[] queue = query.toLowerCase().split("\\s+and\\s+");
            int i = 0;
            if (queue.length == 1) return this.index.get(queue[0]);
            else res = getIntersection(this.index.get(queue[i++]), this.index.get(queue[i++]));
            if (queue.length >= 3) while (i != queue.length) res = getIntersection(res, this.index.get(queue[i++]));
        }
        else if (query.toLowerCase().split("\\s+or\\s+").length > 1) {
            String[] queue = query.toLowerCase().split("\\s+or\\s+");
            int i = 0;
            if (queue.length == 1) return this.index.get(queue[0]);
            else res = getUnion(this.index.get(queue[i++]), this.index.get(queue[i++]));
            if (queue.length >= 3) while (i != queue.length) res = getUnion(res, this.index.get(queue[i++]));
        }
        else res = this.index.get(query);
        if (res == null) return new LinkedList<>();
        return res;
    }*/

    public List<Integer> executeComplexQuery(String query) {
        List<Integer> res = new LinkedList<>();
        Stack<List<Integer>> stack  = new Stack<>();

        String postfix = ShuntingYard.postfix(query.toLowerCase());
        if (postfix.split("\\s").length>2) {
            for (String token : postfix.split("\\s")) { //для алгоритма сортировочной станции не удобно учитывать частоту запроса
                if (token.equals("or")) {
                    stack.push(getUnion(stack.pop(), stack.pop()));
                } else if (token.equals("and")) {
                    stack.push(getIntersection(stack.pop(), stack.pop()));
                } else {
                    stack.push(this.index.get(token).list);
                }
            }
            if (!stack.empty()) res = stack.pop();
        } else {
            res = this.index.get(query.toLowerCase()).list;
            if (res==null) res=new LinkedList<>();
        }
        return res;
    }

    public static List<Integer> getIntersection(List<Integer> list1, List<Integer> list2) {
        List<Integer> list3 = new LinkedList<>();
        if (list1 == null || list2 == null || list1.size()==0 || list2.size()==0) return list3;
        Iterator<Integer> i = list1.iterator();
        Iterator<Integer> j = list2.iterator();
        int a1 = i.next();
        int a2 = j.next();
        while (true){
            if (a1 == a2) {
                list3.add(a1);
                if (i.hasNext()) a1 = i.next();
                else break;
                if (j.hasNext()) a2 = j.next();
                else break;
            } else if (a2 > a1) {
                if (i.hasNext()) a1 = i.next();
                else break;
            } else {
                if (j.hasNext()) a2 = j.next();
                else break;
            }
        }
        return list3;
    }

    public static List<Integer> getUnion(List<Integer> list1, List<Integer> list2) {
        List<Integer> list3 = new LinkedList<>();
        if (list1 == null || list1.size()==0) return list2;
        if (list2 == null || list2.size()==0) return list1;
        Iterator<Integer> i = list1.iterator();
        Iterator<Integer> j = list2.iterator();
        int a1 = i.next();
        int a2 = j.next();
        while (true) {
            if (a1 == a2) {
                list3.add(a1);
                if (i.hasNext()) a1 = i.next();
                else break;
                if (j.hasNext()) a2 = j.next();
                else {
                    list3.add(a1);
                    break;
                }
            } else {
                if (a1 < a2) {
                    list3.add(a1);
                    if (i.hasNext()) a1 = i.next();
                    else break;
                } else {
                    list3.add(a2);
                    if (j.hasNext()) a2 = j.next();
                    else {
                        list3.add(a1);
                        break;
                    }
                }
            }
        }
        while (i.hasNext()){
            list3.add(i.next());
        }
        while (j.hasNext()){
            list3.add(j.next());
        }
        return list3;
    }

    public void indexDocument(String path) {
        File file = new File(path);
        int docID;
        if (!documents.contains(file.getName())) {
            docID = documents.size();
            documents.add(file.getName());
        } else return;
        String line;
        Scanner in;
        String[] words;
        try {
            in = new Scanner(file);
            while (in.hasNextLine()) {
                line = in.nextLine();
                line = line.toLowerCase();
                words = line.split("[^a-zA-Z0-9_']+");
                Pair idx;
                for (int i = 0; i < words.length; i++) {
                    countTokens++;
                    idx = index.get(words[i]);
                    if (idx == null) {
                        idx = new Pair(docID);
                        index.put(words[i], idx);
                    } else {
                        idx.addDocument(docID);
                    }
                }
            }
            System.out.println(docID + " " + file.getPath() + " " + index.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void indexHtm(String path){
        Document doc = null;
        File file = new File(path);
        int docID;
        if (!documents.contains(file.getName())) {
            docID = documents.size();
            documents.add(file.getName());
        } else return;
        try {
            doc = Jsoup.parse(file,"UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String body = doc.select("body").text().toLowerCase();
        String[] words = body.split("[^a-zA-Z0-9_']+");
        Pair idx;
        for (int i = 0; i < words.length; i++) {
            countTokens++;
            idx = index.get(words[i]);
            if (idx == null) {
                idx = new Pair(docID);
                index.put(words[i], idx);
            } else {
                idx.addDocument(docID);
            }
        }
        System.out.println(docID + " " + file.getPath() + " " + index.size());
    }

    public void indexURL(String url){
        Document doc = null;
        String title = null;
        try {
            doc = Jsoup.connect(url).get();
            title = doc.title();
        } catch (IOException e) {
            System.out.println(url);
            e.printStackTrace();
        }
        int docID;
        if (!documents.contains(title)) {
            docID = documents.size();
            documents.add(title);
        } else return;
        String body = doc.select("body").text().toLowerCase();
        String[] words = body.split("[^a-zA-Z0-9_']+");
        Pair idx;
        for (int i = 0; i < words.length; i++) {
            countTokens++;
            idx = index.get(words[i]);
            if (idx == null) {
                idx = new Pair(docID);
                index.put(words[i], idx);
            } else {
                idx.addDocument(docID);
            }
        }
        System.out.println(docID + " " + title + " " + index.size());
    }

    public void indexCollection(String folder) {
        File dir = new File(folder);
        String[] files = dir.list();
        for (String file : files) {
            if (file.endsWith(".txt")) indexDocument(folder + "/" + file);
            else if (file.endsWith(".htm")) indexHtm(folder + "/" + file);
            else indexCollection(folder + "/" + file);
        }
    }

    /*public void urlCrawler(String url, int depth){ //экспериментальная фича краулера
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            System.out.println(url);
            e.printStackTrace();
        }
        indexedUrls.add(url);
        indexURL(url);
        Elements links = doc.select("a[href*=\".htm\"]");
        if (links == null || links.size()==0) return;
        for (Element link : links) {
            if (!indexedUrls.contains(link.absUrl("href")) && depth>0) urlCrawler(link.absUrl("href"),depth-1);
        }
    }*/

    public void saveIndex(){
        try {
            ObjectOutputStream objectOut = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream("Index.bin")));
            objectOut.writeObject(this);
            objectOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}