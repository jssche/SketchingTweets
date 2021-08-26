package com.example;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import org.json.*;

public class App 
{
    private List<String> stopwords = Arrays.asList("rt", "a", "about", "above", "after", "again", "against", "ain", "all", "am", "an", "and", "any", "are", "aren", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can", "couldn", "couldn't", "d", "did", "didn", "didn't", "do", "does", "doesn", "doesn't", "doing", "don", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn", "hadn't", "has", "hasn", "hasn't", "have", "haven", "haven't", "having", "he", "her", "here", "hers", "herself", "him", "himself", "his", "how", "i", "if", "in", "into", "is", "isn", "isn't", "it", "it's", "its", "itself", "just", "ll", "m", "ma", "me", "mightn", "mightn't", "more", "most", "mustn", "mustn't", "my", "myself", "needn", "needn't", "no", "nor", "not", "now", "o", "of", "off", "on", "once", "only", "or", "other", "our", "ours", "ourselves", "out", "over", "own", "re", "s", "same", "shan", "shan't", "she", "she's", "should", "should've", "shouldn", "shouldn't", "so", "some", "such", "t", "than", "that", "that'll", "the", "their", "theirs", "them", "themselves", "then", "there", "these", "they", "this", "those", "through", "to", "too", "under", "until", "up", "ve", "very", "was", "wasn", "wasn't", "we", "were", "weren", "weren't", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "won", "won't", "wouldn", "wouldn't", "y", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves", "could", "he'd", "he'll", "he's", "here's", "how's", "i'd", "i'll", "i'm", "i've", "let's", "ought", "she'd", "she'll", "that's", "there's", "they'd", "they'll", "they're", "they've", "we'd", "we'll", "we're", "we've", "what's", "when's", "where's", "who's", "why's", "would");
    private ArrayList<String> tweets = new ArrayList<String>(); 
    private ArrayList<String> indexedTweetList = new ArrayList<String>();
    private ArrayList<String> queryList = new ArrayList<String>();

    private int getTweetsCount(){
        return this.tweets.size();
    }

    private void processFilesForFolder(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                processFilesForFolder(fileEntry);
            } else {
                System.out.println(fileEntry.getName());
                this.process(fileEntry);
            }
        }
        System.out.println("writing...");
        writeTweet(this.tweets, "processedTweets.txt");
    }

    private void process(File fileEntry){
        Scanner inputStream = null; 
        
        try{
            inputStream = new Scanner(new FileInputStream(fileEntry));
        }
        catch(FileNotFoundException e){
            System.out.println("file was not found");
        }
        while(inputStream.hasNextLine()){
            String line = inputStream.nextLine();
            this.readTweet(line);
        } 
    }

    private void readTweet(String line){
        int endIndex = 1;
        String terms;
        ArrayList<String> termList = null;
         
        try {
            JSONObject jsonLine = new JSONObject(line);          
            termList = this.cleanTweet(jsonLine.getString("text"));            
            if(!termList.isEmpty()){
                terms = termList.toString();
                if(terms.length()>1){
                    endIndex = terms.length() - 1;
                }
                terms = terms.substring(1, endIndex);
                this.tweets.add(terms + "\n");
            }
        } catch (JSONException e) {
        }
    }
    
    
    private ArrayList<String> cleanTweet(String text){
        String[] textArray = null;
        ArrayList<String> termList = new ArrayList<String>();
        text = text.replaceAll("(@[A-Za-z0-9_-]+)|(http\\S+)|(https\\S+)|[^a-zA-Z0-9\\s]|[\\n\\t\\r]","")
                    .toLowerCase().trim();
        textArray = text.split(" ",0);
        for(int i=0; i<textArray.length; i++){
            if(!textArray[i].isBlank()){
                termList.add(textArray[i].trim());
            }
        }
        termList.removeAll(this.stopwords);
        return termList;
    }


    private void writeTweet(ArrayList<String> tweets, String fileName){
        String tweetsString = "";

        for(int i=0; i<tweets.size();i++){
            tweetsString = tweetsString + tweets.get(i);
        }

        try {
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write(tweetsString);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    private void sampleTweets(double size){

        Random rand = new Random();
        rand.setSeed(5);

        ArrayList<String> sampled = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int randomIndex = rand.nextInt(this.getTweetsCount());
            sampled.add(this.tweets.get(randomIndex));
            this.tweets.remove(randomIndex);
        }

        this.writeTweet(sampled, "sampledTweets.txt");
        
    }

    private void divideIndexQueryTweets(String filename){
        Scanner inputStream = null; 
        Random rand = new Random();
        rand.setSeed(5);

        try{
            inputStream = new Scanner(new FileInputStream(filename));
        }
        catch(FileNotFoundException e){
            System.out.println("file not found");
        }

        while(inputStream.hasNextLine()){
            String line = inputStream.nextLine();
            if(line!=""){
                if(rand.nextInt(10)<9){
                    this.indexedTweetList.add(line + "\n");
                }else{
                    this.queryList.add(line + "\n");
                }
            }
        }
    }

    public static void main( String[] args )
    {   
        App prerocessor = new App();
        // final File folder = new File("C:\\Users\\cheng\\Documents\\Study\\Research Project\\RawData");
        // prerocessor.processFilesForFolder(folder);
        // prerocessor.sampleTweets(prerocessor.getTweetsCount() * 0.1);
        prerocessor.divideIndexQueryTweets("sampledTweets.txt");
        prerocessor.writeTweet(prerocessor.indexedTweetList, "tweets_indexing.txt");
        prerocessor.writeTweet(prerocessor.queryList, "tweets_querying.txt");
    }
}
