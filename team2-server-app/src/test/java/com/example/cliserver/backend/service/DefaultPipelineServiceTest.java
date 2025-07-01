package com.example.cliserver.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.cliserver.backend.commands.CheckCommand;
import com.example.cliserver.backend.commands.DryRunCommand;
import com.example.cliserver.backend.commands.FileCommand;
import com.example.cliserver.backend.commands.ReportCommand;
import com.example.cliserver.backend.commands.StatusCommand;
import com.example.cliserver.backend.commands.runCommand.RunCommand;
import com.example.cliserver.backend.database.mongoDB.PipelineRunsDao;
import com.example.cliserver.backend.database.mongoDB.PipelineRunsDaoFactory;
import com.example.cliserver.backend.model.PipelineRequestParameters;
import com.example.cliserver.backend.utils.Constants;
import com.example.cliserver.service.DefaultPipelineService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class DefaultPipelineServiceTest {

    private PipelineRunsDao pipelineRunsDao;
    private DefaultPipelineService defaultPipelineService;
    private PipelineRequestParameters params;

    @BeforeEach
    void setUp() {
        pipelineRunsDao = mock(PipelineRunsDao.class);
        try (MockedStatic<PipelineRunsDaoFactory> factoryMockedStatic = mockStatic(PipelineRunsDaoFactory.class)) {
            factoryMockedStatic.when(PipelineRunsDaoFactory::getInstance).thenReturn(pipelineRunsDao);
            defaultPipelineService = new DefaultPipelineService();
        }
        params = new PipelineRequestParameters()
            .add("filename", "pipeline.yaml")
            .add("repo", "test-repo")
            .add("branch", "main")
            .add("commit", "abc123")
            .add("verboseLogging", true)
            .add("pipelineName", "testPipeline")
            .add("stage", "build")
            .add("job", "compile")
            .add("runNumber", "42");
    }

    @Test
    void testConstructor() {
        assertNotNull(defaultPipelineService.pipelineRunsDao);
    }

    @Test
    void testValidateConfiguration() {
        try (MockedConstruction<CheckCommand> checkConstruction = mockConstruction(CheckCommand.class,
            (mock, context) -> {
                when(mock.validateYaml(Constants.DIRECTORY + "pipeline.yaml", "test-repo", "main", "abc123"))
                    .thenReturn("Validation successful");
            })) {

            String result = defaultPipelineService.validateConfiguration(params);
            assertEquals("Validation successful", result);

            List<CheckCommand> constructed = checkConstruction.constructed();
            assertEquals(1, constructed.size());
            verify(constructed.get(0)).validateYaml(Constants.DIRECTORY + "pipeline.yaml",
                "test-repo", "main", "abc123");
        }
    }

    @Test
    void testRunPipelineLocally() {
        try (MockedConstruction<RunCommand> runConstruction = mockConstruction(RunCommand.class,
            (mock, context) -> {
                when(mock.run(Constants.DIRECTORY + "pipeline.yaml", "test-repo", "main", "abc123", true))
                    .thenReturn("Pipeline run successful");
            })) {
            String result = defaultPipelineService.runPipelineLocally(params);
            assertEquals("Pipeline run successful", result);

            List<RunCommand> constructed = runConstruction.constructed();
            assertEquals(1, constructed.size());
            verify(constructed.get(0)).run(Constants.DIRECTORY + "pipeline.yaml", "test-repo", "main", "abc123", true);
        }
    }

    @Test
    void testPerformDryRun() {
        try (MockedStatic<DryRunCommand> dryRunStatic = mockStatic(DryRunCommand.class)) {
            dryRunStatic.when(() -> DryRunCommand.runDry(
                    Constants.DIRECTORY + "pipeline.yaml", "test-repo", "main", "abc123"))
                .thenReturn("Dry run successful");

            String result = defaultPipelineService.performDryRun(params);
            assertEquals("Dry run successful", result);
            dryRunStatic.verify(() -> DryRunCommand.runDry(
                Constants.DIRECTORY + "pipeline.yaml", "test-repo", "main", "abc123"));
        }
    }

    @Test
    void testCheckFileExists() {
        try (MockedStatic<FileCommand> fileCommandStatic = mockStatic(FileCommand.class)) {
            fileCommandStatic.when(() -> FileCommand.checkFile(
                    "pipeline.yaml", "test-repo", "main", "abc123"))
                .thenReturn("File exists");

            String result = defaultPipelineService.checkFileExists(params);
            assertEquals("File exists", result);
            fileCommandStatic.verify(() -> FileCommand.checkFile(
                "pipeline.yaml", "test-repo", "main", "abc123"));
        }
    }

    @Test
    void testGenerateReport() {
        try (MockedConstruction<ReportCommand> reportConstruction = mockConstruction(ReportCommand.class,
            (mock, context) -> {
                JSONObject expectedJson = new JSONObject().put("status", "success");
                when(mock.generateReport("testPipeline", "build", "compile", "42", "test-repo",  Constants.JSON_FORMAT))
                    .thenReturn(expectedJson);
            })) {

            JSONObject result = defaultPipelineService.generateReport(params);
            JSONObject expectedJson = new JSONObject().put("status", "success");
            assertEquals(expectedJson.toString(), result.toString());

            List<ReportCommand> constructed = reportConstruction.constructed();
            assertEquals(1, constructed.size());
            verify(constructed.get(0)).generateReport("testPipeline", "build", "compile", "42", "test-repo", Constants.JSON_FORMAT);
        }
    }

    @Test
    void testPrintPipelineStatus() {
        try (MockedConstruction<StatusCommand> statusConstruction = mockConstruction(StatusCommand.class,
            (mock, context) -> {
                when(mock.printPipelineStatus("test-repo", "main", "abc123",
                    Constants.DIRECTORY + "pipeline.yaml", 42))
                    .thenReturn("Pipeline status: RUNNING");
            })) {

            String result = defaultPipelineService.printPipelineStatus(params);
            assertEquals("Pipeline status: RUNNING", result);

            List<StatusCommand> constructed = statusConstruction.constructed();
            assertEquals(1, constructed.size());
            verify(constructed.get(0)).printPipelineStatus("test-repo", "main", "abc123",
                Constants.DIRECTORY + "pipeline.yaml", 42);
        }
    }

    @Test
    void testPrintPipelineStatusWithNullFilename() {
        try (MockedConstruction<StatusCommand> statusConstruction = mockConstruction(StatusCommand.class,
            (mock, context) -> {
                when(mock.printPipelineStatus("test-repo", "main", "abc123", "", 42))
                    .thenReturn("Pipeline status: RUNNING");
            })) {

            PipelineRequestParameters paramsWithoutFilename = new PipelineRequestParameters()
                .add("repo", "test-repo")
                .add("branch", "main")
                .add("commit", "abc123")
                .add("runNumber", "42");

            String result = defaultPipelineService.printPipelineStatus(paramsWithoutFilename);
            assertEquals("Pipeline status: RUNNING", result);

            List<StatusCommand> constructed = statusConstruction.constructed();
            assertEquals(1, constructed.size());
            verify(constructed.get(0)).printPipelineStatus("test-repo", "main", "abc123", "", 42);
        }
    }

    @Test
    void testPrintPipelineStatusWithNullRunNumber() {
        try (MockedConstruction<StatusCommand> statusConstruction = mockConstruction(StatusCommand.class,
            (mock, context) -> {
                when(mock.printPipelineStatus("test-repo", "main", "abc123",
                    Constants.DIRECTORY + "pipeline.yaml", null))
                    .thenReturn("Pipeline status: UNKNOWN");
            })) {

            PipelineRequestParameters paramsWithoutRunNumber = new PipelineRequestParameters()
                .add("filename", "pipeline.yaml")
                .add("repo", "test-repo")
                .add("branch", "main")
                .add("commit", "abc123");

            String result = defaultPipelineService.printPipelineStatus(paramsWithoutRunNumber);
            assertEquals("Pipeline status: UNKNOWN", result);

            List<StatusCommand> constructed = statusConstruction.constructed();
            assertEquals(1, constructed.size());
            verify(constructed.get(0)).printPipelineStatus("test-repo", "main", "abc123",
                Constants.DIRECTORY + "pipeline.yaml", null);
        }
    }

    @Test
    void testPrintPipelineStatusWithInvalidRunNumber() {
        PipelineRequestParameters paramsWithInvalidRunNumber = new PipelineRequestParameters()
            .add("filename", "pipeline.yaml")
            .add("repo", "test-repo")
            .add("branch", "main")
            .add("commit", "abc123")
            .add("runNumber", "not-a-number");

        assertThrows(NumberFormatException.class, () ->
            defaultPipelineService.printPipelineStatus(paramsWithInvalidRunNumber));
    }
}
