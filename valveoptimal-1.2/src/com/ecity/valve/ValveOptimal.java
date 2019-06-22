package com.ecity.valve;

import com.ecity.geometry.Geometry;
import com.ecity.network.INetManager;
import com.ecity.valve.graph.gis.GISGraph;
import com.ecity.valve.graph.ne.NEGraphFactory;
import com.ecity.valve.graph.sv.SVGraph;
import com.ecity.valve.nsga_2.alg.AlgorithmProcess;
import com.ecity.valve.nsga_2.alg.Synthesis;
import com.ecity.valve.nsga_2.datastruct.Individual;
import com.ecity.valve.nsga_2.datastruct.Population;
import com.ecity.valve.util.DataCache;
import com.ecity.valve.util.DataUtil;
import com.ecity.valve.util.PropertiesUtil;
import com.ecity.valve.util.Solution;
import java.util.*;

public class ValveOptimal {
    public static List<Solution> run(GISGraph gisGraph) throws Exception{
        DataCache.gisGraph=gisGraph;
        //抽象为NE图。
        DataCache.neGraph = NEGraphFactory.build(gisGraph);
        DataCache.cantInstallPos =DataCache.neGraph.getCantInstallPos();
        //计算基因长度
        Configuration.GENE_LENGTH=DataCache.neGraph.edgeList.size()*2-DataCache.cantInstallPos.size();
        //1随机生成p0
        Long startTime=System.currentTimeMillis();
        //初始化种群
        Population parent=Synthesis.syntesizePopulation();
        //2快速非支配排序,拥挤系数计算
        Map<Integer,List<Individual>> paretoFronts= AlgorithmProcess.fastNonDominatedSort(parent,1);
        for (int i=0;i<paretoFronts.size();i++){
            AlgorithmProcess.crowdingDistanceAssignment(paretoFronts.get(i+1));
        }
        //3产生子代q0
        Population child=Synthesis.synthesizeChild(parent);
        for (int currentGeneration=2;currentGeneration<=Configuration.MAX_GENERATION;currentGeneration++) {
            System.out.println("GENERATION : " + currentGeneration);
            HashMap<Integer, List<Individual>> rankedFronts = AlgorithmProcess.fastNonDominatedSort(AlgorithmProcess.createCombinedPopulation(parent, child),currentGeneration);
            Population nextChildPopulation = new Population();
            List<Individual> childPopulace = new ArrayList<>();
            for(int j = 1; j <= rankedFronts.size(); j++) {
                List<Individual> singularFront = rankedFronts.get(j);
                //因为参与非支配排序的个体是2N，只有一半选择，所以要计算usableSpace，让最后一层（选择的）的部分进入
                int usableSpace = Configuration.POPULATION_SIZE - childPopulace.size();
                if(singularFront != null && !singularFront.isEmpty() && usableSpace > 0) {
                    if(usableSpace >= singularFront.size()){
                        //只有进入下一代的计算拥挤系数，其余不需要计算
                        AlgorithmProcess.crowdingDistanceAssignment(singularFront);
                        childPopulace.addAll(singularFront);
                    }
                    else {
                        List<Individual> latestFront = AlgorithmProcess.crowdingDistanceSort(AlgorithmProcess.crowdingDistanceAssignment(singularFront));
                        for(int k = 0; k < usableSpace; k++) childPopulace.add(latestFront.get(k));
                    }
                }
                else break;
            }
            nextChildPopulation.individuals=childPopulace;
            //清理缓存:将淘汰的个体移除
            if(currentGeneration < Configuration.MAX_GENERATION) {
                parent = nextChildPopulation;
                child = Synthesis.synthesizeChild(nextChildPopulation);
            }
            else {
                Long endTime=System.currentTimeMillis();
                System.out.println("耗时:"+(endTime-startTime));
                return DataUtil.getSolutions(AlgorithmProcess.fastNonDominatedSort(nextChildPopulation,currentGeneration));
            }
        }
        return null;
    }

    /**
     * 根据提供的Geometry对象运行，
     * @param geometry 裁剪多边形,如果为null，则会在整个管网数据上运行
     * @param netManager 提供数据库操作
     * @param configFile 配置文件路径+文件名
     * @throws Exception 文件IO
     */
    public static List<Solution> run(Geometry geometry, INetManager netManager, String configFile) throws Exception{
        PropertiesUtil.load(configFile);
        if (geometry==null){
           return  run(DataUtil.build(netManager));
        }
        return run(DataUtil.builderByClip(netManager,geometry));
    }

    /**
     * 运行本地dbf数据
     * @param path  文件存储路径
     * @param nodeName 结点文件名
     * @param lineName 管线文件名
     * @param configFile 配置文价
     * @throws Exception 文件io异常
     */
    public static List<Solution> run(String path,String nodeName,String lineName,String configFile) throws Exception{
        PropertiesUtil.load(configFile);
        return run(DataUtil.buildLocal(path+"//"+nodeName,path+"//"+lineName));
    }
}

