package com.unimelb.sketchingtweet.v1;


public class MyHashTable {
    // private Pair<String, MyHashNode<Integer>>[] table;
    private MyHashTableNode[] table;
    private int size;
    private final int maxSize = 16384; 
    
    
    public MyHashTable(){
        this.table = new MyHashTableNode[this.maxSize];
        this.size = 0;
    }
    

    private int getIndex(String term){
        int index = Murmur3.hash32(term.getBytes()) % this.maxSize;
        index = index < 0 ? index * -1 : index; 
//        System.out.println(index);
        return index;
    }
    
    
    public MyHashNode<Integer> getDocList(String term){
        int index = this.getIndex(term);
        
        if(this.table[index]==null){
            return null;
        }else{
            return this.table[index].getDocList();
        }
    }

    public int getDocListSize(String term){
        int index = this.getIndex(term);

        if(this.table[index]==null){
            return 0;
        }else{
            return this.table[index].getCount();
        }
    }
    
    
    public void put(String term, int docId){
        int index = this.getIndex(term);
        if(this.table[index] == null){
            this.table[index] = new MyHashTableNode(term, new MyHashNode<Integer>(docId)); 
            // new Pair(key, new MyHashNode(value));
            this.size++;
        } else {
            MyHashNode<Integer> head = this.table[index].getDocList();
            MyHashNode<Integer> newHead = new MyHashNode<Integer>(docId, head);
            this.table[index].incCount();
            this.table[index].setDocList(newHead);
        }
    }
    
    
    public int getSize(){
        return this.size;
    }

    public MyHashTableNode getTableNode(String term){
        int index = this.getIndex(term);
        return this.table[index];
    }
    
    public MyHashTableNode[] getTable(){
        return this.table;
    }
}
