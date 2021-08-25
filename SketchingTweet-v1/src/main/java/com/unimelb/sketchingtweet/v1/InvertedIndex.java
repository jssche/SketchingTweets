package com.unimelb.sketchingtweet.v1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import static java.lang.Math.ceil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;



public class InvertedIndex {
    
    private MyHashTable table = null;
    private int numTweet = 0;
    private ArrayList<String> queryList = new ArrayList<String>();
    private int indexTimingUnit = 200;
    private int queryTimingUnit = 200;
    
    
    
    private void initialize(String queryTweetFile, String indexTweetFile){     

        int counter = 0;  
        Scanner inputStream = null; 
        this.table = new MyHashTable();

        // construct query list
        try{
            inputStream = new Scanner(new FileInputStream(queryTweetFile));
            while(inputStream.hasNextLine()){
                String line = inputStream.nextLine();
                if(line!=""){
                    queryList.add(line);
                }
            }
        }
        catch(FileNotFoundException e){
            System.out.println("file not found");
        }
  
        // construct hash table
        try{
            inputStream = new Scanner(new FileInputStream(indexTweetFile));
            long startTime = System.nanoTime();
            while(inputStream.hasNextLine()){
                String line = inputStream.nextLine();
                if(line!=""){
                    this.addTweetToTable(line);
                }
                // counter++;
                // if(counter == this.indexTimingUnit){
                //     long currentTime = System.nanoTime();
                //     long timeElapsed = currentTime - startTime;
                //     System.out.println(timeElapsed * 1.0 / 1000000.0); // milliseconds
                //     startTime = currentTime;
                //     counter = 0;
                // }
            } 
        }
        catch(FileNotFoundException e){
            System.out.println("file not found");
        }
        // System.out.println(this.queryList);
        // System.out.println(this.queryList.size());
    }


    private void writeTweet(ArrayList<List<String>> tweets, String fileName){
        String tweetsString = "";

        for(int i=0; i<tweets.size();i++){
            tweetsString = tweetsString + tweets.get(i) + "\n";
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
    
   
    private void addTweetToTable(String line){
        List<String> termList = null;
        termList = Arrays.asList(line.split(",",0));
         
        if(termList.size() != 0){
            this.numTweet++;
            // System.out.println(numTweet);
            // System.out.println(termList);
            for(int i=0;i<termList.size();i++){
                this.table.put(termList.get(i).trim(), numTweet);
            }
        }
    }
    
    
    private String findSimilarTweet(String query, double similarityThreshold){
//      get the document id list for each of the word in the new tweet
        int totalWords;
        List<String> termList;

        termList = Arrays.asList(query.split(",", 0));
        // System.out.println(termList);
        totalWords = termList.size();
        
        MyHashNode[] docLists = new MyHashNode[totalWords];
        for(int i=0;i<totalWords;i++){
            docLists[i] = this.table.getDocList(termList.get(i).trim());
//            System.out.println(docLists[i]);
        }

//      linear search
        int latestDocID = 0;
        ArrayList<Integer> latestDocIndex = new ArrayList<Integer>();
        int threshold = (int)ceil(similarityThreshold * totalWords);
//        System.out.println(threshold);
        int nullCount = 0;
         
        while(true){
//          latestDocID stores the max document id (the larger doc id is, the newer the doc is)
//          latestDocIndex stores the index of the words in the new tweet that has the max doc id at front of the list/also appeared in the latest doc   
            for(int i=0;i<totalWords;i++){
                if(docLists[i]!=null){
                    if((int)docLists[i].getKey() > latestDocID){
                        latestDocID = (int)docLists[i].getKey();
                        latestDocIndex.clear();
                        latestDocIndex.add(i);
                    }else if((int)docLists[i].getKey() == latestDocID){
                        latestDocIndex.add(i);
                    } 
                }else{
                    nullCount += 1;
                }
            }
            
//          check if the num of words that has the max doc id meets the similarity criteria 
//          if yes, return the doc id
//          if no, move the pointers of the words that have the max doc id to the next doc id and restores the maxDocID
            if(nullCount == totalWords){
                // System.out.println("Similarity: 0 \nNo similar tweet found");
                return "-1";
            }else{
                if(latestDocIndex.size() >= threshold){
                    double similar = (latestDocIndex.size() * 1.0)/(totalWords * 1.0);
                    // System.out.println("Similarity: " + String.format("%.5f", similar) +"\n" + "The most similar doc id: " + Integer.toString(latestDocID));
                    return Integer.toString(latestDocID);
                }else{
                    for(int i=0;i<latestDocIndex.size();i++){
                        docLists[latestDocIndex.get(i)] = docLists[latestDocIndex.get(i)].getNext();
                }
                latestDocIndex.clear();
                latestDocID = 0;
                nullCount = 0;
                }
            }              
        }
    }

    private void runQueryList(ArrayList<String> queryList, double threshold){
        long startTime = System.nanoTime();

        for(int i=0;i<this.queryList.size();i++){
            // System.out.println("Query id " + Integer.toString(i+1));
            this.findSimilarTweet(this.queryList.get(i), 0.5);

            if(i % this.queryTimingUnit == 0){
                long currentTime = System.nanoTime();
                long timeElapsed = currentTime - startTime;
                System.out.println(timeElapsed * 1.0 / 1000000.0); // milliseconds
                startTime = currentTime;
            }
        }
    }


    public static void main(String[] arg){
        InvertedIndex fileIndex = new InvertedIndex();
        fileIndex.initialize("tweets_querying.txt", "tweets_indexing.txt");
        fileIndex.runQueryList(fileIndex.queryList, 0.5);

        
        


        // System.out.println(fileIndex.table.getTableNode("latest").getCount());
        // System.out.println(fileIndex.findSimilarTweet("sxx sxx xxx", 0.5));
        // System.out.println(fileIndex.findSimilarTweet("Latest level: 0.8815m at 31/03/2016 23:00:00(GMT). Further information available at https://t.co/Jy7C3IU3nQ #riverlevels", 0.5));
        // System.out.println(fileIndex.findSimilarTweet("spend more time trying to change someone and less time trying to love and understand them xxx", 0.5));
        // System.out.println(fileIndex.findSimilarTweet("Get Weather Updates from The Weather Channel", 1));
        
    }
}
