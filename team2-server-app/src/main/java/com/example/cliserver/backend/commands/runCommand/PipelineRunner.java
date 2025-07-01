package com.example.cliserver.backend.commands.runCommand;

import com.example.cliserver.backend.database.artifactsDB.ArtifactsUploader;
import com.example.cliserver.backend.database.mongoDB.PipelineRunsDao;
import com.example.cliserver.backend.model.Job;
import com.example.cliserver.backend.model.PipelineRun;
import com.example.cliserver.backend.model.Stage;
import com.example.cliserver.backend.model.Status;
import com.example.cliserver.backend.utils.Constants;
import com.example.cliserver.backend.utils.PipelineUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * A runner class responsible for executing CI/CD pipelines defined in configuration files. This
 * class manages pipeline executions, handles job dependencies, and ensures proper execution order
 * of jobs within stages.
 */
public class PipelineRunner {
    public final PipelineRunsDao pipelineRunsDao;
    public final ArtifactsUploader uploader;
    private final DockerContainerExecutor dockerContainerExecutor = new DockerContainerExecutor();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * Constructs a {@code PipelineRunner} object with the specified {@code PipelineRunsDao}.
     * <p>
     * This constructor initializes the {@code PipelineRunner} instance, allowing it to interact
     * with the provided {@code PipelineRunsDao} for performing database operations related to
     * pipeline runs.
     * </p>
     *
     * @param pipelineRunsDao the {@code PipelineRunsDao} used for interacting with the database to
     *                        store pipeline run reports
     */
    public PipelineRunner(PipelineRunsDao pipelineRunsDao) {
        this.pipelineRunsDao = pipelineRunsDao;
        this.uploader = new ArtifactsUploader();
    }

    /**
     * Map to track currently active pipeline executions, keyed by pipeline ID.
     */
    public final Map<String, PipelineExecution> activeExecutions = new ConcurrentHashMap<>();

    /**
     * Initiates the execution of a pipeline defined in the specified configuration file. The
     * pipeline execution is started in a new thread to allow for asynchronous processing.
     *
     * @param pipelineRunReport The pipeline run report to update with execution results
     * @param useVerboseLogging Whether the user specified verbose logging output for this
     *                          pipeline run
     * @return A status message indicating whether the pipeline was started successfully or any
     * error occurred
     */
    public String runPipeline(
            PipelineRun pipelineRunReport,
            boolean useVerboseLogging
    ){
        // Generate pipeline ID based on the file path
        String pipelineId = PipelineUtils.generatePipelineId(pipelineRunReport.getFileName())
                + "_" + pipelineRunReport.getRunNumber();
        pipelineRunReport.setStartTime(System.currentTimeMillis());
        if (activeExecutions.containsKey(pipelineId)) {
            pipelineRunReport.setPipelineStatus(Status.CANCELED);
            pipelineRunReport.setCompletionTime(System.currentTimeMillis());
            updatePipelineRunReport(pipelineRunReport);
            return "Duplicate pipeline execution detected. Using existing execution.";
        }
        activeExecutions.put(pipelineId, new PipelineExecution(pipelineId, useVerboseLogging));

        PipelineExecution execution = activeExecutions.get(pipelineId);

        executePipeline(execution, pipelineRunReport);

        return pipelineRunReport.getPipelineName() + " run: " + pipelineRunReport.getRunNumber();
    }

    /**
     * Creates a new pipeline run report if it doesn't exist, otherwise updates it,
     * and sets its completion time.
     *
     * @param pipelineRun the pipeline run to create or update
     */
    private void updatePipelineRunReport(PipelineRun pipelineRun) {
        pipelineRunsDao.updatePipelineRun(pipelineRun);
    }

