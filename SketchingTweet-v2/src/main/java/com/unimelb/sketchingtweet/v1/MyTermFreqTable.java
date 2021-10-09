package com.unimelb.sketchingtweet.v1;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class MyTermFreqTable {
    // private Pair<String, MyHashNode<Integer>>[] table;
    private Pair<Pair<String, Integer>,Integer>[] table;
    
    public MyTermFreqTable(int size, MyHashTable indexTable){
        this.table = new Pair[size];
        int index = 0;
        for(MyHashTableNode node: indexTable.getTable()){
            if(node != null){
                this.table[index] = new Pair<>(new Pair<>(node.getTerm(), node.getTermCount()), node.getCount());
                index++;
            }
        }
        
        Arrays.sort(this.table, new Comparator<Pair>() {
            @Override
            public int compare(Pair p1, Pair p2) {
                return p1.getValue().compareTo(p2.getValue());
            }
         });

        // System.out.println(this.table[0]);
        // System.out.println(this.table[this.size-1]);
    }
    

    public Pair<Pair<String, Integer>,Integer>[] getMyTermFreqTable(){
        return this.table;
    }


    public String toString(){
        String table = "";

        for(Pair<Pair<String, Integer>,Integer> entry: this.table){
            table = table + entry.getKey().getKey() + "," + entry.getKey().getValue() + ","  + entry.getValue() + "\n";
        }

        return table;
    }
}

