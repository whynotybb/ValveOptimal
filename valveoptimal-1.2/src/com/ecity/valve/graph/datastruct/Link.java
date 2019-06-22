package com.ecity.valve.graph.datastruct;

import java.io.Serializable;

public class Link implements Serializable {
    private static final long serialVersionUID = 7876123039725715123L;
    public int id;//对应顶点编号
    public double length;//对应管长
    public int valve;//对应两个关断单元的公共边-阀门
    public Link next;
//    public int pipeID;

    public Link(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Link(int id, int valve, double length) {
        this.id = id;
//        this.pipeID = pipeID;
        this.valve=valve;
        this.length = length;
    }

    public Link(int id, double l) {
        this.id = id;
        this.length = l;
    }

    public Link(int id, int valve) {
        this.id = id;
        this.valve=valve;
    }
}
