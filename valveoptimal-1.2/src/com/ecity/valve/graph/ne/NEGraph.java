package com.ecity.valve.graph.ne;

import com.ecity.valve.Configuration;
import com.ecity.valve.exception.NotFoundPropException;
import com.ecity.valve.graph.Graph;
import com.ecity.valve.graph.datastruct.Link;

import java.util.*;

public class NEGraph extends Graph<NEVertex,NEdge>{
    private List<Integer> valves;
    private Set<Integer> sources;
    //SV中的水源单元
    public Set<Integer> souIUs;
    private Map<Integer,Set<Integer>> valvesToIUs;
    private List<Integer> theQueue;
    public Map<Integer,Set<Integer>> segPipesMap;
    public Map<Integer,Double> segDemandMap;
    public Map<Integer,Double> segLengthMap;
    public int[] pipesToSeg;
    public NEGraph(){
        valves=new ArrayList<>();
        sources=new HashSet<>();
        souIUs=new HashSet<>();
        valvesToIUs=new HashMap<>();
        theQueue=new ArrayList<>();
        segPipesMap=new HashMap<>();
        segLengthMap=new HashMap<>();
        segDemandMap=new HashMap<>();
        int SIZE=Configuration.MAX_NODE_SIZE;
        pipesToSeg=new int[SIZE];
        for (int i=0;i<SIZE;i++){
            pipesToSeg[i]=-1;
        }
    }
    @Override
    public void addVertex(NEVertex neVertex) {
        super.addVertex(neVertex);
        if (neVertex.getNodetype()==Configuration.TYPE_VALVE &&neVertex.getLines()==2)
            valves.add(neVertex.id);
        if (neVertex.getNodetype()==Configuration.TYPE_SOURCE)
            sources.add(neVertex.id);
    }
    @Override
    public void addEdge(NEdge nEdge) {
        super.addEdge(nEdge);
        adjTable.insert(nEdge.fromId,nEdge.endId,nEdge.id,nEdge.getLength());
        adjTable.insert(nEdge.endId,nEdge.fromId,nEdge.id,nEdge.getLength());
    }

    private ArrayList<Integer> valvesQueue;
    public void createIU(){
        int nIUs=0;
        valvesQueue =new ArrayList<>();
        if (!sources.isEmpty()) {
            int source = sources.iterator().next();
            valvesQueue.add(source);
        }else {
            throw  new NotFoundPropException("您的数据中没有水源结点，或者结点类型不一致，请核对！");
        }
        int seed;
        while((seed=findSeed())!=-1) {
            Set<Integer> pipesInSeg=new HashSet<>();
            double length=0,demand=0;
            edgeList.get(seed).setVisited(true);
            length+=edgeList.get(seed).getLength();
            pipesInSeg.add(seed);
            pipesToSeg[seed]=nIUs;
            int start=edgeList.get(seed).getFromId();
            int end=edgeList.get(seed).getEndId();
            //记录水源单元
            if (sources.contains(start)||sources.contains(end)){
                souIUs.add(nIUs);
            }
            demand+=vertexList.get(start).getDemand()+vertexList.get(end).getDemand();
            //如果不是阀门，将其添加到队列中，如果是其他结点，将其添加到
            putInMapOrQueue(start,nIUs);
            putInMapOrQueue(end,nIUs);
            int v2;
            while(!theQueue.isEmpty()) {
                int v1=theQueue.remove(theQueue.size()-1);//v1一定不是阀门
                while((v2=getUnvisitedEdge(v1))!=-1) {
                    edgeList.get(v2).setVisited(true);
                    length+=edgeList.get(v2).getLength();
                    pipesInSeg.add(v2);
                    pipesToSeg[v2]=nIUs;
                    int start1=edgeList.get(v2).getFromId();
                    //判断那个是未访问
                    int v=(v1==start1?edgeList.get(v2).getEndId():start1);
                    if (sources.contains(v))
                        souIUs.add(nIUs);
                    demand+=vertexList.get(v).getDemand();
                    putInMapOrQueue(v,nIUs);
                }
            }
            segPipesMap.put(nIUs,pipesInSeg);
            segLengthMap.put(nIUs,length);
            segDemandMap.put(nIUs,demand);
            nIUs++;
        }
        for(NEdge edge:edgeList.values()){
            edge.setVisited(false);
        }
    }
    //找到与阀门相连的未访问边
    private int findSeed(){
        while (!valvesQueue.isEmpty()){
            int q= valvesQueue.remove(valvesQueue.size()-1);
            //与阀门相连的边只有两条，而且一定有一条已经访问
            Link current=adjTable.get(q).first;
            while (current!=null){
                if (!edgeList.get(current.valve).isVisited()){
                    return current.valve;
                }
                current=current.next;
            }
        }
        return -1;
    }

    /**
     * 如果是阀门结点，记录与关断单元的映射，如果不是阀门，将其记录在队列中
     */
    private void putInMapOrQueue(int v,int nIUs) {
        if (isNotValve(v)) theQueue.add(v);
        else {
            Set<Integer> temp;
            temp=valvesToIUs.get(v)==null?new HashSet<Integer>():valvesToIUs.get(v);
            temp.add(nIUs);
            valvesToIUs.put(v, temp);
            valvesQueue.add(v);
        }
    }

