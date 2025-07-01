package com.example.cliserver.backend.model;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineRunTest {

    private PipelineRun pipelineRun;

    @BeforeEach
    void setUp() {
        pipelineRun = new PipelineRun();
    }

    @Test
    void testDefaultConstructor() {
        assertNull(pipelineRun.getId());
        assertNull(pipelineRun.getRepo());
        assertNull(pipelineRun.getBranch());
        assertNull(pipelineRun.getCommit());
        assertNull(pipelineRun.getPipelineName());
        assertNull(pipelineRun.getFileName());
        assertEquals(0, pipelineRun.getRunNumber());
        assertEquals(0, pipelineRun.getStartTime());
        assertEquals(0, pipelineRun.getCompletionTime());
        assertNull(pipelineRun.getStatus());
        assertNull(pipelineRun.getPipelineStatus());
        assertTrue(pipelineRun.getStages().isEmpty());
        assertFalse(pipelineRun.isLocal());
        assertNull(pipelineRun.getRegistry());
        assertNull(pipelineRun.getImage());
        assertNull(pipelineRun.getUploadRepo());
    }

    @Test
    void testSetAndGetId() {
        pipelineRun.setId(null);
        assertNull(pipelineRun.getId());
        ObjectId id = new ObjectId();
        pipelineRun.setId(id);
        ObjectId returnedId = pipelineRun.getId();
        assertNotNull(returnedId);
        assertEquals(id.toString(), returnedId.toString());
        assertNotSame(id, returnedId);
    }

    @Test
    void testSetAndGetRepo() {
        String repo = "https://github.com/example/repo.git";
        pipelineRun.setRepo(repo);
        assertEquals(repo, pipelineRun.getRepo());
    }

    @Test
    void testSetAndGetBranch() {
        String branch = "main";
        pipelineRun.setBranch(branch);
        assertEquals(branch, pipelineRun.getBranch());
    }

    @Test
    void testSetAndGetCommit() {
        String commit = "abc123def456";
        pipelineRun.setCommit(commit);
        assertEquals(commit, pipelineRun.getCommit());
    }

    @Test
    void testSetAndGetPipelineName() {
        String pipelineName = "test-pipeline";
        pipelineRun.setPipelineName(pipelineName);
        assertEquals(pipelineName, pipelineRun.getPipelineName());
    }

    @Test
    void testSetAndGetFileName() {
        String fileName = "pipeline.yaml";
        pipelineRun.setFileName(fileName);
        assertEquals(fileName, pipelineRun.getFileName());
    }

    @Test
    void testSetAndGetRunNumber() {
        int runNumber = 42;
        pipelineRun.setRunNumber(runNumber);
        assertEquals(runNumber, pipelineRun.getRunNumber());
    }

    @Test
    void testSetAndGetStartTime() {
        long startTime = System.currentTimeMillis();
        pipelineRun.setStartTime(startTime);
        assertEquals(startTime, pipelineRun.getStartTime());
    }

    @Test
    void testSetAndGetCompletionTime() {
        long completionTime = System.currentTimeMillis();
        pipelineRun.setCompletionTime(completionTime);
        assertEquals(completionTime, pipelineRun.getCompletionTime());
    }

    @Test
    void testSetAndGetStatus() {
        for (Status status : Status.values()) {
            pipelineRun.setStatus(status);
            assertEquals(status, pipelineRun.getStatus());
            assertEquals(status, pipelineRun.getPipelineStatus());
        }
    }

    @Test
    void testSetAndGetPipelineStatus() {
        for (Status status : Status.values()) {
            pipelineRun.setPipelineStatus(status);
            assertEquals(status, pipelineRun.getPipelineStatus());
            assertEquals(status, pipelineRun.getStatus());
        }
    }

    @Test
    void testSetAndGetStages_WithValidList() {
        Stage stage1 = new Stage();
        stage1.setStageName("build");
        Stage stage2 = new Stage();
        stage2.setStageName("test");
        List<Stage> stages = Arrays.asList(stage1, stage2);
        pipelineRun.setStages(stages);
        List<Stage> returnedStages = pipelineRun.getStages();
        assertEquals(2, returnedStages.size());
        assertEquals("build", returnedStages.get(0).getStageName());
        assertEquals("test", returnedStages.get(1).getStageName());
        assertNotSame(stages, returnedStages);
    }

    @Test
    void testSetAndGetStages_WithNullList() {
        pipelineRun.setStages(null);
        List<Stage> returnedStages = pipelineRun.getStages();
        assertNotNull(returnedStages);
        assertTrue(returnedStages.isEmpty());
    }

    @Test
    void testGetStages_WithNullStages() {
        List<Stage> returnedStages = pipelineRun.getStages();
        assertNotNull(returnedStages);
        assertTrue(returnedStages.isEmpty());
    }

    @Test
    void testSetAndGetIsLocal() {
        assertFalse(pipelineRun.isLocal());
        pipelineRun.setLocal(true);
        assertTrue(pipelineRun.isLocal());
        pipelineRun.setLocal(false);
        assertFalse(pipelineRun.isLocal());
    }

    @Test
    void testSetAndGetRegistry() {
        String registry = "docker.io";
        pipelineRun.setRegistry(registry);
        assertEquals(registry, pipelineRun.getRegistry());
    }

    @Test
    void testSetAndGetImage() {
        String image = "ubuntu:20.04";
        pipelineRun.setImage(image);
        assertEquals(image, pipelineRun.getImage());
    }

    @Test
    void testSetAndGetUploadRepo() {
        String uploadRepo = "github.com/user/repo";
        pipelineRun.setUploadRepo(uploadRepo);
        assertEquals(uploadRepo, pipelineRun.getUploadRepo());
    }

    @Test
    void testAddStage() {
        pipelineRun.setStages(new ArrayList<>());
        Stage stage = new Stage();
        stage.setStageName("build");
        pipelineRun.addStage(stage);
        List<Stage> stages = pipelineRun.getStages();
        assertEquals(1, stages.size());
        assertEquals("build", stages.get(0).getStageName());
        Stage stage2 = new Stage();
        stage2.setStageName("test");
        pipelineRun.addStage(stage2);
        stages = pipelineRun.getStages();
        assertEquals(2, stages.size());
        assertEquals("build", stages.get(0).getStageName());
        assertEquals("test", stages.get(1).getStageName());
    }

    @Test
    void testAddStage_WithNullStages() {
        Stage stage = new Stage();
        stage.setStageName("build");
        assertThrows(NullPointerException.class, () -> {
            pipelineRun.addStage(stage);
        });
    }

    @Test
    void testGetStages_DefensiveCopy() {
        List<Stage> originalStages = new ArrayList<>();
        Stage stage = new Stage();
        stage.setStageName("build");
        originalStages.add(stage);
        pipelineRun.setStages(originalStages);
        List<Stage> returnedStages = pipelineRun.getStages();
        Stage newStage = new Stage();
        newStage.setStageName("test");
        returnedStages.add(newStage);
        List<Stage> currentStages = pipelineRun.getStages();
        assertEquals(1, currentStages.size());
        assertEquals("build", currentStages.get(0).getStageName());
    }
}