package project.datamining.hacettepe.entity;

import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.*;

public class WordDocumentCountMatrixTest {

    @Test
    public void addWordByDocumentName() throws Exception {
        WordDocumentCountMatrix wordDocumentCountMatrix = new WordDocumentCountMatrix();
        Map<Integer, Integer> countOfWords;
        Document document;
        Integer indexOfWord;

        // Add one document to word document count matrix
        document = new Document(new File("class1/document1"));
        document.addWord("word1");
        document.addWord("word2");
        document.addWord("word3");
        document.addWord("word1");
        wordDocumentCountMatrix.addDocument(document);
        countOfWords = wordDocumentCountMatrix.getCountsOfWordsOfDocuments().get(document.getName());
        assertNotNull(countOfWords);
        // for word1
        indexOfWord = wordDocumentCountMatrix.getVocabulary().getIndexOfWord("word1");
        assertNotNull(indexOfWord);
        assertEquals(new Integer(2), countOfWords.get(indexOfWord));
        // for word2
        indexOfWord = wordDocumentCountMatrix.getVocabulary().getIndexOfWord("word2");
        assertNotNull(indexOfWord);
        assertEquals(new Integer(1), countOfWords.get(indexOfWord));
        // for word3
        indexOfWord = wordDocumentCountMatrix.getVocabulary().getIndexOfWord("word3");
        assertNotNull(indexOfWord);
        assertEquals(new Integer(1), countOfWords.get(indexOfWord));
        // for non existing word in vocabulary
        indexOfWord = wordDocumentCountMatrix.getVocabulary().getIndexOfWord("nonExistingWord");
        assertNull(indexOfWord);

        assertEquals(1, wordDocumentCountMatrix.getCountsOfWordsOfDocuments().size());


        // Add another document to word document count matrix
        document = new Document(new File("class1/document2"));
        document.addWord("word1");
        document.addWord("word2");
        document.addWord("word4");
        document.addWord("word2");
        wordDocumentCountMatrix.addDocument(document);
        countOfWords = wordDocumentCountMatrix.getCountsOfWordsOfDocuments().get(document.getName());
        assertNotNull(countOfWords);
        // for word1
        indexOfWord = wordDocumentCountMatrix.getVocabulary().getIndexOfWord("word1");
        assertNotNull(indexOfWord);
        assertEquals(new Integer(1), countOfWords.get(indexOfWord));
        // for word2
        indexOfWord = wordDocumentCountMatrix.getVocabulary().getIndexOfWord("word2");
        assertNotNull(indexOfWord);
        assertEquals(new Integer(2), countOfWords.get(indexOfWord));
        // for word4
        indexOfWord = wordDocumentCountMatrix.getVocabulary().getIndexOfWord("word4");
        assertNotNull(indexOfWord);
        assertEquals(new Integer(1), countOfWords.get(indexOfWord));
        // for word3 which does not exist in this document but exists in vocabulary
        indexOfWord = wordDocumentCountMatrix.getVocabulary().getIndexOfWord("word3");
        assertNotNull(indexOfWord);
        assertNull(countOfWords.get(indexOfWord));

        assertEquals(2, wordDocumentCountMatrix.getCountsOfWordsOfDocuments().size());


        // Try to add document to word document count matrix with same document name
        document = new Document(new File("class1/document2"));
        document.addWord("word5");
        document.addWord("word1");
        document.addWord("word2");
        document.addWord("word3");
        wordDocumentCountMatrix.addDocument(document);

        assertEquals(2, wordDocumentCountMatrix.getCountsOfWordsOfDocuments().size());

        countOfWords = wordDocumentCountMatrix.getCountsOfWordsOfDocuments().get(document.getName());
        assertNotNull(countOfWords);
        // for word1, assert not changed
        indexOfWord = wordDocumentCountMatrix.getVocabulary().getIndexOfWord("word1");
        assertNotNull(indexOfWord);
        assertEquals(new Integer(1), countOfWords.get(indexOfWord));
        // for word2, assert not changed
        indexOfWord = wordDocumentCountMatrix.getVocabulary().getIndexOfWord("word2");
        assertNotNull(indexOfWord);
        assertEquals(new Integer(2), countOfWords.get(indexOfWord));
        // for word4, assert not changed
        indexOfWord = wordDocumentCountMatrix.getVocabulary().getIndexOfWord("word4");
        assertNotNull(indexOfWord);
        assertEquals(new Integer(1), countOfWords.get(indexOfWord));
        // for word3 which does not exist in this document but exists in vocabulary, assert not changed
        indexOfWord = wordDocumentCountMatrix.getVocabulary().getIndexOfWord("word3");
        assertNotNull(indexOfWord);
        assertNull(countOfWords.get(indexOfWord));
        // for word5 which should not have been added
        indexOfWord = wordDocumentCountMatrix.getVocabulary().getIndexOfWord("word5");
        assertNull(indexOfWord);
    }

}