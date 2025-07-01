package com.example.cliserver.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ConfigJobTest {

    @Test
    void testDefaultConstructor() {
        ConfigJob job = new ConfigJob();
        assertNull(job.getName());
        assertNull(job.getStage());
        assertNull(job.getRegistry());
        assertNull(job.getImage());
        assertNull(job.getUploadRepo());
        assertTrue(job.getScript().isEmpty());
        assertTrue(job.getNeeds().isEmpty());
        assertTrue(job.getPorts().isEmpty());
        assertFalse(job.getAllowFailure());
        assertTrue(job.getArtifacts().isEmpty());
    }

    @Test
    void testCopyConstructor() {
        ConfigJob original = new ConfigJob();
        original.setName("job1");
        original.setStage("build");
        original.setImage("ubuntu:20.04");
        original.setAllowFailure(true);
        original.setRegistry("docker.io");
        original.setUploadRepo("github.com/user/repo");

        List<String> script = Arrays.asList("echo hello", "make build");
        original.setScript(script);

        List<String> needs = Arrays.asList("job0");
        original.setNeeds(needs);

        List<String> ports = Arrays.asList("8080:8080", "9000:9000");
        original.setPorts(ports);

        List<String> artifacts = Arrays.asList("build/output", "dist/");
        original.setArtifacts(artifacts);
        ConfigJob copy = new ConfigJob(original);
        assertEquals("job1", copy.getName());
        assertEquals("build", copy.getStage());
        assertEquals("ubuntu:20.04", copy.getImage());
        assertTrue(copy.getAllowFailure());
        assertTrue(copy.isAllowFailure());
        assertEquals("docker.io", copy.getRegistry());
        assertEquals("github.com/user/repo", copy.getUploadRepo());
        assertEquals(script, copy.getScript());
        assertNotSame(script, copy.getScript());
        assertEquals(needs, copy.getNeeds());
        assertNotSame(needs, copy.getNeeds());
        assertEquals(ports, copy.getPorts());
        assertNotSame(ports, copy.getPorts());
        assertEquals(artifacts, copy.getArtifacts());
        assertNotSame(artifacts, copy.getArtifacts());
        original.setName("changed");
        assertNotEquals(original.getName(), copy.getName());

    }

    @Test
    void testCopyConstructorWithNullLists() {
        ConfigJob original = new ConfigJob();
        original.setName("job1");
        ConfigJob copy = new ConfigJob(original);
        assertEquals("job1", copy.getName());
        assertTrue(copy.getScript().isEmpty());
        assertTrue(copy.getNeeds().isEmpty());
        assertTrue(copy.getPorts().isEmpty());
        assertTrue(copy.getArtifacts().isEmpty());
    }

    @Test
    void testSetName_ValidInput() {
        ConfigJob job = new ConfigJob();
        job.setName("test-job");
        assertEquals("test-job", job.getName());
    }

    @Test
    void testSetName_InvalidInput() {
        ConfigJob job = new ConfigJob();
        assertThrows(IllegalArgumentException.class, () -> {
            job.setName(123);
        });
    }

    @Test
    void testSetStage_ValidInput() {
        ConfigJob job = new ConfigJob();
        job.setStage("test-stage");
        assertEquals("test-stage", job.getStage());
    }

    @Test
    void testSetStage_InvalidInput() {
        ConfigJob job = new ConfigJob();
        assertThrows(IllegalArgumentException.class, () -> {
            job.setStage(123);
        });
    }

    @Test
    void testSetRegistry_ValidInput() {
        ConfigJob job = new ConfigJob();
        job.setRegistry("docker.io");
        assertEquals("docker.io", job.getRegistry());
    }

    @Test
    void testSetRegistry_InvalidInput() {
        ConfigJob job = new ConfigJob();
        assertThrows(IllegalArgumentException.class, () -> {
            job.setRegistry(123);
        });
    }

    @Test
    void testSetImage_ValidInput() {
        ConfigJob job = new ConfigJob();
        job.setImage("ubuntu:20.04");
        assertEquals("ubuntu:20.04", job.getImage());
    }

    @Test
    void testSetImage_InvalidInput() {
        ConfigJob job = new ConfigJob();
        assertThrows(IllegalArgumentException.class, () -> {
            job.setImage(123);
        });
    }

    @Test
    void testSetUploadRepo_ValidInput() {
        ConfigJob job = new ConfigJob();
        job.setUploadRepo("github.com/user/repo");
        assertEquals("github.com/user/repo", job.getUploadRepo());
    }

    @Test
    void testSetUploadRepo_InvalidInput() {
        ConfigJob job = new ConfigJob();
        assertThrows(IllegalArgumentException.class, () -> {
            job.setUploadRepo(123);
        });
    }

    @Test
    void testSetScript_ValidInput() {
        ConfigJob job = new ConfigJob();
        List<String> script = Arrays.asList("echo hello", "make build");
        job.setScript(script);
        assertEquals(script, job.getScript());
        assertNotSame(script, job.getScript());
    }

    @Test
    void testSetScript_NullInput() {
        ConfigJob job = new ConfigJob();
        assertThrows(IllegalArgumentException.class, () -> {
            job.setScript(null);
        });
    }

    @Test
    void testGetScript_DefensiveCopy() {
        ConfigJob job = new ConfigJob();
        List<String> script = new ArrayList<>(Arrays.asList("echo hello", "make build"));
        job.setScript(script);
        List<String> returnedScript = job.getScript();
        returnedScript.add("should not affect original");

        assertEquals(2, job.getScript().size());
    }

    @Test
    void testSetNeeds_ValidInput() {
        ConfigJob job = new ConfigJob();
        List<String> needs = Arrays.asList("job1", "job2");
        job.setNeeds(needs);
        assertEquals(needs, job.getNeeds());
        assertNotSame(needs, job.getNeeds());
    }

    @Test
    void testSetNeeds_NullInput() {
        ConfigJob job = new ConfigJob();
        job.setNeeds(null);
        assertNotNull(job.getNeeds());
        assertTrue(job.getNeeds().isEmpty());
    }

    @Test
    void testGetNeeds_DefensiveCopy() {
        ConfigJob job = new ConfigJob();
        List<String> needs = new ArrayList<>(Arrays.asList("job1", "job2"));
        job.setNeeds(needs);

        List<String> returnedNeeds = job.getNeeds();
        returnedNeeds.add("should not affect original");

        assertEquals(2, job.getNeeds().size());
    }

    @Test
    void testSetPorts_ValidInput() {
        ConfigJob job = new ConfigJob();
        List<String> ports = Arrays.asList("8080:8080", "9000:9000");
        job.setPorts(ports);
        assertEquals(ports, job.getPorts());
        assertNotSame(ports, job.getPorts());
    }

    @Test
    void testSetPorts_NullInput() {
        ConfigJob job = new ConfigJob();
        job.setPorts(null);
        assertTrue(job.getPorts().isEmpty());
    }

    @Test
    void testGetPorts_DefensiveCopy() {
        ConfigJob job = new ConfigJob();
        List<String> ports = new ArrayList<>(Arrays.asList("8080:8080", "9000:9000"));
        job.setPorts(ports);
        List<String> returnedPorts = job.getPorts();
        returnedPorts.add("should not affect original");
        assertEquals(2, job.getPorts().size());
    }

    @Test
    void testSetArtifacts_ValidInput() {
        ConfigJob job = new ConfigJob();
        List<String> artifacts = Arrays.asList("build/output", "dist/");
        job.setArtifacts(artifacts);
        assertEquals(artifacts, job.getArtifacts());
        assertNotSame(artifacts, job.getArtifacts());
    }

    @Test
    void testSetArtifacts_NullInput() {
        ConfigJob job = new ConfigJob();
        assertThrows(IllegalArgumentException.class, () -> {
            job.setArtifacts(null);
        });
    }

    @Test
    void testGetArtifacts_DefensiveCopy() {
        ConfigJob job = new ConfigJob();
        List<String> artifacts = new ArrayList<>(Arrays.asList("build/output", "dist/"));
        job.setArtifacts(artifacts);
        List<String> returnedArtifacts = job.getArtifacts();
        returnedArtifacts.add("should not affect original");
        assertEquals(2, job.getArtifacts().size());
    }

    @Test
    void testSetAllowFailure() {
        ConfigJob job = new ConfigJob();
        assertFalse(job.getAllowFailure());
        assertFalse(job.isAllowFailure());
        job.setAllowFailure(true);
        assertTrue(job.getAllowFailure());
        assertTrue(job.isAllowFailure());
        job.setAllowFailure(false);
        assertFalse(job.getAllowFailure());
        assertFalse(job.isAllowFailure());
    }

    @Test
    void testGetEmptyCollections() {
        ConfigJob job = new ConfigJob();
        assertNotNull(job.getScript());
        assertTrue(job.getScript().isEmpty());
        assertNotNull(job.getNeeds());
        assertTrue(job.getNeeds().isEmpty());
        assertNotNull(job.getPorts());
        assertTrue(job.getPorts().isEmpty());
        assertNotNull(job.getArtifacts());
        assertTrue(job.getArtifacts().isEmpty());
    }
}