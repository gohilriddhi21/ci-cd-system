package com.example.cliserver.backend.commands;

import com.example.cliserver.backend.database.mongoDB.PipelineRunsDao;
import com.example.cliserver.backend.model.PipelineRun;
import com.example.cliserver.backend.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.json.JSONArray;
import org.json.JSONObject;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;

import static com.example.cliserver.backend.utils.Constants.JSON_DATA_KEY_JOB;
import static com.example.cliserver.backend.utils.Constants.JSON_DATA_KEY_DEFAULT;
import static com.example.cliserver.backend.utils.Constants.JSON_DATA_KEY_STAGE;

public class ReportCommand {

    private final PipelineRunsDao pipelineRunsDao;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public ReportCommand(PipelineRunsDao pipelineRunsDao) {
        this.pipelineRunsDao = pipelineRunsDao;
    }

    /**
     * Recursively processes a JSON object to replace zero or missing timestamp values
     * with "N/A". This handles the nested structure of PipelineRun objects with stages and jobs.
     *
     * @param jsonObject The JSON object to process
     * @return The processed JSON object with formatted timestamps
     */
    public JSONObject processTimestampsRecursively(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }

        processTimestampField(jsonObject, "startTime");
        processTimestampField(jsonObject, "completionTime");

        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                processTimestampsRecursively((JSONObject) value);
            }
            else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                for (int i = 0; i < array.length(); i++) {
                    Object item = array.get(i);
                    if (item instanceof JSONObject) {
                        processTimestampsRecursively((JSONObject) item);
                    }
                }
            }
        }

        return jsonObject;
    }

    /**
     * Helper method to process a specific timestamp field in a JSON object.
     *
     * @param jsonObject The JSON object containing the field
     * @param fieldName The name of the timestamp field to process
     */
    private void processTimestampField(JSONObject jsonObject, String fieldName) {
        if (jsonObject.has(fieldName)) {
            Object timeValue = jsonObject.get(fieldName);

            if (timeValue == null ||
                (timeValue instanceof Number && ((Number)timeValue).longValue() <= 0) ||
                (timeValue instanceof String && (((String)timeValue).isEmpty()
                    || "0".equals(timeValue)))) {

                jsonObject.put(fieldName, "N/A");
            }
        }
    }

    public JSONObject generateReport(
        String pipelineName,
        String stageName,
        String jobName,
        String runNumber,
        String repo,
        String format
    ) {
        JSONObject jsonObject;
        if (stageName != null) {
            if (jobName != null) {
                jsonObject = generateJobReport(pipelineName, stageName, jobName, runNumber);
            } else {
                jsonObject = generateStageReport(pipelineName, stageName, runNumber);
            }
        } else {
            jsonObject = generateDefaultReport(pipelineName, repo, runNumber);
        }

        if (format.equalsIgnoreCase(Constants.JSON_FORMAT)) {
            return processTimestampsRecursively(jsonObject);
        }
        return jsonObject;
    }

    private JSONObject generateJobReport(String pipelineName,
                                         String stageName, String jobName, String runNumber) {
        AggregateIterable<Document> reports = pipelineRunsDao.getJobSummary(
                pipelineName, stageName, jobName, runNumber);
        return buildReportResponse(JSON_DATA_KEY_JOB, reports);
    }

    private JSONObject generateStageReport(String pipelineName, 
                                           String stageName, String runNumber) {
        AggregateIterable<Document> reports = pipelineRunsDao.getStageSummary(
                pipelineName, stageName, runNumber);
        return buildReportResponse(JSON_DATA_KEY_STAGE, reports);
    }

    private JSONObject generateDefaultReport(String pipelineName, String repo, String runNumber) {
        AggregateIterable<Document> reports = pipelineRunsDao.getDefaultReportSummary(
                pipelineName, repo, runNumber);
        return buildReportResponse(JSON_DATA_KEY_DEFAULT, reports);
    }

    private JSONObject buildReportResponse(String reportTitle,
                                           AggregateIterable<Document> reports) {
        JSONObject result = new JSONObject();
        JSONArray reportsArray = new JSONArray();
        if (reports != null) {
            for (Document document : reports) {
                reportsArray.put(new JSONObject(document.toJson()));
            }
        }
        result.put(reportTitle, reportsArray);
        return result;
    }
}
