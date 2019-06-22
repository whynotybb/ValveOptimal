package com.ecity.valve.nsga_2.objective;


import com.ecity.valve.nsga_2.datastruct.Individual;

import java.util.Map;

public class NValves implements IObjectiveFunction{
    @Override
    public double calculateObjectiveValue(Individual individual) {
        double n=0;
        Map<Integer,Integer> genes= individual.genes;
        for (int key:individual.genes.keySet()) {
            if (genes.get(key)==1) n++;
        }
        return n;
    }
}
