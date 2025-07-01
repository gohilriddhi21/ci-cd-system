package com.example.cliserver.backend.utils;

import com.example.cliserver.backend.model.Job;
import com.example.cliserver.backend.model.PipelineRun;
import com.example.cliserver.backend.model.Stage;
import com.example.cliserver.backend.model.Status;
import java.nio.file.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

public class PipelineUtilsTest {

    @TempDir
    public Path tempFolder;

    private List<Job> sampleJobs;
    private List<String> sampleStages;

    @BeforeEach
    public void setUp() {
        // Setup sample jobs and stages for testing
        sampleJobs = new ArrayList<>();

        Job job1 = new Job();
        job1.setName("job1");
        job1.setStage("build");
        job1.setNeeds(new ArrayList<>());
        job1.setScript(Arrays.asList("echo 'Hello'"));

        Job job2 = new Job();
        job2.setName("job2");
        job2.setStage("test");
        job2.setNeeds(Arrays.asList("job1"));
        job2.setScript(Arrays.asList("echo 'World'"));

        Job job3 = new Job();
        job3.setName("job3");
        job3.setStage("deploy");
        job3.setNeeds(Arrays.asList("job2"));
        job3.setScript(Arrays.asList("echo 'Deploy'"));

        sampleJobs.add(job1);
        sampleJobs.add(job2);
        sampleJobs.add(job3);

        sampleStages = Arrays.asList("build", "test", "deploy");
    }

    @Test
    public void testGeneratePipelineId() {
        // Test with a typical Unix-style path
        String unixPath = "/home/user/pipelines/pipeline.yaml";
        String expectedUnixId = Paths.get(unixPath).toAbsolutePath().toString().replace("/", "_").replace("\\", "_");
        assertEquals(expectedUnixId, PipelineUtils.generatePipelineId(unixPath));

        // Test with Windows-style path
        String windowsPath = "C:\\Users\\user\\pipelines\\pipeline.yaml";
        String expectedWindowsId = Paths.get(windowsPath).toAbsolutePath().toString().replace("/", "_").replace("\\", "_");
        assertEquals(expectedWindowsId, PipelineUtils.generatePipelineId(windowsPath));

        // Test with a relative path
        String relativePath = "relative/path/pipeline.yaml";
        String expectedRelativeId = Paths.get(relativePath).toAbsolutePath().toString().replace("/", "_").replace("\\", "_");
        assertEquals(expectedRelativeId, PipelineUtils.generatePipelineId(relativePath));
    }

    @Test
    public void testInitializePipelineRunReport_WithRepo() {
        // Test with a repository URL
        String repo = "https://github.com/example/repo";
        PipelineRun pipelineRun = PipelineUtils.initializePipelineRunReport(repo);

        assertEquals(repo, pipelineRun.getRepo());
        assertFalse(pipelineRun.isLocal());
        assertTrue(pipelineRun.getStartTime() > 0); // Start time should be set
    }

    @Test
    public void testInitializePipelineRunReport_LocalRepo() {
        // Test with null repo (local execution)
        PipelineRun pipelineRun = PipelineUtils.initializePipelineRunReport(null);

        assertEquals(Constants.LOCAL_REPO, pipelineRun.getRepo());
        assertTrue(pipelineRun.isLocal());
        assertTrue(pipelineRun.getStartTime() > 0); // Start time should be set
    }

    @Test
    public void testMarkAllStagesPending() {
        // Test marking all stages as pending
        List<Stage> pendingStages = PipelineUtils.markAllStagesPending(sampleStages, sampleJobs);

        // Verify number of stages
        assertEquals(3, pendingStages.size());

        // Verify all stages and their jobs are marked as PENDING
        for (Stage stage : pendingStages) {
            assertEquals(Status.PENDING, stage.getStageStatus());
            for (Job job : stage.getJobs()) {
                assertEquals(Status.PENDING, job.getJobStatus());
            }
        }

        // Verify stages are in the correct order
        assertEquals("build", pendingStages.get(0).getStageName());
        assertEquals("test", pendingStages.get(1).getStageName());
        assertEquals("deploy", pendingStages.get(2).getStageName());

        // Verify jobs are assigned to the correct stages
        assertEquals(1, pendingStages.get(0).getJobs().size());
        assertEquals("job1", pendingStages.get(0).getJobs().get(0).getName());

        assertEquals(1, pendingStages.get(1).getJobs().size());
        assertEquals("job2", pendingStages.get(1).getJobs().get(0).getName());

        assertEquals(1, pendingStages.get(2).getJobs().size());
        assertEquals("job3", pendingStages.get(2).getJobs().get(0).getName());
    }

