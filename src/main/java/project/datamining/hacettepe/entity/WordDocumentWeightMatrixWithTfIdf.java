package project.datamining.hacettepe.entity;

import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class WordDocumentWeightMatrixWithTfIdf extends AbstractWordDocumentWeightMatrix {

    final static Logger LOGGER = Logger.getLogger(WordDocumentWeightMatrixWithTfIdf.class);

    private Map<String, Vector> vectorsByDocumentName = new HashMap<>();
    private WordDocumentCountMatrix wordDocumentCountMatrix;

    public WordDocumentWeightMatrixWithTfIdf(WordDocumentCountMatrix wordDocumentCountMatrix) {
        this.wordDocumentCountMatrix = wordDocumentCountMatrix;
        calculateTfIdfWeights();
    }

    private void calculateTfIdfWeights() {
        LOGGER.debug("---Calculating tf idf weights---");
        Map<String, Map<Integer, Integer>> countsOfWordsByDocumentName = wordDocumentCountMatrix
                .getCountsOfWordsOfDocuments();
        int wordIndex;
        int wordCount;
        double termFrequency;
        double inverseDocumentFrequency;
        double weight;
        for (Map.Entry<String, Map<Integer, Integer>> countsOfWordsOfDocumentEntry : countsOfWordsByDocumentName.entrySet())
        {
            String documentName = countsOfWordsOfDocumentEntry.getKey();
            int totalWordsInThisDocument = wordDocumentCountMatrix.getWordCountsOfDocuments().get(documentName);
            for (Map.Entry<Integer, Integer> countsOfWordsEntry: countsOfWordsOfDocumentEntry.getValue().entrySet()) {
                wordIndex = countsOfWordsEntry.getKey();
                wordCount = countsOfWordsEntry.getValue();

                termFrequency = wordCount / (float) totalWordsInThisDocument;
                inverseDocumentFrequency = Math.log10( (float) countsOfWordsByDocumentName.size() /
                        (float) getNumberOfOccurrencesInDocumentsOfWord(wordIndex) );
                weight = termFrequency * inverseDocumentFrequency;

                // Formatting
                NumberFormat formatter = new DecimalFormat("#0.00000");
                String formattedWeightStr = formatter.format(weight);
                double parsedWeight = Double.parseDouble(formattedWeightStr);

                Vector weightVector = vectorsByDocumentName.get(documentName);
                if (weightVector == null) {
                    weightVector = new Vector();
                    weightVector.assignWeightForAxis(wordIndex, parsedWeight);
                    vectorsByDocumentName.put(documentName, weightVector);
                } else {
                    if (weightVector.getAxis(wordIndex) != null) {
                        throw new RuntimeException("think about this case!");
                    }
                    weightVector.assignWeightForAxis(wordIndex, parsedWeight);
                }
            }
        }
    }

    private int getNumberOfOccurrencesInDocumentsOfWord(Integer wordIndex) {
        Integer numberOfOccurrences = wordDocumentCountMatrix.getOccurrencesOfWordInDifferentDocuments().get(wordIndex);
        if (numberOfOccurrences == null) {
            throw new RuntimeException("There should be a bug while generating \'occurrencesOfWordInDifferentDocuments\'" +
                    " in \'WordDocumentCountMatrix\' class!");
        }
        return numberOfOccurrences;
    }

    @Override
    public Map<String, Vector> getVectorsByDocumentName() {
        return vectorsByDocumentName;
    }

}
