package com.ecity.valve.util;

import com.ecity.valve.Configuration;
import com.ecity.valve.graph.Edge;
import com.ecity.valve.graph.datastruct.Link;
import com.ecity.valve.graph.datastruct.SortedList;
import com.ecity.valve.graph.ne.NEGraph;
import com.ecity.valve.graph.ne.NEVertex;
import com.ecity.valve.graph.ne.NEdge;
import com.ecity.valve.graph.sv.SVEdge;
import com.ecity.valve.graph.sv.SVGraph;
import com.ecity.valve.graph.sv.SVertex;
import java.util.*;

/*
 * 用于将Individual解码为增阀方案，添加阀门之后拓扑关系处理。
 */
public class TopoUtil {
    public static void addEdge(NEGraph graph, int key, int vertexId, int edgeId) {
        Edge edge = graph.edgeList.get(key >> 1);
        //在靠近起点处添加阀门+-nEdge-+-------key/2-------+
        if ((key & 1) == 0) {
            graph.addEdge(new NEdge(edgeId, edge.getFromId(), vertexId, 0));
        }
        //在终点处添加阀门+-------key/2---------+--nEdge-+
        else {
            graph.addEdge(new NEdge(edgeId, vertexId, edge.getEndId(), 0));
        }
        updateEdge(graph, key, vertexId);
    }

    public static void addVertex(NEGraph graph, int vertexId) {
        graph.addVertex(new NEVertex(vertexId, Configuration.TYPE_VALVE, 2, 0));
    }

    private static void updateEdge(NEGraph graph, int key, int vertexId) {
        int edgeId=key>>1;
        NEdge edge = graph.edgeList.get(edgeId);
        //在邻接表中断开边key/2的联系
        graph.adjTable.remove(edge.fromId,edge.endId);
        graph.adjTable.remove(edge.endId,edge.fromId);
        //+---+------key/2------------+
        if ((key & 1) == 0) {
            graph.adjTable.insert(vertexId,edge.endId,edgeId,edge.getLength());
            graph.adjTable.insert(edge.endId,vertexId,edgeId,edge.getLength());
            edge.setFromId(vertexId);
            edge.setLength(edge.getLength());
        }
        //+---------key/2----------+---+
        else {
            graph.adjTable.insert(edge.fromId,vertexId,edgeId,edge.getLength());
            graph.adjTable.insert(vertexId,edge.fromId,edgeId,edge.getLength());
            edge.setEndId(vertexId);
            edge.setLength(edge.getLength());
        }
    }
    /**
     * 新增阀门后关断单元分裂操作
     * @param svGraph     需要修改的SVGraph
     * @param geneKey 阀门所在的管线位置编号
     */
    public static void splitSegment(SVGraph svGraph, int geneKey) {
        NEGraph graph = svGraph.graph;
        //get a new id for newSeg
        int nSegs = getNewIndex(svGraph.vertexList,2);
        //得到未添加阀门之前该管段所属关断单元
        int[] pipesToSeg= graph.pipesToSeg;
        int oldSeg =pipesToSeg[geneKey>>1];
        //新增阀门之后NE图新增一个顶点和一条边
        int vertexId = getNewIndex(graph.vertexList,0);
        int edgeId = getNewIndex(graph.edgeList,1);
        addVertex(graph, vertexId);
        addEdge(graph, geneKey, vertexId, edgeId);
        Set<Integer> pipesInSeg = new HashSet<>();
        Map<Integer, Integer> valves = new HashMap<>();
        //从阀门所连接的长边开始搜索，将搜索到的关断单元作为新单元，所以长边属于新单元，新增阀门后的短边属于oldSeg
        double[] ld=new double[2];
        graph.getSegment(geneKey,pipesInSeg, valves,ld);
        if (valves.get(vertexId) != 2) {
            Set<Integer> sous = svGraph.sources;
            //newSge is new Source Segment
            if (valves.get(-1) != null) {
                sous.remove(oldSeg);
                sous.add(nSegs);
                valves.remove(-1);
            }
            double oldDemand = svGraph.vertexList.get(oldSeg).getDemand();
            double oldLength = svGraph.vertexList.get(oldSeg).getLength();
            //获取分类后nSeg关断单元用水量和长度信息
            // new SV vertex nSegs &edge
            svGraph.addVertex(new SVertex(nSegs, ld[1], ld[0]));
            svGraph.addEdge(new SVEdge(vertexId, oldSeg, nSegs));
            //modify sv old vertex
            svGraph.vertexList.get(oldSeg).setLength(oldLength - ld[1]);
            svGraph.vertexList.get(oldSeg).setDemand(oldDemand - ld[0]);
            //modify sv old vertex info
            graph.segPipesMap.get(oldSeg).removeAll(pipesInSeg);
            graph.segPipesMap.put(nSegs, pipesInSeg);
            for (Integer p:pipesInSeg)
                pipesToSeg[p]=nSegs;
            valves.remove(vertexId);
            transferOldSegToNewSeg(svGraph, nSegs, oldSeg, valves);
        }
        graph.segPipesMap.get(oldSeg).add(edgeId);
        pipesToSeg[edgeId]=oldSeg;
    }