    @Test
    public void testGetTopologicallySortedJobs() {
        // Test topological sorting with linear dependencies
        List<Job> sortedJobs = PipelineUtils.getTopologicallySortedJobs(sampleJobs);

        // Verify correct order
        assertEquals(3, sortedJobs.size());
        assertEquals("job1", sortedJobs.get(0).getName()); // No dependencies, should be first
        assertEquals("job2", sortedJobs.get(1).getName()); // Depends on job1
        assertEquals("job3", sortedJobs.get(2).getName()); // Depends on job2
    }

    @Test
    public void testGetTopologicallySortedJobs_ComplexDependencies() {
        // Create a more complex dependency graph:
        // job1 <-- job3
        //  ^       ^
        //  |       |
        // job2 <-- job4
        //  ^
        //  |
        // job5

        List<Job> complexJobs = new ArrayList<>();

        Job job1 = new Job();
        job1.setName("job1");
        job1.setNeeds(new ArrayList<>());

        Job job2 = new Job();
        job2.setName("job2");
        job2.setNeeds(Arrays.asList("job1"));

        Job job3 = new Job();
        job3.setName("job3");
        job3.setNeeds(Arrays.asList("job1", "job4"));

        Job job4 = new Job();
        job4.setName("job4");
        job4.setNeeds(Arrays.asList("job2"));

        Job job5 = new Job();
        job5.setName("job5");
        job5.setNeeds(Arrays.asList("job2"));

        // Add jobs out of order
        complexJobs.add(job3);
        complexJobs.add(job5);
        complexJobs.add(job1);
        complexJobs.add(job4);
        complexJobs.add(job2);

        List<Job> sortedJobs = PipelineUtils.getTopologicallySortedJobs(complexJobs);

        // Verify dependency order is maintained
        Map<String, Integer> positions = new HashMap<>();
        for (int i = 0; i < sortedJobs.size(); i++) {
            positions.put(sortedJobs.get(i).getName(), i);
        }

        // Check that dependencies come before dependents
        assertTrue(positions.get("job1") < positions.get("job2"));
        assertTrue(positions.get("job2") < positions.get("job4"));
        assertTrue(positions.get("job2") < positions.get("job5"));
        assertTrue(positions.get("job1") < positions.get("job3"));
        assertTrue(positions.get("job4") < positions.get("job3"));
    }

    @Test
    public void testGetTopologicallySortedJobs_WithCycle() {
        // Create jobs with a cyclic dependency: job1 -> job2 -> job3 -> job1
        Job job1 = new Job();
        job1.setName("job1");
        job1.setNeeds(Arrays.asList("job3"));

        Job job2 = new Job();
        job2.setName("job2");
        job2.setNeeds(Arrays.asList("job1"));

        Job job3 = new Job();
        job3.setName("job3");
        job3.setNeeds(Arrays.asList("job2"));

        List<Job> cycleJobs = Arrays.asList(job1, job2, job3);


        assertThrows(IllegalArgumentException.class, () -> {
            PipelineUtils.getTopologicallySortedJobs(cycleJobs);
        });
    }

    @Test
    public void testCheckFieldIsString_ValidString() {
        // Should not throw exception
        PipelineUtils.checkFieldIsString("test", "fieldName");
    }

