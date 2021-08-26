package com.unimelb.sketchingtweet.v1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import static java.lang.Math.ceil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;



public class InvertedIndex {
    
    private MyHashTable table = null;
    private int numTweet = 0;
    private ArrayList<String> queryList = new ArrayList<String>();
    private int indexTimingUnit = 200;
    private int queryTimingUnit = 200;
    private String[] queryResults;
    private String[] queryRestltSimilarity;
    private ArrayList<Double> initTime = new ArrayList<Double> ();
    private ArrayList<Double> queryTime = new ArrayList<Double> ();
    
    
    private void initialize(String queryTweetFile, String indexTweetFile, double prob){     

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
                    this.addTweetToTable(line, prob);
                }
                counter++;
                if(counter == this.indexTimingUnit){
                    long currentTime = System.nanoTime();
                    long timeElapsed = currentTime - startTime;
                    this.initTime.add(timeElapsed * 1.0 / 1000000.0);
                    // System.out.println(timeElapsed * 1.0 / 1000000.0); // milliseconds
                    startTime = currentTime;
                    counter = 0;
                }
            } 
        }
        catch(FileNotFoundException e){
            System.out.println("file not found");
        }
        // System.out.println(this.queryList);
        // System.out.println(this.queryList.size());
    }
   
   
    private boolean toInclude(String term, double prob){
        return Murmur3.hash32(term.getBytes()) % 100 < prob * 100;
    }


    private void addTweetToTable(String line, double prob){

        String term;
        List<String> termList = null;

        termList = Arrays.asList(line.split(",",0));
         
        if(termList.size() != 0){
            this.numTweet++;
            // System.out.println(numTweet);
            // System.out.println(termList);
            for(int i=0;i<termList.size();i++){
                term = termList.get(i).trim();
                if(prob == 1){
                    this.table.put(term, numTweet);
                }else{
                    if(toInclude(term, prob)){
                        this.table.put(term, numTweet);
                    }   
                }         
            }
        }
    }
    
    
    private Pair<String, String> findSimilarTweet(String query, double similarityThreshold, double prob){

//      get the document id list for each of the word in the new tweet
        int numTerm;
        String term;
        int fingerprintSize;
        List<String> termList;
        ArrayList<String> fingerprint = new ArrayList<String> ();

        termList = Arrays.asList(query.split(",", 0));
        numTerm = termList.size();
        // MyHashNode[] docLists = new MyHashNode[numTerm];
        ArrayList<MyHashNode> docLists = new ArrayList<MyHashNode>();
        for(int i=0;i<numTerm;i++){
            term = termList.get(i).trim();
            if(prob == 1){
                fingerprint.add(term);
                docLists.add(this.table.getDocList(term));
            }else{
                if(toInclude(term, prob)){
                    fingerprint.add(term);
                    docLists.add(this.table.getDocList(term));
                }   
            }    
        }
        fingerprintSize = docLists.size();
        // System.out.println(numTerm);
        // System.out.println(fingerprint);
        // System.out.println(docLists);


//      linear search
        int latestDocID = 0;
        ArrayList<Integer> latestDocIndex = new ArrayList<Integer>();
        int threshold = (int)ceil(similarityThreshold * fingerprintSize);
        int nullCount = 0;
         
        while(true){
//          latestDocID stores the max document id (the larger doc id is, the newer the doc is)
//          latestDocIndex stores the index of the words in the new tweet that has the max doc id at front of the list/also appeared in the latest doc   
            for(int i=0;i<fingerprintSize;i++){
                if(docLists.get(i)!=null){
                    if((int)docLists.get(i).getKey() > latestDocID){
                        latestDocID = (int)docLists.get(i).getKey();
                        latestDocIndex.clear();
                        latestDocIndex.add(i);
                    }else if((int)docLists.get(i).getKey() == latestDocID){
                        latestDocIndex.add(i);
                    } 
                }else{
                    nullCount += 1;
                }
            }
            
//          check if the num of words that has the max doc id meets the similarity criteria 
//          if yes, return the doc id
//          if no, move the pointers of the words that have the max doc id to the next doc id and restores the maxDocID
            if(nullCount == fingerprintSize){
                // System.out.println("Similarity: 0 \nNo similar tweet found");
                return new Pair<String,String>("-1", "0");
            }else{
                if(latestDocIndex.size() >= threshold){
                    double similar = (latestDocIndex.size() * 1.0)/(fingerprintSize * 1.0);
                    // System.out.println("Similarity: " + String.format("%.5f", similar) +"\n" + "The most similar doc id: " + Integer.toString(latestDocID));
                    return new Pair<String,String>(Integer.toString(latestDocID), String.format("%.5f", similar));
                }else{
                    for(int i=0;i<latestDocIndex.size();i++){
                        docLists.set(latestDocIndex.get(i), docLists.get(latestDocIndex.get(i)).getNext()); 
                    }
                    latestDocIndex.clear();
                    latestDocID = 0;
                    nullCount = 0;
                    }
            }              
        }
    }


    private void runQueryList(ArrayList<String> queryList, double threshold, double prob){
        long startTime = System.nanoTime();
        this.queryResults = new String[this.queryList.size()];
        this.queryRestltSimilarity = new String[this.queryList.size()];

        for(int i=0;i<this.queryList.size();i++){
            // System.out.println("Query id " + Integer.toString(i+1));
            Pair<String,String> result = this.findSimilarTweet(this.queryList.get(i), threshold, prob);
            this.queryResults[i] = result.getKey();
            this.queryRestltSimilarity[i] = result.getValue();

            if(i % this.queryTimingUnit == 0){
                long currentTime = System.nanoTime();
                long timeElapsed = currentTime - startTime;
                this.queryTime.add(timeElapsed * 1.0 / 1000000.0);
                // System.out.println(timeElapsed * 1.0 / 1000000.0); // milliseconds
                startTime = currentTime;
            }
        }
    }

    private void writeQueryResult(String[] results, String fileName){
        try {
            FileWriter myWriter = new FileWriter(fileName);
            for(int i=0;i<this.queryList.size();i++){
                myWriter.append(results[i] + "\n");
            }
            myWriter.flush();
            myWriter.close();
            System.out.println("Successfully wrote " + fileName + "to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private void writeTimeResult(ArrayList<Double> results, String fileName){
        try {
            FileWriter myWriter = new FileWriter(fileName);
            for(int i=0;i<results.size();i++){
                myWriter.append(Double.toString(results.get(i)) + "\n");
            }
            myWriter.flush();
            myWriter.close();
            System.out.println("Successfully wrote " + fileName + " to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    public static void main(String[] arg){
        double prob = 0.9;
        InvertedIndex fileIndex = new InvertedIndex();
        fileIndex.initialize("tweets_querying.txt", "tweets_indexing.txt", prob);
        fileIndex.runQueryList(fileIndex.queryList, 0.5, prob);
        fileIndex.writeTimeResult(fileIndex.initTime, "run_" + arg[0] + "_init_time.csv");
        fileIndex.writeTimeResult(fileIndex.queryTime, "run_" + arg[0] + "_query_time.csv");
        // "run " + arg[0] + 
        // fileIndex.writeQueryResult(fileIndex.queryResults, "queryResults.csv");
        // fileIndex.writeQueryResult(fileIndex.queryRestltSimilarity, "queryResultSimilarity.csv");        
    }
}
