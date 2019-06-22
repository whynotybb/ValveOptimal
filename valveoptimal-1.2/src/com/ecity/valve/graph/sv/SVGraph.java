package com.ecity.valve.graph.sv;

import com.ecity.valve.Configuration;
import com.ecity.valve.exception.DataTypeNotMatchException;
import com.ecity.valve.graph.Graph;
import com.ecity.valve.graph.datastruct.HashTable;
import com.ecity.valve.graph.datastruct.Link;
import com.ecity.valve.graph.datastruct.SortedList;
import com.ecity.valve.graph.ne.NEGraph;
import com.ecity.valve.graph.ne.NEVertex;
import com.ecity.valve.graph.ne.NEdge;

import java.util.*;

import static java.lang.Math.min;

public class SVGraph extends Graph<SVertex,SVEdge> {
    private static final long serialVersionUID = -6778928582410769237L;
    public NEGraph graph;
    public Set<Integer> sources;
    private double totalDemand;
    private double totalLength;
    private Set<Integer> cutPoints;
    //求解SV图割点的变量
    private int parent[];
    private int low[];
    private int dfn[];
    private int count;

    public SVGraph() {
    }

    public SVGraph(NEGraph graph){
        super();
        this.graph=graph;
        graph.createIU();
        sources=graph.souIUs;
        Map<Integer,Set<Integer>> valvesMap=graph.getValvesToIUs();
        List<Integer> valves= graph.getValves();
        Map<Integer,Double> segDemandMap= graph.segDemandMap;
        Map<Integer,Double> segLengthMap= graph.segLengthMap;
        int nVerts=segDemandMap.size();
        for (int i = 0; i < nVerts; i++) {
            double length=segLengthMap.get(i);
            double demand=segDemandMap.get(i);
            totalDemand+=demand;
            totalLength+=length;
            addVertex(new SVertex(i,length,demand));
        }
        //根据识别关断单元的结果构建SV的边
        for (Integer valve:valves) {
            Set<Integer> segments= valvesMap.get(valve);
            //todo:segements 会出现null
            if (segments!=null&&segments.size()!=1){
                Iterator<Integer> iterator=segments.iterator();
                int start=iterator.next();
                int end=iterator.next();
                addEdge(new SVEdge(valve,start,end));
            }
        }
    }

    @Override
    public void addEdge(SVEdge svEdge) {
        super.addEdge(svEdge);
        adjTable.insert(svEdge.fromId,svEdge.endId,svEdge.id);
        adjTable.insert(svEdge.endId,svEdge.fromId,svEdge.id);
    }

    /**
     * 求解关断单元的欠缺供应量
     * @param iu 关断单元编号
     * @param isSingleSources 是否为单水源
     * @return 欠缺供应量
     */
    private double getAffectedDemandByIU(int iu,boolean isSingleSources){
        if(sources.contains(iu)&&isSingleSources){
            return totalDemand;
        }
//        非割点单元
        if (!cutPoints.contains(iu)){
            return vertexList.get(iu).getDemand();
        }
//        既不是水源单元也不是叶子单元
        double ld=0;
        //删除关断单元
        //删除以关断单元为终点的边
        List<Link> list=new ArrayList<>();
        Link current=adjTable.get(iu).first;
        while (current!=null){
            list.add(current);
//            adjTable.hashArray[current.id].delete(iu);
            adjTable.get(current.id).delete(iu);
            current=current.next;
        }
        //删除以关断单元为起点的边
        SortedList sortedList=copySortedList(adjTable.get(iu));
        adjTable.remove(iu);
        //从每个水源单元出发,搜索到的点是正常供应的
        Set<Integer> connectedSet=new HashSet<>();
        for (Integer source:sources){
            if (source==iu){
                continue;
            }
            connectedSet.addAll(getNormalSupplyFromSource(source,adjTable));
        }
        //计算水量
        for (Integer c:connectedSet) {
            ld+=vertexList.get(c).getDemand();
        }
        //恢复Adj
        adjTable.set(iu,sortedList);
        for (Link l:list){
            adjTable.insert(l.id,iu,l.valve);
        }
        return totalDemand-ld;
    }

