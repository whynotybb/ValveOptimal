package com.ecity.valve.nsga_2.datastruct;


import java.util.*;

public class Individual {
    //拥挤系数
    public double distance;
    //虚拟适应度(非支配级别)
    public int irank;
    //支配p的个体数
    public int np;
    //受p支配的个体
    public List<Individual> Sp;
    //key :pipeId
    public Map<Integer,Integer> genes;
    public double cost;//成本
    public double reliability;//可靠性
    public Set<Integer> adds;
    public Set<Integer> removes;
    public Individual parent;
    public Individual child;
    public Individual(Map<Integer,Integer> genes) {
        this.distance = 0;
        this.irank = 0;
        this.np = 0;
        Sp = new ArrayList<>();
        this.genes =genes;
        this.cost =0;
        this.reliability =0;
        adds=new HashSet<>();
        removes=new HashSet<>();
        parent=null;
        child=null;
    }
}
