package com.example.cliapplication.service;

import org.apache.commons.cli.*;
import com.example.cliapplication.view.ConsoleOutput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handles the parsing and validation of command-line arguments.
 * <p>
 * This class defines supported CLI options, parses incoming arguments,
 * and validates commands based on the expected syntax for different operations
 * like `check`, `dry-run`, `run`, `status`, or `report`.
 */
public class CommandLineHandler {
    private final Options options = new Options();

    /**
     * Constructs a new {@code CommandLineHandler} and
     * initializes the supported command-line options.
     */
    public CommandLineHandler() {
        initializeOptions();
    }


    /**
     * Defines all supported command-line options for the application.
     * <p>
     * Each option may have a short alias, an optional argument, and a help description.
     */
    private void initializeOptions() {
        options.addOption(Option.builder("c")
                .longOpt("check")
                .desc("Perform a general file check operation")
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

        options.addOption(Option.builder("ft")
                .longOpt("format")
                .hasArg()
                .desc("Displays the reports in a specified format")
                .build());

        options.addOption(Option.builder("ld")
                .longOpt("localDir")
                .hasArg()
                .desc("Local .pipelines directory path of the client")
                .build());
    }

    /**
     * Parses the command-line arguments provided by the user.
     *
     * @param args The raw command-line arguments.
     * @return A {@link CommandLine} object representing the parsed input.
     * @throws ParseException If parsing fails due to invalid or incomplete arguments.
     */
    public CommandLine parse(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    /**
     * Retrieves the {@link Options} object containing all defined CLI options.
     *
     * @return The options supported by this CLI tool.
     */
    public Options getOptions() {
        return options;
    }

    /**
     * Validates whether the parsed command-line arguments form a valid command
     * based on supported combinations like `check -f`, `dry-run -f`, or `report --pipeline`.
     *
     * @param cmd     The parsed command line.
     * @param args    The raw command-line argument array.
     * @param options The full set of expected CLI options.
     * @param output  The output handler for displaying errors and help.
     * @return {@code true} if the command is valid; {@code false} otherwise.
     */
    public boolean isValidCommand(CommandLine cmd, String[] args,
        Options options, ConsoleOutput output) {
        List<String> argsList = Arrays.asList(args);

        boolean hasFilename = hasOption(cmd, "filename", "f");

        boolean isCheck = hasOption(cmd, "check", "c");
        boolean isDryRun = hasOption(cmd, "dry-run", "d");
        boolean isRun = argsList.contains("run");
        boolean isStatus = argsList.contains("status");
        boolean isReport = argsList.contains("report") && cmd.hasOption("pipeline");

        boolean isJustFilename = hasFilename && !(isCheck ||
            isRun || isStatus ||
            isDryRun || isReport);

        if (!( (isCheck && hasFilename)
            || (isDryRun && hasFilename)
            || (isRun && hasFilename)
            || (isStatus && hasFilename)
            || isReport
            || isJustFilename)) {
            suggestValidCommands(args[0], output);
            output.displayError("Invalid command or missing required options.");
            output.printHelp(options);
            return false;
        }

        return true;
    }

    /**
     * Checks if the command-line arguments contain a specific option
     * by matching either its long or short form.
     *
     * @param cmd      The parsed command-line object.
     * @param longOpt  The long name of the option (e.g., {@code "filename"}).
     * @param shortOpt The short alias of the option (e.g., {@code "f"}).
     * @return {@code true} if either form is present; otherwise {@code false}.
     */
    private boolean hasOption(CommandLine cmd, String longOpt, String shortOpt) {
        return cmd.hasOption(longOpt) || cmd.hasOption(shortOpt);
    }

    /**
     * Suggests valid commands when the user enters an unknown command.
     *
     * @param userCommand The command entered by the user.
     * @param output The output handler for displaying suggestions.
     */
    public void suggestValidCommands(String userCommand, ConsoleOutput output) {
        String[] validCommands = {"check", "dry-run", "run", "status", "report", "help"};

        // Find commands that are similar (1-2 characters off)
        List<String> suggestions = new ArrayList<>();
        for (String cmd : validCommands) {
            if (isCloseMatch(userCommand, cmd)) {
                suggestions.add(cmd);
            }
        }

        if (!suggestions.isEmpty()) {
            output.displayError("Unknown command: '" + userCommand + "'. Did you mean: " +
                String.join(", ", suggestions) + "?");
        } else {
            output.displayError("Unknown command: '" + userCommand +
                "'. Valid commands are: " + String.join(", ", validCommands));
        }
    }

    /**
     * Checks if two strings are close matches based on character differences.
     *
     * @param input The input string to check.
     * @param validOption The valid option to compare against.
     * @return True if the strings are close matches, false otherwise.
     */
    private boolean isCloseMatch(String input, String validOption) {
        if (input.equalsIgnoreCase(validOption)) return true;

        // If length differs by more than 2, not a close match
        if (Math.abs(input.length() - validOption.length()) > 2) return false;

        int differences = 0;
        for (int i = 0, j = 0; i < input.length() && j < validOption.length(); i++, j++) {
            if (Character.toLowerCase(input.charAt(i)) !=
                    Character.toLowerCase(validOption.charAt(j))) {
                differences++;
                if (differences > 2) return false;
            }
        }

        return differences <= 2;
    }
}