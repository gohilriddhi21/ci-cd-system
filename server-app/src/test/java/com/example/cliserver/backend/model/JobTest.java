package com.example.cliserver.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
class JobTest {

    @Test
    void testDefaultConstructor() {
        Job job = new Job();
        assertNull(job.getJobStatus());
        assertEquals(0, job.getStartTime());
        assertEquals(0, job.getCompletionTime());
        assertNull(job.getName());
        assertNull(job.getStage());
        assertNull(job.getRegistry());
        assertNull(job.getImage());
        assertTrue(job.getScript().isEmpty());
        assertTrue(job.getNeeds().isEmpty());
        assertFalse(job.getAllowFailure());
    }

    @Test
    void testConstructorWithConfigJob() {
        ConfigJob configJob = new ConfigJob();
        configJob.setName("test-job");
        configJob.setStage("build");
        configJob.setImage("ubuntu:20.04");
        configJob.setAllowFailure(true);
        List<String> script = Arrays.asList("echo hello", "make build");
        configJob.setScript(script);
        List<String> needs = Arrays.asList("dependency-job");
        configJob.setNeeds(needs);
        Job job = new Job(configJob);
        assertNull(job.getJobStatus());
        assertEquals(0, job.getStartTime());
        assertEquals(0, job.getCompletionTime());
        assertEquals("test-job", job.getName());
        assertEquals("build", job.getStage());
        assertEquals("ubuntu:20.04", job.getImage());
        assertTrue(job.getAllowFailure());
        assertEquals(script, job.getScript());
        assertEquals(needs, job.getNeeds());
    }

    @Test
    void testSetAndGetJobStatus() {
        Job job = new Job();
        assertNull(job.getJobStatus());
        job.setJobStatus(Status.PENDING);
        assertEquals(Status.PENDING, job.getJobStatus());
        job.setJobStatus(Status.RUNNING);
        assertEquals(Status.RUNNING, job.getJobStatus());
        job.setJobStatus(Status.SUCCESS);
        assertEquals(Status.SUCCESS, job.getJobStatus());
        job.setJobStatus(Status.FAILED);
        assertEquals(Status.FAILED, job.getJobStatus());
        job.setJobStatus(Status.CANCELED);
        assertEquals(Status.CANCELED, job.getJobStatus());
    }

    @Test
    void testSetAndGetStartTime() {
        Job job = new Job();
        assertEquals(0, job.getStartTime());
        long startTime = System.currentTimeMillis();
        job.setStartTime(startTime);
        assertEquals(startTime, job.getStartTime());
        job.setStartTime(1000);
        assertEquals(1000, job.getStartTime());
    }

    @Test
    void testSetAndGetCompletionTime() {
        Job job = new Job();
        assertEquals(0, job.getCompletionTime());
        long completionTime = System.currentTimeMillis();
        job.setCompletionTime(completionTime);
        assertEquals(completionTime, job.getCompletionTime());
        job.setCompletionTime(2000);
        assertEquals(2000, job.getCompletionTime());
    }

    @Test
    void testInheritanceFromConfigJob() {
        ConfigJob configJob = new ConfigJob();
        configJob.setName("original-job");
        Job job = new Job(configJob);
        assertEquals("original-job", job.getName());
        job.setName("modified-job");
        assertEquals("original-job", configJob.getName());
        assertEquals("modified-job", job.getName());
    }

    @Test
    void testExtendingConfigJobFunctionality() {
        Job job = new Job();
        job.setName("test-job");
        job.setJobStatus(Status.RUNNING);
        job.setStartTime(1000);
        Job newJob = new Job(job);
        assertEquals("test-job", newJob.getName());
        assertNull(newJob.getJobStatus());
        assertEquals(0, newJob.getStartTime());
        assertEquals(0, newJob.getCompletionTime());
    }
}