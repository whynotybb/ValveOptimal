package com.ecity.valve.graph.ne;

import com.ecity.valve.Configuration;
import com.ecity.valve.graph.datastruct.HashTable;
import com.ecity.valve.graph.datastruct.Link;
import com.ecity.valve.graph.gis.GISEdge;
import com.ecity.valve.graph.gis.GISGraph;
import com.ecity.valve.graph.gis.GISVertex;

import java.util.*;

/*
GIS数据模型简化生成NE图,即保留水源，阀门和连接点以及度不为2的其余节点
将度为2的多余顶点连接的两条边抽象为一条边。
 */
public class NEGraphFactory {
    private static Random LOCAL_RANDOM=new Random();
    public static NEGraph build(GISGraph gisGraph) throws Exception{
        NEGraph neGraph=new NEGraph();
        Set<Integer> nodes=new HashSet<>();
        gisGraph.getSourceComp(Configuration.SOURCE_NODE,nodes);
        System.out.println("连通结点个数:"+nodes.size());
        Map<Integer,List<Integer>> redundantMap=new HashMap<>();
        for (Integer node:nodes){
            if (!isRedundantVertex(node,gisGraph)){
                addVertexToList(gisGraph,neGraph,node);
            }
            else {
                redundantMap.put(node,getTwoEdges(node,gisGraph.adjTable));
            }
        }
        //备份一个edges,记录合并后的边
        Set<Integer> records=copySet(gisGraph.edgeList.keySet());
        //此处有死循环
        for (int line:gisGraph.edgeList.keySet()){
            if (nodes.contains(gisGraph.edgeList.get(line).fromId)&&nodes.contains(gisGraph.edgeList.get(line).endId)){
                //redundantMap:冗余点连接的两条边
                if(containsLine(redundantMap,line)&&records.contains(line)){
                     List<Integer> eList=new ArrayList<>();
                     int nodesOfLine[]= getNodesOnLine(line,redundantMap,gisGraph,eList);
                     double length=0;
                     for (Integer i:eList){
                          length+=gisGraph.edgeList.get(i).getLength();
                     }
                     neGraph.addEdge(new NEdge(line,nodesOfLine[0],nodesOfLine[1],length));
                     Configuration.MAX_NE_EDGE_ID=Configuration.MAX_NE_EDGE_ID>line?Configuration.MAX_NE_EDGE_ID:line;
                     records.removeAll(eList);
                 }else if (records.contains(line)){
                     GISEdge edge = gisGraph.edgeList.get(line);
                     neGraph.addEdge(new NEdge(edge.id, edge.fromId, edge.endId, edge.getLength()));
                     Configuration.MAX_NE_EDGE_ID=Configuration.MAX_NE_EDGE_ID>edge.id?Configuration.MAX_NE_EDGE_ID:edge.id;
                 }
            }
        }
        System.out.println("[NE边规模]"+neGraph.edgeList.size());
        return neGraph;
    }
    private static void addVertexToList(GISGraph gisGraph, NEGraph neGraph, Integer v) {
        GISVertex gisVertex=gisGraph.vertexList.get(v);
        int lines=gisGraph.adjTable.getListSize(v);
        if (lines==1) {
            if (Configuration.DEMAND_CREATE_MODE == 1) {
                gisVertex.setDemand(LOCAL_RANDOM.nextInt(100));
            }
            else if (Configuration.DEMAND_CREATE_MODE==2){
                gisVertex.setDemand(Configuration.NODE_DEMAND_DEFAULTVALUE);
            }
        }
        Configuration.MAX_NE_NODE_ID=Configuration.MAX_NE_NODE_ID>gisVertex.id?
                Configuration.MAX_NE_NODE_ID:gisVertex.id;
        neGraph.addVertex(new NEVertex(gisVertex.id,gisVertex.getDno(),lines,gisVertex.getDemand()));
    }
    //获取冗余顶点相连的边
    private static List<Integer> getTwoEdges(int v, HashTable adj){
        List<Integer> res=new ArrayList<>();
        Link first= adj.get(v).first;
        res.add(first.valve);
        res.add(first.next.valve);
        return res;
    }
    private static boolean isRedundantVertex(int v,GISGraph graph){
        int lines=graph.adjTable.getListSize(v);
        return lines==2&&graph.vertexList.get(v).getDno()!=Configuration.TYPE_LINK_NODE
                &&graph.vertexList.get(v).getDno()!=Configuration.TYPE_VALVE
                &&graph.vertexList.get(v).getDno()!=Configuration.TYPE_SOURCE;
    }
    private static boolean containsLine(Map<Integer,List<Integer>> map,Integer line){
        for (List<Integer> l:map.values()){
            if (l.contains(line)){
                return true;
            }
        }
        return false;
       // return map.values().stream().anyMatch(a->a.contains(line));
    }
    private static Set<Integer> copySet(Set<Integer> set){
        if (set==null) return null;
        return new HashSet<>(set);
    }
    //通过多余管线找出与之相连的其余多余管线，可能不止两条，
    // 并且返回这些多余边。用于求解合并后边的长度
    private static int[] getNodesOnLine(int line,Map<Integer,List<Integer>> map,GISGraph gisGraph,List<Integer> eList){
        int node1,node2;
        int key;
        eList.add(line);
        GISEdge edge= gisGraph.edgeList.get(line);
        if (map.containsKey(edge.fromId)){
            //起点是冗余结点
            key=edge.fromId;
            node1=edge.endId;
        }else {
            key=edge.endId;
            node1=edge.fromId;
        }
        //获取与冗余结点相连的另一条边
        List<Integer> edges= map.get(key);
        int otherLine=(edges.get(0)==edge.id?edges.get(1):edges.get(0));
        eList.add(otherLine);
        GISEdge otherEdge= gisGraph.edgeList.get(otherLine);
        node2=otherEdge.fromId==key?otherEdge.endId:otherEdge.fromId;
        while (isRedundantVertex(node1,gisGraph)){
            node1 = getNewNode(gisGraph, eList, node1);
        }
        //todo bug
        while (isRedundantVertex(node2,gisGraph)){
            node2= getNewNode(gisGraph,eList,node2);
        }
        return new int[]{node1,node2};
    }
    //eList记录了访问过的边
    private static int getNewNode(GISGraph gisGraph, List<Integer> eList, int node1) {
        //node1一定是度为2,获取与node1相连的两个结点 59 60 61
        List<Integer> nodes= getNodesConnectedWithR(node1,gisGraph.adjTable);
        //由nodes中筛选出没有访问过的点
        List<Integer> nodes1=new ArrayList<>();
        for (int e:eList){
            nodes1.add(gisGraph.edgeList.get(e).fromId);
            nodes1.add(gisGraph.edgeList.get(e).endId);
        }
        int newNode = nodes1.contains(nodes.get(0))?nodes.get(1):nodes.get(0);
        int newEdge = gisGraph.adjTable.get(node1).find(newNode).valve;
        eList.add(newEdge);
        node1=newNode;
        return node1;
    }
    //获取与冗余结点直接相连的两个结点
    private static List<Integer> getNodesConnectedWithR(int node,HashTable adj){
        List<Integer> list=new ArrayList<>();
        list.add(adj.get(node).first.id);
        list.add(adj.get(node).first.next.id);
        return list;
    }
    //管网简化
    public static NEGraph build1(GISGraph gisGraph){
        //直接读取建立
        Map<Integer,GISVertex> vertexMap= gisGraph.vertexList;
        Map<Integer,GISEdge> edgeMap=gisGraph.edgeList;
        NEGraph neGraph=new NEGraph();
        for (GISVertex vertex:vertexMap.values()){
            neGraph.addVertex(new NEVertex(vertex.id,vertex.getDno(),gisGraph.adjTable.getListSize(vertex.id),vertex.getDemand()));
        }
        for (GISEdge edge:edgeMap.values()){
            neGraph.addEdge(new NEdge(edge.id,edge.fromId,edge.endId,edge.getLength()));
        }
        return neGraph;
    }
}
