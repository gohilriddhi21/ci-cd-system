package com.example.cliserver.backend.database.mongoDB;

import com.example.cliserver.backend.model.PipelineRun;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import org.bson.Document;

/**
 * Interface for accessing and manipulating pipeline run data in the database.
 */
public interface PipelineRunsDao {

    /**
     * Retrieves the summary of the stages for a specific pipeline and stage name.
     *
     * @param pipelineName The name of the pipeline to filter by
     * @param stageName    The name of the stage within the pipeline to filter by
     * @param runNumber    The run number for which to generate reports for, or null for all runs
     * @return An {@link AggregateIterable} containing documents with the pipeline, stage, and
     * job details
     */
    AggregateIterable<Document> getStageSummary(
            String pipelineName, String stageName, String runNumber
    );

    /**
     * Retrieves the summary of the jobs for a specific pipeline, stage, and job name.
     *
     * @param pipelineName The name of the pipeline to filter by
     * @param stageName    The name of the stage within the pipeline to filter by
     * @param jobName      The name of the job within the stage to filter by
     * @param runNumber    The run number for which to generate reports for, or null for all runs
     * @return An {@link AggregateIterable} containing documents with the pipeline, stage, and
     * job details
     */
    AggregateIterable<Document> getJobSummary(
            String pipelineName, String stageName, String jobName, String runNumber
    );

    /**
     * Retrieves the most recent pipeline report document based on the pipeline name.
     * This method finds the most recent pipeline run by searching for the given
     * pipeline name and sorting by the run number in descending order.
     *
     * @param pipelineName The name of the pipeline to search for
     * @return The document representing the most recent pipeline run, or null if not found
     * @throws IllegalArgumentException If the pipelineName is null or empty
     */
    Document getByPipelineName(String pipelineName);

    /**
     * Retrieves local pipeline run reports for a specific pipeline.
     *
     * @param pipelineName The name of the pipeline to filter by
     * @return FindIterable of documents containing local pipeline run reports
     */
    FindIterable<Document> getLocalPipelineRunReports(String pipelineName);

    /**
     * Retrieves pipeline run reports for a specific repository and pipeline.
     *
     * @param repo         The repository identifier to filter by
     * @param pipelineName The name of the pipeline to filter by
     * @return FindIterable of documents containing matching pipeline run reports
     */
    FindIterable<Document> getRepoPipelineRunReports(String repo, String pipelineName);

    /**
     * Retrieves the next run number for the given pipeline name and repo.
     * This method finds the most recent pipeline run for the given pipeline and
     * increments the run number by 1 to generate the next run number.
     * If no previous run exists, the run number starts at 1.
     *
     * @param pipelineName The name of the pipeline to retrieve the run number for
     * @param repo         The name of the repo to retrieve the run number for
     * @return The next run number for the pipeline
     */
    int getRunNumber(String pipelineName, String repo);

    /**
     * Retrieves pipeline runs as per the criteria, sorted by completion time.
     *
     * @param repo         The repository identifier to filter pipeline runs
     * @param pipelineName The name of the pipeline to filter, or null for all pipelines
     * @param runNumber    The specific run number to find, or null for all runs
     * @return A FindIterable containing matching pipeline runs, sorted by completion time
     * (most recent first)
     */
    FindIterable<Document> getTimeFilteredPipelineRunReports(
            String repo, String pipelineName, Integer runNumber
    );

    /**
     * Finds actively executing pipeline runs based on the provided criteria.
     *
     * @param repo         The repository identifier to filter pipeline runs
     * @param pipelineName The name of the pipeline to filter, or null for all pipelines
     * @param runNumber    The specific run number to find, or null for all runs
     * @return A FindIterable containing matching active pipeline runs
     */
    FindIterable<Document> findActiveRuns(String repo, String pipelineName, Integer runNumber);

    /**
     * Prints the fields of a MongoDB Document in a human-readable format.
     *
     * @param doc The {@link Document} object to be printed
     */
    void prettyPrintDocument(Document doc);

    /**
     * Deletes all pipeline run records from the collection.
     * This method removes all documents from the underlying database collection.
     * It does not apply any filter, so it will delete every document within the collection.
     * Use this method with caution, as it will result in the loss of all stored pipeline run data.
     *
     * @return The number of deleted documents
     */
    long deleteAllPipelineRuns();

    /**
     * Retrieves the summary of the reports for a specific pipeline name.
     *
     * @param pipelineName The name of the pipeline to filter by
     * @param repo   The url of the remote repo to filter by.
     * @param runNumber    The run number for which to generate reports for, or null for all runs
     * @return An {@link AggregateIterable} containing documents with the pipeline, stage, and
     * job details
     */
    AggregateIterable<Document> getDefaultReportSummary(
            String pipelineName, String repo, String runNumber);


    /**
     * Updates an existing pipeline run in the database collection.
     * This method updates an existing record with new data from the provided PipelineRun object.
     *
     * @param pipelineRun The pipeline run object with updated data
     */
    void updatePipelineRun(PipelineRun pipelineRun);

    /**
     * Closes any resources associated with this DAO.
     * This method should be called when the DAO is no longer needed to release
     * any database connections or other resources.
     *
     * @throws Exception If an error occurs while closing resources
     */
    void close() throws Exception;
}