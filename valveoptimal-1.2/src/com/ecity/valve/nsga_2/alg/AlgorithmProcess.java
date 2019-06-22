package com.ecity.valve.nsga_2.alg;

import com.ecity.valve.Configuration;
import com.ecity.valve.nsga_2.datastruct.Individual;
import com.ecity.valve.nsga_2.datastruct.Population;
import com.ecity.valve.nsga_2.objective.IObjectiveFunction;
import com.ecity.valve.nsga_2.objective.util.QuickSort;
import com.ecity.valve.util.DataCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//nsga-II算法：包括非支配排序，拥挤度计算
public class AlgorithmProcess {
    public static HashMap<Integer, List<Individual>> fastNonDominatedSort(Population population, int current) {
        //存储分级之后的种群,帕累托前沿有多个
        HashMap<Integer, List<Individual>> paretoFront = new HashMap<>();
        List<Individual> singularFront = new ArrayList<>();
        List<Individual> populace = population.individuals;
        List<IObjectiveFunction> functions = Configuration.getObjectives();
//        long start=System.currentTimeMillis();
        int SIZE= Configuration.POPULATION_SIZE;
        for (int i = 0; i < SIZE; i++) {
            populace.get(i).cost = functions.get(0).calculateObjectiveValue(populace.get(i));
//            populace.get(i).reliability = functions.get(1).calculateObjectiveValue(populace.get(i));
        }
        Individual[] individuals=new Individual[SIZE];
        for (int i = 0; i < SIZE; i++) {
            individuals[i]=populace.get(i);
        }
        QuickSort.sort(individuals);
        for (int i=SIZE-1;i>=0;i--){
           individuals[i].reliability = functions.get(1).calculateObjectiveValue(individuals[i]);
        }
//        long end=System.currentTimeMillis();
//        System.out.println("current:"+current+":"+(end-start));
        /*
         * iterating over each chromosome of the population
         */
        for (Individual chromosome : populace) {

            chromosome.np = 0;
            chromosome.Sp = new ArrayList<>();
            /*
             * an initial domination rank of 0 is set for each chromosome and a blank list is set for the number of
             * chromosomes that the present chromosome dominates.
             */
            for (Individual competitor : populace)
                if (!competitor.equals(chromosome)) {
                    /*
                     * if the present chromosome dominates the competitor, then:
                     *      i:   check if the competitor already exists in the list of dominated chromosomes of the present chromosome.
                     *     ii:   if the competitor does not exist within the list, then add it to the list of dominated chromosomes
                     *           of the present chromosome.
                     * else, if the competitor dominates the present chromosome, then increment the domination rank of the present
                     * chromosome by one.
                     */
                    //如果当前个体支配竞争者,检查竞争者是否已存在于当前个体支配集合中，如果不存在，则将其加入
                    if (dominates(chromosome, competitor)) {
                        if (!chromosome.Sp.contains(competitor))
                            chromosome.Sp.add(competitor);
                    }
                    //否则，判断竞争者是否支配当前个体，如果是，则将np+1；表示支配p的个体多了一个
                    else if (dominates(competitor, chromosome))
                        chromosome.np = chromosome.np + 1;
                }
            /**
             * if the domination rank of the present chromosome is 0, it means that this chromosome is a non-dominated chromosome
             * and hence it is added to the clot of chromosomes that are also non-dominated.
             */
            //如果当前个体的支配数为0，说明没有个体比当前个体表现优秀（至少在一个目标上），则将其加入第一级
            if (chromosome.np == 0) {
                chromosome.irank = 1;
                singularFront.add(chromosome);
            }
        }

        /**
         * the first clot of non-dominated chromosomes is added to the HashMap with rank label 1.
         */

        paretoFront.put(1, singularFront);

        int i = 1;
        //取出第一级：表现最好的一层
        List<Individual> previousFront = paretoFront.get(i);
        List<Individual> nextFront = new ArrayList<>();

        /*
         * the current/previous ranked clot of chromosomes with rank i is iterated over to find the next clot of chromosomes
         * with rank (i+1)
         */
        while (previousFront != null && !previousFront.isEmpty()) {
            for (Individual chromosome : previousFront) {
                for (Individual recessive : chromosome.Sp) {
                    /*
                     * if the domination rank of the current recessive chromosome in consideration is not 0, then
                     * decrement it's rank by 1.
                     * if the domination rank of the current recessive chromosome in consideration is 0, then add
                     * it to the next front [clot of chromosomes that belong to rank (i+1)].
                     */
                    //如果当前个体的被支配数不为0，则将当前个体被支配数-1；因为有一个支配该个体的个体已经存在于Z1
                    if (recessive.np != 0) {
                        recessive.np = recessive.np - 1;
                        //如果-1之后=0；则将其加入Z2
                        if (recessive.np == 0)
                            if (!nextFront.contains(recessive))
                                nextFront.add(recessive);
                    }
                }
            }

            /*
             * this code snippet ensures "rank jumps" to create all the possible rank lists from the parent
             * population.
             * new ranks are created only when there are recessive chromosomes with domination rank = 1 which are
             * decremented to domination rank 0 and then added to the next front.
             * 新的分层只有当有个体的非支配级别为1时，被创建
             * but, due to the randomness of the algorithm, situation may occur such that even after decrementing all recessive
             * chromosome domination ranks by 1, none have domination rank 0 and hence the next front remains empty.
             * 但是，由于算法的随机性，当减一之后可能不会出现非支配级别为0的个体，并且下一层依然为空
             * to ensure that all recessive chromosomes are added to some rank list, the program jumps domination ranks
             * of each recessive chromosome by decrementing domination rank by 1 until at least one of them reaches a
             * domination rank count of 0 and then that recessive chromosome is added to the next front.
             * 为了确保对所有个体进行分层，一直减一到有个体为0
             *
             * if the next front is empty and the previous front has at least one dominated chromosome:
             * 如果next front为空，但是当前front有至少有一个支配个体
             * i:  find the minimum rank among all the recessive chromosomes available:
             * 找到当前层支配个体的最小np
             *              1:  iterate over all the chromosomes of the previous front
             *              2:  while the chromosomes have no dominated chromosomes with rank 0:
             *                      a:  iterate over all the recessive chromosomes of the current chromosome
             *                      b:  if the minimum rank is greater than the dominated rank of the present recessive,
             *                          mark this as the minimum rank recorded among all recessive chromosomes available.
             *              3:  end while
             *     ii:  iterate over all the chromosomes of the previous front
             *              1: while the chromosomes have no dominated chromosomes with rank 0:
             *                      a:  iterate over all the dominated chromosomes of the current chromosome
             *                      b:  if the domination rank of the recessive chromosome is not 0, then decrement the
             *                          domination count by value of minimum rank.
             *                      c:  if the domination rank is 0, then add it to the next front.
             *              2:  end while
             */
            //为了解决出现断层情况
            if (nextFront.isEmpty() && !isDominatedChromosomesEmpty(previousFront)) {
                int minimumRank = -1;
                for (Individual chromosome : previousFront)
                    while (hasRecessiveRankGreaterThanZero(chromosome))
                        for (Individual recessive : chromosome.Sp)
                            if ((minimumRank == -1) || minimumRank > recessive.np)
                                minimumRank = recessive.np;
                if (minimumRank != -1) for (Individual chromosome : previousFront)
                    while (hasRecessiveRankGreaterThanZero(chromosome))
                        for (Individual recessive : chromosome.Sp) {
                            if (recessive.np != 0) recessive.np = recessive.np - minimumRank;
                            if (recessive.np == 0)
                                if (!nextFront.contains(recessive))
                                    nextFront.add(recessive);
                        }
            }

            /*
             * if the next front calculated is not empty, then it is added to the ranked HashMap data-structure
             * with the rank (i+1), else all chromosomes are sorted into some rank or the other and the program
             * breaks out of the loop.
             */
            if (!nextFront.isEmpty()) {
                for (Individual individual : nextFront) {
                    individual.irank = i + 1;
                }
                paretoFront.put(++i, nextFront);
            }
            //如果前面两个if都不满足，break
            if (nextFront.isEmpty())
                break;
            /*
             * the next front (i) calculated is marked as the previous front for the next iteration (i+1) and
             * an empty next front is created.
             */
            previousFront = nextFront;
            nextFront = new ArrayList<>();
        }
        return paretoFront;
    }

