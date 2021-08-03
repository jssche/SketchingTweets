package com.unimelb.sketchingtweet.v1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
                Pair tweet = new Pair(numTweet, textList);
//                System.out.println(tweetID);
//                System.out.println(tweet);
                for(int i=0;i<textList.size();i++){
                    this.table.put(textList.get(i), tweetID);
                }
            }
            textList.clear();
            numTweet++;
        } catch (JSONException e) {
            System.out.println("not a json file");
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
    
    
    public String findSimilarTweet(String newTweet){
        ArrayList<String> newTweetList = new ArrayList();
        newTweetList = this.cleanTweet(newTweet);
        System.out.println(newTweetList);
        return "";
    }
    
    
    public static void main(String[] arg){
        InvertedIndex fileIndex = new InvertedIndex();
        fileIndex.initialize();
        fileIndex.findSimilarTweet("spend more time trying to change someone and less time trying to love and understand them");

    }
}
