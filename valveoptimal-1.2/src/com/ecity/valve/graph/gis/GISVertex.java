package com.ecity.valve.graph.gis;

import com.ecity.valve.graph.Vertex;

public class GISVertex extends Vertex {
    private static final long serialVersionUID = 606104807221768067L;
    private int gid;
    private int dno;
    private double demand;
    private boolean visited;

    public GISVertex(int id, int gid, int dno, double demand) {
        super(id);
        this.gid = gid;
        this.dno = dno;
        this.demand = demand;
        visited=false;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public int getDno() {
        return dno;
    }

    public void setDno(int dno) {
        this.dno = dno;
    }

    public double getDemand() {
        return demand;
    }

    public void setDemand(double demand) {
        this.demand = demand;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
