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
    
    public K getKey(MyHashNode node){
        return this.key;
    }
    
    public MyHashNode<K> getNext(MyHashNode node){
        return this.next;
    }
}
