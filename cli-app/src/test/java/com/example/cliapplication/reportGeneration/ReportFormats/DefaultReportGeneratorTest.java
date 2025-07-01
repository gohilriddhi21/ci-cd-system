package com.example.cliapplication.reportGeneration.ReportFormats;

import de.vandermeer.asciitable.AsciiTable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.cliapplication.utils.Constants.*;
import static org.mockito.Mockito.*;

class DefaultReportGeneratorTest {

    private DefaultReportGenerator generator;
    private AsciiTable mockTable;

    @BeforeEach
    void setUp() {
        generator = new DefaultReportGenerator("TABLE");
        mockTable = mock(AsciiTable.class);
    }

    @Test
    void testGenerateReport_whenDataIsEmpty_addsNoDataMessage() {
        JSONObject data = new JSONObject();  // No "default" array

        generator.generateReport(data, mockTable);

        verify(mockTable, times(2)).addRule();
        verify(mockTable).addRow("No default report data found.");
    }

    @Test
    void testGenerateReport_whenDataIsPresent_addsFormattedRows() throws JSONException {
        JSONObject job = new JSONObject();
        job.put(PIPELINE_NAME_FIELD, "MyPipeline");
        job.put(RUN_NUMBER_FIELD, 123);
        job.put(COMMIT_FIELD, "abc123");
        job.put(PIPELINE_STATUS_FIELD, "SUCCESS");
        job.put(STAGE_NAME_FIELD, "Build");
        job.put(STAGE_STATUS_FIELD, "PASSED");

        long now = System.currentTimeMillis();
        job.put(START_TIME_FIELD, now - 10000);
        job.put(COMPLETION_TIME_FIELD, now);

        JSONArray array = new JSONArray();
        array.put(job);

        JSONObject data = new JSONObject();
        data.put(DEFAULT_JSON_KEY, array);

        generator.generateReport(data, mockTable);

        verify(mockTable, atLeast(3)).addRule();

        verify(mockTable).addRow(
                "PipelineName", "RunNo", "Commit", "PipelineStatus",
                "StageName", "StageStatus", "JobStartTime", "JobEndTime");

        // Row with actual data
        verify(mockTable).addRow(
                eq("MyPipeline"),
                eq(123),
                eq("abc123"),
                eq("SUCCESS"),
                eq("Build"),
                eq("PASSED"),
                anyString(),
                anyString()
        );
    }
}