package com.example.cliserver.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class StageTest {
    @Test
    public void testDefaultConstructor() {
        Stage stage = new Stage();
        assertNull(stage.getStageName(), "Stage name should be null by default");
        assertNull(stage.getStageStatus(), "Stage status should be null by default");
        assertEquals(0, stage.getStartTime(), "Start time should be 0 by default");
        assertEquals(0, stage.getCompletionTime(), "Completion time should be 0 by default");
        assertNull(stage.getJobs(), "Jobs should be null until initialized");
    }

    @Test
    public void testStageNameSetterGetter() {
        Stage stage = new Stage();
        stage.setStageName("Build");
        assertEquals("Build", stage.getStageName(), "Stage name getter/setter failure");
    }

    @Test
    public void testStageStatusSetterGetter() {
        Stage stage = new Stage();
        stage.setStageStatus(Status.SUCCESS);
        assertEquals(Status.SUCCESS, stage.getStageStatus(), "Stage status getter/setter failure");
    }

    @Test
    public void testStageStatusSetterGetterCancel() {
        Stage stage = new Stage();
        stage.setStageStatus(Status.CANCELED);
        assertEquals(Status.CANCELED, stage.getStageStatus(), "Stage status getter/setter failure");
    }

    @Test
    public void testStartTimeSetterGetter() {
        Stage stage = new Stage();
        long start = 1000L;
        stage.setStartTime(start);
        assertEquals(start, stage.getStartTime(), "Start time getter/setter failure");
    }

    @Test
    public void testCompletionTimeSetterGetter() {
        Stage stage = new Stage();
        long completion = 2000L;
        stage.setCompletionTime(completion);
        assertEquals(completion, stage.getCompletionTime(), "Completion time getter/setter failure");
    }

    @Test
    public void testSetJobsWithNull() {
        Stage stage = new Stage();
        stage.setJobs(null);
        List<Job> jobs = stage.getJobs();
        assertNotNull(jobs, "Jobs list should be initialized as a non-null empty list");
        assertTrue(jobs.isEmpty(), "Jobs list should be empty when initialized with null");
    }

    @Test
    public void testSetJobsWithNonNullList() {
        Stage stage = new Stage();
        List<Job> jobList = new ArrayList<>();
        Job job1 = new Job();
        Job job2 = new Job();
        job1.setName("Job1");
        job1.setName("Job2");
        jobList.add(job1);
        jobList.add(job2);
        stage.setJobs(jobList);

        List<Job> stageJobs = stage.getJobs();
        assertNotNull(stageJobs, "Jobs list should not be null when set with a non-null list");
        assertEquals(2, stageJobs.size(), "Jobs list should contain 2 items");
        assertTrue(stageJobs.contains(job1), "Jobs list should contain job1");
        assertTrue(stageJobs.contains(job2), "Jobs list should contain job2");

        stageJobs.remove(job1);
        List<Job> jobsAfterModification = stage.getJobs();
        assertEquals(2, jobsAfterModification.size(), "Internal jobs list should not be affected by modifications to the returned copy");
    }

    @Test
    public void testAddJobSuccess() {
        Stage stage = new Stage();
        stage.setJobs(null);
        Job job = new Job();
        job.setName("Job1");
        stage.addJob(job);
        List<Job> jobs = stage.getJobs();
        assertNotNull(jobs, "Jobs list should not be null after initialization and adding a job");
        assertEquals(1, jobs.size(), "Jobs list should contain one item after addJob");
        assertEquals(job, jobs.get(0), "The added job should be present in the jobs list");
    }

    @Test
    public void testAddJobWithoutInitializingJobs() {
        Stage stage = new Stage();
        Job job = new Job();
        job.setName("Job1");
        stage.addJob(job);
        String result = stage.toString();
        assertTrue(result.contains(job.toString()), "toString should contain the jobs list details");
    }

    @Test
    public void testToString() {
        Stage stage = new Stage();
        stage.setStageName("Deploy");
        stage.setStageStatus(Status.FAILED);
        stage.setStartTime(3000L);
        stage.setCompletionTime(4000L);
        stage.setJobs(null);
        Job job = new Job();
        job.setName("JobDeploy");
        stage.addJob(job);

        String result = stage.toString();
        assertTrue(result.contains("stageName='Deploy'"), "toString should contain the stage name");
        assertTrue(result.contains("stageStatus='" + Status.FAILED.toString() + "'"), "toString should contain the stage status");
        assertTrue(result.contains("startTime=3000"), "toString should contain the start time");
        assertTrue(result.contains("completionTime=4000"), "toString should contain the completion time");
        assertTrue(result.contains(job.toString()), "toString should contain the jobs list details");
    }
}
