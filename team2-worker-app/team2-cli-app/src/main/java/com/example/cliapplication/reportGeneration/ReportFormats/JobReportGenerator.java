package com.example.cliapplication.reportGeneration.ReportFormats;

import com.example.cliapplication.reportGeneration.ReportGenerator;
import de.vandermeer.asciitable.AsciiTable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import static com.example.cliapplication.utils.Constants.*;

/**
 * This class is responsible for generating a report
 * of job details related to a pipeline. It formats the job data fetched
 * from a JSONObject into an ASCII table.
 * <p>
 * This class extends ReportGenerator and overrides the
 * generateReport method to process and displays the job information
 * in a tabular format.
 */
public class JobReportGenerator extends ReportGenerator {

    /**
     * Constructs a JobReportGenerator with the specified output format.
     *
     * @param format the output format for the generated report
     */
    public JobReportGenerator(String format) {
        super(format);
    }

    /**
     * Generates a report of job details in the form of an ASCII table
     * from a provided JSONObject. It expects the JSONObject to contain
     * a JSONArray where each element represents job data.
     * <p>
     * This method iterates over the job data, and adds the relevant job
     * details to the asciiTable such as pipeline name, run number,
     * stage name, job name, job status, failure allowance, and start/completion times.
     *
     * @param reportData The JSONObject containing the report data, expected to
     * have a JSONArray of job details.
     * @param asciiTable The AsciiTable object where the
     * report data will be formatted and added.
     */
    @Override
    public void generateReport(JSONObject reportData, AsciiTable asciiTable) {
        if (reportData == null || reportData.length() == 0) {
            System.out.println("No job data found in the provided JSONObject.");
            return;
        }

        JSONArray jobDataList = reportData.getJSONArray(JSON_DATA_KEY_JOB);

        if(jobDataList.length() > 0) {

            asciiTable.addRule();
            asciiTable.addRow(
                    "PipelineName",
                    "RunNo",
                    "Commit",
                    "StageName",
                    "JobName",
                    "JobStatus",
                    "AllowsFailure",
                    "JobStartTime",
                    "JobEndTime");
            asciiTable.addRule();

            for (int i = 0; i < jobDataList.length(); i++) {
                try {
                    JSONObject jobData = jobDataList.getJSONObject(i);

                    String startTimeStr = formatTimestamp(
                            jobData.optLong(START_TIME_FIELD, -1));
                    String completionTimeStr = formatTimestamp(
                            jobData.optLong(COMPLETION_TIME_FIELD, -1));

                    asciiTable.addRow(
                            jobData.optString(PIPELINE_NAME_FIELD, " "),
                            jobData.optInt(RUN_NUMBER_FIELD, -1),
                            jobData.optString(COMMIT_FIELD, "None"),
                            jobData.optString(STAGE_NAME_FIELD, " "),
                            jobData.optString(JOB_NAME_FIELD, " "),
                            jobData.optString(JOB_STATUS_FIELD, "UNKNOWN"),
                            jobData.optBoolean(ALLOWS_FAILURE_FIELD,
                                    false) ? "Yes" : "No",
                            startTimeStr,
                            completionTimeStr
                    );
                    asciiTable.addRule();

                } catch (JSONException e) {
                    System.err.println("Error processing job data at index " +
                            i + ": " + e.getMessage());
                }
            }
        }
    }
}