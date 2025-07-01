package com.example.cliserver.backend.commands.runCommand;

import static com.example.cliserver.backend.utils.Constants.DOCKER_EXEC_TIMEOUT_SECONDS;
import static com.example.cliserver.backend.utils.Constants.WORKSPACE_MOUNT_PATH;

import com.example.cliserver.backend.model.Job;
import com.example.cliserver.backend.utils.YamlConfigLoader;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Executes pipeline jobs inside Docker containers using the
 * docker-java library. This class handles container creation,
 * execution of commands, and container cleanup.
 */
public class DockerContainerExecutor {

    /**
     * docker client object
     */
    public DockerClient dockerClient;
    /**
     * default constructor
     */
    public DockerContainerExecutor(){}

    /**
     * Creates and initializes a Docker client based on
     * the system configuration.
     *
     * @param dockerRegistry The docker registry for this docker client
     *
     * @return A configured DockerClient instance
     */
    private DockerClient initializeDockerClient(String dockerRegistry) {
        DockerClientConfig config =
                DefaultDockerClientConfig.createDefaultConfigBuilder()
                        .withRegistryUrl(dockerRegistry)
                        .withDockerTlsVerify(false)
                        .withDockerHost(YamlConfigLoader.getConfigValue("docker", "host"))
                        .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        return DockerClientImpl.getInstance(config, httpClient);
    }


    /**
     * Executes a pipeline job in a Docker container.
     *
     * @param job       The job configuration to execute
     * @param execution The pipeline execution context for logging
     * @param dockerRegistry The docker registry to pull the docker image from
     * @param dockerImage The docker image to use to execute the job
     * @return true if the job executed successfully, false otherwise
     */
    @SuppressFBWarnings("PA_PUBLIC_PRIMITIVE_ATTRIBUTE")
    public boolean executeJobInContainer(Job job, PipelineExecution execution,
                                         String dockerRegistry, String dockerImage) {
        String containerId = null;
        boolean jobSuccess = false;
        final long startTime = System.currentTimeMillis();

        // Initialize a Docker client if not already done
        if (dockerClient == null) {
            dockerClient = initializeDockerClient(dockerRegistry);
        }

        execution.log("--- Starting job: " + job.getName() + " ---");
        execution.log("Using image: " + dockerImage);

        try {
            // Verify Docker daemon is running and accessible
            execution.log("Starting the Docker daemon..");
            dockerClient.pingCmd().exec();
            execution.log("Successfully connected to Docker daemon");

            // Pull the specified image
            execution.log(
                    "Pulling Docker image: " + dockerImage +
                            " (this may take a moment)");
            try {
                dockerClient.pullImageCmd(dockerImage)
                        .exec(new PullImageResultCallback())
                        .awaitCompletion(60, TimeUnit.SECONDS);
                execution.log("Image pulled successfully");
            } catch (Exception e) {
                execution.log("Warning: Failed to pull latest image. Will attempt "
                        + "to use cached version if available.");
            }

            // Prepare container configuration
            containerId = createAndStartContainer(job, execution, dockerImage);
            if (containerId == null) {
                execution.log("Failed to create container");
                return false;
            }

            // Execute each script command in the container
            jobSuccess = executeScriptsInContainer(job, containerId, execution);

        } catch (Exception e) {
            execution.log("Error during Docker execution: " + e.getMessage());
            jobSuccess = false;
            return false;
        } finally {
            // Clean up the container
            if (containerId != null) {
                cleanupContainer(containerId, execution);
            }

            long duration = System.currentTimeMillis() - startTime;
            String status = jobSuccess ?
                    "succeeded" :
                    (job.isAllowFailure() ? "failed but allowed" : "failed");
            execution.log("--- Job " + job.getName() + " " +
                    status + " (took " + duration + "ms) ---");
        }

        // Return true if the job succeeded or if failures are allowed
        return jobSuccess;
    }

    /**
     * Creates and starts a Docker container for a job.
     *
     * @param job       The job configuration
     * @param execution The pipeline execution context for logging
     * @param dockerImage The docker image to create the container from
     * @return The container ID if successful, null otherwise
     */
    private String createAndStartContainer(Job job, PipelineExecution execution,
                                           String dockerImage) {
        try {
            // Get the project directory to mount in the container
            String projectDir = System.getenv("PROJECT_DIR");
            execution.log("Mounting directory: " + projectDir + " to "
                    + WORKSPACE_MOUNT_PATH + " in container");

            // Create volume configuration
            Volume appVolume = new Volume(WORKSPACE_MOUNT_PATH);
            List<Bind> binds = new ArrayList<>();
            binds.add(new Bind(projectDir, appVolume));

            // Handle ports if specified
            List<ExposedPort> exposedPorts = new ArrayList<>();
            Ports portBindings = new Ports();

            List<String> jobPorts = job.getPorts();
            if (jobPorts != null && !jobPorts.isEmpty()) {
                execution.log("Configuring ports: " + String.join(", ", jobPorts));
                for (String port : jobPorts) {
                    try {
                        int portNum = Integer.parseInt(port);
                        ExposedPort exposedPort = ExposedPort.tcp(portNum);
                        exposedPorts.add(exposedPort);
                        portBindings.bind(exposedPort, Ports.Binding.bindPort(portNum));
                    } catch (NumberFormatException e) {
                        execution.log("Warning: Invalid port number: " + port + ", skipping");
                    }
                }
            }

            // Create host configuration
            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withBinds(binds)
                    .withPortBindings(portBindings);

            execution.log("Creating container from image: " + dockerImage);

            // Create a unique container name based on job name
            String containerName = "pipeline-" +
                    job.getName().toLowerCase().replaceAll("[^a-z0-9]", "-")
                    + "-" + UUID.randomUUID().toString().substring(0, 8);

            // Create a container
            CreateContainerResponse containerResponse =
                    dockerClient.createContainerCmd(dockerImage)
                            .withName(containerName)
                            .withHostConfig(hostConfig)
                            .withExposedPorts(exposedPorts)
                            .withWorkingDir(WORKSPACE_MOUNT_PATH)
                            .withCmd("tail", "-f", "/dev/null")
                            .withAttachStdout(true)
                            .withAttachStderr(true)
                            .exec();

            String containerId = containerResponse.getId();
            execution.log("Container created: " + containerId.substring(0, 12)
                    + " (name: " + containerName + ")");

            // Start the container
            execution.log("Starting container...");
            dockerClient.startContainerCmd(containerId).exec();
            execution.log("Container started successfully");

            return containerId;
        } catch (Exception e) {
            execution.log("Error creating/starting container: " + e.getMessage());
            return null;
        }
    }

