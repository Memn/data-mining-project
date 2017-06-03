package project.datamining.hacettepe.util;

import project.datamining.hacettepe.entity.Cluster;
import project.datamining.hacettepe.entity.Document;

import java.util.List;
import java.util.Map;

public final class Measure {

    /**
     * |selected| = true positives + false positives <br>
     * the count of selected (or retrieved) items
     */
    private long selected = 0;

    /**
     * |target| = true positives + false negatives <br>
     * the count of target (or correct) items
     */
    private long target;

    private long truePositive = 0;

    private Map.Entry<String, Cluster> clusterNameClusterEntry;

    public Measure(Map.Entry<String, Cluster> clusterNameClusterEntry, int expectedCountOfThisCategory) {
        this.clusterNameClusterEntry = clusterNameClusterEntry;
        target = expectedCountOfThisCategory;
    }

    public void initScores() {
        String clusterName = clusterNameClusterEntry.getKey();
        List<String> documentsAssignedToThisCluster = clusterNameClusterEntry.getValue().getDocumentsAssignedToThisCluster();
        for (String documentName : documentsAssignedToThisCluster) {
            String actualClusterName = Document.getClassOfDocumentByDocumentName(documentName);
            if (actualClusterName.equals(clusterName)) {
                truePositive++;
            }
            selected++;
        }
    }

    public double getPrecisionScore() {
        return selected > 0 ? (double) truePositive / (double) selected : 0;
    }
    public double getRecallScore() {
        return target > 0 ? (double) truePositive / (double) target : 0;
    }

    /**
     *
     * @return the f-measure or -1 if precision + recall is less or equal to 0
     */
    public double getFMeasure() {

        if (getPrecisionScore() + getRecallScore() > 0) {
            return 2 * (getPrecisionScore() * getRecallScore())
                    / (getPrecisionScore() + getRecallScore());
        } else {
            // cannot divide by zero, return error code
            return -1;
        }
    }

    @Override
    public String toString() {
        return "Precision: " + Double.toString(getPrecisionScore()) + "\n"
                + "Recall: " + Double.toString(getRecallScore()) + "\n" + "F-Measure: "
                + Double.toString(getFMeasure());
    }

    public String getClusterName() {
        return clusterNameClusterEntry.getKey();
    }

    public Cluster getCluster() {
        return clusterNameClusterEntry.getValue();
    }
}
