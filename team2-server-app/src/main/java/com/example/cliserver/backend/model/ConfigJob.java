package com.example.cliserver.backend.model;

import java.util.ArrayList;
import java.util.List;

import static com.example.cliserver.backend.utils.PipelineUtils.checkFieldIsString;

/**
 * Represents the configuration of a job within a pipeline.
 * <p>
 * This class encapsulates all the configuration details of a job, including its name,
 * stage, execution environment, dependencies, scripts, and failure handling settings.
 * It is used to parse and validate job configurations from YAML definition files.
 * </p>
 */
public class ConfigJob {

    /**
     * Creates a deep copy of another ConfigJob instance.
     * This constructor initializes a new ConfigJob with the same properties and collections
     * as the provided ConfigJob object.
     *
     * @param configJob the source ConfigJob to copy properties from
     */
    public ConfigJob(ConfigJob configJob) {
        this.name = configJob.name;
        this.stage = configJob.stage;
        this.image = configJob.image;
        this.allowFailure = configJob.allowFailure;
        this.registry = configJob.registry;
        this.uploadRepo = configJob.uploadRepo;
        if (configJob.script != null) {
            this.script = configJob.getScript();
        }
        if (configJob.needs != null) {
            this.needs = configJob.getNeeds();
        }
        if (configJob.ports != null) {
            this.ports = configJob.getPorts();
        }
        if(configJob.artifacts != null) {
            this.artifacts = configJob.getArtifacts();
        }
    }

    /**
     * Constructs a new empty ConfigJob.
     */
    public ConfigJob() {
    }

    private String name;
    private String stage;
    private String registry;
    private String image;
    private String uploadRepo;
    private List<String> script;
    private List<String> needs;
    private List<String> ports;
    private boolean allowFailure;
    private List<String> artifacts;

    /**
     * Returns an unmodifiable list of artifacts to prevent external modifications.
     *
     * @return an unmodifiable list of artifacts
     */
    public List<String> getArtifacts() {
        return artifacts != null ? new ArrayList<>(artifacts) : new ArrayList<>();
    }

    /**
     * Sets the artifact list with a defensive copy to prevent external modifications.
     *
     * @param newArtifacts the list of artifacts to set
     */
    public void setArtifacts(List<String> newArtifacts) {
        if (newArtifacts == null) {
            throw new IllegalArgumentException("Artifacts list cannot be null");
        }
        this.artifacts = new ArrayList<>(newArtifacts);
    }

    /**
     * Gets the raw allowFailure flag for a job or stage.
     *
     * @return the Boolean value indicating whether failures are allowed,
     *         or null if no explicit failure behavior is set
     */
    public boolean getAllowFailure() {
        return allowFailure;
    }

    /**
     * Sets the allowFailure flag to determine if failures should be tolerated.
     *
     * @param allowFailure a Boolean indicating whether job or stage failures
     *                     are permitted to continue the pipeline execution
     */
    public void setAllowFailure(boolean allowFailure) {
        this.allowFailure = allowFailure;
    }

    /**
     * Checks if the job or stage is configured to allow failures.
     *
     * @return true if failures are explicitly allowed, false otherwise
     */
    public boolean isAllowFailure() {
        return Boolean.TRUE.equals(allowFailure);
    }

    /**
     * Gets the ports to be exposed for this job.
     *
     * @return list of ports to expose, or null if not specified
     */
    public List<String> getPorts() {
        return ports != null ? new ArrayList<>(ports) : new ArrayList<>();
    }

    /**
     * Sets the ports to be exposed for this job.
     *
     * @param ports list of ports to expose
     */
    public void setPorts(List<String> ports) {
        this.ports = ports != null ? new ArrayList<>(ports) : null;
    }

    /**
     * Gets the name of the job.
     *
     * @return the name of the job
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the job.
     *
     * <p>Validates that the name is of type String. Throws an {@link IllegalArgumentException}
     * if the value is not a string.</p>
     *
     * @param name the job name
     */
    public void setName(Object name) {
        checkFieldIsString(name, "jobs: name");
        this.name = (String) name;
    }

    /**
     * Gets the stage of the job.
     *
     * @return the stage of the job
     */
    public String getStage() {
        return stage;
    }

    /**
     * Sets the stage of the job.
     *
     * <p>Validates that the stage is of type String. Throws an {@link IllegalArgumentException}
     * if the value is not a string.</p>
     *
     * @param stage the stage of the job
     */
    public void setStage(Object stage) {
        checkFieldIsString(stage, "jobs: stage");
        this.stage = (String) stage;
    }

    /**
     * Gets the Docker registry to use for this job.
     *
     * @return the Docker registry url
     */
    public String getRegistry() {
        return registry;
    }

    /**
     * Sets the Docker registry to use for this job.
     *
     * <p>Validates that the registry url is of type String. Throws an
     * {@link IllegalArgumentException} if the value is not a string.</p>
     *
     * @param registry the Docker registry to be used for this job
     */
    public void setRegistry(Object registry) {
        checkFieldIsString(registry, "jobs: registry");
        this.registry = (String) registry;
    }

    /**
     * Gets the image associated with the job.
     *
     * @return the image used for the job (typically a Docker image)
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets the image used for the job.
     *
     * <p>Validates that the image is of type String. Throws an {@link IllegalArgumentException}
     * if the value is not a string.</p>
     *
     * @param image the Docker image to be used with this job
     */
    public void setImage(Object image) {
        checkFieldIsString(image, "jobs: image");
        this.image = (String) image;
    }

    /**
     * Gets the upload repo to use for this job.
     *
     * @return the upload repo path
     */
    public String getUploadRepo() {
        return uploadRepo;
    }

    /**
     * Sets the upload repo to use for this job.
     *
     * <p>Validates that the upload repo path is of type String. Throws an
     * {@link IllegalArgumentException} if the value is not a string.</p>
     *
     * @param uploadRepo the upload repo path to be used for this job
     */
    public void setUploadRepo(Object uploadRepo) {
        checkFieldIsString(uploadRepo, "jobs: upload-repo");
        this.uploadRepo = (String) uploadRepo;
    }

    /**
     * Gets the script associated with the job.
     *
     * @return the list of script commands for the job
     */
    public List<String> getScript() {
        return script != null ? new ArrayList<>(script) : new ArrayList<>();
    }

    /**
     * Sets the script commands for the job.
     *
     * <p>Validates that the script is not null. Throws an {@link IllegalArgumentException}
     * if the list is null.</p>
     *
     * @param script the list of script commands for the job
     */
    public void setScript(List<String> script) {
        if (script == null) {
            throw new IllegalArgumentException("empty job script key. There must be at least " +
                    "one script element defined.");
        }
        this.script = new ArrayList<>(script);
    }

    /**
     * Gets the dependencies (other jobs that must be executed before this one) for the job.
     *
     * @return the list of job names that this job depends on
     */
    public List<String> getNeeds() {
        return needs != null ? new ArrayList<>(needs) : new ArrayList<>();
    }

    /**
     * Sets the list of job dependencies.
     *
     * @param needs the list of job names that this job depends on
     */
    public void setNeeds(List<String> needs) {
        this.needs = (needs == null) ? new ArrayList<>() : new ArrayList<>(needs);
    }

}
