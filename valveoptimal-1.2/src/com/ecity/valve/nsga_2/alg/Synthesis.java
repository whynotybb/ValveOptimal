package com.ecity.valve.nsga_2.alg;

import com.ecity.valve.Configuration;
import com.ecity.valve.graph.ne.NEdge;
import com.ecity.valve.nsga_2.datastruct.Individual;
import com.ecity.valve.nsga_2.datastruct.Population;
import com.ecity.valve.util.DataCache;

import java.util.*;

//遗传算法：选择，交叉，变异，初始化种群
public class Synthesis {
    private static final Random LOCAL_RANDOM = new Random();
    public static Population syntesizePopulation() {
        List<Individual> populace = new ArrayList<>();
        for(int i = 0; i < Configuration.POPULATION_SIZE; i++) {
            populace.add(new Individual(synthesizeGeneticCode()));
        }
        return new Population(populace);
    }
    //记录已有阀门的管段。
    //产生编码0001000100001000
    private static Map<Integer,Integer> synthesizeGeneticCode() {
        Map<Integer,NEdge> edgeMap= DataCache.neGraph.edgeList;
        Map<Integer,Integer> genesMap=new HashMap<>();
        for(int key:edgeMap.keySet()) {
            //每条边都有两个可以添加阀门的潜在位置
            for (int i=0;i<2;i++){
                if (DataCache.cantInstallPos.contains((key<<1)+i))
                    continue;
                genesMap.put((key<<1)+i,synthesizeGene());
            }
        }
        return genesMap;
    }
    private static int synthesizeGene(){
        double r=LOCAL_RANDOM.nextFloat();
        if (r<Configuration.GENE_BE_ZERO)
            return 0;
        else
            return 1;
    }
    //二进制锦标赛选择
    //拥挤度比较算子来进行选择
    private static Individual binaryTournamentSelection(Population population) {
        Individual individual1 = population.individuals.get(LOCAL_RANDOM.nextInt(Configuration.POPULATION_SIZE));
        Individual individual2 = population.individuals.get(LOCAL_RANDOM.nextInt(Configuration.POPULATION_SIZE));
        if (individual1.irank<individual2.irank){
            return individual1;
        }
        else if (individual1.irank>individual2.irank){
            return individual2;
        }
        else {
            if (individual1.distance>individual2.distance){
                return individual1;
            }
            else {
                return individual2;
            }
        }
    }
    //O(n)
    private static Individual[] crossover(Individual individual1, Individual individual2) {
        Map<Integer,Integer> genes1=new HashMap<>(Configuration.GENE_LENGTH);
        Map<Integer,Integer> genes2=new HashMap<>(Configuration.GENE_LENGTH);
        Map<Integer,Integer> genes11=individual1.genes;
        Map<Integer,Integer> genes22=individual2.genes;
        for (int i:genes11.keySet()) {
            Integer geneA = genes11.get(i);
            Integer geneB = genes22.get(i);
            float a = LOCAL_RANDOM.nextFloat();
            if (a < Configuration.CROSSOVER_Probility) {
                genes1.put(i,geneA);
                genes2.put(i,geneB);
                } else {
                genes2.put(i,geneA);
                genes1.put(i,geneB);
            }
        }
        return new Individual[]{new Individual(genes1), new Individual(genes2)};
    }
    //两点交叉
    private static Individual[] crossover1(Individual individual1, Individual individual2) {
        Map<Integer,Integer> genes11=new HashMap<>(Configuration.GENE_LENGTH);
        Map<Integer,Integer> genes22=new HashMap<>(Configuration.GENE_LENGTH);
        Map<Integer,Integer> genes1=individual1.genes;
        Map<Integer,Integer> genes2=individual2.genes;
        double p=LOCAL_RANDOM.nextDouble();
        Individual individual11=new Individual(genes11);
        Individual individual22=new Individual(genes22);
        individual11.parent=individual1;
        individual22.parent=individual2;
        individual1.child=individual11;
        individual2.child=individual22;
        if (p<=Configuration.CROSSOVER_Probility){
            //1，随机生成两个交叉点，
            int a=LOCAL_RANDOM.nextInt(Configuration.GENE_LENGTH);
            int b=LOCAL_RANDOM.nextInt(Configuration.GENE_LENGTH);
            if (b<a){
                int temp=a;
                a=b;
                b=temp;
            }
            for (int i:genes1.keySet()) {
                //交换a和b之间的基因
                if (i>=a&&i<=b){
                    genes11.put(i,genes2.get(i));
                    genes22.put(i,genes1.get(i));
                    //1:1->0 2:0->1
                    if (genes1.get(i)==1&&genes2.get(i)==0){
                        individual11.removes.add(i);
                        individual22.adds.add(i);
                    }
                    //gene1:0->1 gene2:1->0
                    if (genes1.get(i)==0&&genes2.get(i)==1){
                        individual11.adds.add(i);
                        individual22.removes.add(i);
                    }
                }else {
                    genes11.put(i,genes1.get(i));
                    genes22.put(i,genes2.get(i));
                }
            }
            return new Individual[]{individual11,individual22};
        }
        //不发生交叉
        for (int k:genes1.keySet()){
            genes11.put(k,genes1.get(k));
            genes22.put(k,genes2.get(k));
        }
        return new Individual[]{individual11,individual22};
    }
    //变异，变异的概率随着代数的大小减小 O(n)
    private static Individual mutation(Individual individual) {
        Map<Integer,Integer> newGenes=new HashMap<>();
        Map<Integer,Integer> genes=individual.genes;
        for (int i :genes.keySet()){
            Integer gene= genes.get(i);
            if (LOCAL_RANDOM.nextFloat() < Configuration.MUTATION_Probility) {
                if (gene == 1) {
                    gene = 0;
                }
                else{
                    gene = 1;
                }
            }
                newGenes.put(i,gene);
            }
        return new Individual(newGenes);
    }
    private static Individual mutation1(Individual individual) {
        Map<Integer,Integer> genes=individual.genes;
        Individual parent= individual.parent;
        for (int i :genes.keySet()){
            Integer gene= genes.get(i);
            if (LOCAL_RANDOM.nextFloat() < Configuration.MUTATION_Probility) {
                if (gene == 1) {
                    gene = 0;
                    // 1->0
                    if (parent.genes.get(i)==1){
                        individual.removes.add(i);
                    }else {
                        individual.adds.remove(i);
                    }
                }
                else {
                    gene = 1;
                    if (parent.genes.get(i)==0){
                        individual.adds.add(i);
                    }else {
                        individual.removes.remove(i);
                    }
                }
            }
            genes.put(i,gene);
        }
        return individual;
    }
    //产生下一代
    public static Population synthesizeChild(Population parent) {
        Population child = new Population();
        List<Individual> populace = new ArrayList<>();
        while(populace.size() < Configuration.POPULATION_SIZE)
            for(Individual chromosome : crossover(binaryTournamentSelection(parent), binaryTournamentSelection(parent))) {
                populace.add(mutation(chromosome));
        }
        child.individuals=populace;
        return child;
    }
}
