package org.apache.ctakes.pipelines;

import org.apache.commons.io.FileUtils;
import org.apache.ctakes.utils.RushConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;


public class RushEndToEndPipelineTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void before() throws IOException {
        // make sure setup is correct, including a couple "hardcoded" paths
        FileUtils.forceMkdir(new File("/tmp/random")); // required for current implementation...

        Path link = Paths.get("/tmp/ctakes-config");
        if (Files.exists(link)) {
            Files.delete(link);
        }
        Files.createSymbolicLink(link, Paths.get("resources").toAbsolutePath());
    }

    @Test
    public void testPipeline() throws Exception {

        File inputDirectory = Paths.get("src/test/resources/input").toFile();
        File expectedXMIsDirectory = Paths.get("src/test/resources/expectedOutput/xmis/").toFile();
        File expectedCUIsDirectory = Paths.get("src/test/resources/expectedOutput/cuis/").toFile();

        File masterFolder = Paths.get("resources").toFile();
        File tempMasterFolder = folder.newFolder("tempMasterFolder");

        RushConfig config = new RushConfig(masterFolder.getAbsolutePath(),
                tempMasterFolder.getAbsolutePath());
        config.initialize();
        RushEndToEndPipeline pipeline = new RushEndToEndPipeline(config, true);

        for (File file : inputDirectory.listFiles()) {
            String t = FileUtils.readFileToString(file);
            CTakesResult result = pipeline.getResult(file.getAbsolutePath(), 1, t);

            String expectedOutput = FileUtils.readFileToString(new File(expectedXMIsDirectory, file.getName()));
            String expectedCuis = FileUtils.readFileToString(new File(expectedCUIsDirectory, file.getName()));

//            assertEquals(expectedOutput,result.getOutput()); //TODO find way to compare
            assertEquals(expectedCuis, result.getCuis());
        }
        System.out.println("Closing Pipeline");
        pipeline.close();
        config.close();
    }

}