    private static void transferOldSegToNewSeg(SVGraph svGraph, int nSegs, int oldSeg, Map<Integer, Integer> valves) {
        for (Integer valve : valves.keySet()){
            if (valves.get(valve) == 2) {
                continue;
            }
            // 之前的内部阀门可能会变为边界阀
            if(svGraph.edgeList.get(valve) == null) {
                //oldSeg-nSegs:valve
                svGraph.addEdge(new SVEdge(valve, oldSeg, nSegs));
            } else {
                int seg1 = svGraph.edgeList.get(valve).fromId;
                int seg2 = svGraph.edgeList.get(valve).endId;
                //dSeg is valve connected & not splited;
                int dSeg = seg1 == oldSeg ? seg2 : seg1;
                //these edges connected with oldSeg ,but now connected with newSegs
                  svGraph.adjTable.remove(dSeg,oldSeg,valve);
                  svGraph.adjTable.remove(oldSeg,dSeg,valve);
                  svGraph.adjTable.insert(dSeg,nSegs,valve);
                  svGraph.adjTable.insert(nSegs,dSeg,valve);
                  if (svGraph.edgeList.get(valve).fromId==dSeg){
                      svGraph.edgeList.get(valve).endId=nSegs;
                  }else {
                      svGraph.edgeList.get(valve).fromId=nSegs;
                  }
            }
        }
    }

    //由geneKey获得对应的阀门编号
    private static int getValveOfGeneKey(NEGraph graph, int geneKey){
        int oldEdge = geneKey >> 1;
        int fromId = graph.edgeList.get(oldEdge).getFromId();
        int endId = graph.edgeList.get(oldEdge).getEndId();
        //find the other edge of fromid connected with
        if (graph.vertexList.get(fromId).getNodetype() == Configuration.TYPE_VALVE && graph.vertexList.get(endId).getNodetype() == Configuration.TYPE_VALVE) {
//            F---V------oldEdge-----V---E
            int otherEdgeF = getOtherEdge(oldEdge, graph, fromId);
            if ((geneKey & 1) == 0)
                return graph.edgeList.get(otherEdgeF).endId;
            int otherEdgeE = getOtherEdge(oldEdge, graph, endId);
            return graph.edgeList.get(otherEdgeE).getFromId();
        } else if (graph.vertexList.get(fromId).getNodetype() == Configuration.TYPE_VALVE) {
//             F--otherEdge-V------------oldEdge------------E
            int otherEdge = getOtherEdge(oldEdge, graph, fromId);
            return graph.edgeList.get(otherEdge).endId;
        } else if (graph.vertexList.get(endId).getNodetype() == Configuration.TYPE_VALVE) {
            int otherEdge = getOtherEdge(oldEdge, graph, endId);
            return graph.edgeList.get(otherEdge).fromId;
        } else {
            System.out.println("程序出错:[" + oldEdge + "的边起始点和终止点都不为阀门]");
            return -1;
        }
    }

    private static int getOtherEdge(int oldEdge, NEGraph graph, int node) {
        Link first = graph.adjTable.get(node).first;
        return first.valve == oldEdge ? first.next.valve : first.valve;
    }

