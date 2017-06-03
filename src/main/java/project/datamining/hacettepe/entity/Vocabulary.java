package project.datamining.hacettepe.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vocabulary {

    private Map<String, Integer> wordsWithIndices = new HashMap<>();
    private List<String> words = new ArrayList<>();
    private int index = 0;

    /**
     *
     * @param word
     * @return index of the added word
     */
    public int addWordIfDoesNotExist(String word) {
        Integer correspondingIndexOfWordInVocabulary = wordsWithIndices.get(word);
        if (correspondingIndexOfWordInVocabulary == null) {
            correspondingIndexOfWordInVocabulary = index;
            wordsWithIndices.put(word, index++);
            words.add(word);
        }
        return correspondingIndexOfWordInVocabulary;
    }

    protected Integer getIndexOfWord(String word) {
        return wordsWithIndices.get(word);
    }

    protected Map<String, Integer> getWordsWithIndices() {
        return wordsWithIndices;
    }


    public String getWordAtIndex(Integer wordIndex) {
        return words.get(wordIndex);
    }
}
