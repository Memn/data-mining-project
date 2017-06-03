package project.datamining.hacettepe.entity;

import java.util.HashMap;
import java.util.Map;

public class Vector {

    // an axis here is the corresponding int value of a word
    private Map<Integer, Double> weightsByAxis = new HashMap<>();

    public void assignWeightForAxis(int axis, double weight) {
        weightsByAxis.put(axis, weight);
    }

    public Double getAxis(int wordIndex) {
        return weightsByAxis.get(wordIndex);
    }

    public Map<Integer, Double> getWeightsByAxis() {
        return weightsByAxis;
    }
}