    //输入起点
    private int getUnvisitedEdge(int e) {
        Link current= adjTable.get(e).first;
        while(current!=null) {
            if (!edgeList.get(current.valve).isVisited()) {
                return current.valve;
            }
            current=current.next;
        }
        return -1;
    }
    private boolean isNotValve(int node) {
        return vertexList.get(node).getNodetype()!=Configuration.TYPE_VALVE
                ||vertexList.get(node).getLines()!=2;
    }
    //获取不能添加阀门的位置
    public  Set<Integer> getCantInstallPos(){
        Set<Integer> result=new HashSet<>();
        for (Integer valve:valves){
            //首先获取阀门连接的两条边
            Link current= adjTable.get(valve).first;
            //找到最短的边
            int shortEdge,longEdge;
            if(edgeList.get(current.valve).getLength()< edgeList.get(current.next.valve).getLength()){
                shortEdge=current.valve;
                longEdge=current.next.valve;
            }else {
                longEdge=current.valve;
                shortEdge=current.next.valve;
            }
            result.add(shortEdge*2);
            result.add(shortEdge*2+1);
            if (edgeList.get(longEdge).getFromId()==valve) {
                result.add(longEdge * 2);
            }else
                result.add(longEdge*2+1);
        }
        return result;
    }

    public NEGraph copy(){
        NEGraph graph=new NEGraph();
        for (NEVertex vertex:this.vertexList.values()){
            graph.addVertex(new NEVertex(vertex.id,vertex.getNodetype(),vertex.getLines(),vertex.getDemand()));
        }
        for (NEdge edge:this.edgeList.values()){
            graph.addEdge(new NEdge(edge.id,edge.fromId,edge.endId,edge.getLength()));
        }
        return graph;
    }
    //查找新增冗余阀对应的基因编号-冗余阀连接长边的2倍（起点）或2倍+1（终点）
    public List<Integer> getValves() {
        return valves;
    }
    public Map<Integer, Set<Integer>> getValvesToIUs() {
        return valvesToIUs;
    }


    /**
     *   从阀门所在的长管段出发，识别新生成的一个关断单元的结点和管段信息
     * @param geneKey 新增阀门所在边位置（边的二倍-起点，边的二倍+1-终点
     * @param pipesInSeg 返回该关断单元包含的管线
     * @param valvesCount 返回该关断单元包含的所有阀门
     */
    public void getSegment(int geneKey, Set<Integer> pipesInSeg,Map<Integer,Integer> valvesCount,double[] ld){
        //得到阀门所在的管段
        int edge=geneKey>>1;
        edgeList.get(edge).setVisited(true);
        pipesInSeg.add(edge);
        ld[1]+=edgeList.get(edge).getLength();
        int start = edgeList.get(edge).getFromId();
        int end = edgeList.get(edge).getEndId();
        ld[0]+=vertexList.get(start).getDemand()+vertexList.get(end).getDemand();
        if (isNotValve(start)) theQueue.add(start);
        else {
            int count=1;
            if (valvesCount.get(start)!=null){
                count=2;
            }
            valvesCount.put(start,count);
        }
        if (sources.contains(start)||sources.contains(end)){
            valvesCount.put(-1,start);
        }
        if (isNotValve(end)) theQueue.add(end);
        else {
            int count=1;
            if (valvesCount.get(end)!=null){
                count=2;
            }
            valvesCount.put(end,count);
        }
        int v2;
        while (!theQueue.isEmpty()) {
            int v1 = theQueue.remove(theQueue.size() - 1);//v1一定不是阀门
            while ((v2 = getUnvisitedEdge(v1)) != -1) {
                edgeList.get(v2).setVisited(true);
                pipesInSeg.add(v2);
                ld[1]+=edgeList.get(v2).getLength();
                int start1 = edgeList.get(v2).getFromId();
                //判断那个是未访问
                int v = (v1 == start1 ? edgeList.get(v2).getEndId() : start1);
                ld[0]+=vertexList.get(v).getDemand();
                if (sources.contains(v)){
                    valvesCount.put(-1,v);
                }
                if (isNotValve(v)) theQueue.add(v);
                else {
                    int count = 1;
                    if (valvesCount.get(v) != null) {
                        count =2;
                    }
                    valvesCount.put(v, count);
                }
            }
        }
        //reset：
        for (Integer pipe:pipesInSeg)
            edgeList.get(pipe).setVisited(false);
    }
    //筛选候选点
    public Set<Integer> getCanInstallPos(){
        Set<Integer> res=new HashSet<>();
        for (int i=0;i<Configuration.MAX_NE_NODE_ID;i++){
            if (adjTable.getListSize(i)>2){
                //degree is three and bigger
                //get edges connected with i
                Link connectedWithI= adjTable.get(i).first;
                while (connectedWithI!=null){
                    int v=connectedWithI.id;
                    if (isNotValve(v)){
                        res.add(v);
                    }
                    connectedWithI=connectedWithI.next;
                }
            }
        }
        return res;
    }
}