    public double getLD(){
        double p;
        double ld = 0;
        int nVerts=vertexList.size();
        if (nVerts!=1){
            getCutPoints();
        }
        else {
            return totalDemand;
        }
        boolean isSingleSource=sources.size()==1;
        for(int k:vertexList.keySet()){
            p=vertexList.get(k).getLength()/totalLength;
            ld+= p*getAffectedDemandByIU(k,isSingleSource);
        }
        return ld;
    }
    /**
     * 隔离关断单元后的正常供水量
     * @param source 起点
     * @param adj 修改后的邻接表
     * @return 搜索到的顶点
     */
    private Set<Integer> getNormalSupplyFromSource(int source,HashTable adj) {
        Set<Integer> iSet=new HashSet<>();
        List<Integer> queue=new ArrayList<>();
        vertexList.get(source).setVisited(true);
        iSet.add(source);
        queue.add(source);
        int v2;
        while(!queue.isEmpty()) {
            int v1=queue.remove(0);
            while ((v2=getNextUnvisitedVertex(v1,adj))!=-1) {
                vertexList.get(v2).setVisited(true);
                iSet.add(v2);
                queue.add(v2);
            }
        }
        //reset iSet
        for (int v:iSet){
            vertexList.get(v).setVisited(false);
        }
        return iSet;
    }
    private int getNextUnvisitedVertex(int v,HashTable adj) {
        Link  current=adj.get(v).first;
        while(current!=null)
        {
            if (!vertexList.get(current.id).isVisited())
            {
                return current.id;
            }
            current=current.next;
        }
        return -1;
    }
    private SortedList copySortedList(SortedList oldList){
        SortedList newList=new SortedList();
        Link current=oldList.first;
        while (current!=null){
            newList.insert(current.id,current.valve);
            current=current.next;
        }
        return newList;
    }
    private void getCutPoints(){
        if(sources.size()==0){
            throw new DataTypeNotMatchException("水源结点个数为0，请检查Configuration中的结点类型");
        }
        int source=sources.iterator().next();
        cutPoints=new HashSet<>();
        parent=new int[Configuration.MAX_NODE_SIZE];
        low=new int[Configuration.MAX_NODE_SIZE];
        dfn=new int[Configuration.MAX_NODE_SIZE];
        count=0;
        findArc(source);
        for (int i:vertexList.keySet()){
            vertexList.get(i).setVisited(false);
        }
    }
    /**
     *求图的割点
     */
    private void findArc(int u) {
        int source=sources.iterator().next();
        parent[source]=-1;
        vertexList.get(u).setVisited(true);
        dfn[u] = low[u] = ++count;
        int children = 0;
        Link current = adjTable.get(u).first;//将与start连接的第一个顶点先访问，
        while (current != null) {
            int v = current.id;
            //(u,v)为树边
            if (!vertexList.get(v).isVisited()) {
                //记录结点u的孩子结点
                children++;
                parent[v] = u;
                findArc(v);
                low[u] = min(low[u], low[v]);
                //根节点，子树数大于1，为割点
                if (parent[u] == -1 && children > 1) {
                    cutPoints.add(u);
                    //	 System.out.println("割点集中添加了"+u);
                }
                //非根节点，子树的低位数大于父树的dfn
                else if (parent[u] != -1 && low[v] >= dfn[u]) {
                    cutPoints.add(u);
                }
            }
            //回边
            else if (v != parent[u]) {
                low[u] = min(low[u], dfn[v]);
            }
            current = current.next;
        }
    }

    public SVGraph copy(){
        NEGraph neGraph= graph.copy();
        SVGraph svGraph=new SVGraph();
        svGraph.sources=new HashSet<>(sources);
        svGraph.graph=neGraph;
        for (SVertex vertex:vertexList.values()){
            svGraph.addVertex(new SVertex(vertex.id,vertex.getLength(),vertex.getDemand()));
        }
        for (SVEdge edge:edgeList.values()){
            svGraph.addEdge(new SVEdge(edge.id,edge.fromId,edge.endId));
        }
        svGraph.totalDemand=totalDemand;
        svGraph.totalLength=totalLength;
        return svGraph;
    }
}
