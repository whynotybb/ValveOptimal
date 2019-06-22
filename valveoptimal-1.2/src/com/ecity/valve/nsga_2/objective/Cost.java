package com.ecity.valve.nsga_2.objective;

import com.ecity.valve.nsga_2.datastruct.Individual;
import com.ecity.valve.util.DataCache;
public class Cost implements IObjectiveFunction {
    public double calculateObjectiveValue(Individual individual){
        double cost=0;
        for (int key:individual.genes.keySet()) {
            if (individual.genes.get(key)==1){
               //获取管段口径，并且计算价格
                 double dia= DataCache.gisGraph.edgeList.get(key>>1).getDia();
                 cost+=getCostByDia(dia);
            }
        }
        return cost;
    }
    private double getCostByDia(double dia){
       return   -0.0085*dia*dia + 9.4566*dia + 114.74;
    }
}
