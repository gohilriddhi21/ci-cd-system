package com.example.cliserver.backend.commands.runCommand;

import com.example.cliserver.backend.model.Job;
import com.example.cliserver.backend.model.Status;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DockerContainerExecutorTest {

    public DockerContainerExecutor executor;
    private DockerClient mockDockerClient;
    private PipelineExecution mockExecution;
    private Job mockJob;

    @BeforeEach
    public void setUp() {
        executor = new DockerContainerExecutor();
        mockDockerClient = mock(DockerClient.class);
        executor.dockerClient = mockDockerClient;

        mockExecution = mock(PipelineExecution.class);
        mockJob = new Job();
        mockJob.setName("SampleJob");
        mockJob.setScript(Arrays.asList("echo Hello", "echo World"));
        mockJob.setJobStatus(Status.PENDING);
    }

    @Test
    public void testExecuteJobInContainer_ImagePullFails_UsesCache() throws Exception {
        when(mockDockerClient.pingCmd()).thenReturn(mock(PingCmd.class));
        PullImageCmd pullImageCmd = mock(PullImageCmd.class);
        when(pullImageCmd.exec(any())).thenThrow(new RuntimeException("Image pull failed"));
        when(mockDockerClient.pullImageCmd(any())).thenReturn(pullImageCmd);

        mockDockerClientSetup(true);

        boolean result = executor.executeJobInContainer(mockJob, mockExecution, "reg", "img");

        assertFalse(result);
    }

    @Test
    public void testExecuteJobInContainer_ScriptFailure() throws Exception {
        mockDockerClientSetup(false);

        boolean result = executor.executeJobInContainer(mockJob, mockExecution, "reg", "img");

        assertFalse(result);
    }

    @Test
    public void testExecuteJobInContainer_ContainerCreationFails() throws Exception {
        when(mockDockerClient.pingCmd()).thenReturn(mock(PingCmd.class));
        when(mockDockerClient.pullImageCmd(any())).thenReturn(mock(PullImageCmd.class));
        when(mockDockerClient.createContainerCmd(any())).thenThrow(new RuntimeException("Create failed"));

        boolean result = executor.executeJobInContainer(mockJob, mockExecution, "reg", "img");

        assertFalse(result);
    }

    @Test
    public void testCloseDockerClient_Success() throws IOException {
        DockerClient client = mock(DockerClient.class);
        executor.dockerClient = client;
        executor.close();

        verify(client, times(1)).close();
    }

    private void mockDockerClientSetup(boolean scriptSuccess) throws Exception {
        // Ping
        when(mockDockerClient.pingCmd()).thenReturn(mock(PingCmd.class));

        // Pull
        PullImageCmd pullCmd = mock(PullImageCmd.class);
        when(mockDockerClient.pullImageCmd(any())).thenReturn(pullCmd);
        when(pullCmd.exec(any())).thenReturn(null);

        // Container creation
        CreateContainerCmd createCmd = mock(CreateContainerCmd.class);
        StartContainerCmd startCmd = mock(StartContainerCmd.class);
        CreateContainerResponse containerResponse = mock(CreateContainerResponse.class);
        when(containerResponse.getId()).thenReturn("container123");
        when(mockDockerClient.createContainerCmd(any())).thenReturn(createCmd);
        when(createCmd.withName(any())).thenReturn(createCmd);
        when(createCmd.withHostConfig(any())).thenReturn(createCmd);
        when(createCmd.withWorkingDir(any())).thenReturn(createCmd);
        when(createCmd.withCmd(anyString())).thenReturn(createCmd);
        when(createCmd.withAttachStdout(true)).thenReturn(createCmd);
        when(createCmd.withAttachStderr(true)).thenReturn(createCmd);
        when(createCmd.exec()).thenReturn(containerResponse);
        when(mockDockerClient.startContainerCmd(any())).thenReturn(startCmd);

        // Exec script
        ExecCreateCmd createExecCmd = mock(ExecCreateCmd.class);
        ExecCreateCmdResponse createResp = mock(ExecCreateCmdResponse.class);
        when(createResp.getId()).thenReturn("execId");
        when(mockDockerClient.execCreateCmd(any())).thenReturn(createExecCmd);
        when(createExecCmd.withCmd(any())).thenReturn(createExecCmd);
        when(createExecCmd.withAttachStdout(true)).thenReturn(createExecCmd);
        when(createExecCmd.withAttachStderr(true)).thenReturn(createExecCmd);
        when(createExecCmd.exec()).thenReturn(createResp);

        ExecStartCmd startExecCmd = mock(ExecStartCmd.class);
        when(mockDockerClient.execStartCmd("execId")).thenReturn(startExecCmd);
        when(startExecCmd.exec(any())).thenReturn(mock(ExecStartResultCallback.class));

        InspectExecCmd inspectCmd = mock(InspectExecCmd.class);
        InspectExecResponse inspectResp = mock(InspectExecResponse.class);
        when(mockDockerClient.inspectExecCmd("execId")).thenReturn(inspectCmd);
        when(inspectCmd.exec()).thenReturn(inspectResp);
        when(inspectResp.getExitCodeLong()).thenReturn(scriptSuccess ? 0L : 1L);

        // Cleanup
        KillContainerCmd killCmd = mock(KillContainerCmd.class);
        RemoveContainerCmd removeCmd = mock(RemoveContainerCmd.class);
        when(mockDockerClient.killContainerCmd(any())).thenReturn(killCmd);
        when(mockDockerClient.removeContainerCmd(any())).thenReturn(removeCmd);
        when(removeCmd.withForce(true)).thenReturn(removeCmd);
        when(removeCmd.withRemoveVolumes(false)).thenReturn(removeCmd);
    }
}
