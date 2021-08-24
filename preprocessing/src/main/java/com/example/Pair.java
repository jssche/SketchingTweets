package com.example;

public class Pair<K,V> {
    private K key;
    private V value;
    
    public Pair(){
        this.key = null;
        this.value = null;
    }
    
    public Pair(K key, V value){
        this.key = key;
        this.value = value;
    }
    
    public void setKey(K key){
        this.key = key;
    }
    
    public void setValue(V value){
        this.value = value;
    }
    
    public K getKey(){
        return this.key;
    }
    
    public V getValue(){
        return this.value;
    }
    
    public String toString(){
        return (this.key.toString() + " : " + this.value.toString());
    }
}
