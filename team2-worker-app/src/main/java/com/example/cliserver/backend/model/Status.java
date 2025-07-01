package com.example.cliserver.backend.model;

/**
 * Enum representing the possible statuses for pipeline execution.
 * The possible statuses are:
 * - {@link #PENDING} - The pipeline has been created but has not yet started.
 * - {@link #RUNNING} - The pipeline is currently in progress.
 * - {@link #SUCCESS} - The pipeline has completed successfully.
 * - {@link #FAILED} - The pipeline has failed during execution.
 * - {@link #CANCELED} - The pipeline run was canceled before completion.
 */
public enum Status {
    /**
     * The pipeline has been created but has not yet started.
     */
    PENDING("Pending"),
    /**
     * The pipeline is currently in progress.
     */
    RUNNING("Running"),
    /**
     * The pipeline has completed successfully.
     */
    SUCCESS("Success"),
    /**
     * The pipeline run was canceled before completion.
     */
    CANCELED("Canceled"),
    /**
     * The pipeline has failed during execution.
     */
    FAILED("Failed");

    private final String status;

    /**
     * Constructor to set the status string for the enum value.
     *
     * @param status the status string associated with this enum value
     */
    Status(String status) {
        this.status = status;
    }

    /**
     * Returns the string representation of the pipeline status.
     * This method overrides the {@link Object#toString()} method.
     *
     * @return the string representation of the status
     */
    @Override
    public String toString() {
        return status;
    }
}