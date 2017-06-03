package project.datamining.hacettepe.entity;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class ClusterTest {

    @Test
    public void testAddVectorAndCalculateCenterOfCluster() throws Exception {
        Cluster cluster = new Cluster();

        Vector vector = new Vector();
        vector.assignWeightForAxis(1, 5);
        cluster.addVector(vector);
        assertEquals(new Double(5), cluster.getSumsOfVectors().get(1));
        assertNull(cluster.getSumsOfVectors().get(0));

        vector = new Vector();
        vector.assignWeightForAxis(2, 3);
        cluster.addVector(vector);
        assertEquals(new Double(5), cluster.getSumsOfVectors().get(1));
        assertEquals(new Double(3), cluster.getSumsOfVectors().get(2));
        assertNull(cluster.getSumsOfVectors().get(0));

        vector = new Vector();
        vector.assignWeightForAxis(1, 3);
        cluster.addVector(vector);
        assertEquals(new Double(8), cluster.getSumsOfVectors().get(1));

        vector = new Vector();
        vector.assignWeightForAxis(3, 4);
        vector.assignWeightForAxis(0, 5);
        vector.assignWeightForAxis(5, 6);
        cluster.addVector(vector);
        assertEquals(new Double(5), cluster.getSumsOfVectors().get(0));
        assertEquals(new Double(8), cluster.getSumsOfVectors().get(1));
        assertEquals(new Double(3), cluster.getSumsOfVectors().get(2));
        assertEquals(new Double(4), cluster.getSumsOfVectors().get(3));
        assertNull(cluster.getSumsOfVectors().get(4));
        assertEquals(new Double(6), cluster.getSumsOfVectors().get(5));

        Map<Integer, Double> centerOfCluster = cluster.calculateCenterOfCluster();
        // Assert not changed
        assertEquals(new Double(5), cluster.getSumsOfVectors().get(0));
        assertEquals(new Double(8), cluster.getSumsOfVectors().get(1));
        assertEquals(new Double(3), cluster.getSumsOfVectors().get(2));
        assertEquals(new Double(4), cluster.getSumsOfVectors().get(3));
        assertNull(cluster.getSumsOfVectors().get(4));
        assertEquals(new Double(6), cluster.getSumsOfVectors().get(5));

        assertEquals(new Double(1.25), centerOfCluster.get(0));
        assertEquals(new Double(2), centerOfCluster.get(1));
        assertEquals(new Double(0.75), centerOfCluster.get(2));
        assertEquals(new Double(1), centerOfCluster.get(3));
        assertNull(centerOfCluster.get(4));
        assertEquals(new Double(1.5), centerOfCluster.get(5));

    }

}