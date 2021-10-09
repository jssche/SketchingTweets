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
import org.apache.commons.cli.*;



public class InvertedIndex {

    private int power;
    private String chance;
    private int indexTimingUnit;
    private int queryTimingUnit;
    private final double similarityThreshold;
    private MyHashTable table = null;

    private int numTweet = 0;
    private int numTerms = 0;
    private ArrayList<String> queryList = new ArrayList<String>();
    private MyTermFreqTable freqTable = null;
      
    private String[] queryResults;
    private String[] queryRestltSimilarity;
    private int[] lookupCounts;
    private ArrayList<Double> indexTime = new ArrayList<Double> ();
    private ArrayList<Double> queryTime = new ArrayList<Double> ();
    private ArrayList<Integer> fingerprintSize = new ArrayList<Integer> ();


    public InvertedIndex(int indexTimingUnit, int queryTimingUnit, int power, double similarityThreshold){
        this.indexTimingUnit = indexTimingUnit;
        this.queryTimingUnit = queryTimingUnit;
        this.power = power;
        this.similarityThreshold = similarityThreshold;
        this.chance = Double.toString((1-1/Math.pow(2, power))*100 == 0 ? 100 : (1-1/Math.pow(2, power))*100) + "%";
        this.table = new MyHashTable();
    }
    
    private void initialize(String queryTweetFile, String indexTweetFile){     
        int counter = 0;  
        Scanner inputStream = null; 
        

        // construct query list
        try{
            inputStream = new Scanner(new FileInputStream(queryTweetFile));
            while(inputStream.hasNextLine()){
                String line = inputStream.nextLine();
                if(line!=""){
                    this.queryList.add(line);
                }
            }
            this.lookupCounts = new int[this.queryList.size()];
            
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
                counter++;
                if(counter == this.indexTimingUnit){
                    long currentTime = System.nanoTime();
                    long timeElapsed = currentTime - startTime;
                    this.indexTime.add(timeElapsed * 1.0 / 1000000.0);
                    // System.out.println(timeElapsed * 1.0 / 1000000.0); // milliseconds
                    startTime = currentTime;
                    counter = 0;
                }
            } 
            this.numTerms = this.table.getSize();
        }
        catch(FileNotFoundException e){
            System.out.println("file not found");
        }
        // System.out.println("Term frequency of the word 'plane': " + this.getTermFreq("plane"));


        // construct term frequency table
        this.freqTable = new MyTermFreqTable(this.table.getSize(), this.table);

