package com.example.cliserver.backend.model;

/**
 * Represents a single job in the pipeline.
 */
public class Job extends ConfigJob{
    /**
     * Constructs a new Job instance.
     *
     */
    public Job() {
        super();
    }

    /**
     * Constructs a new Job based on a ConfigJob.
     * This constructor copies all relevant properties from the ConfigJob to this Job.
     *
     * @param configJob the ConfigJob to copy properties from
     */
    public Job(ConfigJob configJob) {
        super(configJob);
    }

    private Status jobStatus;

    private long startTime;

    private long completionTime;

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
        return completionTime;
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
     * Gets the status of the job.
     *
     * @return the status of the job
     */
    public Status getJobStatus() {
        return jobStatus;
    }

    /**
     * Sets the status of the job.
     *
     * @param jobStatus is the status of the corresponding job
     */
    public void setJobStatus(Status jobStatus) {
        this.jobStatus = jobStatus;
    }


}
