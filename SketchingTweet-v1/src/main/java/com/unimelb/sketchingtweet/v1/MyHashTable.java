package com.unimelb.sketchingtweet.v1;


import java.util.Deque;
import java.util.LinkedList;


public class MyHashTable {
//    TODO: customized linked list to replace deque
    private Pair<String, Deque<String>>[] table;
    private int size;
    private final int maxSize = 10000;
    
    
    public MyHashTable(){
        this.table = new Pair[this.maxSize];
        this.size = 0;
    }
    
    private int getIndex(String key){
        int index = Murmur3.hash32(key.getBytes()) % this.maxSize;
        index = index < 0 ? index * -1 : index;
//        System.out.println(index);
        return index;
    }
    
    
    public Deque<String> get(String key){
        int index = this.getIndex(key);
        
        if(this.table[index]==null){
            return null;
        }else{
            return this.table[index].getValue();
        }
    }
    
    
    public void put(String key, String value){
        int index = this.getIndex(key);
        if(this.table[index] == null){
            this.table[index] = new Pair(key, new LinkedList<String>());
            this.table[index].getValue().addFirst(value);
            this.size++;
        } else {
            this.table[index].getValue().addFirst(value);
        }
    }
    
    
    public int getSize(){
        return this.size;
    }
    
    public Pair<String, Deque<String>>[] getTable(){
        return this.table;
    }
}
