package com.example.cliapplication.reportGeneration;

import org.json.JSONObject;

/**
 * Handles the creation and processing of reports based on the provided format and report type.
 */
public class ReportProcessor {
    private ReportGenerator reportGenerator;

    /**
     * Constructs a ReportProcessor with the specified format and report type.
     * Initializes the appropriate {@link ReportGenerator} using the {@link ReportGeneratorFactory}.
     *
     * @param format     the desired format of the report (e.g., "table")
     * @param reportType the type of report to generate
     */
    public ReportProcessor(String format, String reportType) {
        this.reportGenerator = ReportGeneratorFactory.createReportGenerator(format, reportType);
    }

    /**
     * Processes the given JSON data using the configured {@link ReportGenerator}.
     *
     * @param jsonData the data to include in the generated report
     */
    public void processReport(JSONObject jsonData) {
        if (reportGenerator == null) {
            System.err.println("Report Generator is not initialized.");
            return;
        }
        reportGenerator.renderReport(jsonData);
    }
}
