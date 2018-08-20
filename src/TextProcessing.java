/*
************** The University of Queensland ********************************
*
*  @course      : INFS7410 Information Retrieval
*  @assignment  : Assignment 1 - Text Processing
*  @author      : Jackson Joseph Mathew, 4524077
*  @email       : j.mathew@uqconnect.edu.au, jacksonj.mathew@gmail.com
*
* **************************************************************************
*/

import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.*;

public class TextProcessing {

    //Regular expression for SGML Tags

    private static final String HTML_PATTERN = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>";

    // Used to count the total number of words in the collection

    private static int collectionWordCount = 0;

    // Array list for vocabulary

    private static ArrayList<VocabularyWord> vocabulary = new ArrayList<VocabularyWord>();

    public static void main(String[] args) throws IOException {

        // File streams to read from a folder

        File folder = new File(System.getProperty("user.dir") + "/tokens/withstoppingwords");
        for (final File fileEntry : folder.listFiles()) {
            System.out.println(fileEntry.getName() + " started to process file.");


            File file = new File(System.getProperty("user.dir") + "/dataset/cranfieldDocs/" + fileEntry.getName());
            Scanner input = new Scanner(file);

            // Streams to write on token files

            PrintWriter tokenFile = new PrintWriter("tokens/withstoppingwords/" + fileEntry.getName(), "UTF-8");

            int wordCount = 0;
            while (input.hasNext()) {
                String word = input.next();
                if (!isTag(word)) {
                    word = tokenize(word);
                    if(word.trim().length() > 0) {
                        tokenFile.println(word.trim());
                        wordCount = wordCount + 1;
                        addVocabularyWord(word.trim());
                    }
                }
            }
            tokenFile.print("Word count: " + wordCount);
            tokenFile.close();
            collectionWordCount = collectionWordCount + wordCount;

            // Sort the Vocabulary based on frequency of the word in descending order.

            Collections.sort(vocabulary, new Comparator<VocabularyWord>() {
                public int compare(VocabularyWord w1, VocabularyWord w2) {
                    return Integer.valueOf(w2.frequency).compareTo(w1.frequency);
                }
            });

            System.out.println("Finisheed processing file: " + fileEntry.getName());

        }

        // Print the top 50 words of the vocabulary in the text file

        PrintWriter task1Stat = new PrintWriter("tasks/task1.txt","UTF-8");
        task1Stat.println("Total number of words in the collection: " + collectionWordCount);
        task1Stat.println("Vocabulary Size: " + vocabulary.size());

        // allocate rank to the top 50 words in the vocabulary and write them on the file

        task1Stat.println("Word" + "\t" + "Frequency" + "\t" + "Rank");
        for (int i=0; i<50 && i<vocabulary.size() ; i++) {

            // updates the rank of the words in the vocabulary
            VocabularyWord updateVocab = new VocabularyWord();
            updateVocab = vocabulary.get(i);
            updateVocab.updaterank(i+1);
            vocabulary.set(i,updateVocab);

            // Write the vocabulary in the file
            task1Stat.println(vocabulary.get(i).word + "\t" + vocabulary.get(i).frequency + "\t" + vocabulary.get(i).rank);
        }
        task1Stat.close();

        // Removing Stopwords

        ArrayList<VocabularyWord> refinedVocabulary = new ArrayList<VocabularyWord>();
        refinedVocabulary = vocabulary;
        removeStopWords(refinedVocabulary);


    }

    /*
    * The function is used to check weather a word is a SGML tag or not. Returns true if it is a tag or else sends false
    * @param word String
    * @return boolean
    */
    private static boolean isTag(String word) {
        Pattern tagPattern = Pattern.compile(HTML_PATTERN);
        Matcher tagMatcher = tagPattern.matcher(word);
        return tagMatcher.matches();
    }

    /*
    * The function tokanises a word that is passes as parameter. The following operations are done
    * 1. Change capital alphabetical characters to small aphabetical characters
    * 2. Replace all non-alphanumeric values with space
    * @param word String
    * @return String
    */
    private static String tokenize(String word) {
        word.toLowerCase();
        return word.replaceAll("[^A-Za-z0-9]", " ");
    }

