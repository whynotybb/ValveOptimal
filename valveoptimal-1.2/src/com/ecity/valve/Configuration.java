package com.ecity.valve;
import com.ecity.valve.nsga_2.objective.*;
import java.util.ArrayList;
import java.util.List;

//负责配置参数的声明，具体参数值从配置文件中读取，配置文件路径在src
public class Configuration {
    //设置算法所需的参数
    public static int GENE_LENGTH =2*102;//ne.edgeList.size*2-cantInstallPos;
    public static int POPULATION_SIZE=80;
    public static double CROSSOVER_Probility=0.75;
    public static double MUTATION_Probility=0.01;
    public static int MAX_NE_EDGE_ID=0;
    public static int MAX_NE_NODE_ID=0;
    public static List<IObjectiveFunction> getObjectives(){
        List<IObjectiveFunction> functions=new ArrayList<>();
        functions.add(new Cost());
        functions.add(new Reliability4());
        return functions;
    }
    public static int MAX_GENERATION=1200;
    //编码出现0的概率
    public static double GENE_BE_ZERO =0.80;
    //字段
    public static  String FIELD_NAME_ID;
    public static  String FIELD_NAME_DEMAND;
    public static  String FIELD_NAME_NODETYPE;
    public static  String FIELD_NAME_STNOD;
    public static  String FIELD_NAME_EDNOD;
    public static  String FIELD_NAME_CBZQ;
    public static  String FIELD_NAME_DIA;//口径
    public static  String FIELD_NAME_LENGTH;
    //字段值:从元数据中读取
    public static boolean HAS_TYPE_CONFIG=false;
    public static int TYPE_SOURCE=2;
    public static int TYPE_VALVE=1;
    public static int TYPE_LINK_NODE=0;
    public static int DEMAND_CREATE_MODE=-1;
    public static int NODE_DEMAND_DEFAULTVALUE;
    //裁剪后数据可能没有水源节点，这时需要从配置文件传参
    public static int SOURCE_NODE=0;
    //新增阀门结点后的最大结点规模，推荐设置为边最大编号的三倍
    public static   int MAX_NODE_SIZE=100*3;
    //默认数据坐标系
    public static int COORDINATE_WKID;
    //默认口径
    public static int defaultDiameter;
    public static int NEGRAPH_COPY_MODE;
    //解决方案保存地址
    public static String SAVE_SOLUTIONS_FILEPATH;
}
