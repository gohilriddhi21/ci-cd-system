package com.example.cliserver.cli;

import com.example.cliserver.backend.model.PipelineRequestParameters;
import com.example.cliserver.controller.SseController;
import com.example.cliserver.service.DefaultPipelineService;
import com.example.cliserver.service.PipelineService;
import org.apache.commons.cli.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;

/**
 * This class processes options like --filename, --run and --check
 * for configuration validation.
 * <p>
 * It Handles command-line arguments and
 * invokes the appropriate functionality.
 */
public class CommandLineHandler {
    private final SseEmitter emitter;

    /**
     * HelpFormatter for displaying command-line help.
     */
    private final HelpFormatter formatter;
    /**
     * String array for Command Line arguments.
     */
    private String[] args;
    /**
     * Options for storing command names and configuration.
     */
    private Options options;

    /**
     * Pipeline Service to run backend for commands
     */
    public final PipelineService pipelineService;

    /**
     * Constructs a CommandLineHandler with the given arguments.
     *
     * @param args Command-line arguments provided by the user.
     * @param emitter The SSE emitter for sending events to clients during pipeline operations
     */
    public CommandLineHandler(String[] args, SseEmitter emitter) {
        this.args = Arrays.copyOf(args, args.length);
        this.options = new Options();
        this.emitter = emitter;

        // Create the service with the DAO
        this.pipelineService = new DefaultPipelineService();
        this.formatter = new HelpFormatter();

        options.addOption(Option.builder("branch")
                .longOpt("branch")
                .hasArg()
                .desc("Specifies the Git branch to use")
                .build());

        options.addOption(Option.builder("c")
                .longOpt("check")
                .desc("Perform a general file check operation")
                .build());

        options.addOption(Option.builder("commit")
                .longOpt("commit")
                .hasArg()
                .desc("Specifies the Git commit hash to use")
                .build());

        options.addOption(Option.builder("d")
                .longOpt("dry-run")
                .desc("Execute the pipeline locally without running actual jobs")
                .build());

        options.addOption(Option.builder("f")
                .longOpt("filename")
                .hasArg()
                .desc("Check if a file exists in the .pipeline directory")
                .build());

        options.addOption(Option.builder("ft")
                .longOpt("format")
                .hasArg()
                .desc("Displays the reports in a specified format")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Display help information")
                .build());

        options.addOption(Option.builder("j")
                .longOpt("job")
                .hasArg()
                .desc("Specifies the job name in the pipeline stage")
                .build());

        options.addOption(Option.builder("local")
                .longOpt("local")
                .desc("Retrieve pipeline reports for the local repo")
                .build());

        options.addOption(Option.builder("pipeline")
                .longOpt("pipeline")
                .hasArg()
                .desc("Retrieve pipeline reports with this pipeline name")
                .build());

        options.addOption(Option.builder("r")
                .longOpt("repo")
                .hasArg()
                .desc("Specifies the location of the repository to use")
                .build());

        options.addOption(Option.builder("rn")
                .longOpt("runNumber")
                .hasArg()
                .desc("Specifies the run number for the pipeline to report on")
                .build());

        options.addOption(Option.builder("s")
                .longOpt("stage")
                .hasArg()
                .desc("Specifies the stage name in the pipeline")
                .build());

        options.addOption(Option.builder("vv")
                .longOpt("vv")
                .desc("Displays detailed logging when running a pipeline")
                .build());

        options.addOption(Option.builder("ld")
                .longOpt("localDir")
                .hasArg()
                .desc("Local .pipelines directory path of the client")
                .build());
    }

    /**
     * Displays the help message showing all available command-line options.
     * <p>
     * This method prints usage instructions and available command-line options
     * to guide the user on how to use the application effectively.
     * </p>
     */
    public void printHelpFormatter() {
        String header = "\n";
        String footer = String.format(" %-29s%s%n %-29s%s%n %-29s%s",
                "report", "Generate reports for pipeline runs",
                "run", "Executes the pipeline locally",
                "status", "Display status information for pipeline runs"
        );
        formatter.printHelp(
                "java -jar pipeline-tool.jar [options]",
                header,
                options,
                footer
        );
    }

