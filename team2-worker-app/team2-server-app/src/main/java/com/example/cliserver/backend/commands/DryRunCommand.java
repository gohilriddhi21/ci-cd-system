package com.example.cliserver.backend.commands;

import com.example.cliserver.backend.model.Job;
import com.example.cliserver.backend.model.PipelineConfig;
import com.example.cliserver.backend.model.ValidationResult;
import com.example.cliserver.backend.utils.ConfigurationValidator;
import com.example.cliserver.backend.utils.PipelineUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Executes a dry run of the pipeline configuration, printing the job execution order.
 */
public class DryRunCommand {

    /**
     * Runs a dry run on the provided pipeline configuration file.
     *
     * @param filename The path to the pipeline configuration file.
     * @param repo The url of the remote repo where the pipeline configuration file is, or null if
     *             local
     * @param branch The branch to check out for the given repo
     * @param commit The commit hash to check out for the given repo
     * @return A string representing the job execution order or an error message if the validation
     * fails.
     */
    public static String runDry(String filename, String repo, String branch, String commit) {
        try {
            StringBuilder runDryResult = new StringBuilder("Filename: " + filename + "\n");

            ConfigurationValidator configurationValidator = new ConfigurationValidator();
            // Validate the pipeline configuration
            ValidationResult validationResult = configurationValidator.validateYaml(filename, repo,
                    branch, commit);
            if (!validationResult.isValid()) {
                runDryResult.append(validationResult.getErrorMessage());
                return runDryResult.toString();
            }

            // Load and process the pipeline
            PipelineConfig config = validationResult.getConfig();
            if (config == null) {
                runDryResult.append(filename + "Failed to load pipeline configuration.");
                return runDryResult.toString();
            }

            // Build the pipeline execution order
            runDryResult.append(buildYamlOutput(config.getPipeline(), configurationValidator));
            return runDryResult.toString();

        } catch (Exception e) {
            return filename + ": " + e.getMessage();
        }
    }

    /**
     * Builds a YAML-like representation of the pipeline execution order using shared logic.
     *
     * @param pipeline The pipeline configuration containing jobs and stages.
     * @param configurationValidator The configuration validator to use for this pipeline.
     * @return A string representation of the job execution order.
     */
    private static String buildYamlOutput(PipelineConfig.Pipeline pipeline,
                                          ConfigurationValidator configurationValidator) {
        StringBuilder yaml = new StringBuilder();

        // Track visited jobs to avoid re-processing
        Set<String> visited = new HashSet<>();

        // Process each stage in order
        for (String stage : pipeline.getStages()) {
            yaml.append(stage).append(":\n");

            // Get topologically sorted jobs for this stage
            List<Job> stageJobs = pipeline.getJobs().stream()
                    .filter(job -> job.getStage().equals(stage))
                    .toList();

            List<Job> sortedJobs = PipelineUtils.getTopologicallySortedJobs(stageJobs);

            // Print jobs in the correct order
            for (Job job : sortedJobs) {
                printJobDetails(job, visited, yaml);
            }
        }

        return yaml.toString();
    }

    /**
     * Prints job details in YAML format.
     *
     * @param job     The job to print.
     * @param visited A set of visited jobs to avoid duplication.
     * @param yaml    The StringBuilder to append job information.
     */
    private static void printJobDetails(Job job,
                                        Set<String> visited,
                                        StringBuilder yaml) {

        // If already visited, avoid reprocessing
        if (visited.contains(job.getName())) return;

        yaml.append("  ").append(job.getName()).append(":\n")
                .append("    image: ").append(job.getImage()).append("\n")
                .append("    script:\n");
        job.getScript().forEach(cmd -> yaml.append("      - ").append(cmd).append("\n"));

        if (job.getNeeds() != null && !job.getNeeds().isEmpty()) {
            yaml.append("    needs:\n");
            job.getNeeds().forEach(dep -> yaml.append("      - ").append(dep).append("\n"));
        }

        visited.add(job.getName());
    }
}