    //在某一个目标上占优，在其余目标上持平。
    private static boolean dominates(final Individual competitor1, final Individual competitor2) {
        return ((competitor1.cost <= competitor2.cost) && (competitor1.reliability < competitor2.reliability));
    }

    //有任何个体支配的个体集合不为空，返回false
    private static boolean isDominatedChromosomesEmpty(List<Individual> front) {
        for (Individual individual : front) {
            if (!individual.Sp.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    //个体所支配的个体集合中，没有一个个体的np=0，（都大于0）
    private static boolean hasRecessiveRankGreaterThanZero(Individual individual) {
        if (individual.Sp.isEmpty()) return false;
        for (Individual individual1 : individual.Sp) {
            if (individual1.np != 0) {
                return true;
            }
        }
        return false;
        //return individual.Sp.stream().noneMatch((recessive) -> (recessive.np == 0));
    }

    //将父代和子代进行合并
    public static Population createCombinedPopulation(Population parent, Population child) {
        List<Individual> combinedPopulace = new ArrayList<>();
        Population combinedPopulation = new Population();
        combinedPopulace.addAll(child.individuals);
        combinedPopulace.addAll(parent.individuals);
        combinedPopulation.individuals = combinedPopulace;
        return combinedPopulation;
    }

    //拥挤系数计算，参数是已经分好层的个体集合
    //1，先对每个目标函数值进行升序排序，将最大和最小目标函数值保留
    //2，将排名第一的和最后的，拥挤系数设为无穷大,让他们优先进入
    //3，对于其余中间的解，按照公式进行计算
    public static List<Individual> crowdingDistanceAssignment(List<Individual> singularFront) {
        List<Individual> individuals = new ArrayList<>();
        //reset
        for (Individual individual : singularFront) {
            individual.distance = 0;
        }
        int i;
        int end = singularFront.size() - 1;
        Double maxObjectiveValue;
        Double minObjectiveValue;
        for (int index = 0; index < Configuration.getObjectives().size(); index++) {
            i = 0;
            individuals = sort(singularFront, index);
            individuals.get(i).distance = Double.MAX_VALUE;
            individuals.get(end).distance = Double.MAX_VALUE;
            if (index == 0) {
                maxObjectiveValue = individuals.get(end).cost;
                minObjectiveValue = individuals.get(0).cost;
            } else {
                maxObjectiveValue = individuals.get(end).reliability;
                minObjectiveValue = individuals.get(0).reliability;
            }
            for (i = 1; i < end; i++) {
                if (individuals.get(i).distance == Double.MAX_VALUE) {
                    continue;
                }
                // System.out.println(calculateCrowdingDistance(individuals,i,index,maxObjectiveValue,minObjectiveValue));
                individuals.get(i).distance = calculateCrowdingDistance(individuals, i, index, maxObjectiveValue, minObjectiveValue);
            }
        }
        return individuals;
    }

    private static double calculateCrowdingDistance(List<Individual> singlePareto,
                                                    final int presentIndex,
                                                    final int index,
                                                    final double maxObjectiveValue,
                                                    final double minObjectiveValue) {
        if (index == 0) {
            return singlePareto.get(presentIndex).distance
                    + (singlePareto.get(presentIndex + 1).cost -
                    singlePareto.get(presentIndex - 1).cost) / (maxObjectiveValue - minObjectiveValue);
        } else {
            return singlePareto.get(presentIndex).distance
                    + (singlePareto.get(presentIndex + 1).reliability -
                    singlePareto.get(presentIndex - 1).reliability) / (maxObjectiveValue - minObjectiveValue);
        }
    }

    //快速排序算法,i是目标函数index
    private static List<Individual> sort(List<Individual> singlePareto, int i) {
        Individual[] individuals = new Individual[singlePareto.size()];
        singlePareto.toArray(individuals);
        quickSort(individuals, 0, individuals.length - 1, i);
        return (new ArrayList<>(Arrays.asList(individuals)));
    }

    private static void quickSort(Individual[] individuals, int left, int right, int objectivefunctionIndex) {
        if (right - left > 0) {
            int partition = partition(individuals, left, right, objectivefunctionIndex);
            quickSort(individuals, left, partition - 1, objectivefunctionIndex);
            quickSort(individuals, partition + 1, right, objectivefunctionIndex);
        }
    }

    //划分
    private static int partition(Individual[] paretoArray, int head, int tail, int index) {
        Individual pivot = paretoArray[tail];
        int leftPtr = head - 1;
        int rightPtr = tail;
        while (true) {
            if (index == 0) {
                while (leftPtr < tail && paretoArray[++leftPtr].cost < pivot.cost) ;
                while (rightPtr > head && paretoArray[--rightPtr].cost > pivot.cost) ;
                if (leftPtr >= rightPtr) {
                    break;
                } else {
                    swap(paretoArray, leftPtr, rightPtr);
                }
            } else if (index == 1) {
                while (leftPtr < tail && paretoArray[++leftPtr].reliability < pivot.reliability) ;
                while (rightPtr > head && paretoArray[--rightPtr].reliability > pivot.reliability) ;
                if (leftPtr >= rightPtr) {
                    break;
                } else {
                    swap(paretoArray, leftPtr, rightPtr);
                }
            } else if (index == 2) {
                while (leftPtr < tail && paretoArray[++leftPtr].distance > pivot.distance) ;
                while (rightPtr > head && paretoArray[--rightPtr].distance < pivot.distance) ;
                if (leftPtr >= rightPtr) {
                    break;
                } else {
                    swap(paretoArray, leftPtr, rightPtr);
                }
            }
        }
        swap(paretoArray, leftPtr, tail);
        return leftPtr;
    }

    private static void swap(Individual[] paretoArray, int a, int b) {
        Individual temp = paretoArray[a];
        paretoArray[a] = paretoArray[b];
        paretoArray[b] = temp;
    }

    //按照拥挤距离倒序输出
    public static List<Individual> crowdingDistanceSort(List<Individual> singleFront) {
        //排序
        Individual[] individuals = new Individual[singleFront.size()];
        singleFront.toArray(individuals);
        quicksortByDistance(individuals, 0, singleFront.size() - 1);
        return (new ArrayList<>(Arrays.asList(individuals)));
    }

    public static void quicksortByDistance(Individual[] individuals, int left, int right) {
        if (right - left > 0) {
            int partition = partition(individuals, left, right, 2);//2表示按照距离进行排序
            quickSort(individuals, left, partition - 1, 2);
            quickSort(individuals, partition + 1, right, 2);
        }
    }
}
