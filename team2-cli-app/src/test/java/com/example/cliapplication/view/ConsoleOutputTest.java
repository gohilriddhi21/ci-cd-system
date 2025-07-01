package com.example.cliapplication.view;

import org.apache.commons.cli.Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleOutputTest {

    private final PrintStream standardOut = System.out;
    private final PrintStream standardErr = System.err;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errorStreamCaptor = new ByteArrayOutputStream();

    private ConsoleOutput consoleOutput;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
        System.setErr(new PrintStream(errorStreamCaptor));
        consoleOutput = new ConsoleOutput();
    }

    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
        System.setErr(standardErr);
    }

    @Test
    void displayOutput_shouldPrintStringToSystemOut() {
        String message = "This is a test output message.";
        consoleOutput.displayOutput(message);
        assertEquals(message + System.lineSeparator(), outputStreamCaptor.toString());
        assertEquals("", errorStreamCaptor.toString());
    }

    @Test
    void printHelp_shouldPrintHelpMessageToSystemOut() {
        Options options = new Options();
        options.addOption("h", "help", false, "Print this help message.");
        options.addOption("f", "file", true, "Path to the pipeline definition file.");
        options.addOption("r", "repo", true, "Repository URL for the pipeline definition.");
        options.addOption("l", "local", false, "Retrieve pipeline reports for the local repo.");
        options.addOption("c", "check", false, "Perform a general file check operation.");
        options.addOption("d", "dry-run", false, "Execute the pipeline locally without running actual jobs.");
        options.addOption("j", "job", true, "Specifies the job name in the pipeline stage.");
        options.addOption("pipeline", true, "Retrieve pipeline reports with this pipeline name.");
        options.addOption("rn", "runNumber", true, "Specifies the run number for the pipeline to report on.");
        options.addOption("s", "stage", true, "Specifies the stage name in the pipeline.");
        options.addOption("vv", false, "Displays detailed logging when running a pipeline.");
        options.addOption("ft", "format", true, "Displays the reports in a specified format.");
        options.addOption("ld", "localDir", true, "Local .pipelines directory path of the client.");

        consoleOutput.printHelp(options);

        String expectedOutputStart = "usage: java -jar pipeline-tool.jar [options]";
        String expectedFooter = String.format(" %-10s %s%n %-10s %s%n %-10s %s",
                "report", "Generate reports for pipeline runs",
                "run", "Executes the pipeline locally",
                "status", "Display status information for pipeline runs"
        );

        String actualOutput = outputStreamCaptor.toString();
        assertTrue(actualOutput.contains(expectedOutputStart));
        assertTrue(actualOutput.contains(expectedFooter));
    }
}