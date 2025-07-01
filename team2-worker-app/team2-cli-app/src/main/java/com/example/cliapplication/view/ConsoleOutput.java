package com.example.cliapplication.view;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 * Responsible for displaying output to the console.
 *
 * This includes standard output messages, error messages, and
 * help information based on the command-line options.
 */
public class ConsoleOutput {
    private final HelpFormatter formatter;

    /**
     * Default Constructor
     *
     */
    public ConsoleOutput() {
        this.formatter = new HelpFormatter();
    }

    /**
     * Displays a standard output message to the console.
     *
     * @param output The string to be displayed as output.
     */
    public void displayOutput(String output) {
        System.out.println(output);
    }

    /**
     * Displays an error message to the console.
     *
     * @param error The string to be displayed as an error.
     */
    public void displayError(String error) {
        System.err.println(error);
    }

    /**
     * Prints the help information for the command-line application to the console.
     * This includes the usage syntax and descriptions of the available options.
     *
     * @param options The {@code Options} object defining the command-line options.
     */
    public void printHelp(Options options) {
        String header = "\n";
        String footer = String.format(" %-10s %s%n %-10s %s%n %-10s %s",
                "report", "Generate reports for pipeline runs",
                "run", "Executes the pipeline locally",
                "status", "Display status information for pipeline runs"
        );
        formatter.printHelp("java -jar pipeline-tool.jar [options]", header, options, footer);
    }
}