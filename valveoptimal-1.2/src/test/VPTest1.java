package test;

import com.ecity.valve.ValveOptimal;
import com.ecity.valve.graph.gis.GISGraph;
import com.ecity.valve.util.DataUtil;
import com.ecity.valve.util.PropertiesUtil;
import com.ecity.valve.util.Solution;

import java.util.List;

public class VPTest1 {
    public static void main(String[] args) throws Exception{
        GISGraph graph= getGISGraph();
        List<Solution> solutions= ValveOptimal.run(graph);
        for (Solution solution:solutions){
            System.out.print(solution.getId()+" "+solution.getCost()+" "+solution.getReliability());
            List<String> valves= solution.getValveOnLines();
            for (String valve:valves){
                System.out.print(" "+valve+",");
            }
            System.out.println();
        }
    }
    private static GISGraph getGISGraph() throws Exception{
        String path= "F:\\创新研究院\\data\\la_la";
        PropertiesUtil.load("F:\\vp1.0\\application2.properties");
        return DataUtil.buildLocal(path+"\\node.dbf",path+"\\"+"line.dbf");
    }
}
