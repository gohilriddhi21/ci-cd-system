package com.example.cliserver.backend.database.mongoDB;

import com.example.cliserver.backend.model.PipelineRun;
import com.mongodb.client.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;

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
    void testGetStageSummary_withValidArgs() {
        when(mockCollection.aggregate(any())).thenReturn(mock(AggregateIterable.class));

        dao.getStageSummary("pipe1", "build", "10");

        verify(mockCollection).aggregate(any());
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
}
