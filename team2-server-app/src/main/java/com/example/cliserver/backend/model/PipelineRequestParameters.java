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

    // Convenience methods for common parameters
    public String getFilename() {
        return (String) get("filename");
    }

    public String getRepo() {
        return (String) get("repo");
    }

    public String getBranch() {
        return (String) get("branch");
    }

    public String getCommit() {
        return (String) get("commit");
    }

    public Boolean getVerboseLogging() {
        return (Boolean) get("verboseLogging", false);
    }

    public String getPipelineName() {
        return (String) get("pipelineName");
    }

    public String getStage() {
        return (String) get("stage");
    }

    public String getJob() {
        return (String) get("job");
    }

    public String getRunNumber() {
        return (String) get("runNumber");
    }

    public String getFormat() {
        return (String) get("format");
    }

    public Boolean isLocal() {
        return (Boolean) get("isLocal", false);
    }

    public String getLocalDirPath() {
        return (String) get("localDir", Constants.DIRECTORY);
    }
}