package com.example.cliserver.backend.database.mongoDB;

import com.example.cliserver.backend.model.PipelineRun;
import com.example.cliserver.backend.utils.Constants;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MongoDBPipelineRunsDaoTest {
    private MongoDBPipelineRunsDao dao;
    private MongoCollection<Document> mockCollection;

    @BeforeEach
    void setup() {
        mockCollection = mock(MongoCollection.class);

        try {
            var field = MongoDBPipelineRunsDao.class.getDeclaredField("collection");
            field.setAccessible(true);
            field.set(null, mockCollection);
        } catch (Exception e) {
            fail("Failed to inject mock MongoCollection", e);
        }

        dao = (MongoDBPipelineRunsDao) MongoDBPipelineRunsDao.getInstance();
    }

    @Test
    void testGetDefaultReportSummary_withPipelineAndRun() {
        when(mockCollection.aggregate(any())).thenReturn(mock(AggregateIterable.class));

        dao.getDefaultReportSummary("pipe1", "repo1", "42");

        verify(mockCollection).aggregate(argThat(pipeline -> !pipeline.isEmpty()));
    }

    @Test
    void testGetStageSummary_withValidArgs_inspectPipeline() {
        AggregateIterable<Document> mockResult = mock(AggregateIterable.class);
        ArgumentCaptor<List<Bson>> pipelineCaptor = ArgumentCaptor.forClass(List.class);

        when(mockCollection.aggregate(any())).thenReturn(mockResult);

        dao.getStageSummary("pipe1", "build", "10");

        verify(mockCollection).aggregate(pipelineCaptor.capture());

        List<Bson> pipeline = pipelineCaptor.getValue();
        assertFalse(pipeline.isEmpty());
    }


    @Test
    void testUpdatePipelineRun_shouldCallReplaceOne() {
        PipelineRun mockRun = mock(PipelineRun.class);
        when(mockRun.getPipelineName()).thenReturn("pipe");
        when(mockRun.getRunNumber()).thenReturn(1);
        when(mockRun.getRepo()).thenReturn("repo");
        when(mockRun.getFileName()).thenReturn("file");
        when(mockRun.getBranch()).thenReturn("main");
        when(mockRun.getCommit()).thenReturn("c123");
        when(mockRun.getStartTime()).thenReturn(System.currentTimeMillis());
        when(mockRun.getCompletionTime()).thenReturn(System.currentTimeMillis() + 1000);
        when(mockRun.getPipelineStatus()).thenReturn(com.example.cliserver.backend.model.Status.SUCCESS);
        when(mockRun.isLocal()).thenReturn(true);
        when(mockRun.getStages()).thenReturn(Collections.emptyList());

        dao.updatePipelineRun(mockRun);

        verify(mockCollection).replaceOne((Bson) any(), any(), any());
    }
    @Test
    void testGetByPipelineName_nullOrEmpty_shouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> dao.getByPipelineName(null));
        assertThrows(IllegalArgumentException.class, () -> dao.getByPipelineName("  "));
    }

    @Test
    void testGetByPipelineName_valid() {
        Document mockDoc = new Document("name", "pipeline1");

        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(any())).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(mockDoc);

        Document result = dao.getByPipelineName("pipeline1");
        assertNotNull(result);
        assertEquals("pipeline1", result.getString("name"));
    }


    @Test
    void testGetRepoPipelineRunReports_withEmptyPipelineName() {
        FindIterable<Document> mockResult = mock(FindIterable.class);
        when(mockCollection.find(any(Bson.class))).thenReturn(mockResult);

        FindIterable<Document> result = dao.getRepoPipelineRunReports("repo1", "");
        assertEquals(mockResult, result);
    }

    @Test
    void testGetRunNumber_whenNoPreviousRun() {
        FindIterable<Document> mockFindIterable = mock(FindIterable.class);

        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(any())).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(null);  // Simulate no previous run

        int runNumber = dao.getRunNumber("pipe1", "repo1");
        assertEquals(1, runNumber); // Should return 1 when no previous run exists
    }


    @Test
    void testGetRunNumber_whenPreviousRunExists() {
        Document lastRun = new Document(Constants.RUN_NUMBER_FIELD, 5);

        FindIterable<Document> mockFindIterable = mock(FindIterable.class);
        when(mockCollection.find(any(Bson.class))).thenReturn(mockFindIterable);
        when(mockFindIterable.sort(any())).thenReturn(mockFindIterable);
        when(mockFindIterable.first()).thenReturn(lastRun);

        int runNumber = dao.getRunNumber("pipe1", "repo1");
        assertEquals(6, runNumber);
    }

    @Test
    void testGetTimeFilteredPipelineRunReports_withNulls() {
        FindIterable<Document> mockResult = mock(FindIterable.class);
        when(mockCollection.find(any(Document.class))).thenReturn(mockResult);
        when(mockResult.sort(any())).thenReturn(mockResult);

        FindIterable<Document> result = dao.getTimeFilteredPipelineRunReports("repo1", null, null);
        assertEquals(mockResult, result);
    }

    @Test
    void testFindActiveRuns_withVariousNulls() {
        FindIterable<Document> mockResult = mock(FindIterable.class);
        when(mockCollection.find(any(Document.class))).thenReturn(mockResult);

        dao.findActiveRuns(null, null, null);
        dao.findActiveRuns("repo1", null, null);
        dao.findActiveRuns("repo1", "pipe", null);
        dao.findActiveRuns("repo1", "pipe", 5);

        verify(mockCollection, atLeastOnce()).find(any(Document.class));
    }

    @Test
    void testPrettyPrintDocument() {
        Document doc = new Document("field1", "value1").append("field2", 123);
        dao.prettyPrintDocument(doc);
    }
}