    /**
     * Executes the pipeline according to the configuration, processing stages sequentially and
     * managing the pipeline's execution state.
     *
     * @param execution The execution context for tracking status and logging
     * @param pipelineRunReport the {@link PipelineRun} object used to store the details and
     * results of the pipeline run
     */
    private void executePipeline(PipelineExecution execution,
                                 PipelineRun pipelineRunReport) {
        try {
            execution.setStatus(Status.PENDING);
            execution.log("Pipeline transitioned to Pending.");

            boolean pipelineFailed = false;

            List<Stage> allStages = pipelineRunReport.getStages();
            String dockerRegistry = pipelineRunReport.getRegistry();
            String dockerImage = pipelineRunReport.getImage();

            execution.setStatus(Status.RUNNING);
            pipelineRunReport.setPipelineStatus(Status.RUNNING);

            execution.log("Pipeline transitioned to Running.");
            updatePipelineRunReport(pipelineRunReport);

            for (Stage stage : allStages) {
                // Get topologically sorted jobs
                List<Job> stageJobs = stage.getJobs();

                // Log stage dependencies
                logStageDependencies(stage, stageJobs, execution);

                stage.setStageStatus(Status.RUNNING);
                stage.setStartTime(System.currentTimeMillis());
                updatePipelineRunReport(pipelineRunReport);

                // Execute jobs in sorted order
                for (Job job : stageJobs) {
                    job.setStartTime(System.currentTimeMillis());

                    job.setJobStatus(Status.RUNNING);
                    execution.log("Executing job: " + job.getName());

                    updatePipelineRunReport(pipelineRunReport);

                    boolean jobResult =
                            executeJob(job, execution, dockerRegistry, dockerImage);

                    job.setCompletionTime(System.currentTimeMillis());

                    // Update job status based on an execution result
                    if (jobResult) {
                        job.setJobStatus(Status.SUCCESS);
                    } else {
                        job.setJobStatus(Status.FAILED);
                        if (!job.isAllowFailure()) {
                            pipelineFailed = true;
                            execution.log(
                                    "Stage " + stage.getStageName() +
                                            " failed due to job failure: " + job.getName());
                            break;  // Stop execution on failure if `allowFailure` is false
                        }
                    }
                    updatePipelineRunReport(pipelineRunReport);
                }

                stage.setStageStatus(calculateStageStatus(stage.getJobs()));
                stage.setCompletionTime(System.currentTimeMillis());
                updatePipelineRunReport(pipelineRunReport);

                if (pipelineFailed) break;
                execution.log("Stage " + stage.getStageName() + " completed successfully");
            }

            if (pipelineFailed) {
                execution.setStatus(Status.FAILED);
                pipelineRunReport.setPipelineStatus(Status.FAILED);
            } else {
                execution.setStatus(Status.SUCCESS);
                pipelineRunReport.setPipelineStatus(Status.SUCCESS);
                execution.log("Pipeline completed successfully.");

                // Deleting the artifacts for all the jobs,
                // after the entire pipeline run is successfully completed
                execution.log("Starting to delete the artifacts for pipeline: " +
                        pipelineRunReport.getPipelineName());
                PipelineUtils.deleteFile(new File(Constants.LOCAL_ARTIFACTS_DIRECTORY));
                execution.log("Deleted the artifacts for pipeline run: " +
                        pipelineRunReport.getPipelineName());
            }

            pipelineRunReport.setCompletionTime(System.currentTimeMillis());
            updatePipelineRunReport(pipelineRunReport);
        } catch (Exception e) {
            execution.log("Pipeline execution failed due to exception: " + e.getMessage());
        } finally {
            activeExecutions.remove(pipelineRunReport.getPipelineName());
        }
    }

    /**
     * Calculates the status of a stage based on its jobs.
     *
     * @param jobs the jobs in the stage
     * @return the calculated stage status
     */
    private Status calculateStageStatus(List<Job> jobs) {
        // Check if all jobs are pending
        boolean allPending = jobs.stream().allMatch(job ->
                Status.PENDING.equals(job.getJobStatus()));
        if (allPending) {
            return Status.PENDING;
        }

        // Check for failed jobs that don't allow failure
        boolean anyFailedImportant = jobs.stream().anyMatch(job ->
                Status.FAILED.equals(job.getJobStatus()) && !job.isAllowFailure());
        if (anyFailedImportant) {
            return Status.FAILED;
        }

        // Check if any job is running
        boolean anyRunning = jobs.stream().anyMatch(job ->
                Status.RUNNING.equals(job.getJobStatus()));
        if (anyRunning) {
            return Status.RUNNING;
        }

        // Check if all jobs are successful
        boolean allSuccess = jobs.stream().allMatch(job ->
                Status.SUCCESS.equals(job.getJobStatus()) ||
                        (Status.FAILED.equals(job.getJobStatus()) && job.isAllowFailure()));
        if (allSuccess) {
            return Status.SUCCESS;
        }

        // If some jobs are SUCCESS and others are PENDING, we consider it RUNNING
        return Status.RUNNING;
    }

