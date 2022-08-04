import java.io.*;
import java.util.*;

/**
 * Class that holds the document, the document's word counts, and the term frequency
 */
class Document {

    /* Instance variables */
    public HashMap<String,Double> termFreqMap ;
    public HashMap<String,Integer> DocWordCounts;

    /* Getter and Setter methods */
    public HashMap<String,Double> getTermFreqMap() {
        return termFreqMap;
    }

    public HashMap<String,Integer> getWordCountMap() {
        return DocWordCounts;
    }

    public void setTermFreqMap(HashMap<String,Double> inMap) {
        termFreqMap = new HashMap<String, Double>(inMap);
    }

    public void setWordCountMap(HashMap<String,Integer> inMap) {
        DocWordCounts =new HashMap<String, Integer>(inMap);
    }
}

/**
 * Calculates the TF-IDF scores for each word in each document
 */
class Tfidf {

    // keeps track of all the words (sorted)
    SortedSet<String> wordList = new TreeSet(String.CASE_INSENSITIVE_ORDER);

    //Calculates inverse Doc frequency.
    public HashMap<String,Double> calculateInverseDocFrequency(Document[] docs) {
        HashMap<String,Double> InverseDocFreqMap = new HashMap<>();
        int size = docs.length;
        double wordCount ;
        for (String word : wordList) {
            wordCount = 0;
            for(int i=0;i<size;i++) {
                HashMap<String,Integer> tempMap = docs[i].getWordCountMap();
                if(tempMap.containsKey(word)) {
                    wordCount++;
                    continue;
                }
            }
            // calculates IDF score
            double temp = size/wordCount;
            double idf = 1 + Math.log(temp);
            InverseDocFreqMap.put(word,idf);
        }
        return InverseDocFreqMap;
    }

    // calculates Term frequency for all terms
    public HashMap<String,Double> calculateTermFrequency(HashMap<String,Integer>inputMap) {
        HashMap<String ,Double> termFreqMap = new HashMap<>();
        double totalWords = 0;
        // calculate total amount of words
        for (float val : inputMap.values()) {
            totalWords += val;
        }

        // create a new hashMap with Tf values in it.
        Iterator it = inputMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            double tf = (Integer)pair.getValue()/totalWords;
            termFreqMap.put(pair.getKey().toString(),tf);
        }
        return termFreqMap;
    }

    // cleaning up the input by removing .,:" (Got help from source)
    public  String clean(String input) {
        String cleaned = input.replaceAll("[, . : ;\"]", "");
        cleaned = cleaned.replaceAll("\\p{P}","");
        cleaned = cleaned.replaceAll("\t","");
        return cleaned;
    }

    // Converts the input text file to hashmap
    public  HashMap<String, Integer> getTermsFromFile(String Filename,int count,File folder) {
        HashMap<String,Integer> WordCount = new HashMap<String,Integer>();
        BufferedReader reader = null;
        HashMap<String, Integer> map = new HashMap<>();
        try {
            reader = new BufferedReader(new FileReader(Filename));
            String line = reader.readLine();
            while(line!=null) {
                String[] words = line.toLowerCase().split(" ");
                for (String term : words) {
                    term = clean(term);
                    if (term.length() == 0) continue;
                    wordList.add(term);
                    if (WordCount.containsKey(term)) {
                        WordCount.put(term,WordCount.get(term)+1);
                    }
                    else {
                        WordCount.put(term,1);
                    }
                }
                line = reader.readLine();
            }
            // sorting
            Map<String, Integer> treeMap = new TreeMap<>(WordCount);
            map = new HashMap<String, Integer>(treeMap);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    // outputs results to a CSV (Got help from source)
    public void outputAsCSV(HashMap<String,Double>treeMap, String out) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("Word,TF-IDF\r\n");
        for (Map.Entry<String, Double> keymap : treeMap.entrySet()) {
            // adds the word with its tf-idf value in a new row
            builder.append(keymap.getKey() + "," + keymap.getValue() + "\r\n");
        }
        String content = builder.toString().trim();
        BufferedWriter writer = new BufferedWriter(new FileWriter(out));
        writer.write(content);
        writer.close();
    }

    // main method
    public static void main(String[] args) throws IOException {

        // reads in the path to the folder
        Scanner scanner = new Scanner(System.in);
        System.out.println("What is the folder path?");
        String folderPath = scanner.next();

        // retrieves all the files in the folder
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        // creates tfidf object
        Tfidf tfidf = new Tfidf();

        // holds the documents and their properties required to calculate final score
        Document[] docs = new Document[files.length];
        int count = 0;

        // finds wordcount and calculates tf
        for (File file : files) {
            if (file.isFile()) {
                docs[count] = new Document();
                HashMap<String,Integer> wordCount = tfidf.getTermsFromFile(file.getAbsolutePath(), count, folder);
                docs[count].setWordCountMap(wordCount);
                HashMap<String,Double> termFrequency = tfidf.calculateTermFrequency(docs[count].DocWordCounts);
                docs[count].setTermFreqMap(termFrequency);
                count++;
            }
        }

        // calculates IDF
        HashMap<String,Double> inverseDocFreqMap = tfidf.calculateInverseDocFrequency(docs);

        // calculates tf-idf
        count = 0;
        for (File file : files) {
            if (file.isFile()) {
                HashMap<String,Double> tfIDF = new HashMap<>();
                double tfIdfValue = 0;
                double idfVal = 0;
                HashMap<String,Double> tf = docs[count].getTermFreqMap();
                Iterator itTF = tf.entrySet().iterator();
                while (itTF.hasNext()) {
                    Map.Entry pair = (Map.Entry)itTF.next();
                    double tfVal  = (Double)pair.getValue() ;
                    if(inverseDocFreqMap.containsKey((String)pair.getKey())) {
                        idfVal = inverseDocFreqMap.get((String)pair.getKey());
                    }
                    tfIdfValue = tfVal * idfVal;
                    tfIDF.put((pair.getKey().toString()),tfIdfValue);
                }
                int fileNameNumber = (count + 1);
                String OutPut = folder.getAbsolutePath() + "/tfidf" + file.getName() + fileNameNumber + ".csv";
                tfidf.outputAsCSV(tfIDF,OutPut);
                count++;
            }
        }

    }

}
