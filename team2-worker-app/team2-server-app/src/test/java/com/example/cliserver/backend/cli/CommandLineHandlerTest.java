package com.example.cliserver.cli;

import com.example.cliserver.backend.model.PipelineRequestParameters;
import com.example.cliserver.controller.SseController;
import com.example.cliserver.service.PipelineService;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.lang.reflect.Field;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.bson.Document;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class CommandLineHandlerTest {

    @TempDir
    Path tempDir;

    private MongoClient mockMongoClient;
    private MongoDatabase mockDatabase;
    private MongoCollection<Document> mockCollection;

    @BeforeEach
    void setUp() {
        // Set up MongoDB mocks to be used across tests
        mockMongoClient = mock(MongoClient.class);
        mockDatabase = mock(MongoDatabase.class);
        mockCollection = mock(MongoCollection.class);

        when(mockMongoClient.getDatabase(anyString())).thenReturn(mockDatabase);
        when(mockDatabase.getCollection(anyString())).thenReturn(mockCollection);
    }

    @Test
    void testHelpOption() throws IOException {
        String[] args = {"-h"};
        SseEmitter emitter = new SseEmitter();
        CommandLineHandler handler = new CommandLineHandler(args, emitter);
        try (MockedStatic<SseController> sseMock = Mockito.mockStatic(SseController.class)) {
            handler.run();
            sseMock.verify(() -> SseController.sendEventAndComplete(any(), any()), Mockito.never());
        }
    }

    @Test
    void testCheckOption() throws Exception {
        String[] args = {"-c", "-f", "pipeline.yaml"};
        SseEmitter emitter = new SseEmitter();
        CommandLineHandler handler = new CommandLineHandler(args, emitter);
        try (MockedStatic<SseController> sseMock = Mockito.mockStatic(SseController.class)) {
            handler.run();
            sseMock.verify(() -> SseController.sendEventAndComplete(Mockito.eq(emitter), any()), Mockito.times(1));
        }
    }

    @Test
    void testRunOption() throws Exception {
        String[] args = {"run", "-f", "pipeline.yaml"};
        SseEmitter emitter = new SseEmitter();
        CommandLineHandler handler = new CommandLineHandler(args, emitter);
        try (MockedStatic<SseController> sseMock = Mockito.mockStatic(SseController.class)) {
            handler.run();
            sseMock.verify(() -> SseController.sendEventAndComplete(Mockito.eq(emitter), any()), Mockito.times(1));
        }
    }

    @Test
    void testStatusOption() throws Exception {
        String[] args = {"status"};
        SseEmitter emitter = new SseEmitter();
        CommandLineHandler handler = new CommandLineHandler(args, emitter);
        injectDummyPipelineService(handler);
        try (MockedStatic<SseController> sseMock = Mockito.mockStatic(SseController.class)) {
            handler.run();
            sseMock.verify(() -> SseController.sendEventAndComplete(emitter, "Status OK"), Mockito.times(1));
        }
    }

    @Test
    void testDryRunOption() throws Exception {
        String[] args = {"-d", "-f", "pipeline.yaml"};
        SseEmitter emitter = new SseEmitter();
        CommandLineHandler handler = new CommandLineHandler(args, emitter);
        try (MockedStatic<SseController> sseMock = Mockito.mockStatic(SseController.class)) {
            handler.run();
            sseMock.verify(() -> SseController.sendEventAndComplete(Mockito.eq(emitter), any()), Mockito.times(1));
        }
    }

    @Test
    void testFilenameOnlyOption() throws Exception {
        String[] args = {"-f", "pipeline.yaml"};
        SseEmitter emitter = new SseEmitter();
        CommandLineHandler handler = new CommandLineHandler(args, emitter);
        try (MockedStatic<SseController> sseMock = Mockito.mockStatic(SseController.class)) {
            handler.run();
            sseMock.verify(() -> SseController.sendEventAndComplete(Mockito.eq(emitter), any()), Mockito.times(1));
        }
    }

    @Test
    void testReportOption() throws Exception {
        // Use MockedStatic to mock MongoClients
        try (MockedStatic<MongoClients> mongoClientsMock = Mockito.mockStatic(MongoClients.class)) {
            // Set up MongoDB mocking
            mongoClientsMock.when(() -> MongoClients.create(any(String.class))).thenReturn(mockMongoClient);

            // Prepare test
            String[] args = {"report", "-f", "my-pipeline", "--format", "json"};
            SseEmitter emitter = new SseEmitter();
            CommandLineHandler handler = new CommandLineHandler(args, emitter);

            // Inject our dummy service to avoid MongoDB issues
            injectDummyPipelineService(handler);

            // Execute test and verify
            try (MockedStatic<SseController> sseMock = Mockito.mockStatic(SseController.class)) {
                handler.run();
                sseMock.verify(() -> SseController.sendEventAndComplete(emitter, "File Exists"), times(1));
            }
        }
    }

    @Test
    void testInvalidArguments() throws Exception {
        String[] args = {"--illegalOption"};
        SseEmitter emitter = new SseEmitter();
        CommandLineHandler handler = new CommandLineHandler(args, emitter);
        try (MockedStatic<SseController> sseMock = Mockito.mockStatic(SseController.class)) {
            handler.run();
            sseMock.verify(() -> SseController.sendEvent(contains("Error parsing command line arguments:")), times(1));
        }
    }

    @Test
    void testParseCommandLineToParams() throws Exception {
        String[] args = {
            "-f", "file.yaml",
            "--repo", "http://repo-url",
            "-vv",
            "--pipeline", "my-pipeline",
            "--stage", "build",
            "--job", "job1",
            "--runNumber", "10",
            "--format", "xml",
            "--localDir", "local/path",
            "--branch", "dev",
            "--commit", "12345"
        };
        SseEmitter emitter = new SseEmitter();
        CommandLineHandler handler = new CommandLineHandler(args, emitter);

        Method method = CommandLineHandler.class.getDeclaredMethod("parseCommandLineToParams", CommandLine.class);
        method.setAccessible(true);

        Options options = new Options();
        options.addOption(Option.builder("f").longOpt("filename").hasArg().build());
        options.addOption(Option.builder("repo").longOpt("repo").hasArg().build());
        options.addOption(Option.builder("vv").longOpt("vv").build());
        options.addOption(Option.builder("pipeline").longOpt("pipeline").hasArg().build());
        options.addOption(Option.builder("stage").longOpt("stage").hasArg().build());
        options.addOption(Option.builder("job").longOpt("job").hasArg().build());
        options.addOption(Option.builder("runNumber").longOpt("runNumber").hasArg().build());
        options.addOption(Option.builder("format").longOpt("format").hasArg().build());
        options.addOption(Option.builder("localDir").longOpt("localDir").hasArg().build());
        options.addOption(Option.builder("branch").longOpt("branch").hasArg().build());
        options.addOption(Option.builder("commit").longOpt("commit").hasArg().build());
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        PipelineRequestParameters params = (PipelineRequestParameters) method.invoke(handler, cmd);
        assertEquals("file.yaml", params.get("filename"));
        assertEquals("http://repo-url", params.get("repo"));
        assertEquals(true, params.get("verboseLogging"));
        assertEquals("my-pipeline", params.get("pipelineName"));
        assertEquals("build", params.get("stage"));
        assertEquals("job1", params.get("job"));
        assertEquals("10", params.get("runNumber"));
        assertEquals("xml", params.get("format"));
        assertEquals("local/path", params.get("localDir"));
        assertEquals("dev", params.get("branch"));
        assertEquals("12345", params.get("commit"));
    }

    static class DummyPipelineService implements PipelineService {
        @Override
        public String validateConfiguration(PipelineRequestParameters params) {
            return "Validated";
        }

        @Override
        public String runPipelineLocally(PipelineRequestParameters params) {
            return "Run Locally";
        }

        @Override
        public String printPipelineStatus(PipelineRequestParameters params) {
            return "Status OK";
        }

        @Override
        public String performDryRun(PipelineRequestParameters params) {
            return "Dry Run Executed";
        }

        @Override
        public String checkFileExists(PipelineRequestParameters params) {
            return "File Exists";
        }

        @Override
        public JSONObject generateReport(PipelineRequestParameters params) {
            return new JSONObject("Report Generated");
        }
    }

    private void injectDummyPipelineService(CommandLineHandler handler) throws Exception {
        Field serviceField = CommandLineHandler.class.getDeclaredField("pipelineService");
        serviceField.setAccessible(true);
        serviceField.set(handler, new DummyPipelineService());
    }

    @Test
    void testCheckOptionWithoutFilename() throws Exception {
        String[] args = {"-c"};  // Missing -f
        SseEmitter emitter = new SseEmitter();
        CommandLineHandler handler = new CommandLineHandler(args, emitter);
        try (MockedStatic<SseController> sseMock = Mockito.mockStatic(SseController.class)) {
            handler.run();
            sseMock.verify(() -> SseController.sendEventAndComplete(any(), any()), Mockito.never());
        }
    }

    @Test
    void testRunOptionWithoutFilename() throws Exception {
        String[] args = {"run"};  // Missing -f
        SseEmitter emitter = new SseEmitter();
        CommandLineHandler handler = new CommandLineHandler(args, emitter);
        try (MockedStatic<SseController> sseMock = Mockito.mockStatic(SseController.class)) {
            handler.run();
            sseMock.verify(() -> SseController.sendEventAndComplete(any(), any()), Mockito.never());
        }
    }
    @Test
    void testStatusWithAdditionalArgs() throws Exception {
        String[] args = {"status", "-vv"};  // Extra option that shouldn’t block execution
        SseEmitter emitter = new SseEmitter();
        CommandLineHandler handler = new CommandLineHandler(args, emitter);
        injectDummyPipelineService(handler);
        try (MockedStatic<SseController> sseMock = Mockito.mockStatic(SseController.class)) {
            handler.run();
            sseMock.verify(() -> SseController.sendEventAndComplete(emitter, "Status OK"), times(1));
        }
    }
    @Test
    void testNoValidCommandProvided() throws Exception {
        String[] args = {"--branch", "main"};
        SseEmitter emitter = new SseEmitter();
        CommandLineHandler handler = new CommandLineHandler(args, emitter);
        try (MockedStatic<SseController> sseMock = Mockito.mockStatic(SseController.class)) {
            handler.run();
            sseMock.verify(() -> SseController.sendEventAndComplete(any(), any()), Mockito.never());
        }
    }
}