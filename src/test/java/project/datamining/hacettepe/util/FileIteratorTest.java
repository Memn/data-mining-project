package project.datamining.hacettepe.util;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class FileIteratorTest {

    @Test
    public void testGetTrainingFiles() {
        File file = new File("./src/test/resources/20news-bydate-train");
        List<File> trainingFiles = FileIterator.getFilesUnderFile(file);
        assertEquals(4, trainingFiles.size());
    }

}