        System.out.println("Query file size: " + Integer.toString(this.queryList.size()));
        System.out.println("Number of tweets indexed: " + this.numTweet);
        System.out.println("Index size (Number of non-empty posting lists): " + this.numTerms);

    }
   
   
    private boolean toInclude(String term){
        long l = CityHash.cityHash64(term.getBytes(), 0, term.length());
        Long denominator = (long) Math.pow(2, this.power);
        return l % denominator != 0;
        // return l % 100 < 0.8 * 100;
    }


    private void addTweetToTable(String line){
        int numTerm;
        int counter = 0;
        String term;
        List<String> termList = null;

        termList = Arrays.asList(line.split(",",0));
        numTerm = Math.min(termList.size(), 37);
        
         
        if(termList.size() != 0){
            this.numTweet++;
            for(int i=0;i<numTerm;i++){
                term = termList.get(i).trim();
                if(this.power == 0){
                    this.table.put(term, numTweet);
                    counter++;
                }else{
                    if(toInclude(term)){
                        counter++;
                        this.table.put(term, numTweet);
                    }
                }         
            }
            this.fingerprintSize.add(counter);
        }
    }


    private int getTermFreq(String term){
        int count;
        try{
            count = this.table.getTableNode(term).getCount();
        }catch(NullPointerException e){
            System.out.println("The term is not in the index.");
            count = -1;
        }
        return count;
    }
    
    
    private Pair<String, String> findSimilarTweet(String query, int queryIndex){

//      get the document id list for each of the word in the new tweet
        int numTerm;
        String term;
        int fingerprintSize;
        List<String> termList;
        ArrayList<String> fingerprint = new ArrayList<String> ();

        termList = Arrays.asList(query.split(",", 0));
        // numTerm = termList.size();
        numTerm = Math.min(termList.size(), 37);
        // MyHashNode[] docLists = new MyHashNode[numTerm];
        ArrayList<MyHashNode> docLists = new ArrayList<MyHashNode>();
        for(int i=0;i<numTerm;i++){
            term = termList.get(i).trim();
            if(this.power == 0){
                fingerprint.add(term);
                docLists.add(this.table.getDocList(term));
            }else{
                if(toInclude(term)){
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
        int threshold = (int)ceil(this.similarityThreshold * fingerprintSize);
        int nullCount = 0;
        int lookupCount = 0;
         
        while(true){
//          latestDocID stores the max document id (the larger doc id is, the newer the doc is)
//          latestDocIndex stores the index of the words in the new tweet that has the max doc id at front of the list/also appeared in the latest doc   
            for(int i=0;i<fingerprintSize;i++){
                lookupCount++;
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
                this.lookupCounts[queryIndex] = -1;
                return new Pair<String,String>("-1", "0");
            }else{
                if(latestDocIndex.size() >= threshold){
                    double similar = (latestDocIndex.size() * 1.0)/(fingerprintSize * 1.0);
                    // System.out.println("Similarity: " + String.format("%.5f", similar) +"\n" + "The most similar doc id: " + Integer.toString(latestDocID));
                    this.lookupCounts[queryIndex] = lookupCount;
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


    private void runQueryList(ArrayList<String> queryList){
        long startTime = System.nanoTime();
        this.queryResults = new String[this.queryList.size()];
        this.queryRestltSimilarity = new String[this.queryList.size()];

        for(int i=0;i<this.queryList.size();i++){        
            Pair<String,String> result = this.findSimilarTweet(this.queryList.get(i), i);
            this.queryResults[i] = result.getKey();
            this.queryRestltSimilarity[i] = result.getValue(); 
            // System.out.println("\nQuery id: " + Integer.toString(i+1));
            // System.out.println("Similar tweet id: " + result.getKey());
            // System.out.println("Similarity: " + result.getValue());
            // System.out.println("Number of lookups: " + this.lookupCounts[i]);

            if(i % this.queryTimingUnit == 0){
                long currentTime = System.nanoTime();
                long timeElapsed = currentTime - startTime;
                this.queryTime.add(timeElapsed * 1.0 / 1000000.0);
                // System.out.println(timeElapsed * 1.0 / 1000000.0); // milliseconds
                startTime = currentTime;
            }
        }
    }


    private void writeSummary(int epoch, String fileName){

        String summary;
        summary = String.format("%d\n%s\n%d\n%d\n%f\n%d\n%d\n%d" 
                    , epoch, this.chance, this.indexTimingUnit, this.queryTimingUnit, this.similarityThreshold, this.queryList.size(), this.numTweet, this.numTerms); 

        try {
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.append(summary);
            myWriter.flush();
            myWriter.close();
            System.out.println("Successfully wrote to " + fileName);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    private void writeDocListSize(String fileName){
        try {
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.append(this.freqTable.toString());
            myWriter.flush();
            myWriter.close();
            System.out.println("Successfully wrote to " + fileName);
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
            System.out.println("Successfully wrote to " + fileName);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
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
            System.out.println("Successfully wrote to " + fileName);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    private void writeLookUps(String fileName){
        try {
            FileWriter myWriter = new FileWriter(fileName);
            for(int i=0;i<this.lookupCounts.length;i++){
                myWriter.append(Integer.toString(this.lookupCounts[i]) + "\n");
            }
            myWriter.flush();
            myWriter.close();
            System.out.println("Successfully wrote to " + fileName);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }



    public static void main(String[] args){

        int indexTimingUnit = 1000;
        int queryTimingUnit = 1000;
        int power = 2;  // the chance to include a term in the index and fingerprint is 1-1/2^power, eg. power=0, 100%, power=1, 50%, power=2, 75% chance
        double similarityThreshold = 0.5;
        int epoch = 1;
        String input_dir = "./input/";
        String output_dir = "./output/";


        Options options = new Options();
        options.addOption("iu", true, "indexing timing unit");
        options.addOption("qu", true, "querying timing unit");
        options.addOption("p", true, "power of two");
        options.addOption("s", true, "similarity threshold");
        options.addOption("n", true, "epoch");
        options.addOption("i", true, "input directory");
        options.addOption("o", true, "output directory");


        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        if(cmd.hasOption("iu")) {
            indexTimingUnit = Integer.parseInt(cmd.getOptionValue("iu"));
        }
        if(cmd.hasOption("qu")) {
            queryTimingUnit = Integer.parseInt(cmd.getOptionValue("qu"));
        }
        if(cmd.hasOption("p")) {
            power = Integer.parseInt(cmd.getOptionValue("p"));
        }
        if(cmd.hasOption("s")) {
            similarityThreshold = Double.parseDouble(cmd.getOptionValue("s"));
        }
        if(cmd.hasOption("n")) {
            epoch = Integer.parseInt(cmd.getOptionValue("n"));
        }
        if(cmd.hasOption("i")) {
            input_dir = cmd.getOptionValue("i");
        }
        if(cmd.hasOption("o")) {
            output_dir = cmd.getOptionValue("o");
        }
        

        InvertedIndex fileIndex = new InvertedIndex(indexTimingUnit, queryTimingUnit, power, similarityThreshold);
        fileIndex.initialize(input_dir + "query_tweets_stemmed.txt", input_dir +"indexing_tweets_stemmed.txt");
        // System.out.println(fileIndex.fingerprintSize);
        // System.out.println(fileIndex.fingerprintSize.size());
        fileIndex.runQueryList(fileIndex.queryList);

        String filenameTemplate = String.format("%sEpoch_%d_iu_%d_qu_%d_p_%d_s_%.2f", output_dir, epoch, indexTimingUnit, queryTimingUnit, power, similarityThreshold);
        if(epoch == 1){
            
            fileIndex.writeSummary(epoch, filenameTemplate + "_summary.csv");
            fileIndex.writeDocListSize(filenameTemplate + "_term_freq.csv");
            
            fileIndex.writeQueryResult(fileIndex.queryResults, filenameTemplate + "_query_result.csv");
            fileIndex.writeQueryResult(fileIndex.queryRestltSimilarity, filenameTemplate + "_query_similarity.csv"); 
            
            fileIndex.writeLookUps(filenameTemplate + "_lookups.csv"); 
        }
        
        fileIndex.writeTimeResult(fileIndex.indexTime, filenameTemplate + "_index_time.csv");
        fileIndex.writeTimeResult(fileIndex.queryTime, filenameTemplate + "_query_time.csv");
    }
}
