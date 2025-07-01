package com.example.cliserver.backend.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.cliserver.backend.database.mongoDB.PipelineRunsDao;
import com.example.cliserver.backend.model.PipelineConfig;
import com.example.cliserver.backend.model.PipelineConfig.Pipeline;
import com.example.cliserver.backend.model.ValidationResult;
import com.example.cliserver.backend.utils.ConfigurationValidator;
import com.example.cliserver.backend.utils.Constants;
import com.mongodb.client.FindIterable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StatusCommandTest {

    @TempDir
    Path tempDir;

    @Mock
    private PipelineRunsDao pipelineRunsDao;

    @Mock
    private FindIterable<Document> findIterable;

    private StatusCommand statusCommand;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        statusCommand = new StatusCommand(pipelineRunsDao);
    }

    @Test
    public void testPrintPipelineStatusWithDefaultRepo() throws IOException {
        String repo = null;
        String branch = "main";
        String commit = "abc123";
        String pipelineName = "test-pipeline";
        String validYaml = createValidYaml();
        Path pipelineFile = tempDir.resolve("valid-pipeline.yaml");
        Files.write(pipelineFile, validYaml.getBytes());
        setupConfigValidatorMock(pipelineName);

        Document run = createTestPipelineRun(pipelineName, "COMPLETED", 1);
        when(pipelineRunsDao.findActiveRuns(eq(Constants.LOCAL_REPO), eq(pipelineName), eq(null))).thenReturn(findIterable);
        doAnswer(invocation -> {
            ((List<Document>) invocation.getArgument(0)).clear();
            return null;
        }).when(findIterable).into(any(List.class));

        FindIterable<Document> localFindIterable = mock(FindIterable.class);
        when(pipelineRunsDao.getTimeFilteredPipelineRunReports(eq(Constants.LOCAL_REPO), eq(pipelineName), eq(null))).thenReturn(localFindIterable);
        when(localFindIterable.first()).thenReturn(run);

        String result = statusCommand.printPipelineStatus(repo, branch, commit, pipelineFile.toString(), null);
        assertTrue(result.contains("Pipeline: test-pipeline"));
    }

    @Test
    public void testPrintPipelineStatus_NoRunsFound() {
        when(pipelineRunsDao.findActiveRuns(eq("repo"), eq(null), eq(null))).thenReturn(findIterable);
        doAnswer(invocation -> {
            ((List<Document>) invocation.getArgument(0)).clear();
            return null;
        }).when(findIterable).into(any(List.class));

        FindIterable<Document> emptyFind = mock(FindIterable.class);
        when(pipelineRunsDao.getTimeFilteredPipelineRunReports(eq("repo"), eq(null), eq(null))).thenReturn(emptyFind);
        when(emptyFind.first()).thenReturn(null);

        String result = statusCommand.printPipelineStatus("repo", "main", "commit", "", null);
        assertEquals("No pipeline runs found.", result);
    }

    @Test
    public void testFormatRunStatus() throws Exception {
        Document pipelineRun = createTestPipelineRun("test-pipeline", "COMPLETED", 3);
        Method method = StatusCommand.class.getDeclaredMethod("formatRunStatus", Document.class);
        method.setAccessible(true);
        String result = (String) method.invoke(statusCommand, pipelineRun);

        assertTrue(result.contains("Pipeline: test-pipeline"));
        assertTrue(result.contains("Status: COMPLETED"));
        assertTrue(result.contains("Run Number: 3"));
        assertTrue(result.contains("Build:"));
        assertTrue(result.contains("Test:"));
    }

    @Test
    public void testFormatRunStatusWithNoStages() throws Exception {
        Document pipelineRun = new Document();
        pipelineRun.put(Constants.PIPELINE_NAME_FIELD, "test-pipeline");
        pipelineRun.put(Constants.PIPELINE_STATUS_FIELD, "COMPLETED");
        pipelineRun.put(Constants.RUN_NUMBER_FIELD, 4);

        Method method = StatusCommand.class.getDeclaredMethod("formatRunStatus", Document.class);
        method.setAccessible(true);
        String result = (String) method.invoke(statusCommand, pipelineRun);

        assertEquals("No stages found in pipeline run.", result);
    }

    private void setupConfigValidatorMock(String pipelineName) {
        ConfigurationValidator validatorMock = mock(ConfigurationValidator.class);
        ValidationResult resultMock = mock(ValidationResult.class);
        PipelineConfig configMock = mock(PipelineConfig.class);
        Pipeline pipelineMock = mock(Pipeline.class);

        when(pipelineMock.getName()).thenReturn(pipelineName);
        when(configMock.getPipeline()).thenReturn(pipelineMock);
        when(resultMock.getConfig()).thenReturn(configMock);
        when(validatorMock.validateYaml(any(), any(), any(), any())).thenReturn(resultMock);
    }

    private Document createTestPipelineRun(String pipelineName, String status, int runNumber) {
        Document pipelineRun = new Document();
        pipelineRun.put(Constants.PIPELINE_NAME_FIELD, pipelineName);
        pipelineRun.put(Constants.PIPELINE_STATUS_FIELD, status);
        pipelineRun.put(Constants.RUN_NUMBER_FIELD, runNumber);

        List<Document> stages = new ArrayList<>();

        Document buildStage = new Document();
        buildStage.put(Constants.STAGE_NAME_FIELD, "Build");
        buildStage.put(Constants.STAGE_STATUS_FIELD, "COMPLETED");
        List<Document> buildJobs = new ArrayList<>();
        Document compileJob = new Document();
        compileJob.put(Constants.JOB_NAME_FIELD, "compile");
        compileJob.put(Constants.JOB_STATUS_FIELD, "COMPLETED");
        buildJobs.add(compileJob);
        buildStage.put(Constants.JOBS_FIELD, buildJobs);

        Document testStage = new Document();
        testStage.put(Constants.STAGE_NAME_FIELD, "Test");
        testStage.put(Constants.STAGE_STATUS_FIELD, "COMPLETED");
        List<Document> testJobs = new ArrayList<>();
        Document testJob = new Document();
        testJob.put(Constants.JOB_NAME_FIELD, "unit-test");
        testJob.put(Constants.JOB_STATUS_FIELD, "COMPLETED");
        testJobs.add(testJob);
        testStage.put(Constants.JOBS_FIELD, testJobs);

        stages.add(buildStage);
        stages.add(testStage);
        pipelineRun.put(Constants.STAGES_FIELD, stages);

        return pipelineRun;
    }

    private String createValidYaml() {
        return "pipeline:\n" +
                "  name: test-pipeline\n" +
                "  stages: [build, test, deploy]\n" +
                "  jobs:\n" +
                "    - name: build-job\n" +
                "      stage: build\n" +
                "      script: [echo \"Building...\"]\n" +
                "    - name: test-job\n" +
                "      stage: test\n" +
                "      script: [echo \"Testing...\"]\n" +
                "      needs: [build-job]\n" +
                "    - name: deploy-job\n" +
                "      stage: deploy\n" +
                "      script: [echo \"Deploying...\"]\n" +
                "      needs: [test-job]";
    }
}
