package com.example.cliapplication.reportGeneration;

import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.TA_GridThemes;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstract class for generating specific reports
 */
public abstract class ReportGenerator {

    /**
     * Format for displaying the reports: JSON or Table.
     * Defaults to JSON.
     */
    private String format = "JSON";

    /**
     * Date object to format date in specific format.
     */
    public final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    /**
     * Default Constructor to set the format of the report.
     *
     * @param format is the specified format; Table or JSON
     */
    public ReportGenerator(String format) {
        this.format = format;
    }

    /**
     * Initialize the AsciiTable with specific grid theme and width.
     *
     * @return asciiTable object with set configuration.
     */
    public AsciiTable initializeAsciiTable() {
        AsciiTable asciiTable = new AsciiTable();
        asciiTable.getContext().setWidth(165);
        asciiTable.getContext().setGridTheme(TA_GridThemes.FULL);
        return asciiTable;
    }

    /**
     * Default implementation to render the report for a single JSONObject.
     *
     * @param jsonData a {@link JSONObject} containing the data to be included in the report.
     */
    public void renderReport(JSONObject jsonData) {
        if (jsonData == null || jsonData.length() == 0) {
            System.out.println("No data found for the specified input.");
            return;
        }
        printReport(jsonData);
    }

    /**
     * Prints the report based on the specified format.
     *
     * @param jsonData the {@link JSONObject} to be printed.
     */
    private void printReport(JSONObject jsonData) {
        if ("table".equalsIgnoreCase(format)) {
            AsciiTable asciiTable = initializeAsciiTable();
            generateReport(jsonData, asciiTable);

            // Prevent rendering empty table
            if (asciiTable.getRawContent().isEmpty()) {
                System.err.println("No data found.");
                return;
            }

            System.out.println(asciiTable.render());
        } else {
            System.out.println(jsonData.toString(2));
        }
    }

    /**
     * Abstract method for subclasses to define report-specific generation logic.
     *
     * @param data an {@link JSONObject} containing the aggregated data
     *                     to be included in the report.
     * @param asciiTable   an {@link AsciiTable} object used to generate and format the table.
     */
    public abstract void generateReport(JSONObject data,
                                        AsciiTable asciiTable);

    /**
     * Formats a given timestamp (in milliseconds) into a human-readable date string.
     * <p>
     * If the timestamp is {@code null}, {@code 0}, or {@code -1}, the current system
     * date and time is used instead.
     *
     * @param timestampMillis The timestamp in milliseconds since the epoch,
     *                        or {@code null}, {@code 0}, or {@code -1} to use the current time.
     * @return A formatted date string (e.g., {@code "14-04-2025 13:48:52"}).
     */
    public String formatTimestamp(Long timestampMillis) {
        if (timestampMillis == null || timestampMillis <= 0) {
            return "N/A";
        }
        Date dateToFormat = new Date(timestampMillis);
        return DATE_FORMAT.format(dateToFormat);
    }
}
