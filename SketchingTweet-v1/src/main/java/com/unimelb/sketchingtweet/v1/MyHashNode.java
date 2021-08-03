package com.unimelb.sketchingtweet.v1;

public class MyHashNode<K> {
    private K key;
    private MyHashNode<K> next;
    
    public MyHashNode() {
        this.key = null;
        this.next = null;
    }
    
    public MyHashNode(K key){
        this.key = key;
        this.next = null;
    }
    
     public MyHashNode(K key, MyHashNode<K> next){
        this.key = key;
        this.next = next;
    }
    
    public K getKey(){
        return this.key;
    }
    
    public MyHashNode<K> getNext(){
        return this.next;
    }
    
    public String toString(){
        return (this.key.toString());
    }
    
    public boolean hasNext(){
        return this.next != null;
    }
}
