package com.example.cliapplication.reportGeneration.ReportFormats;

import com.example.cliapplication.reportGeneration.ReportGenerator;
import de.vandermeer.asciitable.AsciiTable;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import static com.example.cliapplication.utils.Constants.*;

/**
 * This class is responsible for generating a report of stage summaries.
 * <p>
 * This class extends ReportGenerator and overrides the
 * generateReport method to process and displays the stage information
 * in a tabular format.
 */
public class StageReportGenerator extends ReportGenerator {

    /**
     * Constructs a StageReportGenerator with the specified output format.
     *
     * @param format the output format for the generated report
     */
    public StageReportGenerator(String format) {
        super(format);
    }

    /**
     * Generates a report of stage summaries and populates the provided AsciiTable
     * with the data from a JSONObject. It expects the JSONObject to contain
     * a JSONArray where each element represents a stage summary.
     * <p>
     * The report includes details like pipeline name, run number,
     * commit hash, stage name, job details, and job status.
     *
     * @param reportData The JSONObject containing the report data, expected to
     * have a JSONArray of stage summaries.
     * @param asciiTable The AsciiTable to populate with the report data.
     */
    @Override
    public void generateReport(JSONObject reportData, AsciiTable asciiTable) {
        if (reportData == null || reportData.length() == 0) {
            System.out.println("No data found in the provided JSONObject.");
            return;
        }
        JSONArray  stageSummaryList = reportData.getJSONArray(JSON_DATA_KEY_STAGE);

        if(stageSummaryList.length() > 0) {

            asciiTable.addRule();
            asciiTable.addRow(
                    "PipelineName",
                    "RunNo",
                    "Commit",
                    "StageName",
                    "StageStatus",
                    "JobName",
                    "JobStatus",
                    "AllowsFailure",
                    "JobStartTime",
                    "JobEndTime");
            asciiTable.addRule();

            for (int i = 0; i < stageSummaryList.length(); i++) {
                try {
                    JSONObject stageSummary = stageSummaryList.getJSONObject(i);

                    String startTimeStr = formatTimestamp(
                            stageSummary.optLong(START_TIME_FIELD, -1));
                    String completionTimeStr = formatTimestamp(
                            stageSummary.optLong(COMPLETION_TIME_FIELD, -1));

                    asciiTable.addRow(
                            stageSummary.optString(PIPELINE_NAME_FIELD, " "),
                            stageSummary.optInt(RUN_NUMBER_FIELD, -1),
                            stageSummary.optString(COMMIT_FIELD, "None"),
                            stageSummary.optString(STAGE_NAME_FIELD, " "),
                            stageSummary.optString(STAGE_STATUS_FIELD, "UNKNOWN"),
                            stageSummary.optString(JOB_NAME_FIELD, " "),
                            stageSummary.optString(JOB_STATUS_FIELD, "UNKNOWN"),
                            stageSummary.optBoolean(ALLOWS_FAILURE_FIELD, false) ? "Yes" : "No",
                            startTimeStr,
                            completionTimeStr
                    );
                    asciiTable.addRule();

                } catch (JSONException e) {
                    System.err.println("Error processing stage summary at index " +
                            i + ": " + e.getMessage());
                }
            }
        }
    }
}