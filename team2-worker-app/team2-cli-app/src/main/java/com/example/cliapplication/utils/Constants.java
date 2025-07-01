package com.example.cliapplication.utils;

/**
 * Contains field name constants used across the CLI application.
 * <p>
 * These constants are primarily keys used in MongoDB documents or JSON reports,
 * and are not expected to change during runtime.
 */
public class Constants {

    // === Pipeline & Execution Metadata Fields ===

    /**
     * Field name for the pipeline name.
     */
    public static final String PIPELINE_NAME_FIELD = "pipelineName";

    /**
     * Field name for the run number.
     */
    public static final String RUN_NUMBER_FIELD = "runNumber";

    /**
     * Field name for the commit hash or ID.
     */
    public static final String COMMIT_FIELD = "commit";

    /**
     * Field name representing the pipeline's overall status.
     */
    public static final String PIPELINE_STATUS_FIELD = "pipelineStatus";

    // === Stage Fields ===

    /**
     * Field name for the stage name.
     */
    public static final String STAGE_NAME_FIELD = "stageName";

    /**
     * Field name for the stage status.
     */
    public static final String STAGE_STATUS_FIELD = "stageStatus";

    // === Job Fields ===

    /**
     * Field name for the job name.
     */
    public static final String JOB_NAME_FIELD = "jobName";

    /**
     * Field name for the job status.
     */
    public static final String JOB_STATUS_FIELD = "jobStatus";

    /**
     * Field name indicating whether the job allows failure.
     */
    public static final String ALLOWS_FAILURE_FIELD = "allowsFailure";

    // === Timing Fields ===

    /**
     * Field name for job or stage start time (milliseconds).
     */
    public static final String START_TIME_FIELD = "startTime";

    /**
     * Field name for job or stage completion time (milliseconds).
     */
    public static final String COMPLETION_TIME_FIELD = "completionTime";

    // === JSON Report Keys ===

    /**
     * JSON key used to access job-related data.
     */
    public static final String JSON_DATA_KEY_JOB = "Job Report";

    /**
     * JSON key used to access stage-related data.
     */
    public static final String JSON_DATA_KEY_STAGE = "Stage Report";

    /**
     * Default key used when no specific report type is defined.
     */
    public static final String DEFAULT_JSON_KEY = "Report";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Constants() {}
}
