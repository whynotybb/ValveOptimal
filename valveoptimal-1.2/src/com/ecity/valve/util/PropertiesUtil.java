package com.ecity.valve.util;

import com.ecity.network.INetManager;
import com.ecity.valve.Configuration;

import java.io.*;
import java.util.Properties;

public class PropertiesUtil {
    public static void load(String configFile) throws Exception{
        if (configFile==null){
            throw  new FileNotFoundException("config文件为null,必须提供配置文件");
        }
        Properties properties=new Properties();
        InputStream in;
        try{
            in= new BufferedInputStream(new FileInputStream(configFile));
            properties.load(in);    //加载属性列表
            Configuration.FIELD_NAME_ID=properties.getProperty("FIELD_NAME_ID","gid");
            Configuration.FIELD_NAME_DIA=properties.getProperty("FIELD_NAME_DIA","管网规格");
            Configuration.FIELD_NAME_LENGTH=properties.getProperty("FIELD_NAME_LENGTH","管长");
            Configuration.FIELD_NAME_STNOD=properties.getProperty("FIELD_NAME_STNOD","stnod");
            Configuration.FIELD_NAME_EDNOD=properties.getProperty("FIELD_NAME_EDNOD","ednod");
            Configuration.FIELD_NAME_CBZQ=properties.getProperty("FIELD_NAME_CBZQ","抄表周期");
            Configuration.FIELD_NAME_DEMAND=properties.getProperty("FIELD_NAME_DEMAND","demand");
            Configuration.FIELD_NAME_NODETYPE=properties.getProperty("FIELD_NAME_NODETYPE","dno");
            Configuration.GENE_BE_ZERO=Double.valueOf(properties.getProperty("GENE_BE_ZERO","0.8"));
            Configuration.MAX_GENERATION=Integer.valueOf(properties.getProperty("MAX_GENERATION"));
            Configuration.POPULATION_SIZE=Integer.valueOf(properties.getProperty("POPULATION_SIZE","1000"));
            Configuration.CROSSOVER_Probility=Double.valueOf(properties.getProperty("CROSSOVER_Probility","0.5"));
            Configuration.MUTATION_Probility=Double.valueOf(properties.getProperty("MUTATION_Probility"));
            Configuration.COORDINATE_WKID=Integer.valueOf(properties.getProperty("COORDINATE_WKID","2383"));
            Configuration.SAVE_SOLUTIONS_FILEPATH=properties.getProperty("SAVE_SOLUTIONS_FILEPATH",configFile);
            Configuration.MAX_NODE_SIZE=Integer.valueOf(properties.getProperty("MAX_NODE_SIZE","1000"));
            Configuration.defaultDiameter=Integer.valueOf(properties.getProperty("defaultDiameter","25"));
            //配置用水量生成策略
            Configuration.DEMAND_CREATE_MODE=Integer.valueOf(properties.getProperty("DEMAND_CREATE_MODE","-1"));
            Configuration.NODE_DEMAND_DEFAULTVALUE=Integer.valueOf(properties.getProperty("NODE_DEMAND_DEFAULTVALUE","10"));
            if (properties.getProperty("SOURCE_NODE")!=null){
                Configuration.SOURCE_NODE=Integer.valueOf(properties.getProperty("SOURCE_NODE"));
            }
            //节点类型优先从配置文件中获取，如果没有配置，再从数据库中获取
            if (Boolean.valueOf(properties.getProperty("HAS_TYPE_CONFIG"))){
                Configuration.TYPE_VALVE = Integer.valueOf(properties.getProperty("TYPE_VALVE","3"));
                Configuration.TYPE_LINK_NODE = Integer.valueOf(properties.getProperty("TYPE_LINK_NODE","2"));
                Configuration.TYPE_SOURCE = Integer.valueOf(properties.getProperty("TYPE_SOURCE","4"));
            }
            Configuration.NEGRAPH_COPY_MODE=Integer.valueOf(properties.getProperty("NEGRAPH_COPY_MODE","1"));
            in.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    //dno类型值从netManager元数据获取
    public static void load(String configFile, INetManager netManager) throws Exception{
        load(configFile);
        DataUtil.getNodeTypeValue(netManager);
    }
}
