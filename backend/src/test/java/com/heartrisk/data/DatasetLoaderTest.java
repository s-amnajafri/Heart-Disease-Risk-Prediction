package com.heartrisk.data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.heartrisk.config.ConfigManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** {@link DatasetLoader}: header handling, label mapping, "?" rows and error messages. */
class DatasetLoaderTest {

    private static final String HEADER = "age,sex,cp,trestbps,chol,fbs,restecg,thalach,exang,oldpeak,slope,ca,thal,target";

    @TempDir
    Path tempDir;

    private Path write(String... lines) throws IOException {
        return Files.write(tempDir.resolve("heart.csv"), String.join("\r\n", lines).getBytes());
    }

    @Test
    void targetZeroMeansDiseaseAndRowsWithMissingValuesAreSkipped() throws IOException {
        Path file = write(HEADER,
                "52,1,0,125,212,0,1,168,0,1,2,2,3,0",
                "58,0,0,100,248,0,0,122,0,1,1,0,2,1",
                "60,1,0,130,250,0,0,150,0,1.5,1,?,3,0",
                "61,1,0,130,250,0,0,150,0,1.5,1,1,,0",
                "");
        LabelledDataset data = new DatasetLoader().load(file);
        assertEquals(2, data.size());
        assertEquals(2, data.skippedRows());
        assertArrayEquals(new int[] {1, 0}, data.labels());
        assertEquals(52, data.records().get(0).getAge());
        assertEquals(1.0, data.records().get(0).getOldpeak(), 1e-9);
    }

    @Test
    void columnOrderInTheHeaderDoesNotMatter() throws IOException {
        Path file = write("target,thal,ca,slope,oldpeak,exang,thalach,restecg,fbs,chol,trestbps,cp,sex,age",
                "0,3,2,2,1,0,168,1,0,212,125,0,1,52");
        LabelledDataset data = new DatasetLoader().load(file);
        assertEquals(52, data.records().get(0).getAge());
        assertEquals(212, data.records().get(0).getChol());
    }

    @Test
    void outOfRangeValueReportsTheLineNumber() throws IOException {
        Path file = write(HEADER, "52,1,0,125,212,0,1,168,0,1,2,2,3,0", "999,1,0,125,212,0,1,168,0,1,2,2,3,0");
        IOException e = assertThrows(IOException.class, () -> new DatasetLoader().load(file));
        assertTrue(e.getMessage().contains("line 3"), e.getMessage());
    }

    @Test
    void missingFileAndMissingColumnAreReported() throws IOException {
        assertThrows(IOException.class, () -> new DatasetLoader().load(tempDir.resolve("nope.csv")));
        Path file = write("age,sex", "50,1");
        IOException e = assertThrows(IOException.class, () -> new DatasetLoader().load(file));
        assertTrue(e.getMessage().contains("Missing column"), e.getMessage());
    }

    @Test
    void splitIsReproducibleAndSeparatesTrainAndTest() throws IOException {
        Path file = write(HEADER, "52,1,0,125,212,0,1,168,0,1,2,2,3,0", "58,0,0,100,248,0,0,122,0,1,1,0,2,1",
                "50,1,0,120,200,0,1,150,0,1,1,0,2,0", "45,0,1,110,190,0,0,160,0,0,2,0,2,1",
                "66,1,0,150,260,0,1,120,1,2,1,2,3,0");
        LabelledDataset data = new DatasetLoader().load(file);
        LabelledDataset.Split a = data.split(0.8, 42);
        LabelledDataset.Split b = data.split(0.8, 42);
        assertEquals(4, a.train().size());
        assertEquals(1, a.test().size());
        assertArrayEquals(a.train().labels(), b.train().labels());
    }

    @Test
    void theSuppliedDatasetLoadsWithoutErrors() throws IOException {
        Path path = ConfigManager.getInstance().getDatasetPath();
        Assumptions.assumeTrue(Files.isRegularFile(path), "heart.csv not found - test skipped");
        LabelledDataset data = new DatasetLoader().load(path);
        assertTrue(data.size() > 0);
        assertTrue(java.util.Arrays.stream(data.labels()).anyMatch(l -> l == 1));
        assertTrue(java.util.Arrays.stream(data.labels()).anyMatch(l -> l == 0));
    }
}
