package com.example.cliapplication.reportGeneration.ReportFormats;

import de.vandermeer.asciitable.AsciiTable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.cliapplication.utils.Constants.*;
import static org.mockito.Mockito.*;

class StageReportGeneratorTest {

    private StageReportGenerator generator;
    private AsciiTable mockTable;

    @BeforeEach
    void setup() {
        generator = new StageReportGenerator("table");
        mockTable = mock(AsciiTable.class);
    }

    @Test
    void testGenerateReport_whenReportDataIsNull_shouldPrintMessage() {
        generator.generateReport(null, mockTable);
        verifyNoInteractions(mockTable);
    }

    @Test
    void testGenerateReport_whenReportDataIsEmpty_shouldPrintMessage() {
        generator.generateReport(new JSONObject(), mockTable);
        verifyNoInteractions(mockTable);
    }


    @Test
    void testGenerateReport_withValidStageSummary_shouldAddRows() throws JSONException {
        JSONObject stageSummary = new JSONObject();
        stageSummary.put(PIPELINE_NAME_FIELD, "MyPipe");
        stageSummary.put(RUN_NUMBER_FIELD, 10);
        stageSummary.put(COMMIT_FIELD, "abc123");
        stageSummary.put(STAGE_NAME_FIELD, "Build");
        stageSummary.put(STAGE_STATUS_FIELD, "SUCCESS");
        stageSummary.put(JOB_NAME_FIELD, "Compile");
        stageSummary.put(JOB_STATUS_FIELD, "PASSED");
        stageSummary.put(ALLOWS_FAILURE_FIELD, false);
        stageSummary.put(START_TIME_FIELD, 1712345678000L);
        stageSummary.put(COMPLETION_TIME_FIELD, 1712345688000L);

        JSONArray array = new JSONArray();
        array.put(stageSummary);

        JSONObject reportData = new JSONObject();
        reportData.put(JSON_DATA_KEY_STAGE, array);

        generator.generateReport(reportData, mockTable);

        verify(mockTable, atLeast(3)).addRule(); // header, row, end
        verify(mockTable).addRow(
                eq("PipelineName"), eq("RunNo"), eq("Commit"), eq("StageName"),
                eq("StageStatus"), eq("JobName"), eq("JobStatus"),
                eq("AllowsFailure"), eq("JobStartTime"), eq("JobEndTime")
        );
        verify(mockTable).addRow(
                eq("MyPipe"), eq(10), eq("abc123"), eq("Build"),
                eq("SUCCESS"), eq("Compile"), eq("PASSED"),
                eq("No"), anyString(), anyString()
        );
    }

    @Test
    void testGenerateReport_withInvalidStageEntry_shouldSkipAndLog() throws JSONException {
        JSONArray array = new JSONArray();
        array.put("not-a-json-object");

        JSONObject reportData = new JSONObject();
        reportData.put(JSON_DATA_KEY_STAGE, array);

        generator.generateReport(reportData, mockTable);

        verify(mockTable, times(2)).addRule();

        verify(mockTable).addRow(
                eq("PipelineName"), eq("RunNo"), eq("Commit"), eq("StageName"),
                eq("StageStatus"), eq("JobName"), eq("JobStatus"),
                eq("AllowsFailure"), eq("JobStartTime"), eq("JobEndTime")
        );
    }
}
