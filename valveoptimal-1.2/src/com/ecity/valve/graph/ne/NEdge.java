package com.ecity.valve.graph.ne;


import com.ecity.valve.graph.Edge;

public class NEdge extends Edge {
    private static final long serialVersionUID = -2564150204525217359L;
    private double length;
    private boolean visited;
    public NEdge(int id,int fromId,int endId,double length){
        super(id,fromId,endId);
        this.length=length;
        visited=false;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
