package com.example.cliserver.backend.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.cliserver.backend.utils.Constants;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for the PipelineConfig class and its inner Pipeline class.
 */
public class PipelineConfigTest {

    private PipelineConfig.Pipeline createPipelineInstance() {
        try {
            Constructor<PipelineConfig.Pipeline> cons = PipelineConfig.Pipeline.class.getDeclaredConstructor();
            cons.setAccessible(true);
            return cons.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Pipeline instance", e);
        }
    }

    @Test
    public void testSetAndGetNameValid() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        pipeline.setName("TestPipeline");
        assertEquals("TestPipeline", pipeline.getName());
    }

    @Test
    public void testSetNameInvalidType() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        assertThrows(IllegalArgumentException.class, () -> {
            pipeline.setName(123);
        });
    }

    @Test
    public void testSetAndGetRegistryValid() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        pipeline.setRegistry("DockerRegistry");
        assertEquals("DockerRegistry", pipeline.getRegistry());
    }

    @Test
    public void testSetRegistryInvalidType() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        assertThrows(IllegalArgumentException.class, () -> {
            pipeline.setRegistry(456);
        });
    }

    @Test
    public void testSetAndGetImageValid() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        pipeline.setImage("docker/image:latest");
        assertEquals("docker/image:latest", pipeline.getImage());
    }

    @Test
    public void testSetImageInvalidType() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        assertThrows(IllegalArgumentException.class, () -> {
            pipeline.setImage(789);
        });
    }

    @Test
    public void testSetAndGetUploadRepoValid() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        pipeline.setUploadRepo("repo/path");
        assertEquals("repo/path", pipeline.getUploadRepo());
    }

    @Test
    public void testSetUploadRepoInvalidType() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        assertThrows(IllegalArgumentException.class, () -> {
            pipeline.setUploadRepo(true);
        });
    }

    @Test
    public void testSetStagesNullUsesDefault() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        pipeline.setStages(null);
        List<String> expected = Constants.defaultPipelineStages;
        assertEquals(expected, pipeline.getStages());
    }

    @Test
    public void testSetStagesWithValidList() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        List<String> stages = Arrays.asList("stage1", "stage2");
        pipeline.setStages(stages);
        assertEquals(stages, pipeline.getStages());
        List<String> retrieved = pipeline.getStages();
        retrieved.add("stage3");
        assertNotEquals(retrieved, pipeline.getStages());
    }

    @Test
    public void testSetStagesWithInvalidElement() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        List<Object> stages = new ArrayList<>();
        stages.add("validStage");
        stages.add(123);
        assertThrows(IllegalArgumentException.class, () -> {
            pipeline.setStages(stages);
        });
    }

    @Test
    public void testGetStagesWhenNotSet() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        assertTrue(pipeline.getStages().isEmpty());
    }

    @Test
    public void testGetJobsWhenNotSet() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        assertTrue(pipeline.getJobs().isEmpty());
    }

    @Test
    public void testSetJobsWithUniqueJobNames() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        List<ConfigJob> configJobs = new ArrayList<>();
        ConfigJob configJob1 = new ConfigJob();
        configJob1.setName("job1");
        ConfigJob configJob2 = new ConfigJob();
        configJob2.setName("job2");
        configJobs.add(configJob1);
        configJobs.add(configJob2);

        pipeline.setJobs(configJobs);
        List<Job> jobs = pipeline.getJobs();
        assertEquals(2, jobs.size());
        for (Job j : jobs) {
            String name = j.getName();
            assertTrue(name.equals("job1") || name.equals("job2"));
        }
    }

    @Test
    public void testSetJobsWithDuplicateJobNames() {
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        List<ConfigJob> configJobs = new ArrayList<>();
        ConfigJob configJob1 = new ConfigJob();
        configJob1.setName("job1");
        ConfigJob configJob2 = new ConfigJob();
        configJob1.setName("job2");
        configJobs.add(configJob1);
        configJobs.add(configJob2);
        configJobs.add(configJob1);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pipeline.setJobs(configJobs);
        });
        String expectedMessage = "Job names must be unique";
        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void testPipelineCopyConstructor() {
        PipelineConfig.Pipeline original = createPipelineInstance();
        original.setName("Original");
        original.setRegistry("Registry1");
        original.setImage("Image1");
        original.setUploadRepo("Repo1");
        original.setStages(Arrays.asList("s1", "s2"));
        List<ConfigJob> configJobs = new ArrayList<>();
        ConfigJob configJob1 = new ConfigJob();
        configJob1.setName("job1");
        configJobs.add(configJob1);
        original.setJobs(configJobs);
        PipelineConfig.Pipeline copy = new PipelineConfig.Pipeline(original);
        assertEquals("Original", copy.getName());
        assertEquals("Registry1", copy.getRegistry());
        assertEquals("Image1", copy.getImage());
        assertEquals("Repo1", copy.getUploadRepo());
        assertEquals(original.getStages(), copy.getStages());
        List<Job> originalJobs = original.getJobs();
        List<Job> copyJobs = copy.getJobs();
        assertEquals(originalJobs.size(), copyJobs.size());
        for (int i = 0; i < originalJobs.size(); i++) {
            assertEquals(originalJobs.get(i).getName(), copyJobs.get(i).getName());
        }
    }
    @Test
    public void testPipelineConfigGetPipelineWithNullPipeline() throws Exception {
        Constructor<PipelineConfig> cons = PipelineConfig.class.getDeclaredConstructor();
        cons.setAccessible(true);
        PipelineConfig config = cons.newInstance();

        Field pipelineField = PipelineConfig.class.getDeclaredField("pipeline");
        pipelineField.setAccessible(true);
        pipelineField.set(config, null);
        assertThrows(NullPointerException.class, () -> {
            config.getPipeline();
        });
    }

    @Test
    public void testPipelineConfigGetPipelineWithNonNullPipeline() throws Exception {
        Constructor<PipelineConfig> cons = PipelineConfig.class.getDeclaredConstructor();
        cons.setAccessible(true);
        PipelineConfig config = cons.newInstance();
        PipelineConfig.Pipeline pipeline = createPipelineInstance();
        pipeline.setName("ConfigPipeline");
        pipeline.setRegistry("ConfigRegistry");
        pipeline.setImage("ConfigImage");
        pipeline.setUploadRepo("ConfigRepo");
        pipeline.setStages(Arrays.asList("stageA", "stageB"));
        List<ConfigJob> configJobs = new ArrayList<>();
        ConfigJob configJob = new ConfigJob();
        configJob.setName("jobA");
        pipeline.setJobs(configJobs);

        Field pipelineField = PipelineConfig.class.getDeclaredField("pipeline");
        pipelineField.setAccessible(true);
        pipelineField.set(config, pipeline);

        PipelineConfig.Pipeline copiedPipeline = config.getPipeline();
        assertEquals("ConfigPipeline", copiedPipeline.getName());
        assertEquals("ConfigRegistry", copiedPipeline.getRegistry());
        assertEquals("ConfigImage", copiedPipeline.getImage());
        assertEquals("ConfigRepo", copiedPipeline.getUploadRepo());
        assertEquals(pipeline.getStages(), copiedPipeline.getStages());

        List<Job> originalJobs = pipeline.getJobs();
        List<Job> copiedJobs = copiedPipeline.getJobs();
        assertEquals(originalJobs.size(), copiedJobs.size());
        for (int i = 0; i < originalJobs.size(); i++) {
            assertEquals(originalJobs.get(i).getName(), copiedJobs.get(i).getName());
        }
    }
}
