package com.example.cliapplication.reportGeneration;

import com.example.cliapplication.reportGeneration.ReportFormats.DefaultReportGenerator;
import com.example.cliapplication.reportGeneration.ReportFormats.JobReportGenerator;
import com.example.cliapplication.reportGeneration.ReportFormats.StageReportGenerator;

/**
 * Factory class responsible for creating
 * appropriate {@link ReportGenerator} instances
 * based on the specified report type and output format.
 */
public class ReportGeneratorFactory {
    /**
     * Creates a new instance.
     */
    private ReportGeneratorFactory() {
        // default constructor
    }

    /**
     * Factory method to create the correct {@link ReportGenerator}
     * implementation based on the given report type.
     *
     * <p>Supported report types:
     * <ul>
     *     <li>{@code stage} - Returns an instance of {@link StageReportGenerator}</li>
     *     <li>{@code job} - Returns an instance of {@link JobReportGenerator}</li>
     *     <li>Any other or null - Returns an instance of {@link DefaultReportGenerator}</li>
     * </ul>
     *
     * @param format     the desired report output format
     *                   (e.g., {@code JSON}, {@code table})
     * @param reportType the type of report to generate
     *                   (e.g., {@code stage}, {@code job}, or null/default)
     * @return a concrete instance of {@link ReportGenerator}
     *         based on the provided report type
     */
    public static ReportGenerator createReportGenerator(String format, String reportType) {
        if ("stage".equalsIgnoreCase(reportType)) {
            return new StageReportGenerator(format);
        } else if ("job".equalsIgnoreCase(reportType)) {
            return new JobReportGenerator(format);
        }
        return new DefaultReportGenerator(format);
    }
}
