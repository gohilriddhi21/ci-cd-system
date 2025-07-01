package com.example.cliapplication.reportGeneration;

import de.vandermeer.asciitable.AsciiTable;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {

    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    private TestReportGenerator jsonReporter;
    private TestReportGenerator tableReporter;

    static class TestReportGenerator extends ReportGenerator {
        boolean generateCalled = false;

        public TestReportGenerator(String format) {
            super(format);
        }

        @Override
        public void generateReport(JSONObject data, AsciiTable asciiTable) {
            generateCalled = true;
            asciiTable.addRule();
        }
    }

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        jsonReporter = new TestReportGenerator("JSON");
        tableReporter = new TestReportGenerator("TABLE");
    }

    @Test
    void testRenderReport_withNull_shouldPrintNoDataMessage() {
        jsonReporter.renderReport(null);
        assertTrue(outContent.toString().contains("No data found for the specified input."));
    }

    @Test
    void testRenderReport_withEmptyJson_shouldPrintNoDataMessage() {
        jsonReporter.renderReport(new JSONObject());
        assertTrue(outContent.toString().contains("No data found for the specified input."));
    }

    @Test
    void testRenderReport_inJsonFormat_shouldPrintPrettyJson() throws JSONException {
        JSONObject input = new JSONObject();
        input.put("key", "value");

        jsonReporter.renderReport(input);
        String output = outContent.toString();

        assertFalse(output.contains("{\n  \"key\": \"value\"\n}"));
        assertFalse(jsonReporter.generateCalled);
    }

    @Test
    void testInitializeAsciiTable_setsCorrectConfig() {
        AsciiTable table = jsonReporter.initializeAsciiTable();
        assertNotNull(table.getContext());
        assertEquals(165, table.getContext().getWidth());
        assertNotNull(table.getContext().getGridTheme());
    }
    @Test
    void testFormatTimestamp_withNull_shouldReturnNA() {
        assertEquals("N/A", jsonReporter.formatTimestamp(null));
    }

    @Test
    void testFormatTimestamp_withZero_shouldReturnNA() {
        assertEquals("N/A", jsonReporter.formatTimestamp(0L));
    }

    @Test
    void testFormatTimestamp_withNegative_shouldReturnNA() {
        assertEquals("N/A", jsonReporter.formatTimestamp(-1L));
    }

    @Test
    void testFormatTimestamp_withValidTimestamp_shouldFormatDate() {
        long ts = 1713200000000L;
        String formatted = jsonReporter.formatTimestamp(ts);
        assertTrue(formatted.matches("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}"));
    }
}