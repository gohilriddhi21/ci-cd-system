package com.example.cliapplication.reportGeneration.ReportFormats;

import de.vandermeer.asciitable.AsciiTable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.cliapplication.utils.Constants.*;
import static org.mockito.Mockito.*;

class JobReportGeneratorTest {
    private JobReportGenerator generator;
    private AsciiTable mockTable;

    @BeforeEach
    void setUp() {
        generator = new JobReportGenerator("TABLE");
        mockTable = mock(AsciiTable.class);
    }

    @Test
    void testGenerateReport_nullData_shouldPrintMessage() {
        generator.generateReport(null, mockTable);

        verifyNoInteractions(mockTable);
    }

    @Test
    void testGenerateReport_emptyJson_shouldPrintMessage() {
        generator.generateReport(new JSONObject(), mockTable);

        verifyNoInteractions(mockTable);
    }

    @Test
    void testGenerateReport_missingJobKey_shouldPrintError() {
        JSONObject data = new JSONObject();  // no "jobData" key

        generator.generateReport(data, mockTable);

        verifyNoInteractions(mockTable);
    }

    @Test
    void testGenerateReport_withValidJobData_shouldAddRows() throws JSONException {
        JSONObject job = new JSONObject();
        job.put(PIPELINE_NAME_FIELD, "PipelineX");
        job.put(RUN_NUMBER_FIELD, 42);
        job.put(COMMIT_FIELD, "commit-sha123");
        job.put(STAGE_NAME_FIELD, "Deploy");
        job.put(JOB_NAME_FIELD, "Job1");
        job.put(JOB_STATUS_FIELD, "SUCCESS");
        job.put(ALLOWS_FAILURE_FIELD, true);

        long now = System.currentTimeMillis();
        job.put(START_TIME_FIELD, now - 5000);
        job.put(COMPLETION_TIME_FIELD, now);

        JSONArray jobArray = new JSONArray();
        jobArray.put(job);

        JSONObject reportData = new JSONObject();
        reportData.put(JSON_DATA_KEY_JOB, jobArray);

        generator.generateReport(reportData, mockTable);

        verify(mockTable, atLeast(2)).addRule();

        verify(mockTable).addRow(
                "PipelineName", "RunNo", "Commit", "StageName",
                "JobName", "JobStatus", "AllowsFailure",
                "JobStartTime", "JobEndTime"
        );

        verify(mockTable).addRow(
                eq("PipelineX"),
                eq(42),
                eq("commit-sha123"),
                eq("Deploy"),
                eq("Job1"),
                eq("SUCCESS"),
                eq("Yes"),
                anyString(),
                anyString()
        );
    }

    @Test
    void testGenerateReport_withMalformedJobEntry_shouldContinue() throws JSONException {
        JSONArray jobArray = new JSONArray();
        jobArray.put("not-a-json-object"); // Malformed entry

        JSONObject reportData = new JSONObject();
        reportData.put(JSON_DATA_KEY_JOB, jobArray);

        generator.generateReport(reportData, mockTable);

        verify(mockTable, times(2)).addRule();

        verify(mockTable).addRow(
                "PipelineName", "RunNo", "Commit", "StageName",
                "JobName", "JobStatus", "AllowsFailure",
                "JobStartTime", "JobEndTime"
        );
    }

}
