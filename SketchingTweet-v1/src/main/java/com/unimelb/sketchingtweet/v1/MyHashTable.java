package com.unimelb.sketchingtweet.v1;


public class MyHashTable {
    private Pair<String, MyHashNode<Integer>>[] table;
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
    
    
    public MyHashNode<Integer> get(String key){
        int index = this.getIndex(key);
        
        if(this.table[index]==null){
            return null;
        }else{
            return this.table[index].getValue();
        }
    }
    
    
    public void put(String key, int value){
        int index = this.getIndex(key);
        if(this.table[index] == null){
            this.table[index] = new Pair(key, new MyHashNode(value));
            this.size++;
        } else {
            MyHashNode head = this.table[index].getValue();
            MyHashNode newHead = new MyHashNode(value, head);
            this.table[index].setValue(newHead);
        }
    }
    
    
    public int getSize(){
        return this.size;
    }
    
    public Pair<String, MyHashNode<Integer>>[] getTable(){
        return this.table;
    }
}
