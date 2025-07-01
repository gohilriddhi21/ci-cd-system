package com.example.cliserver.backend.commands.runCommand;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.cliserver.backend.database.mongoDB.PipelineRunsDao;
import com.example.cliserver.backend.messaging.PipelinePublisher;
import com.example.cliserver.backend.model.*;
import com.example.cliserver.backend.utils.ConfigurationValidator;
import com.example.cliserver.backend.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class RunCommandTest {

    @TempDir
    Path tempDir;

    @Mock
    private PipelineRunsDao pipelineRunsDao;

    @Mock
    private ConfigurationValidator configurationValidator;

    private RunCommand runCommand;
    private Path pipelineFile;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        runCommand = new RunCommand(pipelineRunsDao);

        // Create directory for pipeline files
        Path directoryPath = tempDir.resolve("pipelines");
        Files.createDirectories(directoryPath);

        // Create a valid pipeline YAML file
        String validYaml = createValidYaml();
        pipelineFile = tempDir.resolve("valid-pipeline.yaml");
        Files.write(pipelineFile, validYaml.getBytes());
    }

    @Test
    public void testRunWithInvalidConfiguration() {
        // Prepare
        String fileName = pipelineFile.toString();
        String repo = Constants.LOCAL_REPO;
        String branch = "main";
        String commit = "abc123";

        // Mock validation result to be invalid
        ValidationResult mockValidationResult = mock(ValidationResult.class);
        when(mockValidationResult.isValid()).thenReturn(false);
        try (MockedConstruction<ConfigurationValidator> mocked = mockConstruction(
            ConfigurationValidator.class,
            (mock, context) -> {
                when(mock.validateYaml(eq(fileName), eq(repo), eq(branch), eq(commit)))
                    .thenReturn(mockValidationResult);
            })) {

            String result = runCommand.run(fileName, repo, branch, commit, false);
            ArgumentCaptor<PipelineRun> pipelineRunCaptor = ArgumentCaptor.forClass(PipelineRun.class);
            verify(pipelineRunsDao, times(1)).updatePipelineRun(pipelineRunCaptor.capture());

            PipelineRun capturedRun = pipelineRunCaptor.getValue();
            assertEquals(Status.FAILED, capturedRun.getPipelineStatus());
            assertNotNull(capturedRun.getCompletionTime());
        }
    }

    @Test
    public void testRunWithNullConfiguration() {
        // Prepare
        String fileName = pipelineFile.toString();
        String repo = Constants.LOCAL_REPO;
        String branch = "main";
        String commit = "abc123";

        // Mock validation result to be valid but with null config
        ValidationResult mockValidationResult = mock(ValidationResult.class);
        when(mockValidationResult.isValid()).thenReturn(true);
        when(mockValidationResult.getConfig()).thenReturn(null);

        try (MockedConstruction<ConfigurationValidator> mocked = mockConstruction(
            ConfigurationValidator.class,
            (mock, context) -> {
                when(mock.validateYaml(eq(fileName), eq(repo), eq(branch), eq(commit)))
                    .thenReturn(mockValidationResult);
            })) {

            // Execute
            String result = runCommand.run(fileName, repo, branch, commit, false);

            // Verify
            assertEquals("Failed to load pipeline configuration.", result);

            // Verify pipeline run was updated with FAILED status
            ArgumentCaptor<PipelineRun> pipelineRunCaptor = ArgumentCaptor.forClass(PipelineRun.class);
            verify(pipelineRunsDao, times(1)).updatePipelineRun(pipelineRunCaptor.capture());

            PipelineRun capturedRun = pipelineRunCaptor.getValue();
            assertEquals(Status.FAILED, capturedRun.getPipelineStatus());
            assertNotNull(capturedRun.getCompletionTime());
        }
    }

    @Test
    public void testRunWithValidConfigurationVerboseLogging() {
        // Prepare
        String fileName = pipelineFile.toString();
        String repo = Constants.LOCAL_REPO;
        String branch = "main";
        String commit = "abc123";

        // Set up valid pipeline configuration
        PipelineConfig.Pipeline pipeline = createMockPipeline("test-pipeline");
        PipelineConfig mockConfig = mock(PipelineConfig.class);
        when(mockConfig.getPipeline()).thenReturn(pipeline);

        ValidationResult mockValidationResult = mock(ValidationResult.class);
        when(mockValidationResult.isValid()).thenReturn(true);
        when(mockValidationResult.getConfig()).thenReturn(mockConfig);

        // Mock PipelineRunner
        PipelineRunner mockRunner = mock(PipelineRunner.class);
        when(mockRunner.runPipeline(any(PipelineRun.class), eq(true))).thenReturn("Pipeline execution complete");

        try (MockedConstruction<ConfigurationValidator> mockedValidator = mockConstruction(
            ConfigurationValidator.class,
            (mock, context) -> {
                when(mock.validateYaml(eq(fileName), eq(repo), eq(branch), eq(commit)))
                    .thenReturn(mockValidationResult);
            });
            MockedConstruction<PipelineRunner> mockedRunner = mockConstruction(
                PipelineRunner.class,
                (mock, context) -> {
                    when(mock.runPipeline(any(PipelineRun.class), eq(true)))
                        .thenReturn("Pipeline execution complete");
                })
        ) {
            // Mock run number for the pipeline
            when(pipelineRunsDao.getRunNumber(eq("test-pipeline"), eq(repo))).thenReturn(5);

            // Execute with verbose logging
            String result = runCommand.run(fileName, repo, branch, commit, true);

            // Verify
            assertEquals("Pipeline execution complete", result);

            // Verify pipeline run was initialized correctly
            ArgumentCaptor<PipelineRun> pipelineRunCaptor = ArgumentCaptor.forClass(PipelineRun.class);
            verify(pipelineRunsDao, times(1)).updatePipelineRun(pipelineRunCaptor.capture());

            PipelineRun capturedRun = pipelineRunCaptor.getValue();
            assertEquals("test-pipeline", capturedRun.getPipelineName());
            assertEquals(Status.PENDING, capturedRun.getPipelineStatus());
            assertEquals(5, capturedRun.getRunNumber());
            assertEquals(fileName, capturedRun.getFileName());

            // Verify stages were initialized
            assertNotNull(capturedRun.getStages());
        }
    }

    @Test
    public void testRunWithValidConfigurationAsynchronous() {
        // Prepare
        String fileName = pipelineFile.toString();
        String repo = Constants.LOCAL_REPO;
        String branch = "main";
        String commit = "abc123";

        // Set up valid pipeline configuration
        PipelineConfig.Pipeline pipeline = createMockPipeline("test-pipeline");
        PipelineConfig mockConfig = mock(PipelineConfig.class);
        when(mockConfig.getPipeline()).thenReturn(pipeline);

        ValidationResult mockValidationResult = mock(ValidationResult.class);
        when(mockValidationResult.isValid()).thenReturn(true);
        when(mockValidationResult.getConfig()).thenReturn(mockConfig);

        try (MockedConstruction<ConfigurationValidator> mockedValidator = mockConstruction(
            ConfigurationValidator.class,
            (mock, context) -> {
                when(mock.validateYaml(eq(fileName), eq(repo), eq(branch), eq(commit)))
                    .thenReturn(mockValidationResult);
            });
            MockedConstruction<PipelinePublisher> mockedPublisher = mockConstruction(
                PipelinePublisher.class,
                (mock, context) -> {
                    when(mock.publishPipelineRun(any(PipelineRun.class)))
                        .thenReturn("Pipeline run #5 scheduled");
                })
        ) {
            // Mock run number for the pipeline
            when(pipelineRunsDao.getRunNumber(eq("test-pipeline"), eq(repo))).thenReturn(5);

            // Execute with async mode (non-verbose)
            String result = runCommand.run(fileName, repo, branch, commit, false);

            // Verify
            assertEquals("Pipeline run #5 scheduled", result);

            // Verify pipeline run was initialized correctly
            ArgumentCaptor<PipelineRun> pipelineRunCaptor = ArgumentCaptor.forClass(PipelineRun.class);
            verify(pipelineRunsDao, times(1)).updatePipelineRun(pipelineRunCaptor.capture());

            PipelineRun capturedRun = pipelineRunCaptor.getValue();
            assertEquals("test-pipeline", capturedRun.getPipelineName());
            assertEquals(Status.PENDING, capturedRun.getPipelineStatus());
            assertEquals(5, capturedRun.getRunNumber());
            assertEquals(fileName, capturedRun.getFileName());

            // Verify stages were initialized
            assertNotNull(capturedRun.getStages());
        }
    }

    @Test
    public void testInitialisePipelineRunReport() {
        // Create pipeline run report
        PipelineRun pipelineRun = new PipelineRun();
        pipelineRun.setRepo(Constants.LOCAL_REPO);

        // Create mock pipeline config
        PipelineConfig.Pipeline pipeline = createMockPipeline("test-pipeline");
        PipelineConfig config = mock(PipelineConfig.class);
        when(config.getPipeline()).thenReturn(pipeline);

        // Mock run number
        when(pipelineRunsDao.getRunNumber(eq("test-pipeline"), eq(Constants.LOCAL_REPO))).thenReturn(10);

        // Call method under test
        runCommand.initialisePipelineRunReport(pipelineRun, config);

        // Verify pipeline run report was initialized correctly
        assertEquals("test-pipeline", pipelineRun.getPipelineName());
        assertEquals(10, pipelineRun.getRunNumber());
        assertEquals("example-registry", pipelineRun.getRegistry());
        assertEquals("example-repo", pipelineRun.getUploadRepo());
        assertEquals("example-image", pipelineRun.getImage());
        assertEquals(Status.PENDING, pipelineRun.getPipelineStatus());

        // Verify stages were initialized
        assertNotNull(pipelineRun.getStages());
        assertEquals(3, pipelineRun.getStages().size());
        for (Stage stage : pipelineRun.getStages()) {
            assertEquals(Status.PENDING, stage.getStageStatus());
        }

        // Verify DAO update was called
        verify(pipelineRunsDao, times(1)).updatePipelineRun(eq(pipelineRun));
    }

    @Test
    public void testUpdatePipelineRunReport() throws Exception {
        // Create pipeline run report
        PipelineRun pipelineRun = new PipelineRun();
        pipelineRun.setPipelineName("test-pipeline");
        pipelineRun.setPipelineStatus(Status.RUNNING);

        // Call the private updatePipelineRunReport method using reflection
        Method updateMethod = RunCommand.class.getDeclaredMethod("updatePipelineRunReport", PipelineRun.class);
        updateMethod.setAccessible(true);
        updateMethod.invoke(runCommand, pipelineRun);

        // Verify DAO update was called
        verify(pipelineRunsDao, times(1)).updatePipelineRun(eq(pipelineRun));
        assertEquals(0L, pipelineRun.getCompletionTime());
    }

    @Test
    public void testUpdatePipelineRunReportWithEndTime() throws Exception {
        // Create pipeline run report
        PipelineRun pipelineRun = new PipelineRun();
        pipelineRun.setPipelineName("test-pipeline");
        pipelineRun.setPipelineStatus(Status.SUCCESS);

        // Create a known date
        Date endTime = new Date(1617235200000L); // 2021-04-01

        // Call the private updatePipelineRunReport method using reflection
        Method updateMethod = RunCommand.class.getDeclaredMethod("updatePipelineRunReport", PipelineRun.class, Date.class);
        updateMethod.setAccessible(true);
        updateMethod.invoke(runCommand, pipelineRun, endTime);

        // Verify DAO update was called
        verify(pipelineRunsDao, times(1)).updatePipelineRun(eq(pipelineRun));
        assertEquals(1617235200000L, pipelineRun.getCompletionTime());
    }

    private PipelineConfig.Pipeline createMockPipeline(String pipelineName) {
        PipelineConfig.Pipeline pipeline = mock(PipelineConfig.Pipeline.class);
        when(pipeline.getName()).thenReturn(pipelineName);
        when(pipeline.getRegistry()).thenReturn("example-registry");
        when(pipeline.getUploadRepo()).thenReturn("example-repo");
        when(pipeline.getImage()).thenReturn("example-image");

        // Mock stages and jobs
        List<String> stages = Arrays.asList("build", "test", "deploy");
        when(pipeline.getStages()).thenReturn(stages);

        List<Job> jobs = new ArrayList<>();
        Job buildJob = mock(Job.class);
        when(buildJob.getName()).thenReturn("build-job");
        when(buildJob.getStage()).thenReturn("build");
        jobs.add(buildJob);

        Job testJob = mock(Job.class);
        when(testJob.getName()).thenReturn("test-job");
        when(testJob.getStage()).thenReturn("test");
        jobs.add(testJob);

        Job deployJob = mock(Job.class);
        when(deployJob.getName()).thenReturn("deploy-job");
        when(deployJob.getStage()).thenReturn("deploy");
        jobs.add(deployJob);

        when(pipeline.getJobs()).thenReturn(jobs);

        return pipeline;
    }

    private String createValidYaml() {
        return "pipeline:\n" +
            "  name: test-pipeline\n" +
            "  registry: example-registry\n" +
            "  uploadRepo: example-repo\n" +
            "  image: example-image\n" +
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