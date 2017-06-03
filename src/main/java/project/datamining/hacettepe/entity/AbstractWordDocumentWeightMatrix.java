package project.datamining.hacettepe.entity;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

public abstract class AbstractWordDocumentWeightMatrix implements WordDocumentWeightMatrix {

    final static Logger LOGGER = Logger.getLogger(WordDocumentWeightMatrixWithTfIdf.class);

    public void writeToDirectoryWithFileName(String directory, String fileName) {
        LOGGER.debug("---Writing word document weight matrix---");
        File file = new File(directory);
        file.mkdirs();
        String filePath = directory + "/" + fileName;
        try{
            PrintWriter writer = new PrintWriter(filePath, "UTF-8");
            writer.println("Document Name, Word Index, Weight");
            for ( Map.Entry<String, Vector> weightsOfWordsOfDocument : getVectorsByDocumentName().entrySet()) {
                String documentName = weightsOfWordsOfDocument.getKey();
                for (Map.Entry<Integer, Double> weightsOfWords : weightsOfWordsOfDocument.getValue().getWeightsByAxis().entrySet()) {
                    writer.println(documentName + ", " + weightsOfWords.getKey() + ", " + weightsOfWords.getValue());
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
