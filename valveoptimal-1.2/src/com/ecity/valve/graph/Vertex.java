package com.ecity.valve.graph;

import java.io.Serializable;

public class Vertex implements Serializable {
    private static final long serialVersionUID = -1528213713555850843L;
    public int id;
    public Vertex(int id) {
        this.id = id;
    }
}
