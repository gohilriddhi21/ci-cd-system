package com.example.cliapplication.reportGeneration;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.mockito.Mockito.*;

class ReportProcessorTest {

    private final JSONObject testJson = new JSONObject().put("key", "value");
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    ReportProcessorTest() throws JSONException {
    }

    @BeforeEach
    void setup() {
        originalOut = System.err;
        outContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(outContent));
    }

    @Test
    void testProcessReport_withValidGenerator_callsRenderReport() throws Exception {
        ReportProcessor processor = new ReportProcessor("TABLE", "job");

        ReportGenerator mockGenerator = mock(ReportGenerator.class);

        Field field = ReportProcessor.class.getDeclaredField("reportGenerator");
        field.setAccessible(true);
        field.set(processor, mockGenerator);

        JSONObject testJson = new JSONObject().put("key", "value");
        processor.processReport(testJson);

        verify(mockGenerator, times(1)).renderReport(testJson);
    }
    @Test
    void testProcessReport_withValidGenerator_callsRenderReportNull() throws Exception {
        ReportProcessor processor = new ReportProcessor("TABLE", "job");

        ReportGenerator mockGenerator = mock(ReportGenerator.class);

        Field field = ReportProcessor.class.getDeclaredField("reportGenerator");
        field.setAccessible(true);
        field.set(processor, mockGenerator);

        JSONObject testJson = new JSONObject().put("key", "value");
        processor.processReport(null);
    }
}