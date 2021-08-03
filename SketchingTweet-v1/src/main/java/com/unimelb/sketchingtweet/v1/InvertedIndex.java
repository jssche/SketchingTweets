package com.unimelb.sketchingtweet.v1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import static java.lang.Math.ceil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.json.*;


public class InvertedIndex {
    
    private List<String> stopwords = Arrays.asList("rt", "a", "about", "above", "after", "again", "against", "ain", "all", "am", "an", "and", "any", "are", "aren", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can", "couldn", "couldn't", "d", "did", "didn", "didn't", "do", "does", "doesn", "doesn't", "doing", "don", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn", "hadn't", "has", "hasn", "hasn't", "have", "haven", "haven't", "having", "he", "her", "here", "hers", "herself", "him", "himself", "his", "how", "i", "if", "in", "into", "is", "isn", "isn't", "it", "it's", "its", "itself", "just", "ll", "m", "ma", "me", "mightn", "mightn't", "more", "most", "mustn", "mustn't", "my", "myself", "needn", "needn't", "no", "nor", "not", "now", "o", "of", "off", "on", "once", "only", "or", "other", "our", "ours", "ourselves", "out", "over", "own", "re", "s", "same", "shan", "shan't", "she", "she's", "should", "should've", "shouldn", "shouldn't", "so", "some", "such", "t", "than", "that", "that'll", "the", "their", "theirs", "them", "themselves", "then", "there", "these", "they", "this", "those", "through", "to", "too", "under", "until", "up", "ve", "very", "was", "wasn", "wasn't", "we", "were", "weren", "weren't", "what", "when", "where", "which", "while", "who", "whom", "why", "will", "with", "won", "won't", "wouldn", "wouldn't", "y", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves", "could", "he'd", "he'll", "he's", "here's", "how's", "i'd", "i'll", "i'm", "i've", "let's", "ought", "she'd", "she'll", "that's", "there's", "they'd", "they'll", "they're", "they've", "we'd", "we'll", "we're", "we've", "what's", "when's", "where's", "who's", "why's", "would");
    private MyHashTable table = null;
    private int numTweet = 0;
    
    
    private void initialize(){       
        this.table = new MyHashTable();
//        System.out.println(this.table.get("xss"));
//        this.table.put("thank", 0);
//        System.out.println(this.table.get("thank"));
//        this.table.put("thank", 52);
//        System.out.println(this.table.get("thank").getKey());
        
        Scanner inputStream = null; 
        try{
            inputStream = new Scanner(new FileInputStream("tweets.txt"));
        }
        catch(FileNotFoundException e){
            System.out.println("tweets.txt was not found");
        }
        while(inputStream.hasNextLine()){
            String line = inputStream.nextLine();
            this.addTweet(line);
        } 
//        System.out.println(this.table.getSize());
//        System.out.println(this.table.get("thank"));
    }
    
    
    private void addTweet(String line){
        String tweetID = null;
        ArrayList<String> textList = null;
         
        try {
            JSONObject jsonLine = new JSONObject(line);
            tweetID = jsonLine.getString("id_str");            
            textList = this.cleanTweet(jsonLine.getString("text"));            
            if(!textList.isEmpty()){
                numTweet++;
                Pair tweet = new Pair(numTweet, textList);
//                System.out.println(tweetID);
                System.out.println(tweet);
                for(int i=0;i<textList.size();i++){
                    this.table.put(textList.get(i), numTweet);
                }
            }
            textList.clear();
        } catch (JSONException e) {
//            System.out.println("not a json file");
        }
    }
    
    
    private ArrayList<String> cleanTweet(String text){
        String[] textArray = null;
        ArrayList<String> textList = new ArrayList();
        text = text.replaceAll("(@[A-Za-z0-9_-]+)|(http\\S+)|(https\\S+)|[^a-zA-Z0-9\\s]|[\\n\\t\\r]","")
                    .toLowerCase().trim();
        textArray = text.split(" ",0);
        for(int i=0; i<textArray.length; i++){
            if(!textArray[i].isBlank()){
                textList.add(textArray[i].trim());
            }
        }
        textList.removeAll(this.stopwords);
        return textList;
    }
    
    
    public String findSimilarTweet(String newTweet, double similarity){
//      get the document id list for each of the word in the new tweet
        int totalWords;
        ArrayList<String> newTweetList = new ArrayList();
        
        newTweetList = this.cleanTweet(newTweet);
        System.out.println(newTweetList);
        totalWords = newTweetList.size();
        
        MyHashNode[] wordIndex = new MyHashNode[totalWords];
        for(int i=0;i<totalWords;i++){
            wordIndex[i] = this.table.get(newTweetList.get(i));
            System.out.println(wordIndex[i]);
        }
        
//      linear search
        int latestDocID = 0;
        ArrayList<Integer> latestDocIndex = new ArrayList<Integer>();
        int docCountBenchmark = (int)ceil(similarity * totalWords);
//        System.out.println(docCountBenchmark);
        int nullCount = 0;
         
        while(true){
//          latestDocID stores the max document id (the larger doc id is, the newer the doc is)
//          latestDocIndex stores the index of the words in the new tweet that has the max doc id at front of the list/also appeared in the latest doc
            for(int i=0;i<totalWords;i++){
                if(wordIndex[i]!=null){
                    if((int)wordIndex[i].getKey() > latestDocID){
                        latestDocID = (int)wordIndex[i].getKey();
                        latestDocIndex.clear();
                        latestDocIndex.add(i);
                    }else if((int)wordIndex[i].getKey() == latestDocID){
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
                return "No similar tweet";
            }else{
                if(latestDocIndex.size() >= docCountBenchmark){
                    return Integer.toString(latestDocID);
                }else{
                    for(int i=0;i<latestDocIndex.size();i++){
                        wordIndex[latestDocIndex.get(i)] = wordIndex[latestDocIndex.get(i)].getNext();
                    }
                    latestDocIndex.clear();
                    latestDocID = 0;
                    nullCount = 0;
                }
            }              
        }        
    }
    
    
    public static void main(String[] arg){
        InvertedIndex fileIndex = new InvertedIndex();
        fileIndex.initialize();
        System.out.println("doc id: " + fileIndex.findSimilarTweet("sxx sxx xxx", 0.5));
        System.out.println("doc id: " + fileIndex.findSimilarTweet("Latest level: 0.8815m at 31/03/2016 23:00:00(GMT). Further information available at https://t.co/Jy7C3IU3nQ #riverlevels", 0.5));
        System.out.println("doc id: " + fileIndex.findSimilarTweet("spend more time trying to change someone and less time trying to love and understand them xxx", 0.5));
        System.out.println("doc id: " + fileIndex.findSimilarTweet("Get Weather Updates from The Weather Channel", 1));
    }
}
