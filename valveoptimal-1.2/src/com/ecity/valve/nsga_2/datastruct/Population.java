package com.ecity.valve.nsga_2.datastruct;

import java.util.List;

public class Population {
    public List<Individual> individuals;
    public Population(List<Individual> individuals) {
        this.individuals = individuals;
    }
    public Population() {
    }
}
