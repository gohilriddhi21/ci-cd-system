package com.example.cliapplication.reportGeneration;

import com.example.cliapplication.reportGeneration.ReportFormats.DefaultReportGenerator;
import com.example.cliapplication.reportGeneration.ReportFormats.JobReportGenerator;
import com.example.cliapplication.reportGeneration.ReportFormats.StageReportGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorFactoryTest {

    @Test
    void testCreateReportGenerator_withStageType_returnsStageReportGenerator() {
        ReportGenerator generator = ReportGeneratorFactory.createReportGenerator("TABLE", "stage");
        assertNotNull(generator);
        assertTrue(generator instanceof StageReportGenerator);
    }

    @Test
    void testCreateReportGenerator_withJobType_returnsJobReportGenerator() {
        ReportGenerator generator = ReportGeneratorFactory.createReportGenerator("JSON", "job");
        assertNotNull(generator);
        assertTrue(generator instanceof JobReportGenerator);
    }

    @Test
    void testCreateReportGenerator_withDefaultType_returnsDefaultReportGenerator() {
        ReportGenerator generator = ReportGeneratorFactory.createReportGenerator("TABLE", "default");
        assertNotNull(generator);
        assertTrue(generator instanceof DefaultReportGenerator);
    }

    @Test
    void testCreateReportGenerator_withNullType_returnsDefaultReportGenerator() {
        ReportGenerator generator = ReportGeneratorFactory.createReportGenerator("TABLE", null);
        assertNotNull(generator);
        assertTrue(generator instanceof DefaultReportGenerator);
    }

    @Test
    void testCreateReportGenerator_withUnknownType_returnsDefaultReportGenerator() {
        ReportGenerator generator = ReportGeneratorFactory.createReportGenerator("TABLE", "foo-bar");
        assertNotNull(generator);
        assertTrue(generator instanceof DefaultReportGenerator);
    }
}
