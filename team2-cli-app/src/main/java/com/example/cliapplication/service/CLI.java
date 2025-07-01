package com.example.cliapplication.service;

import com.example.cliapplication.reportGeneration.ReportProcessor;
import com.example.cliapplication.view.ConsoleOutput;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * The {@code CLI} class orchestrates the command-line interface execution.
 * <p>
 * It handles command parsing, argument validation, API request construction,
 * Server-Sent Events (SSE) response handling, and delegate report generation.
 */
public class CLI {
    private static final String BACKEND_URL = "http://localhost:8080/execute";
    private final CommandLineHandler commandLineHandler;
    private final ConsoleOutput consoleOutput;
    private final HttpClient httpClient;
    private String[] args;

    /**
     * Constructs a CLI instance with all dependencies provided.
     *
     * @param args               The command-line arguments.
     * @param commandLineHandler The handler responsible for parsing CLI arguments.
     * @param consoleOutput      The console output handler.
     * @param httpClient         The HTTP client used to send requests.
     */
    public CLI(String[] args, CommandLineHandler commandLineHandler,
               ConsoleOutput consoleOutput, HttpClient httpClient) {
        this.args = args.clone();
        this.commandLineHandler = commandLineHandler;
        this.consoleOutput = consoleOutput;
        this.httpClient = httpClient;
    }

    /**
     * Default constructor for general use, creates its own dependencies.
     *
     * @param args The command-line arguments provided by the user.
     */
    public CLI(String[] args) {
        this(args, new CommandLineHandler(), new ConsoleOutput(), HttpClient.newHttpClient());
    }

    /**
     * Entry point to execute the CLI.
     * <p>
     * Parses arguments, validates commands, constructs SSE request,
     * and processes the streaming response from the backend.
     */
    public void run() {
        try {
            CommandLine cmd = commandLineHandler.parse(args);

            if (handleHelpOption(cmd)) {
                return;
            }

            if (!validateCommand(cmd)) {
                return;
            }

            String rawCommand = String.join(" ", updateArgsWithLocalDir(cmd));
            URI sseUri = createSseUri(rawCommand);
            HttpRequest sseRequest = createSseRequest(sseUri);

            sendSseRequest(sseRequest, response -> processSseResponse(response, cmd));

        } catch (ParseException e) {
            consoleOutput.displayError("Argument parsing error: " + e.getMessage());
            consoleOutput.printHelp(commandLineHandler.getOptions());
        } catch (Exception e) {
            consoleOutput.displayError("Error during API request: " + e.getMessage() +
                    (e.getCause() != null ? " (Cause: " + e.getCause().getMessage() + ")" : ""));
            e.printStackTrace();
        }
    }

    /**
     * Prints help documentation if the help option is present.
     *
     * @param cmd Parsed command line.
     * @return {@code true} if help was printed and execution should stop, otherwise {@code false}.
     */
    private boolean handleHelpOption(CommandLine cmd) {
        if (cmd.hasOption("h")) {
            consoleOutput.printHelp(commandLineHandler.getOptions());
            return true;
        }
        return false;
    }

    /**
     * Validates the parsed command using the handler.
     *
     * @param cmd Parsed command line.
     * @return {@code true} if the command is valid;
     *         {@code false} otherwise.
     */
    private boolean validateCommand(CommandLine cmd) {
        if (!commandLineHandler.isValidCommand(cmd, args,
                commandLineHandler.getOptions(), consoleOutput)) {
            return false;
        }
        return true;
    }

    /**
     * Updates the argument list to include a local directory path if using a local file.
     *
     * @param cmd Parsed command line.
     * @return Updated argument list including --localDir if applicable.
     */
    private String[] updateArgsWithLocalDir(CommandLine cmd) {
        String localFilePath = getLocalFilePath(cmd);
        if (localFilePath != null) {
            File localFile = new File(localFilePath);
            String localDir = localFile.getParent();
            if (localDir != null) {
                String[] newArgs = new String[args.length + 2];
                System.arraycopy(args, 0, newArgs, 0, args.length);
                newArgs[args.length] = "--localDir";
                newArgs[args.length + 1] = localDir + "/";
                return newArgs;
            }
        }
        return args;
    }

