package project.datamining.hacettepe.entity;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class WordDocumentCountMatrix {

    private final static Logger LOGGER = Logger.getLogger(WordDocumentCountMatrix.class);
    public static final String WORD_OCCURRED_IN_MULTIPLE_DIFFERENT_CATEGORIES = "WOIMDC";

    private Vocabulary vocabulary = new Vocabulary();
    private boolean isVocabularyProvided = false;
    private Map<String, Map<Integer, Integer>> countsOfWordsOfDocuments = new HashMap<>();
    private Map<Integer, Integer> occurrencesOfWordInDifferentDocuments = new HashMap<>();
    private Map<Integer, String> categoryNamesOfWords = new HashMap<>();
    private Map<String, Integer> wordCountsOfDocuments = new HashMap<>();

    public WordDocumentCountMatrix() {
    }

    public WordDocumentCountMatrix(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
        isVocabularyProvided = true;
    }

    public void addDocument(Document document) {
        String documentName = document.getName();
        Map<Integer, Integer> countsOfWords = countsOfWordsOfDocuments.get(documentName);
        if (countsOfWords != null) {
            LOGGER.warn("A document with same name is already added, skipping!");
            return;
        }
        countsOfWords = new HashMap<>();

        int indexOfWord;
        Integer countOfWord;
        wordCountsOfDocuments.put(documentName, document.getWords().size());
        for (String word : document.getWords()) {
            if (!isVocabularyProvided || vocabulary.getWordsWithIndices().containsKey(word)) {
                indexOfWord = vocabulary.addWordIfDoesNotExist(word);

                countOfWord = countsOfWords.get(indexOfWord);
                countOfWord = countOfWord == null ? 0 : countOfWord;
                countsOfWords.put(indexOfWord, countOfWord + 1);

                // Calculate number of occurrence of this word in different documents
                // so if it is the first occurrence of this word in this document,
                // increase the count of this word by one
                if (countOfWord == 0) {
                    Integer occurrenceOfWordInDifferentDocuments = occurrencesOfWordInDifferentDocuments.get(indexOfWord);
                    occurrenceOfWordInDifferentDocuments = occurrenceOfWordInDifferentDocuments == null ? 1
                            : occurrenceOfWordInDifferentDocuments + 1;
                    occurrencesOfWordInDifferentDocuments.put(indexOfWord, occurrenceOfWordInDifferentDocuments);
                }

                // Calculate if this word passed in different categories
                // so if it is the first occurrence of this word in this document,
                // increase the count of this word by one
                if (countOfWord == 0) {
                    String categoryName = categoryNamesOfWords.get(indexOfWord);
                    String classOfDocument = Document.getClassOfDocumentByDocumentName(documentName);
                    if (categoryName == null) {
                        categoryName = classOfDocument;
                    } else if (!categoryName.equals(classOfDocument)) {
                        categoryName = WORD_OCCURRED_IN_MULTIPLE_DIFFERENT_CATEGORIES;
                    }
                    categoryNamesOfWords.put(indexOfWord, categoryName);
                }
            }
        }
        countsOfWordsOfDocuments.put(documentName, countsOfWords);
    }

    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    protected Map<String, Map<Integer, Integer>> getCountsOfWordsOfDocuments() {
        return countsOfWordsOfDocuments;
    }

    public void writeWordDocumentCountMatrixToDirectory(String directory) {
        LOGGER.debug("---Writing word document count matrix---");
        for (Map.Entry<String, Map<Integer, Integer>> countsOfWordsOfDocument : countsOfWordsOfDocuments.entrySet())
        {
            writeWordCountsOfDocumentToFile(countsOfWordsOfDocument.getKey(), countsOfWordsOfDocument, directory);
        }
    }

    public void writeVocabularyToDirectory(String directory) {
        LOGGER.debug("---Writing vocabulary---");
        String documentName = "vocabulary";
        File file = new File(directory);
        file.mkdirs();
        String filePath = directory + "/" +  documentName;
        try{
            PrintWriter writer = new PrintWriter(filePath, "UTF-8");
            writer.println("--Word-- : --Corresponding Index Value--");
            for (Map.Entry<String, Integer> wordIndexEntry : vocabulary.getWordsWithIndices().entrySet())
            {
                writer.println(wordIndexEntry.getKey() + ":" + wordIndexEntry.getValue());
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeWordCountsOfDocumentToFile(String documentName, Map.Entry<String,
            Map<Integer, Integer>> countsOfWordsOfDocument, String directory) {
        String previousDocumentName = documentName.substring(0, documentName.lastIndexOf("/"));
        File file = new File(directory + "/" + previousDocumentName);
        file.mkdirs();
        String filePath = directory + "/" + documentName;
        if (new File(filePath).exists()) {
            LOGGER.debug("found duplicate document with same name: " + documentName);
        }
        try{
            PrintWriter writer = new PrintWriter(filePath, "UTF-8");
            writer.println("Word : # of Occurrence");
            for ( Map.Entry<Integer, Integer> wordIndexAndCountEntry : countsOfWordsOfDocument.getValue().entrySet()) {
                Integer wordIndex = wordIndexAndCountEntry.getKey();
                writer.println(vocabulary.getWordAtIndex(wordIndex) + ":" + wordIndexAndCountEntry.getValue());
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Map<Integer, Integer> getOccurrencesOfWordInDifferentDocuments() {
        return occurrencesOfWordInDifferentDocuments;
    }

    public boolean wordOccurredInMultipleCategories(int wordIndex) {
        String categoryName = categoryNamesOfWords.get(wordIndex);
        if (categoryName == null) {
            throw new RuntimeException("could not find word with index + " + wordIndex);
        } else {
            if (categoryName.equals(WORD_OCCURRED_IN_MULTIPLE_DIFFERENT_CATEGORIES)) {
                return false;
            } else {
                return true;
            }
        }
    }

    protected Map<String, Integer> getWordCountsOfDocuments() {
        return wordCountsOfDocuments;
    }
}
