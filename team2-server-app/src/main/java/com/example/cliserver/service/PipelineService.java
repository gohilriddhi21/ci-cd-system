package com.example.cliserver.service;

import com.example.cliserver.backend.model.PipelineRequestParameters;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Interface defining the core operations for pipeline management.
 */
public interface PipelineService {
    /**
     * Validates a pipeline configuration file.
     *
     * @param params Request parameters
     * @return Validation result or error message
     */
    String validateConfiguration(PipelineRequestParameters params) throws IOException;

    /**
     * Runs a pipeline locally.
     *
     * @param params Request parameters
     * @return Execution result or error message
     */
    String runPipelineLocally(PipelineRequestParameters params) throws IOException;

    /**
     * Performs a dry run of the pipeline.
     *
     * @param params Request parameters
     * @return Dry run result
     */
    String performDryRun(PipelineRequestParameters params);

    /**
     * Checks if a specific file exists.
     *
     * @param params Request parameters
     * @return string mentioning if file exists or not
     */
    String checkFileExists(PipelineRequestParameters params);

    /**
     * Generates a pipeline report based on specified parameters.
     *
     * @param params Request parameters
     * @return JSON object of the report
     */
    JSONObject generateReport(PipelineRequestParameters params);

    /**
     * Retrieves and prints the status of pipeline runs.
     *
     * @param params Request parameters
     * @return A formatted string representing the pipeline run status
     */
    String printPipelineStatus(PipelineRequestParameters params);

}