    /**
     * Logs the dependencies for all jobs in the given stage.
     * <p>
     * This method iterates over the list of jobs in the specified stage and logs the names of
     * each job along with its dependencies. If a job does not have any dependencies, it logs
     * "No dependencies" for that job.
     *
     * @param stage The {@link Stage} instance for which job dependencies are being logged.
     * @param jobs A list of {@link Job} objects representing the jobs in the stage.
     * @param execution The {@link PipelineExecution} instance used to log the dependencies.
     */
    private void logStageDependencies(Stage stage, List<Job> jobs,
                                      PipelineExecution execution) {
        execution.log("Stage Dependencies for stage: " + stage.getStageName());
        execution.log("====================================");
        for (Job job : jobs) {
            String dependencies = job.getNeeds() == null || job.getNeeds().isEmpty()
                    ? "No dependencies"
                    : String.join(", ", job.getNeeds());
            execution.log(String.format("  %-20s: Depends on [%s]", job.getName(), dependencies));
        }
        execution.log("====================================");
    }

    /**
     * Executes a single job by running all its defined script commands sequentially. Commands are
     * executed in a shell environment appropriate for the operating system. Tracks and logs the
     * execution time of the job.
     * It also calls upload Artifacts method that uploads the user specified artifacts
     * in MinIO.
     *
     * @param job       The job to execute
     * @param execution The execution context for tracking status and logging
     * @param dockerImage The docker image to use to execute the job
     * @param dockerRegistry The docker registry to pull the docker image from
     * @return true if the job executed successfully, false if any command failed
     */
    private boolean executeJob(Job job, PipelineExecution execution,
                               String dockerRegistry, String dockerImage) {
        long startTime = System.currentTimeMillis();

        execution.log("Starting job: " + job.getName() + " at " + startTime);
        String registry = job.getRegistry() == null ? dockerRegistry : job.getRegistry();
        String image = job.getImage() == null ? dockerImage : job.getImage();

        boolean jobSuccess = dockerContainerExecutor.executeJobInContainer(job, execution,
                registry, image);
        long endTime = System.currentTimeMillis();

        if (!jobSuccess) {
            execution.log("Job " + job.getName() + " failed at " + endTime +
                    " (took " + (endTime - startTime) + "ms)");

            // If the job allows failure, still upload artifacts
            if (job.isAllowFailure()) {
                execution.log("Job " + job.getName() + " allows failure, uploading artifacts.");
                uploadArtifacts(job, execution);
                return true;
            }
            return false;
        }

        execution.log("Job " + job.getName() + " completed successfully at " + endTime +
                " (took " + (endTime - startTime) + "ms)");

        // Upload Artifacts
        if (job.getArtifacts() != null && !job.getArtifacts().isEmpty()) {
            return uploadArtifacts(job, execution);
        }
        return true;
    }

    /**
     * Helper function to upload artifacts associated with a job to a MinIO bucket.
     * If the upload is successful, it logs the success message and returns true.
     * If an error occurs, it logs the failure message and returns false.

     * @param job        The job whose artifacts need to be uploaded.
     * @param execution  The pipeline execution instance for logging purposes.
     * @return           {@code true} if all artifacts are uploaded successfully,
     *                   {@code false} otherwise.
     */
    private boolean uploadArtifacts(Job job, PipelineExecution execution) {
        execution.log("Starting to upload artifacts for job: " + job.getName());
        try {
            String bucketName = "artifacts-" + System.currentTimeMillis() % 100000;
            execution.log("Created bucket " + bucketName
                    + " to upload artifacts for job: " + job.getName());
            this.uploader.uploadArtifacts(bucketName, job.getArtifacts());
            execution.log("Successfully uploaded artifacts for job: " + job.getName());
            return true;
        } catch (Exception e) {
            execution.log("Failed to upload artifacts for job " +
                    job.getName() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Initiates shutdown of the executor service.
     * <p>
     * The method first attempts to shut down the executor by calling {@code shutdown()} and waits
     * for up to 30 seconds for all tasks to finish. If the tasks do not complete within the
     * specified time, it forcefully shuts down the executor by calling {@code shutdownNow()}.
     * </p>
     * <p>
     * If the current thread is interrupted while waiting for tasks to terminate, the executor is
     * forcefully shut down, and the thread's interrupt status is preserved.
     * </p>
     */
    public void shutdownExecutor() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}