    /*
    * The function adds a word to the vocabulary or updates the frequency of the word in the vocabulary if the word is already there in the vocabulary
    * @param word String
    * @return NULL
    */
    private static void addVocabularyWord(String vocabword) {
        int flag = 0, listIndex = 0;
        for (int i=0; i<vocabulary.size(); i++) {
            if (vocabword.equals(vocabulary.get(i).word)){
                flag = 1;
                listIndex = i;
                break;
            }
        }
        if (flag == 0) {
            VocabularyWord vocab = new VocabularyWord(vocabword);
            vocabulary.add(vocab);
        }
        else {
            VocabularyWord updateVocab = new VocabularyWord();
            updateVocab = vocabulary.get(listIndex);
            updateVocab.updatefrequency();
            vocabulary.set(listIndex,updateVocab);
        }
    }

    /*
     * The function removes the stopwords from the vocabulary. The stopwords are taken from the common_words.txt file provided
     * @param refinedVocabulary ArrayList
     * @return NULL
     */
    private static void removeStopWords(ArrayList<VocabularyWord> refinedVocabulary) throws IOException {


        // File streams to read from a folder

        File folder = new File(System.getProperty("user.dir") + "/tokens/withstoppingwords");
        for (final File fileEntry : folder.listFiles()) {
            System.out.println(fileEntry.getName() + " started to process file.");


            File file = new File(System.getProperty("user.dir") + "/tokens/withstoppingwords/" + fileEntry.getName());
            Scanner input = new Scanner(file);

            // Streams to write on different files

            PrintWriter tokenFile = new PrintWriter("tokens/withoutstoppingwords/" + fileEntry.getName(), "UTF-8");

            int wordCount = 0;
            while (input.hasNext()) {
                String word = input.next();
                if ((word.trim().length() > 0) && !checkIsStopWord(word.trim())) {
                    tokenFile.println(word.trim());
                    wordCount = wordCount + 1;
                }
            }
            tokenFile.print("Word count: " + wordCount);
            tokenFile.close();
        }


        PrintWriter task2Stat = new PrintWriter("tasks/task2.txt","UTF-8");
        //PrintWriter tokenFile = new PrintWriter("tokens/withoutstoppingwords/cranfield0001", "UTF-8");

        int j=0;
        while(j<refinedVocabulary.size()) {
            if(checkIsStopWord(refinedVocabulary.get(j).word))
                refinedVocabulary.remove(j);
            else
                j++;
        }

        task2Stat.println("Total number of words in the collection: " + collectionWordCount);
        task2Stat.println("Vocabulary Size: " + refinedVocabulary.size());

        // allocate rank to the top 50 words in the vocabulary and write them on the file

        task2Stat.println("Word" + "\t" + "Frequency" + "\t" + "Rank");
        for (int i=0; i<50 && i<refinedVocabulary.size() ; i++) {

            // updates the rank of the words in the vocabulary
            VocabularyWord updateVocab = new VocabularyWord();
            updateVocab = vocabulary.get(i);
            updateVocab.updaterank(i+1);
            vocabulary.set(i,updateVocab);

            // Write the vocabulary in the file
            task2Stat.println(refinedVocabulary.get(i).word + "\t" + refinedVocabulary.get(i).frequency + "\t" + refinedVocabulary.get(i).rank);
            //tokenFile.println(refinedVocabulary.get(i).word);
        }
        task2Stat.close();
        //tokenFile.close();
    }

    /*
     * The function checks weather a given word is a stopword or not by comparing the word with the content of the file common_words.txt
     * @param word String
     * @return boolean
     */
    private static boolean checkIsStopWord(String word) throws IOException {
        File file = new File(System.getProperty("user.dir") + "/dataset/common_words.txt");
        Scanner input = new Scanner(file);

        while(input.hasNextLine()) {
            if(word.equals(input.nextLine().trim()))
                return true;
        }
        return false;
    }
}

/*
*This class is used to represent a word stored in the vocabulary as an object.
*/
class VocabularyWord {

    // The string or word in the vocabulary

    public String word;

    // The number of times the word appears in the collection

    public int frequency;

    // The rank of the word in the collection. It is based on frequency

    public int rank;

    /*
    * Constructor used to initialize the object
    * @param vocabWord String
    * @return NULL
    */
    public VocabularyWord(String vocabWord) {
        this.word = vocabWord;
        this.frequency = 1;
        this.rank = 0;
    }

    /*
     * An Empty Constructor
     */
    public VocabularyWord() {

    }

    /*
     * Function used to update the frequency of a word
     * @param NULL
     * @return NULL
     */
    public void updatefrequency() {
        frequency = frequency+1;
    }

    /*
     * Function used to update the rank of a word in the dictionary
     * @param i int
     * @return NULL
     */
    public void updaterank(int i) {
        rank = i;
    }
}
