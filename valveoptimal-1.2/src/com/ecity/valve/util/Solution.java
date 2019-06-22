package com.ecity.valve.util;

import java.util.List;

public class Solution {
    private int id;
    private double cost;
    private double reliability;
    //起点：返回边编号的两倍，终点：返回边编号的两倍加一
    private List<String> valveOnLines;

    public Solution(int id, double cost, double reliability, List<String> valveOnLines) {
        this.id = id;
        this.cost = cost;
        this.reliability = reliability;
        this.valveOnLines = valveOnLines;
    }

    public Solution() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getReliability() {
        return reliability;
    }

    public void setReliability(double reliability) {
        this.reliability = reliability;
    }

    public List<String> getValveOnLines() {
        return valveOnLines;
    }
}