    /**
     * Parses and processes command-line arguments, executing the corresponding action.
     * - If `-h` | `--help` is provided, it displays help information.
     * - If `-f` | `--filename` is provided, it checks for the existence of the file.
     * - If `-c` | `--check` is provided, it validates the configuration file.
     * - If 'run' is provided, it runs the pipeline locally
     * * --f must be passed to specify the filename of the pipeline to run
     * * --repo (optional) can be passed to specify the repository where the pipeline is located
     * * --vv (optional) can be passed to enable verbose logging output in the terminal while
     * executing the pipeline
     * - If 'status' is provided, it displays the current status of pipeline runs.
     * - If `-d` | `--dry-run` is provided with `-f`, it executes the pipeline in dry run mode.
     * - If `report` and `--pipeline` and (`--stage` or `--job` or `--repo` or '--runNumber' or
     * '--format') is provided, it generates reports based on the parameters.
     */
    public void run() throws IOException {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            PipelineRequestParameters params = parseCommandLineToParams(cmd);

            if (cmd.hasOption("h")) {
                printHelpFormatter();
                return;
            }

            if (cmd.hasOption("c") && (cmd.hasOption("f"))) {
                SseController.sendEventAndComplete(
                    emitter, pipelineService.validateConfiguration(params)
                );
            } else if (Arrays.asList(args).contains("run") &&
                    (cmd.hasOption("f"))) {
                SseController.sendEventAndComplete(
                    emitter,
                    pipelineService.runPipelineLocally(params)
                );
            } else if (Arrays.asList(args).contains("status")) {
                SseController.sendEventAndComplete(
                    emitter,
                    pipelineService.printPipelineStatus(params)
                );
            } else if (cmd.hasOption("d") && (cmd.hasOption("f"))) {
                SseController.sendEventAndComplete(
                    emitter,
                    pipelineService.performDryRun(params)
                );
            } else if (cmd.hasOption("f")) {
                SseController.sendEventAndComplete(
                    emitter,
                    pipelineService.checkFileExists(params)
                );
            } else if (Arrays.asList(args).contains("report")) {
                SseController.sendEventAndComplete(emitter,
                        pipelineService.generateReport(params).toString());
            } else {
                printHelpFormatter();
            }
        } catch (ParseException e) {
            SseController.sendEvent(
                "Error parsing command line arguments: " + e.getMessage()
            );
        }
    }


    /**
     * Converts parsed command line arguments to a unified request parameters object.
     *
     * @param cmd Parsed command line
     * @return Unified request parameters
     */
    private PipelineRequestParameters parseCommandLineToParams(CommandLine cmd) {
        PipelineRequestParameters params = new PipelineRequestParameters();

        // Filename
        if (cmd.hasOption("f")) {
            params.add("filename", cmd.getOptionValue("f"));
        }

        // Repository
        if (cmd.hasOption("repo")) {
            params.add("repo", cmd.getOptionValue("repo"));
        }

        // Verbose logging
        if (cmd.hasOption("vv")) {
            params.add("verboseLogging", true);
        }

        // Pipeline name
        if (cmd.hasOption("pipeline")) {
            params.add("pipelineName", cmd.getOptionValue("pipeline"));
        }

        // Stage
        if (cmd.hasOption("stage")) {
            params.add("stage", cmd.getOptionValue("stage"));
        }

        // Job
        if (cmd.hasOption("job")) {
            params.add("job", cmd.getOptionValue("job"));
        }

        // Run number
        if (cmd.hasOption("runNumber")) {
            params.add("runNumber", cmd.getOptionValue("runNumber"));
        }

        // Format
        if (cmd.hasOption("format")) {
            params.add("format", cmd.getOptionValue("format"));
        }

        // Local flag
        if (cmd.hasOption("local") ||
                !cmd.hasOption("repo") || cmd.hasOption("f")
        ) {
            params.add("isLocal", true);
            params.add("localDir", cmd.getOptionValue("localDir"));
        }

        // Branch
        if (cmd.hasOption("branch")) {
            params.add("branch", cmd.getOptionValue("branch"));
        }

        // Commit
        if (cmd.hasOption("commit")) {
            params.add("commit", cmd.getOptionValue("commit"));
        }
        return params;
    }
}
