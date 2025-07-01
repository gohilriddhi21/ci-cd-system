package com.example.cliserver.backend.commands.runCommand;

import com.example.cliserver.backend.database.mongoDB.PipelineRunsDao;
import com.example.cliserver.backend.messaging.PipelinePublisher;
import com.example.cliserver.backend.model.*;
import com.example.cliserver.backend.utils.ConfigurationValidator;
import com.example.cliserver.backend.utils.Constants;
import com.example.cliserver.backend.utils.PipelineUtils;

import java.util.Date;
import java.util.List;

/**
 * Command to execute pipeline configurations.
 * Validates, initializes, and runs pipelines either synchronously or asynchronously.
 */
public class RunCommand {

    public final PipelineRunsDao pipelineRunsDao;

    /**
     * Constructs a new RunCommand with the specified DAO.
     *
     * @param pipelineRunsDao The DAO for accessing and updating pipeline run data
     */
    public RunCommand(PipelineRunsDao pipelineRunsDao) {
        this.pipelineRunsDao = pipelineRunsDao;
    }
    /**
     * Executes a pipeline configuration file.
     * Validates the pipeline, initializes run reporting, and executes either
     * synchronously (when verbose logging is enabled) or asynchronously through messaging.
     *
     * @param fileName The pipeline configuration file to run
     * @param repo The repository identifier for the pipeline run
     * @param branch The branch to check out for the given repo
     * @param commit The commit hash to check out for the given repo
     * @param useVerboseLogging When true, runs synchronously with verbose logging
     * @return A result message or run number for tracking execution
     */
    public String run(String fileName, String repo, String branch, String commit,
                      boolean useVerboseLogging) {
        PipelineRun pipelineRunReport = PipelineUtils.initializePipelineRunReport(repo);
        ConfigurationValidator configurationValidator = new ConfigurationValidator();
        // Validate the pipeline configuration
        ValidationResult validationResult = configurationValidator.validateYaml(fileName, repo,
                branch, commit);
        if (!validationResult.isValid()) {
            pipelineRunReport.setPipelineStatus(Status.FAILED);
            updatePipelineRunReport(pipelineRunReport, new Date());
            return validationResult.getErrorMessage();
        }

        PipelineConfig config = validationResult.getConfig();
        if (config == null) {
            pipelineRunReport.setPipelineStatus(Status.FAILED);
            updatePipelineRunReport(pipelineRunReport, new Date());
            return "Failed to load pipeline configuration.";
        }

        pipelineRunReport.setFileName(fileName);

        initialisePipelineRunReport(pipelineRunReport, config);

        if(useVerboseLogging) {
            PipelineRunner pipelineRunner = new PipelineRunner(this.pipelineRunsDao);
            // To run pipeline and see logs - run it here itself
            String result = pipelineRunner.runPipeline(pipelineRunReport, true);
            pipelineRunner.shutdownExecutor();
            return result;
        }
        PipelinePublisher publisher = new PipelinePublisher(this.pipelineRunsDao);
        return publisher.publishPipelineRun(pipelineRunReport);
    }

    /**
     * Initializes pipeline run reporting with configuration details.
     * Sets up run number, pipeline metadata, and creates pending stages.
     *
     * @param pipelineRunReport The pipeline run report to initialize
     * @param config The validated pipeline configuration
     */
    public void initialisePipelineRunReport(PipelineRun pipelineRunReport, PipelineConfig config) {
        pipelineRunReport.setPipelineName(config.getPipeline().getName());

        String pipelineName = config.getPipeline().getName();
        int runNumber = this.pipelineRunsDao.getRunNumber(
                pipelineName,
                pipelineRunReport.getRepo() != null ?
                    pipelineRunReport.getRepo() : Constants.LOCAL_REPO
        );
        pipelineRunReport.setRunNumber(runNumber);
        pipelineRunReport.setRegistry(config.getPipeline().getRegistry());
        pipelineRunReport.setUploadRepo(config.getPipeline().getUploadRepo());
        pipelineRunReport.setImage(config.getPipeline().getImage());
        pipelineRunReport.setPipelineStatus(Status.PENDING);

        // Initialize all stages with Pending status before execution starts
        List<Stage> pendingStages = PipelineUtils.markAllStagesPending(
                config.getPipeline().getStages(),
                config.getPipeline().getJobs()
        );

        // Set the stages with Pending status
        pipelineRunReport.setStages(pendingStages);

        // Create initial record in a database
        updatePipelineRunReport(pipelineRunReport);
    }

    /**
     * Creates a new pipeline run report if it doesn't exist, otherwise updates it.
     *
     * @param pipelineRun the pipeline run to create or update
     */
    private void updatePipelineRunReport(PipelineRun pipelineRun) {
        this.pipelineRunsDao.updatePipelineRun(pipelineRun);
    }

    /**
     * Creates a new pipeline run report if it doesn't exist, otherwise updates it,
     * and sets its completion time.
     *
     * @param pipelineRun the pipeline run to create or update
     * @param endTime if not null, will be set as the completion time for the pipeline run
     */
    private void updatePipelineRunReport(PipelineRun pipelineRun, Date endTime) {
        pipelineRun.setCompletionTime(endTime.getTime());
        this.pipelineRunsDao.updatePipelineRun(pipelineRun);
    }
}