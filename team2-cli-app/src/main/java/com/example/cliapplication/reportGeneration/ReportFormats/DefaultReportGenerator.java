package com.example.cliapplication.reportGeneration.ReportFormats;

import com.example.cliapplication.reportGeneration.ReportGenerator;
import com.example.cliapplication.utils.Constants;
import de.vandermeer.asciitable.AsciiTable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

import static com.example.cliapplication.utils.Constants.*;

/**
 * Report generator for a default report type when no stage or job is specified.
 */
public class DefaultReportGenerator extends ReportGenerator {

    /**
     * Constructor to set the format of the default report.
     *
     * @param format The format in which to display the report (Table or JSON).
     */
    public DefaultReportGenerator(String format) {
        super(format);
    }

    /**
     * Generates the default stage report (used when no stage or job options are provided).
     *
     * @param data The JSON data to be displayed in the table.
     * @param asciiTable The AsciiTable object used to format the table.
     */
    @Override
    public void generateReport(JSONObject data, AsciiTable asciiTable) {
        JSONArray reportArray = data.optJSONArray(DEFAULT_JSON_KEY);
        if (reportArray == null || reportArray.length() == 0) {
            asciiTable.addRule();
            asciiTable.addRow("No default report data found.");
            asciiTable.addRule();
            return;
        }

        if(reportArray.length() > 0) {
            asciiTable.addRule();

            asciiTable.addRow(
                    "PipelineName",
                    "RunNo",
                    "Commit",
                    "PipelineStatus",
                    "StageName",
                    "StageStatus",
                    "JobStartTime",
                    "JobEndTime");
            asciiTable.addRule();

            for (int i = 0; i < reportArray.length(); i++) {
                JSONObject jobData = reportArray.getJSONObject(i);

                String startTimeStr = formatTimestamp(
                        jobData.optLong(START_TIME_FIELD, -1));
                String completionTimeStr = formatTimestamp(
                        jobData.optLong(COMPLETION_TIME_FIELD, -1));

                asciiTable.addRow(
                        jobData.optString(PIPELINE_NAME_FIELD, " "),
                        jobData.optInt(RUN_NUMBER_FIELD, -1),
                        jobData.optString(COMMIT_FIELD, "None"),
                        jobData.optString(Constants.PIPELINE_STATUS_FIELD, "UNKNOWN"),
                        jobData.optString(STAGE_NAME_FIELD, " "),
                        jobData.optString(STAGE_STATUS_FIELD, "UNKNOWN"),
                        startTimeStr,
                        completionTimeStr
                );
                asciiTable.addRule();
            }
        }
    }
}
