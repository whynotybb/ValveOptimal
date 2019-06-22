package com.ecity.valve;

import com.ecity.datasource.ConnectionProperty;
import com.ecity.datasource.IWorkspace;
import com.ecity.datasource.WorkspaceFactory;
import com.ecity.define.common.DB_INFO;
import com.ecity.define.enums.EnumConnectionMode;
import com.ecity.geometry.Geometry;
import com.ecity.network.INetManager;
import com.ecity.valve.graph.gis.GISGraph;
import com.ecity.valve.graph.ne.NEGraph;
import com.ecity.valve.graph.ne.NEGraphFactory;
import com.ecity.valve.graph.sv.SVGraph;
import com.ecity.valve.nsga_2.alg.Synthesis;
import com.ecity.valve.util.DataUtil;
import com.ecity.valve.util.PropertiesUtil;
import com.ecity.valve.util.Solution;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        String json = "{\"rings\":[[[65063.315629854798,47930.059040861204],[65154.759614119306,47790.568217407912],[65797.897303125821,47590.97376219742],[65593.37458976265,47403.699952371418],[65206.50632472802,47241.067433312535],[65014.304256749339,47223.818529779091],[64827.030446924269,47211.497884392738],[64743.250058317557,46918.266524273902],[64605.258830027655,46918.266524273902],[64514.086054191925,47053.793623486534],[64469.731730812229,47142.502270244062],[64344.061147900298,47189.320722702891],[64050.829787779599,47167.143561013043],[64036.04501331877,47359.345628991723],[64336.66876067128,47531.834664359689]," +
                "[64467.267601734959,47554.011826049536],[64484.516505270265,47918.702929392457],[64907.492597530596,47994.163452895358],[65063.315629854798,47930.059040861204]]]}";
        testJson(json);
//        testNE(json);
    }

    private static void testJson(String json) throws Exception {
        IWorkspace workspace = getWorkSpace();
        Geometry geometry = Geometry.getInstance(json, Configuration.COORDINATE_WKID);
        INetManager netManager = workspace.getNetManager("net");
        List<Solution> solutions = ValveOptimal.run(geometry, netManager, "F:\\vp1.0\\application1.properties");
        for (Solution solution : solutions) {
            System.out.print(solution.getId() + ":" + solution.getCost() + ":" + solution.getReliability() + ":");
            for (String i : solution.getValveOnLines()) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
    }

    public static void testNE(String json) throws Exception {
        PropertiesUtil.load("F:\\vp1.0\\application1.properties");
        Geometry geometry = Geometry.getInstance(json, Configuration.COORDINATE_WKID);
        IWorkspace workspace = getWorkSpace();
        INetManager netManager = workspace.getNetManager("net");
        GISGraph gisGraph = DataUtil.builderByClip(netManager, geometry);
        NEGraph neGraph = NEGraphFactory.build(gisGraph);
        NEGraph neGraph1 = neGraph.copy();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 96000; i++) {
            SVGraph svGraph = new SVGraph(neGraph1);
            svGraph.getLD();
        }
//        System.out.println(svGraph.vertexList.size());
        long end = System.currentTimeMillis();
        System.out.println(end - start);
//       SVGraph svGraph=new SVGraph(neGraph);
//       System.out.println(svGraph.getLD());
    }

    public static IWorkspace getWorkSpace() {
        ConnectionProperty conn = new ConnectionProperty();
        conn.connectionmode = EnumConnectionMode.EnumConnectionModeGdb;
        conn.dbInfo = new DB_INFO("sql", "192.168.8.238:1433:lagisn", "sa", "YSD@city", 1, 2);
        IWorkspace workspace = null;
        try {
            workspace = WorkspaceFactory.getWorkspace(conn);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return workspace;
    }

    public static void testOnWhole() throws Exception {
        ValveOptimal.run(null, getWorkSpace().getNetManager("net"), ValveOptimal.class.getClassLoader().getResource("application1.properties").getPath());
    }
}
