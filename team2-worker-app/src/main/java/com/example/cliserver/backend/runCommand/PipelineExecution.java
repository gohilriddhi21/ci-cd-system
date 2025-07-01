package com.example.cliserver.backend.runCommand;

import com.example.cliserver.backend.model.Status;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.example.cliserver.backend.utils.Constants.PIPELINE_LOG_DIRECTORY;

/**
 * Represents an execution instance of a CI/CD pipeline.
 * This class manages the execution state, status tracking, and logging functionality
 * for a single pipeline run. It provides thread-safe status updates and logging capabilities.
 */
public class PipelineExecution {

    /**
     * Unique identifier for this pipeline execution.
     */
    private final String pipelineId;
    /**
     * Current status of the pipeline execution.
     */
    private Status status;
    /**
     * Collection of log messages for this pipeline execution.
     */
    private final List<String> logs = new ArrayList<>();

    /**
     * Creates a new pipeline execution instance.
     * Initializes the execution with a PENDING status and creates the log directory
     * if it doesn't already exist.
     *
     * @param pipelineId Unique identifier for this pipeline execution
     */
    public PipelineExecution(String pipelineId) {
        this.pipelineId = pipelineId;
        this.status = Status.PENDING;

        try {
            // Create logs directory if it doesn't exist
            File logDir = new File(PIPELINE_LOG_DIRECTORY);
            if (!logDir.exists()) {
                if (!logDir.mkdirs()) {
                    throw new IOException("Failed to create pipeline log directory: "
                            + PIPELINE_LOG_DIRECTORY);
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Updates the status of the pipeline execution in a thread-safe manner.
     * Validates the new status and logs the status change.
     *
     * @param status The new status to set
     * @throws IllegalArgumentException if the provided status is not valid
     */
    public synchronized void setStatus(Status status) {
        this.status = status;
        log("Pipeline status updated to: " + status);
    }

    /**
     * Logs a message with timestamp and pipeline identifier.
     * The message is written to both console output and a pipeline-specific log file.
     * This method is thread-safe.
     *
     * @param message The message to log
     */
    public synchronized void log(String message) {
        String timestamp = String.format("%tF %<tT", System.currentTimeMillis());
        String logMessage = String.format("[Pipeline: %s] [%s] %s",
                pipelineId,
                timestamp,
                message);

        logs.add(logMessage);

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(
                PIPELINE_LOG_DIRECTORY + "/pipeline_" + pipelineId + ".log", true),
                StandardCharsets.UTF_8)) {
            writer.write(logMessage + "\n");
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    /**
     * Retrieves the current status of the pipeline execution.
     * This method is thread-safe.
     *
     * @return The current status of the pipeline execution
     */
    public synchronized Status getStatus() {
        return status;
    }

    /**
     * Retrieves a copy of all logs for this pipeline execution.
     * Returns a new list to prevent external modification of the internal logs.
     * This method is thread-safe.
     *
     * @return A new List containing all log messages
     */
    public synchronized List<String> getLogs() {
        return new ArrayList<>(logs);
    }

    public String getPipelineId() {
        return this.pipelineId;
    }
}