package com.ecity.valve.graph.gis;


import com.ecity.valve.graph.Edge;

public class GISEdge extends Edge {
    private static final long serialVersionUID = 4606927912214245345L;
    private double dia;
    private double length;
    private int gid;

    public GISEdge(int id,int gid, int fromId, int endId, double dia, double length) {
        super(id, fromId, endId);
        this.dia = dia;
        this.length = length;
        this.gid = gid;
    }

    public double getDia() {
        return dia;
    }

    public void setDia(double dia) {
        this.dia = dia;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    @Override
    public String toString() {
        return "GISEdge{" +
                "id=" + id +
                ", stnod=" + fromId +
                ", ednod=" + endId +
                ", dia=" + dia +
                ", length=" + length +
                '}';
    }
}
