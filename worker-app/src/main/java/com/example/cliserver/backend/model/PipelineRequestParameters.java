package com.example.cliserver.backend.model;

import com.example.cliserver.backend.utils.Constants;
import java.util.HashMap;
import java.util.Map;

/**
 * Unified parameters for pipeline operations.
 */
public class PipelineRequestParameters {
    private final Map<String, Object> parameters;

    /**
     * Constructs an empty PipelineRequestParameters.
     */
    public PipelineRequestParameters() {
        this.parameters = new HashMap<>();
    }

    /**
     * Adds a parameter to the request.
     *
     * @param key Parameter name
     * @param value Parameter value
     * @return this, for method chaining
     */
    public PipelineRequestParameters add(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    /**
     * Retrieves a parameter value.
     *
     * @param key Parameter name
     * @return Parameter value or null
     */
    public Object get(String key) {
        return parameters.get(key);
    }

    /**
     * Retrieves a parameter value with a default.
     *
     * @param key Parameter name
     * @param defaultValue Default value if parameter is not found
     * @return Parameter value or default value
     */
    public Object get(String key, Object defaultValue) {
        return parameters.getOrDefault(key, defaultValue);
    }

    /**
     * Checks if a parameter exists.
     *
     * @param key Parameter name
     * @return true if parameter exists, false otherwise
     */
    public boolean has(String key) {
        return parameters.containsKey(key);
    }

    /**
     * Gets all parameters.
     *
     * @return Map of all parameters
     */
    public Map<String, Object> getAll() {
        return new HashMap<>(parameters);
    }

    /**
     * Retrieves the filename parameter.
     *
     * @return the filename string, or {@code null} if not set
     */
    public String getFilename() {
        return (String) get("filename");
    }

    /**
     * Retrieves the repository URL parameter.
     *
     * @return the repository URL string, or {@code null} if not set
     */
    public String getRepo() {
        return (String) get("repo");
    }

    /**
     * Retrieves the branch name parameter.
     *
     * @return the branch name string, or {@code null} if not set
     */
    public String getBranch() {
        return (String) get("branch");
    }

    /**
     * Retrieves the commit hash parameter.
     *
     * @return the commit hash string, or {@code null} if not set
     */
    public String getCommit() {
        return (String) get("commit");
    }

    /**
     * Retrieves the verbose logging flag.
     *
     * @return {@code true} if verbose logging is enabled; {@code false} otherwise
     */
    public Boolean getVerboseLogging() {
        return (Boolean) get("verboseLogging", false);
    }

    /**
     * Retrieves the pipeline name parameter.
     *
     * @return the pipeline name string, or {@code null} if not set
     */
    public String getPipelineName() {
        return (String) get("pipelineName");
    }

    /**
     * Retrieves the pipeline stage name parameter.
     *
     * @return the stage name string, or {@code null} if not set
     */
    public String getStage() {
        return (String) get("stage");
    }

    /**
     * Retrieves the job name parameter.
     *
     * @return the job name string, or {@code null} if not set
     */
    public String getJob() {
        return (String) get("job");
    }

    /**
     * Retrieves the pipeline run number parameter.
     *
     * @return the run number string, or {@code null} if not set
     */
    public String getRunNumber() {
        return (String) get("runNumber");
    }

    /**
     * Retrieves the report format parameter.
     *
     * @return the format string, or {@code null} if not set
     */
    public String getFormat() {
        return (String) get("format");
    }

    /**
     * Checks whether the operation should be performed on a local repository.
     *
     * @return {@code true} if the local flag is set; {@code false} otherwise
     */
    public Boolean isLocal() {
        return (Boolean) get("isLocal", false);
    }

    /**
     * Retrieves the local directory path to be used when working with local repositories.
     * Defaults to {@link Constants#DIRECTORY} if not explicitly provided.
     *
     * @return the local directory path string
     */
    public String getLocalDirPath() {
        return (String) get("localDir", Constants.DIRECTORY);
    }
}