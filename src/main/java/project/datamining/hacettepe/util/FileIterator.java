package project.datamining.hacettepe.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileIterator {

    public static final List<String> IGNORED_FILE_NAMES = new ArrayList<String>(){{
        add(".DS_Store");
}};
    public static List<File> getFilesUnderFile(File file) {
        List<File> trainingFiles = new ArrayList<>();
        File[] files = file.listFiles();
        addAllFilesToProvidedList(files, trainingFiles);
        return trainingFiles;
    }

    private static void addAllFilesToProvidedList(File[] files, List<File> fileList) {
        for (File file : files) {
            if (file.isDirectory()) {
                addAllFilesToProvidedList(file.listFiles(), fileList);
            } else {
                if (!IGNORED_FILE_NAMES.contains(file.getName())) {
                    fileList.add(file);
                }
            }
        }
    }
}
