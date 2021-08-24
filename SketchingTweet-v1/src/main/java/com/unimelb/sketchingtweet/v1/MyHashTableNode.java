package com.unimelb.sketchingtweet.v1;

public class MyHashTableNode {
    private String term;
    private int count;
    private MyHashNode<Integer> docList;

    public MyHashTableNode(){
        this.term = null;
        this.count = 0;
        this.docList = null;
    }

    public MyHashTableNode(String term, MyHashNode<Integer> docList){
        this.term = term;
        this.count = 1;
        this.docList = docList;
    }

    public String getTerm(){
        return this.term;
    }

    public int getCount(){
        return this.count;
    }

    public MyHashNode<Integer> getDocList(){
        return this.docList;
    }

    public void setTerm(String term){
        this.term = term;
    }

    public void incCount(){
        this.count ++ ;
    }

    public void setDocList(MyHashNode<Integer> docList){
        this.docList = docList;
    }

}
