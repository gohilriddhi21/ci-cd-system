package com.example.cliserver.backend.commands;

import com.example.cliserver.backend.database.mongoDB.PipelineRunsDao;
import com.example.cliserver.backend.model.PipelineConfig;
import com.example.cliserver.backend.utils.ConfigurationValidator;
import com.example.cliserver.backend.utils.Constants;
import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to retrieve and format pipeline execution status information.
 * Retrieves pipeline run data from MongoDB and returns it as a formatted string.
 */
public class StatusCommand {

    public final PipelineRunsDao pipelineRunsDao;

    public StatusCommand(PipelineRunsDao pipelineRunsDao) {
        this.pipelineRunsDao = pipelineRunsDao;
    }

    /**
     * Retrieves the status of pipeline runs for a given repository and pipeline.
     *
     * @param repo The repository identifier to filter pipeline runs
     * @param branch The branch to checkout for the given repo
     * @param commit The commit hash to checkout for the given repo
     * @param filePath The name of the pipeline to filter, or null for all pipelines
     * @param runNumber The specific run number to display, or null for all runs
     * @return A formatted string representing the pipeline run status
     */
    public String printPipelineStatus(String repo, String branch, String commit, String filePath,
                                      Integer runNumber) {
        String pipelineName = null;
        ConfigurationValidator validator = new ConfigurationValidator();
        if(!filePath.isEmpty()) {
            PipelineConfig config = validator.validateYaml(filePath, repo, branch, commit)
                    .getConfig();
            if (config != null) {
                pipelineName = config.getPipeline().getName();
            }
        }

        // Set repo to local if null
        repo = repo != null ? repo : Constants.LOCAL_REPO;

        // First case: Check for actively executing runs
        FindIterable<Document> activeRuns = this.pipelineRunsDao.findActiveRuns(
                repo,
                pipelineName,
                runNumber
        );
        List<Document> activeRunsList = new ArrayList<>();
        activeRuns.into(activeRunsList);

        if (!activeRunsList.isEmpty()) {
            // We have active runs, format them
            StringBuilder output = new StringBuilder();
            for (Document run : activeRunsList) {
                output.append(formatRunStatus(run)).append("\n\n");
            }

            return output.toString().trim();
        }

        // Second case: No actively executing runs, get most recent completed run
        Document pipelineRun = this.pipelineRunsDao.getTimeFilteredPipelineRunReports(
                repo,
                pipelineName,
                runNumber
        ).first();

        if (pipelineRun == null) {
            return "No pipeline runs found.";
        }

        return formatRunStatus(pipelineRun);
    }

    /**
     * Formats the status details of a single pipeline run document.
     * Formats stages and jobs in a YAML-like structure.
     *
     * @param doc The MongoDB document containing pipeline run information
     * @return A formatted string representing the pipeline run status
     */
    private String formatRunStatus(Document doc) {
        // Format stages and jobs in YAML format
        List<Document> stages = (List<Document>) doc.get(Constants.STAGES_FIELD);
        if (stages == null) return "No stages found in pipeline run.";

        StringBuilder output = new StringBuilder();
        output.append("Pipeline: ").append(doc.getString(Constants.PIPELINE_NAME_FIELD))
            .append("\n").append("Status: ").append(doc.getString(Constants.PIPELINE_STATUS_FIELD))
            .append("\n").append("Run Number: ").append(doc.getInteger(Constants.RUN_NUMBER_FIELD))
            .append("\n\n");

        for (Document stage : stages) {
            output.append(stage.getString(Constants.STAGE_NAME_FIELD)).append(":\n")
                    .append("    status: ").append(stage.getString(Constants.STAGE_STATUS_FIELD))
                    .append("\n");

            List<Document> jobs = (List<Document>) stage.get(Constants.JOBS_FIELD);
            for (Document job : jobs) {
                output.append("    ").append(job.getString(Constants.JOB_NAME_FIELD)).append(":\n")
                        .append("        status: ")
                    .append(job.getString(Constants.JOB_STATUS_FIELD)).append("\n");
            }
        }

        return output.toString().trim();
    }
}