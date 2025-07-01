package com.example.cliserver.backend.database.mongoDB;

import com.example.cliserver.backend.model.PipelineRun;
import com.example.cliserver.backend.model.Status;
import com.example.cliserver.backend.utils.Constants;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.cliserver.backend.utils.Constants.*;


/**
 * Data Access Object (DAO) for interacting with the MongoDB collection of pipeline runs.
 * Provides methods to create and retrieve pipeline run records in the database.
 */
public class MongoDBPipelineRunsDao implements PipelineRunsDao {
    private static MongoCollection<Document> collection;

    private static class InstanceHolder {
        private static final PipelineRunsDao INSTANCE =
                new MongoDBPipelineRunsDao();
    }

    /**
     * Checks if the collection exists in the database.
     *
     * @param database  The MongoDB database.
     * @param collectionName The name of the collection to check.
     * @return true if the collection exists, false otherwise.
     */
    private static boolean collectionExists(MongoDatabase database, String collectionName) {
        for (String name : database.listCollectionNames()) {
            if (name.equals(collectionName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initializes the static collection field.
     *
     * @param mongoClient the MongoClient used to connect to the database
     * @param mongoConfig the MongoDB configuration containing database and collection details
     */
    private static void initializeCollection(
            MongoClient mongoClient,
            MongoDBConnector mongoConfig
    ) {
        MongoDatabase database = mongoClient.getDatabase(mongoConfig.getDatabase());

        // Check if the collection exists, and create it if not
        if (!collectionExists(database, mongoConfig.getCollection())) {
            database.createCollection(mongoConfig.getCollection());
            System.out.println("Collection created: " + mongoConfig.getCollection());
        }

        collection = database.getCollection(mongoConfig.getCollection());
    }

    public static PipelineRunsDao getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Constructs a PipelineRunsDao with the provided MongoClient and MongoDB configuration.
     *
     */
    private MongoDBPipelineRunsDao() {
        // Add constructor implementation that uses the config file path
        try {
            MongoDBConnector mongoConfig = MongoDBConnector.loadMongoConfig();
            MongoClient mongoClient = MongoClients.create(mongoConfig.getUri());
            initializeCollection(mongoClient, mongoConfig);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Retrieves the summary of the reports for a specific pipeline name.
     *
     * @param pipelineName The name of the pipeline to filter by.
     * @param repo   The url of the remote repo to filter by.
     * @param runNumber    The run number for which the user wants to generate reports for.
     *
     * @return An {@link AggregateIterable} containing documents with
     * the pipeline, stage, and job details.
     */
    public AggregateIterable<Document> getDefaultReportSummary(String pipelineName, String repo,
                                                               String runNumber) {
        List<Bson> pipeline = new ArrayList<>();

        if (repo != null && !repo.isEmpty()) {
            pipeline.add(
                    Aggregates.match(
                            Filters.eq(REPO_FIELD, repo)
                    )
            );
        }

        if (runNumber != null && !runNumber.isEmpty()) {
            pipeline.add(
                    Aggregates.match(
                            Filters.eq(RUN_NUMBER_FIELD, Integer.valueOf(runNumber))
                    )
            );
        }

        pipeline.addAll(Arrays.asList(
                Aggregates.match(Filters.eq(PIPELINE_NAME_FIELD, pipelineName)),
                Aggregates.unwind("$stages"),
                Aggregates.unwind("$stages.jobs"),
                Aggregates.project(Projections.fields(
                        Projections.include(PIPELINE_NAME_FIELD, RUN_NUMBER_FIELD,
                                COMMIT_FIELD, PIPELINE_STATUS_FIELD),
                        Projections.computed(STAGE_NAME_FIELD, "$stages.stageName"),
                        Projections.computed(STAGE_STATUS_FIELD, "$stages.stageStatus"),
                        Projections.computed(START_TIME_FIELD, "$stages.jobs.startTime"),
                        Projections.computed(COMPLETION_TIME_FIELD,
                                "$stages.jobs.completionTime")
                ))
        ));

        return collection.aggregate(pipeline);
    }

    /**
     * Retrieves the summary of the stages for a specific pipeline and stage name.
     *
     * @param pipelineName The name of the pipeline to filter by.
     * @param stageName    The name of the stage within the pipeline to filter by.
     * @param runNumber    The run number for which use wants to generate reports for.
     *
     * @return An {@link AggregateIterable} containing documents with
     * the pipeline, stage, and job details.
     *
     */
    @Override
    public AggregateIterable<Document> getStageSummary(
            String pipelineName, String stageName, String runNumber) {

        List<Bson> pipeline = new ArrayList<>();

        if (runNumber != null && !runNumber.isEmpty()) {
            pipeline.add(
                    Aggregates.match(
                            Filters.eq(RUN_NUMBER_FIELD, Integer.valueOf(runNumber))
                    )
            );
        }

        pipeline.addAll(Arrays.asList(
                Aggregates.match(Filters.eq(PIPELINE_NAME_FIELD, pipelineName)),
                Aggregates.unwind("$stages"),
                Aggregates.match(Filters.eq("stages.stageName", stageName)),
                Aggregates.unwind("$stages.jobs"),
                Aggregates.project(Projections.fields(
                        Projections.include(PIPELINE_NAME_FIELD, RUN_NUMBER_FIELD, COMMIT_FIELD),
                        Projections.computed(STAGE_NAME_FIELD, "$stages.stageName"),
                        Projections.computed(STAGE_STATUS_FIELD, "$stages.stageStatus"),
                        Projections.computed(JOB_NAME_FIELD, "$stages.jobs.jobName"),
                        Projections.computed(JOB_STATUS_FIELD, "$stages.jobs.jobStatus"),
                        Projections.computed(ALLOWS_FAILURE_FIELD, "$stages.jobs.allowsFailure"),
                        Projections.computed(START_TIME_FIELD, "$stages.jobs.startTime"),
                        Projections.computed(COMPLETION_TIME_FIELD, "$stages.jobs.completionTime")
                ))
        ));

        return collection.aggregate(pipeline);
    }

    /**
     * Retrieves the summary of the jobs for a specific pipeline and stage name.
     *
     * @param pipelineName The name of the pipeline to filter by.
     * @param stageName    The name of the stage within the pipeline to filter by.
     * @param jobName      The name of the job within the stage to filter by.
     * @param runNumber    The run number for which use wants to generate reports for.
     *
     * @return An {@link AggregateIterable} containing documents with
     * the pipeline, stage, and job details.
     *
     */
    @Override
    public AggregateIterable<Document> getJobSummary(
            String pipelineName, String stageName, String jobName, String runNumber) {

        List<Bson> pipeline = new ArrayList<>();

        if (runNumber != null) {
            pipeline.add(Aggregates.match(
                    Filters.eq(RUN_NUMBER_FIELD,
                            Integer.valueOf(runNumber))
            ));
        }

        pipeline.addAll(Arrays.asList(
                Aggregates.match(Filters.eq(PIPELINE_NAME_FIELD, pipelineName)),
                Aggregates.unwind("$stages"),
                Aggregates.match(Filters.eq("stages.stageName", stageName)),
                Aggregates.unwind("$stages.jobs"),
                Aggregates.match(Filters.eq("stages.jobs.jobName", jobName)),
                Aggregates.project(Projections.fields(
                        Projections.include(PIPELINE_NAME_FIELD, RUN_NUMBER_FIELD, COMMIT_FIELD),
                        Projections.computed(STAGE_NAME_FIELD, "$stages.stageName"),
                        Projections.computed(JOB_NAME_FIELD, "$stages.jobs.jobName"),
                        Projections.computed(JOB_STATUS_FIELD, "$stages.jobs.jobStatus"),
                        Projections.computed(ALLOWS_FAILURE_FIELD, "$stages.jobs.allowsFailure"),
                        Projections.computed(START_TIME_FIELD, "$stages.jobs.startTime"),
                        Projections.computed(COMPLETION_TIME_FIELD, "$stages.jobs.completionTime")
                ))
        ));

        return collection.aggregate(pipeline);
    }

    /**
     * Retrieves the most recent pipeline report document based on the pipeline name.
     * This method finds the most recent pipeline run by searching for the given
     * pipeline name and sorting by the run number in descending order.
     *
     * @param pipelineName the name of the pipeline to search for
     * @return the document representing the most recent pipeline run, or null if not found
     * @throws IllegalArgumentException if the pipelineName is null or empty
     */
    @Override
    public Document getByPipelineName(String pipelineName) {
        if (pipelineName == null || pipelineName.trim().isEmpty()) {
            throw new IllegalArgumentException("pipelineName cannot be null or empty");
        }

        return collection.find(
                        Filters.eq(PIPELINE_NAME_FIELD, pipelineName))
                .sort(Sorts.descending(RUN_NUMBER_FIELD)).first();
    }

    /**
     * Retrieves local pipeline run reports for a specific pipeline.
     *
     * @param pipelineName The name of the pipeline to filter by
     * @return FindIterable of documents containing local pipeline run reports
     */
    @Override
    public FindIterable<Document> getLocalPipelineRunReports(String pipelineName) {
        return getRepoPipelineRunReports(Constants.LOCAL_REPO, pipelineName);
    }

    /**
     * Retrieves pipeline run reports for a specific repository and pipeline.
     *
     * @param repo The repository identifier to filter by
     * @param pipelineName The name of the pipeline to filter by
     * @return FindIterable of documents containing matching pipeline run reports
     */
    @Override
    public FindIterable<Document> getRepoPipelineRunReports(String repo, String pipelineName) {
        if (pipelineName.isEmpty()) {
            return collection.find(Filters.eq(REPO_FIELD, repo));
        } else {
            return collection.find(
                    Filters.and(
                            Filters.eq(REPO_FIELD, repo),
                            Filters.eq(PIPELINE_NAME_FIELD, pipelineName)));
        }
    }

    /**
     * Retrieves the next run number for the given pipeline name and repo.
     * This method finds the most recent pipeline run for the given pipeline and
     * increments the run number by 1 to generate the next run number.
     * If no previous run exists, the run number starts at 1.
     *
     * @param pipelineName the name of the pipeline to retrieve the run number for
     * @param repo         the name of the repo to retrieve the run number for
     * @return the next run number for the pipeline
     */
    @Override
    public int getRunNumber(String pipelineName, String repo) {
        Document lastRun = collection.find(
                        Filters.and(
                                Filters.eq(REPO_FIELD, repo),
                                Filters.eq(PIPELINE_NAME_FIELD, pipelineName)))
                .sort(Sorts.descending(RUN_NUMBER_FIELD)).first();
        // Start with 1 if no previous run exists
        return (lastRun != null) ? lastRun.getInteger(RUN_NUMBER_FIELD) + 1 : 1;
    }

    /**
     * Retrieves pipeline runs as per the criteria, sorted by completion time.
     *
     * @param repo The repository identifier to filter pipeline runs
     * @param pipelineName The name of the pipeline to filter, or null for all pipelines
     * @param runNumber The specific run number to find, or null for all runs
     * @return A FindIterable containing matching pipeline runs, sorted by completion time
     *         (most recent first)
     */
    @Override
    public FindIterable<Document> getTimeFilteredPipelineRunReports(
            String repo,
            String pipelineName,
            Integer runNumber
    ) {
        // Create filter to find runs with status RUNNING
        Document filter = new Document(Constants.REPO_FIELD, repo);

        if (pipelineName != null && !pipelineName.isEmpty()) {
            filter.append(Constants.PIPELINE_NAME_FIELD, pipelineName);
        }

        if (runNumber != null) {
            filter.append(Constants.RUN_NUMBER_FIELD, runNumber);
        }

        return collection.find(filter).sort(Sorts.descending(COMPLETION_TIME_FIELD));
    }

    /**
     * Finds actively executing pipeline runs based on the provided criteria.
     *
     * @param repo The repository identifier to filter pipeline runs
     * @param pipelineName The name of the pipeline to filter, or null for all pipelines
     * @param runNumber The specific run number to find, or null for all runs
     * @return A FindIterable containing matching pipeline runs
     *
     */
    @Override
    public FindIterable<Document> findActiveRuns(String repo, String pipelineName,
                                                 Integer runNumber) {
        // Create filter to find runs with status RUNNING
        Document filter = new Document(Constants.PIPELINE_STATUS_FIELD, Status.RUNNING.toString());

        if (pipelineName != null && !pipelineName.isEmpty()) {
            filter.append(Constants.PIPELINE_NAME_FIELD, pipelineName);
        }

        if (repo != null) {
            filter.append(Constants.REPO_FIELD, repo);
        }

        if (runNumber != null) {
            filter.append(Constants.RUN_NUMBER_FIELD, runNumber);
        }

        return collection.find(filter);
    }

    /**
     * Prints the fields of a MongoDB {@link Document} in a human-readable format.
     *
     * @param doc the {@link Document} object to be printed.
     */
    @Override
    public void prettyPrintDocument(Document doc) {
        for (Map.Entry<String, Object> entry : doc.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            System.out.println(key + ": " + value);
        }
    }

    /**
     * Deletes all pipeline run records from the collection.
     * <p>
     * This method removes all documents from the underlying MongoDB collection. It does not
     * apply any filter, so it will delete every document within the collection. Use this method
     * with caution, as it will result in the loss of all stored pipeline run data.
     * </p>
     * @return the number of deleted documents
     */
    @Override
    public long deleteAllPipelineRuns() {
        // Delete all documents in the collection
        return collection.deleteMany(new Document()).getDeletedCount();
    }

    /**
     * Updates an existing pipeline run in the MongoDB collection.
     * This method updates an existing document in the collection with new data
     * from the provided PipelineRun object.
     *
     * @param pipelineRun the pipeline run object with updated data
     */
    @Override
    public void updatePipelineRun(PipelineRun pipelineRun) {
        try {

            Document filter = new Document()
                    .append(PIPELINE_NAME_FIELD, pipelineRun.getPipelineName())
                    .append(RUN_NUMBER_FIELD, pipelineRun.getRunNumber())
                    .append(REPO_FIELD, pipelineRun.getRepo());

            Document pipelineRunDocument = new Document()
                    .append(REPO_FIELD, pipelineRun.getRepo())
                    .append(FILE_NAME_FIELD, pipelineRun.getFileName())
                    .append(BRANCH_FIELD, pipelineRun.getBranch())
                    .append(COMMIT_FIELD, pipelineRun.getCommit())
                    .append(PIPELINE_NAME_FIELD, pipelineRun.getPipelineName())
                    .append(RUN_NUMBER_FIELD, pipelineRun.getRunNumber())
                    .append(START_TIME_FIELD, pipelineRun.getStartTime())
                    .append(COMPLETION_TIME_FIELD, pipelineRun.getCompletionTime())
                    .append(PIPELINE_STATUS_FIELD, pipelineRun.getPipelineStatus().toString())
                    .append(IS_LOCAL_FIELD, pipelineRun.isLocal());

            // Add stages to the document
            List<Document> stagesDocuments = pipelineRun.getStages().stream().map(stage -> {
                List<Document> jobsDocuments = stage.getJobs().stream().map(job ->
                        new Document(JOB_NAME_FIELD, job.getName())
                                .append(JOB_STATUS_FIELD, job.getJobStatus().toString())
                                .append(ALLOWS_FAILURE_FIELD, job.isAllowFailure())
                                .append(START_TIME_FIELD, job.getStartTime())
                                .append(COMPLETION_TIME_FIELD, job.getCompletionTime())
                ).collect(Collectors.toList());

                return new Document(STAGE_NAME_FIELD, stage.getStageName())
                        .append(STAGE_STATUS_FIELD, stage.getStageStatus().toString())
                        .append(START_TIME_FIELD, stage.getStartTime())
                        .append(COMPLETION_TIME_FIELD, stage.getCompletionTime())
                        .append(JOBS_FIELD, jobsDocuments);
            }).collect(Collectors.toList());

            pipelineRunDocument.append(STAGES_FIELD, stagesDocuments);

            // Replace the entire document
            collection.replaceOne(filter, pipelineRunDocument, new ReplaceOptions().upsert(true));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    @Override
    public void close() throws Exception {
        // Close any resources if needed, e.g., MongoClient
        // If your MongoClient is maintained as an instance variable
    }
}