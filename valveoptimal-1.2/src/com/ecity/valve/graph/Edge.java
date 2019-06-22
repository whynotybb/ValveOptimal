package com.ecity.valve.graph;

import java.io.Serializable;

public class Edge implements Serializable {
  private static final long serialVersionUID = 7245802523528929000L;
  public int id;
  public int fromId;
  public int endId;

  public Edge(int id, int fromId, int endId) {
    this.id = id;
    this.fromId = fromId;
    this.endId = endId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getFromId() {
    return fromId;
  }

  public void setFromId(int fromId) {
    this.fromId = fromId;
  }

  public int getEndId() {
    return endId;
  }

  public void setEndId(int endId) {
    this.endId = endId;
  }
}
