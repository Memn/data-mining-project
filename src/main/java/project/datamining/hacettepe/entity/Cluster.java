package project.datamining.hacettepe.entity;

import java.util.*;

public class Cluster {

    private Map<Integer, Double> center = new HashMap<>();
    private Map<Integer, Double> sumsOfVectors = new HashMap<>();
    private int vectorCount = 0;
    private List<String> documentsAssignedToThisCluster = Collections.synchronizedList(new ArrayList<String>());

    protected Map<Integer, Double> getCenter() {
        return center;
    }

    protected Map<Integer, Double> getSumsOfVectors() {
        return sumsOfVectors;
    }

    public void addVector(project.datamining.hacettepe.entity.Vector vector) {
        if (sumsOfVectors.size() == 0) {
            for (Map.Entry<Integer, Double> weightByAxis: vector.getWeightsByAxis().entrySet()) {
                Integer axis = weightByAxis.getKey();
                Double weight = weightByAxis.getValue();
                sumsOfVectors.put(axis, weight);
            }
        } else {
            for (Map.Entry<Integer, Double> weightByAxis: vector.getWeightsByAxis().entrySet()) {
                Integer axis = weightByAxis.getKey();
                Double weight = weightByAxis.getValue();
                Double existingWeightOfIndex = sumsOfVectors.get(axis);
                existingWeightOfIndex = existingWeightOfIndex == null ? 0 : existingWeightOfIndex;
                weight = existingWeightOfIndex + weight;
                sumsOfVectors.put(axis, weight);
            }
        }
        vectorCount++;
    }

    public Map<Integer, Double> calculateCenterOfCluster() {
        for (Map.Entry<Integer, Double> weightByAxis: sumsOfVectors.entrySet()) {
            Integer axis = weightByAxis.getKey();
            Double weight = weightByAxis.getValue();
            center.put(axis, weight / vectorCount);
        }
        return center;
    }

    public void addAssignedDocument(String documentName) {
        documentsAssignedToThisCluster.add(documentName);
    }

    public List<String> getDocumentsAssignedToThisCluster() {
        return documentsAssignedToThisCluster;
    }
}
