package com.example;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import org.json.*;

public class App 
{
    private List<String> stopwords = Arrays.asList("rt", "a", "about", "above", "after", "again", "against", "ain", "all", "am", "an", "and", "any", "are", "aren", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can", "couldn", "couldn't", "d", "did", "didn", "didn't", "do", "does", "doesn", "doesn't", "doing", "don", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn", "hadn't", "has", "hasn", "hasn't", "have", "haven", "haven't", "having", "he", "her", "here", "hers", "herself", "him", "himself", "his", "how", "i", "if", "in", "into", "is", "isn", "isn't", "it", "it's", "its", "itself", "just", "ll", "m", "ma", "me", "mightn", "mightn't", "more", "most", "mustn", "mustn't", "my", "myself", "needn", "needn't", "no", "nor", "not", "now", "o", "of", "off", "on", "once", "only", "or", "other", "our", "ours", "ourselves", "out", "over", "own", "re", "s", "same", "shan", "shan't", "she", "she's", "should", "should've", "shouldn", "shouldn't", "so", "some", "such", "t", "than", "that", "that'll", "the", "their", "theirs", "them", "themselves", "then", "there", "these", "they", "this", "those", "through", "to", "too", "under", "until", "up", "ve", "very", "was", "wasn", "wasn't", "we", "were", "weren", "weren't", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "won", "won't", "wouldn", "wouldn't", "y", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves", "could", "he'd", "he'll", "he's", "here's", "how's", "i'd", "i'll", "i'm", "i've", "let's", "ought", "she'd", "she'll", "that's", "there's", "they'd", "they'll", "they're", "they've", "we'd", "we'll", "we're", "we've", "what's", "when's", "where's", "who's", "why's", "would");
    private ArrayList<String> tweets = new ArrayList<String>();
    private ArrayList<Integer> tweetLengths  = new ArrayList<Integer>(); 
    private int lengthThreshold = 6;
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
                System.out.println("Processing " + fileEntry.getName());
                this.process(fileEntry);
            }
        }
        // System.out.println("Number of tweets to write: " + Integer.toString(this.tweets.size()));
        // writeTweet(this.tweets, "processedTweets.txt");
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
            if(termList.size() >= this.lengthThreshold){
                terms = termList.toString();
                if(terms.length()>1){
                    endIndex = terms.length() - 1;
                }
                terms = terms.substring(1, endIndex);
                this.tweetLengths.add(termList.size());
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


    private void findTweetLengthStats(){
        int sum = 0;
        int aboveAvgCount = 0;
        for(int l : this.tweetLengths){
            sum += l;
            if(l >= 6){
                aboveAvgCount += 1;
            }
        }

        // Find the average of the tweet lengths
        System.out.println(this.tweetLengths.size());
        double avg = sum / this.tweetLengths.size();
        System.out.println("Average tweet length: " + avg);
        System.out.println("Number fo tweets has a length of at least 6 terms: " + aboveAvgCount);

        // Find the 25th percentile of the tweet lengths
        Collections.sort(this.tweetLengths);
        int index25 = (int) Math.ceil(0.25 * this.tweetLengths.size());
        int index50 = (int) Math.ceil(0.5 * this.tweetLengths.size());
        int index75 = (int) Math.ceil(0.75 * this.tweetLengths.size());
        System.out.println("25th percentile of tweet length: " + this.tweetLengths.get(index25-1));
        System.out.println("50th percentile of tweet length: " + this.tweetLengths.get(index50-1));
        System.out.println("75th percentile of tweet length: " + this.tweetLengths.get(index75-1));
    }


    private void findTweetLengthStats(String filename){
        Scanner inputStream = null; 
        String[] textArray = null;

        try{
            inputStream = new Scanner(new FileInputStream(filename));
        }
        catch(FileNotFoundException e){
            System.out.println("file not found");
        }

        while(inputStream.hasNextLine()){
            String line = inputStream.nextLine();
            if(line!=""){
                textArray = line.split(",",0);
                this.tweetLengths.add(textArray.length);
            }
        }

        this.findTweetLengthStats();
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
        // prerocessor.findTweetLengthStats();
        // prerocessor.sampleTweets(prerocessor.getTweetsCount() * 0.2);

        // prerocessor.divideIndexQueryTweets("processedTweets.txt");
        // prerocessor.findTweetLengthStats("processedTweets.txt");
        // prerocessor.writeTweet(prerocessor.indexedTweetList, "indexing_tweets.txt");
        // prerocessor.writeTweet(prerocessor.queryList, "query_tweets.txt");

        // prerocessor.divideIndexQueryTweets("sampledTweets.txt");
        // prerocessor.findTweetLengthStats("sampledTweets.txt");
        // prerocessor.writeTweet(prerocessor.indexedTweetList, "sampled_indexing_tweets.txt");
        // prerocessor.writeTweet(prerocessor.queryList, "sampled_query_tweets.txt");

        // prerocessor.findTweetLengthStats("sampled_indexing_tweets.txt");
        // prerocessor.findTweetLengthStats("sampled_query_tweets.txt");
    }
}
