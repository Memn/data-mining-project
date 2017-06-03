package project.datamining.hacettepe.entity;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class VectorSpaceTest {

    @Test
    public void testInitializeClusters() throws Exception {

        final Map<String, Vector> vectorsByDocumentName = new HashMap<>();

        Vector vector = new Vector();
        vector.assignWeightForAxis(1, 6);
        vectorsByDocumentName.put("category1/doc1", vector);

        vector = new Vector();
        vector.assignWeightForAxis(2, 3);
        vectorsByDocumentName.put("category1/doc2", vector);

        vector = new Vector();
        vector.assignWeightForAxis(1, 3);
        vectorsByDocumentName.put("category1/doc3", vector);


        vector = new Vector();
        vector.assignWeightForAxis(3, 4);
        vector.assignWeightForAxis(0, 5);
        vector.assignWeightForAxis(5, 6);
        vectorsByDocumentName.put("category2/doc1", vector);


        vector = new Vector();
        vector.assignWeightForAxis(1, 5);
        vectorsByDocumentName.put("category3/doc1", vector);


        vector = new Vector();
        vector.assignWeightForAxis(2, 3);
        vectorsByDocumentName.put("category3/doc2", vector);

        vector = new Vector();
        vector.assignWeightForAxis(1, 3);
        vectorsByDocumentName.put("category3/doc3", vector);

        vector = new Vector();
        vector.assignWeightForAxis(3, 4);
        vector.assignWeightForAxis(0, 5);
        vector.assignWeightForAxis(5, 6);
        vectorsByDocumentName.put("category3/doc4", vector);


        WordDocumentWeightMatrix wordDocumentWeightMatrix = new WordDocumentWeightMatrix() {
            @Override
            public Map<String, Vector> getVectorsByDocumentName() {
                return vectorsByDocumentName;
            }

            @Override
            public void writeToDirectoryWithFileName(String directory, String fileName) {
            }
        };

        VectorSpace vectorSpace = new VectorSpace(wordDocumentWeightMatrix);
        Map<String, Cluster> clusterMapByClusterName = vectorSpace.initializeClusters(false);
        assertEquals(3, clusterMapByClusterName.size());


        Cluster category1 = clusterMapByClusterName.get("category1");
        Map<Integer, Double> center = category1.getCenter();
        assertEquals(2, center.size());
        assertEquals(new Double(3), center.get(1));
        assertEquals(new Double(1), center.get(2));

        Cluster category2 = clusterMapByClusterName.get("category2");
        center = category2.getCenter();
        assertEquals(3, center.size());
        assertEquals(new Double(4), center.get(3));
        assertEquals(new Double(5), center.get(0));
        assertEquals(new Double(6), center.get(5));

        Cluster category3 = clusterMapByClusterName.get("category3");
        center = category3.getCenter();
        assertEquals(5, center.size());
        assertEquals(new Double(2), center.get(1));
        assertEquals(new Double(0.75), center.get(2));
        assertEquals(new Double(1.25), center.get(0));
        assertEquals(new Double(1), center.get(3));
        assertEquals(new Double(1.5), center.get(5));

    }

}