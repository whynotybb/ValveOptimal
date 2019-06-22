package com.ecity.valve.graph.datastruct;

import java.io.Serializable;

public class SortedList implements Serializable{
    private static final long serialVersionUID = 1917004098254436032L;
    public Link first;
    public SortedList() {
        first=null;
    }
    public void insert(int key) {
        Link previous=null;
        Link current=first;
        while(current!=null&&current.getId()<key) {
            previous=current;
            current=current.next;
        }
        Link theLink=new Link(key);
        if (previous==null) {
            first=theLink;
        }
        else {
            previous.next=theLink;
        }
        theLink.next=current;
    }
    public void insert(int key,double length) {
        Link previous=null;
        Link current=first;
        while(current!=null&&current.getId()<key) {
            previous=current;
            current=current.next;
        }
        Link theLink=new Link(key,length);
        if (previous==null) {
            first=theLink;
        }
        else {
            previous.next=theLink;
        }
        theLink.next=current;
    }
    public void insert(int key,int valve) {
        Link previous=null;
        Link current=first;
        while(current!=null&&current.getId()<key) {
            previous=current;
            current=current.next;
        }
        Link theLink=new Link(key,valve);
        if (previous==null) {
            first=theLink;
        }
        else {
            previous.next=theLink;
        }
        theLink.next=current;
    }
    public void insert(int key,int valve,double length) {
        Link previous=null;
        Link current=first;
        while(current!=null&&current.getId()<key) {
            previous=current;
            current=current.next;
        }
        Link theLink=new Link(key,valve,length);
        if (previous==null) {
            first=theLink;
        }
        else {
            previous.next=theLink;
        }
        theLink.next=current;
    }
    public void delete(int key) {
        Link previous=null;
        Link current=first;
        while(current!=null&&current.getId()<key) {
            previous=current;
            current=current.next;
        }
        if (first==null) {
            return;
        }
        if (current==first) {
            first=first.next;
        }
        else if (current!=null&&previous!=null){
            previous.next=current.next;
        }
    }
    public void delete(int key,int value) {
       //先线性查找
        Link previous=null;
        Link current=first;
        while (current!=null&&(current.getId()<key||(current.getId()==key&&current.valve!=value))){
            previous=current;
            current=current.next;
        }
        //如果没有找到
        if (first==null||current==null||current.id>key){
            return;
        }
        if (current==first){
            first=first.next;
        }
        else if (previous!=null){
            previous.next=current.next;
        }
    }
    public void deleteAll() {
        first=null;
    }
    public Link find(int key) {
        Link current=first;
        while(current!=null&&current.getId()<=key) {
            if (current.getId()==key) {
                return current;
            }
            current=current.next;
        }
        return null;
    }

    public boolean contains(int key) {
        Link current=first;
        while(current!=null&&current.getId()<=key) {
            if (current.getId()==key) {
                return true;
            }
            current=current.next;
        }
        return false;
    }

}
