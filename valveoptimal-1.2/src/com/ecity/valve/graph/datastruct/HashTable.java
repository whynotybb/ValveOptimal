package com.ecity.valve.graph.datastruct;

import com.ecity.valve.exception.AdjTableTooSmallException;

import java.io.Serializable;

public class HashTable implements Serializable{
    private static final long serialVersionUID = -7228367420865538452L;
    private SortedList[] hashArray;
    private int arraySize;
    public HashTable(int s) {
        arraySize=s;
        hashArray=new SortedList[arraySize];
        for(int i=0;i<arraySize;i++) {
            hashArray[i]=new SortedList();
        }
    }
    public void remove(int id){
        hashArray[id].deleteAll();
    }
    public void remove(int id,int key){
        hashArray[id].delete(key);
    }
    public void remove(int id,int key,int value){
        hashArray[id].delete(key,value);
    }
    public void insert(int id,int key) {
        hashArray[id].insert(key);
    }
    public void insert(int id,int key,double length) {
        hashArray[id].insert(key,length);
    }
    public void insert(int fromId,int endId,int valve, double length) {
        if (fromId>=arraySize){
            throw  new AdjTableTooSmallException("MAX_NODE_SIZE参数配置太小，请设置为顶点规模的三倍");
        }
        hashArray[fromId].insert(endId,valve,length);
    }

    public void insert(int id,int key,int value) {
        if (id>=arraySize){
            throw  new AdjTableTooSmallException("MAX_NODE_SIZE参数配置太小，请设置为顶点规模的三倍");
        }
        hashArray[id].insert(key,value);
    }
    public int getListSize(int v){
        int i=0;
        Link current=hashArray[v].first;
        while (current!=null){
            i++;
            current=current.next;
        }
        return i;
    }
    public SortedList get(int id){
        return hashArray[id];
    }
    public void set(int id,SortedList sortedList){
        hashArray[id]=sortedList;
    }
}
