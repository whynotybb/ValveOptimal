package com.ecity.valve.nsga_2.objective;

import com.ecity.valve.nsga_2.datastruct.Individual;

public interface IObjectiveFunction {
    double calculateObjectiveValue(Individual individual);
}
