package com.example.cliserver.backend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a stage within a pipeline run.
 */
public class Stage {
    private String stageName;
    private Status stageStatus;
    private long startTime;
    private long completionTime;
    private List<Job> jobs;

    /**
     * Constructs a {@code Stage} object.
     */
    public Stage() {}

    /**
     * Add jobs to the stage object.
     *
     * @param job the array of jobs
     */
    public void addJob(Job job) {
        if (this.jobs == null) {
            this.jobs = new ArrayList<>();
        }
        this.jobs.add(job);
    }

    /**
     * Gets the name of the stage.
     *
     * @return the name of the stage
     */
    public String getStageName() {
        return stageName;
    }

    /**
     * Sets the name of the stage.
     *
     * @param stageName the name of the stage
     */
    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    /**
     * Gets the status of the stage.
     *
     * @return the status of the stage
     */
    public Status getStageStatus() {
        return stageStatus;
    }

    /**
     * Sets the status of the stage.
     *
     * @param stageStatus the status of the stage
     */
    public void setStageStatus(Status stageStatus) {
        this.stageStatus = stageStatus;
    }

    /**
     * Gets the start time of the stage.
     *
     * @return the start time of the stage
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time of the stage.
     *
     * @param startTime the start time of the stage
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    /**
     * Gets the completion time of the stage.
     *
     * @return the completion time of the stage
     */
    public long getCompletionTime() {
        return this.completionTime;
    }

    /**
     * Sets the completion time of the stage.
     *
     * @param completionTime the completion time of the stage
     */
    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }
    /**
     * Gets the jobs in this stage.
     *
     * @return the jobs in this stage
     */
    public List<Job> getJobs() {
        return jobs != null ? new ArrayList<>(jobs) : null;
    }

    /**
     * Sets the jobs for this stage.
     *
     * @param jobs the list of jobs in this stage
     */
    public void setJobs(List<Job> jobs) {
        this.jobs = Objects.requireNonNullElseGet(jobs, ArrayList::new);
    }

    @Override
    public String toString() {
        return "Stage{" +
                "stageName='" + stageName + '\'' +
                ", stageStatus='" + stageStatus + '\'' +
                ", startTime=" + startTime +
                ", completionTime=" + completionTime +
                ", jobs=" + jobs +
                '}';
    }
}
