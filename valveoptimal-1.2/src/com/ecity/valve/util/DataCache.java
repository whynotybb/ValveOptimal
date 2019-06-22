package com.ecity.valve.util;

import com.ecity.valve.graph.gis.GISGraph;
import com.ecity.valve.graph.ne.NEGraph;
import com.ecity.valve.graph.sv.SVGraph;
import com.ecity.valve.nsga_2.datastruct.Individual;

import java.util.Set;


public class DataCache {
    public static NEGraph neGraph;
    public static GISGraph gisGraph;
    public static SVGraph svGraph;
    public static Individual samest;
    public static Set<Integer> cantInstallPos;
    public static int[] oldLd=new int[3];
}