    private String getLocalFilePath(CommandLine cmd) {
        if (cmd.hasOption("local") || !cmd.hasOption("--repo") || cmd.hasOption("-f")) {
            String filename = cmd.getOptionValue("f");
            if (filename != null) {
                String currentWorkingDirectory = System.getProperty("user.dir");
                File localFile = new File(currentWorkingDirectory +
                        File.separator + ".pipelines" + File.separator + filename);
                return localFile.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * Resolves the full local file path for a provided pipeline file.
     *
     * @param rawCommand Parsed command line.
     * @return Absolute file path if applicable, otherwise {@code null}.
     */
    URI createSseUri(String rawCommand) {
        return URI.create(BACKEND_URL +
                "?command=" + java.net.URLEncoder.encode(rawCommand, StandardCharsets.UTF_8));
    }

    /**
     * Creates the HTTP GET request for SSE communication.
     *
     * @param sseUri URI to connect to.
     * @return Configured HttpRequest object.
     */
    HttpRequest createSseRequest(URI sseUri) {
        return HttpRequest.newBuilder()
                .uri(sseUri)
                .GET()
                .header("Accept", "text/event-stream")
                .build();
    }

    /**
     * Sends the SSE request and delegates handling to the provided response handler.
     *
     * @param request         The SSE request.
     * @param responseHandler Consumer for the SSE response stream.
     */
    void sendSseRequest(HttpRequest request,
                        Consumer<HttpResponse<Stream<String>>> responseHandler) {
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(responseHandler)
                .exceptionally(e -> {
                    System.err.println("Error during SSE connection: " + e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("Cause: " + e.getCause().getMessage());
                    }
                    return null;
                })
                .join();
    }

    /**
     * Processes the streaming SSE response and prints or delegates output.
     *
     * @param response The SSE response object.
     * @param cmd      Parsed command line used to determine output format.
     */
    void processSseResponse(HttpResponse<Stream<String>> response, CommandLine cmd) {
        if (response.statusCode() == 200) {
            response.body().forEach(line -> {
                if (line.startsWith("data:")) {
                    String data = line.substring("data:".length());
                    if (!data.isEmpty()) {
                        if(hasRawArg("report")) {
                            System.out.println("Records:");
                            processTableOutput(data, cmd);
                        } else {
                            System.out.println(data);
                        }
                    }
                } else if (!line.trim().isEmpty()) {
                    System.out.println("Received: " + line);
                }
            });
        } else {
            System.err.println("Failed to connect to SSE endpoint. Status code: " +
                    response.statusCode());
        }
    }

    /**
     * Delegates table or JSON output formatting based on command flags.
     *
     * @param jsonData Raw JSON data as string.
     * @param cmd      Parsed command line for output options.
     */
    void processTableOutput(String jsonData, CommandLine cmd) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            boolean hasData = false;

            for (String key : jsonObject.keySet()) {
                if (jsonObject.get(key) instanceof JSONArray) {
                    JSONArray array = jsonObject.getJSONArray(key);
                    if (array.length() > 0) {
                        hasData = true;
                        break;
                    }
                }
            }

            if (!hasData) {
                System.out.println("No Data found.");
                return;
            }

            String reportType = cmd.hasOption("job") ? "job" :
                    cmd.hasOption("stage") ? "stage" : "default";

            String outputFormat = "JSON";
            if (cmd.hasOption("format")) {
                String formatValue = cmd.getOptionValue("format");
                outputFormat = "table".equalsIgnoreCase(formatValue) ? "table" : "JSON";
            }

            if ("JSON".equalsIgnoreCase(outputFormat)) {
                System.out.println(jsonObject.toString(2));
                return;
            }

            ReportProcessor reportProcessor = new ReportProcessor(outputFormat, reportType);
            reportProcessor.processReport(jsonObject);

        } catch (JSONException e) {
            throw new RuntimeException("Error parsing JSON data for table output: " +
                    e.getMessage());
        }
    }

    /**
     * Checks if the given raw CLI argument exists (case-insensitive).
     *
     * @param argName Name of the raw CLI argument to search for.
     * @return {@code true} if the raw argument exists; {@code false} otherwise.
     */
    private boolean hasRawArg(String argName) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase(argName)) {
                return true;
            }
        }
        return false;
    }
}