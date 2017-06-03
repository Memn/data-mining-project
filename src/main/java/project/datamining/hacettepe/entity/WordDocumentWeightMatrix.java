package project.datamining.hacettepe.entity;

import java.util.Map;

public interface WordDocumentWeightMatrix {

    Map<String, Vector> getVectorsByDocumentName();

    void writeToDirectoryWithFileName(String directory, String fileName);
}
