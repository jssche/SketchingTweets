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
    private ArrayList<String> tweets = new ArrayList<String>();
    private ArrayList<Integer> tweetLengths  = new ArrayList<Integer>(); 
    private int lengthThreshold = 6;
    private ArrayList<String> indexedTweetList = new ArrayList<String>();
    private ArrayList<String> queryList = new ArrayList<String>();
    private int totalTweet = 0;


    private int getTweetsCount(){
        return this.tweets.size();
    }

    public int getTotalTweet(){
        return this.totalTweet;
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
            totalTweet++;   
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
        System.out.println("Max tweet length: " + this.tweetLengths.get(this.tweetLengths.size()-1));
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
                textArray = line.split(",", 0);
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

        this.indexedTweetList.clear();
        this.queryList.clear();

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
        // System.out.println(prerocessor.getTotalTweet());
        // prerocessor.findTweetLengthStats();
        // prerocessor.sampleTweets(prerocessor.getTweetsCount() * 0.2);

        prerocessor.divideIndexQueryTweets("processedTweets_stemmed.txt");
        prerocessor.writeTweet(prerocessor.indexedTweetList, "indexing_tweets_stemmed.txt");
        prerocessor.writeTweet(prerocessor.queryList, "query_tweets_stemmed.txt");

        prerocessor.divideIndexQueryTweets("sampledTweets_stemmed.txt");
        prerocessor.writeTweet(prerocessor.indexedTweetList, "sampled_indexing_tweets_stemmed.txt");
        prerocessor.writeTweet(prerocessor.queryList, "sampled_query_tweets_stemmed.txt");
    }
}
