package com.ecity.valve.graph.gis;

import com.ecity.valve.exception.TopoException;
import com.ecity.valve.graph.Graph;
import com.ecity.valve.graph.datastruct.Link;
import java.util.*;

//gis管网模型
public class GISGraph extends Graph<GISVertex,GISEdge> {
    private static final long serialVersionUID = -7649905740039137023L;
    public void getSourceComp(int from, Set<Integer> nodeSet) throws Exception{
        List<Integer> queue=new ArrayList<>();
        try {
            vertexList.get(from).setVisited(true);
        }catch (Exception e){
            throw new TopoException("数据拓扑有错误，水源结点错误,请重新配置水源结点");
        }
        nodeSet.add(from);
        queue.add(from);
        int v2;
        while(!queue.isEmpty()) {
            int v1=queue.remove(queue.size()-1);
            while ((v2=getNextUnvisitedVertex(v1))!=-1) {
                vertexList.get(v2).setVisited(true);
                nodeSet.add(v2);
                queue.add(v2);
            }
        }
        //reset
        for (GISVertex vertex:vertexList.values()){
            vertex.setVisited(false);
        }
    }
    private int getNextUnvisitedVertex(int v) {
        Link current=adjTable.get(v).first;
        while(current!=null) {
            if (!vertexList.get(current.id).isVisited())
            {
                return current.id;
            }
            current=current.next;
        }
        return -1;
    }
    public void addVertex( GISVertex gisVertex) {
        super.addVertex(gisVertex);
    }
    public void addEdge(GISEdge gisEdge) {
        super.addEdge(gisEdge);
        adjTable.insert(gisEdge.fromId,gisEdge.endId,gisEdge.id);
        adjTable.insert(gisEdge.endId,gisEdge.fromId,gisEdge.id);
    }
    private Map<Integer,Set<Integer>> getConnectedComponent(){
        Map<Integer,Set<Integer>> conMap=new HashMap<>();
        int from=0;
        int i=0;
        while (conMap.isEmpty()||isWholeVertexVisited(conMap)){
            while (vertexList.get(from)==null||visited(conMap,from)){
                from++;
            }
            conMap.put(i++,bfs(from++));
        }
        return conMap;
    }
    private boolean isWholeVertexVisited(Map<Integer,Set<Integer>> map){
        int nVerts=0;
        for (Set<Integer> set:map.values()){
            nVerts+=set.size();
         }
         return nVerts==vertexList.size();
    }
    private boolean visited(Map<Integer,Set<Integer>> map,Integer node){
        for (Set<Integer> set:map.values()){
            if (set.contains(node)){
                return true;
            }
        }
        return false;
//        return map.values().stream().anyMatch(s->s.contains(node));
    }
    //数据连通性分析：
    //1,如果是连通的，直接调用一次bfs确定true即可
    //2,如果是不连通的，则需返回最大联通分量的顶点集合
    public int analysisConnectivity(){
         Map<Integer,Set<Integer>> connectedComponent= getConnectedComponent();
         if (connectedComponent.size()==1){
             System.out.println("数据连通");
             return connectedComponent.size();
         }
         System.out.println("数据连通性较差,共有连通分量："+connectedComponent.size()+"个，各连通分量顶点数如下");
         int max=0;
         Set<Integer> maxConnectedComponent=null;
         for (int key:connectedComponent.keySet()){
             Set<Integer> set= connectedComponent.get(key);
             if (max<set.size()){
                 max=set.size();
                 maxConnectedComponent=set;
             }
             System.out.println(key+":"+set.size());
             }
             return maxConnectedComponent.size();
    }

    private Set<Integer> bfs(int from){
        Set<Integer> nodeSet=new HashSet<>();
        List<Integer> queue=new ArrayList<>();
        vertexList.get(from).setVisited(true);
        nodeSet.add(from);
        queue.add(from);
        int v2;
        while(!queue.isEmpty()) {
            int v1=queue.remove(queue.size()-1);
            while ((v2=getNextUnvisitedVertex(v1))!=-1) {
                vertexList.get(v2).setVisited(true);
                nodeSet.add(v2);
                queue.add(v2);
            }
        }
        //reset
        for (GISVertex vertex:vertexList.values()){
            vertex.setVisited(false);
        }
        return nodeSet;
    }
}
