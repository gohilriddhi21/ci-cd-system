package com.example.cliserver.backend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bson.types.ObjectId;


/**
 * Represents a pipeline run document in MongoDB.
 */
public class PipelineRun {
    private ObjectId id;
    private String repo;
    private String branch;
    private String commit;
    private String pipelineName;
    private String fileName;
    private int runNumber;
    private long startTime;
    private long completionTime;
    private Status status;
    private List<Stage> stages;
    private boolean isLocal;
    private String registry;
    private String image;
    private String uploadRepo;

    /**
     * Constructs a {@code PipelineRun} object.
     */
    public PipelineRun() {

    }
    /**
     * Returns the name of the file associated with the report.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the name of the file associated with the report.
     *
     * @param fileName the name of the file to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns the current status of the report.
     *
     * @return the report status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status of the report.
     *
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Gets the ID of the pipeline run.
     *
     * @return the ID of the pipeline run
     */
    public ObjectId getId() {
        return id != null ? new ObjectId(id.toString()) : null;
    }

    /**
     * Sets the ID of the pipeline run.
     *
     * @param id the ID of the pipeline run
     */
    public void setId(ObjectId id) {
        this.id = id != null ? new ObjectId(id.toString()) : null;
    }

    /**
     * Gets the repository of the pipeline run.
     *
     * @return the repository of the pipeline run
     */
    public String getRepo() {
        return repo;
    }

    /**
     * Sets the repository of the pipeline run.
     *
     * @param repo the repository of the pipeline run
     */
    public void setRepo(String repo) {
        this.repo = repo;
    }

    /**
     * Gets the branch of the pipeline run.
     *
     * @return the branch of the pipeline run
     */
    public String getBranch() {
        return branch;
    }

    /**
     * Sets the branch of the pipeline run.
     *
     * @param branch the branch of the pipeline run
     */
    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * Gets the commit ID for the pipeline run.
     *
     * @return the commit ID of the pipeline run
     */
    public String getCommit() {
        return commit;
    }

    /**
     * Sets the commit ID for the pipeline run.
     *
     * @param commit the commit ID of the pipeline run
     */
    public void setCommit(String commit) {
        this.commit = commit;
    }

    /**
     * Gets the name of the pipeline.
     *
     * @return the name of the pipeline
     */
    public String getPipelineName() {
        return pipelineName;
    }

    /**
     * Sets the name of the pipeline.
     *
     * @param pipelineName the name of the pipeline
     */
    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    /**
     * Gets the run number of the pipeline.
     *
     * @return the run number of the pipeline
     */
    public int getRunNumber() {
        return runNumber;
    }

    /**
     * Sets the run number of the pipeline.
     *
     * @param runNumber the run number of the pipeline
     */
    public void setRunNumber(int runNumber) {
        this.runNumber = runNumber;
    }

    /**
     * Gets the start time of the pipeline run.
     *
     * @return the start time of the pipeline run
     */
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * Sets the start time of the pipeline run.
     *
     * @param startTime the start time of the pipeline run
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    /**
     * Gets the completion time of the pipeline run.
     *
     * @return the completion time of the pipeline run
     */
    public long getCompletionTime() {
        return this.completionTime;
    }

    /**
     * Sets the completion time of the pipeline run.
     *
     * @param completionTime the completion time of the pipeline run
     */
    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }
    /**
     * Gets the pipeline status.
     *
     * @return the status of the pipeline
     */
    public Status getPipelineStatus() {
        return status;
    }

    /**
     * Sets the pipeline status.
     *
     * @param status the status of the pipeline
     */
    public void setPipelineStatus(Status status) {
        this.status = status;
    }

    /**
     * Gets the stages of the pipeline run.
     *
     * @return the stages of the pipeline
     */
    public List<Stage> getStages() {
        return stages != null ? new ArrayList<>(stages) : new ArrayList<>();
    }

    /**
     * Sets the stages of the pipeline run.
     *
     * @param stages the stages of the pipeline run
     */
    public void setStages(List<Stage> stages) {
        // Initialize if null
        this.stages = Objects.requireNonNullElseGet(stages, ArrayList::new);
    }

    /**
     * Checks if the pipeline run is local.
     *
     * @return true if the pipeline is local, false otherwise
     */
    public boolean isLocal() {
        return isLocal;
    }

    /**
     * Sets whether the pipeline run is local.
     *
     * @param isLocal true if the pipeline is local, false otherwise
     */
    public void setLocal(boolean isLocal) {
        this.isLocal = isLocal;
    }

    /**
     * Adds stages data into the pipeline run report object.
     *
     * @param stage contains information about stages.
     */
    public void addStage(Stage stage) {
        stages.add(stage);
    }

    /**
     * Gets the docker registry for the pipeline.
     *
     * @return the registry for the pipeline
     */
    public String getRegistry() {
        return registry;
    }

    /**
     * Sets the registry for the pipeline.
     *
     *
     * @param registry the registry of the pipeline
     */
    public void setRegistry(String registry) {
        this.registry = registry;
    }

    /**
     * Gets the docker image for the pipeline.
     *
     * @return the image for the pipeline
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets the docker image for the pipeline.
     *
     * @param image the image of the pipeline
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Gets the upload repo path for the pipeline.
     *
     * @return the upload repo for the pipeline
     */
    public String getUploadRepo() {
        return uploadRepo;
    }

    /**
     * Sets the upload repo for the pipeline.
     *
     *
     * @param uploadRepo the upload repo of the pipeline
     */
    public void setUploadRepo(String uploadRepo) {
        this.uploadRepo = uploadRepo;
    }
}
