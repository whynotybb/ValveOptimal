package com.ecity.valve.graph.sv;

import com.ecity.valve.graph.Vertex;

public class SVertex extends Vertex {
   private static final long serialVersionUID = 7792133946653668343L;
   private double length;
   private double demand;
   private boolean visited;
   public SVertex(int id,double length, double demand) {
      super(id);
      this.length = length;
      this.demand = demand;
   }

   public double getLength() {
      return length;
   }

   public double getDemand() {
      return demand;
   }

   public boolean isVisited() {
      return visited;
   }

   public void setVisited(boolean visited) {
      this.visited = visited;
   }

   public void setLength(double length) {
      this.length = length;
   }

   public void setDemand(double demand) {
      this.demand = demand;
   }
}
