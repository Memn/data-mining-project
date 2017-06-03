package project.datamining.hacettepe.entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Document {
    public static final List<String> IGNORED_WORDS = new ArrayList<String>(){{
            add("From:");
            add("Subject:");
            add("Reply-To:");
            add("and");
            add("the");
            add("of");
    }};

    private String name;
    private List<String> words = new ArrayList<>();

    public Document(File file) {
        name = generateDocumentNameFromFile(file);
    }

    private static String generateDocumentNameFromFile(File file) {
        return file.getParentFile().getName() + "/" + file.getName();
    }

    public static String getClassOfDocumentByDocumentName(String documentName) {
        return documentName.substring(0, documentName.lastIndexOf("/"));
    }

    public String getName() {
        return name;
    }

    protected void addWord(String word) {
        words.add(word);
    }

    public List<String> getWords() {
        return words;
    }

    public void initContentWithFile(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] words = line.split(" ");
            for (String word : words) {
                word = word.replaceAll(">", "");
                word = word.replaceAll("\t","");
                if (!IGNORED_WORDS.contains(word) && !word.equals("")) {
                    addWord(word);
                }
            }
        }
    }
}
