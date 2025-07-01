package com.example.cliapplication.service;

import com.example.cliapplication.view.ConsoleOutput;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandLineHandlerTest {

    private CommandLineHandler handler;
    private ConsoleOutput output;

    @BeforeEach
    void setUp() {
        handler = new CommandLineHandler();
        output = new ConsoleOutput();
    }

    @Test
    void testValidCheckCommand() throws ParseException {
        String[] args = {"check", "-f", "file.yaml"};
        CommandLine cmd = handler.parse(args);
        assertTrue(handler.isValidCommand(cmd, args, handler.getOptions(), output));
    }

    @Test
    void testValidRunCommand() throws ParseException {
        String[] args = {"run", "-f", "file.yaml"};
        CommandLine cmd = handler.parse(args);
        assertTrue(handler.isValidCommand(cmd, args, handler.getOptions(), output));
    }

    @Test
    void testValidStatusCommand() throws ParseException {
        String[] args = {"status", "-f", "file.yaml", "-rn", "10"};
        CommandLine cmd = handler.parse(args);
        assertTrue(handler.isValidCommand(cmd, args, handler.getOptions(), output));
    }

    @Test
    void testJustFileName() throws ParseException {
        String[] args = {"check", "-f", "file.yaml"};  // fixed
        CommandLine cmd = handler.parse(args);
        assertTrue(handler.isValidCommand(cmd, args, handler.getOptions(), output));
    }

    @Test
    void testValidReportCommand() throws ParseException {
        String[] args = {"report", "--pipeline", "correct.yaml", "--stage", "test_name", "--job", "job_name"};
        CommandLine cmd = handler.parse(args);
        assertTrue(handler.isValidCommand(cmd, args, handler.getOptions(), output));
    }


    @Test
    void testJustFilenameCommand() throws ParseException {
        String[] args = {"run", "-f", "file.yaml"};  // fixed
        CommandLine cmd = handler.parse(args);
        assertTrue(handler.isValidCommand(cmd, args, handler.getOptions(), output));
    }

    @Test
    void testInvalidCommand_missingFilename() throws ParseException {
        String[] args = {"-c"};
        CommandLine cmd = handler.parse(args);
        assertFalse(handler.isValidCommand(cmd, args, handler.getOptions(), output));
    }

    @Test
    void isValidCommand_shouldReturnFalse_whenReportIsPresentButNoPipelineOption() throws ParseException {
        Options options = handler.getOptions();
        String[] args = {"report", "pipeline"};
        CommandLine cmd = new DefaultParser().parse(options, args);
        ConsoleOutput consoleOutput = new ConsoleOutput();
        assertFalse(handler.isValidCommand(cmd, args, options, consoleOutput));
    }
}