    /**
     * Executes the script commands for a job in the specified container.
     *
     * @param job         The job configuration
     * @param containerId The Docker container ID
     * @param execution   The pipeline execution context for logging
     * @return true if all scripts executed successfully, false otherwise
     */
    private boolean executeScriptsInContainer(
            Job job,
            String containerId,
            PipelineExecution execution
    ) {
        List<String> scripts = job.getScript();
        execution.log("Executing " + scripts.size() + " script" + (scripts.size() != 1 ? "s" : ""));

        // Create a script file that sets up logging for each command
        StringBuilder scriptContent = new StringBuilder("#!/bin/sh\nset -e\n\n");


        // Add echo statements before each command to show what's being executed
        for (int i = 0; i < scripts.size(); i++) {
            String script = scripts.get(i);
            String scriptNumber = (i + 1) + "/" + scripts.size();

            // Add logging for the script execution
            scriptContent.append("echo '[PIPELINE] Running script " + scriptNumber
                    + ": " + script.replace("'", "'\\''") + "'\n");
            // Add the actual script command
            scriptContent.append(script).append("\n");

            // Add separator for readability in logs
            scriptContent.append("echo \"[PIPELINE] Script " + scriptNumber +
                    " completed with exit code $?\"\n");
            scriptContent.append("echo \"----------------------------------------\"\n\n");
        }

        String combinedScript = scriptContent.toString();
        execution.log("Preparing combined job script with " + scripts.size() + " commands");

        try {
            // Create temp script file in container
            String scriptPath = "/tmp/job_script_" +
                    UUID.randomUUID().toString().substring(0, 8) + ".sh";

            // Write script to container
            ExecCreateCmdResponse writeCmd = dockerClient.execCreateCmd(containerId)
                    .withCmd("sh", "-c", "cat > " + scriptPath + " << 'EOFSCRIPT'\n"
                            + combinedScript + "\nEOFSCRIPT\nchmod +x " + scriptPath)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            // Execute the write command
            dockerClient.execStartCmd(writeCmd.getId())
                    .exec(new ExecStartResultCallback())
                    .awaitCompletion(10, TimeUnit.SECONDS);

            // Execute the combined script
            ExecCreateCmdResponse execResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("sh", "-c", scriptPath)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .exec();

            final StringBuilder outputBuffer = new StringBuilder();

            // Execute the command and collect output
            dockerClient.execStartCmd(execResponse.getId())
                    .exec(new ExecStartResultCallback() {
                        @Override
                        public void onNext(Frame item) {
                            String output = new String(
                                    item.getPayload(),
                                    StandardCharsets.UTF_8).trim();
                            if (!output.isEmpty()) {
                                execution.log(output);
                                outputBuffer.append(output).append("\n");
                            }
                        }
                    })
                    .awaitCompletion(DOCKER_EXEC_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // Check exit code
            InspectExecResponse inspectResponse =
                    dockerClient.inspectExecCmd(execResponse.getId()).exec();
            Long exitCode = inspectResponse.getExitCodeLong();

            if (exitCode == null) {
                execution.log("Warning: Unable to determine exit code for combined script");
                return false;
            }

            if (exitCode == 0) {
                execution.log("All scripts completed successfully");
                return true;
            } else {
                execution.log("Script execution failed with exit code: " + exitCode);
                return false;
            }
        } catch (Exception e) {
            execution.log("Error executing script: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cleans up a Docker container by stopping and removing it.
     *
     * @param containerId The Docker container ID to clean up
     * @param execution   The pipeline execution context for logging
     */
    private void cleanupContainer(String containerId, PipelineExecution execution) {
        try {
            // Try to get a short ID for logging
            String shortId = containerId.length() > 12 ?
                    containerId.substring(0, 12) :
                    containerId;

            execution.log("Attempting to stop container: " + shortId);
            dockerClient.killContainerCmd(containerId).exec();
            execution.log("Container killed: " + shortId);

            execution.log("Removing container: " + shortId);
            dockerClient.removeContainerCmd(containerId)
                    .withForce(true)
                    .withRemoveVolumes(false)
                    .exec();

            execution.log("Container cleanup complete");
        } catch (Exception e) {
            execution.log("Error cleaning up container: " + e.getMessage());
        }
    }

    /**
     * Closes the Docker client and releases resources.
     * This method should be called when the executor is no longer needed.
     */
    public void close() {
        if (dockerClient != null) {
            try {
                dockerClient.close();
            } catch (IOException e) {
                System.err.println("Error closing Docker client: " + e.getMessage());
            } finally {
                dockerClient = null;
            }
        }
    }
}