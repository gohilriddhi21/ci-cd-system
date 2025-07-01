package com.example.cliserver.backend.utils;

import com.example.cliserver.backend.model.Job;
import com.example.cliserver.backend.model.PipelineRun;
import com.example.cliserver.backend.model.Stage;
import com.example.cliserver.backend.model.Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for pipeline-related helper methods.
 * <p>
 * This class provides utility methods that assist in the handling of pipeline-related tasks,
 * such as generating valid pipeline IDs from file paths.
 * </p>
 */
public class PipelineUtils {
    private PipelineUtils(){}

    /**
     * Generates a valid pipeline ID from a file path.
     * <p>
     * - Converts an absolute path to a safe filename by replacing slashes.
     *
     * @param filePath Path to the pipeline YAML file.
     * @return A valid filename-friendly ID.
     */
    public static String generatePipelineId(String filePath) {
        String absPath = Paths.get(filePath).toAbsolutePath().toString();
        return absPath.replace("/", "_").replace("\\", "_");
    }


    /**
     * Creates and initializes a new PipelineRun object with basic information.
     *
     * @param repo The repository identifier, or null for local execution
     * @return A newly initialized PipelineRun object with start time and repo information
     */
    public static PipelineRun initializePipelineRunReport(String repo) {
        PipelineRun pipelineRunReport = new PipelineRun();
        pipelineRunReport.setStartTime(new Date().getTime());
        if (repo == null) {
            pipelineRunReport.setRepo(Constants.LOCAL_REPO);
            pipelineRunReport.setLocal(true);
        } else {
            pipelineRunReport.setRepo(repo);
            pipelineRunReport.setLocal(false);
        }
        return pipelineRunReport;
    }

    /**
     * Creates a list of Stage objects with PENDING status from stage names and jobs.
     *
     * @param stages List of stage names from the pipeline configuration
     * @param jobs List of all jobs defined in the pipeline configuration
     * @return List of Stage objects with jobs assigned and status set to PENDING
     */
    public static List<Stage> markAllStagesPending(List<String> stages, List<Job> jobs) {
        // Initialize all stages with Pending status before execution starts
        List<Stage> pendingStages = new ArrayList<>();
        for (String stageName : stages) {
            Stage stage = new Stage();
            stage.setStageName(stageName);
            stage.setStageStatus(Status.PENDING);

            // Get jobs for this stage
            List<Job> stageJobs = jobs.stream()
                    .filter(job -> job.getStage().equals(stageName))
                    .collect(Collectors.toList());

            // Get topologically sorted jobs - same as in executePipeline
            List<Job> sortedJobs =
                    PipelineUtils.getTopologicallySortedJobs(stageJobs);

            for (Job configJob : sortedJobs) {
                configJob.setJobStatus(Status.PENDING);
            }

            stage.setJobs(sortedJobs);
            pendingStages.add(stage);
        }
        return pendingStages;
    }

    /**
     * Retrieves jobs sorted topologically based on their dependencies.
     * Used by both `ConfigurationValidator` and `PipelineRunner`.
     *
     * @param jobs the {@link Job} list of object to validate
     * @return list of topologically sorted jobs
     */
    public static List<Job> getTopologicallySortedJobs(List<Job> jobs) {
        // Build dependency graph
        Map<String, List<String>> graph = jobs.stream()
                .collect(Collectors.toMap(
                        Job::getName,
                        Job::getNeeds
                ));

        List<Job> sorted = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();
        Set<String> cycle = new HashSet<>();
        Map<String,Job> jobMap = jobs.stream()
                .collect(Collectors.toMap(Job::getName, job -> job));

        // Traverse jobs with DFS
        for (Job job : jobs) {
            if (!visited.contains(job.getName())) {
                if (dfs(job.getName(), graph, visited, stack, cycle, sorted, jobMap)) {
                    throw new IllegalArgumentException("Cycle detected in jobs: " + cycle);
                }
            }
        }
        return sorted;
    }

