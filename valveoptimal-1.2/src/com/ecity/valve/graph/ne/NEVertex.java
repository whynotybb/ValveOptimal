package com.ecity.valve.graph.ne;


import com.ecity.valve.graph.Vertex;

public class NEVertex extends Vertex {
     private static final long serialVersionUID = -413032666940453054L;
     private int nodetype;
     private int lines;
     private double demand;//用水量
     public NEVertex(int id,int nodetype,int lines,double demand){
          super(id);
          this.nodetype=nodetype;
          this.demand=demand;
          this.lines=lines;
     }

     public int getNodetype() {
          return nodetype;
     }

     public void setNodetype(int nodetype) {
          this.nodetype = nodetype;
     }

     public int getLines() {
          return lines;
     }

     public void setLines(int lines) {
          this.lines = lines;
     }

     public double getDemand() {
          return demand;
     }

     public void setDemand(double demand) {
          this.demand = demand;
     }
}
