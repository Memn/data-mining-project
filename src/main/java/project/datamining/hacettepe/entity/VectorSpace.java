package project.datamining.hacettepe.entity;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VectorSpace {
    final static Logger LOGGER = Logger.getLogger(VectorSpace.class);

    public static final int EXPECTED_CENTROID_COUNT = 20;

    private Map<String, Cluster> clustersByClusterName = new HashMap<>();
    private Map<String, Integer> numberOfDocumentsThatShouldBeCategorizedInThisCategory = new HashMap<>();
    private WordDocumentWeightMatrix wordDocumentWeightMatrix;

    public VectorSpace(WordDocumentWeightMatrix wordDocumentWeightMatrix) {
        this.wordDocumentWeightMatrix = wordDocumentWeightMatrix;
    }

    public Map<String, Cluster> initializeClusters(boolean checkClusterSizeWithExpectedCentroidCount) {
        for (Map.Entry<String, Vector> vectorOfDocument : wordDocumentWeightMatrix.getVectorsByDocumentName().entrySet()) {
            String documentName = vectorOfDocument.getKey();
            String classOfDocument = Document.getClassOfDocumentByDocumentName(documentName);
            Cluster cluster = clustersByClusterName.get(classOfDocument);
            if (cluster == null) {
                cluster = new Cluster();
                cluster.addVector(vectorOfDocument.getValue());
                clustersByClusterName.put(classOfDocument, cluster);
            } else {
                cluster.addVector(vectorOfDocument.getValue());
            }
        }
        if (checkClusterSizeWithExpectedCentroidCount && clustersByClusterName.size() != EXPECTED_CENTROID_COUNT) {
            throw new RuntimeException("Real cluster size(" + clustersByClusterName.size()
                    + " is different from expected cluster size(" + EXPECTED_CENTROID_COUNT + ")");
        }
        for (Map.Entry<String, Cluster> stringClusterEntry : clustersByClusterName.entrySet()) {
            Cluster cluster = stringClusterEntry.getValue();
            cluster.calculateCenterOfCluster();
        }
        return clustersByClusterName;
    }

    public void assignDocumentsToRelatedClusters(WordDocumentWeightMatrix wordDocumentWeightMatrixOfTestData) {
        Map<String, Vector> vectorsByDocumentName = new HashMap<>(wordDocumentWeightMatrixOfTestData.getVectorsByDocumentName());
        int threadCount = 20;
        int sizeOfDocuments = vectorsByDocumentName.size();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Map<String, Vector>> smallerMaps = new ArrayList<>();

        int sizeOfEachMap = vectorsByDocumentName.size() / threadCount;
        for (int i = 0; i < threadCount; i++) {
            int addedEntryCount = 0;
            Map documentVectorMap = new HashMap();
            HashMap<String, Vector> unModifiedDocumentVectors = new HashMap<>(vectorsByDocumentName);
            for (Map.Entry<String, Vector> documentNameVectorEntry : unModifiedDocumentVectors.entrySet()) {
                if (addedEntryCount++ < sizeOfEachMap) {
                    documentVectorMap.put(documentNameVectorEntry.getKey(), documentNameVectorEntry.getValue());
                    vectorsByDocumentName.remove(documentNameVectorEntry.getKey());
                } else if (i == threadCount -1) {
                    documentVectorMap.put(documentNameVectorEntry.getKey(), documentNameVectorEntry.getValue());
                    vectorsByDocumentName.remove(documentNameVectorEntry.getKey());
                }
            }
            smallerMaps.add(i, documentVectorMap);
        }

        int totalSize = 0;
        for (Map<String , Vector> documentVectors : smallerMaps) {
            totalSize+= documentVectors.size();
            LOGGER.trace("Size of each partitioned map for parallel processing: " + documentVectors.size());
        }

        if (totalSize != sizeOfDocuments) {
            throw new RuntimeException("there is a miscalculation, there are missing entries in smaller maps! " +
                    "sizeOfDocuments: " + sizeOfDocuments + ", accumulated size: " + totalSize);
        }

        for (Map<String, Vector> stringVectorMap : smallerMaps) {
            executor.execute(new AssignDocumentToClusterTask(stringVectorMap));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class AssignDocumentToClusterTask implements Runnable {

        Map<String, Vector> vectorsByDocumentName;
        public AssignDocumentToClusterTask(Map<String, Vector> vectorsByDocumentName) {
            this.vectorsByDocumentName = vectorsByDocumentName;

        }

        @Override
        public void run() {
            for (Map.Entry<String, Vector> documentNameVectorEntry : vectorsByDocumentName.entrySet()) {
                String documentName = documentNameVectorEntry.getKey();
                Vector vector = documentNameVectorEntry.getValue();
                Cluster cluster = getSimilarClusterForVectorWithCosineSimilarity(vector, documentName);
                cluster.addAssignedDocument(documentName);
                addRealCategorizationOfDocumentForMeasurement(documentName);
            }
        }
    }

    private void addRealCategorizationOfDocumentForMeasurement(String documentName) {
        String realCategoryOfDocument = Document.getClassOfDocumentByDocumentName(documentName);
        Integer numberOfDocumentsInThisCategory = numberOfDocumentsThatShouldBeCategorizedInThisCategory
                .get(realCategoryOfDocument);
        numberOfDocumentsInThisCategory = numberOfDocumentsInThisCategory == null ? 0 : numberOfDocumentsInThisCategory;
        numberOfDocumentsThatShouldBeCategorizedInThisCategory.put(realCategoryOfDocument, numberOfDocumentsInThisCategory+1);
    }

    private Cluster getSimilarClusterForVectorWithCosineSimilarity(
        Vector vector, String documentName) {
        double maximumCosineSimilarity = 0;
        double cosineSimilarity;
        Cluster mostSimilarCluster = null;
        String clusterName = "";
        for (Map.Entry<String, Cluster> clusterNameClusterEntry : clustersByClusterName.entrySet()) {
            Cluster currentCluster = clusterNameClusterEntry.getValue();
            cosineSimilarity = calculateCosineSimilarityOfVectorWithCluster(currentCluster, vector);
            if (cosineSimilarity >= maximumCosineSimilarity) {
                maximumCosineSimilarity = cosineSimilarity;
                mostSimilarCluster = currentCluster;
                clusterName = clusterNameClusterEntry.getKey();
            }
        }
        LOGGER.trace("Assigned document " + documentName + " to cluster: " + clusterName);
        return mostSimilarCluster;
    }

    private double calculateCosineSimilarityOfVectorWithCluster(Cluster cluster, Vector vector) {
        Map<Integer, Double> centerVector = cluster.getCenter();
        Map<Integer, Double> comparedVector = vector.getWeightsByAxis();
        if (centerVector == null || comparedVector == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }

        Set<Integer> intersectionIndices = getIntersectionIndices(centerVector, comparedVector);

        double dotProduct = dot(centerVector, comparedVector, intersectionIndices);
        double d1 = 0.0d;
        for (Double value : centerVector.values()) {
            d1 += Math.pow(value, 2);
        }
        double d2 = 0.0d;
        for (Double value : comparedVector.values()) {
            d2 += Math.pow(value, 2);
        }
        double cosineSimilarity;
        if (d1 <= 0.0 || d2 <= 0.0) {
            cosineSimilarity = 0.0;
        } else {
            cosineSimilarity = dotProduct / (Math.sqrt(d1) * Math.sqrt(d2));
        }
        return cosineSimilarity;
    }

    private Set<Integer> getIntersectionIndices(Map<Integer, Double> leftVector,
                                                Map<Integer, Double> rightVector) {
        Set<Integer> intersectionIndices = new HashSet<>(leftVector.keySet());
        intersectionIndices.retainAll(rightVector.keySet());
        return intersectionIndices;
    }

    private double dot(Map<Integer, Double> leftVector, Map<Integer, Double> rightVector,
                       Set<Integer> intersectionIndices) {
        double dotProduct = 0;
        for (Integer key : intersectionIndices) {
            dotProduct += leftVector.get(key) * rightVector.get(key);
        }
        return dotProduct;
    }

    public void printAssignedDocumentsToRelevantClustersInformationToDirectory(String directory) {
        LOGGER.debug("---Writing categorization of documents---");
        File file = new File(directory);
        file.mkdirs();
        String filePath = directory + "/categorization";
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filePath, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        writer.println("Cluster Name : Assigned Document Name");
        for (Map.Entry<String, Cluster> clusterNameAndClusterEntry : clustersByClusterName.entrySet()) {
            String clusterName = clusterNameAndClusterEntry.getKey();
            Cluster cluster = clusterNameAndClusterEntry.getValue();
            for ( String documentName :cluster.getDocumentsAssignedToThisCluster()) {
                writer.println(clusterName + " : " + documentName);
            }
        }
        writer.close();
    }

    public void writeToDirectoryWithFileName(String directory) {
        LOGGER.debug("---Writing vector space with centroids matrix---");
        File file = new File(directory);
        file.mkdirs();
        for (Map.Entry<String, Cluster> clusterNameAndClusterEntry : clustersByClusterName.entrySet()) {
            String clusterName = clusterNameAndClusterEntry.getKey();
            Cluster cluster = clusterNameAndClusterEntry.getValue();
            String filePath = directory + "/" + clusterName;
            try{
                PrintWriter writer = new PrintWriter(filePath, "UTF-8");
                writer.println("Axis(Word Index): Average Weight");
                for ( Map.Entry<Integer, Double> averageWeightOfAxis :cluster.getCenter().entrySet()) {
                    writer.println(averageWeightOfAxis.getKey() + ":" + averageWeightOfAxis.getValue());
                }
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Integer getNumberOfDocumentsThatShouldBeCategorizedInThisCluster(String clusterName) {
        return numberOfDocumentsThatShouldBeCategorizedInThisCategory.get(clusterName);
    }

    public Map<String, Cluster> getClustersByClusterName() {
        return clustersByClusterName;
    }
}