    /**
     * Validates that given field is a String otherwise throws an IllegalArgumentException
     * @param field the field to validate
     * @param fieldName the name of the field to include in the exception error message
     */
    public static void checkFieldIsString(Object field, String fieldName) {
        if (!(field instanceof String)) {
            throw new IllegalArgumentException("syntax error, wrong type for value " + field +
                    " in key `" + fieldName + "`, expected a String value.");
        }
    }

    /**
     * Helper method that performs a Depth-First Search (DFS) to detect cycles in the job
     * dependency graph.
     * It traverses through the graph and checks if a job’s dependencies form a cycle.
     *
     * @param jobName the current job being checked
     * @param graph   the job dependency graph
     * @param visited a set of already visited jobs
     * @param stack   a set to track the current stack of jobs being processed
     * @param cycle   a set to hold the job names forming the cycle
     * @param sorted  the list of sorted jobs
     * @param jobMap  map of job names to jobs
     * @return true if a cycle is detected, false otherwise
     */
    static boolean dfs(
            String jobName,
            Map<String, List<String>> graph,
            Set<String> visited,
            Set<String> stack,
            Set<String> cycle,
            List<Job> sorted,
            Map<String,Job> jobMap
    ) {
        stack.add(jobName);
        cycle.add(jobName);

        for (String dependency : graph.getOrDefault(jobName, Collections.emptyList())) {
            // Cycle Detection: Dependency already in stack -> cycle found
            if (stack.contains(dependency)) {
                cycle.add(dependency);
                return true;
            }

            // If dependency not visited, recurse
            if (!visited.contains(dependency) &&
                    dfs(dependency, graph, visited, stack, cycle, sorted, jobMap)) {
                return true;
            }
        }
        visited.add(jobName);
        stack.remove(jobName);

        // Topological Sort: Add job after processing dependencies
        Job job = jobMap.get(jobName);
        if (job != null) {
            sorted.add(job);
        }

        return false;
    }

    /**
     * Checks if there are any cyclical dependencies in the job dependency graph.
     * It uses Depth-First Search (DFS) to traverse the graph and detect cycles.
     *
     * @param jobs a list of jobs to analyze for cyclic dependencies
     * @return a set of job names that constitute a cycle, or an empty set if no cycle is detected
     */
    public static Set<String> hasCyclicDependency(List<Job> jobs) {
        Map<String, List<String>> graph = buildJobGraph(jobs);

        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();
        Set<String> cycle = new HashSet<>();
        Map<String,Job> jobMap = jobs.stream()
                .collect(Collectors.toMap(Job::getName, job -> job));

        // Run DFS for each job
        for (Job job : jobs) {
            if (!visited.contains(job.getName())) {
                if (dfs(job.getName(), graph, visited, stack, cycle, new ArrayList<>(), jobMap)) {
                    return cycle; // Cycle detected
                }
            }
        }

        return Collections.emptySet(); // No cycle found
    }

    /**
     * Builds a job dependency graph where each job is mapped to a list of its dependent jobs.
     * The graph represents job names as keys and their dependencies as values.
     *
     * @param jobs a list of jobs to analyze
     * @return a map representing the job dependency graph, where keys are job names and values are
     * lists of dependent job names
     */
    static Map<String, List<String>> buildJobGraph(List<Job> jobs) {
        Map<String, List<String>> graph = new HashMap<>();

        for (Job job : jobs) {
            String jobName = job.getName();
            List<String> dependencies = job.getNeeds();
            graph.put(jobName, dependencies);
        }

        return graph;
    }

    /**
     * Recursively deletes the files
     * (except local_artifacts - default mount for artifacts)
     * @param file The file object that needs to be deleted.
     * @throws java.io.IOException if artifact file fails to be deleted
     */
    public static void deleteFile(File file) throws IOException {
        if (file.isDirectory()) {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    deleteFile(f);
                }
            }
        }

        if (!file.delete() && !file.getName().equals(Constants.LOCAL_ARTIFACTS_DIRECTORY)) {
            throw new IOException("Failed to delete Artifacts file: " + file.getAbsolutePath());
        }
    }
}