package com.ecity.valve.graph;

import com.ecity.valve.Configuration;
import com.ecity.valve.graph.datastruct.HashTable;
import java.util.HashMap;
import java.util.Map;

/**
 * 图模型的父类
 * V:是指顶点
 * E:是指边
 */
public class Graph<V extends Vertex,E extends Edge>{
    public Map<Integer,V> vertexList;
    public Map<Integer,E> edgeList;
    public HashTable adjTable;
    public Graph(){
        vertexList=new HashMap<>();
        edgeList=new HashMap<>();
        adjTable=new HashTable(Configuration.MAX_NODE_SIZE);
    }
    public void addVertex(V v){
        vertexList.put(v.id,v);
    }
    public void addEdge(E e){
        edgeList.put(e.id,e);
    }
}
