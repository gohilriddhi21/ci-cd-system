package com.example.cliserver.backend.commands.runCommand;

import com.example.cliserver.backend.model.Status;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PipelineExecutionTest {
    private final String testPipelineId = "test123";

    @Test
    public void testInitialStatusIsPending() {
        PipelineExecution execution = new PipelineExecution(testPipelineId, false);
        assertEquals(Status.PENDING, execution.getStatus());
    }

    @Test
    public void testSetStatusUpdatesCorrectly() {
        PipelineExecution execution = new PipelineExecution(testPipelineId, false);
        execution.setStatus(Status.SUCCESS);
        assertEquals(Status.SUCCESS, execution.getStatus());
    }

    @Test
    public void testLogMessageIsStoredAndFormatted() {
        PipelineExecution execution = new PipelineExecution(testPipelineId, false);
        execution.log("Test log entry");

        List<String> logs = execution.getLogs();
        assertEquals(1, logs.size());
        assertTrue(logs.get(0).contains("[Pipeline: test123]"));
        assertTrue(logs.get(0).contains("Test log entry"));
    }

    @Test
    void testPipelineIdGetter() {
        PipelineExecution execution = new PipelineExecution(testPipelineId, true);
        assertEquals(testPipelineId, execution.getPipelineId());
    }
}
