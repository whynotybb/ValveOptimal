package com.ecity.valve.nsga_2.objective;

import com.ecity.valve.Configuration;
import com.ecity.valve.graph.ne.NEGraph;
import com.ecity.valve.graph.sv.SVGraph;
import com.ecity.valve.nsga_2.datastruct.Individual;
import com.ecity.valve.util.DataCache;
import com.ecity.valve.util.TopoUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Reliability4 implements IObjectiveFunction{
    @Override
    public double calculateObjectiveValue(Individual individual) {
        if (DataCache.samest==null){
            return calculateObjectiveValue1(individual);
        }else {
             int[] ids= DataCache.oldLd;
             ids[0]=DataCache.neGraph.vertexList.size();
             ids[1]=DataCache.neGraph.edgeList.size();
             ids[2]=DataCache.svGraph.vertexList.size();
             SVGraph svGraph= DataCache.svGraph;
             List<Integer> adds=new ArrayList<>();
             List<Integer> removes=new ArrayList<>();
             getNeedToModifyValves(individual.genes,adds,removes);
//            System.out.println(adds.size()+"-"+removes.size());
             for (Integer a:adds){
                 TopoUtil.splitSegment(svGraph,a);
             }
             for (Integer r:removes){
                 TopoUtil.mergeSegment(svGraph,r);
             }
             DataCache.samest=individual;
             return svGraph.getLD();
        }
    }
    private void getNeedToModifyValves(Map<Integer,Integer> cal, List<Integer> needToAdd, List<Integer> needToRemove){
        Map<Integer,Integer> samest=DataCache.samest.genes;
        for(int k:cal.keySet()){
            if (cal.get(k)==1&&samest.get(k)==0){
                needToAdd.add(k);
            }
            else if (cal.get(k)==0&&samest.get(k)==1){
                needToRemove.add(k);
            }
        }
    }
    private double calculateObjectiveValue1(Individual individual) {
        NEGraph graph=DataCache.neGraph.copy();
        Map<Integer,Integer> genes =individual.genes;
        int [] index=new int[]{Configuration.MAX_NE_NODE_ID,Configuration.MAX_NE_EDGE_ID};
        for (int key:genes.keySet()){
            if (genes.get(key)==1){
                TopoUtil.addVertex(graph,++index[0]);
                TopoUtil.addEdge(graph,key,index[0],++index[1]);
            }
        }
        SVGraph svGraph=new SVGraph(graph);
        //找到冗余阀对应的基因编号，已有的冗余阀不用去除。
        DataCache.svGraph=svGraph;
        DataCache.samest=individual;
        return svGraph.getLD();
    }
}
