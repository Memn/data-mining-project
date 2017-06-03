package project.datamining.hacettepe;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import project.datamining.hacettepe.entity.Cluster;
import project.datamining.hacettepe.entity.Document;
import project.datamining.hacettepe.entity.VectorSpace;
import project.datamining.hacettepe.entity.Vocabulary;
import project.datamining.hacettepe.entity.WordDocumentCountMatrix;
import project.datamining.hacettepe.entity.WordDocumentWeightMatrix;
import project.datamining.hacettepe.entity.WordDocumentWeightMatrixWithCustomWeightingFunction;
import project.datamining.hacettepe.entity.WordDocumentWeightMatrixWithTfIdf;
import project.datamining.hacettepe.util.FileIterator;
import project.datamining.hacettepe.util.Measure;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Application {

    final static Logger LOGGER = Logger.getLogger(Application.class);
    private static final String outputDirectory = "target/out/";

    public static void main(String [] args) throws IOException {
        if (args.length != 1) {
            LOGGER.error("Please set root training dataset directory as the first parameter to this program. Aborting!");
            return;
        }
        String rootDocumentsDirectory = args[0];
        FileUtils.deleteDirectory(new File(outputDirectory));
        List<Measure> measuresOfTfIdf = runCategorizationWithWeightingFunction(rootDocumentsDirectory, WeightingFunction.TF_IDF);
        List<Measure> measuresOfCustomWF = runCategorizationWithWeightingFunction(rootDocumentsDirectory, WeightingFunction.CUSTOM);
        printMacroAverageOfMeasuresToDirectory(measuresOfTfIdf, measuresOfCustomWF, outputDirectory);
    }

    private static List<Measure> runCategorizationWithWeightingFunction(String rootDocumentsDirectory, WeightingFunction weightingFunction) throws IOException {
        String rootTrainingDocumentsDirectory = rootDocumentsDirectory + "/20news-bydate-train";

        File[] files = new File(rootTrainingDocumentsDirectory).listFiles();
        if (files == null) {
            LOGGER.error("No file found under directory \'" + rootTrainingDocumentsDirectory + "\'. Aborting!");
            return null;
        }
        String parentOutputDirectory = outputDirectory + weightingFunction.getValue() + "/";
        FileUtils.deleteDirectory(new File(parentOutputDirectory));

        WordDocumentCountMatrix wordDocumentCountMatrix = new WordDocumentCountMatrix();
        Vocabulary vocabularyOfTrainingDataSet;
        List<File> trainingFiles = FileIterator.getFilesUnderFile(new File(rootTrainingDocumentsDirectory));
        LOGGER.debug("Found " + trainingFiles.size() + " files under " + rootTrainingDocumentsDirectory);
        for (File file : trainingFiles) {
            Document document = new Document(file);
            document.initContentWithFile(file);
            wordDocumentCountMatrix.addDocument(document);
        }
        vocabularyOfTrainingDataSet = wordDocumentCountMatrix.getVocabulary();
        wordDocumentCountMatrix.writeWordDocumentCountMatrixToDirectory(parentOutputDirectory + "/word-counts/");
        wordDocumentCountMatrix.writeVocabularyToDirectory(parentOutputDirectory);

        WordDocumentWeightMatrix wordDocumentWeightMatrix;
        if (weightingFunction.equals(WeightingFunction.TF_IDF)) {
            wordDocumentWeightMatrix = new WordDocumentWeightMatrixWithTfIdf(wordDocumentCountMatrix);
        } else {
            wordDocumentWeightMatrix = new WordDocumentWeightMatrixWithCustomWeightingFunction(wordDocumentCountMatrix);
        }
        wordDocumentWeightMatrix.writeToDirectoryWithFileName(parentOutputDirectory + "/vectors/", "weights");

        VectorSpace vectorSpace = new VectorSpace(wordDocumentWeightMatrix);
        vectorSpace.initializeClusters(true);
        vectorSpace.writeToDirectoryWithFileName(parentOutputDirectory + "/vectors-20-centroid/");


        parentOutputDirectory = parentOutputDirectory + "/test/";
        String rootTestDocumentsDirectory = rootDocumentsDirectory + "/20news-bydate-test";

        files = new File(rootTestDocumentsDirectory).listFiles();
        if (files == null) {
            LOGGER.error("No file found under directory \'" + rootTestDocumentsDirectory + "\'. Aborting!");
            return null;
        }
        WordDocumentCountMatrix wordDocumentCountMatrixOfTestData = new WordDocumentCountMatrix(vocabularyOfTrainingDataSet);

        List<File> testFiles = FileIterator.getFilesUnderFile(new File(rootTestDocumentsDirectory));
        LOGGER.debug("Found " + testFiles.size() + " files under " + rootTestDocumentsDirectory);
        for (File file : testFiles) {
            Document document = new Document(file);
            document.initContentWithFile(file);
            wordDocumentCountMatrixOfTestData.addDocument(document);
        }
        wordDocumentCountMatrixOfTestData.writeWordDocumentCountMatrixToDirectory(parentOutputDirectory + "/word-counts/");
        wordDocumentCountMatrixOfTestData.writeVocabularyToDirectory(parentOutputDirectory);

        WordDocumentWeightMatrix wordDocumentWeightMatrixOfTestData =
                new WordDocumentWeightMatrixWithTfIdf(wordDocumentCountMatrixOfTestData);
        wordDocumentWeightMatrixOfTestData.writeToDirectoryWithFileName(parentOutputDirectory + "/vectors/", "weights");

        LOGGER.info("Categorizing test documents with existing clusters using " + weightingFunction.getValue() + " weight function. This might take a while...(approx: 2 minutes)");
        vectorSpace.assignDocumentsToRelatedClusters(wordDocumentWeightMatrixOfTestData);
        vectorSpace.printAssignedDocumentsToRelevantClustersInformationToDirectory(parentOutputDirectory);

        List<Measure> measures = new ArrayList<>();
        for (Map.Entry<String, Cluster> clusterNameClusterEntry : vectorSpace.getClustersByClusterName().entrySet()) {
            Integer targetCount = vectorSpace.getNumberOfDocumentsThatShouldBeCategorizedInThisCluster(clusterNameClusterEntry.getKey());
            Measure measure = new Measure(clusterNameClusterEntry, targetCount);
            measure.initScores();
            measures.add(measure);
        }
        printMeasuresToDirectory(measures, parentOutputDirectory);
        return measures;
    }

    public static void printMeasuresToDirectory(List<Measure> measures, String directory) {
        LOGGER.debug("---Writing measurement of clusters---");
        File file = new File(directory);
        file.mkdirs();
        String filePath = directory + "/measurement";
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(filePath, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        writer.println("Cluster Name : Recall Score : Precision Score: F1 Measure");
        for (Measure measure : measures) {
            NumberFormat formatter = new DecimalFormat("#0.0000");
            writer.println(measure.getClusterName() + " : " + formatter.format(measure.getRecallScore()) + " : "
                    + formatter.format(measure.getPrecisionScore()) + " : " + formatter.format(measure.getFMeasure()));
        }
        writer.close();;
    }

    public static void printMacroAverageOfMeasuresToDirectory(List<Measure> measures1, List<Measure> measures2, String directory) {
        LOGGER.debug("---Writing macro average measurement of clusters---");
        File file = new File(directory);
        file.mkdirs();
        String filePath = directory + "/macro-average-measurement";
        PrintWriter writer;
        try {
            writer = new PrintWriter(filePath, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        writer.println("Cluster Name : Macro Average Recall Score : Macro Average Precision Score: Macro Average F1 Measure");
        for (Measure measure1 : measures1) {
            for (Measure measure2 : measures2) {
                if (measure1.getClusterName().equals(measure2.getClusterName())) {
                    NumberFormat formatter = new DecimalFormat("#0.0000");
                    writer.println(measure1.getClusterName() + " : "
                            + formatter.format((measure1.getRecallScore() + measure2.getRecallScore()) / 2) + " : "
                            + formatter.format((measure1.getPrecisionScore() + measure2.getPrecisionScore()) / 2) + " : "
                            + formatter.format((measure1.getFMeasure() + measure2.getFMeasure()) / 2));
                    break;
                }
            }
        }
        writer.close();;
    }

    private enum WeightingFunction {
        TF_IDF("tf-idf"),
        CUSTOM("custom-wf");

        private String value;

        WeightingFunction(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