    /**
     * 合并关断单元
     * 1，判断阀门移除之后会不会发生合并
     * 2，如果会合并
     * 2.1 移除新关断单元
     * 2.2 将与新单元相连的阀门移接到旧关断单元
     */
    public static void mergeSegment(SVGraph svGraph, int geneKey) {
        NEGraph graph = svGraph.graph;
        int removeValve = getValveOfGeneKey(graph, geneKey);
        int removePipe = getRemovePipe(graph.adjTable.get(removeValve), geneKey);
        //1, 判断阀门移除后需不需要合并关断单元
        Map<Integer,Set<Integer>> pipesInSegMap = graph.segPipesMap;
        int oldSeg;
        SVEdge svEdge = svGraph.edgeList.remove(removeValve);
        int[] pipesToSeg= graph.pipesToSeg;
        if (svEdge!=null){
            //获取要合并的两个关断单元
            int from = svEdge.getFromId();
            int end = svEdge.getEndId();
            oldSeg = (from < end) ? from : end;
            int newSeg = (from == oldSeg ? end : from);
            // 如果有一个关断单元是水源，则合并之后的关断单元是水源
            Set<Integer> sous = svGraph.sources;
            if (sous.contains(newSeg)){
                sous.remove(newSeg);
                sous.add(oldSeg);
            }
            SVertex oldV = svGraph.vertexList.get(oldSeg);
            SVertex newV = svGraph.vertexList.get(newSeg);
            double demand = oldV.getDemand() + newV.getDemand();
            double length = oldV.getLength() + newV.getLength();
            oldV.setDemand(demand);
            oldV.setLength(length);
            modifyNewSegToOldSeg(svGraph, oldSeg, newSeg);
            svGraph.adjTable.remove(newSeg);
            svGraph.vertexList.remove(newSeg);
            //合并后的两个关断单元
            Set<Integer> pipesInNewSeg= pipesInSegMap.remove(newSeg);
            graph.segPipesMap.get(oldSeg).addAll(pipesInNewSeg);
            for (int p:pipesInNewSeg)
                pipesToSeg[p]=oldSeg;
        }else {
            oldSeg=pipesToSeg[geneKey>>1];
        }
        graph.segPipesMap.get(oldSeg).remove(removePipe);
        pipesToSeg[removePipe]=-1;
        updateAfterRemoveValve(graph, removeValve, geneKey, removePipe);
    }
    private static void modifyNewSegToOldSeg(SVGraph svGraph, int oldSeg, int newSeg) {
        SortedList connectedWithNewSeg = svGraph.adjTable.get(newSeg);
        Link link = connectedWithNewSeg.first;
        while (link != null) {
            //移除
            if (link.id==oldSeg){
                svGraph.edgeList.remove(link.valve);
            }else {
                if (svGraph.edgeList.get(link.valve).fromId==newSeg){
                    svGraph.edgeList.get(link.valve).fromId=oldSeg;
                }else {
                    svGraph.edgeList.get(link.valve).endId=oldSeg;
                }
                svGraph.adjTable.insert(link.id,oldSeg,link.valve);
                svGraph.adjTable.insert(oldSeg,link.id,link.valve);
            }
            svGraph.adjTable.remove(link.id, newSeg, link.valve);
            link = link.next;
        }
    }

    //修改NE拓扑关系,恢复原来的起点和终点关系
    //F--------l------------V---r--E
    private static void updateAfterRemoveValve(NEGraph graph, int node, int geneKey, int removePipe) {
        //remove the short Edge
        NEdge removedEdge = graph.edgeList.remove(removePipe);
        graph.adjTable.remove(removedEdge.fromId,removedEdge.endId);
        graph.adjTable.remove(removedEdge.endId,removedEdge.fromId);
        //modify long edge
        NEdge theEdge = graph.edgeList.get(geneKey >> 1);
        graph.adjTable.remove(theEdge.fromId,theEdge.endId);
        graph.adjTable.remove(theEdge.endId,theEdge.fromId);
        graph.vertexList.remove(node);
        List<Integer> valves = graph.getValves();
        int index;
        if ((index=valves.indexOf(node))!=-1){
            valves.remove(index);
        }else {
            System.out.println("删除的阀门未找到");
        }
        //找到原来的起点和终点
        int fromID = removedEdge.getFromId() == node ? removedEdge.endId : removedEdge.fromId;
        int endID = theEdge.fromId == node ? theEdge.endId : theEdge.fromId;
        //F------------------------V--r--E
        if ((geneKey & 1) != 0) {
            //swap
            int temp = fromID;
            fromID = endID;
            endID = temp;
        }
        theEdge.setFromId(fromID);
        theEdge.setEndId(endID);
        theEdge.setLength(theEdge.getLength() + removedEdge.getLength());
        graph.adjTable.insert(fromID, endID, theEdge.id, theEdge.getLength() + removedEdge.getLength());
        graph.adjTable.insert(endID, fromID, theEdge.id, theEdge.getLength() + removedEdge.getLength());
    }
    private static int getRemovePipe(SortedList sortedList, int geneKey) {
        //find two edges connected with valve F-remove-V------line-------E
        Link first = sortedList.first;
        //2，find which pipe should remove& remove the edge
        int firstPipeId = first.valve;
        return (firstPipeId == (geneKey >> 1)) ? first.next.valve : firstPipeId;
    }

    private static int getNewIndex(Map<Integer,?> map,int flag) {
        int id= DataCache.oldLd[flag];
        while (map.get(++id)!=null);
        DataCache.oldLd[flag]=id;
        return id;
    }
}
