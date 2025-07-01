package com.example.cliapplication.service;

import com.example.cliapplication.reportGeneration.ReportGenerator;
import com.example.cliapplication.reportGeneration.ReportGeneratorFactory;
import com.example.cliapplication.reportGeneration.ReportProcessor;
import com.example.cliapplication.view.ConsoleOutput;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CLITest {

    @Mock
    private CommandLineHandler commandLineHandler;

    @Mock
    private ConsoleOutput consoleOutput;

    @Mock
    private HttpClient httpClient;

    private CLI cli;
    private String[] testArgs;
    private Options mockOptions;
    private CommandLine mockCmd;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;


    @BeforeEach
    void setUp() throws ParseException {
        MockitoAnnotations.openMocks(this);
        testArgs = new String[]{"-c", "myCommand", "-f", "myFile.yaml"};
        mockOptions = new Options();
        mockOptions.addOption("h", "help", false, "Prints help message");
        mockOptions.addOption("c", "command", true, "The command to execute");
        mockOptions.addOption("f", "file", true, "Path to the pipeline file");
        mockOptions.addOption("local", false, "Indicates local file");
        mockOptions.addOption("repo", true, "Repository URL");
        mockCmd = mock(CommandLine.class);

        when(commandLineHandler.getOptions()).thenReturn(mockOptions);
        when(commandLineHandler.parse(any())).thenReturn(mockCmd);

        cli = new CLI(testArgs, commandLineHandler, consoleOutput, httpClient);
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void run_shouldValidateCommand() {
        when(mockCmd.hasOption("h")).thenReturn(false);
        when(commandLineHandler.isValidCommand(eq(mockCmd), eq(testArgs), eq(mockOptions), eq(consoleOutput))).thenReturn(true);
        when(httpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(mock(HttpResponse.class)));
        cli.run();
        verify(commandLineHandler).isValidCommand(eq(mockCmd), eq(testArgs), eq(mockOptions), eq(consoleOutput));
    }

    @Test
    void run_shouldNotProceedIfCommandIsInvalid() {
        when(mockCmd.hasOption("h")).thenReturn(false);
        when(commandLineHandler.isValidCommand(eq(mockCmd), eq(testArgs), eq(mockOptions), eq(consoleOutput))).thenReturn(false);
        cli.run();
        verify(commandLineHandler).isValidCommand(eq(mockCmd), eq(testArgs), eq(mockOptions), eq(consoleOutput));
        verifyNoInteractions(httpClient);
    }

    @Test
    void updateArgsWithLocalDir_shouldNotAddLocalDirArgument_whenRepoOptionIsPresentAndNoLocalOrFile() throws ParseException {
        String[] repoArgs = new String[]{"-c", "build", "--repo", "https://github.com/owner/repo.git"};
        when(commandLineHandler.parse(repoArgs)).thenReturn(mockCmd);
        when(mockCmd.hasOption("local")).thenReturn(false);
        when(mockCmd.hasOption("--repo")).thenReturn(true);
        when(mockCmd.hasOption("-f")).thenReturn(false);

        CLI repoCli = new CLI(repoArgs, commandLineHandler, consoleOutput, httpClient);
        repoCli.run();

        ArgumentCaptor<String[]> argsCaptor = ArgumentCaptor.forClass(String[].class);
        verify(commandLineHandler).parse(argsCaptor.capture());
        assertArrayEquals(repoArgs, argsCaptor.getValue());
    }

    @Test
    void createSseRequest_shouldCreateGetRequestWithCorrectHeaders() {
        URI uri = URI.create("http://localhost:8080/execute?command=test");
        HttpRequest request = cli.createSseRequest(uri);
        assertEquals("GET", request.method());
        assertEquals(uri, request.uri());
        assertEquals("text/event-stream", request.headers().firstValue("Accept").orElse(null));
    }

    @Test
    void sendSseRequest_shouldCallHttpClientWithCorrectArguments() throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:8080/execute?command=test");
        HttpRequest request = cli.createSseRequest(uri);
        HttpResponse<Stream<String>> mockResponse = mock(HttpResponse.class);
        CompletableFuture<HttpResponse<Stream<String>>> futureResponse = CompletableFuture.completedFuture(mockResponse);
        when(httpClient.sendAsync(eq(request), eq(HttpResponse.BodyHandlers.ofLines()))).thenReturn(futureResponse);

        Consumer<HttpResponse<Stream<String>>> responseHandler = mock(Consumer.class);
        cli.sendSseRequest(request, responseHandler);

        verify(httpClient).sendAsync(eq(request), eq(HttpResponse.BodyHandlers.ofLines()));
        verify(responseHandler).accept(mockResponse);
    }


    @Test
    void run_shouldHandleParseException() throws ParseException {
        ParseException parseException = new ParseException("Invalid option");
        when(commandLineHandler.parse(any())).thenThrow(parseException);

        cli.run();

        ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);
        verify(consoleOutput).displayError(errorCaptor.capture());
        assertEquals("Argument parsing error: Invalid option", errorCaptor.getValue());
        verify(consoleOutput).printHelp(mockOptions);
    }

    @Test
    void run_shouldHandleHelpOption() throws ParseException {
        when(mockCmd.hasOption("h")).thenReturn(true);
        cli.run();
        verify(consoleOutput).printHelp(mockOptions);
        verifyNoInteractions(httpClient);
    }

    @Test
    void updateArgsWithLocalDir_shouldNotAddLocalDirArgument_whenNoLocalOrFileOptions() throws ParseException {
        String[] commandOnlyArgs = new String[]{"-c", "deploy"};
        when(commandLineHandler.parse(commandOnlyArgs)).thenReturn(mockCmd);
        when(mockCmd.hasOption("local")).thenReturn(false);
        when(mockCmd.hasOption("--repo")).thenReturn(false);
        when(mockCmd.hasOption("-f")).thenReturn(false);
        when(commandLineHandler.isValidCommand(any(), eq(commandOnlyArgs), eq(mockOptions), eq(consoleOutput))).thenReturn(true);
        when(httpClient.sendAsync(any(), any())).thenReturn(CompletableFuture.completedFuture(mock(HttpResponse.class)));

        CLI commandOnlyCli = new CLI(commandOnlyArgs, commandLineHandler, consoleOutput, httpClient);
        commandOnlyCli.run();

        ArgumentCaptor<HttpRequest> httpRequestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).sendAsync(httpRequestCaptor.capture(), any());

        HttpRequest sentRequest = httpRequestCaptor.getValue();
        assertEquals(URI.create("http://localhost:8080/execute?command=-c+deploy"), sentRequest.uri());
    }

    @Test
    void createSseUri_shouldEncodeCommand() {
        String rawCommand = "run pipeline with spaces";
        URI uri = cli.createSseUri(rawCommand);
        assertEquals("http://localhost:8080/execute?command=run+pipeline+with+spaces", uri.toString());
    }

    @Test
    void run_shouldNotCallSendSseRequestIfValidationFails () throws ParseException, Exception {
        when(mockCmd.hasOption("h")).thenReturn(false);
        when(commandLineHandler.isValidCommand(any(), eq(testArgs), eq(mockOptions), eq(consoleOutput))).thenReturn(false);

        cli.run();

        verifyNoInteractions(httpClient);
    }

    @Test
    void run_shouldNotCallSendSseRequestIfHelpOptionPresent() throws ParseException, Exception {
        when(mockCmd.hasOption("h")).thenReturn(true);

        cli.run();

        verifyNoInteractions(httpClient);
    }

    @Test
    void run_shouldProceedToSseRequestAfterSuccessfulValidation() throws ParseException, Exception {
        when(mockCmd.hasOption("h")).thenReturn(false);
        when(commandLineHandler.isValidCommand(any(), eq(testArgs), eq(mockOptions), eq(consoleOutput))).thenReturn(true);
        when(httpClient.sendAsync(any(HttpRequest.class), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(HttpResponse.class)));
        when(mockCmd.getOptionValue("f")).thenReturn("test.yaml"); // To avoid potential NPE in updateArgsWithLocalDir

        cli.run();

        verify(httpClient).sendAsync(any(HttpRequest.class), any());
    }

    @Test
    void processSseResponse_shouldPrintErrorForNon200Status() {
        HttpResponse<Stream<String>> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn(Stream.empty());

        cli.processSseResponse(mockResponse, mockCmd);

    }

    @Test
    public void testProcessSseResponseDoesNotThrowException() {
        // Arrange
        cli = new CLI(new String[]{"report"}, commandLineHandler, consoleOutput, httpClient);
        CommandLine mockCmd = mock(CommandLine.class);
        when(mockCmd.hasOption("job")).thenReturn(false);
        when(mockCmd.hasOption("stage")).thenReturn(false);
        when(mockCmd.hasOption("format")).thenReturn(false);

        HttpResponse<Stream<String>> mockResponse = mock(HttpResponse.class);
        Stream<String> stream = Stream.of("data:{\"default\":[{\"foo\":\"bar\"}]}");
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(stream);

        // Act (no exception expected)
        cli.processSseResponse(mockResponse, mockCmd);

        // Assert via side effect (e.g., JSON was printed)
        String printed = outContent.toString();
        assertTrue(printed.contains("\"foo\":\"bar\"") || printed.contains("\"foo\": \"bar\""));
    }

    @Test
    public void testProcessSseResponse_TriggersProcessTableOutput() {
        // Arrange
        String[] args = {
                "report", "--local", "--pipeline", "some-pipeline-name",
                "--stage", "build", "--job", "job-name", "--format", "table"
        };
        CLI cli = new CLI(args);

        String validJson = "data: {\"job\": [{\"jobName\": \"generate-files\", \"status\": \"SUCCESS\", \"duration\": 1234}]}";

        CommandLine mockCmd = mock(CommandLine.class);
        when(mockCmd.hasOption("job")).thenReturn(true);
        when(mockCmd.hasOption("stage")).thenReturn(true);
        when(mockCmd.hasOption("format")).thenReturn(true);
        when(mockCmd.getOptionValue("format")).thenReturn("table");

        @SuppressWarnings("unchecked")
        HttpResponse<Stream<String>> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(Stream.of(validJson));

        // Redirect stdout
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try (MockedConstruction<ReportProcessor> mocked = mockConstruction(ReportProcessor.class)) {
            // Act
            cli.processSseResponse(mockResponse, mockCmd);

            // Assert
            List<ReportProcessor> instances = mocked.constructed();
            assertEquals(1, instances.size());
            verify(instances.get(0)).processReport(any(JSONObject.class));

            // Optional: assert output printed something meaningful
            String printed = outContent.toString();
            assertTrue(printed.contains("Records:")); // because "--report" was in args
        } finally {
            System.setOut(originalOut); // Restore System.out
        }
    }

    @Test
    void createSseUri_shouldHandleSpecialCharacters() {
        String command = "build --pipeline pipeline-name";
        URI uri = cli.createSseUri(command);

        assertTrue(uri.toString().contains("pipeline-name"));
    }

    @Test
    void sendSseRequest_shouldHandleHttpClientFailure() {
        URI uri = URI.create("http://localhost:8080/execute?command=test");
        HttpRequest request = cli.createSseRequest(uri);

        CompletableFuture<HttpResponse<Stream<String>>> failedFuture =
                CompletableFuture.failedFuture(new IOException("Simulated failure"));
        when(httpClient.sendAsync(eq(request), eq(HttpResponse.BodyHandlers.ofLines())))
                .thenReturn(failedFuture);

        cli.sendSseRequest(request, response -> fail("Response handler should not be called"));
    }

    @Test
    void processSseResponse_shouldSkipTableOutputIfReportArgMissing() {
        String[] args = {
                "--report","--local", "--pipeline", "some-pipeline-name",
                "--stage", "build", "--job", "job-name", "--format", "table"
        };
        CLI cli = new CLI(args);

        String sseData = "data: {\"jobName\": \"generate-files\", \"status\": \"SUCCESS\"}";

        CommandLine mockCmd = mock(CommandLine.class);
        when(mockCmd.hasOption(anyString())).thenReturn(false);

        @SuppressWarnings("unchecked")
        HttpResponse<Stream<String>> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(Stream.of(sseData));

        assertDoesNotThrow(() -> cli.processSseResponse(mockResponse, mockCmd));
    }

    @Test
    public void testProcessTableOutputWithJsonFormat() throws JSONException {
        cli = new CLI(new String[]{"report", "-f", "artifacts.yaml"}, commandLineHandler, consoleOutput, httpClient);
        CommandLine cmd = mock(CommandLine.class);

        when(cmd.hasOption("job")).thenReturn(false);
        when(cmd.hasOption("stage")).thenReturn(false);
        when(cmd.hasOption("format")).thenReturn(true);
        when(cmd.getOptionValue("format")).thenReturn("json");

        JSONObject root = new JSONObject();
        JSONArray arr = new JSONArray();
        JSONObject row = new JSONObject();
        row.put("foo", "bar");
        arr.put(row);
        root.put("default", arr);  // Avoid "job" or "stage" so it picks "default"

        cli.processTableOutput(root.toString(), cmd);

        String output = outContent.toString();
        assertTrue(output.contains("\"foo\": \"bar\""));
    }

}