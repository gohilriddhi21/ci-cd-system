

package com.example.cliserver.backend.commands.runCommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.cliserver.backend.database.artifactsDB.ArtifactsUploader;
import com.example.cliserver.backend.database.mongoDB.PipelineRunsDao;
import com.example.cliserver.backend.model.Job;
import com.example.cliserver.backend.model.PipelineRun;
import com.example.cliserver.backend.model.Stage;
import com.example.cliserver.backend.model.Status;
import com.example.cliserver.controller.SseController;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class PipelineRunnerTest {

    private PipelineRunner pipelineRunner;
    private PipelineRunsDao pipelineRunsDaoMock;
    private DockerContainerExecutor dockerExecutorMock;
    private ArtifactsUploader uploaderMock;
    private SseEmitter sseEmitterMock;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        pipelineRunsDaoMock = mock(PipelineRunsDao.class);
        pipelineRunner = new PipelineRunner(pipelineRunsDaoMock);
        dockerExecutorMock = mock(DockerContainerExecutor.class);
        Field dockerField = PipelineRunner.class.getDeclaredField("dockerContainerExecutor");
        dockerField.setAccessible(true);
        dockerField.set(pipelineRunner, dockerExecutorMock);
        uploaderMock = mock(ArtifactsUploader.class);
        Field uploaderField = PipelineRunner.class.getDeclaredField("uploader");
        uploaderField.setAccessible(true);
        uploaderField.set(pipelineRunner, uploaderMock);
        sseEmitterMock = mock(SseEmitter.class);
        try {
            Field sseControllerField = SseController.class.getDeclaredField("currentEmitter");
            sseControllerField.setAccessible(true);
            sseControllerField.set(null, sseEmitterMock);
        } catch (NoSuchFieldException e) {
            System.out.println("No currentEmitter field found in SseController: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        pipelineRunner.shutdownExecutor();
        pipelineRunner.activeExecutions.clear();
        try {
            Field sseControllerField = SseController.class.getDeclaredField("currentEmitter");
            sseControllerField.setAccessible(true);
            sseControllerField.set(null, null);
        } catch (Exception e) {

            System.out.println("Failed to clean up SseController: " + e.getMessage());
        }
    }
    @Test
    public void testRunPipelineDuplicateExecution() {
        PipelineRun pr = new PipelineRun();
        pr.setFileName("pipeline.yaml");
        pr.setRunNumber(1);
        pr.setPipelineName("TestPipeline");
        pr.setStages(new ArrayList<>());
        String result1 = pipelineRunner.runPipeline(pr, false);
        assertEquals("TestPipeline run: 1", result1);

        String result2 = pipelineRunner.runPipeline(pr, false);
        assertEquals("Duplicate pipeline execution detected. Using existing execution.", result2);
        assertEquals(Status.CANCELED, pr.getPipelineStatus());
        verify(pipelineRunsDaoMock, atLeast(1)).updatePipelineRun(pr);
    }

    @Test
    public void testRunPipelineNormalSuccess() throws Exception {
        Job job = new Job();
        job.setName("Job1");
        job.setAllowFailure(false);
        job.setArtifacts(Arrays.asList("artifact1"));
        job.setJobStatus(Status.PENDING);
        Stage stage = new Stage();
        stage.setStageName("Build");
        stage.setJobs(Collections.singletonList(job));
        PipelineRun pr = new PipelineRun();
        pr.setFileName("pipeline.yaml");
        pr.setRunNumber(2);
        pr.setPipelineName("TestPipeline");
        pr.setRegistry("registry");
        pr.setImage("image");
        pr.setUploadRepo("uploadRepo");
        pr.setStages(Collections.singletonList(stage));
        when(dockerExecutorMock.executeJobInContainer(eq(job), any(), anyString(), anyString()))
            .thenReturn(true);

        doNothing().when(uploaderMock).uploadArtifacts(anyString(), eq(job.getArtifacts()));

        String result = pipelineRunner.runPipeline(pr, true);
        assertEquals("TestPipeline run: 2", result);
        assertEquals(Status.SUCCESS, pr.getPipelineStatus());
        verify(pipelineRunsDaoMock, atLeast(1)).updatePipelineRun(pr);
    }

    @Test
    public void testRunPipelineJobFailureAllowed() throws Exception {
        Job job = new Job();
        job.setName("JobAllowedFail");
        job.setAllowFailure(true);
        job.setArtifacts(Arrays.asList("artifact2"));
        job.setJobStatus(Status.PENDING);

        Stage stage = new Stage();
        stage.setStageName("Test");
        stage.setJobs(Collections.singletonList(job));

        PipelineRun pr = new PipelineRun();
        pr.setFileName("pipeline.yaml");
        pr.setRunNumber(3);
        pr.setPipelineName("TestPipeline");
        pr.setRegistry("reg");
        pr.setImage("img");
        pr.setUploadRepo("repo");
        pr.setStages(Collections.singletonList(stage));

        when(dockerExecutorMock.executeJobInContainer(eq(job), any(), anyString(), anyString()))
            .thenReturn(false);
        doNothing().when(uploaderMock).uploadArtifacts(anyString(), eq(job.getArtifacts()));

        String result = pipelineRunner.runPipeline(pr, false);
        assertEquals("TestPipeline run: 3", result);
        assertEquals(Status.SUCCESS, pr.getPipelineStatus());
        verify(uploaderMock).uploadArtifacts(anyString(), eq(job.getArtifacts()));
    }

    @Test
    public void testRunPipelineJobFailureNotAllowed() throws Exception {
        Job job = new Job();
        job.setName("JobNotAllowedFail");
        job.setAllowFailure(false);
        job.setArtifacts(new ArrayList<>());
        job.setJobStatus(Status.PENDING);

        Stage stage = new Stage();
        stage.setStageName("Deploy");
        stage.setJobs(Collections.singletonList(job));

        PipelineRun pr = new PipelineRun();
        pr.setFileName("pipeline.yaml");
        pr.setRunNumber(4);
        pr.setPipelineName("TestPipeline");
        pr.setRegistry("reg");
        pr.setImage("img");
        pr.setUploadRepo("repo");
        pr.setStages(Collections.singletonList(stage));

        when(dockerExecutorMock.executeJobInContainer(eq(job), any(), anyString(), anyString()))
            .thenReturn(false);

        String result = pipelineRunner.runPipeline(pr, true);
        assertEquals("TestPipeline run: 4", result);
        assertEquals(Status.FAILED, pr.getPipelineStatus());
    }
    @Test
    public void testExecuteJobArtifactsFailure() throws Exception {
        Job job = new Job();
        job.setName("JobArtifactFail");
        job.setAllowFailure(false);
        job.setArtifacts(Arrays.asList("artifact3"));
        job.setJobStatus(Status.PENDING);

        Stage stage = new Stage();
        stage.setStageName("Package");
        stage.setJobs(Collections.singletonList(job));

        PipelineRun pr = new PipelineRun();
        pr.setFileName("pipeline.yaml");
        pr.setRunNumber(5);
        pr.setPipelineName("TestPipeline");
        pr.setRegistry("reg");
        pr.setImage("img");
        pr.setUploadRepo("repo");
        pr.setStages(Collections.singletonList(stage));
        when(dockerExecutorMock.executeJobInContainer(eq(job), any(), anyString(), anyString()))
            .thenReturn(true);
        doThrow(new RuntimeException("Upload failed")).when(uploaderMock)
            .uploadArtifacts(anyString(), eq(job.getArtifacts()));

        String result = pipelineRunner.runPipeline(pr, false);
        assertEquals("TestPipeline run: 5", result);
        assertEquals(Status.FAILED, pr.getPipelineStatus());
    }

    @Test
    public void testShutdownExecutor() {
        pipelineRunner.shutdownExecutor();
    }

    @Test
    public void testStageWithAllPendingJobs() throws Exception {
        Job job1 = new Job();
        job1.setName("Job1");
        job1.setJobStatus(Status.PENDING);
        job1.setAllowFailure(false);

        Stage stage = new Stage();
        stage.setStageName("PendingStage");
        stage.setJobs(Collections.singletonList(job1));

        PipelineRun pr = new PipelineRun();
        pr.setFileName("pipeline.yaml");
        pr.setRunNumber(6);
        pr.setPipelineName("TestPipeline");
        pr.setRegistry("reg");
        pr.setImage("img");
        pr.setUploadRepo("repo");
        pr.setStages(Collections.singletonList(stage));

        when(dockerExecutorMock.executeJobInContainer(eq(job1), any(), anyString(), anyString()))
            .thenReturn(true);

        String result = pipelineRunner.runPipeline(pr, false);
        assertEquals("TestPipeline run: 6", result);
        assertEquals(Status.SUCCESS, pr.getPipelineStatus());
    }

    @Test
    public void testJobFailureAllowed_NoArtifacts() throws Exception {
        Job job = new Job();
        job.setName("AllowFailJob");
        job.setAllowFailure(true);
        job.setArtifacts(Collections.emptyList());
        job.setJobStatus(Status.PENDING);

        Stage stage = new Stage();
        stage.setStageName("OptionalStage");
        stage.setJobs(Collections.singletonList(job));

        PipelineRun pr = new PipelineRun();
        pr.setFileName("pipeline.yaml");
        pr.setRunNumber(7);
        pr.setPipelineName("TestPipeline");
        pr.setRegistry("reg");
        pr.setImage("img");
        pr.setUploadRepo("repo");
        pr.setStages(Collections.singletonList(stage));

        when(dockerExecutorMock.executeJobInContainer(eq(job), any(), anyString(), anyString()))
            .thenReturn(false); // Simulate job failure

        String result = pipelineRunner.runPipeline(pr, false);
        assertEquals("TestPipeline run: 7", result);
        assertEquals(Status.SUCCESS, pr.getPipelineStatus());
    }

    @Test
    public void testArtifactUploadFailureAfterSuccessJob() throws Exception {
        Job job = new Job();
        job.setName("ArtifactJob");
        job.setAllowFailure(false);
        job.setArtifacts(Arrays.asList("artifactX"));
        job.setJobStatus(Status.PENDING);

        Stage stage = new Stage();
        stage.setStageName("StageWithArtifact");
        stage.setJobs(Collections.singletonList(job));

        PipelineRun pr = new PipelineRun();
        pr.setFileName("pipeline.yaml");
        pr.setRunNumber(8);
        pr.setPipelineName("TestPipeline");
        pr.setRegistry("reg");
        pr.setImage("img");
        pr.setUploadRepo("repo");
        pr.setStages(Collections.singletonList(stage));

        when(dockerExecutorMock.executeJobInContainer(eq(job), any(), anyString(), anyString()))
            .thenReturn(true); // Job success
        doThrow(new RuntimeException("Upload failed")).when(uploaderMock)
            .uploadArtifacts(anyString(), eq(job.getArtifacts())); // Upload fails

        String result = pipelineRunner.runPipeline(pr, false);
        assertEquals("TestPipeline run: 8", result);
        assertEquals(Status.FAILED, pr.getPipelineStatus());
    }

    @Test
    public void testJobUsesCustomImageAndRegistry() throws Exception {
        Job job = new Job();
        job.setName("CustomJob");
        job.setAllowFailure(false);
        job.setRegistry("custom.registry");
        job.setImage("custom/image");
        job.setArtifacts(Arrays.asList("artifactX"));
        job.setJobStatus(Status.PENDING);

        Stage stage = new Stage();
        stage.setStageName("CustomStage");
        stage.setJobs(Collections.singletonList(job));

        PipelineRun pr = new PipelineRun();
        pr.setFileName("pipeline.yaml");
        pr.setRunNumber(9);
        pr.setPipelineName("TestPipeline");
        pr.setRegistry("default.registry");
        pr.setImage("default/image");
        pr.setUploadRepo("repo");
        pr.setStages(Collections.singletonList(stage));

        when(dockerExecutorMock.executeJobInContainer(eq(job), any(), eq("custom.registry"), eq("custom/image")))
            .thenReturn(true);
        doNothing().when(uploaderMock).uploadArtifacts(anyString(), eq(job.getArtifacts()));

        String result = pipelineRunner.runPipeline(pr, false);
        assertEquals("TestPipeline run: 9", result);
        assertEquals(Status.SUCCESS, pr.getPipelineStatus());
    }

    @Test
    public void testExecutePipelineThrowsException() throws Exception {
        Job job = mock(Job.class);
        when(job.getName()).thenReturn("ExplodingJob");
        when(job.getArtifacts()).thenReturn(Collections.emptyList());
        when(job.getJobStatus()).thenReturn(Status.PENDING);
        when(job.isAllowFailure()).thenReturn(false);
        when(dockerExecutorMock.executeJobInContainer(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Simulated crash"));

        Stage stage = new Stage();
        stage.setStageName("ExplodingStage");
        stage.setJobs(Collections.singletonList(job));

        PipelineRun pr = new PipelineRun();
        pr.setFileName("pipeline.yaml");
        pr.setRunNumber(10);
        pr.setPipelineName("ExplodingPipeline");
        pr.setRegistry("reg");
        pr.setImage("img");
        pr.setUploadRepo("repo");
        pr.setStages(Collections.singletonList(stage));

        String result = pipelineRunner.runPipeline(pr, false);
        assertEquals("ExplodingPipeline run: 10", result);
    }



}

