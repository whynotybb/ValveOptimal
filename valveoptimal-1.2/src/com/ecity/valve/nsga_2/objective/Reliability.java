package com.ecity.valve.nsga_2.objective;

import com.ecity.valve.Configuration;
import com.ecity.valve.graph.ne.NEGraph;
import com.ecity.valve.graph.sv.SVGraph;
import com.ecity.valve.nsga_2.datastruct.Individual;
import com.ecity.valve.util.DataCache;
import com.ecity.valve.util.TopoUtil;
import java.util.Map;

public class Reliability implements IObjectiveFunction {

    public double calculateObjectiveValue(Individual individual) {
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
         return svGraph.getLD();
    }
}
