package com.ecity.valve.util;

import com.ecity.db.IECityDb;
import com.ecity.define.common.DbMetaInfo;
import com.ecity.define.common.DbMetaNetFeature;
import com.ecity.exception.EcityException;
import com.ecity.feature.Feature;
import com.ecity.feature.QueryFilter;
import com.ecity.geometry.Geometry;
import com.ecity.network.INetManager;
import com.ecity.valve.Configuration;
import com.ecity.valve.graph.gis.GISEdge;
import com.ecity.valve.graph.gis.GISGraph;
import com.ecity.valve.graph.gis.GISVertex;
import com.ecity.valve.nsga_2.datastruct.Individual;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;
import com.linuxense.javadbf.DBFUtils;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/*
数据接入类
 */
public class DataUtil {
    public static GISGraph build(INetManager netManager) throws Exception{
        GISGraph gisGraph=new GISGraph();
        //获取数据
        Map<Integer,GISVertex> nodesMap=getNodes(netManager);
        for (GISVertex vertex:nodesMap.values()){
            gisGraph.addVertex(vertex);
        }
        Map<Integer,GISEdge> linesMap= getLines(netManager);
        for (GISEdge edge:linesMap.values()){
            gisGraph.addEdge(edge);
        }
        return gisGraph;
    }
    private static Map<Integer,GISVertex> getNodes(INetManager netManager)
            throws EcityException {
        int index=0;
        String strSql ="select "+Configuration.FIELD_NAME_ID+","+
                Configuration.FIELD_NAME_NODETYPE +","+
                Configuration.FIELD_NAME_CBZQ+","+
                Configuration.FIELD_NAME_DEMAND;
        strSql = strSql+" from " + netManager.getNetName()
                + "_nod where "+Configuration.FIELD_NAME_ID+" in (select +"+Configuration.FIELD_NAME_STNOD+ " from " + netManager.getNetName()
                + "_lin union select "+Configuration.FIELD_NAME_EDNOD+" from " + netManager.getNetName()
                + "_lin) order by "+Configuration.FIELD_NAME_ID;
        ResultSet rs;
        Map<Integer, GISVertex> points = new HashMap<>();
        IECityDb objDb = netManager.getWorkspace().getDb();
        try {
            rs = objDb.executeSqlRs(strSql);
            while (rs.next()) {
                int gid = rs.getInt(Configuration.FIELD_NAME_ID);
                int nodetype = rs.getShort(Configuration.FIELD_NAME_NODETYPE);
                int demand = rs.getInt(Configuration.FIELD_NAME_DEMAND);
                String time = rs.getString(Configuration.FIELD_NAME_CBZQ);
                if ("双月".equals(time)){
                    demand=demand<<1;
                }
                GISVertex node = new GISVertex(index,gid, nodetype,demand);
                points.put(gid, node);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            objDb.close();
        }
        System.out.println("finish "+ netManager.getNetName() + " node cache "
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date()));
        return points;
    }
    private static Map<Integer,GISEdge> getLines(INetManager netManager)
            throws EcityException{
        String strSql = "select "+Configuration.FIELD_NAME_ID+","+ Configuration.FIELD_NAME_STNOD+","
                +Configuration.FIELD_NAME_EDNOD+","
                +Configuration.FIELD_NAME_DIA+","
                +Configuration.FIELD_NAME_LENGTH+" from "
                + netManager.getNetName()
                + "_lin where "+Configuration.FIELD_NAME_STNOD+
                " is not null and "+Configuration.FIELD_NAME_EDNOD+" is not null order by "
                + Configuration.FIELD_NAME_ID;
        ResultSet rs;
        IECityDb objDb = netManager.getWorkspace().getDb();
        Map<Integer, GISEdge> lines = new HashMap<>();
        try {
            rs = objDb.executeSqlRs(strSql);
            int index=0;
            while (rs.next()){
                int gid=rs.getInt(Configuration.FIELD_NAME_ID);
                int stnod=rs.getInt(Configuration.FIELD_NAME_STNOD);
                int ednod=rs.getInt(Configuration.FIELD_NAME_EDNOD);
                int dia=rs.getInt(Configuration.FIELD_NAME_DIA);
                double length=rs.getDouble(Configuration.FIELD_NAME_LENGTH);
                lines.put(gid, new GISEdge(index++,gid,stnod,ednod,dia,length));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            objDb.close();
        }
        System.out.println("finish "
                + netManager.getNetName()
                + " line cache "
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date()));
        return lines;
    }
    //数据裁剪
    private static List<Feature> clipByPolygon(QueryFilter queryFilter, String featureName, INetManager netManager) throws EcityException{
        return netManager.getWorkspace().getFeatureClass(featureName).search(queryFilter);
    }
    //输入多边形，根据多边形边界获取点和线要素,并且构建GISGraph.
    public static GISGraph builderByClip(INetManager netManager,Geometry geometry) throws Exception{
        GISGraph gisGraph=new GISGraph();
        QueryFilter queryFilter1=new QueryFilter();
        queryFilter1.setGeometry(geometry);
        int mode=1;
        if (Configuration.FIELD_NAME_CBZQ!=null&&Configuration.FIELD_NAME_DEMAND!=null){
            queryFilter1.setOutFields(Configuration.FIELD_NAME_ID
                    +","+Configuration.FIELD_NAME_NODETYPE
                    +","+Configuration.FIELD_NAME_CBZQ
                    +","+Configuration.FIELD_NAME_DEMAND);
        }
        else if (Configuration.FIELD_NAME_DEMAND==null){
            mode=2;
            queryFilter1.setOutFields(Configuration.FIELD_NAME_ID
                    +","+Configuration.FIELD_NAME_NODETYPE);
            Configuration.DEMAND_CREATE_MODE=2;
        }
        else {
            mode=3;
            queryFilter1.setOutFields(Configuration.FIELD_NAME_ID
                    +","+Configuration.FIELD_NAME_NODETYPE
                    +","+Configuration.FIELD_NAME_DEMAND);
        }
         List<Feature> nodes=clipByPolygon(queryFilter1,netManager.getNetName()+"_nod",netManager);
         int count=0;
         Map<Integer,Integer> map=new HashMap<>();
         for (Feature feature:nodes){
            int id= feature.getIntValue(Configuration.FIELD_NAME_ID);
            int dno=feature.getIntValue(Configuration.FIELD_NAME_NODETYPE);
            map.put(id,count);
            double demand=0;
            String time;
            if (mode==1){
                demand= feature.getDoubleValue(Configuration.FIELD_NAME_DEMAND);
                time = feature.getStringValue(Configuration.FIELD_NAME_CBZQ);
                if ("双月".equals(time)){
                    demand=demand*2;
                }
            }
            gisGraph.addVertex(new GISVertex(count++,id,dno,demand));
        }
        QueryFilter queryFilter2=new QueryFilter();
        queryFilter2.setGeometry(geometry);
        queryFilter2.setOutFields(Configuration.FIELD_NAME_ID
                + ","+Configuration.FIELD_NAME_STNOD
                +"," +Configuration.FIELD_NAME_EDNOD
                +","+Configuration.FIELD_NAME_DIA+
                ","+Configuration.FIELD_NAME_LENGTH);
        List<Feature> lines=clipByPolygon(queryFilter2,netManager.getNetName()+"_lin",netManager);
        count=0;
        for (Feature line:lines){
             int id=line.getIntValue(Configuration.FIELD_NAME_ID);
             int stnod= line.getIntValue(Configuration.FIELD_NAME_STNOD);
             int ednod= line.getIntValue(Configuration.FIELD_NAME_EDNOD);
             if (gisGraph.vertexList.containsKey(map.get(stnod))&&gisGraph.vertexList.containsKey(map.get(ednod))){
                 //可能为null 或者0，这时用默认值
                 double dia=line.getDoubleValue(Configuration.FIELD_NAME_DIA);
                 dia=dia==0?Configuration.defaultDiameter:dia;
                 double length=line.getDoubleValue(Configuration.FIELD_NAME_LENGTH);
                 gisGraph.addEdge(new GISEdge(count++,id,map.get(stnod),map.get(ednod),dia,length));
             }
             else {
                 //只有一端有结点的边，这个结点作为新水源。
                 int source=gisGraph.vertexList.containsKey(map.get(stnod))?map.get(stnod):map.get(ednod);
                 Configuration.SOURCE_NODE=source;
                 //修改水源结点类型
                 gisGraph.vertexList.get(source).setDno(Configuration.TYPE_SOURCE);
             }
         }
         return gisGraph;
    }
    //从本地读数据
    public static GISGraph buildLocal(String nodepath,String linepath){
        GISGraph gisGraph=new GISGraph();
        DBFReader nodeReader=getReader(nodepath);
        DBFRow row;
        int count=0;
        Map<Integer,Integer> idMap=new HashMap<>();
        while ((row = nodeReader.nextRow()) != null) {
            int gid=row.getInt(Configuration.FIELD_NAME_ID);
            int nodetype=row.getInt(Configuration.FIELD_NAME_NODETYPE);
            int demand=row.getInt(Configuration.FIELD_NAME_DEMAND);
            idMap.put(gid,count);
            gisGraph.addVertex(new GISVertex(count++,gid,nodetype,demand));
        }
        int lineIndex=0;
        DBFReader lineReader=getReader(linepath);
        //gisGraph的起点是刷新后的较小的编号
        while ((row = lineReader.nextRow()) != null){
            int id=row.getInt(Configuration.FIELD_NAME_ID);
            int stnod=row.getInt(Configuration.FIELD_NAME_STNOD);
            int ednod=row.getInt(Configuration.FIELD_NAME_EDNOD);
            int dia=row.getInt(Configuration.FIELD_NAME_DIA);
            double length=row.getDouble(Configuration.FIELD_NAME_LENGTH);
            gisGraph.addEdge(new GISEdge(lineIndex++,id,idMap.get(stnod),idMap.get(ednod),dia,length));
        }
        DBFUtils.close(nodeReader);
        DBFUtils.close(lineReader);
        return gisGraph;
    }
    private static DBFReader getReader(String path){
        DBFReader dbfReader;
        try{
           dbfReader=new DBFReader(new FileInputStream(path));
           return dbfReader;
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }
    public static void writeDataOnLocal(String path,String solution) throws Exception{
        String fileNmae=path+"_"+new Date().getTime();
        FileOutputStream fileOutputStream=new FileOutputStream(fileNmae);
        fileOutputStream.write(solution.getBytes());
        System.out.println("解决方案保存在"+fileNmae+"，请查看！");
        fileOutputStream.close();
    }
    public static List<Solution> getSolutions(HashMap<Integer, List<Individual>> paretoFront) {
        List<Solution> solutions=new ArrayList<>();
        DecimalFormat df   = new DecimalFormat("######0.00");
        int max = paretoFront.size();
        int count=0;
        for(int i = 1; i <= max; i++) {
            List<Individual> population = paretoFront.get(i);
            if(population != null && !population.isEmpty()) {
                for(Individual c : population) {
                    List<String> valves= new ArrayList<>();
                    for (int key:c.genes.keySet()){
                        if ((c.cost-0)<0.1){
                            continue;
                        }
                        if (c.genes.get(key)==1){
                            GISEdge edge= DataCache.gisGraph.edgeList.get(key>>1);
                            int gid=edge.getGid();
                            if ((key&1)==0) {
                                int fromId=DataCache.gisGraph.vertexList.get(edge.fromId).getGid();
                                valves.add(gid+"_"+fromId);
                            }
                            else {
                                int endId=DataCache.gisGraph.vertexList.get(edge.endId).getGid();
                                valves.add(gid+"_"+endId);
                            }
                        }
                    }
                    Solution solution=new Solution(count++,Double.valueOf(df.format(c.cost)),Double.valueOf(df.format(c.reliability)),valves);
                    solutions.add(solution);
                }
            }
        }
        return solutions;
    }
    //从元数据获取字段类型对应的值
    public static void getNodeTypeValue(INetManager netManager){
        try{
            List<DbMetaInfo> list = netManager.getWorkspace().getMetaManager().getMetas().getListDbMetaInfo();
            for (DbMetaInfo metaInfo:list){
                if (netManager.getNetName().equals(metaInfo.getLayername())){
                     List<DbMetaNetFeature> features= metaInfo.getNet();
                     for (DbMetaNetFeature feature:features){
                         if ("阀门".equals(feature.getDname())){
                             Configuration.TYPE_VALVE=feature.getDno();
                         }
                         if ("泵站".equals(feature.getDname())){
                             Configuration.TYPE_SOURCE=feature.getDno();
                         }
                         if ("节点".equals(feature.getDname())){
                             Configuration.TYPE_LINK_NODE=feature.getDno();
                         }
                     }
                }
            }
        }catch (Exception e){
            System.out.println("获取元数据失败");
            e.printStackTrace();
        }
    }
}