    @Test
    public void testCheckFieldIsString_Integer() {
        // Should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            PipelineUtils.checkFieldIsString(123, "fieldName");
        });
    }

    @Test
    public void testCheckFieldIsString_List() {
        // Should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            PipelineUtils.checkFieldIsString(Arrays.asList("item"), "fieldName");
        });
    }

    @Test
    public void testHasCyclicDependency_NoCycle() {
        // Test with acyclic dependencies
        Set<String> cycle = PipelineUtils.hasCyclicDependency(sampleJobs);
        assertTrue(cycle.isEmpty());
    }

    @Test
    public void testHasCyclicDependency_WithCycle() {
        // Create jobs with a cyclic dependency
        Job job1 = new Job();
        job1.setName("job1");
        job1.setNeeds(Arrays.asList("job3"));

        Job job2 = new Job();
        job2.setName("job2");
        job2.setNeeds(Arrays.asList("job1"));

        Job job3 = new Job();
        job3.setName("job3");
        job3.setNeeds(Arrays.asList("job2"));

        List<Job> cycleJobs = Arrays.asList(job1, job2, job3);

        Set<String> cycle = PipelineUtils.hasCyclicDependency(cycleJobs);

        // Verify cycle was detected
        assertFalse(cycle.isEmpty());
        assertTrue(cycle.contains("job1"));
        assertTrue(cycle.contains("job2"));
        assertTrue(cycle.contains("job3"));
    }

    @Test
    public void testHasCyclicDependency_SelfReferential() {
        // Test with a job that depends on itself
        Job job = new Job();
        job.setName("job1");
        job.setNeeds(Arrays.asList("job1"));

        List<Job> jobs = Collections.singletonList(job);

        Set<String> cycle = PipelineUtils.hasCyclicDependency(jobs);

        // Verify cycle was detected
        assertFalse(cycle.isEmpty());
        assertTrue(cycle.contains("job1"));
    }

    @Test
    public void testBuildJobGraph() {
        // Test building the job dependency graph
        Map<String, List<String>> graph = PipelineUtils.buildJobGraph(sampleJobs);

        // Verify graph structure
        assertEquals(3, graph.size());
        assertTrue(graph.containsKey("job1"));
        assertTrue(graph.containsKey("job2"));
        assertTrue(graph.containsKey("job3"));

        assertTrue(graph.get("job1").isEmpty());
        assertEquals(1, graph.get("job2").size());
        assertEquals("job1", graph.get("job2").get(0));
        assertEquals(1, graph.get("job3").size());
        assertEquals("job2", graph.get("job3").get(0));
    }

    @Test
    public void testDfs_NoCycle() {
        // Test DFS with no cycle
        Map<String, List<String>> graph = PipelineUtils.buildJobGraph(sampleJobs);

        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();
        Set<String> cycle = new HashSet<>();
        List<Job> sorted = new ArrayList<>();

        Map<String, Job> jobMap = new HashMap<>();
        for (Job job : sampleJobs) {
            jobMap.put(job.getName(), job);
        }

        // Run DFS starting from job3
        boolean hasCycle = PipelineUtils.dfs("job3", graph, visited, stack, cycle, sorted, jobMap);

        // Verify no cycle was detected
        assertFalse(hasCycle);
        assertEquals(3, sorted.size()); // All jobs should be added to sorted list
    }

    @Test
    public void testDfs_WithCycle() {
        // Create jobs with a cyclic dependency
        Job job1 = new Job();
        job1.setName("job1");
        job1.setNeeds(Arrays.asList("job3"));

        Job job2 = new Job();
        job2.setName("job2");
        job2.setNeeds(Arrays.asList("job1"));

        Job job3 = new Job();
        job3.setName("job3");
        job3.setNeeds(Arrays.asList("job2"));

        List<Job> cycleJobs = Arrays.asList(job1, job2, job3);
        Map<String, List<String>> graph = PipelineUtils.buildJobGraph(cycleJobs);

        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();
        Set<String> cycle = new HashSet<>();
        List<Job> sorted = new ArrayList<>();

        Map<String, Job> jobMap = new HashMap<>();
        for (Job job : cycleJobs) {
            jobMap.put(job.getName(), job);
        }

        // Run DFS starting from job1
        boolean hasCycle = PipelineUtils.dfs("job1", graph, visited, stack, cycle, sorted, jobMap);

        // Verify cycle was detected
        assertTrue(hasCycle);
        assertTrue(cycle.contains("job1"));
        assertTrue(cycle.contains("job2"));
        assertTrue(cycle.contains("job3"));
    }

    @Test
    public void testDeleteFile() throws IOException {
        // Create a temporary directory structure
        Path rootDir = tempFolder.resolve("testDir");
        Files.createDirectory(rootDir);

        Path subDirPath = rootDir.resolve("subDir");
        Files.createDirectory(subDirPath);

        Path file1Path = rootDir.resolve("file1.txt");
        Path file2Path = subDirPath.resolve("file2.txt");

        Files.createFile(file1Path);
        Files.createFile(file2Path);

        // Convert to File objects for verification
        File rootDirFile = rootDir.toFile();
        File subDirFile = subDirPath.toFile();
        File file1 = file1Path.toFile();
        File file2 = file2Path.toFile();

        // Verify files exist
        assertTrue(file1.exists());
        assertTrue(file2.exists());

        // Delete the root directory
        PipelineUtils.deleteFile(rootDirFile);

        // Verify files and directories are deleted
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertFalse(subDirFile.exists());
        assertFalse(rootDirFile.exists());
    }

    @Test
    public void testDeleteFile_FailToDelete() throws IOException {
        // Mock a file that cannot be deleted
        File mockFile = mock(File.class);
        when(mockFile.isDirectory()).thenReturn(false);
        when(mockFile.delete()).thenReturn(false);
        when(mockFile.getName()).thenReturn("test.txt");
        when(mockFile.getAbsolutePath()).thenReturn("/path/to/test.txt");

        // Should throw IOException
        assertThrows(IOException.class, () -> {
            PipelineUtils.deleteFile(mockFile);
        });

    }

    @Test
    public void testDeleteFile_EmptyDirectory() throws IOException {
        // Create an empty directory
        Path emptyDirPath = tempFolder.resolve("emptyDir");
        Files.createDirectory(emptyDirPath);
        File emptyDir = emptyDirPath.toFile();

        // Verify directory exists
        assertTrue(emptyDir.exists());

        // Delete the directory
        PipelineUtils.deleteFile(emptyDir);

        // Verify directory is deleted
        assertFalse(emptyDir.exists());
    }
}

