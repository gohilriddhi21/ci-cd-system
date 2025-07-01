package com.example.cliserver.backend.utils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;

/**
 * This class contains constants that are typically not expected to change
 * during the execution of the program.
 */
public class Constants {

    public static final String JSON_FORMAT = "JSON";
    /**
     * The directory path where pipeline-related files are stored.
     */
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static String DIRECTORY = ".pipelines/";

    /**
     * The directory path where pipeline-related files are stored.
     */
    public static final String REMOTE_DIRECTORY = "remote/";

    /**
     * The field name for the repository in the MongoDB document.
     */
    public static final String REPO_FIELD = "repo";

    /**
     * The field name for the file name in the MongoDB document.
     */
    public static final String FILE_NAME_FIELD = "fileName";

    /**
     * The field name for the branch in the MongoDB document.
     */
    public static final String BRANCH_FIELD = "branch";
    /**
     * The field name for the commit ID in the MongoDB document.
     */
    public static final String COMMIT_FIELD = "commit";
    /**
     * The field name for the pipeline name in the MongoDB document.
     */
    public static final String PIPELINE_NAME_FIELD = "pipelineName";
    /**
     * The field name for the run number in the MongoDB document.
     */
    public static final String RUN_NUMBER_FIELD = "runNumber";
    /**
     * The field name for the start time in the MongoDB document.
     */
    public static final String START_TIME_FIELD = "startTime";
    /**
     * The field name for the completion time in the MongoDB document.
     */
    public static final String COMPLETION_TIME_FIELD = "completionTime";
    /**
     * The field name for the pipeline status in the MongoDB document.
     */
    public static final String PIPELINE_STATUS_FIELD = "pipelineStatus";
    /**
     * The field name for the stages array in the MongoDB document.
     */
    public static final String STAGES_FIELD = "stages";
    /**
     * The field name for the stage name in the MongoDB document.
     */
    public static final String STAGE_NAME_FIELD = "stageName";

    /**
     * The field name for the stage status in the MongoDB document.
     */
    public static final String STAGE_STATUS_FIELD = "stageStatus";
    /**
     * The field name for the jobs array in the MongoDB document.
     */
    public static final String JOBS_FIELD = "jobs";
    /**
     * The field name for the job name in the MongoDB document.
     */
    public static final String JOB_NAME_FIELD = "jobName";
    /**
     * The field name for the job status in the MongoDB document.
     */
    public static final String JOB_STATUS_FIELD = "jobStatus";
    /**
     * The field name for the 'allows failure' flag in the MongoDB document.
     */
    public static final String ALLOWS_FAILURE_FIELD = "allowsFailure";
    /**
     * The field name for the 'is local' flag in the MongoDB document.
     */
    public static final String IS_LOCAL_FIELD = "isLocal";
    /**
     * The repo name used for the local working directory
     */
    public static final String LOCAL_REPO = "local";

    /**
     * The queue name used for the RabbitMQ
     */
    public static final String QUEUE_NAME = "pipeline-jobs";

    /**
     * Key used for storing and retrieving pipeline run JSON in messages.
     */
    public static final String PIPELINE_RUN_JSON_KEY = "pipelineRun";

    /**
     * The format field for displaying reports
     */
    public static final String FORMAT_FIELD = "format";

    /**
     * Default list of stages for a pipeline. These are used if the user does not specify their own
     * custom stages in the pipeline yaml file.
     */
    public static final List<String> defaultPipelineStages =
            List.of("build", "test", "doc", "deploy");

    /**
     * The path for config yaml file that has all the credentials.
     */
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static String CONFIG_FILE_PATH = "src/main/resources/config.yaml";
    public static final String REMOTE_BRANCH_PREFIX = "refs/remotes/origin/";
    @SuppressFBWarnings("MS_SHOULD_BE_FINAL")
    public static String LOCAL_ARTIFACTS_DIRECTORY = "local_artifacts";

    public static final String WORKSPACE_MOUNT_PATH = "/app";

    /**
     * Directory where pipeline execution logs are stored.
     */
    public static final String PIPELINE_LOG_DIRECTORY = "pipeline_logs";

    public static final int DOCKER_EXEC_TIMEOUT_SECONDS = 600;

    public static final String JSON_DATA_KEY_JOB = "Job Report";
    public static final String JSON_DATA_KEY_STAGE = "Stage Report";
    public static final String JSON_DATA_KEY_DEFAULT = "Report";

    private Constants() {
    }
}

